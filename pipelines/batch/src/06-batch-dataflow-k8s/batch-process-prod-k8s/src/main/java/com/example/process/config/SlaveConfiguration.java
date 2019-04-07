package com.example.process.config;

import com.example.process.batch.PersonEnrichProcessor;
import com.example.process.mapper.RecordFieldSetMapper;
import com.example.process.model.Customer;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.task.batch.partition.DeployerStepExecutionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
@Profile("worker")
@Configuration
public class SlaveConfiguration {

    @Value("${batch.max-threads:4}")
    private int maxThreads;

    @Value("${batch.tempPath:/tmp/data}")
    String tempPath;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    MinioClient client;

    @Bean
    public DeployerStepExecutionHandler stepExecutionHandler(ApplicationContext context,
                                                             JobExplorer jobExplorer,
                                                             JobRepository jobRepository) {
        return new DeployerStepExecutionHandler(context, jobExplorer, jobRepository);
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> reader(@Value("#{stepExecutionContext['fileName']}") String source)
            throws Exception {
        log.info("Slave processing the file: " + source );

        String[] parts = source.split(":");
        String bucketName = parts[0];
        String objectName = parts[1];

        log.debug("Bucket Name: " + bucketName);
        log.debug("Object Name: " + objectName);

        client.statObject(bucketName, objectName);

        String slaveTempPath = tempPath + "/slave_data";
        File destDir = new File(slaveTempPath);

        log.info("Creating the temp directory.");

        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new Exception("Folder cannot be created: " + slaveTempPath);
        }

        log.info("Fetching the file to process locally.");

        InputStream inStream = client.getObject(bucketName, objectName);

        final String filename = parts[1].split("/")[parts[1].split("/").length - 1];
        File targetFile = new File(slaveTempPath + "/" + filename);
        OutputStream outStream = new FileOutputStream(targetFile);

        byte[] buffer = new byte[16 * 1024];
        int bytesRead;
        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        inStream.close();
        outStream.close();

        Resource resource = resourceLoader.getResource("file:"  + targetFile);

        return new FlatFileItemReaderBuilder<Customer>()
                .name("personReader")
                .resource(resource)
                .delimited()
                .names(new String[]{
                        "id",
                        "firstName",
                        "lastName",
                        "fullName",
                        "title",
                        "email",
                        "phone",
                        "birth",
                        "address",
                        "street",
                        "city",
                        "zipCode",
                        "country",
                        "state",
                        "company",
                        "creditCardNumber",
                        "jobTitle",
                        "department",
                        "startDate",
                        "endDate"})
                .fieldSetMapper(new RecordFieldSetMapper())
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Customer> writer(@Qualifier("secondDataSource") DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO customer (first_name, last_name, full_name, title, email, phone_number, birth_date, address, street_name, city, country, " +
                        "state, zip_code, company_name, credit_card, job_title, department, start_date, end_date, group_name, update_time) " +
                        "VALUES (:firstName, :lastName, :fullName, :title, :email, :phone, :birth, :address, :street, :city, :country, :state, " +
                        ":zipCode, :company, :creditCardNumber, :jobTitle, :department, :startDate, :endDate, :groupName, :updateTime)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Step slaveStep(StepBuilderFactory stepBuilderFactory,
                          PersonEnrichProcessor processor) throws Exception {
        return stepBuilderFactory.get("slaveStep")
                .<Customer, Customer>chunk(10)
                .reader(reader(null))
                .processor(processor)
                .writer(writer(null))
                //.taskExecutor(taskExecutor())
                //.throttleLimit(20)
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(maxThreads);
        //taskExecutor.setCorePoolSize(maxThreads);
        //taskExecutor.setQueueCapacity(maxThreads);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

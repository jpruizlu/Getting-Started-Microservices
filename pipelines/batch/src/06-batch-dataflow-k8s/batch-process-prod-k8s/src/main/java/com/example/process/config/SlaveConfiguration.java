package com.example.process.config;

import com.example.process.batch.PersonEnrichProcessor;
import com.example.process.listener.SlaveChunkListener;
import com.example.process.listener.SlaveStepListener;
import com.example.process.mapper.RecordFieldSetMapper;
import com.example.process.model.Customer;
import com.example.process.utils.ChaosMonkey;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.task.batch.partition.DeployerStepExecutionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Slf4j
@Profile("worker")
@Configuration
public class SlaveConfiguration {

    @Value("${batch.max-threads:4}")
    private int maxThreads;

    @Value("${batch.slaveWriterFailurePercentage:0}")
    private int slaveWriterFailurePercentage;

    @Value("${batch.slaveReaderFailurePercentage:0}")
    private int slaveReaderFailurePercentage;

    @Bean
    public DeployerStepExecutionHandler stepExecutionHandler(ApplicationContext context,
                                                             JobExplorer jobExplorer,
                                                             JobRepository jobRepository) {
        return new DeployerStepExecutionHandler(context, jobExplorer, jobRepository);
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> reader(@Value("#{stepExecutionContext['resourceFile']}") Resource resource)
            throws Exception {
        log.info("Slave processing the file: " + resource.getFilename());
        // Check whether chaos monkey must be activated - AOP
        ChaosMonkey.check("slaveReaderFailurePercentage", slaveReaderFailurePercentage);
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
    public JdbcBatchItemWriter<Customer> writer(@Qualifier("secondDataSource") DataSource dataSource) throws Exception {
        // Check whether chaos monkey must be activated - AOP
        ChaosMonkey.check("slaveWriterFailurePercentage", slaveWriterFailurePercentage);
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
                          PersonEnrichProcessor processor,
                          SlaveStepListener slaveListener,
                          SlaveChunkListener chunkListener) throws Exception {
        return stepBuilderFactory.get("slaveStep")
                .<Customer, Customer>chunk(10)
                .reader(reader(null))
                .processor(processor)
                .writer(writer(null))
                //.taskExecutor(taskExecutor())
                //.throttleLimit(20)
                .listener(slaveListener)
                .listener(chunkListener)
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

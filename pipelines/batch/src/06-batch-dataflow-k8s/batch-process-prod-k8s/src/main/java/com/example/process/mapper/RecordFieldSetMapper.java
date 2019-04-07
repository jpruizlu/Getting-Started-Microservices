package com.example.process.mapper;

import com.example.process.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.text.SimpleDateFormat;

@Slf4j
public class RecordFieldSetMapper implements FieldSetMapper<Customer> {

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

    public Customer mapFieldSet(FieldSet fieldSet) throws BindException {
        try {
            return Customer.builder()
                    .id(fieldSet.readString("id"))
                    .firstName(fieldSet.readString("firstName"))
                    .lastName(fieldSet.readString("lastName"))
                    .fullName(fieldSet.readString("fullName"))
                    .title(fieldSet.readString("title"))
                    .email(fieldSet.readString("email"))
                    .phone(fieldSet.readString("phone"))
                    .birth(dateFormat.parse(fieldSet.readString("birth")))
                    .address(fieldSet.readString("address"))
                    .street(fieldSet.readString("street"))
                    .city(fieldSet.readString("city"))
                    .zipCode(fieldSet.readString("zipCode"))
                    .country(fieldSet.readString("country"))
                    .state(fieldSet.readString("state"))
                    .company(fieldSet.readString("company"))
                    .creditCardNumber(fieldSet.readString("creditCardNumber"))
                    .jobTitle(fieldSet.readString("jobTitle"))
                    .department(Integer.parseInt(fieldSet.readString("department")))
                    .startDate(dateFormat.parse(fieldSet.readString("startDate")))
                    .endDate(dateFormat.parse(fieldSet.readString("endDate")))
                    .build();
        } catch (Exception ex) {
            throw new BindException(Customer.builder().build(), "Customer");
        }
    }
}

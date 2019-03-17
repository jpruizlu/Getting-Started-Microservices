package com.example.batch.batch;

import com.example.batch.model.Department;
import com.example.batch.model.Person;
import com.example.batch.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Optional;

@Slf4j
@Component
public class PersonEnrichProcessor implements ItemProcessor<Person, Person> {

    @Autowired
    DepartmentService deparmentService;

    @Recover
    public Person fallback(final Person person) {
        final Person transformedPerson = new Person(person.getFirstName().toUpperCase(),
                person.getLastName().toUpperCase(),
                "Fatal Error retrieving the department",
                Calendar.getInstance().getTime());
        log.info("From Fallback, converting (" + person + ") into (" + transformedPerson + ")");
        return transformedPerson;
    }

    @Override
    @CircuitBreaker(maxAttempts = 2, resetTimeout= 20000, openTimeout = 5000)
    public Person process(final Person person) throws Exception {
        Optional<Department> dep = deparmentService.getById(person.getDepartmentId());
        if (dep.isPresent()) {
             if (dep.get().getName().toUpperCase().equals("UNKNOWN")) {
                 log.info("Filtering (" + person + ")");
                return null;
            }
            log.info("From Rest Service; " + dep.get().getName());
        } else {
            dep = Optional.of(new Department(0, "Error retrieving the department"));
        }
        final Person transformedPerson = new Person(person.getFirstName().toUpperCase(),
                person.getLastName().toUpperCase(),
                dep.get().getName(),
                Calendar.getInstance().getTime());
        log.info("Converting (" + person + ") into (" + transformedPerson + ")");
        return transformedPerson;
    }

}

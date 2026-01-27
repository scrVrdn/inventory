package io.github.scrvrdn.inventory.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.dto.Person;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class PersonRepositoryIntegrationTests {

    private final JdbcTemplate jdbcTemplate;
    private final PersonRepository underTest;

    @Autowired
    public PersonRepositoryIntegrationTests(final JdbcTemplate jdbcTemplate, final PersonRepository underTest) {
        this.jdbcTemplate = jdbcTemplate;
        this.underTest = underTest;
    }

    @BeforeEach
    public void setup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "persons");
    }

    @Test
    public void testThatPersonCanBeCreatedAndRetrieved() {
        Person person = TestDataUtil.createTestPerson();
        underTest.create(person);

        Optional<Person> result = underTest.findById(person.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(person);
    }

    @Test
    public void testThatNoDuplicateIsCreated() {
        Person person1 = TestDataUtil.createTestPerson();
        underTest.create(person1);

        Person person2 = TestDataUtil.createTestPerson();
        underTest.create(person2);

        Optional<Person> result = underTest.findById(person2.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(person1);
    }

    @Test
    public void testThatAllPersonsCanBeCreated() {
        Person person1 = TestDataUtil.createTestPerson();
        Person person2 = TestDataUtil.createTestPerson2();
        Person person3 = TestDataUtil.createTestPerson3();
        Person person4 = TestDataUtil.createTestPerson4();
        underTest.createAll(List.of(person1, person2, person3, person4));

        List<Person> result = underTest.findAll();
        assertThat(result)
                .hasSize(4)
                .containsExactly(person1, person2, person3, person4);
    }

    

    

    @Test
    public void testThatAllPersonsCanBeRetrieved() {
        Person person1 = TestDataUtil.createTestPerson();
        underTest.create(person1);

        Person person2 = TestDataUtil.createTestPerson2();
        underTest.create(person2);

        Person person3 = TestDataUtil.createTestPerson3();
        underTest.create(person3);

        List<Person> result = underTest.findAll();
        assertThat(result)
            .hasSize(3)
            .containsExactly(person1, person2, person3);
    }

    

    @Test
    public void testThatPersonCanBeUpdated() {
        Person person = TestDataUtil.createTestPerson();
        underTest.create(person);
        person.setLastName("UPDATED");
        person.setFirstNames("UPDATED");
        
        underTest.update(person);
        Optional<Person> result = underTest.findById(person.getId());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(person);
    }

    @Test
    public void testThatPersonCanBeDeleted() {
        Person person = TestDataUtil.createTestPerson();
        underTest.create(person);

        underTest.delete(person.getId());

        Optional<Person> result = underTest.findById(person.getId());
        assertThat(result).isEmpty();
    }    
}

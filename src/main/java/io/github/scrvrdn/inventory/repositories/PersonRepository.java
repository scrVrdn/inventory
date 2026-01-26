package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.Person;

public interface PersonRepository {

    void create(Person person);

    void createAll(List<Person> persons);
    
    Optional<Person> findById(long id);

    List<Person> findAll();

    void update(Person person);

    void delete(long id);
}

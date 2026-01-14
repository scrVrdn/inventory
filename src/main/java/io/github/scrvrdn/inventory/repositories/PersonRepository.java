package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.scrvrdn.inventory.domain.Person;

public interface PersonRepository {

    void create(Person person);

    void createAll(List<Person> persons);
    
    Optional<Person> findById(long id);

    Map<Long, List<Person>> findAuthorsGroupedByBookId();

    Map<Long, List<Person>> findEditorsGroupedByBookId();

    List<Person> findAll();

    void update(Person person);

    void delete(long id);
}

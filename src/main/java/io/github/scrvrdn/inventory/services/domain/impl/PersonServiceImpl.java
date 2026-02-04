package io.github.scrvrdn.inventory.services.domain.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.repositories.PersonRepository;
import io.github.scrvrdn.inventory.services.domain.PersonService;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    public PersonServiceImpl(final PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public List<Long> updateAuthorsByName(List<Person> authors) {
        personRepository.createAll(authors);
        return authors.stream().map(Person::getId).distinct().collect(Collectors.toList());         
    }

    @Override
    public List<Long> updateEditorsByName(List<Person> editors) {
        personRepository.createAll(editors);
        return editors.stream().map(Person::getId).distinct().collect(Collectors.toList());
    }
}

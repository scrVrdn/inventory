package io.github.scrvrdn.inventory.services;

import java.util.List;

import io.github.scrvrdn.inventory.dto.Person;

public interface PersonService {

    List<Long> updateAuthorsByName(List<Person> authors);
    List<Long> updateEditorsByName(List<Person> editors);
}

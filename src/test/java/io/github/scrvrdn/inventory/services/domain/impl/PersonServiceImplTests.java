package io.github.scrvrdn.inventory.services.domain.impl;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.repositories.PersonRepository;

@ExtendWith(MockitoExtension.class)
public class PersonServiceImplTests {
    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonServiceImpl underTest;

    @Test
    public void testThatUpdateAuthorsByNameCallsPersonRepository() {
        Person author1 = TestDataUtil.createTestPerson();
        Person author2 = TestDataUtil.createTestPerson2();
        Person author3 = TestDataUtil.createTestPerson3();
        
        List<Person> authors = List.of(author1, author2, author3);
        
        underTest.updateAuthorsByName(authors);

        verify(personRepository).createAll(authors);
    }

    @Test
    public void testThatUpdateEditorsByNameCallsPersonRepository() {
        Person editor1 = TestDataUtil.createTestPerson();
        Person editor2 = TestDataUtil.createTestPerson2();
        Person editor3 = TestDataUtil.createTestPerson3();
        
        List<Person> editors = List.of(editor1, editor2, editor3);
        
        underTest.updateEditorsByName(editors);

        verify(personRepository).createAll(editors);
    }
}

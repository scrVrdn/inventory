package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Map;

import io.github.scrvrdn.inventory.dto.Person;

public interface BookPersonRepository {

    void assignAuthorsToBook(long bookId, List<Long> authorIds);

    List<Person> findAuthorsByBookId(long bookId);

    List<Long> findAuthorIdsByBookId(long bookId);

    Map<Long, List<Person>> findAllAuthorsGroupedByBookId();

    void removeAuthorsFromBook(long bookId, List<Long> authorIds);


    void assignEditorsToBook(long bookId, List<Long> editorIds);

    List<Person> findEditorsByBookId(long bookId);

    List<Long> findEditorIdsByBookId(long bookId);

    Map<Long, List<Person>> findAllEditorsGroupedByBookId();

    void removeEditorsFromBook(long bookId, List<Long> editorIds);
}

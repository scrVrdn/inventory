package io.github.scrvrdn.inventory.repositories;

import java.util.Map;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.Publisher;

public interface BookPublisherRepository {

    void assignPublisherToBook(long bookId, long publisherId);

    void removePublisherFromBook(long bookId, long publisherId);

    Optional<Publisher> findPublisherByBookId(long bookId);

    Map<Long, Publisher> findPublishersGroupedByBookId();

}

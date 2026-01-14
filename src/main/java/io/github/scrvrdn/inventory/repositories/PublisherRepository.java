package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.scrvrdn.inventory.domain.Publisher;

public interface PublisherRepository {

    void create(Publisher publisher);

    Optional<Publisher> findById(long id);

    List<Publisher> findAll();

    Map<Long, Publisher> findPublishersGroupedByBookId();

    void update(Publisher publisher);

    void delete(long id);
}

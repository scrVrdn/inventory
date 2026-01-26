package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.Publisher;

public interface PublisherRepository {

    void create(Publisher publisher);

    Optional<Publisher> findById(long id);

    List<Publisher> findAll();

    void update(Publisher publisher);

    void delete(long id);

}

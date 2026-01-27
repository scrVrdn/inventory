package io.github.scrvrdn.inventory.services.impl;

import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.dto.Publisher;
import io.github.scrvrdn.inventory.repositories.PublisherRepository;
import io.github.scrvrdn.inventory.services.PublisherService;

@Service
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;

    public PublisherServiceImpl(final PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    @Override
    public Long updatePublisherByNameAndLocation(Publisher publisher) {
        if (publisher.getName() != null || publisher.getLocation() != null) publisherRepository.create(publisher);
        return publisher.getId();
    }
}

package io.github.scrvrdn.inventory.services;

import io.github.scrvrdn.inventory.dto.Publisher;

public interface PublisherService {
    
    Long updatePublisherByNameAndLocation(Publisher publisher);
}

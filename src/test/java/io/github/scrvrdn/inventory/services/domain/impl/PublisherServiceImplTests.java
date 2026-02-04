package io.github.scrvrdn.inventory.services.domain.impl;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.scrvrdn.inventory.TestDataUtil;
import io.github.scrvrdn.inventory.dto.Publisher;
import io.github.scrvrdn.inventory.repositories.PublisherRepository;

@ExtendWith(MockitoExtension.class)
public class PublisherServiceImplTests {

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private PublisherServiceImpl underTest;

    @Test
    public void testThatUpdatePublisherByNameAndLocationCallsPublisherRepository()  {
        Publisher publisher = TestDataUtil.createTestPublisher();
        
        underTest.updatePublisherByNameAndLocation(publisher);

        verify(publisherRepository).create(publisher);
    }
}

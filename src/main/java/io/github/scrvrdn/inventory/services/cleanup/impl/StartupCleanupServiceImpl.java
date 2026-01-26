package io.github.scrvrdn.inventory.services.cleanup.impl;


import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import io.github.scrvrdn.inventory.events.StageReadyEvent;
import io.github.scrvrdn.inventory.services.cleanup.PersonCleanupService;
import io.github.scrvrdn.inventory.services.cleanup.PublisherCleanupService;

@Service
public class StartupCleanupServiceImpl implements ApplicationListener<StageReadyEvent> {

    private final PersonCleanupService personCleanupService;
    private final PublisherCleanupService publisherCleanupService;

    public StartupCleanupServiceImpl(final PersonCleanupService personCleanupService, final PublisherCleanupService publisherCleanupService) {
        this.personCleanupService = personCleanupService;
        this.publisherCleanupService = publisherCleanupService;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        personCleanupService.cleanupUnusedPersons();
        publisherCleanupService.cleanupUnusedPublishers();
    };
}

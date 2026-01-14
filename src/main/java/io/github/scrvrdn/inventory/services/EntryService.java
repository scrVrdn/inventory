package io.github.scrvrdn.inventory.services;


import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.EntryDto;
import io.github.scrvrdn.inventory.dto.EntryRow;


public interface EntryService {

    void create(EntryDto entry);

    List<EntryDto> findAll();

    Optional<EntryRow> getEntryRow(long bookId);

    List<EntryRow> getAllEntryRows();

    void update(EntryDto entry);
}

package io.github.scrvrdn.inventory.services.facade;


import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;


public interface EntryService {

    Optional<FlatEntryDto> createEmptyEntry();

    List<FullEntryDto> findAll();

    Optional<FullEntryDto> findById(long id);

    Optional<FlatEntryDto> getFlatEntryDto(long bookId);

    List<FlatEntryDto> getFlatEntryDtos(long fromRow, int numberOfEntries);

    List<FlatEntryDto> getAllFlatEntryDtos();

    FlatEntryDto update(FullEntryDto entry);

    void delete(long bookId);

    int numberOfRows();
}

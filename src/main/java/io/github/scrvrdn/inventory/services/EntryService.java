package io.github.scrvrdn.inventory.services;


import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;


public interface EntryService {

    Optional<FlatEntryDto> createEmptyEntry();

    void create(FullEntryDto entry);

    List<FullEntryDto> findAll();

    Optional<FullEntryDto> findById(long id);

    Optional<FlatEntryDto> getFlatEntryDto(long bookId);

    List<FlatEntryDto> getAllFlatEntryDtos();

    void update(FullEntryDto entry);
}

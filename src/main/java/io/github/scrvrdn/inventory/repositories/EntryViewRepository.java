package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import io.github.scrvrdn.inventory.dto.FullEntryDto;

public interface EntryViewRepository {
    List<FullEntryDto> findAll();

    Optional<FullEntryDto> findById(long id);

    Optional<FlatEntryDto> getFlatEntryDtoByBookId(long bookId);

    Optional<FlatEntryDto> getNextFlatEntryDtoAfterBookId(long bookId);

    List<FlatEntryDto> getFlatEntryDtos(int numberOfEntries, int fromRow);

    List<FlatEntryDto> getAllFlatEntryDtos();
}

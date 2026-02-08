package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Page;

public interface EntryViewRepository {
    List<FullEntryDto> findAll();

    Optional<FullEntryDto> findById(long id);

    Optional<FlatEntryDto> getFlatEntryDtoByBookId(long bookId);

    Optional<FlatEntryDto> getNextFlatEntryDtoAfterBookId(long bookId);

    List<FlatEntryDto> getFlatEntryDtos(int numberOfEntries, int fromRow);

    public Page getSortedAndFilteredEntries(int numberOfEntries, int pageIndex, String sortBy, String[] searchString);

    List<FlatEntryDto> getAllFlatEntryDtos();
}

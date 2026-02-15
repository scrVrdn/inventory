package io.github.scrvrdn.inventory.repositories;

import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Page;
import io.github.scrvrdn.inventory.dto.PageRequest;

public interface EntryViewRepository {
    List<FullEntryDto> findAll();

    Optional<FullEntryDto> findById(long id);

    Optional<FlatEntryDto> getFlatEntryDtoByBookId(long bookId);

    Optional<FlatEntryDto> getNextFlatEntryDtoAfterBookId(long bookId);

    List<FlatEntryDto> getFlatEntryDtos(int pageSize, int fromRow);

    Page getSortedAndFilteredEntries(PageRequest request);

    List<FlatEntryDto> getSortedEntries(PageRequest request);

    int findRow(long bookId, PageRequest request);

    List<FlatEntryDto> getAllFlatEntryDtos();
}

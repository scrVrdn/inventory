package io.github.scrvrdn.inventory.services.facade;


import java.util.List;
import java.util.Optional;

import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Page;
import io.github.scrvrdn.inventory.dto.PageRequest;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;


public interface EntryService {

    Optional<FlatEntryDto> createEmptyEntry();

    List<FullEntryDto> findAll();

    Optional<FullEntryDto> findById(long id);

    Optional<FlatEntryDto> getFlatEntryDtoByBookId(long bookId);

    Optional<FlatEntryDto> getNextFlatEntryDtoAfterBookId(long bookId);

    List<FlatEntryDto> getFlatEntryDtos(int pageSize, int fromRow);

    Page getPage(PageRequest request);

    Page getPageWithBook(long bookId, PageRequest request);

    List<FlatEntryDto> getAllFlatEntryDtos();

    FlatEntryDto update(FullEntryDto entry);

    void delete(long bookId);

    int numberOfRows();
}

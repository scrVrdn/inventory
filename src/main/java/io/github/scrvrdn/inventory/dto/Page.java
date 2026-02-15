package io.github.scrvrdn.inventory.dto;

import java.util.List;

public record Page(List<FlatEntryDto> entries, int pageIndex, int totalNumberOfRows) {}

package io.github.scrvrdn.inventory.dto;

public record PageRequest(int pageIndex, int pageSize, String searchString, String sortBy, String sortDir, boolean caseInsensitive) {
    
    public PageRequest {
        sortBy = sortBy == null ? "title" : sortBy;
        sortDir = sortDir == null ? "ASC" : sortDir;
    }
}
package io.github.scrvrdn.inventory.dto;

public record PublisherDto(Long id, String name, String location) {


    @Override
    public String toString() {
        if (name == null && location == null) return "";
        if (location == null) return name;
        if (name == null) return location;

        return location + ": " + name;
    }
}

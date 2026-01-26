package io.github.scrvrdn.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Publisher {
    
    private Long id;
    private String name;
    private String location;

    @Override
    public String toString() {
        if (name == null && location == null) return "";
        if (location == null) return name;
        if (name == null) return location;

        return location + ": " + name;
    }
}

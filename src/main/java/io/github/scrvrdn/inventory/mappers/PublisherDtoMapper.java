package io.github.scrvrdn.inventory.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.dto.PublisherDto;

@Component
public class PublisherDtoMapper implements RowMapper<PublisherDto> {

    @Override
    public PublisherDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PublisherDto(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("location"));
    }
    

}

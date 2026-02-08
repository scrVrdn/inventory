package io.github.scrvrdn.inventory.repositories.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Repository;

import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Page;
import io.github.scrvrdn.inventory.mappers.EntryDtoExtractor;
import io.github.scrvrdn.inventory.mappers.EntryDtoListExtractor;
import io.github.scrvrdn.inventory.mappers.FlatEntryDtoRowMapper;
import io.github.scrvrdn.inventory.repositories.EntryViewRepository;

@Repository
public class EntryViewRepositoryImpl implements EntryViewRepository {

    private final JdbcTemplate jdbcTemplate;
    private final EntryDtoExtractor entryDtoExtractor;
    private final EntryDtoListExtractor entryDtoListExtractor;
    private final FlatEntryDtoRowMapper flatEntryDtoRowMapper;

    public EntryViewRepositoryImpl(final JdbcTemplate jdbcTemplate, final EntryDtoExtractor entryDtoExtractor, final FlatEntryDtoRowMapper flatEntryDtoRowMapper, final EntryDtoListExtractor entryDtoListExtractor) {
        this.jdbcTemplate = jdbcTemplate;
        this.entryDtoExtractor = entryDtoExtractor;
        this.entryDtoListExtractor = entryDtoListExtractor;
        this.flatEntryDtoRowMapper = flatEntryDtoRowMapper;
    }

    public List<FullEntryDto> findAll() {
        String query = """
                SELECT
                    b."id" AS "id", b."title", b."year", b."isbn10", b."isbn13", b."shelf_mark",
                    a."id" AS "author_id", a."last_name" AS "author_last_name", a."first_names" AS "author_first_names",
                    e."id" AS "editor_id", e."last_name" AS "editor_last_name", e."first_names" AS "editor_first_names",
                    "publishers"."id" AS "publisher_id", "publishers"."location" AS "publisher_location", "publishers"."name" AS "publisher_name"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" a ON "book_person"."person_id" = a."id" AND "book_person"."role" = 'AUTHOR'
                LEFT JOIN "persons" e ON "book_person"."person_id" = e."id" AND "book_person"."role" = 'EDITOR'
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                ORDER BY b."id", "book_person"."role", "book_person"."order_index";
                """;
        return jdbcTemplate.query(query, entryDtoListExtractor);
    }

    public Optional<FullEntryDto> findById(long id) {
          String query = """
                SELECT
                    b."id" AS "id", b."title", b."year", b."isbn10", b."isbn13", b."shelf_mark",
                    a."id" AS "author_id", a."last_name" AS "author_last_name", a."first_names" AS "author_first_names",
                    e."id" AS "editor_id", e."last_name" AS "editor_last_name", e."first_names" AS "editor_first_names",
                    "publishers"."id" AS "publisher_id", "publishers"."location" AS "publisher_location", "publishers"."name" AS "publisher_name"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" a ON "book_person"."person_id" = a."id" AND "book_person"."role" = 'AUTHOR'
                LEFT JOIN "persons" e ON "book_person"."person_id" = e."id" AND "book_person"."role" = 'EDITOR'
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                WHERE b."id" = ?
                ORDER BY "book_person"."role", "book_person"."order_index";
                """;

        FullEntryDto entry = jdbcTemplate.query(query, entryDtoExtractor, id);
        return Optional.ofNullable(entry);
    }

    public Optional<FlatEntryDto> getFlatEntryDtoByBookId(long bookId) {
        String query = """
                SELECT 
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                WHERE b."id" = ?
                GROUP BY b."id";
                """;
                
        List<FlatEntryDto> result = jdbcTemplate.query(query, flatEntryDtoRowMapper, bookId);
        return result.stream().findFirst();
    }

    public Optional<FlatEntryDto> getNextFlatEntryDtoAfterBookId(long bookId) {
        String query = """
                SELECT
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"    
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                WHERE b."id" > ?
                GROUP BY b."id"
                ORDER BY b."id"
                LIMIT 1;
                """;

        return jdbcTemplate.query(query, flatEntryDtoRowMapper, bookId).stream().findFirst();
    }

    public List<FlatEntryDto> getFlatEntryDtos(int numberOfEntries, int fromRow) {
        String query = """
                SELECT
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                GROUP BY b."id"
                ORDER BY b."id"
                LIMIT ?
                OFFSET ?;
                """;

        return jdbcTemplate.query(query, flatEntryDtoRowMapper, numberOfEntries, fromRow);
    }

    @Override
    public Page getSortedAndFilteredEntries(int pageSize, int pageIndex, String sortBy, String[] searchString) {
        String querySELECT = """
                SELECT
                    b."id", b."title" AS "title", b."year" AS "year", b."shelf_mark" AS "shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                """;
                        
        String queryFROM = """
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                GROUP by b."id"
                """;
       
        List<Object> params = new ArrayList<>();

        StringBuilder filterSql = new StringBuilder("HAVING 1 = 1");
        if (searchString != null) {
            for (String str : searchString) {
                filterSql.append("""
                             AND (
                                "title" LIKE ?
                                OR "year" LIKE ?
                                OR "shelf_mark" LIKE ?
                                OR "authors" LIKE ?
                                OR "editors" LIKE ?
                                OR "publisher" LIKE ?
                                OR "isbn10" LIKE ?
                                OR "isbn13" LIKE ?
                            )""");

                            str = "%" + str + "%";
                            int n = 8;
                            while (n > 0) {
                                params.add(str);
                                n--;
                            }
            }
        }

        // StringBuilder countQuery = new StringBuilder("SELECT COUNT(*) ");
        // countQuery.append(queryFROM);
        // countQuery.append(queryFilter);
        // countQuery.append(";");

        // System.out.println(countQuery.toString());
        int totalNumberOfRows = getNumberOfRowsAfterFiltering(filterSql, params);
        // int totalNumberOfRows = jdbcTemplate.queryForObject(countQuery.toString(), Integer.class, params.toArray());
        //int totalNumberOfRows = 10;
        sortBy = (sortBy != null) ? sortBy : "b.\"id\"";
        StringBuilder dataSql = new StringBuilder(querySELECT)
                                    .append(queryFROM)
                                    .append(filterSql)
                                    .append(" ORDER BY ")
                                    .append(sortBy)
                                    .append("\nLIMIT ? OFFSET ?;\n");
        
        params.add(pageSize);
        params.add(pageIndex * pageSize);
        
        System.out.println(dataSql.toString());
        List<FlatEntryDto> data = jdbcTemplate.query(dataSql.toString(), flatEntryDtoRowMapper, params.toArray());

        return new Page(data, pageIndex, pageSize, totalNumberOfRows);
    }

    
    private int getNumberOfRowsAfterFiltering(StringBuilder filterSql, List<Object> params) {
        String querySql = """
                SELECT COUNT(*),
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; '
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; '
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                GROUP by b."id"
        """;

        StringBuilder query = new StringBuilder("""
        SELECT COUNT(*),
            GROUP_CONCAT(
                CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; '
            ) AS "authors",
            GROUP_CONCAT(
                CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; '
            ) AS "editors",
            CASE
                WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                THEN NULL
                ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
            END AS "publisher"
        FROM "books" b
        LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
        LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
        LEFT JOIN "published" ON b."id" = "published"."book_id"
        LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
        GROUP by b."id"
        """);
                        
                        
        
        query.append(filterSql).append(";");

        return jdbcTemplate.queryForObject(query.toString(),
        (rs, rowNum) -> {
            return rs.getInt("COUNT(*)");
        },
        params.toArray());
    }

    @Override
    public List<FlatEntryDto> getAllFlatEntryDtos() {
        String query = """
                SELECT
                    b."id", b."title", b."year", b."shelf_mark",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'AUTHOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "authors",
                    GROUP_CONCAT(
                        CASE WHEN "book_person"."role" = 'EDITOR' THEN CONCAT_WS(', ', p."last_name", p."first_names") END, '; ' ORDER BY "book_person"."order_index"
                    ) AS "editors",
                    CASE
                        WHEN "publishers"."location" IS NULL AND "publishers"."name" IS NULL
                        THEN NULL
                        ELSE CONCAT_WS(': ', "publishers"."location", "publishers"."name")
                    END AS "publisher"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                GROUP BY b."id";
                """;

        return jdbcTemplate.query(query, flatEntryDtoRowMapper);
    }
}

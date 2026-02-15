package io.github.scrvrdn.inventory.repositories.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Page;
import io.github.scrvrdn.inventory.dto.PageRequest;
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

    public List<FlatEntryDto> getFlatEntryDtos(int pageSize, int fromRow) {
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

        return jdbcTemplate.query(query, flatEntryDtoRowMapper, pageSize, fromRow);
    }
    
    @Override
    public Page getSortedAndFilteredEntries(PageRequest request) {

        List<Long> filteredIds = getFilteredEntries(request.searchString().split(" "));
        int totalNumberOfRows = filteredIds.size();

        List<FlatEntryDto> entries = getSortedEntries(filteredIds, request);
        return new Page(entries, request.pageIndex(), totalNumberOfRows);
    }


    private List<Long> getFilteredEntries(String[] searchString) {
        StringBuilder sql = new StringBuilder("""
                SELECT b."id"
                FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                WHERE 1 = 1""");

        List<Object> params = new ArrayList<>();

        if (searchString != null) {
            for (String str : searchString) {
                sql.append("""
                         AND (
                        b."title" LIKE ?
                        OR b."year" LIKE ?
                        OR b."shelf_mark" LIKE ?
                        OR b."isbn10" LIKE ?
                        OR b."isbn13" LIKE ?
                        OR p."last_name" LIKE ?
                        OR p."first_names" LIKE ?
                        OR "publishers"."name" LIKE ?
                        OR "publishers"."location" LIKE ?
                        )""");
                
                str = "%" + str + "%";
                int n = 9;
                while (n > 0) {
                    params.add(str);
                    n--;
                }
            }
        }
            sql.append(" GROUP BY b.\"id\";");

            return jdbcTemplate.queryForList(sql.toString(), Long.class, params.toArray());
    }

    private List<FlatEntryDto> getSortedEntries(List<Long> filteredIds, PageRequest request) {
        String placeHolders = filteredIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String orderBy = buildOrderBy(request);

        String sql = String.format("""
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
                    FROM "books" b
                LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
                LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
                LEFT JOIN "published" ON b."id" = "published"."book_id"
                LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
                WHERE b."id" IN (%s)
                GROUP BY b."id"
                ORDER BY %s, b."id" %s
                LIMIT %d OFFSET %d;
                """, placeHolders, orderBy, request.sortDir(), request.pageSize(), request.pageSize() * request.pageIndex());

        return jdbcTemplate.query(sql, flatEntryDtoRowMapper, filteredIds.toArray());
    }

    public List<FlatEntryDto> getSortedEntries(PageRequest request) {
        String orderBy = buildOrderBy(request);

        StringBuilder sql = new StringBuilder("""
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
                FROM "books" b
            LEFT JOIN "book_person" ON b."id" = "book_person"."book_id"
            LEFT JOIN "persons" p ON "book_person"."person_id" = p."id"
            LEFT JOIN "published" ON b."id" = "published"."book_id"
            LEFT JOIN "publishers" ON "published"."publisher_id" = "publishers"."id"
            GROUP BY b."id"
        """)
            .append("ORDER BY " + orderBy)
            .append(", b.\"id\" " + request.sortDir())
            .append("\nLIMIT " + request.pageSize())
            .append("\nOFFSET " + (request.pageIndex() * request.pageSize()) + ";");
        
        return jdbcTemplate.query(sql.toString(), flatEntryDtoRowMapper);
    }

    @Override
    public int findRow(long bookId, PageRequest request) {
        String orderBy = buildOrderBy(request);
        System.out.println(orderBy);
        StringBuilder sql = new StringBuilder("""
                SELECT "row_number" FROM (
                    SELECT ROW_NUMBER() OVER(
                        ORDER BY 
                """);
                        
            sql.append(orderBy)
                .append(", \"id\" " + request.sortDir())
                .append("""
                    ) AS "row_number", "id" FROM (
                        SELECT
                            b."id" AS "id", b."title" AS "title", b."year" AS "year", b."shelf_mark" AS "shelf_mark",
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
                        ) grouped
                ) AS t
                WHERE t."id" = ?;
                """);

        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, bookId);
    }

    private String buildOrderBy(PageRequest request) {
        return request.caseInsensitive() ? request.sortBy() + " COLLATE NOCASE " + request.sortDir(): request.sortBy() + " " + request.sortDir();
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

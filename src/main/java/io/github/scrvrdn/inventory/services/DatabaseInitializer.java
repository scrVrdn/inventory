package io.github.scrvrdn.inventory.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DatabaseInitializer {
    private final DataSource dataSource;

    public DatabaseInitializer(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS "books" (
                            "id" INTEGER,
                            "title" TEXT,
                            "year" INTEGER,
                            "isbn10" TEXT UNIQUE,
                            "isbn13" TEXT UNIQUE,
                            "shelf_mark" TEXT,
                            PRIMARY KEY("id")
                        );
                        """);

                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS "persons" (
                            "id" INTEGER,
                            "last_name" TEXT,
                            "first_names" TEXT,
                            PRIMARY KEY("id"),
                            UNIQUE("last_name", "first_names"),
                            CHECK ("last_name" IS NOT NULL OR "first_names" IS NOT NULL)
                        );
                        """);

                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS "publishers" (
                            "id" INTEGER,
                            "name" TEXT,
                            "location" TEXT,
                            PRIMARY KEY("id"),
                            UNIQUE("name", "location"),
                            CHECK ("name" IS NOT NULL OR "location" IS NOT NULL)
                        );
                        """);

                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS "book_person" (
                            "book_id" INTEGER NOT NULL,
                            "person_id" INTEGER NOT NULL,
                            "role" TEXT CHECK("role" IN ('AUTHOR', 'EDITOR')),
                            "order_index" INTEGER DEFAULT 0,
                            PRIMARY KEY("book_id", "person_id", "role"),
                            FOREIGN KEY("book_id") REFERENCES "books"("id") ON DELETE CASCADE,
                            FOREIGN KEY("person_id") REFERENCES "persons"("id") ON DELETE CASCADE
                        );
                        """);

                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS "published" (
                            "book_id" INTEGER NOT NULL,
                            "publisher_id" INTEGER NOT NULL,
                            FOREIGN KEY("book_id") REFERENCES "books"("id") ON DELETE CASCADE,
                            FOREIGN KEY("publisher_id") REFERENCES "publishers"("id") ON DELETE CASCADE,
                            UNIQUE("book_id")
                        );
                        """);

                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS "row_counters"(
                            "table_name" TEXT,
                            "total_rows" INTEGER DEFAULT 0,
                            PRIMARY KEY("table_name")
                        );
                        """);

                stmt.execute("""
                        INSERT OR IGNORE INTO "row_counters" ("table_name", "total_rows")
                        VALUES ('books', 0);
                        """);
                
                stmt.execute("""
                        CREATE TRIGGER IF NOT EXISTS "books_insert_trigger"
                        AFTER INSERT ON "books"
                        FOR EACH ROW
                        BEGIN
                            UPDATE "row_counters"
                            SET "total_rows" = "total_rows" + 1
                            WHERE "table_name" = 'books';
                        END;
                        """);

                stmt.execute("""
                        CREATE TRIGGER IF NOT EXISTS "books_delete_trigger"
                        AFTER DELETE ON "books"
                        FOR EACH ROW
                        BEGIN
                            UPDATE "row_counters"
                            SET "total_rows" = "total_rows" - 1
                            WHERE "table_name" = 'books';
                        END;
                        """);

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database init failed", e);
        }
    }
}

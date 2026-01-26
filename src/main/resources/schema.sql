-- DROP TABLE IF EXISTS "books";
-- DROP TABLE IF EXISTS "persons";
-- DROP TABLE IF EXISTS "publishers";
-- DROP TABLE IF EXISTS "book_person";
-- DROP TABLE IF EXISTS "published";

CREATE TABLE IF NOT EXISTS "books" (
    "id" INTEGER,
    "title" TEXT,
    "year" INTEGER,
    "isbn10" TEXT UNIQUE,
    "isbn13" TEXT UNIQUE,
    "shelf_mark" TEXT,
    PRIMARY KEY("id")
);


CREATE TABLE IF NOT EXISTS "persons" (
    "id" INTEGER,
    "last_name" TEXT,
    "first_names" TEXT,
    PRIMARY KEY("id"),
    UNIQUE("last_name", "first_names"),
    CHECK ("last_name" IS NOT NULL OR "first_names" IS NOT NULL)
);


CREATE TABLE IF NOT EXISTS "publishers" (
    "id" INTEGER,
    "name" TEXT,
    "location" TEXT,
    PRIMARY KEY("id"),
    UNIQUE("name", "location"),
    CHECK ("name" IS NOT NULL OR "location" IS NOT NULL)
);


CREATE TABLE IF NOT EXISTS "book_person" (
    "book_id" INTEGER NOT NULL,
    "person_id" INTEGER NOT NULL,
    "role" TEXT CHECK("role" IN ('AUTHOR', 'EDITOR')),
    "order_index" INTEGER DEFAULT 0,
    PRIMARY KEY("book_id", "person_id", "role"),
    FOREIGN KEY("book_id") REFERENCES "books"("id") ON DELETE CASCADE,
    FOREIGN KEY("person_id") REFERENCES "persons"("id") ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS "published" (
    "book_id" INTEGER NOT NULL,
    "publisher_id" INTEGER NOT NULL,
    FOREIGN KEY("book_id") REFERENCES "books"("id") ON DELETE CASCADE,
    FOREIGN KEY("publisher_id") REFERENCES "publishers"("id") ON DELETE CASCADE,
    UNIQUE("book_id")
);
CREATE TABLE IF NOT EXISTS "books" (
    "id" INTEGER,
    "title" TEXT NOT NULL,
    "year" INTEGER,
    "isbn10" TEXT UNIQUE,
    "isbn13" TEXT UNIQUE,
    "shelf_mark" TEXT,
    PRIMARY KEY("id")
);

CREATE TABLE IF NOT EXISTS "persons" (
    "id" INTEGER,
    "last_name" TEXT NOT NULL,
    "first_names" TEXT,
    PRIMARY KEY("id"),
    UNIQUE("last_name", "first_names")
);

CREATE TABLE IF NOT EXISTS "publishers" (
    "id" INTEGER,
    "name" TEXT,
    "location" TEXT,
    PRIMARY KEY("id"),
    UNIQUE("name", "location")
);

-- DROP TABLE IF EXISTS "book_person";
CREATE TABLE IF NOT EXISTS "book_person" (
    "book_id" INTEGER,
    "person_id" INTEGER,
    "role" TEXT CHECK("role" IN ('AUTHOR', 'EDITOR')),
    "order_index" INTEGER DEFAULT 0,
    PRIMARY KEY("book_id", "person_id", "role"),
    FOREIGN KEY("book_id") REFERENCES "books"("id") ON DELETE CASCADE,
    FOREIGN KEY("person_id") REFERENCES "persons"("id") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "published" (
    "book_id" INTEGER,
    "publisher_id" INTEGER,
    FOREIGN KEY("book_id") REFERENCES "books"("id") ON DELETE CASCADE,
    FOREIGN KEY("publisher_id") REFERENCES "publishers"("id") ON DELETE CASCADE
);
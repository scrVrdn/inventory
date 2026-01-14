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

CREATE TABLE IF NOT EXISTS "authored" (
    "book_id" INTEGER,
    "person_id" INTEGER,
    FOREIGN KEY("book_id") REFERENCES "books"("id") ON DELETE CASCADE,
    FOREIGN KEY("person_id") REFERENCES "persons"("id") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "edited" (
    "book_id" INTEGER,
    "person_id" INTEGER,
    FOREIGN KEY("book_id") REFERENCES "books"("id") ON DELETE CASCADE,
    FOREIGN KEY("person_id") REFERENCES "persons"("id") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "published" (
    "book_id" INTEGER,
    "publisher_id" INTEGER,
    FOREIGN KEY("book_id") REFERENCES "books"("id") ON DELETE CASCADE,
    FOREIGN KEY("publisher_id") REFERENCES "publishers"("id") ON DELETE CASCADE
);

-- CREATE VIEW IF NOT EXISTS "overview" AS
-- SELECT concat_ws(", ", a."last_name", a."first_names") AS "author", b."title", concat_ws(", ", e."last_name", e."first_names") AS "editor", b."location", b."year", b."shelfmark"
-- FROM "books" b
-- LEFT JOIN "authored" ON "books"."id" = "authored"."book_id"
-- LEFT JOIN "persons" a ON "authored"."person_id" = a."id"
-- LEFT JOIN "edited" ON "books"."id" = "edited"."book_id"
-- LEFT JOIN "persons" e ON "edited"."person_id" = e."id";
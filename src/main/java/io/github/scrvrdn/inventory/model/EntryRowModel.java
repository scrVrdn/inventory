package io.github.scrvrdn.inventory.model;

import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EntryRowModel {
    public final LongProperty bookId = new SimpleLongProperty();
    public final StringProperty bookTitle = new SimpleStringProperty();
    public final IntegerProperty bookYear = new SimpleIntegerProperty();
    public final StringProperty shelfMark = new SimpleStringProperty();
    public final StringProperty authors = new SimpleStringProperty();
    public final StringProperty editors = new SimpleStringProperty();
    public final StringProperty publisher = new SimpleStringProperty();

    public EntryRowModel(FlatEntryDto dto) {
        updateFromDto(dto);
    }

    public void updateFromDto(FlatEntryDto dto) {
        bookId.set(dto.bookId());
        bookTitle.set(dto.bookTitle());
        bookYear.set(dto.bookYear());
        shelfMark.set(dto.shelfMark());
        authors.set(dto.authors());
        editors.set(dto.editors());
        publisher.set(dto.publisher());
    }


    public LongProperty bookIdProperty() {
        return bookId;
    }

    public StringProperty bookTitleProperty() {
        return bookTitle;
    }

    public IntegerProperty bookYearProperty() {
        return bookYear;
    }

    public StringProperty shelfMarkProperty() {
        return shelfMark;
    }

    public StringProperty authorsProperty() {
        return authors;
    }

    public StringProperty editorsProperty() {
        return editors;
    }

    public StringProperty publisherProperty() {
        return publisher;
    }
}

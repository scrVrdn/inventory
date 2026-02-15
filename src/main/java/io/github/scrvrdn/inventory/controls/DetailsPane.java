package io.github.scrvrdn.inventory.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.dto.Book;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Person;
import io.github.scrvrdn.inventory.dto.Publisher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Component
public class DetailsPane {
    private static final int LAST_NAME_IDX = 1;
    private static final int FIRST_NAME_IDX = 3;

    private FullEntryDto entry;

    private Consumer<FullEntryDto> saveCallback;

    @FXML private BorderPane details;
    @FXML private Label nothingSelectedLabel;

    @FXML private DynamicTextField titleField;
    @FXML private VBox authorBox;
    @FXML private VBox editorBox;
    @FXML private DynamicTextField publisherPlaceField;
    @FXML private DynamicTextField publisherNameField;
    @FXML private DynamicTextField yearField;
    @FXML private DynamicTextField isbn10Field;
    @FXML private DynamicTextField isbn13Field;
    @FXML private DynamicTextField shelfMarkField;

    public void setVisibility(boolean visible) {
        details.setVisible(visible);
        details.setManaged(visible);
        nothingSelectedLabel.setVisible(!visible);
        nothingSelectedLabel.setManaged(!visible);
    }
   
    public void showDetails(FullEntryDto entry) {
        this.entry = entry;

        populateBookDetails();
        populateAuthors();
        populateEditors();
        populatePublisherDetails();
    }

    private void populateBookDetails() {
         if (entry.getBook() != null) {
            titleField.setText(getNullSafeString(entry.getBook().getTitle()));            
            yearField.setText(Objects.toString(entry.getBook().getYear(), ""));
            isbn10Field.setText(getNullSafeString(entry.getBook().getIsbn10()));
            isbn13Field.setText(getNullSafeString(entry.getBook().getIsbn13()));
            shelfMarkField.setText(getNullSafeString(entry.getBook().getShelfMark()));

        } else {
            titleField.setText("");
            yearField.setText("");
            isbn10Field.setText("");
            isbn13Field.setText("");
            shelfMarkField.setText("");
        }
    }

    private String getNullSafeString(String str) {
        return str != null ? str : "";
    }

    private void populateAuthors() {
        prepareAuthorBox();
        if (entry.getAuthors().isEmpty()) {
            clearAuthorEntries();
            return;
        }

        int i = 0;
        
        for (Node authorEntry : authorBox.getChildren()) {
            
            HBox authorEntryAsHBox = (HBox) authorEntry;
            TextField lastName = (TextField) authorEntryAsHBox.getChildren().get(LAST_NAME_IDX);
            lastName.setText(getNullSafeString(entry.getAuthors().get(i).getLastName()));

            TextField firstNames = (TextField) authorEntryAsHBox.getChildren().get(FIRST_NAME_IDX);
            firstNames.setText(getNullSafeString(entry.getAuthors().get(i).getFirstNames()));
            i++;                 
        }
    }

    private  void prepareAuthorBox() {
        while (authorBox.getChildren().size() < entry.getAuthors().size()) {
            addAuthorTextField();
        }

        int minSize = Math.max(1, entry.getAuthors().size());
        while (authorBox.getChildren().size() > minSize) {
            authorBox.getChildren().removeLast();
        }
    }

    private void clearAuthorEntries() {
        for (Node authorEntry : authorBox.getChildren()) {
            HBox authorEntryAsHBox = (HBox) authorEntry;
            ((TextField) authorEntryAsHBox.getChildren().get(LAST_NAME_IDX)).clear();
            ((TextField) authorEntryAsHBox.getChildren().get(FIRST_NAME_IDX)).clear();
        }
    }
   
    private void populateEditors() {
        prepareEditorBox();
        if (entry.getEditors().isEmpty()) {
            clearEditorEntries();
            return;
        }

        int i = 0;
        
        for (Node editorEntry : editorBox.getChildren()) {
            
            HBox editorEntryAsHBox = (HBox) editorEntry;
            DynamicTextField lastName = (DynamicTextField) editorEntryAsHBox.getChildren().get(LAST_NAME_IDX);
            lastName.setText(getNullSafeString(entry.getEditors().get(i).getLastName()));

            DynamicTextField firstNames = (DynamicTextField) editorEntryAsHBox.getChildren().get(FIRST_NAME_IDX);
            firstNames.setText(getNullSafeString(entry.getEditors().get(i).getFirstNames()));
            i++;    
        }
    }

    private void prepareEditorBox() {
        while (editorBox.getChildren().size() < entry.getEditors().size()) {
            addEditorTextField();
        }

        int minSize = Math.max(1, entry.getEditors().size());
        while (editorBox.getChildren().size() > minSize) {
            editorBox.getChildren().removeLast();
        }
    }

    private void clearEditorEntries() {
        for (Node editorEntry : editorBox.getChildren()) {
            HBox editorEntryAsHBox = (HBox) editorEntry;
            ((TextField) editorEntryAsHBox.getChildren().get(LAST_NAME_IDX)).clear();
            ((TextField) editorEntryAsHBox.getChildren().get(FIRST_NAME_IDX)).clear();
        }
    }

    @FXML
    private void addAuthorTextField() {
        HBox fields = new HBox();
        fields.getStyleClass().add("details-person-hbox");

        Label label = new Label("Author");
        label.getStyleClass().add("details-label");

        DynamicTextField lastName = new DynamicTextField();
        lastName.getStyleClass().add("details-field");

        Label delimiter = new Label(",");
        delimiter.getStyleClass().add("details-delimiter");

        DynamicTextField firstNames = new DynamicTextField();
        firstNames.getStyleClass().add("details-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("+");
        addButton.setOnAction(e -> addAuthorTextField());
        addButton.getStyleClass().add("details-person-button");

        Button deleteButton = new Button("-");
        deleteButton.setOnAction(e -> deleteAuthorTextField(e));
        deleteButton.getStyleClass().add("details-person-button");

        fields.getChildren().addAll(label, lastName, delimiter, firstNames, spacer, addButton, deleteButton);
        authorBox.getChildren().add(fields);
    }

    @FXML
    private void deleteAuthorTextField(ActionEvent event) {
        Button delButton = (Button) event.getSource();
        HBox parent = (HBox) delButton.getParent();

        if (authorBox.getChildren().size() > 1) {            
            authorBox.getChildren().remove(parent);
        } else {
            for (Node child : parent.getChildren()) {
                if (child instanceof TextField) {
                    ((TextField) child).clear();
                }
            }
        }        
    }

    @FXML
    private void addEditorTextField() {
        HBox fields = new HBox();
        fields.getStyleClass().add("details-person-hbox");

        Label label = new Label("Editor");
        label.getStyleClass().add("details-label");

        DynamicTextField lastName = new DynamicTextField();
        lastName.getStyleClass().add("details-field");

        Label delimiter = new Label(",");
        delimiter.getStyleClass().add("details-delimiter");

        DynamicTextField firstNames = new DynamicTextField();
        firstNames.getStyleClass().add("details-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("+");
        addButton.setOnAction(e -> addEditorTextField());
        addButton.getStyleClass().add("details-person-button");

        Button deleteButton = new Button("-");
        deleteButton.setOnAction(e -> deleteEditorTextField(e));
        deleteButton.getStyleClass().add("details-person-button");

        fields.getChildren().addAll(label, lastName, delimiter, firstNames, spacer, addButton, deleteButton);
        editorBox.getChildren().add(fields);
    }

    @FXML
    private void deleteEditorTextField(ActionEvent event) {
        Button delButton = (Button) event.getSource();
        HBox parent = (HBox) delButton.getParent();

        if (editorBox.getChildren().size() > 1) {
            editorBox.getChildren().remove(parent);
        } else {
            for (Node child : parent.getChildren()) {
                if (child instanceof TextField) {
                    ((TextField) child).clear();
                }
            }
        }
    }

    private void populatePublisherDetails() {
        if (entry.getPublisher() != null) {
            publisherPlaceField.setText(getNullSafeString(entry.getPublisher().getLocation()));
            publisherNameField.setText(getNullSafeString(entry.getPublisher().getName()));
        } else {
            publisherPlaceField.setText("");
            publisherNameField.setText("");
        }
    }

    public void setSaveCallback(Consumer<FullEntryDto> callback) {
        this.saveCallback = callback;
    }

    @FXML
    private void onSave() {
        FullEntryDto newEntryDto = buildEntryDto();
        saveCallback.accept(newEntryDto);
        entry = newEntryDto;
    }

    private FullEntryDto buildEntryDto() {
        Book bookData = getBookData();
        List<Person> authorData = getAuthorData();
        List<Person> editorData = getEditorData();
        Publisher publisherData = getPublisherData();

        FullEntryDto updatedEntry = FullEntryDto.builder()
                                .book(bookData)
                                .authors(authorData)
                                .editors(editorData)
                                .publisher(publisherData)
                                .build();


        return updatedEntry;
    }

    private Book getBookData() {
        String title = titleField.getText().strip().replaceAll("\\s+", " ");
        
        Integer year = getYear();

        String isbn10 = isbn10Field.getText().replaceAll("\\D+", "");
        
        String isbn13 = isbn13Field.getText().replaceAll("\\D+", "");
        
        String shelfMark = shelfMarkField.getText().strip().replaceAll("\\s+", " ");
       
        return Book.builder()
                    .id(entry.getBook().getId())
                    .title(title)
                    .year(year)
                    .isbn10(isbn10)
                    .isbn13(isbn13)
                    .shelfMark(shelfMark)
                    .build();
    }

    private Integer getYear() {
        String raw = yearField.getText().replaceAll("\\D+", "");
        if (raw.isEmpty()) return null;

        try {
            long year = Long.parseLong(raw);
            if (year > Integer.MAX_VALUE || year < Integer.MIN_VALUE) {
                return null;
            }

            return (int) year;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<Person> getAuthorData() {
        List<Person> authors = new ArrayList<>();
        
        for (Node authorRow : authorBox.getChildren()) {
            HBox authorRowAsHBox = (HBox) authorRow;

            String lastName = ((TextField) authorRowAsHBox.getChildren().get(LAST_NAME_IDX))
                                                                            .getText()
                                                                            .strip()
                                                                            .replaceAll("\\s+", " ");
            if (lastName.isEmpty()) lastName = null;

            String firstName = ((TextField) authorRowAsHBox.getChildren().get(FIRST_NAME_IDX))
                                                                            .getText()
                                                                            .strip()
                                                                            .replaceAll("\\s+", " ");
            if (firstName.isEmpty()) firstName = null;

            if (lastName != null || firstName != null) {
                authors.add(Person.builder()
                                    .lastName(lastName)
                                    .firstNames(firstName)
                                    .build()
                );
            }            
        }

        return authors;
    }

    private List<Person> getEditorData() {
        List<Person> editors = new ArrayList<>();

        for (Node editorRow : editorBox.getChildren()) {
            HBox editorRowAsHBox = (HBox) editorRow;

            String lastName = ((TextField) editorRowAsHBox.getChildren().get(LAST_NAME_IDX))
                                                                            .getText()
                                                                            .strip()
                                                                            .replaceAll("\\s+", " ");
            if (lastName.isEmpty()) lastName = null;

            String firstName = ((TextField) editorRowAsHBox.getChildren().get(FIRST_NAME_IDX))
                                                                            .getText()
                                                                            .strip()
                                                                            .replaceAll("\\s+", " ");

            if (firstName.isEmpty()) firstName = null;

            if (lastName != null || firstName != null) {
                editors.add(Person.builder()
                                    .lastName(lastName)
                                    .firstNames(firstName)
                                    .build()
                );
            }
        }

        return editors;
    }

    private Publisher getPublisherData() {
        String place = publisherPlaceField.getText().strip().replaceAll("\\s+", " ");

        String name = publisherNameField.getText().strip().replaceAll("\\s+", " ");

        Long id = entry.getPublisher() != null ? entry.getPublisher().getId() : null;
        return Publisher.builder()
                        .id(id)
                        .name(name)
                        .location(place)
                        .build();
    }

}

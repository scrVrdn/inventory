package io.github.scrvrdn.inventory.controller;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import io.github.scrvrdn.inventory.controls.DynamicTextField;
import io.github.scrvrdn.inventory.dto.FullEntryDto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
public class DetailsPaneController {

    private FullEntryDto entry;

    private Consumer<FullEntryDto> saveCallback;

    @FXML
    private DynamicTextField titleField;

    @FXML
    private VBox authorBox;

    @FXML
    private VBox editorBox;

    @FXML
    private DynamicTextField publisherPlaceField;

    @FXML
    private DynamicTextField publisherNameField;

    @FXML
    private DynamicTextField yearField;

    @FXML
    private DynamicTextField isbn10Field;

    @FXML
    private DynamicTextField isbn13Field;

    @FXML
    private DynamicTextField shelfMarkField;



    
    public void showDetails(FullEntryDto entry) {
        this.entry = entry;

        titleField.setText(entry.getBook().getTitle());
        populateAuthors();
        populateEditors();

        if (entry.getPublisher() != null) {
            publisherPlaceField.setText(entry.getPublisher().getLocation());
            publisherNameField.setText(entry.getPublisher().getName());
        }
        
        yearField.setText(String.valueOf(entry.getBook().getYear()));
        isbn10Field.setText(entry.getBook().getIsbn10());
        isbn13Field.setText(entry.getBook().getIsbn13());
        shelfMarkField.setText(entry.getBook().getShelfMark());
    }

    private void populateAuthors() {
        prepareAuthorBox();

        int lastNameIdx = 1;
        int firstNameIdx = 3;
        int i = 0;
        
        for (Node authorEntry : authorBox.getChildren()) {
            
            HBox authorEntryAsHBox = (HBox) authorEntry;
            TextField lastName = (TextField) authorEntryAsHBox.getChildren().get(lastNameIdx);
            lastName.setText(entry.getAuthors().get(i).getLastName());

            TextField firstNames = (TextField) authorEntryAsHBox.getChildren().get(firstNameIdx);
            firstNames.setText(entry.getAuthors().get(i).getFirstNames());
            i++;                 
        }
    }

    private  void prepareAuthorBox() {
        while (authorBox.getChildren().size() < entry.getAuthors().size()) {
            addAuthorTextField();
        }

        while (authorBox.getChildren().size() > entry.getAuthors().size()) {
            authorBox.getChildren().removeLast();
        }
    }
   
    private void populateEditors() {
        prepareEditorBox();

        int lastNameIdx = 1;
        int firstNameIdx = 3;
        int i = 0;
        
        for (Node editorEntry : editorBox.getChildren()) {
            
            HBox editorEntryAsHBox = (HBox) editorEntry;
            DynamicTextField lastName = (DynamicTextField) editorEntryAsHBox.getChildren().get(lastNameIdx);
            lastName.setText(entry.getEditors().get(i).getLastName());

            DynamicTextField firstNames = (DynamicTextField) editorEntryAsHBox.getChildren().get(firstNameIdx);
            firstNames.setText(entry.getEditors().get(i).getFirstNames());
            i++;    
        }
    }

    private void prepareEditorBox() {
        while (editorBox.getChildren().size() < entry.getEditors().size()) {
            addEditorTextField();
        }

        while (editorBox.getChildren().size() > entry.getEditors().size()) {
            editorBox.getChildren().removeLast();
        }
    }

    @FXML
    private void addAuthorTextField() {
        HBox fields = new HBox();
        Label label = new Label("Author");
        label.getStyleClass().add("details-label");

        DynamicTextField lastName = new DynamicTextField();
        lastName.getStyleClass().add("details-field");

        Label delimiter = new Label(",");
        delimiter.getStyleClass().add("details-delimiter");

        DynamicTextField firstNames = new DynamicTextField();
        firstNames.getStyleClass().add("details-field");

        Button addButton = new Button("+");
        addButton.setOnAction(e -> addAuthorTextField());

        Button deleteButton = new Button("-");
        deleteButton.setOnAction(e -> deleteAuthorTextField(e));

        fields.getChildren().addAll(label, lastName, delimiter, firstNames, addButton, deleteButton);
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
        Label label = new Label("Editor");
        label.getStyleClass().add("details-label");

        DynamicTextField lastName = new DynamicTextField();
        lastName.getStyleClass().add("details-field");

        Label delimiter = new Label(",");
        delimiter.getStyleClass().add("details-delimiter");

        DynamicTextField firstNames = new DynamicTextField();
        firstNames.getStyleClass().add("details-field");

        Button addButton = new Button("+");
        addButton.setOnAction(e -> addEditorTextField());

        Button deleteButton = new Button("-");
        deleteButton.setOnAction(e -> deleteEditorTextField(e));

        fields.getChildren().addAll(label, lastName, delimiter, firstNames, addButton, deleteButton);
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

    public void setSaveCallback(Consumer<FullEntryDto> callback) {
        this.saveCallback = callback;
    }

    @FXML
    private void onSave() {
        saveCallback.accept(buildEntryDto());
    }

    private FullEntryDto buildEntryDto() {
        return null;
    }

}

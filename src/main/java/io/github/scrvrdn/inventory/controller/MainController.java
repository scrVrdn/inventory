package io.github.scrvrdn.inventory.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;

import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import io.github.scrvrdn.inventory.services.EntryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;



@Controller
public class MainController {

    private final EntryService entryService;

    @FXML private TableView<FlatEntryDto> table;
    @FXML private TableColumn<FlatEntryDto, Long> id;
    @FXML private TableColumn<FlatEntryDto, String> authors;
    @FXML private TableColumn<FlatEntryDto, String> title;
    @FXML private TableColumn<FlatEntryDto, String> editors;
    @FXML private TableColumn<FlatEntryDto, String> publisher;
    @FXML private TableColumn<FlatEntryDto, Integer> year;
    @FXML private TableColumn<FlatEntryDto, String> shelfMark;

    @FXML private TextField titleField;
    @FXML private TextField authorLastNameField;
    @FXML private TextField authorFirstNameField;
    @FXML private TextField editorLastNameField;
    @FXML private TextField editorFirstNameField;
    @FXML private TextField publisherNameField;
    @FXML private TextField publisherPlaceField;
    @FXML private TextField yearField;
    @FXML private TextField isbn10Field;
    @FXML private TextField isbn13Field;
    @FXML private TextField shelfMarkField;

    @FXML private Button addNewEntryButton;

    private DetailsPaneController detailsPane;

    public MainController(final EntryService entryService, DetailsPaneController detailsPaneController) {
        this.entryService = entryService;
        this.detailsPane = detailsPaneController;
    }

    @FXML
    private void initialize() {
        id.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        authors.setCellValueFactory(new PropertyValueFactory<>("authors"));
        title.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        editors.setCellValueFactory(new PropertyValueFactory<>("editors"));
        publisher.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        
        year.setCellValueFactory(new PropertyValueFactory<>("bookYear"));

        shelfMark.setCellValueFactory(new PropertyValueFactory<>("shelfMark"));

        table.setItems(getEntries());
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            Long entryId = newSelection.getBookId();
            entryService.findById(entryId)
                .ifPresent(this::showDetails);
        });

        detailsPane.setSaveCallback(this::handleSave);
    }

    private ObservableList<FlatEntryDto> getEntries() {
        ObservableList<FlatEntryDto> rows = FXCollections.observableArrayList();
        rows.addAll(entryService.getAllFlatEntryDtos());
        return rows;
    }

    @FXML
    private void handleAddNewEntryButton() {
        Optional<FlatEntryDto> entry = entryService.createEmptyEntry();
        if (entry.isPresent()) {
            
            table.getItems().add(entry.get());
            table.getSelectionModel().selectLast();
            table.scrollTo(table.getItems().size() - 1);
        }        
    }

    @FXML
    private void showDetails(FullEntryDto entry) {
        detailsPane.showDetails(entry);
        detailsPane.setSaveCallback(this::handleSave);
    }

    private void handleSave(FullEntryDto entry) {

    }
}

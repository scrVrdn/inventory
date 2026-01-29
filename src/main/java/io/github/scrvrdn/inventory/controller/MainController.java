package io.github.scrvrdn.inventory.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;

import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.services.facade.EntryService;
import io.github.scrvrdn.inventory.controls.DetailsPane;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;


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

    @FXML private Button addNewEntryButton;
    @FXML private Button deleteEntryButton;

    private DetailsPane detailsPane;

    public MainController(final EntryService entryService, DetailsPane detailsPaneController) {
        this.entryService = entryService;
        this.detailsPane = detailsPaneController;
    }

    @FXML
    private void initialize() {
        id.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().bookId()));
        authors.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().authors()));
        title.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().bookTitle()));
        editors.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().editors()));
        publisher.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().publisher()));
        year.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().bookYear()));
        shelfMark.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().shelfMark()));

        table.setItems(getEntries());
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                detailsPane.setVisibility(true);
                Long entryId = newSelection.bookId();
                entryService.findById(entryId)
                            .ifPresent(this::showDetails);
            } else {
                detailsPane.setVisibility(false);
            }
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
    private void handleDeleteEntryButton() {
        FlatEntryDto selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            entryService.delete(selected.bookId());
            table.getItems().remove(selected);
        }
    }

    @FXML
    private void showDetails(FullEntryDto entry) {
        detailsPane.showDetails(entry);
        detailsPane.setSaveCallback(this::handleSave);
    }

    private void handleSave(FullEntryDto entry) {
        FlatEntryDto updatedEntry = entryService.update(entry);
        int idx = table.getSelectionModel().getSelectedIndex();
        table.getItems().set(idx, updatedEntry);
        table.getSelectionModel().select(idx);
    }
}

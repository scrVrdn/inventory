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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;


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

    @FXML private Button firstPage;
    @FXML private Button fastBack;
    @FXML private Button previousPage;
    @FXML private Button nextPage;
    @FXML private Button fastForward;
    @FXML private Button lastPage;
    @FXML private TextField currentPage;
    @FXML private Label totalPageCountLabel;

    private DetailsPane detailsPane;
    private int itemsPerPage = 5;
    private int totalNumberOfRows;
    private int currentPageIndex = 0;
    private int totalPageCount;
    private int skipPages = 5;
    private ObservableList<FlatEntryDto> entryRows = FXCollections.observableArrayList();

    public MainController(final EntryService entryService, DetailsPane detailsPaneController) {
        this.entryService = entryService;
        this.detailsPane = detailsPaneController;
    }

    @FXML
    private void initialize() {
        createTable();
        totalNumberOfRows = entryService.numberOfRows();
        calculateTotalPageCount();
        detailsPane.setSaveCallback(this::handleSave);
    }

    private void createTable() {
        id.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().bookId()));
        authors.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().authors()));
        title.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().bookTitle()));
        editors.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().editors()));
        publisher.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().publisher()));
        year.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().bookYear()));
        shelfMark.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().shelfMark()));

        table.setItems(entryRows);
        updateTableViewPage(currentPageIndex);
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
    }

    private void refreshTable() {
        totalNumberOfRows = entryService.numberOfRows();
        
    }

    private void calculateTotalPageCount() {
        int pages = (int) Math.ceil(totalNumberOfRows / (double) itemsPerPage);
        totalPageCount = pages;
        totalPageCountLabel.setText(String.valueOf(pages));
    }

    private void updateTableViewPage(int pageIndex) {
        int fromIndex = pageIndex * itemsPerPage;
        currentPage.setText(String.valueOf(pageIndex + 1));
        validatePageButtons();
        entryRows.setAll(entryService.getFlatEntryDtos(fromIndex, itemsPerPage));
    }

    @FXML
    private void handlePreviousPage() {
        currentPageIndex = Math.max(currentPageIndex - 1, 0);
        updateTableViewPage(currentPageIndex);
    }

    @FXML
    private void handleFastBack() {
        currentPageIndex = Math.max(currentPageIndex - skipPages, 0);
        updateTableViewPage(currentPageIndex);
    }

    @FXML
    private void handleFirstPage() {
        currentPageIndex = 0;
        updateTableViewPage(currentPageIndex);
    }

    @FXML
    private void handleNextPage() {
        currentPageIndex = Math.min(currentPageIndex + 1, totalPageCount - 1);
        updateTableViewPage(currentPageIndex);
    }

    @FXML
    private void handleFastForward() {
        currentPageIndex = Math.min(currentPageIndex + skipPages, totalPageCount - 1);
        updateTableViewPage(currentPageIndex);
    }

    @FXML
    private void handleLastPage() {
        currentPageIndex = totalPageCount - 1;
        updateTableViewPage(currentPageIndex);
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

            // refresh table
        }        
    }

    private void validatePageButtons() {
        if (currentPageIndex == 0) {
            previousPage.setDisable(true);
            fastBack.setDisable(true);
            firstPage.setDisable(true);

        } else {
            previousPage.setDisable(false);
            fastBack.setDisable(false);
            firstPage.setDisable(false);
        }

        if (currentPageIndex == totalPageCount - 1) {
            nextPage.setDisable(true);
            fastForward.setDisable(true);
            lastPage.setDisable(true);

        } else {
            nextPage.setDisable(false);
            fastForward.setDisable(false);
            lastPage.setDisable(false);
        }

    }

    @FXML
    private void handleDeleteEntryButton() {
        FlatEntryDto selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            entryService.delete(selected.bookId());
            table.getItems().remove(selected);
            totalNumberOfRows = entryService.numberOfRows();
            calculateTotalPageCount();
            updateTableViewPage(currentPageIndex);
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
        totalNumberOfRows = entryService.numberOfRows();
        calculateTotalPageCount();
        updateTableViewPage(currentPageIndex);
    }
}

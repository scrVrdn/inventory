package io.github.scrvrdn.inventory.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;

import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.services.facade.EntryService;
import io.github.scrvrdn.inventory.controls.DetailsPane;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;


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

    @FXML private Label itemsPerPageLabel;
    @FXML private ComboBox<Integer> itemsPerPageComboBox; 
    private IntegerProperty itemsPerPage = new SimpleIntegerProperty();

    @FXML private Button firstPage;
    @FXML private Button fastBack;
    @FXML private Button previousPage;
    @FXML private Button nextPage;
    @FXML private Button fastForward;
    @FXML private Button lastPage;
    @FXML private TextField currentPageField;
    @FXML private Label totalPageCountLabel;

    private DetailsPane detailsPane;
    
    
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
        detailsPane.setSaveCallback(this::handleSave);
        setupItemsPerPageCombobox();        
        setupCurrentPageField();
        calculateTotalPageCount();
        updateTableViewPage();
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

    private void setupItemsPerPageCombobox() {
        itemsPerPage.bind(itemsPerPageComboBox.getSelectionModel().selectedItemProperty());
        
        ObservableList<Integer> list = itemsPerPageComboBox.getItems();
        list.addAll(10, 25, 50, 75, 100);        
        itemsPerPageComboBox.getSelectionModel().select(0);
    }

    private void setupCurrentPageField() {
        TextFormatter<Integer> formatter = new TextFormatter<>(
            new IntegerStringConverter(),
            1,
            change -> {
                String newText = change.getControlNewText();
                if (newText.matches("\\d*")) {
                    return change;
                }

                return null;
            }
        );

        currentPageField.setTextFormatter(formatter);
    }

    private void refreshTable() {
        currentPageField.setText(String.valueOf(currentPageIndex + 1));
        validatePageButtons();
    }

    private void calculateTotalPageCount() {
        totalNumberOfRows = entryService.numberOfRows();
        totalPageCount = (totalNumberOfRows + itemsPerPage.intValue() - 1) / itemsPerPage.intValue();
        totalPageCountLabel.setText("/ " + String.valueOf(totalPageCount));
    }

    private void updateTableViewPage() {
        int fromRow = currentPageIndex * itemsPerPage.intValue();
        currentPageField.setText(String.valueOf(currentPageIndex + 1));
        validatePageButtons();
        entryRows.setAll(entryService.getFlatEntryDtos(itemsPerPage.intValue(), fromRow));
    }

    @FXML
    private void goToPage() {
        Integer requestedPage = (Integer) currentPageField.getTextFormatter().getValue();
        
        if (requestedPage != null && requestedPage >= 1 && requestedPage <= totalPageCount) {
            currentPageIndex = requestedPage - 1;
            updateTableViewPage();
        } else {
            currentPageField.setText(String.valueOf(currentPageIndex + 1));
        }
    }

    @FXML
    private void goToPreviousPage() {
        currentPageIndex = Math.max(currentPageIndex - 1, 0);
        updateTableViewPage();
    }

    @FXML
    private void handleFastBack() {
        currentPageIndex = Math.max(currentPageIndex - skipPages, 0);
        updateTableViewPage();
    }

    @FXML
    private void goToFirstPage() {
        currentPageIndex = 0;
        updateTableViewPage();
    }

    @FXML
    private void goToNextPage() {
        currentPageIndex = Math.min(currentPageIndex + 1, totalPageCount - 1);
        updateTableViewPage();
    }

    @FXML
    private void handleFastForward() {
        currentPageIndex = Math.min(currentPageIndex + skipPages, totalPageCount - 1);
        updateTableViewPage();
    }

    @FXML
    private void goToLastPage() {
        currentPageIndex = totalPageCount - 1;
        updateTableViewPage();
    }

    private ObservableList<FlatEntryDto> getEntries() {
        ObservableList<FlatEntryDto> rows = FXCollections.observableArrayList();
        rows.addAll(entryService.getAllFlatEntryDtos());
        return rows;
    }

    @FXML
    private void handleAddNewEntryButton() {
        boolean newEntryOnNewPage = lastPageIsFull();
        boolean currentPageIsNotLastPage = !onLastPage();
        
        try {
            Optional<FlatEntryDto> entry = entryService.createEmptyEntry();
            if (entry.isPresent()) {
                
                calculateTotalPageCount();
            
                if (newEntryOnNewPage) {
                    entryRows.clear();
                    currentPageIndex = totalPageCount - 1;
                    entryRows.add(entry.get());
                } else {
                    if (currentPageIsNotLastPage) {
                        goToLastPage();
                    } else {
                        entryRows.add(entry.get());
                    }
                }
                
                table.getSelectionModel().selectLast();
                table.scrollTo(table.getSelectionModel().getSelectedIndex());
                refreshTable();
            }

        } catch (Exception e) {
            System.err.println("Failed to add Item");
        }
    }

    private boolean lastPageIsFull() {
        return totalNumberOfRows % itemsPerPage.intValue() == 0;
    }

    private boolean onLastPage() {
        return currentPageIndex == totalPageCount - 1;
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
        int selectedIdx = table.getSelectionModel().getSelectedIndex();

        if (selected != null) {
            boolean toDeleteIsOnLastPage = onLastPage();
            boolean toDeleteIsSingleItemOnPage = table.getItems().size() == 1;
           
            try {
                entryService.delete(selected.bookId());
                calculateTotalPageCount();

                if (toDeleteIsOnLastPage && toDeleteIsSingleItemOnPage) {                    
                    goToLastPage();

                } else {
                    entryRows.remove(selected);
                    if (!toDeleteIsOnLastPage) fillTable();
                }
                
                int targetIdx = selectedIdx < table.getItems().size() ? selectedIdx : Math.max(0, selectedIdx - 1);
                if (!entryRows.isEmpty()) {
                    table.getSelectionModel().select(targetIdx);
                } else {
                    table.getSelectionModel().clearSelection();
                }

            } catch (Exception e) {
                System.err.println("Failed to delete: " + selected.toString());
            }
            
            refreshTable();
        }
    }

    private void fillTable() {
        FlatEntryDto lastEntry = entryRows.getLast();
        Optional<FlatEntryDto> nextEntry = entryService.getNextFlatEntryDtoAfterBookId(lastEntry.bookId());
        if (nextEntry.isPresent()) entryRows.addLast(nextEntry.get());
    }

    @FXML
    private void handleItemsPerPageSelection() {
        currentPageIndex = 0;
        totalPageCount = (totalNumberOfRows + itemsPerPage.intValue() - 1) / itemsPerPage.intValue();
        totalPageCountLabel.setText("/ " + String.valueOf(totalPageCount));
        updateTableViewPage();
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

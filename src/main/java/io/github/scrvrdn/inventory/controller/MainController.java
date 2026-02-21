package io.github.scrvrdn.inventory.controller;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Controller;

import io.github.scrvrdn.inventory.dto.FullEntryDto;
import io.github.scrvrdn.inventory.dto.Page;
import io.github.scrvrdn.inventory.dto.PageRequest;
import io.github.scrvrdn.inventory.services.facade.EntryService;
import io.github.scrvrdn.inventory.controls.DetailsPane;
import io.github.scrvrdn.inventory.dto.FlatEntryDto;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.scene.control.TableColumn.SortType;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;

@Controller
public class MainController {
    private static final int SEARCH_DELAY = 600;

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

    @FXML private TextField searchField;
    private Timeline searchDelay;

    private String currentFilter;
    private String sortBy;
    private String sortDir;

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
        
        detailsPane.setSaveCallback(this::handleSave);
        setupItemsPerPageCombobox();        
        setupCurrentPageField();
        setupSearchField();
        calculateTotalPageCount();
        createTable();
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
        table.setPlaceholder(new Label("No entries"));
        
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

        table.setSortPolicy(table -> {

            if (!table.getSortOrder().isEmpty()) {
                TableColumn<FlatEntryDto, ?> col = table.getSortOrder().get(0);
                String newSortBy = col.getId();
                String newSortDir = col.getSortType() == SortType.ASCENDING ? "ASC" : "DESC";

                if (newSortBy.equals(sortBy) && newSortDir.equals(sortDir)) {
                    return true;
                } else {
                    sortBy = newSortBy;
                    sortDir = newSortDir;
                }
            }
            
            goToFirstPage();
            return true;
        });

        
    }

    private CompletableFuture<ObservableList<FlatEntryDto>> getEntries() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PageRequest request = new PageRequest(currentPageIndex, itemsPerPage.get(), currentFilter, sortBy, sortDir, isCaseInsensitiveSort());
                Page page = entryService.getPage(request);
                currentPageIndex = page.pageIndex();
                totalNumberOfRows = page.totalNumberOfRows();
                updateTotalPageCount();
                
                return page.entries();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            
        }).thenApply(FXCollections::observableArrayList);
    }

    private CompletableFuture<ObservableList<FlatEntryDto>> getEntriesAfterUpdate(long bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PageRequest request = new PageRequest(currentPageIndex, itemsPerPage.get(), currentFilter, sortBy, sortDir, isCaseInsensitiveSort());
                Page page = entryService.getPageWithBook(bookId, request);
                currentPageIndex = page.pageIndex();
                totalNumberOfRows = page.totalNumberOfRows();
                updateTotalPageCount();
                
                return page.entries();
                
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            
        }).thenApply(FXCollections::observableArrayList);
    }

    private boolean isCaseInsensitiveSort() {
        if (sortBy == null) return true;
        if (sortBy.equals(id.getId()) || sortBy.equals(year.getId())) return false;
        return true;
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

    private void setupSearchField() {
        searchDelay = new Timeline();
        searchDelay.setCycleCount(1);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) {
                searchDelay.stop();
                currentFilter = null;
                goToFirstPage();
                return;
            }

            searchDelay.stop();
            searchDelay.getKeyFrames().setAll(new KeyFrame(Duration.millis(SEARCH_DELAY), event -> {
                    currentFilter = searchField.getText();
                    goToFirstPage();
                })
            );

            searchDelay.play();
        });
    }

    private void refreshTable() {
        currentPageField.setText(String.valueOf(currentPageIndex + 1));
        totalPageCountLabel.setText("/ " + String.valueOf(totalPageCount));
        validatePageButtons();
    }

    private void calculateTotalPageCount() {
        totalNumberOfRows = entryService.numberOfRows();
        totalPageCount = (totalNumberOfRows + itemsPerPage.intValue() - 1) / itemsPerPage.intValue();
        totalPageCountLabel.setText("/ " + String.valueOf(totalPageCount));
    }

    private void updateTableViewPage() {
         getEntries().thenAccept(data -> Platform.runLater(() -> {
                entryRows.setAll(data);
                refreshTable();
        }));
    }

    private void updateTableViewPage(long bookId) {
        getEntriesAfterUpdate(bookId).thenAccept(data -> Platform.runLater(() -> {
            entryRows.setAll(data);
            selectByBookId(bookId);
            refreshTable();
        }));
    }

    private void selectByBookId(long bookId) {
        FlatEntryDto entry = entryRows.stream().filter(r -> r.bookId() == bookId).findFirst().orElse(null);
        table.getSelectionModel().select(entry);
        table.scrollTo(entry);
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

    @FXML
    private void handleAddNewEntryButton() {
        boolean newEntryOnNewPage = lastPageIsFull();
        boolean currentPageIsNotLastPage = !onLastPage();
        
        try {
           FlatEntryDto entry = entryService.createEmptyEntry().orElseThrow();
           updateTableViewPage(entry.bookId());
                // calculateTotalPageCount();

                // if (newEntryOnNewPage) {
                //     entryRows.clear();
                //     currentPageIndex = totalPageCount - 1;
                //     entryRows.add(entry);
                // } else {
                //     if (currentPageIsNotLastPage) {
                //         goToLastPage();
                //     } else {
                //         entryRows.add(entry);
                //     }
                // }
                
                // table.getSelectionModel().selectLast();
                // table.scrollTo(table.getSelectionModel().getSelectedIndex());
               
                // refreshTable();

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
                    if (!toDeleteIsOnLastPage) {
                        // fillTable();
                        updateTableViewPage();
                    }
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
        // Object sortKey = lastEntry.bookTitle();
        // if (!table.getSortOrder().isEmpty()) {
        //     TableColumn<FlatEntryDto, ?> col = table.getSortOrder().get(0);
        //     sortKey = col.getCellData(entryRows.size() - 1);
        // }

        Optional<FlatEntryDto> nextEntry = entryService.getNextFlatEntryDtoAfterBookId(lastEntry.bookId());
        if (nextEntry.isPresent()) entryRows.addLast(nextEntry.get());
    }

    @FXML
    private void handleItemsPerPageSelection() {
        updateTotalPageCount();
        goToFirstPage();
    }

    private void updateTotalPageCount() {
         totalPageCount = (totalNumberOfRows + itemsPerPage.intValue() - 1) / itemsPerPage.intValue();
    }

    @FXML
    private void showDetails(FullEntryDto entry) {
        detailsPane.showDetails(entry);
        detailsPane.setSaveCallback(this::handleSave);
    }

    private void handleSave(FullEntryDto entry) {
        FlatEntryDto updatedEntry = entryService.update(entry);
        updateTableViewPage(entry.getBook().getId());      
    }
}

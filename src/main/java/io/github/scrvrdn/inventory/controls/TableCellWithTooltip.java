package io.github.scrvrdn.inventory.controls;

import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;

public class TableCellWithTooltip<S, T> extends TableCell<S, T> {
    private final Tooltip tooltip = new Tooltip();
    
    public TableCellWithTooltip() {
        tooltip.setFont(Font.font("Arial", 12));
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(300);
    }
    
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(null, empty);

        if (empty || item == null) {
            setText(null);
            setTooltip(null);
        } else {
            setText(item.toString());
            tooltip.setText(item.toString());
            setTooltip(tooltip);
        }
    }
}

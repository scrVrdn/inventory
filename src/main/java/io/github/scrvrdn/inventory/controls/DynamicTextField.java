package io.github.scrvrdn.inventory.controls;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;

public class DynamicTextField extends TextField {
    private static final double MIN_CHARS = 1;

    private TextFieldSkin skin;
    private double padding = 1.5;

    public DynamicTextField() {
        initSizing();
    }

    public DynamicTextField(String text) {
        super(text);
        initSizing();
    }

    private void initSizing() {
        prefColumnCountProperty().bind(Bindings.max(MIN_CHARS, lengthProperty()));

        skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin instanceof TextFieldSkin) {
                skin = (TextFieldSkin) newSkin;
                Platform.runLater(this::updateWidth);
            }
        });

        textProperty().addListener((obs, old, text) -> {
            if (skin != null) {
                Platform.runLater(this::updateWidth);
            }
        });
    }

    private void updateWidth() {
        String text = getText();
        if (skin == null || text == null || text.length() < MIN_CHARS) {
            setPrefWidth(-1);
            return;
        }

        try {
            int lastIdx = text.offsetByCodePoints(text.length(), -1);
            Rectangle2D first = skin.getCharacterBounds(0);
            Rectangle2D last = skin.getCharacterBounds(lastIdx);
            double width = last.getMaxX() - first.getMinX() + padding;
           
            setPrefWidth(width);
        } catch (Exception e) {
            System.err.print(e.getMessage());
        }        
    }
}

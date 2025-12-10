package com.example.demo.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

/**
 * Small helper to normalize TableView visuals.
 */
public final class TableUtils {
    private TableUtils() {}

    @SafeVarargs
    public static <T> void style(TableView<T> table, TableColumn<?, ?>... columns) {
        if (table != null) {
            table.setTableMenuButtonVisible(true); // show column menu (+)
        }
        if (columns != null) {
            for (TableColumn<?, ?> col : columns) {
                if (col != null) col.setStyle("-fx-alignment: CENTER; -fx-text-fill: #FFFFFF;");
            }
        }
    }

    /**
     * Creates a TableCell with dynamic ellipsis if text is too long and tooltip on hover.
     */
    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> createEllipsisCell() {
        return col -> new TableCell<>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    setTooltip(tooltip);
                    tooltip.setText(item);

                    // Dynamically add ellipsis if text is too wide
                    this.widthProperty().addListener((obs, oldW, newW) -> {
                        setText(fitTextToWidth(item, newW.doubleValue()));
                    });
                }
            }

            private String fitTextToWidth(String text, double width) {
                // Rough approximation: 7 pixels per character (adjust if needed)
                int maxChars = (int) (width / 7);
                if (text.length() > maxChars && maxChars > 3) {
                    return text.substring(0, maxChars - 3) + "…";
                }
                return text;
            }
        };
    }
}

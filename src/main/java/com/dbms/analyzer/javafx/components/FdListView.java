package com.dbms.analyzer.javafx.components;

import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import com.dbms.analyzer.model.FunctionalDependency;

public class FdListView extends ListView<FunctionalDependency> {

    public FdListView() {
        this.setCellFactory(param -> new FdListCell());
    }

    /**
     * Custom list cell for FD display
     */
    private static class FdListCell extends ListCell<FunctionalDependency> {

        private ContextMenu contextMenu;

        public FdListCell() {
            // Create context menu for FD operations
            contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                FunctionalDependency fd = getItem();
                getListView().getItems().remove(fd);
            });
            contextMenu.getItems().add(deleteItem);
        }

        @Override
        protected void updateItem(FunctionalDependency item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setContextMenu(null);
            } else {
                setText(item.toString());
                setContextMenu(contextMenu);
            }
        }
    }
}

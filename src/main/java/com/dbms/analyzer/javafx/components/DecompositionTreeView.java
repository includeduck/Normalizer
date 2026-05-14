package com.dbms.analyzer.javafx.components;

import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import com.dbms.analyzer.model.Relation;
import java.util.Set;

public class DecompositionTreeView extends TreeView<String> {

    public DecompositionTreeView() {
        this.setShowRoot(true);
    }

    /**
     * Builds and displays a decomposition tree
     */
    public void displayDecomposition(String rootName, Set<Relation> decomposedRelations) {
        TreeItem<String> root = new TreeItem<>(rootName);
        
        decomposedRelations.forEach(relation -> {
            TreeItem<String> relationItem = new TreeItem<>(
                relation.toString());
            
            // Add attributes
            TreeItem<String> attrsItem = new TreeItem<>(
                "Attributes: " + relation.getAttributes());
            
            // Add FDs
            TreeItem<String> fdsItem = new TreeItem<>(
                "Functional Dependencies: " + 
                relation.getFunctionalDependencies());
            
            relationItem.getChildren().addAll(attrsItem, fdsItem);
            root.getChildren().add(relationItem);
        });
        
        this.setRoot(root);
        root.setExpanded(true);
    }

    /**
     * Clears the tree
     */
    public void clear() {
        this.setRoot(null);
    }
}

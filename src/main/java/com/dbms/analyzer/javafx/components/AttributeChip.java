package com.dbms.analyzer.javafx.components;

import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import java.util.Set;

public class AttributeChip extends HBox {

    private Label label;

    public AttributeChip(String attributeName) {
        this.label = new Label(attributeName);
        this.getChildren().add(label);
        
        // Style the chip
        this.setStyle(
            "-fx-border-radius: 15; " +
            "-fx-border-color: #2196F3; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 5 10 5 10; " +
            "-fx-spacing: 5");
        
        this.setPadding(new Insets(5));
    }

    public String getAttributeName() {
        return label.getText();
    }

    /**
     * Creates a container with attribute chips
     */
    public static HBox createChipsContainer(Set<String> attributes) {
        HBox container = new HBox(5);
        attributes.forEach(attr ->
            container.getChildren().add(new AttributeChip(attr)));
        return container;
    }
}

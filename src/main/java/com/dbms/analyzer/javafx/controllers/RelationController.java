package com.dbms.analyzer.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.dbms.analyzer.service.RelationService;

@Controller
public class RelationController {

    @Autowired
    private RelationService relationService;

    @FXML
    private TextField relationInputField;

    @FXML
    public void initialize() {
        // Initialize relation input field
    }

    @FXML
    private void handleCreateRelation() {
        String input = relationInputField.getText().trim();
        
        if (input.isEmpty()) {
            showError("Please enter a relation schema");
            return;
        }

        try {
            relationService.createRelation(input);
            showInfo("Relation created successfully");
            relationInputField.clear();
        } catch (IllegalArgumentException e) {
            showError("Invalid schema: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

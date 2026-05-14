package com.dbms.analyzer.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.dbms.analyzer.service.FdService;

@Controller
public class FdController {

    @Autowired
    private FdService fdService;

    @FXML
    private TextField fdInputField;

    @FXML
    public void initialize() {
        // Initialize FD input field
    }

    @FXML
    private void handleAddFd() {
        String input = fdInputField.getText().trim();
        
        if (input.isEmpty()) {
            showError("Please enter a functional dependency");
            return;
        }

        try {
            fdService.addFunctionalDependency(input);
            showInfo("Functional dependency added successfully");
            fdInputField.clear();
        } catch (IllegalArgumentException e) {
            showError("Invalid FD: " + e.getMessage());
        } catch (IllegalStateException e) {
            showError(e.getMessage());
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

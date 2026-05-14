package com.dbms.analyzer.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.dbms.analyzer.service.ClosureService;

@Controller
public class ClosureController {

    @Autowired
    private ClosureService closureService;

    @FXML
    private TextField attributeInputField;

    @FXML
    private TextArea resultTextArea;

    @FXML
    public void initialize() {
        // Initialize closure controller
    }

    @FXML
    private void handleComputeClosure() {
        String input = attributeInputField.getText().trim();
        
        if (input.isEmpty()) {
            showError("Please enter attribute(s)");
            return;
        }

        try {
            var closure = closureService.computeClosure(input);
            var steps = closureService.computeClosureWithSteps(input);
            
            StringBuilder result = new StringBuilder();
            steps.forEach(step -> result.append(step).append("\n"));
            result.append("\nFinal Closure: ").append(closure);
            
            resultTextArea.setText(result.toString());
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

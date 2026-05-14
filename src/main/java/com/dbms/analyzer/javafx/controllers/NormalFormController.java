package com.dbms.analyzer.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.dbms.analyzer.service.NormalFormService;

@Controller
public class NormalFormController {

    @Autowired
    private NormalFormService normalFormService;

    @FXML
    private Label resultLabel;

    @FXML
    private TextArea explanationArea;

    @FXML
    private Button analyzeBtn;

    @FXML
    public void initialize() {
        // Initialize normal form controller
    }

    @FXML
    private void handleAnalyzeNormalForm() {
        try {
            var result = normalFormService.analyzeNormalForm();
            resultLabel.setText(
                "Highest Normal Form: " + 
                result.getHighestNormalForm());

            StringBuilder explanation = new StringBuilder();
            explanation.append("1NF: ").append(normalFormService.is1NF()).append("\n");
            explanation.append("2NF: ").append(normalFormService.is2NF()).append("\n");
            explanation.append("3NF: ").append(normalFormService.is3NF()).append("\n");
            explanation.append("BCNF: ").append(normalFormService.isBCNF()).append("\n\n");
            
            if (!result.getViolations().isEmpty()) {
                explanation.append("Violations:\n");
                result.getViolations().forEach(v ->
                    explanation.append("  - ").append(v).append("\n"));
            }

            explanationArea.setText(explanation.toString());
        } catch (Exception e) {
            explanationArea.setText("Error: " + e.getMessage());
        }
    }
}

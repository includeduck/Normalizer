package com.dbms.analyzer.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.dbms.analyzer.service.ExplanationService;

@Controller
public class ExplanationController {

    @Autowired
    private ExplanationService explanationService;

    @FXML
    private ComboBox<String> explanationTypeCombo;

    @FXML
    private TextArea explanationTextArea;

    @FXML
    private Button generateBtn;

    @FXML
    public void initialize() {
        if (explanationTypeCombo != null) {
            explanationTypeCombo.getItems().addAll(
                "Relation Overview",
                "Closure Computation",
                "Candidate Keys",
                "Normal Form Analysis");
            explanationTypeCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleGenerateExplanation() {
        try {
            String type = explanationTypeCombo.getValue();
            var steps = switch (type) {
                case "Relation Overview" -> 
                    explanationService.explainRelation();
                case "Closure Computation" -> 
                    explanationService.explainClosure("ABC");
                case "Candidate Keys" -> 
                    explanationService.explainCandidateKeys();
                case "Normal Form Analysis" -> 
                    explanationService.explainNormalForm();
                default -> java.util.List.of("Unknown explanation type");
            };

            StringBuilder result = new StringBuilder();
            steps.forEach(step -> result.append(step).append("\n"));
            explanationTextArea.setText(result.toString());
        } catch (Exception e) {
            explanationTextArea.setText("Error: " + e.getMessage());
        }
    }
}

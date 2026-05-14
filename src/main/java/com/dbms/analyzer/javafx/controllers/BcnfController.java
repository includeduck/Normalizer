package com.dbms.analyzer.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.dbms.analyzer.service.BcnfDecompositionService;

@Controller
public class BcnfController {

    @Autowired
    private BcnfDecompositionService bcnfDecompositionService;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<String> decomposeRelationsListView;

    @FXML
    private TextArea decompositionDetailsArea;

    @FXML
    private Button decomposeBtn;

    @FXML
    public void initialize() {
        // Initialize BCNF controller
    }

    @FXML
    private void handleDecomposeToBcnf() {
        try {
            if (bcnfDecompositionService.isAlreadyBcnf()) {
                statusLabel.setText("Relation is already in BCNF");
                decompositionDetailsArea.setText(
                    "No decomposition needed");
                decomposeRelationsListView.getItems().clear();
                return;
            }

            var decomposed = bcnfDecompositionService.decomposeToBcnf();
            decomposeRelationsListView.getItems().clear();
            decomposed.forEach(relation ->
                decomposeRelationsListView.getItems().add(
                    relation.toString()));

            statusLabel.setText(
                "Decomposition complete: " + decomposed.size() + 
                " relation(s)");
            
            StringBuilder details = new StringBuilder();
            decomposed.forEach(rel -> {
                details.append(rel.getName()).append(":\n");
                details.append("  Attributes: ")
                    .append(rel.getAttributes()).append("\n");
                details.append("  FDs: ").append(rel.getFunctionalDependencies())
                    .append("\n\n");
            });
            
            decompositionDetailsArea.setText(details.toString());
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            decompositionDetailsArea.setText(e.getMessage());
        }
    }
}

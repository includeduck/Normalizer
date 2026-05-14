package com.dbms.analyzer.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.dbms.analyzer.service.RelationService;
import com.dbms.analyzer.service.FdService;

@Controller
public class MainController {

    @Autowired
    private RelationService relationService;

    @Autowired
    private FdService fdService;

    @FXML
    private Label relationLabel;

    @FXML
    private ListView<String> fdListView;

    @FXML
    private Button createRelationBtn;

    @FXML
    private Button addFdBtn;

    @FXML
    private Button closureBtn;

    @FXML
    private Button candidateKeysBtn;

    @FXML
    private Button normalFormBtn;

    @FXML
    private Button bcnfBtn;

    @FXML
    public void initialize() {
        // Initialize UI components
        updateRelationDisplay();
    }

    @FXML
    private void handleCreateRelation() {
        // TODO: Implement relation creation dialog
    }

    @FXML
    private void handleAddFd() {
        // TODO: Implement FD addition dialog
    }

    @FXML
    private void handleComputeClosure() {
        // TODO: Implement closure computation
    }

    @FXML
    private void handleFindCandidateKeys() {
        // TODO: Implement candidate key finding
    }

    @FXML
    private void handleAnalyzeNormalForm() {
        // TODO: Implement normal form analysis
    }

    @FXML
    private void handleBcnfDecomposition() {
        // TODO: Implement BCNF decomposition
    }

    private void updateRelationDisplay() {
        if (relationService.hasRelation()) {
            relationLabel.setText(
                relationService.getCurrentRelation().toString());
            fdListView.getItems().clear();
            fdService.getAllDependencies().forEach(fd ->
                fdListView.getItems().add(fd.toString()));
        } else {
            relationLabel.setText("No relation defined");
            fdListView.getItems().clear();
        }
    }
}

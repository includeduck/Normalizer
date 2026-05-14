package com.dbms.analyzer.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.dbms.analyzer.service.CandidateKeyService;

@Controller
public class CandidateKeyController {

    @Autowired
    private CandidateKeyService candidateKeyService;

    @FXML
    private ListView<String> candidateKeysListView;

    @FXML
    private TextArea primeAttributesArea;

    @FXML
    private Button findKeysBtn;

    @FXML
    public void initialize() {
        // Initialize candidate key controller
    }

    @FXML
    private void handleFindCandidateKeys() {
        try {
            var keys = candidateKeyService.findAllCandidateKeys();
            candidateKeysListView.getItems().clear();
            keys.forEach(key -> 
                candidateKeysListView.getItems().add(key.toString()));

            var primeAttrs = candidateKeyService.getPrimeAttributes();
            var nonPrimeAttrs = candidateKeyService.getNonPrimeAttributes();
            
            StringBuilder result = new StringBuilder();
            result.append("Prime Attributes: ").append(primeAttrs).append("\n");
            result.append("Non-Prime Attributes: ").append(nonPrimeAttrs);
            
            primeAttributesArea.setText(result.toString());
        } catch (Exception e) {
            primeAttributesArea.setText("Error: " + e.getMessage());
        }
    }
}

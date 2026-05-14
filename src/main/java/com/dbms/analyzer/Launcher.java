package com.dbms.analyzer;

import com.dbms.analyzer.javafx.MainApp;
import javafx.application.Application;

public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}

package com.dbms.analyzer.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.dbms.analyzer.DbmsApplication;

public class MainApp extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void start(Stage stage) throws Exception {
        applicationContext = SpringApplication.run(DbmsApplication.class);
        
        FXMLLoader fxmlLoader = new FXMLLoader(
            getClass().getResource("/fxml/main.fxml"));
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("DBMS Normalization & Functional Dependency Analyzer");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        applicationContext.close();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

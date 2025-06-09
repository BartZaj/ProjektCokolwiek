package com.example.projektcokolwiek;

import javafx.application.Application;
import javafx.stage.Stage;

public class WordleFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        WordleGameUI gameUI = new WordleGameUI(primaryStage);
        gameUI.initialize();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package com.example.projektcokolwiek;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WordleFX extends Application {

    private String slowoDoZgadniecia;
    private char[] aktualnyStan;
    private Set<Character> zgadnieteLitery = new HashSet<>();
    private int proby = 0;
    private Label stanLabel = new Label();
    private Label komunikatLabel = new Label();
    private TextField inputField = new TextField();

    @Override
    public void start(Stage primaryStage) {
        List<String> slowa = wczytajSlowaZPliku("slowa.txt");
        if (slowa.isEmpty()) {
            showError("Brak słów w pliku!");
            return;
        }

        slowoDoZgadniecia = slowa.get(new Random().nextInt(slowa.size())).toLowerCase();
        aktualnyStan = new char[slowoDoZgadniecia.length()];
        Arrays.fill(aktualnyStan, '_');

        // UI
        Label tytul = new Label("Zgadnij słowo!");
        tytul.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        stanLabel.setText(String.valueOf(aktualnyStan));
        stanLabel.setStyle("-fx-font-size: 20px;");

        inputField.setPromptText("Wpisz literę");
        inputField.setMaxWidth(100);

        Button zgadnijBtn = new Button("Zgadnij");
        zgadnijBtn.setOnAction(e -> sprawdzLitere());

        VBox root = new VBox(10, tytul, stanLabel, inputField, zgadnijBtn, komunikatLabel);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-font-family: 'Segoe UI';");
        root.setPrefWidth(300);

        primaryStage.setTitle("WordleFX");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void sprawdzLitere() {
        String input = inputField.getText().toLowerCase();
        inputField.clear();

        if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
            komunikatLabel.setText("Podaj jedną literę!");
            return;
        }

        char litera = input.charAt(0);

        if (zgadnieteLitery.contains(litera)) {
            komunikatLabel.setText("Już próbowałeś tej litery.");
            return;
        }

        zgadnieteLitery.add(litera);
        proby++;

        boolean trafiona = false;
        for (int i = 0; i < slowoDoZgadniecia.length(); i++) {
            if (slowoDoZgadniecia.charAt(i) == litera) {
                aktualnyStan[i] = litera;
                trafiona = true;
            }
        }

        if (trafiona) {
            komunikatLabel.setText("Dobrze!");
        } else {
            komunikatLabel.setText("Niestety, tej litery nie ma.");
        }

        stanLabel.setText(String.valueOf(aktualnyStan));

        if (String.valueOf(aktualnyStan).equals(slowoDoZgadniecia)) {
            komunikatLabel.setText("Gratulacje! Odgadłeś słowo \"" + slowoDoZgadniecia + "\" w " + proby + " próbach.");
            inputField.setDisable(true);
        }
    }

    private List<String> wczytajSlowaZPliku(String nazwaPliku) {
        List<String> slowa = new ArrayList<>();
        try {
            List<String> linie = Files.readAllLines(Paths.get(nazwaPliku));
            for (String linia : linie) {
                if (linia.contains(";")) {
                    String[] czesci = linia.split(";");
                    slowa.add(czesci[0].trim().toLowerCase());
                } else {
                    slowa.add(linia.trim().toLowerCase());
                }
            }
        } catch (IOException e) {
            showError("Błąd podczas wczytywania pliku: " + e.getMessage());
        }
        return slowa;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


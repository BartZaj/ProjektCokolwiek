package com.example.projektcokolwiek;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WordleFX extends Application {

    private String slowoDoZgadniecia;
    private int proby;
    private static final int MAKS_PROB = 6;
    private static final int MAKS_DL_SLOWA = 6;

    private VBox wynikBox = new VBox(5);
    private Label komunikatLabel = new Label();
    private ComboBox<String> kategoriaBox = new ComboBox<>();
    private ComboBox<String> trybBox = new ComboBox<>();
    private Map<String, List<String>> kategorieMap = new HashMap<>();
    private List<String> aktualnaListaSlow;
    private VBox klawiaturaBox = new VBox(10);
    private Label tytul;

    @Override
    public void start(Stage primaryStage) {
        Font font36 = Font.loadFont(getClass().getResourceAsStream("/fonts/HelveticaNeueMedium.otf"), 36);
        Font font16 = Font.loadFont(getClass().getResourceAsStream("/fonts/HelveticaNeueMedium.otf"), 16);

        tytul = new Label("STUDLE");
        tytul.setFont(font36);
        tytul.setTextFill(Color.WHITE);
        tytul.setAlignment(Pos.CENTER);
        tytul.setMaxWidth(Double.MAX_VALUE);

        kategoriaBox.getItems().addAll("Zwierzęta", "Państwa", "Losowe");
        kategoriaBox.setPromptText("Wybierz kategorię");
        kategoriaBox.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        kategoriaBox.setOnAction(e -> rozpocznijGre());

        trybBox.getItems().addAll("Zgadywanie liter", "Zgadywanie słów");
        trybBox.setPromptText("Wybierz tryb");
        trybBox.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        trybBox.setOnAction(e -> rozpocznijGre());

        Button resetButton = new Button("Reset");
        resetButton.setFont(font16);
        resetButton.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        resetButton.setOnAction(e -> resetGame());

        HBox selectionPanel = new HBox(10, kategoriaBox, trybBox, resetButton);
        selectionPanel.setAlignment(Pos.CENTER);

        komunikatLabel.setFont(font16);
        komunikatLabel.setTextFill(Color.WHITE);
        komunikatLabel.setText("Wybierz kategorię i tryb, aby rozpocząć grę.");
        komunikatLabel.setAlignment(Pos.CENTER);
        komunikatLabel.setMaxWidth(Double.MAX_VALUE);

        wynikBox.setAlignment(Pos.CENTER);
        wynikBox.setPrefHeight((40 + 5) * MAKS_PROB);
        wynikBox.setStyle("-fx-background-color: black;");
        wynikBox.setVisible(false);

        utworzKlawiature(font16);
        klawiaturaBox.setVisible(false);

        VBox topPanel = new VBox(10, tytul, selectionPanel, komunikatLabel);
        topPanel.setAlignment(Pos.CENTER);

        VBox mainLayout = new VBox(15, topPanel, wynikBox, klawiaturaBox);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(mainLayout, 600, 800, Color.BLACK);
        scene.addEventFilter(KeyEvent.KEY_TYPED, this::handleTyped);

        wczytajKategorie();
        primaryStage.setTitle("WordleFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void rozpocznijGre() {
        String kat = kategoriaBox.getValue();
        String tryb = trybBox.getValue();
        if (kat == null || tryb == null) {
            komunikatLabel.setText("Wybierz kategorię i tryb.");
            return;
        }

        aktualnaListaSlow = kategorieMap.getOrDefault(kat, Collections.emptyList());
        if (aktualnaListaSlow.isEmpty()) {
            komunikatLabel.setText("Brak słów w kategorii.");
            return;
        }

        slowoDoZgadniecia = aktualnaListaSlow.get(new Random().nextInt(aktualnaListaSlow.size()));
        proby = 0;
        komunikatLabel.setText("Gra rozpoczęta! Wpisz literę lub słowo i naciśnij ENTER.");
        wynikBox.getChildren().clear();

        int len = slowoDoZgadniecia.length();
        for (int i = 0; i < MAKS_PROB; i++) {
            HBox row = new HBox(5);
            row.setAlignment(Pos.CENTER);
            for (int j = 0; j < len; j++) {
                Label slot = new Label();
                slot.setMinSize(40, 40);
                slot.setAlignment(Pos.CENTER);
                slot.setStyle("-fx-border-color: gray; -fx-border-width: 1px; -fx-background-color: black;");
                row.getChildren().add(slot);
            }
            wynikBox.getChildren().add(row);
        }
        wynikBox.setVisible(true);
        klawiaturaBox.setVisible(true);
    }

    private void resetGame() {
        slowoDoZgadniecia = null;
        proby = 0;
        kategoriaBox.getSelectionModel().clearSelection();
        trybBox.getSelectionModel().clearSelection();
        wynikBox.setVisible(false);
        klawiaturaBox.setVisible(false);
        komunikatLabel.setText("Wybierz kategorię i tryb, aby rozpocząć grę.");
        for (var row : klawiaturaBox.getChildren()) {
            if (row instanceof HBox) {
                for (var btn : ((HBox) row).getChildren()) {
                    Button b = (Button) btn;
                    b.setDisable(true);
                    b.setStyle("-fx-background-color: lightgray; -fx-text-fill: white; -fx-font-weight: bold;");
                }
            }
        }
    }

    private void handleTyped(KeyEvent e) {
        if (slowoDoZgadniecia == null) return;
        HBox row = (HBox) wynikBox.getChildren().get(proby);
        String chStr = e.getCharacter();
        if (chStr.isEmpty()) return;

        char c = chStr.charAt(0);
        if (c == '\r') {
            String guess = row.getChildren().stream()
                    .map(n -> ((Label) n).getText())
                    .reduce("", String::concat)
                    .toLowerCase();
            if (guess.length() == slowoDoZgadniecia.length()) {
                sprawdzOdpowiedz(guess);
            }
        } else if (c == '\b') {
            var slots = row.getChildren().stream().map(n -> (Label) n).toList();
            for (int i = slots.size() - 1; i >= 0; i--) {
                if (!slots.get(i).getText().isEmpty()) {
                    slots.get(i).setText("");
                    break;
                }
            }
        } else if (Character.isLetter(c)) {
            c = Character.toUpperCase(c);
            for (var node : row.getChildren()) {
                Label slot = (Label) node;
                if (slot.getText().isEmpty()) {
                    slot.setText(String.valueOf(c));
                    break;
                }
            }
        }
    }

    private void sprawdzOdpowiedz(String guess) {
        if (!aktualnaListaSlow.contains(guess)) {
            komunikatLabel.setText("Nie ma takiego słowa.");
            return;
        }
        HBox row = (HBox) wynikBox.getChildren().get(proby);
        for (int i = 0; i < guess.length(); i++) {
            Label slot = (Label) row.getChildren().get(i);
            char ch = guess.charAt(i);
            slot.setText(String.valueOf(ch).toUpperCase());
            if (ch == slowoDoZgadniecia.charAt(i)) {
                slot.setStyle(slot.getStyle().replace("black", "limegreen"));
                updateKeyStyle(ch, "limegreen");
            } else if (slowoDoZgadniecia.contains(String.valueOf(ch))) {
                slot.setStyle(slot.getStyle().replace("black", "gold"));
                updateKeyStyle(ch, "gold");
            } else {
                slot.setStyle(slot.getStyle().replace("black", "salmon"));
                updateKeyStyle(ch, "salmon");
            }
        }
        proby++;
        if (guess.equals(slowoDoZgadniecia)) {
            komunikatLabel.setText("Brawo! Odgadłeś słowo.");
        } else if (proby >= MAKS_PROB) {
            komunikatLabel.setText("Koniec gry! Hasło to: " + slowoDoZgadniecia.toUpperCase());
        } else {
            komunikatLabel.setText("Spróbuj dalej (" + (MAKS_PROB - proby) + " prób pozostało)");
        }
    }

    private void utworzKlawiature(Font font) {
        klawiaturaBox.getChildren().clear();
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM", "ĄĆĘŁŃÓŚŹŻ"};
        for (String r : rows) {
            HBox rowBox = new HBox(5);
            rowBox.setAlignment(Pos.CENTER);
            for (char c : r.toCharArray()) {
                Button btn = new Button(String.valueOf(c));
                btn.setFont(font);
                btn.setMinSize(30, 40);
                btn.setStyle("-fx-background-color: lightgray; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setDisable(true);
                rowBox.getChildren().add(btn);
            }
            klawiaturaBox.getChildren().add(rowBox);
        }
    }

    private void updateKeyStyle(char ch, String color) {
        for (var node : klawiaturaBox.getChildren()) {
            if (node instanceof HBox) {
                for (var btn : ((HBox) node).getChildren()) {
                    Button b = (Button) btn;
                    if (b.getText().charAt(0) == Character.toUpperCase(ch)) {
                        b.setDisable(false);
                        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
                        return;
                    }
                }
            }
        }
    }

    private void wczytajKategorie() {
        kategorieMap.put("Zwierzęta", wczytajSlowaZPliku("zwierzeta.txt"));
        kategorieMap.put("Państwa", wczytajSlowaZPliku("panstwa.txt"));
        kategorieMap.put("Losowe", wczytajSlowaZPliku("slowa.txt"));
    }

    private List<String> wczytajSlowaZPliku(String nazwa) {
        try {
            return Files.readAllLines(Paths.get(nazwa)).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .filter(s -> s.length() <= MAKS_DL_SLOWA)
                    .toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

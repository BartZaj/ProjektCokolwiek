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
    private int maksymalneProby = MAKS_PROB;
    private static final int MAKS_DL_SLOWA = 6;

    private VBox wynikBox = new VBox(5);
    private Label komunikatLabel = new Label();
    private ComboBox<String> kategoriaBox = new ComboBox<>();
    private ComboBox<String> trybBox = new ComboBox<>();
    private ComboBox<Integer> probyBox = new ComboBox<>();
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

        kategoriaBox.getItems().addAll("Zwierzęta", "Państwa", "Losowe", "Liczby");
        kategoriaBox.setPromptText("Wybierz kategorię");
        kategoriaBox.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        kategoriaBox.setOnAction(e -> {
            String selected = kategoriaBox.getValue();
            boolean liczby = "Liczby".equals(selected);

            trybBox.setDisable(liczby);
            //trybBox.setVisible(!liczby);
            //trybBox.setManaged(false);
            probyBox.setDisable(!liczby);
            //probyBox.setVisible(liczby);
            //probyBox.setManaged(false);

            if (liczby) {
                trybBox.getSelectionModel().clearSelection();
            }
            rozpocznijGre();
        });

        trybBox.getItems().addAll("Zgadywanie liter", "Zgadywanie słów");
        trybBox.setPromptText("Wybierz tryb");
        trybBox.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        trybBox.setOnAction(e -> rozpocznijGre());

        probyBox.getItems().addAll(6, 7, 8, 9, 10, 11, 12);
        probyBox.setPromptText("Liczba prób");
        probyBox.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        probyBox.setOnAction(e -> rozpocznijGre());

        Button resetButton = new Button("Reset");
        resetButton.setFont(font16);
        resetButton.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        resetButton.setOnAction(e -> resetGame());

        HBox selectionPanel = new HBox(10, kategoriaBox, trybBox, probyBox);
        selectionPanel.setAlignment(Pos.CENTER);

        HBox resetPanel = new HBox(resetButton);
        resetPanel.setAlignment(Pos.CENTER);

        VBox mainPanel = new VBox(10, selectionPanel, resetPanel);
        mainPanel.setAlignment(Pos.CENTER);

        komunikatLabel.setFont(font16);
        komunikatLabel.setTextFill(Color.WHITE);
        komunikatLabel.setText("Wybierz kategorię i tryb, aby rozpocząć grę.");
        komunikatLabel.setAlignment(Pos.CENTER);
        komunikatLabel.setMaxWidth(Double.MAX_VALUE);

        wynikBox.setAlignment(Pos.CENTER);
        wynikBox.setPrefHeight((40 + 5) * maksymalneProby);
        wynikBox.setStyle("-fx-background-color: black;");
        wynikBox.setVisible(false);

        utworzKlawiature(font16);
        klawiaturaBox.setVisible(false);

        VBox topPanel = new VBox(10, tytul, mainPanel, komunikatLabel);
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

        if (kat == null) {
            komunikatLabel.setText("Wybierz kategorię.");
            return;
        }

        if (!kat.equals("Liczby") && tryb == null) {
            komunikatLabel.setText("Wybierz tryb.");
            return;
        }

        // Obsługa liczby prób tylko dla kategorii "Liczby"
        if (kat.equals("Liczby")) {
            Integer wybraneProby = probyBox.getValue();
            if (wybraneProby == null) {
                komunikatLabel.setText("Wybierz liczbę prób.");
                return;
            }
            maksymalneProby = wybraneProby;
            slowoDoZgadniecia = String.format("%04d", new Random().nextInt(10000));
        } else {
            maksymalneProby = MAKS_PROB;
            aktualnaListaSlow = kategorieMap.getOrDefault(kat, Collections.emptyList());
            if (aktualnaListaSlow.isEmpty()) {
                komunikatLabel.setText("Brak słów w kategorii.");
                return;
            }
            slowoDoZgadniecia = aktualnaListaSlow.get(new Random().nextInt(aktualnaListaSlow.size()));
        }

        proby = 0;
        komunikatLabel.setText("Gra rozpoczęta! Wpisz dane i naciśnij ENTER.");
        wynikBox.getChildren().clear();

        int len = slowoDoZgadniecia.length();
        for (int i = 0; i < maksymalneProby; i++) {
            HBox row = new HBox(5);
            row.setAlignment(Pos.CENTER);

            for (int j = 0; j < len; j++) {
                Label slot = new Label();
                slot.setMinSize(40, 40);
                slot.setAlignment(Pos.CENTER);
                slot.setStyle("-fx-border-color: gray; -fx-border-width: 1px; -fx-background-color: black;");
                row.getChildren().add(slot);
            }

            // Jeśli kategoria to "Liczby", dodajemy Label z wynikiem trafień po prawej
            if ("Liczby".equals(kategoriaBox.getValue())) {
                Label trafioneLabel = new Label();
                trafioneLabel.setMinWidth(120);
                trafioneLabel.setTextFill(Color.WHITE);
                trafioneLabel.setFont(Font.font(16));
                trafioneLabel.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().add(trafioneLabel);
            }

            wynikBox.getChildren().add(row);
        }

        wynikBox.setVisible(true);
        klawiaturaBox.setVisible(!kat.equals("Liczby"));
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
                    .reduce("", String::concat);

            if (guess.length() == slowoDoZgadniecia.length()) {
                if ("Liczby".equals(kategoriaBox.getValue())) {
                    sprawdzLiczbe(guess);
                } else {
                    sprawdzOdpowiedz(guess.toLowerCase());
                }
            }
        } else if (c == '\b') {
            var slots = row.getChildren().stream().map(n -> (Label) n).toList();
            for (int i = slots.size() - 1; i >= 0; i--) {
                if (!slots.get(i).getText().isEmpty()) {
                    slots.get(i).setText("");
                    break;
                }
            }
        } else if (("Liczby".equals(kategoriaBox.getValue()) && Character.isDigit(c))
                || (!"Liczby".equals(kategoriaBox.getValue()) && Character.isLetter(c))) {

            for (var node : row.getChildren()) {
                Label slot = (Label) node;
                if (slot.getText().isEmpty()) {
                    slot.setText(String.valueOf(c));
                    break;
                }
            }
        }
    }

    private void sprawdzLiczbe(String guess) {
        int trafienia = 0;
        for (int i = 0; i < 4; i++) {
            if (guess.charAt(i) == slowoDoZgadniecia.charAt(i)) {
                trafienia++;
            }
        }

        HBox row = (HBox) wynikBox.getChildren().get(proby);
        for (int i = 0; i < guess.length(); i++) {
            Label slot = (Label) row.getChildren().get(i);
            char ch = guess.charAt(i);
            slot.setText(String.valueOf(ch));
            // Tu nie zmieniamy kolorów na bieżąco, tylko zostawiamy czarne lub domyślne
            slot.setStyle(slot.getStyle().replace("limegreen", "black")); // reset koloru na czarny
        }

// Sprawdzenie czy zgadnięto całą liczbę
        boolean zgadniete = guess.equals(slowoDoZgadniecia);
        if (zgadniete) {
            // Jeśli zgadnięto całe słowo/liczbę - ustaw zielony kolor dla całego wiersza
            for (int i = 0; i < guess.length(); i++) {
                Label slot = (Label) row.getChildren().get(i);
                slot.setStyle(slot.getStyle().replace("black", "limegreen"));
            }
        }

        // Ustaw tekst trafionych cyfr w dodatkowym Labelu po prawej
        if (row.getChildren().size() > 4) { // Sprawdzamy czy jest ten Label (indeks 4)
            Label trafioneLabel = (Label) row.getChildren().get(4);
            trafioneLabel.setText("Trafione cyfry: " + trafienia);
        }

        proby++;
        if (trafienia == 4) {
            komunikatLabel.setText("Brawo! Odgadłeś liczbę: " + slowoDoZgadniecia);
        } else if (proby >= maksymalneProby) {
            komunikatLabel.setText("Koniec gry! Liczba to: " + slowoDoZgadniecia);
        } else {
            komunikatLabel.setText("Poprawnych cyfr na miejscu: " + trafienia + ". Pozostało prób: " + (maksymalneProby - proby));
        }
    }

    private void sprawdzOdpowiedz(String guess) {
        if (kategoriaBox.getValue().equals("Liczby")) {
            if (guess.length() != 4 || !guess.matches("\\d{4}")) {
                komunikatLabel.setText("Podaj dokładnie 4 cyfry.");
                return;
            }

            int correctDigits = 0;
            for (int i = 0; i < 4; i++) {
                if (guess.charAt(i) == slowoDoZgadniecia.charAt(i)) {
                    correctDigits++;
                }
            }

            HBox row = (HBox) wynikBox.getChildren().get(proby);
            for (int i = 0; i < 4; i++) {
                Label slot = (Label) row.getChildren().get(i);
                slot.setText(String.valueOf(guess.charAt(i)));
                slot.setStyle("-fx-background-color: gray; -fx-border-color: white; -fx-border-width: 1px;");
            }

            proby++;
            if (correctDigits == 4) {
                komunikatLabel.setText("Brawo! Odgadłeś liczbę: " + slowoDoZgadniecia);
            } else if (proby >= maksymalneProby) {
                komunikatLabel.setText("Koniec gry! Liczba to: " + slowoDoZgadniecia);
            } else {
                komunikatLabel.setText("Trafionych cyfr na dobrych miejscach: " + correctDigits +
                        ". Pozostało prób: " + (maksymalneProby - proby));
            }
            return;
        }
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
        } else if (proby >= maksymalneProby) {
            komunikatLabel.setText("Koniec gry! Hasło to: " + slowoDoZgadniecia.toUpperCase());
        } else {
            komunikatLabel.setText("Spróbuj dalej (" + (maksymalneProby - proby) + " prób pozostało)");
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
        kategorieMap.put("Liczby", Collections.singletonList("0000")); // placeholder
    }

    private List<String> wczytajSlowaZPliku(String nazwa) {
        try {
            return Files.readAllLines(Paths.get(nazwa)).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .filter(s -> s.length() <= maksymalneProby)
                    .toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

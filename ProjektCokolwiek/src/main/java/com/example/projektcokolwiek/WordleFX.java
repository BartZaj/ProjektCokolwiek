package com.example.projektcokolwiek;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class WordleFX extends Application {

    private String slowoDoZgadniecia;
    private int proby = 0;
    private TextField inputField = new TextField();
    private VBox wynikBox = new VBox(5);
    private Label komunikatLabel = new Label();
    private ComboBox<String> kategoriaBox = new ComboBox<>();
    private ComboBox<String> trybBox = new ComboBox<>();
    private Button startBtn = new Button("Start");
    private Map<String, List<String>> kategorieMap = new HashMap<>();
    private static final int MAKS_PRÓB = 6;
    private static final int MAKS_DL_SLOWA = 6;
    private final Map<Character, Button> klawiaturaMap = new HashMap<>();
    private GridPane klawiaturaPane = new GridPane();
    private List<String> aktualnaListaSlow = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        // Tytuł i kontrolki wyboru
        Label tytul = new Label("Gra słowna");
        tytul.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        kategoriaBox.getItems().addAll("Zwierzęta", "Państwa", "Losowe");
        trybBox.getItems().addAll("Zgadywanie liter", "Zgadywanie słów");

        startBtn.setOnAction(e -> rozpocznijGre());

        inputField.setPromptText("Wpisz literę lub słowo");
        inputField.setDisable(true);

        Button zgadnijBtn = new Button("Zgadnij");
        zgadnijBtn.setOnAction(e -> sprawdzOdpowiedz());
        zgadnijBtn.setDisable(true);

        // Wyniki z przewijaniem
        ScrollPane scrollPane = new ScrollPane(wynikBox);
        scrollPane.setPrefHeight(400);
        scrollPane.setFitToWidth(true);
        wynikBox.setPadding(new Insets(10));
        wynikBox.setPrefWidth(400);

        // Klawiatura
        klawiaturaPane.setPadding(new Insets(10));
        utworzKlawiature();

        VBox topPanel = new VBox(10,
                tytul,
                new Label("Wybierz kategorię:"), kategoriaBox,
                new Label("Wybierz tryb gry:"), trybBox,
                startBtn,
                inputField,
                zgadnijBtn,
                komunikatLabel
        );

        VBox mainLayout = new VBox(15,
                topPanel,
                scrollPane,
                klawiaturaPane
        );

        mainLayout.setPadding(new Insets(20));
        wczytajKategorie();
        Scene scene = new Scene(mainLayout, 600, 800);  // zwiększona wysokość
        primaryStage.setTitle("WordleFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // opcjonalnie: blokuj rozmiar
        primaryStage.show();

        startBtn.setUserData(zgadnijBtn);
    }


    private void rozpocznijGre() {
        String kategoria = kategoriaBox.getValue();
        String tryb = trybBox.getValue();

        if (kategoria == null || tryb == null) {
            komunikatLabel.setText("Wybierz kategorię i tryb gry.");
            return;
        }

        List<String> slowa = kategorieMap.getOrDefault(kategoria, Collections.emptyList());
        if (slowa.isEmpty()) {
            komunikatLabel.setText("Brak słów w wybranej kategorii.");
            return;
        }

        aktualnaListaSlow = slowa; // <-- zapamiętaj listę słów

        slowoDoZgadniecia = slowa.get(new Random().nextInt(slowa.size())).toLowerCase();
        proby = 0;
        wynikBox.getChildren().clear();
        inputField.setDisable(false);
        ((Button) startBtn.getUserData()).setDisable(false);
        komunikatLabel.setText("Gra rozpoczęta! Długość słowa: " + slowoDoZgadniecia.length());
    }


    private void sprawdzOdpowiedz() {
        String input = inputField.getText().toLowerCase().trim();
        inputField.clear();

        if (input.isEmpty()) {
            komunikatLabel.setText("Wpisz coś!");
            return;
        }

        if (proby >= MAKS_PRÓB) {
            komunikatLabel.setText("Koniec gry! Wykorzystano wszystkie próby. Hasło to: " + slowoDoZgadniecia.toUpperCase());
            inputField.setDisable(true);
            return;
        }

        proby++;

        if ("Zgadywanie liter".equals(trybBox.getValue())) {
            if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
                komunikatLabel.setText("Wpisz tylko jedną literę.");
                return;
            }

            char litera = input.charAt(0);
            char[] wynik = new char[slowoDoZgadniecia.length()];
            Arrays.fill(wynik, '_');

            for (int i = 0; i < slowoDoZgadniecia.length(); i++) {
                if (slowoDoZgadniecia.charAt(i) == litera) {
                    wynik[i] = litera;
                } else if (wynikBox.getChildren().size() > 0) {
                    wynik[i] = ((Text) wynikBox.getChildren().get(wynikBox.getChildren().size() - 1)).getText().charAt(i);
                }
            }

            Text wynikText = new Text(String.valueOf(wynik));
            wynikBox.getChildren().add(wynikText);

            if (String.valueOf(wynik).equals(slowoDoZgadniecia)) {
                komunikatLabel.setText("Gratulacje! Odgadłeś słowo w " + proby + " próbach.");
                inputField.setDisable(true);
            } else {
                komunikatLabel.setText("Spróbuj dalej!");
            }

        } else { // Tryb zgadywania słów
            if (input.length() != slowoDoZgadniecia.length()) {
                komunikatLabel.setText("Słowo musi mieć " + slowoDoZgadniecia.length() + " liter.");
                return;
            }

            // Walidacja: czy słowo znajduje się w aktualnej liście słów
            if (!aktualnaListaSlow.contains(input)) {
                switch (kategoriaBox.getValue()) {
                    case "Państwa" -> komunikatLabel.setText("Nie ma takiego państwa.");
                    case "Zwierzęta" -> komunikatLabel.setText("Nie ma takiego zwierzęcia.");
                    default -> komunikatLabel.setText("Nie ma takiego słowa.");
                }
                proby--; // Nie licz błędnej próby
                return;
            }

            HBox kolorowyWynik = new HBox(5);
            for (int i = 0; i < input.length(); i++) {
                char ch = input.charAt(i);
                Label literaLabel = new Label(String.valueOf(ch).toUpperCase());

                int szerokosc = 360 / slowoDoZgadniecia.length(); // dostosuj do długości słowa
                literaLabel.setMinSize(szerokosc - 10, 40);
                literaLabel.setAlignment(javafx.geometry.Pos.CENTER);
                literaLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-border-color: black; -fx-border-width: 1px;");

                if (ch == slowoDoZgadniecia.charAt(i)) {
                    literaLabel.setBackground(new Background(new BackgroundFill(Color.LIMEGREEN, new CornerRadii(5), Insets.EMPTY)));
                } else if (slowoDoZgadniecia.contains(String.valueOf(ch))) {
                    literaLabel.setBackground(new Background(new BackgroundFill(Color.GOLD, new CornerRadii(5), Insets.EMPTY)));
                } else {
                    literaLabel.setBackground(new Background(new BackgroundFill(Color.SALMON, new CornerRadii(5), Insets.EMPTY)));
                }

                kolorowyWynik.getChildren().add(literaLabel);
            }

            wynikBox.getChildren().add(kolorowyWynik);
            aktualizujKlawiature(input);

            if (input.equals(slowoDoZgadniecia)) {
                komunikatLabel.setText("Brawo! Odgadłeś całe słowo.");
                inputField.setDisable(true);
            } else if (proby >= MAKS_PRÓB) {
                komunikatLabel.setText("Koniec gry! Hasło to: " + slowoDoZgadniecia.toUpperCase());
                inputField.setDisable(true);
            } else {
                komunikatLabel.setText("Zła próba. Próbuj dalej.");
            }
        }
    }


    private void wczytajKategorie() {
        kategorieMap.put("Zwierzęta", wczytajSlowaZPliku("zwierzeta.txt"));
        kategorieMap.put("Państwa", wczytajSlowaZPliku("panstwa.txt"));
        kategorieMap.put("Losowe", wczytajSlowaZPliku("slowa.txt"));
    }

    private List<String> wczytajSlowaZPliku(String nazwaPliku) {
        try {
            return Files.readAllLines(Paths.get(nazwaPliku)).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .filter(s -> s.length() <= MAKS_DL_SLOWA)
                    .toList();
        } catch (IOException e) {
            System.err.println("Błąd odczytu pliku: " + nazwaPliku + " - " + e.getMessage());
            return Collections.emptyList();
        }
    }
    private void utworzKlawiature() {
        klawiaturaPane.getChildren().clear();
        klawiaturaMap.clear();
        klawiaturaPane.setHgap(5);
        klawiaturaPane.setVgap(5);
        klawiaturaPane.setPadding(new Insets(10));

        String[] wiersze = {
                "QWERTYUIOP",
                "ASDFGHJKL",
                "ZXCVBNM",
                "ĄĆĘŁŃÓŚŹŻ"  // <- polskie znaki w osobnym rzędzie
        };

        for (int w = 0; w < wiersze.length; w++) {
            String rzad = wiersze[w];
            for (int k = 0; k < rzad.length(); k++) {
                char litera = rzad.charAt(k);
                Button btn = new Button(String.valueOf(litera));
                btn.setMinSize(30, 40);
                btn.setStyle("-fx-background-color: lightgray; -fx-font-weight: bold;");
                btn.setDisable(true);  // opcjonalnie: aktywne przyciski
                klawiaturaMap.put(litera, btn);
                klawiaturaPane.add(btn, k, w);
            }
        }
    }

    private void aktualizujKlawiature(String input) {
        input = input.toLowerCase();  // zapewnij spójność

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            Button btn = klawiaturaMap.get(Character.toUpperCase(ch));
            if (btn == null) continue;

            if (ch == slowoDoZgadniecia.charAt(i)) {
                btn.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
            } else if (slowoDoZgadniecia.contains(String.valueOf(ch))) {
                if (!btn.getStyle().contains("green")) {
                    btn.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-font-weight: bold;");
                }
            } else {
                if (!btn.getStyle().contains("green") && !btn.getStyle().contains("orange")) {
                    btn.setStyle("-fx-background-color: darkgray; -fx-text-fill: white; -fx-font-weight: bold;");
                }
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}

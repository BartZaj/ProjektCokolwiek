package com.example.projektcokolwiek;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import static com.example.projektcokolwiek.KeyboardHelper.aktualizujKlawiature;

public class WordleGameUI {
    private String slowoDoZgadniecia;
    private int proby;
    private static final int MAKS_PROB = 6;
    private int maksymalneProby = MAKS_PROB;
    private boolean trybZgadywaniaLiter = false;
    private char[] odkryteLitery;
    private List<String> aktualnaListaSlow;

    private VBox wynikBox = new VBox(5);
    private VBox klawiaturaBox = new VBox(10);
    private Label komunikatLabel = new Label();
    private ComboBox<String> kategoriaBox = new ComboBox<>();
    private ComboBox<String> trybBox = new ComboBox<>();
    private ComboBox<Integer> probyBox = new ComboBox<>();
    private Label tytul;
    private Map<String, List<String>> kategorieMap = new HashMap<>();
    private boolean graTrwa;
    private TextField hiddenFocusField = new TextField();
    private Stage primaryStage;
    public WordleGameUI(Stage stage) {
        this.primaryStage = stage;
    }

    public void initialize() {
        Font font36;
        Font font16;
        try {
            font36 = Font.loadFont(getClass().getResourceAsStream("/fonts/HelveticaNeueMedium.otf"), 36);
            font16 = Font.loadFont(getClass().getResourceAsStream("/fonts/HelveticaNeueMedium.otf"), 16);
        } catch (Exception e) {
            font36 = Font.font("System", 36);
            font16 = Font.font("System", 16);
            komunikatLabel.setText("Błąd ładowania czcionek");
        }
        hiddenFocusField.setOpacity(0); // całkowicie niewidoczny
        hiddenFocusField.setFocusTraversable(true);
        hiddenFocusField.setMaxSize(1, 1);
        hiddenFocusField.setManaged(false); // żeby nie zabierał miejsca w layouci
        tytul = new Label("STUDLE");
        tytul.setFont(font36);
        tytul.setTextFill(Color.WHITE);
        tytul.setAlignment(Pos.CENTER);
        tytul.setMaxWidth(Double.MAX_VALUE);

        kategoriaBox.getItems().addAll("Zwierzęta", "Rośliny", "Państwa", "Miasta", "Imiona", "Losowe", "Liczby");
        kategoriaBox.setPromptText("Wybierz kategorię");
        kategoriaBox.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        graTrwa = true;
        kategoriaBox.setOnAction(e -> {
            clearGameDisplay();
            String sel = kategoriaBox.getValue();
            boolean liczby = "Liczby".equals(sel);

            graTrwa = true; // <-- DODAJ TO TUTAJ

            kategoriaBox.setDisable(graTrwa);
            trybBox.setDisable(liczby);
            trybBox.setValue(null);
            probyBox.setDisable(!liczby);
            probyBox.setValue(null);
            komunikatLabel.setText("Wybierz tryb i/lub liczbę prób.");
        });


        trybBox.getItems().addAll("Zgadywanie liter", "Zgadywanie słów");
        trybBox.setPromptText("Wybierz tryb");
        trybBox.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        trybBox.setDisable(true);
        trybBox.setOnAction(e -> {
            rozpocznijGre();
            trybBox.setDisable(true);
            Platform.runLater(() -> hiddenFocusField.requestFocus());
        });

        probyBox.getItems().addAll(6, 7, 8, 9, 10, 11, 12);
        probyBox.setPromptText("Liczba prób");
        probyBox.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        probyBox.setDisable(true);
        probyBox.setOnAction(e -> {
            rozpocznijGre();
            probyBox.setDisable(true);
            Platform.runLater(() -> hiddenFocusField.requestFocus());
        });

        Button resetButton = new Button("Reset");
        resetButton.setFont(font16);
        resetButton.setStyle("-fx-font-family: 'HelveticaNeueMedium'; -fx-font-size: 16px;");
        resetButton.setOnAction(e -> {
            zresetujGre();
            PauseTransition delay = new PauseTransition(Duration.millis(50));
            delay.setOnFinished(ev -> zresetujGre());
            delay.play();
        });

        HBox selectionPanel = new HBox(10, kategoriaBox, trybBox, probyBox);
        selectionPanel.setAlignment(Pos.CENTER);
        HBox resetPanel = new HBox(resetButton);
        resetPanel.setAlignment(Pos.CENTER);
        VBox controls = new VBox(10, selectionPanel, resetPanel, hiddenFocusField);
        controls.setAlignment(Pos.CENTER);

        komunikatLabel.setFont(font16);
        komunikatLabel.setTextFill(Color.WHITE);
        komunikatLabel.setText("Wybierz kategorię, aby zacząć.");
        komunikatLabel.setAlignment(Pos.CENTER);
        komunikatLabel.setMaxWidth(Double.MAX_VALUE);

        wynikBox.setAlignment(Pos.CENTER);
        wynikBox.setPrefHeight((40 + 5) * MAKS_PROB);
        wynikBox.setStyle("-fx-background-color: black;");
        wynikBox.setVisible(false);

        KeyboardHelper.utworzKlawiature(font16, klawiaturaBox);
        klawiaturaBox.setVisible(false);

        VBox top = new VBox(10, tytul, controls, komunikatLabel);
        top.setAlignment(Pos.CENTER);
        VBox root = new VBox(15, top, wynikBox, klawiaturaBox);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, 600, 800, Color.BLACK);
        scene.addEventFilter(KeyEvent.KEY_TYPED, this::zarzadzajKlawiszami);

        try {
            wczytajKategorie();
        } catch (Exception e) {
            System.err.println("Błąd wczytywania kategorii: " + e.getMessage());
            komunikatLabel.setText("Nie udało się załadować słowników");
        }

        this.primaryStage.setTitle("WordleFX");
        this.primaryStage.setScene(scene);
        this.primaryStage.setResizable(false);
        this.primaryStage.show();
    }
    private void rozpocznijGre() {
        Platform.runLater(() -> hiddenFocusField.requestFocus());
        trybZgadywaniaLiter = false;
        String kat = kategoriaBox.getValue();
        String tryb = trybBox.getValue();
        graTrwa = true;
        if (kat == null) {
            komunikatLabel.setText("Wybierz kategorię.");
            return;
        }
        if (!kat.equals("Liczby") && tryb == null) {
            komunikatLabel.setText("Wybierz tryb.");
            return;
        }
        if (kat.equals("Liczby")) {
            Integer wybrane = probyBox.getValue();
            if (wybrane == null) {
                komunikatLabel.setText("Wybierz liczbę prób.");
                return;
            }
            maksymalneProby = wybrane;
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
        if ("Zgadywanie liter".equals(tryb)) {
            trybZgadywaniaLiter = true;
            slowoDoZgadniecia = slowoDoZgadniecia.toLowerCase();
            odkryteLitery = new char[slowoDoZgadniecia.length()];
            Arrays.fill(odkryteLitery, '_');
            wynikBox.getChildren().clear();
            HBox row = new HBox(5);
            row.setAlignment(Pos.CENTER);
            for (char c : odkryteLitery) {
                Label slot = new Label("_");
                slot.setMinSize(40, 40);
                slot.setAlignment(Pos.CENTER);
                slot.setStyle("-fx-border-color: gray; -fx-border-width: 1px; -fx-background-color: black; -fx-text-fill: white;");
                row.getChildren().add(slot);
            }
            wynikBox.getChildren().add(row);
            wynikBox.setVisible(true);
            klawiaturaBox.setVisible(true);
            komunikatLabel.setText("Zgadnij litery!");
            proby = 0;
            return;
        }
        proby = 0;
        komunikatLabel.setText("Gra rozpoczęta! Wpisz i naciśnij ENTER.");
        wynikBox.getChildren().clear();
        int len = slowoDoZgadniecia.length();
        for (int i = 0; i < maksymalneProby; i++) {
            HBox row = new HBox(5);
            row.setAlignment(Pos.CENTER);
            for (int j = 0; j < len; j++) {
                Label slot = new Label();
                slot.setMinSize(40, 40);
                slot.setAlignment(Pos.CENTER);
                slot.setStyle("-fx-border-color: gray; -fx-border-width: 1px; -fx-background-color: black; -fx-text-fill: white;");
                row.getChildren().add(slot);
            }
            if (kat.equals("Liczby")) {
                Label traf = new Label();
                traf.setMinWidth(120);
                traf.setTextFill(Color.WHITE);
                traf.setFont(Font.font(16));
                traf.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().add(traf);
            }
            wynikBox.getChildren().add(row);
        }
        wynikBox.setVisible(true);
        klawiaturaBox.setVisible(!kat.equals("Liczby"));
    }

    private void clearGameDisplay() {
        slowoDoZgadniecia = null;
        proby = 0;
        trybZgadywaniaLiter = false;
        odkryteLitery = null;
        wynikBox.getChildren().clear();
        wynikBox.setVisible(false);
        klawiaturaBox.setVisible(false);
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

    private void zresetujGre() {
        clearGameDisplay();
        graTrwa = false;



        kategoriaBox.setDisable(graTrwa);
        kategoriaBox.setValue(null);

        trybBox.setDisable(true);
        trybBox.setValue(null);

        probyBox.setDisable(true);
        probyBox.setValue(null);

        odswiezComboBox(kategoriaBox, "Wybierz kategorię");
        odswiezComboBox(trybBox, "Wybierz tryb");
        odswiezComboBox(probyBox, "Liczba prób");
        komunikatLabel.setText("Wybierz kategorię, aby zacząć.");

    }
    private <T> void odswiezComboBox(ComboBox<T> comboBox, String prompt) {
        List<T> items = new ArrayList<>(comboBox.getItems());

        comboBox.getSelectionModel().clearSelection();
        comboBox.setValue(null);
        comboBox.setItems(FXCollections.observableArrayList());

        comboBox.setPromptText(null);
        comboBox.setPromptText(prompt);

        comboBox.setItems(FXCollections.observableArrayList(items));
    }

    private void zarzadzajKlawiszami (KeyEvent e) {
        try {
            if (slowoDoZgadniecia == null || !graTrwa) return;

            String chStr = e.getCharacter();
            if (chStr.isEmpty()) return;

            char c = chStr.charAt(0);

            if (trybZgadywaniaLiter) {
                if (Character.isLetter(c)) {
                    sprawdzZgadywanieLiter(Character.toLowerCase(c));
                }
                return;
            }

            if (proby >= wynikBox.getChildren().size()) {
                graTrwa = false;
                return;
            }

            HBox row = (HBox) wynikBox.getChildren().get(proby);

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
                var slots = row.getChildren().stream()
                        .filter(n -> n instanceof Label)
                        .map(n -> (Label) n)
                        .toList();

                for (int i = slots.size() - 1; i >= 0; i--) {
                    if (!slots.get(i).getText().isEmpty()) {
                        slots.get(i).setText("");
                        break;
                    }
                }
            } else if (("Liczby".equals(kategoriaBox.getValue()) && Character.isDigit(c))
                    || (!"Liczby".equals(kategoriaBox.getValue()) && Character.isLetter(c))) {

                for (int i = 0; i < slowoDoZgadniecia.length(); i++) {
                    Label slot = (Label) row.getChildren().get(i);
                    if (slot.getText().isEmpty()) {
                        slot.setText(String.valueOf(c));
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            graTrwa = false;
            komunikatLabel.setText("Wystąpił błąd podczas przetwarzania wprowadzania");
        }
    }


    private void sprawdzZgadywanieLiter(char litera) {
        boolean trafiona = false;
        if(!graTrwa) return;
        for (int i = 0; i < slowoDoZgadniecia.length(); i++) {
            if (slowoDoZgadniecia.charAt(i) == litera) {
                odkryteLitery[i] = litera;
                trafiona = true;
            }
            else if (proby >= maksymalneProby) {
                komunikatLabel.setText("Koniec gry! Hasło to: " + slowoDoZgadniecia.toUpperCase());
                graTrwa = false;
                return;
            }
        }

        aktualizujWynikBoxZLiterami();

        if (!trafiona) {
            proby++;
        }

        if (String.valueOf(odkryteLitery).equals(slowoDoZgadniecia)) {
            komunikatLabel.setText("Brawo! Odgadłeś słowo.");
            graTrwa = false;
            return;
        } else if (proby >= maksymalneProby) {
            aktualizujKlawiature(litera, trafiona ? "limegreen" : "salmon", klawiaturaBox);
            komunikatLabel.setText("Koniec gry! Hasło to: " + slowoDoZgadniecia.toUpperCase());
            graTrwa = false;
            return;
        } else {
            komunikatLabel.setText("Pozostało prób: " + (maksymalneProby - proby));
        }

        aktualizujKlawiature(litera, trafiona ? "limegreen" : "salmon", klawiaturaBox);

    }


    private void aktualizujWynikBoxZLiterami() {
        HBox row = (HBox) wynikBox.getChildren().get(0);
        row.getChildren().clear();
        for (char ch : odkryteLitery) {
            Label slot = new Label(ch == '_' ? "_" : String.valueOf(Character.toUpperCase(ch)));
            slot.setMinSize(40, 40);
            slot.setAlignment(Pos.CENTER);
            slot.setStyle("-fx-border-color: gray; -fx-border-width: 1px; -fx-background-color: black; -fx-text-fill: white;");
            row.getChildren().add(slot);
        }
    }

    private void sprawdzLiczbe(String guess) {
        try {
            int trafienia = 0;
            for (int i = 0; i < 4; i++) {
                if (guess.charAt(i) == slowoDoZgadniecia.charAt(i)) trafienia++;
            }
            HBox row = (HBox) wynikBox.getChildren().get(proby);
            for (int i = 0; i < 4; i++) {
                Label slot = (Label) row.getChildren().get(i);
                slot.setText(String.valueOf(guess.charAt(i)));
                slot.setTextFill(Color.WHITE);
                slot.setStyle("-fx-background-color: black; -fx-border-color: gray; -fx-border-width: 1px;");
            }
            boolean zgadniete = guess.equals(slowoDoZgadniecia);
            if (zgadniete) {
                for (int i = 0; i < 4; i++) {
                    Label slot = (Label) row.getChildren().get(i);
                    slot.setStyle(slot.getStyle().replace("black", "limegreen"));
                }
            }
            if (row.getChildren().size() > 4) {
                Label trafioneLabel = (Label) row.getChildren().get(4);
                trafioneLabel.setText("Trafione cyfry: " + trafienia);
            }
            proby++;
            if (trafienia == 4) komunikatLabel.setText("Brawo! Odgadłeś liczbę: " + slowoDoZgadniecia);
            else if (proby >= maksymalneProby) komunikatLabel.setText("Koniec gry! Liczba to: " + slowoDoZgadniecia);
            else komunikatLabel.setText("Poprawnych cyfr na miejscu: " + trafienia + ". Pozostało prób: " + (maksymalneProby - proby));
        } catch (Exception ex) {
            graTrwa = false;
            komunikatLabel.setText("Wystąpił błąd podczas sprawdzania liczby");
        }
    }


    private void wczytajKategorie() {
        kategorieMap.put("Zwierzęta", wczytajSlowaZPliku("zwierzeta.txt"));
        kategorieMap.put("Rośliny", wczytajSlowaZPliku("rosliny.txt"));
        kategorieMap.put("Państwa", wczytajSlowaZPliku("panstwa.txt"));
        kategorieMap.put("Miasta", wczytajSlowaZPliku("miasta.txt"));
        kategorieMap.put("Imiona", wczytajSlowaZPliku("imiona.txt"));
        kategorieMap.put("Losowe", wczytajSlowaZPliku("slowa.txt"));
        kategorieMap.put("Liczby", Collections.singletonList("0000"));
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
    private void sprawdzOdpowiedz(String guess) {
        try {
            if (!graTrwa) return;
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
                    graTrwa = false;
                } else if (proby >= maksymalneProby) {
                    komunikatLabel.setText("Koniec gry! Liczba to: " + slowoDoZgadniecia);
                    graTrwa = false;
                } else {
                    komunikatLabel.setText("Trafionych cyfr na dobrych miejscach: " + correctDigits + ". Pozostało prób: " + (maksymalneProby - proby));
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
                    aktualizujKlawiature(ch, "limegreen",klawiaturaBox);
                } else if (slowoDoZgadniecia.contains(String.valueOf(ch))) {
                    slot.setStyle(slot.getStyle().replace("black", "gold"));
                    aktualizujKlawiature(ch, "gold",klawiaturaBox);
                } else {
                    slot.setStyle(slot.getStyle().replace("black", "salmon"));
                    aktualizujKlawiature(ch, "salmon",klawiaturaBox);
                }
            }
            proby++;
            if (guess.equals(slowoDoZgadniecia)) {
                komunikatLabel.setText("Brawo! Odgadłeś słowo.");
                graTrwa = false;
            } else if (proby >= maksymalneProby) {
                komunikatLabel.setText("Koniec gry! Hasło to: " + slowoDoZgadniecia.toUpperCase());
                graTrwa = false;
            } else {
                komunikatLabel.setText("Spróbuj dalej (" + (maksymalneProby - proby) + " prób pozostało)");
            }
        } catch (Exception ex) {
            graTrwa = false;
            komunikatLabel.setText("Wystąpił błąd podczas sprawdzania odpowiedzi");
        }
    }
}

package com.example.projektcokolwiek;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;


public class KeyboardHelper {
    public static void utworzKlawiature(Font font, VBox klawiaturaBox) {
        klawiaturaBox.getChildren().clear();
        String[] rows = {"QWERTYUIOP","ASDFGHJKL","ZXCVBNM","ĄĆĘŁŃÓŚŹŻ"};
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
}

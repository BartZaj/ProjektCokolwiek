import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> slowa = wczytajSlowaZPliku("slowa.txt");
        if (slowa.isEmpty()) {
            System.out.println("Brak słów w pliku!");
            return;
        }

        String slowoDoZgadniecia = slowa.get(new Random().nextInt(slowa.size())).toLowerCase();
        char[] aktualnyStan = new char[slowoDoZgadniecia.length()];
        Arrays.fill(aktualnyStan, '_');

        Set<Character> zgadnieteLitery = new HashSet<>();
        Scanner scanner = new Scanner(System.in);

        int proby = 0;
        while (!String.valueOf(aktualnyStan).equals(slowoDoZgadniecia)) {
            System.out.println("Aktualny stan: " + String.valueOf(aktualnyStan));
            System.out.print("Podaj literę: ");
            String input = scanner.nextLine().toLowerCase();

            if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
                System.out.println("Podaj jedną literę!");
                continue;
            }

            char litera = input.charAt(0);

            if (zgadnieteLitery.contains(litera)) {
                System.out.println("Już próbowałeś tej litery.");
                continue;
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
                System.out.println("Dobrze!");
            } else {
                System.out.println("Niestety, tej litery nie ma.");
            }
        }

        System.out.println("Gratulacje! Odgadłeś słowo \"" + slowoDoZgadniecia + "\" w " + proby + " próbach.");
    }

    private static List<String> wczytajSlowaZPliku(String nazwaPliku) {
        try {
            return Files.readAllLines(Paths.get(nazwaPliku));
        } catch (IOException e) {
            System.out.println("Błąd podczas wczytywania pliku: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
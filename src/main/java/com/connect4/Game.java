package com.connect4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * A Game osztály kezeli a Connect4 játék logikáját.
 */
public class Game {
    /** A játék tábla. */
    private final Board board;

    /** Az első játékos. */
    private final Player player1;

    /** A második játékos. */
    private final Player player2;

    /** A játékosok közötti módszer (true: ember vs ember, ember vs gép). */
    private final boolean isHumanVsHuman;

    /** A gép játékosa. */
    private final AIPlayer aiPlayer;

    /**
     * Konstruktor a Game osztályhoz.
     *
     * @param rows    A sorok száma a táblán.
     * @param columns Az oszlopok száma a táblán.
     * @param name1   Az első játékos neve.
     * @param name2   A második játékos neve.
     * @param humanVsHuman A játék módja (igaz: ember vs ember, ember vs gép).
     */
    public Game(final int rows, final int columns, final String name1,
                final String name2, final boolean humanVsHuman) {
        this.board = new Board(rows, columns);
        this.player1 = new Player(name1, 'X');
        this.player2 = new Player(name2, 'O');
        this.isHumanVsHuman = humanVsHuman;

        // AIPlayer példányosítása a helyes paraméterezéssel
        this.aiPlayer = new AIPlayer(name2, 'O');
    }
    /**
     * Visszaadja a játék tábláját.
     *
     * <p>Ez a metódus a {@link Board} objektumot adja vissza, amely lehetővé
     * teszi a táblához való hozzáférést. Az öröklődő osztályok számára
     * a {@code Board} objektumhoz való hozzáférés biztosítása érdekében
     * gondoskodjon arról, hogy a {@code Board} objektumot ne módosítsák
     * közvetlenül, mert ez a játék logikáját befolyásolhatja.
     * Teszteknél használt.</p>
     *
     * @return A játék tábla.
     */
    public Board getBoard() {
        return this.board;
    }

    /**
     * Betölti a játék állását egy fájlból.
     *
     * @param filename A fájl neve.
     */
    public void loadGameFromFile(final String filename) {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filename))) {
            String line;
            for (int row = 0; row < board.getRows(); row++) {
                line = reader.readLine();
                for (int col = 0; col < board.getColumns(); col++) {
                    board.getGrid()[row][col] = line.charAt(col);
                }
            }
        } catch (IOException e) {
            System.out.println("Hiba a fájl beolvasása során: "
                    +
                    e.getMessage());
        }
    }

    /**
     * Elmenti a játék állását egy fájlba.
     *
     * @param filename A fájl neve.
     */
    public void saveGameToFile(final String filename) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filename))) {
            for (int row = 0; row < board.getRows(); row++) {
                for (int col = 0; col < board.getColumns(); col++) {
                    writer.write(board.getGrid()[row][col] == '\0'
                            ? '.'
                            : board.getGrid()[row][col]);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Hiba a fájl mentése során: "
                    +
                    e.getMessage());
        }
    }

    /**
     * Elindítja a játékot.
     */
    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("A játék kezdődik!");

        // Játékosok választása
        while (true) {
            board.displayBoard();
            System.out.println(player1.getName() + " lépése (oszlop: 0-"
                    + (board.getColumns() - 1) + "):");


            // Ellenőrizni kell, hogy van-e következő bemenet
            if (scanner.hasNextInt()) {
                int column = scanner.nextInt();

                if (board.isColumnValid(column)) {
                    board.placeToken(column, player1.getToken());
                    if (board.checkWin()) {
                        board.displayBoard();
                        System.out.println(player1.getName() + " nyert!");
                        break;
                    }

                    if (isHumanVsHuman) {
                        board.displayBoard();
                        System.out.println(player2.getName()
                                +
                                " lépése (oszlop: 0-"
                                + (board.getColumns() - 1) + "):");

                        if (scanner.hasNextInt()) {
                            column = scanner.nextInt();
                            if (board.isColumnValid(column)) {
                                board.placeToken(column, player2.getToken());
                                if (board.checkWin()) {
                                    board.displayBoard();
                                    System.out.println(player2.getName()
                                            + " nyert!");
                                    break;
                                }
                            }
                        } else {
                            System.out.println("Nincs több bemenet a játékos "
                                    + "lépéséhez.");
                            break;
                        }

                    } else {
                        // Gép lépése
                        int aiColumn = aiPlayer.makeMove(board);
                        board.placeToken(aiColumn, aiPlayer.getToken());
                        if (board.checkWin()) {
                            board.displayBoard();
                            System.out.println(aiPlayer.getName()
                                    + " (a gép) nyert!");
                            break;
                        }
                    }

                } else {
                    System.out.println("Érvénytelen lépés! Próbáld újra.");
                }

                if (board.isFull()) {
                    board.displayBoard();
                    System.out.println("A játék döntetlennel zárult!");
                    break;
                }
            } else {
                System.out.println("Nincs több bemenet a játékos lépéséhez.");
                break;
            }
        }
    }


    /**
     * A program belépési pontja, amely elindítja a játékot.
     *
     * @param args Parancssori argumentumok.
     */
    public static void main(final String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Adja meg az első játékos nevét:");
        String name1 = scanner.nextLine();
        System.out.println("Adja meg a második játékos nevét (ha gép, "
                +
                "írjon 'Gép'): ");
        String name2 = scanner.nextLine();

        boolean isHumanVsHuman = !name2.equalsIgnoreCase("Gép");

        Game game = new Game(Constants.DEFAULT_ROWS,
                Constants.DEFAULT_COLUMNS, name1, name2,
                isHumanVsHuman);

        // Fájl betöltése
        System.out.println("Kérjük, adja meg a fájl nevét (ha nem akar "
                +
                "fájlt betölteni, csak nyomjon Entert):");
        String filename = scanner.nextLine();
        if (!filename.isEmpty()) {
            game.loadGameFromFile(filename);
        }

        game.start(); // Játék indítása

        // Fájl mentése
        System.out.println("Mentse a játék állását (írd be a fájl nevét, "
                +
                "ha nem akar menteni, csak nyomjon Entert):");
        filename = scanner.nextLine();
        if (!filename.isEmpty()) {
            game.saveGameToFile(filename);
        }
    }
}

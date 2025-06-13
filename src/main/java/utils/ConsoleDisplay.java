package utils;

import bo.CaseType;
import config.GameConfig;

public class ConsoleDisplay {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_BG_BLUE = "\u001B[44m";
    private static final String ANSI_BG_GREEN = "\u001B[42m";
    private static final String ANSI_BG_RED = "\u001B[41m";

    private static final int MENU_WIDTH = 40;

    // Simplification des symboles pour n'utiliser que des lettres
    public static String getPieceSymbol(String pieceName) {
        if (pieceName == null) return ".";
        return switch (pieceName.toUpperCase()) {
            case "ELEPHANT", "ÉLÉPHANT" -> "E";
            case "LION" -> "L";
            case "TIGRE", "TIGER" -> "T";
            case "PANTHERE", "PANTHÈRE", "PANTHER" -> "P";
            case "CHIEN", "DOG" -> "D";
            case "LOUP", "WOLF" -> "W";
            case "CHAT", "CAT" -> "C";
            case "RAT" -> "R";
            default -> "?";
        };
    }

    public static String getSpecialCaseSymbol(CaseType type) {
        return switch (type) {
            case RIVER -> "~";    // Rivière
            case TRAP -> "X";     // Piège
            case SANCTUARY -> "#"; // Sanctuaire
            default -> ".";       // Case vide
        };
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printHeader(String text) {
        String line = "=".repeat(MENU_WIDTH);
        System.out.println(line);
        System.out.println(centerText(text, MENU_WIDTH));
        System.out.println(line);
    }

    public static void displayCell(String content, CaseType caseType, boolean isPlayer1Piece) {
        String display = " " + content + " ";
        if (GameConfig.getInstance().isColorEnabled()) {
            String color = isPlayer1Piece ? ANSI_RED : ANSI_BLUE;
            String bgColor = switch (caseType) {
                case RIVER -> ANSI_BG_BLUE;
                case TRAP -> ANSI_BG_GREEN;
                case SANCTUARY -> ANSI_BG_RED;
                default -> "";
            };
            System.out.print("|" + bgColor + color + display + ANSI_RESET);
        } else {
            // En mode non-couleur, ajouter un marqueur pour différencier les joueurs
            String markedContent = isPlayer1Piece ? "[" + content + "]" : "(" + content + ")";
            System.out.print("|" + markedContent);
        }
    }

    public static void displayBoardHeader() {
        System.out.print("     ");
        for (int col = 0; col < 7; col++) {
            System.out.print(col + "   ");
        }
        System.out.println();
        System.out.print("   +");
        for (int i = 0; i < 7; i++) {
            System.out.print("---+");
        }
        System.out.println();
    }

    public static void displayBoardFooter() {
        System.out.print("   +");
        for (int i = 0; i < 7; i++) {
            System.out.print("---+");
        }
        System.out.println();
    }

    public static void displayHorizontalLine() {
        System.out.print("   +");
        for (int i = 0; i < 7; i++) {
            System.out.print("---+");
        }
        System.out.println();
    }

    // Méthodes pour les messages
    public static void printError(String text) {
        if (GameConfig.getInstance().isColorEnabled()) {
            System.err.println(ANSI_RED + "Erreur: " + text + ANSI_RESET);
        } else {
            System.err.println("Erreur: " + text);
        }
    }

    public static void printSuccess(String text) {
        if (GameConfig.getInstance().isColorEnabled()) {
            System.out.println(ANSI_GREEN + text + ANSI_RESET);
        } else {
            System.out.println(text);
        }
    }

    public static void printWarning(String text) {
        System.out.println("! " + text);
    }

    public static void printInfo(String text) {
        System.out.println("> " + text);
    }

    private static String centerText(String text, int width) {
        if (text == null) return " ".repeat(width);
        text = text.trim();
        int padding = Math.max(0, (width - text.length()) / 2);
        int rightPadding = Math.max(0, width - text.length() - padding);
        return " ".repeat(padding) + text + " ".repeat(rightPadding);
    }
}

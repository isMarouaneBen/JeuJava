package console;

import dal.DatabaseManager;
import dal.PlayerAccountManager;
import dal.PlayerStatisticsManager;
import utils.ConsoleDisplay;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final PlayerAccountManager accountManager = new PlayerAccountManager();
    private static final PlayerStatisticsManager statsManager = new PlayerStatisticsManager();    public static void main(String[] args) {
        while (true) {
            try {
                ConsoleDisplay.clearScreen();
                ConsoleDisplay.printHeader("MENU PRINCIPAL");
                System.out.println();
                System.out.println("  1. Démarrer une nouvelle partie");
                System.out.println("  2. Consulter les statistiques");
                System.out.println("  3. Voir l'historique des parties");
                System.out.println("  4. Classement Global (TOP 10)");
                System.out.println("  5. Réinitialiser la base de données (Admin)");
                System.out.println("  6. Quitter");
                System.out.println();
                System.out.print("Votre choix : ");
                
                String choice = scanner.nextLine();
                
                switch (choice) {
                    case "1" -> {
                        GameUI gameUI = new GameUI();
                        gameUI.start();
                    }
                    case "2" -> showPlayerStats();
                    case "3" -> showGameHistory();
                    case "4" -> showGlobalLeaderboard();
                    case "5" -> handleDatabaseReset();
                    case "6" -> {
                        ConsoleDisplay.printInfo("Au revoir !");
                        return;
                    }
                    default -> {
                        ConsoleDisplay.printWarning("Option invalide");
                        System.out.println("Appuyez sur Entrée pour continuer...");
                        scanner.nextLine();
                    }
                }
            } catch (Exception e) {
                ConsoleDisplay.printError(e.getMessage());
                System.out.println("Appuyez sur Entrée pour continuer...");
                scanner.nextLine();
            }
        }
    }

    private static void showPlayerStats() throws SQLException {
        System.out.print("Entrez le nom d'utilisateur : ");
        String username = scanner.nextLine();

        Map<String, Object> stats = statsManager.getPlayerStatistics(username);
        if (stats == null) {
            ConsoleDisplay.printError("Joueur non trouvé !");
            return;
        }

        ConsoleDisplay.printHeader("Statistiques de " + username);
        System.out.println("Score total : " + stats.get("score"));
        System.out.println("Parties jouées : " + stats.get("gamesPlayed"));
        System.out.println("Victoires : " + stats.get("gamesWon"));
        System.out.println("Défaites : " + stats.get("gamesLost"));
        System.out.println("Égalités : " + stats.get("gamesDrawn"));
        
        if ((int)stats.get("gamesPlayed") > 0) {
            double winRate = ((int)stats.get("gamesWon") * 100.0) / (int)stats.get("gamesPlayed");
            System.out.printf("Taux de victoire : %.1f%%\n", winRate);
        }

        System.out.println("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }

    private static void showGameHistory() throws SQLException {
        System.out.print("Entrez le nom d'utilisateur : ");
        String username = scanner.nextLine();

        List<Map<String, Object>> history = statsManager.getPlayerGameHistory(username);
        if (history.isEmpty()) {
            ConsoleDisplay.printError("Aucune partie trouvée pour ce joueur !");
            return;
        }

        ConsoleDisplay.printHeader("Historique des parties de " + username);
        for (Map<String, Object> game : history) {
            System.out.println("\nDate : " + game.get("date"));
            System.out.println("Joueurs : " + game.get("player1") + " vs " + game.get("player2"));
            System.out.println("Vainqueur : " + (game.get("winner") != null ? game.get("winner") : "Égalité"));
            System.out.println("Durée : " + formatDuration((int)game.get("duration")));
            System.out.println("Nombre de coups : " + game.get("movesCount"));
            System.out.println("------------------------");
        }

        System.out.println("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }

    private static void handleDatabaseReset() throws SQLException, IOException {
        System.out.print("Nom d'utilisateur administrateur : ");
        String username = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String password = scanner.nextLine();

        if (!accountManager.isAdmin(username, password)) {
            ConsoleDisplay.printError("Accès refusé ! Seul l'administrateur peut réinitialiser la base de données.");
            System.out.println("Appuyez sur Entrée pour continuer...");
            scanner.nextLine();
            return;
        }

        System.out.println("Êtes-vous sûr de vouloir réinitialiser la base de données ? (o/n)");
        if (scanner.nextLine().toLowerCase().startsWith("o")) {
            DatabaseManager.resetDatabase();
            ConsoleDisplay.printSuccess("Base de données réinitialisée avec succès !");
        }
        
        System.out.println("Appuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }

    private static void showGlobalLeaderboard() throws SQLException {
        PlayerStatisticsManager statsManager = new PlayerStatisticsManager();
        List<Map<String, Object>> leaderboard = statsManager.getGlobalLeaderboard(10);

        if (leaderboard.isEmpty()) {
            ConsoleDisplay.printWarning("Aucun joueur classé pour le moment !");
            return;
        }

        ConsoleDisplay.printHeader("CLASSEMENT GLOBAL - TOP 10");
        
        // Afficher l'en-tête du tableau
        System.out.println("╔════╦════════════════╦══════════╦═══════════╦══════════╗");
        System.out.println("║ #  ║     Joueur     ║   ELO    ║ V/D/N     ║  Win %   ║");
        System.out.println("╠════╬════════════════╬══════════╬═══════════╬══════════╣");

        // Afficher chaque joueur
        for (Map<String, Object> player : leaderboard) {
            int rank = (int) player.get("rank");
            String username = (String) player.get("username");
            int rating = (int) player.get("rating");
            int wins = (int) player.get("gamesWon");
            int losses = (int) player.get("gamesLost");
            int draws = (int) player.get("gamesDrawn");
            String winRate = (String) player.get("winRate");

            System.out.printf("║ %-2d ║ %-14s ║ %-8d ║ %3d/%-3d/%-2d ║ %-8s ║%n",
                rank,
                username.length() > 14 ? username.substring(0, 14) : username,
                rating,
                wins, losses, draws,
                winRate);
        }

        System.out.println("╚════╩════════════════╩══════════╩═══════════╩══════════╝");
        
        // Afficher les légendes
        System.out.println("\nLégende :");
        System.out.println("ELO     : Score de classement");
        System.out.println("V/D/N   : Victoires/Défaites/Nuls");
        System.out.println("Win %   : Pourcentage de victoires");
        
        System.out.println("\nAppuyez sur Entrée pour continuer...");
        scanner.nextLine();
    }

    private static String formatDuration(int seconds) {
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d min %d sec", minutes, seconds);
    }
}

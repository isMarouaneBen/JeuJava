package console;

import bll.GameManager;
import bo.Player;
import dal.PlayerAccountManager;
import dal.PlayerStatisticsManager;
import events.GameEvent;
import events.GameEventListener;
import utils.ConsoleDisplay;

import java.util.Map;
import java.util.Scanner;
import java.sql.SQLException;

public class GameUI implements GameEventListener {
    private GameManager gameManager;
    private final PlayerAccountManager accountManager;
    private final Scanner scanner;
    private boolean gameOver = false;

    private String player1Username;
    private String player2Username;
    private int player1Captures = 0;
    private int player2Captures = 0;

    public GameUI() {
        this.gameManager = new GameManager();
        this.accountManager = new PlayerAccountManager();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        this.gameManager.addEventListener(this);
        try {
            ConsoleDisplay.clearScreen();
            ConsoleDisplay.printHeader("Bienvenue dans le Jeu de la Jungle (Xou Dou Qi)");
            
            if (handlePlayerLogin()) {
                gameLoop();
            }
        } catch (Exception e) {
            ConsoleDisplay.printError("Une erreur est survenue : " + e.getMessage());
        }
    }

    private void handleGameOver(GameEvent event) {
        String winnerName = event.isWinner() ? player1Username : player2Username;
        
        // Afficher le résultat de la partie
        ConsoleDisplay.clearScreen();
        ConsoleDisplay.printHeader("Fin de la partie !");
        System.out.printf("Victoire de %s !%n%n", winnerName);
        
        // Afficher les statistiques de la partie
        System.out.println("Statistiques de la partie :");
        System.out.printf("Durée : %.2f minutes%n", event.getGameDuration() / 60000.0);
        System.out.printf("Nombre de coups : %d%n", event.getMoveCount());
        System.out.printf("Captures %s : %d%n", player1Username, player1Captures);
        System.out.printf("Captures %s : %d%n", player2Username, player2Captures);
        System.out.println();

        // Enregistrer les résultats
        try {
            PlayerStatisticsManager statsManager = new PlayerStatisticsManager();
            statsManager.recordGameResult(
                gameManager.getPlayer1Id(),
                gameManager.getPlayer2Id(),
                event.isWinner() ? gameManager.getPlayer1Id() : gameManager.getPlayer2Id(),
                (int)(event.getGameDuration() / 1000), // convertir en secondes
                event.getMoveCount(),
                player1Captures,
                player2Captures
            );

            // Afficher les nouvelles statistiques des joueurs
            displayPlayerStats(player1Username);
            System.out.println();
            displayPlayerStats(player2Username);
            
        } catch (SQLException e) {
            ConsoleDisplay.printError("Erreur lors de l'enregistrement des statistiques : " + e.getMessage());
        }

        // Proposer une revanche
        System.out.println("\nVoulez-vous faire une revanche ? (o/n)");
        String response = scanner.nextLine().toLowerCase();
        if (response.startsWith("o")) {
            // Réinitialiser pour une nouvelle partie
            player1Captures = 0;
            player2Captures = 0;
            gameOver = false;
            gameManager = new GameManager();
            gameManager.addEventListener(this);
            gameLoop();
        } else {
            gameOver = true;
        }
    }

    private void displayPlayerStats(String username) throws SQLException {
        PlayerStatisticsManager statsManager = new PlayerStatisticsManager();
        Map<String, Object> stats = statsManager.getPlayerStatistics(username);
        if (stats != null) {
            System.out.printf("%nStatistiques de %s :%n", username);
            System.out.printf("Classement ELO : %d%n", stats.get("rating"));
            System.out.printf("Parties jouées : %d%n", stats.get("gamesPlayed"));
            System.out.printf("Victoires : %d%n", stats.get("gamesWon"));
            System.out.printf("Défaites : %d%n", stats.get("gamesLost"));
            System.out.printf("Égalités : %d%n", stats.get("gamesDrawn"));
            System.out.printf("Série de victoires : %d (Record : %d)%n", 
                stats.get("winStreak"), stats.get("bestWinStreak"));
            if (stats.containsKey("winRate")) {
                System.out.printf("Taux de victoire : %s%n", stats.get("winRate"));
            }
        }
    }

    private boolean handlePlayerLogin() {
        try {
            // Joueur 1
            ConsoleDisplay.printHeader("Connexion du Joueur 1");
            System.out.print("Nom d'utilisateur : ");
            player1Username = scanner.nextLine();
            System.out.print("Mot de passe : ");
            String password1 = scanner.nextLine();

            Map<String, Object> player1Info = accountManager.login(player1Username, password1);
            if (player1Info == null) {
                System.out.println("Compte non trouvé. Création d'un nouveau compte...");
                player1Info = accountManager.createAccount(player1Username, password1);
                if (player1Info != null) {
                    ConsoleDisplay.printSuccess("Compte créé avec succès !");
                } else {
                    ConsoleDisplay.printError("Impossible de créer le compte.");
                    return false;
                }
            }

            // Joueur 2
            ConsoleDisplay.printHeader("Connexion du Joueur 2");
            System.out.print("Nom d'utilisateur : ");
            player2Username = scanner.nextLine();
            System.out.print("Mot de passe : ");
            String password2 = scanner.nextLine();

            Map<String, Object> player2Info = accountManager.login(player2Username, password2);
            if (player2Info == null) {
                System.out.println("Compte non trouvé. Création d'un nouveau compte...");
                player2Info = accountManager.createAccount(player2Username, password2);
                if (player2Info != null) {
                    ConsoleDisplay.printSuccess("Compte créé avec succès !");
                } else {
                    ConsoleDisplay.printError("Impossible de créer le compte.");
                    return false;
                }
            }

            // Créer les objets Player et les assigner au GameManager
            Player player1 = new Player((int)player1Info.get("id"), player1Username, (int)player1Info.get("rating"));
            Player player2 = new Player((int)player2Info.get("id"), player2Username, (int)player2Info.get("rating"));
            gameManager.setPlayers(player1, player2);

            return true;
        } catch (Exception e) {
            ConsoleDisplay.printError("Erreur lors de la connexion : " + e.getMessage());
            return false;
        }
    }

    private void gameLoop() {
        gameOver = false;
        while (!gameOver) {
            if (gameManager.isGameOver()) {
                break;
            }

            gameManager.displayBoard();
            
            System.out.println("\nCommandes disponibles :");
            System.out.println("1. Déplacer une pièce (format: 1,1 2,2)");
            System.out.println("2. Voir les règles");
            System.out.println("3. Abandonner la partie");
            System.out.print("\nVotre choix : ");

            String input = scanner.nextLine();
            
            if (input.equals("2")) {
                showRules();
                continue;
            }
            
            if (input.equals("3")) {
                if (confirmAction("Êtes-vous sûr de vouloir abandonner ? (o/n) ")) {
                    ConsoleDisplay.printHeader("Partie abandonnée !");
                    System.out.println("Appuyez sur Entrée pour retourner au menu principal...");
                    scanner.nextLine();
                    gameOver = true;
                }
                continue;
            }

            handleMove(input);
        }
    }

    private void handleMove(String input) {
        try {
            String[] positions = input.split(" ");
            if (positions.length != 2) {
                ConsoleDisplay.printError("Format invalide. Utilisez : ligne,colonne ligne,colonne");
                return;
            }

            String[] from = positions[0].split(",");
            String[] to = positions[1].split(",");

            int fromRow = Integer.parseInt(from[0]);
            int fromCol = Integer.parseInt(from[1]);
            int toRow = Integer.parseInt(to[0]);
            int toCol = Integer.parseInt(to[1]);

            if (!gameManager.makeMove(fromRow, fromCol, toRow, toCol)) {
                ConsoleDisplay.printError("Mouvement invalide !");
            }
        } catch (Exception e) {
            ConsoleDisplay.printError("Format invalide. Utilisez : ligne,colonne ligne,colonne");
        }
    }

    private void showRules() {
        ConsoleDisplay.clearScreen();
        ConsoleDisplay.printHeader("Règles du Jeu de la Jungle");
        System.out.println("""
            1. Le but du jeu est d'entrer dans le sanctuaire adverse
            2. Les pièces se déplacent d'une case horizontalement ou verticalement
            3. Hiérarchie des pièces (du plus fort au plus faible) :
               ÉLÉPHANT > LION > TIGRE > PANTHÈRE > CHIEN > LOUP > CHAT > RAT
            4. Exceptions :
               - Le RAT peut capturer l'ÉLÉPHANT
               - Le LION et le TIGRE peuvent sauter par-dessus la rivière
               - Le RAT est le seul à pouvoir nager dans la rivière
            5. Une pièce dans un piège perd toute sa force
            
            Appuyez sur Entrée pour continuer...""");
        scanner.nextLine();
    }

    private boolean confirmAction(String message) {
        System.out.print(message);
        return scanner.nextLine().toLowerCase().startsWith("o");
    }    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.getType()) {
            case MOVE_MADE -> {
                String playerName = event.getPiece().isPlayer1() ? player1Username : player2Username;
                System.out.printf("%s déplace %s de (%d,%d) à (%d,%d)%n",
                    playerName,
                    event.getPiece().getName(),
                    event.getFromRow(),
                    event.getFromCol(),
                    event.getToRow(),
                    event.getToCol()
                );
            }
            case PIECE_CAPTURED -> {
                String capturingPlayerName = event.getPiece().isPlayer1() ? player1Username : player2Username;
                String capturedPlayerName = event.getCapturedPiece().isPlayer1() ? player1Username : player2Username;
                
                // Mettre à jour le compteur de captures
                if (event.getPiece().isPlayer1()) {
                    player1Captures++;
                } else {
                    player2Captures++;
                }
                
                System.out.printf("%s (%s) capture %s (%s)%n",
                    capturingPlayerName,
                    event.getPiece().getName(),
                    capturedPlayerName,
                    event.getCapturedPiece().getName()
                );
            }
            case GAME_OVER -> handleGameOver(event);
            case TURN_CHANGED -> {
                System.out.printf("C'est au tour de %s%n", 
                    event.isPlayer1Turn() ? player1Username : player2Username);
            }
        }
    }
}

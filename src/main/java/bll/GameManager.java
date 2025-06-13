package bll;

import bo.CaseType;
import bo.GamePiece;
import bo.Player;
import config.GameConfig;
import events.GameEvent;
import events.GameEventListener;
import utils.ConsoleDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameManager {
    private static final int BOARD_WIDTH = 7;
    private static final int BOARD_HEIGHT = 9;
    
    private final GamePiece[][] board;
    private final List<GameEventListener> listeners;
    private boolean isPlayer1Turn;
    private long gameStartTime;
    private int moveCount;
    private boolean gameOver;
    private Player player1;
    private Player player2;

    public GameManager() {
        this.board = new GamePiece[BOARD_HEIGHT][BOARD_WIDTH];
        this.listeners = new CopyOnWriteArrayList<>();
        this.isPlayer1Turn = true;
        this.gameStartTime = System.currentTimeMillis();
        this.moveCount = 0;
        this.gameOver = false;
        initializeGame();
    }

    public void setPlayers(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public int getPlayer1Id() {
        return player1 != null ? player1.getId() : -1;
    }

    public int getPlayer2Id() {
        return player2 != null ? player2.getId() : -1;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private void checkGameOver(int toRow, GamePiece piece) {
        CaseType caseType = CaseType.getType(toRow, 3);
        if (caseType == CaseType.SANCTUARY) {
            if ((piece.isPlayer1() && toRow == 0) || (!piece.isPlayer1() && toRow == BOARD_HEIGHT - 1)) {
                gameOver = true;
                notifyGameOver(piece.isPlayer1());
            }
        }
    }

    private void notifyGameOver(boolean isPlayer1Winner) {
        GameEvent event = new GameEvent(GameEvent.Type.GAME_OVER);
        event.setSource(this);
        event.setWinner(isPlayer1Winner);
        event.setMoveCount(moveCount);
        event.setGameDuration(System.currentTimeMillis() - gameStartTime);
        notifyListeners(event);
        gameOver = true;
    }

    private void notifyListeners(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.onGameEvent(event);
        }
    }
    
    private void initializeGame() {
        // Placement des pièces pour le joueur 2 (haut)
        placePiece(0, 0, "Lion", false);
        placePiece(0, 6, "Tigre", false);
        placePiece(1, 1, "Chien", false);
        placePiece(1, 5, "Chat", false);
        placePiece(2, 0, "Rat", false);
        placePiece(2, 2, "Panthère", false);
        placePiece(2, 4, "Loup", false);
        placePiece(2, 6, "Éléphant", false);

        // Placement des pièces pour le joueur 1 (bas)
        placePiece(8, 6, "Lion", true);
        placePiece(8, 0, "Tigre", true);
        placePiece(7, 5, "Chien", true);
        placePiece(7, 1, "Chat", true);
        placePiece(6, 6, "Rat", true);
        placePiece(6, 4, "Panthère", true);
        placePiece(6, 2, "Loup", true);
        placePiece(6, 0, "Éléphant", true);
    }

    private void placePiece(int row, int col, String name, boolean isPlayer1) {
        board[row][col] = new GamePiece(name, getPieceRank(name), isPlayer1);
    }

    private int getPieceRank(String name) {
        return switch (name.toUpperCase()) {
            case "ÉLÉPHANT", "ELEPHANT" -> 8;
            case "LION" -> 7;
            case "TIGRE" -> 6;
            case "PANTHÈRE", "PANTHERE" -> 5;
            case "CHIEN" -> 4;
            case "LOUP" -> 3;
            case "CHAT" -> 2;
            case "RAT" -> 1;
            default -> 0;
        };
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_HEIGHT && col >= 0 && col < BOARD_WIDTH;
    }

    private boolean isInOwnSanctuary(GamePiece piece, int row) {
        return CaseType.getType(row, 3) == CaseType.SANCTUARY && 
               ((piece.isPlayer1() && row == BOARD_HEIGHT - 1) || (!piece.isPlayer1() && row == 0));
    }

    private boolean isInEnemyTrap(GamePiece piece, int row) {
        return CaseType.getType(row, 2) == CaseType.TRAP && 
               ((piece.isPlayer1() && row == 0) || (!piece.isPlayer1() && row == BOARD_HEIGHT - 1));
    }

    private boolean canLeaveRiver(GamePiece piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (!piece.getName().equalsIgnoreCase("RAT")) {
            return false;
        }

        CaseType targetCase = CaseType.getType(toRow, toCol);
        if (targetCase != CaseType.RIVER) {
            GamePiece targetPiece = board[toRow][toCol];
            if (targetPiece != null) {
                return targetPiece.getName().equalsIgnoreCase("RAT");
            }
        }
        return true;
    }    public void displayBoard() {
        ConsoleDisplay.clearScreen();
        ConsoleDisplay.printHeader("PLATEAU DE JEU");
        ConsoleDisplay.displayBoardHeader();

        for (int row = 0; row < BOARD_HEIGHT; row++) {
            System.out.printf("%2d ", row);
            
            for (int col = 0; col < BOARD_WIDTH; col++) {
                GamePiece piece = board[row][col];
                CaseType caseType = CaseType.getType(row, col);
                
                if (piece == null) {
                    ConsoleDisplay.displayCell(ConsoleDisplay.getSpecialCaseSymbol(caseType), caseType, false);
                } else {
                    ConsoleDisplay.displayCell(piece.getSymbol(), caseType, piece.isPlayer1());
                }
            }
            System.out.println("|");
            
            if (row < BOARD_HEIGHT - 1) {
                ConsoleDisplay.displayHorizontalLine();
            }
        }

        ConsoleDisplay.displayBoardFooter();
        
        System.out.println();
        if (GameConfig.getInstance().isColorEnabled()) {
            System.out.println("Tour du " + 
                (isPlayer1Turn ? 
                    ConsoleDisplay.ANSI_RED + "Joueur 1" + ConsoleDisplay.ANSI_RESET : 
                    ConsoleDisplay.ANSI_BLUE + "Joueur 2" + ConsoleDisplay.ANSI_RESET));
        } else {
            System.out.println("Tour du " + (isPlayer1Turn ? "Joueur 1" : "Joueur 2"));
        }
        System.out.println();
        displayValidMoves();
    }

    private List<String> getValidMovesForPiece(int row, int col) {
        List<String> validMoves = new ArrayList<>();
        GamePiece piece = board[row][col];
        
        if (piece == null || piece.isPlayer1() != isPlayer1Turn) {
            return validMoves;
        }

        // Vérifier les 4 directions
        checkMove(row-1, col, piece, row, col, validMoves); // haut
        checkMove(row+1, col, piece, row, col, validMoves); // bas
        checkMove(row, col-1, piece, row, col, validMoves); // gauche
        checkMove(row, col+1, piece, row, col, validMoves); // droite

        // Vérifier les sauts pour Lion et Tigre
        if (piece.getName().equals("Lion") || piece.getName().equals("Tigre")) {
            checkJumpMove(row, col, piece, validMoves);
        }

        return validMoves;
    }

    private void checkMove(int newRow, int newCol, GamePiece piece, int fromRow, int fromCol, List<String> validMoves) {
        if (isValidPosition(newRow, newCol)) {
            if (isValidDestination(piece, fromRow, fromCol, newRow, newCol)) {
                validMoves.add(String.format("%d,%d", newRow, newCol));
            }
        }
    }

    private void checkJumpMove(int row, int col, GamePiece piece, List<String> validMoves) {
        // Logique pour les sauts au-dessus de la rivière
        if (piece.getName().equals("Lion") || piece.getName().equals("Tigre")) {
            // Saut vertical
            if (col == 1 || col == 2 || col == 4 || col == 5) {
                if (row == 2) checkMove(row+4, col, piece, row, col, validMoves);
                if (row == 6) checkMove(row-4, col, piece, row, col, validMoves);
            }
            // Saut horizontal
            if (row >= 3 && row <= 5) {
                if (col == 0) checkMove(row, col+3, piece, row, col, validMoves);
                if (col == 3) {
                    checkMove(row, col-3, piece, row, col, validMoves);
                    checkMove(row, col+3, piece, row, col, validMoves);
                }
                if (col == 6) checkMove(row, col-3, piece, row, col, validMoves);
            }
        }
    }

    public void displayValidMoves() {
        StringBuilder moves = new StringBuilder("Coups possibles :\n");
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                GamePiece piece = board[row][col];
                if (piece != null && piece.isPlayer1() == isPlayer1Turn) {
                    List<String> validMoves = getValidMovesForPiece(row, col);
                    if (!validMoves.isEmpty()) {
                        moves.append(String.format("%s en (%d,%d) → %s\n", 
                            piece.getName(), row, col, String.join(" ou ", validMoves)));
                    }
                }
            }
        }
        System.out.println(moves.toString());
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Vérifier si les coordonnées sont dans les limites
        if (!isValidPosition(fromRow, fromCol) || !isValidPosition(toRow, toCol)) {
            return false;
        }

        GamePiece piece = board[fromRow][fromCol];
        
        // Vérifier si une pièce est présente à la position de départ
        if (piece == null) {
            return false;
        }

        // Vérifier si c'est le bon tour du joueur
        if (piece.isPlayer1() != isPlayer1Turn) {
            return false;
        }

        // Vérifier si le mouvement est diagonal (interdit)
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }

        // Vérifier si le mouvement est de plus d'une case (sauf pour le saut de rivière)
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        boolean isLongMove = rowDiff > 1 || colDiff > 1;

        // Cas spécial : saut de rivière pour Lion et Tigre
        if (isLongMove) {
            if (!piece.canJumpRiver()) {
                return false;
            }
            return isValidRiverJump(fromRow, fromCol, toRow, toCol);
        }

        // Vérifier si la destination est valide
        return isValidDestination(piece, fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidDestination(GamePiece piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (!isValidPosition(toRow, toCol)) {
            return false;
        }

        GamePiece targetPiece = board[toRow][toCol];
        CaseType fromCase = CaseType.getType(fromRow, fromCol);
        CaseType targetCase = CaseType.getType(toRow, toCol);

        // Vérifier si la case de destination est le propre sanctuaire du joueur
        if (isInOwnSanctuary(piece, toRow)) {
            return false;
        }

        // Vérifier si la pièce est dans un piège ennemi
        boolean isPieceInEnemyTrap = isInEnemyTrap(piece, fromRow);
        if (isPieceInEnemyTrap) {
            // Une pièce dans un piège ennemi ne peut pas capturer
            return targetPiece == null;
        }

        // Vérifier les règles de la rivière
        if (targetCase == CaseType.RIVER) {
            return piece.canMoveInRiver();
        }
        if (fromCase == CaseType.RIVER && !canLeaveRiver(piece, fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        // Si la case cible est occupée
        if (targetPiece != null) {
            // Ne peut pas capturer sa propre pièce
            if (targetPiece.isPlayer1() == piece.isPlayer1()) {
                return false;
            }
            
            // Vérifier si la pièce cible est dans un piège ennemi
            boolean isTargetInEnemyTrap = isInEnemyTrap(targetPiece, toRow);
            
            // Si la cible est dans un piège ennemi, elle peut être capturée
            if (isTargetInEnemyTrap) {
                return true;
            }
              // Règles spéciales pour le Rat
            if (piece.getName().equalsIgnoreCase("RAT")) {
                // Le Rat dans la rivière ne peut capturer que le Rat adverse
                if (fromCase == CaseType.RIVER) {
                    return targetPiece.getName().equalsIgnoreCase("RAT");
                }
                // Le Rat sur la terre peut capturer l'Éléphant
                if (targetPiece.getName().equalsIgnoreCase("ÉLÉPHANT")) {
                    return true;
                }
            }
            // L'Éléphant ne peut pas capturer le Rat
            if (piece.getName().equalsIgnoreCase("ÉLÉPHANT") && 
                targetPiece.getName().equalsIgnoreCase("RAT")) {
                return false;
            }
            
            // Règles standards de capture basées sur le rang
            return piece.canCapture(targetPiece, fromCase == CaseType.RIVER);
        }

        return true;
    }

    private boolean isValidRiverJump(int fromRow, int fromCol, int toRow, int toCol) {
        // Vérifier si le saut traverse la rivière
        if (!isRiverJumpPath(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        // Vérifier s'il y a un rat dans la rivière sur le chemin
        return !isRatInRiverPath(fromRow, fromCol, toRow, toCol);
    }

    private boolean isRiverJumpPath(int fromRow, int fromCol, int toRow, int toCol) {
        // Vérifier si le saut est vertical à travers la rivière
        if (fromCol == toCol && ((fromRow == 2 && toRow == 6) || (fromRow == 6 && toRow == 2))) {
            return true;
        }

        // Vérifier si le saut est horizontal à travers la rivière
        if (fromRow == toRow && fromRow >= 3 && fromRow <= 5) {
            return (fromCol == 0 && toCol == 3) || 
                   (fromCol == 3 && (toCol == 0 || toCol == 6)) ||
                   (fromCol == 6 && toCol == 3);
        }

        return false;
    }

    private boolean isRatInRiverPath(int fromRow, int fromCol, int toRow, int toCol) {
        // Vérifier le chemin vertical
        if (fromCol == toCol) {
            int startRow = Math.min(fromRow, toRow);
            int endRow = Math.max(fromRow, toRow);
            for (int row = startRow + 1; row < endRow; row++) {
                if (isRatInRiver(row, fromCol)) {
                    return true;
                }
            }
        }
        // Vérifier le chemin horizontal
        else if (fromRow == toRow) {
            int startCol = Math.min(fromCol, toCol);
            int endCol = Math.max(fromCol, toCol);
            for (int col = startCol + 1; col < endCol; col++) {
                if (isRatInRiver(fromRow, col)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRatInRiver(int row, int col) {
        GamePiece piece = board[row][col];
        return piece != null && 
               piece.getName().equalsIgnoreCase("RAT") && 
               CaseType.getType(row, col) == CaseType.RIVER;
    }    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isValidMove(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        GamePiece piece = board[fromRow][fromCol];
        GamePiece capturedPiece = board[toRow][toCol];
        
        // Exécuter le mouvement
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        moveCount++;

        // Mettre à jour l'état de la pièce
        CaseType newCase = CaseType.getType(toRow, toCol);
        piece.setInRiver(newCase == CaseType.RIVER);
        piece.setInTrap(isInEnemyTrap(piece, toRow));

        // Notifier les événements
        notifyMoveMade(fromRow, fromCol, toRow, toCol, piece);
        if (capturedPiece != null) {
            notifyPieceCaptured(piece, capturedPiece);
        }

        // Vérifier la victoire
        checkGameOver(toRow, piece);

        // Si le jeu n'est pas terminé, changer de tour
        if (!gameOver) {
            isPlayer1Turn = !isPlayer1Turn;
            notifyTurnChanged();
        }
        
        return true;
    }private boolean checkVictory(int row, int col) {
        CaseType caseType = CaseType.getType(row, col);
        if (caseType == CaseType.SANCTUARY) {
            GamePiece piece = board[row][col];
            boolean isVictory = (piece.isPlayer1() && row == 0) || (!piece.isPlayer1() && row == BOARD_HEIGHT - 1);
            if (isVictory) {
                gameOver = true;
                notifyGameOver(piece.isPlayer1());
            }
            return isVictory;
        }
        return false;
    }

    private void checkGameOver(int toRow) {
        // Vérifier si une pièce a atteint le sanctuaire adverse
        if (toRow == 0 && isPlayer1Turn) {
            gameOver = true;
            notifyGameOver(true);
        } else if (toRow == BOARD_HEIGHT - 1 && !isPlayer1Turn) {
            gameOver = true;
            notifyGameOver(false);
        }
    }

    private void notifyTurnChanged() {
        GameEvent event = new GameEvent(GameEvent.Type.TURN_CHANGED);
        event.setSource(this);
        event.setIsPlayer1Turn(isPlayer1Turn);
        notifyListeners(event);
    }

    private void notifyMoveMade(int fromRow, int fromCol, int toRow, int toCol, GamePiece piece) {
        GameEvent event = new GameEvent(GameEvent.Type.MOVE_MADE);
        event.setSource(this);
        event.setPiece(piece);
        event.setFromRow(fromRow);
        event.setFromCol(fromCol);
        event.setToRow(toRow);
        event.setToCol(toCol);
        notifyListeners(event);
    }

    private void notifyPieceCaptured(GamePiece capturingPiece, GamePiece capturedPiece) {
        GameEvent event = new GameEvent(GameEvent.Type.PIECE_CAPTURED);
        event.setSource(this);
        event.setPiece(capturingPiece);
        event.setCapturedPiece(capturedPiece);
        notifyListeners(event);
    }

    public void addEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void removeEventListener(GameEventListener listener) {
        listeners.remove(listener);
    }
}

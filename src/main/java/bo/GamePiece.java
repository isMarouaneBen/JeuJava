package bo;

import utils.ConsoleDisplay;

public class GamePiece {
    private final String name;
    private final int rank;
    private final boolean isPlayer1;
    private boolean isInTrap;
    private boolean isInRiver;

    public GamePiece(String name, int rank, boolean isPlayer1) {
        this.name = name;
        this.rank = rank;
        this.isPlayer1 = isPlayer1;
        this.isInTrap = false;
        this.isInRiver = false;
    }

    public String getName() {
        return name;
    }

    public int getRank() {
        return rank;
    }

    public boolean isPlayer1() {
        return isPlayer1;
    }

    public boolean isInTrap() {
        return isInTrap;
    }

    public void setInTrap(boolean inTrap) {
        this.isInTrap = inTrap;
    }

    public boolean isInRiver() {
        return isInRiver;
    }

    public void setInRiver(boolean inRiver) {
        this.isInRiver = inRiver;
    }

    public String getSymbol() {
        return ConsoleDisplay.getPieceSymbol(name);
    }

    public boolean canCapture(GamePiece other, boolean isFromRiver) {
        // Si la pièce est dans un piège, elle ne peut pas capturer
        if (this.isInTrap) {
            return false;
        }

        // Le rat ne peut pas capturer en sortant de la rivière
        if (this.name.equalsIgnoreCase("RAT") && isFromRiver) {
            return false;
        }

        // Si la pièce cible est dans un piège, elle peut être capturée par n'importe quelle pièce
        if (other.isInTrap) {
            return true;
        }        // Cas spécial : Rat vs Éléphant
        if ((this.name.equalsIgnoreCase("RAT") && other.name.equalsIgnoreCase("ELEPHANT")) ||
            (this.name.equalsIgnoreCase("ELEPHANT") && other.name.equalsIgnoreCase("RAT"))) {
            // Le rat ne peut pas capturer depuis la rivière
            if (this.name.equalsIgnoreCase("RAT") && this.isInRiver) {
                return false;
            }
            return true;
        }

        // Règle normale : une pièce peut capturer une pièce de rang inférieur ou égal
        return this.rank >= other.rank;
    }

    public boolean canMoveInRiver() {
        return this.name.equalsIgnoreCase("RAT");
    }

    public boolean canJumpRiver() {
        return this.name.equalsIgnoreCase("LION") || this.name.equalsIgnoreCase("TIGRE");
    }
}

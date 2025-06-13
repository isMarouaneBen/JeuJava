package bo;

public enum CaseType {
    NORMAL,
    RIVER,
    TRAP,
    SANCTUARY;

    public static boolean isSpecialCase(int row, int col) {        // Rivière
        if ((row >= 3 && row <= 5) && ((col >= 1 && col <= 2) || (col >= 4 && col <= 5))) {
            return true;
        }
        // Pièges joueur 1 (bas)
        if ((row == 8 && (col == 2 || col == 4)) || (row == 7 && col == 3)) {
            return true;
        }
        // Pièges joueur 2 (haut)
        if ((row == 0 && (col == 2 || col == 4)) || (row == 1 && col == 3)) {
            return true;
        }
        // Sanctuaires
        return (row == 0 && col == 3) || (row == 8 && col == 3);
    }

    public static CaseType getType(int row, int col) {        // Rivière
        if ((row >= 3 && row <= 5) && ((col >= 1 && col <= 2) || (col >= 4 && col <= 5))) {
            return RIVER;
        }
        // Pièges joueur 1 (bas)
        if ((row == 8 && (col == 2 || col == 4)) || (row == 7 && col == 3)) {
            return TRAP;
        }
        // Pièges joueur 2 (haut)
        if ((row == 0 && (col == 2 || col == 4)) || (row == 1 && col == 3)) {
            return TRAP;
        }
        // Sanctuaires
        if ((row == 0 && col == 3) || (row == 8 && col == 3)) {
            return SANCTUARY;
        }
        return NORMAL;
    }
}

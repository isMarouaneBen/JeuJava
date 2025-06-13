package events;

import bo.GamePiece;

public class GameEvent {
    public enum Type {
        MOVE_MADE,
        PIECE_CAPTURED,
        GAME_OVER,
        TURN_CHANGED
    }

    private Type type;
    private Object source;
    private GamePiece piece;
    private GamePiece capturedPiece;
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
    private boolean isPlayer1Turn;
    private boolean winner;
    private int moveCount;
    private long gameDuration;

    public GameEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public GamePiece getPiece() {
        return piece;
    }

    public void setPiece(GamePiece piece) {
        this.piece = piece;
    }

    public GamePiece getCapturedPiece() {
        return capturedPiece;
    }

    public void setCapturedPiece(GamePiece capturedPiece) {
        this.capturedPiece = capturedPiece;
    }

    public int getFromRow() {
        return fromRow;
    }

    public void setFromRow(int fromRow) {
        this.fromRow = fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public void setFromCol(int fromCol) {
        this.fromCol = fromCol;
    }

    public int getToRow() {
        return toRow;
    }

    public void setToRow(int toRow) {
        this.toRow = toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public void setToCol(int toCol) {
        this.toCol = toCol;
    }

    public boolean isPlayer1Turn() {
        return isPlayer1Turn;
    }

    public void setIsPlayer1Turn(boolean isPlayer1Turn) {
        this.isPlayer1Turn = isPlayer1Turn;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }

    public long getGameDuration() {
        return gameDuration;
    }

    public void setGameDuration(long gameDuration) {
        this.gameDuration = gameDuration;
    }
}

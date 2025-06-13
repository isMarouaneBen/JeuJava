package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerStatisticsManager {
    private static final int K_FACTOR = 32; // Facteur K pour le calcul ELO
    private static final int BASE_RATING = 1200;
    
    public Map<String, Object> getPlayerStatistics(String username) throws SQLException {
        String query = """
            SELECT 
                pa.USERNAME, pa.REGISTRATION_DATE,
                pr.RATING, pr.GAMES_PLAYED, pr.GAMES_WON, 
                pr.GAMES_LOST, pr.GAMES_DRAWN, pr.WIN_STREAK,
                pr.BEST_WIN_STREAK, pr.TOTAL_MOVES, pr.TOTAL_CAPTURES,
                pr.LAST_PLAYED
            FROM PLAYER_ACCOUNT pa
            JOIN PLAYER_RATING pr ON pa.ID = pr.PLAYER_ID
            WHERE pa.USERNAME = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("username", rs.getString("USERNAME"));
                stats.put("rating", rs.getInt("RATING"));
                stats.put("registrationDate", rs.getTimestamp("REGISTRATION_DATE"));
                stats.put("gamesPlayed", rs.getInt("GAMES_PLAYED"));
                stats.put("gamesWon", rs.getInt("GAMES_WON"));
                stats.put("gamesLost", rs.getInt("GAMES_LOST"));
                stats.put("gamesDrawn", rs.getInt("GAMES_DRAWN"));
                stats.put("winStreak", rs.getInt("WIN_STREAK"));
                stats.put("bestWinStreak", rs.getInt("BEST_WIN_STREAK"));
                stats.put("totalMoves", rs.getInt("TOTAL_MOVES"));
                stats.put("totalCaptures", rs.getInt("TOTAL_CAPTURES"));
                stats.put("lastPlayed", rs.getTimestamp("LAST_PLAYED"));
                
                // Calculer des statistiques supplémentaires
                int gamesPlayed = rs.getInt("GAMES_PLAYED");
                if (gamesPlayed > 0) {
                    double winRate = (rs.getInt("GAMES_WON") * 100.0) / gamesPlayed;
                    stats.put("winRate", String.format("%.1f%%", winRate));
                    
                    double avgMovesPerGame = rs.getInt("TOTAL_MOVES") / (double) gamesPlayed;
                    stats.put("avgMovesPerGame", String.format("%.1f", avgMovesPerGame));
                }
                
                return stats;
            }
            return null;
        }
    }

    public List<Map<String, Object>> getPlayerGameHistory(String username) throws SQLException {
        String query = """
            SELECT 
                gh.GAME_DATE, gh.GAME_DURATION, gh.MOVES_COUNT,
                gh.PLAYER1_RATING_CHANGE, gh.PLAYER2_RATING_CHANGE,
                gh.PLAYER1_CAPTURES, gh.PLAYER2_CAPTURES,
                p1.USERNAME as PLAYER1_NAME,
                p2.USERNAME as PLAYER2_NAME,
                pw.USERNAME as WINNER_NAME,
                pr1.RATING as PLAYER1_RATING,
                pr2.RATING as PLAYER2_RATING
            FROM GAME_HISTORY gh
            JOIN PLAYER_ACCOUNT p1 ON gh.PLAYER1_ID = p1.ID
            JOIN PLAYER_ACCOUNT p2 ON gh.PLAYER2_ID = p2.ID
            JOIN PLAYER_RATING pr1 ON p1.ID = pr1.PLAYER_ID
            JOIN PLAYER_RATING pr2 ON p2.ID = pr2.PLAYER_ID
            LEFT JOIN PLAYER_ACCOUNT pw ON gh.WINNER_ID = pw.ID
            WHERE p1.USERNAME = ? OR p2.USERNAME = ?
            ORDER BY gh.GAME_DATE DESC
        """;

        List<Map<String, Object>> gameHistory = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> game = new HashMap<>();
                game.put("date", rs.getTimestamp("GAME_DATE"));
                game.put("duration", rs.getInt("GAME_DURATION"));
                game.put("movesCount", rs.getInt("MOVES_COUNT"));
                game.put("player1", rs.getString("PLAYER1_NAME"));
                game.put("player2", rs.getString("PLAYER2_NAME"));
                game.put("winner", rs.getString("WINNER_NAME"));
                game.put("player1Rating", rs.getInt("PLAYER1_RATING"));
                game.put("player2Rating", rs.getInt("PLAYER2_RATING"));
                game.put("player1RatingChange", rs.getInt("PLAYER1_RATING_CHANGE"));
                game.put("player2RatingChange", rs.getInt("PLAYER2_RATING_CHANGE"));
                game.put("player1Captures", rs.getInt("PLAYER1_CAPTURES"));
                game.put("player2Captures", rs.getInt("PLAYER2_CAPTURES"));
                gameHistory.add(game);
            }
        }
        
        return gameHistory;
    }

    public void recordGameResult(int player1Id, int player2Id, Integer winnerId, 
                               int duration, int movesCount, 
                               int player1Captures, int player2Captures) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // Récupérer les classements actuels
            int player1Rating = getPlayerRating(conn, player1Id);
            int player2Rating = getPlayerRating(conn, player2Id);

            // Calculer les changements de classement
            double expectedScore1 = getExpectedScore(player1Rating, player2Rating);
            double expectedScore2 = 1 - expectedScore1;

            double actualScore1, actualScore2;
            if (winnerId == null) { // Match nul
                actualScore1 = 0.5;
                actualScore2 = 0.5;
            } else if (winnerId == player1Id) {
                actualScore1 = 1.0;
                actualScore2 = 0.0;
            } else {
                actualScore1 = 0.0;
                actualScore2 = 1.0;
            }

            int ratingChange1 = (int) (K_FACTOR * (actualScore1 - expectedScore1));
            int ratingChange2 = (int) (K_FACTOR * (actualScore2 - expectedScore2));

            // Enregistrer la partie
            recordGame(conn, player1Id, player2Id, winnerId, duration, movesCount,
                      ratingChange1, ratingChange2, player1Captures, player2Captures);

            // Mettre à jour les statistiques des joueurs
            updatePlayerStats(conn, player1Id, actualScore1 == 1.0, actualScore1 == 0.0,
                            actualScore1 == 0.5, ratingChange1, movesCount, player1Captures);
            updatePlayerStats(conn, player2Id, actualScore2 == 1.0, actualScore2 == 0.0,
                            actualScore2 == 0.5, ratingChange2, movesCount, player2Captures);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getPlayerRating(Connection conn, int playerId) throws SQLException {
        String query = "SELECT RATING FROM PLAYER_RATING WHERE PLAYER_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("RATING");
            }
        }
        return BASE_RATING;
    }

    private double getExpectedScore(int rating1, int rating2) {
        return 1.0 / (1.0 + Math.pow(10, (rating2 - rating1) / 400.0));
    }

    private void recordGame(Connection conn, int player1Id, int player2Id, Integer winnerId,
                          int duration, int movesCount, int ratingChange1, int ratingChange2,
                          int player1Captures, int player2Captures) throws SQLException {
        String query = """
            INSERT INTO GAME_HISTORY (
                PLAYER1_ID, PLAYER2_ID, WINNER_ID, GAME_DURATION,
                MOVES_COUNT, PLAYER1_RATING_CHANGE, PLAYER2_RATING_CHANGE,
                PLAYER1_CAPTURES, PLAYER2_CAPTURES
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, player1Id);
            stmt.setInt(2, player2Id);
            if (winnerId == null) {
                stmt.setNull(3, Types.INTEGER);
            } else {
                stmt.setInt(3, winnerId);
            }
            stmt.setInt(4, duration);
            stmt.setInt(5, movesCount);
            stmt.setInt(6, ratingChange1);
            stmt.setInt(7, ratingChange2);
            stmt.setInt(8, player1Captures);
            stmt.setInt(9, player2Captures);
            stmt.executeUpdate();
        }
    }

    private void updatePlayerStats(Connection conn, int playerId, boolean isWin, boolean isLoss,
                                 boolean isDraw, int ratingChange, int moves, int captures) 
            throws SQLException {
        String query = """
            UPDATE PLAYER_RATING
            SET RATING = RATING + ?,
                GAMES_PLAYED = GAMES_PLAYED + 1,
                GAMES_WON = GAMES_WON + ?,
                GAMES_LOST = GAMES_LOST + ?,
                GAMES_DRAWN = GAMES_DRAWN + ?,
                WIN_STREAK = CASE 
                    WHEN ? THEN WIN_STREAK + 1
                    WHEN ? THEN 0
                    ELSE WIN_STREAK
                END,
                BEST_WIN_STREAK = CASE 
                    WHEN ? AND WIN_STREAK + 1 > BEST_WIN_STREAK THEN WIN_STREAK + 1
                    ELSE BEST_WIN_STREAK
                END,
                TOTAL_MOVES = TOTAL_MOVES + ?,
                TOTAL_CAPTURES = TOTAL_CAPTURES + ?,
                LAST_PLAYED = CURRENT_TIMESTAMP
            WHERE PLAYER_ID = ?
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ratingChange);
            stmt.setInt(2, isWin ? 1 : 0);
            stmt.setInt(3, isLoss ? 1 : 0);
            stmt.setInt(4, isDraw ? 1 : 0);
            stmt.setBoolean(5, isWin);
            stmt.setBoolean(6, isLoss || isDraw);
            stmt.setBoolean(7, isWin);
            stmt.setInt(8, moves);
            stmt.setInt(9, captures);
            stmt.setInt(10, playerId);
            stmt.executeUpdate();
        }
    }

    public List<Map<String, Object>> getGlobalLeaderboard(int limit) throws SQLException {
        String query = """
            SELECT 
                pa.USERNAME,
                pr.RATING,
                pr.GAMES_PLAYED,
                pr.GAMES_WON,
                pr.GAMES_LOST,
                pr.GAMES_DRAWN,
                pr.WIN_STREAK,
                pr.BEST_WIN_STREAK,
                CASE 
                    WHEN pr.GAMES_PLAYED > 0 
                    THEN CAST(pr.GAMES_WON AS FLOAT) / pr.GAMES_PLAYED * 100 
                    ELSE 0 
                END as WIN_RATE
            FROM PLAYER_ACCOUNT pa
            JOIN PLAYER_RATING pr ON pa.ID = pr.PLAYER_ID
            WHERE NOT pa.IS_ADMIN
            ORDER BY pr.RATING DESC, WIN_RATE DESC
            LIMIT ?
        """;

        List<Map<String, Object>> leaderboard = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            int rank = 1;
            while (rs.next()) {
                Map<String, Object> player = new HashMap<>();
                player.put("rank", rank++);
                player.put("username", rs.getString("USERNAME"));
                player.put("rating", rs.getInt("RATING"));
                player.put("gamesPlayed", rs.getInt("GAMES_PLAYED"));
                player.put("gamesWon", rs.getInt("GAMES_WON"));
                player.put("gamesLost", rs.getInt("GAMES_LOST"));
                player.put("gamesDrawn", rs.getInt("GAMES_DRAWN"));
                player.put("winStreak", rs.getInt("WIN_STREAK"));
                player.put("bestWinStreak", rs.getInt("BEST_WIN_STREAK"));
                player.put("winRate", String.format("%.1f%%", rs.getDouble("WIN_RATE")));
                leaderboard.add(player);
            }
        }
        
        return leaderboard;
    }
}

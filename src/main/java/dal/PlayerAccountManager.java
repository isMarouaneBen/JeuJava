package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PlayerAccountManager {    public Map<String, Object> createAccount(String username, String password) throws SQLException {
        String insertQuery = "INSERT INTO PLAYER_ACCOUNT (USERNAME, PASSWORD) VALUES (?, ?)";
        String initRatingQuery = "INSERT INTO PLAYER_RATING (PLAYER_ID, RATING) VALUES (?, 1200)";

        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Créer le compte
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();

                // Récupérer l'ID généré
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int playerId = generatedKeys.getInt(1);

                    // Initialiser le classement
                    PreparedStatement ratingStmt = connection.prepareStatement(initRatingQuery);
                    ratingStmt.setInt(1, playerId);
                    ratingStmt.executeUpdate();

                    connection.commit();

                    Map<String, Object> info = new HashMap<>();
                    info.put("id", playerId);
                    info.put("username", username);
                    info.put("rating", 1200);
                    return info;
                }
                
                connection.rollback();
                return null;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }    public Map<String, Object> login(String username, String password) throws SQLException {
        String query = """
            SELECT pa.ID, pa.USERNAME, pr.RATING 
            FROM PLAYER_ACCOUNT pa 
            JOIN PLAYER_RATING pr ON pa.ID = pr.PLAYER_ID 
            WHERE pa.USERNAME = ? AND pa.PASSWORD = ?
        """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", resultSet.getInt("ID"));
                    info.put("username", resultSet.getString("USERNAME"));
                    info.put("rating", resultSet.getInt("RATING"));
                    return info;
                }
                return null;
            }
        }
    }

    public boolean isAdmin(String username, String password) throws SQLException {
        String query = "SELECT IS_ADMIN FROM PLAYER_ACCOUNT WHERE USERNAME = ? AND PASSWORD = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, password);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean("IS_ADMIN");
            }
        }
    }
}

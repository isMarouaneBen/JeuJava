package dal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String URL = "jdbc:h2:~/test";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() throws SQLException, IOException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/schema.sql"))) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }

            statement.execute(sql.toString());
        }
    }

    public static void resetDatabase() throws SQLException, IOException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            
            // Drop existing tables if they exist
            statement.execute("DROP TABLE IF EXISTS GAME_HISTORY");
            statement.execute("DROP TABLE IF EXISTS PLAYER_ACCOUNT");
            
            // Reinitialize database with schema
            initializeDatabase();
        }
    }
}

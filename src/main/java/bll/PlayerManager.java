package bll;

import bo.Player;
import dal.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    public List<Player> getAllPlayers() throws SQLException {
        List<Player> players = new ArrayList<>();
        String query = "SELECT * FROM PLAYER";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("NAME");
                int score = resultSet.getInt("SCORE");
                players.add(new Player(id, name, score));
            }
        }

        return players;
    }

    public void addPlayer(Player player) throws SQLException {
        String query = "INSERT INTO PLAYER (NAME, SCORE) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, player.getName());
            statement.setInt(2, player.getScore());
            statement.executeUpdate();
        }
    }
}

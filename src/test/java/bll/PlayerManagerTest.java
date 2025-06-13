package bll;

import bo.Player;
import dal.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerManagerTest {

    private PlayerManager playerManager;

    @BeforeEach
    public void setUp() throws SQLException {
        DatabaseManager.initializeDatabase();
        playerManager = new PlayerManager();
    }

    @Test
    public void testAddPlayer() throws SQLException {
        Player player = new Player(0, "Test Player", 100);
        playerManager.addPlayer(player);

        List<Player> players = playerManager.getAllPlayers();
        assertTrue(players.stream().anyMatch(p -> p.getName().equals("Test Player")));
    }

    @Test
    public void testGetAllPlayers() throws SQLException {
        List<Player> players = playerManager.getAllPlayers();
        assertNotNull(players);
    }
}

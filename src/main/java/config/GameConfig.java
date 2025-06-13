package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GameConfig {
    private static final Properties properties = new Properties();
    private static GameConfig instance;

    private GameConfig() {
        try {
            properties.load(new FileInputStream("src/main/resources/config.properties"));
        } catch (IOException e) {
            // Use default values if config file is not found
            setDefaultProperties();
        }
    }

    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }

    private void setDefaultProperties() {
        properties.setProperty("db.url", "jdbc:h2:~/jungle_game");
        properties.setProperty("db.user", "sa");
        properties.setProperty("db.password", "");
        properties.setProperty("game.save.enabled", "true");
        properties.setProperty("game.autosave.interval", "5");
        properties.setProperty("display.colors.enabled", "true");
        properties.setProperty("display.unicode.enabled", "true");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public boolean isColorEnabled() {
        return Boolean.parseBoolean(getProperty("display.colors.enabled"));
    }

    public boolean isUnicodeEnabled() {
        return Boolean.parseBoolean(getProperty("display.unicode.enabled"));
    }
}

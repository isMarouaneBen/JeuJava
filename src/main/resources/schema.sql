-- Drop existing tables if they exist
DROP TABLE IF EXISTS GAME_HISTORY;
DROP TABLE IF EXISTS PLAYER_RATING;
DROP TABLE IF EXISTS PLAYER_ACCOUNT;

-- Create tables
CREATE TABLE PLAYER_ACCOUNT (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    USERNAME VARCHAR(255) NOT NULL UNIQUE,
    PASSWORD VARCHAR(255) NOT NULL,
    IS_ADMIN BOOLEAN DEFAULT FALSE,
    REGISTRATION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table pour les classements et statistiques des joueurs
CREATE TABLE PLAYER_RATING (
    PLAYER_ID INT PRIMARY KEY,
    RATING INT DEFAULT 1200,           -- Score ELO initial
    GAMES_PLAYED INT DEFAULT 0,
    GAMES_WON INT DEFAULT 0,
    GAMES_LOST INT DEFAULT 0,
    GAMES_DRAWN INT DEFAULT 0,
    WIN_STREAK INT DEFAULT 0,          -- Série de victoires actuelle
    BEST_WIN_STREAK INT DEFAULT 0,     -- Meilleure série de victoires
    TOTAL_MOVES INT DEFAULT 0,         -- Nombre total de coups joués
    TOTAL_CAPTURES INT DEFAULT 0,      -- Nombre total de captures
    LAST_PLAYED TIMESTAMP,            -- Dernière partie jouée
    FOREIGN KEY (PLAYER_ID) REFERENCES PLAYER_ACCOUNT(ID)
);

CREATE TABLE GAME_HISTORY (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    PLAYER1_ID INT NOT NULL,
    PLAYER2_ID INT NOT NULL,
    WINNER_ID INT,                     -- NULL en cas d'égalité
    GAME_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    GAME_DURATION INT,                 -- en secondes
    MOVES_COUNT INT DEFAULT 0,
    PLAYER1_RATING_CHANGE INT,         -- Changement de classement pour le joueur 1
    PLAYER2_RATING_CHANGE INT,         -- Changement de classement pour le joueur 2
    PLAYER1_CAPTURES INT DEFAULT 0,    -- Nombre de captures par le joueur 1
    PLAYER2_CAPTURES INT DEFAULT 0,    -- Nombre de captures par le joueur 2
    GAME_TYPE VARCHAR(20) DEFAULT 'STANDARD', -- Pour de futures variantes
    FOREIGN KEY (PLAYER1_ID) REFERENCES PLAYER_ACCOUNT(ID),
    FOREIGN KEY (PLAYER2_ID) REFERENCES PLAYER_ACCOUNT(ID),
    FOREIGN KEY (WINNER_ID) REFERENCES PLAYER_ACCOUNT(ID)
);

-- Insert admin account
INSERT INTO PLAYER_ACCOUNT (USERNAME, PASSWORD, IS_ADMIN) 
VALUES ('adminensah', '1234', TRUE);

-- Insert admin rating
INSERT INTO PLAYER_RATING (PLAYER_ID, RATING) 
SELECT ID, 1200 FROM PLAYER_ACCOUNT WHERE USERNAME = 'adminensah';

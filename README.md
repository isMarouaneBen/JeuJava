# Jungle Game (Xou Dou Qi) - Console Edition

## Overview
A professional implementation of the traditional Chinese board game "Jungle" (Xou Dou Qi) in Java. This console-based application features a complete game system with user authentication, ELO rating, statistics tracking, and a clean layered architecture.

## Architecture

### Layered Architecture
The project follows a clean layered architecture pattern:
- **DAL (Data Access Layer)**: Database operations and data persistence
- **BO (Business Objects)**: Core domain entities
- **BLL (Business Logic Layer)**: Game rules and business logic
- **Console**: User interface and game presentation

### Key Components
- **GameManager**: Core game logic implementation
- **PlayerManager**: User account and authentication handling
- **StatisticsManager**: Player statistics and ELO rating system
- **DatabaseManager**: H2 database connection and management
- **ConsoleDisplay**: ASCII-based game board rendering

## Features
- Complete implementation of official Jungle game rules
- User account system with authentication
- ELO rating system for competitive play
- Detailed player statistics and match history
- Global leaderboard system
- In-game move validation
- Special terrain handling (rivers, traps, sanctuaries)
- Piece hierarchy system with special cases
- Clean console-based UI with colored output

## Technical Stack
- Java 17+
- H2 Database (Embedded)
- Maven for dependency management
- JDBC for database operations

## Requirements
- Java Development Kit (JDK) 17 or higher
- Maven 3.6+
- Windows/Linux/MacOS compatible

## Installation

1. Clone the repository
```bash
git clone https://github.com/isMarouaneBen/JeuJava
cd JeuJava
```

2. Build the project
```bash
mvn clean install
```

3. Run the game
```bash
mvn exec:java -Dexec.mainClass="console.Main"
```

## Database Schema

### PLAYER_ACCOUNT
```sql
CREATE TABLE PLAYER_ACCOUNT (
    ID INTEGER PRIMARY KEY AUTO_INCREMENT,
    USERNAME VARCHAR(50) UNIQUE NOT NULL,
    PASSWORD VARCHAR(50) NOT NULL,
    REGISTRATION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IS_ADMIN BOOLEAN DEFAULT FALSE
);
```

### PLAYER_RATING
```sql
CREATE TABLE PLAYER_RATING (
    PLAYER_ID INTEGER PRIMARY KEY,
    RATING INTEGER DEFAULT 1200,
    GAMES_PLAYED INTEGER DEFAULT 0,
    GAMES_WON INTEGER DEFAULT 0,
    GAMES_LOST INTEGER DEFAULT 0,
    GAMES_DRAWN INTEGER DEFAULT 0,
    WIN_STREAK INTEGER DEFAULT 0,
    BEST_WIN_STREAK INTEGER DEFAULT 0
);
```

### GAME_HISTORY
```sql
CREATE TABLE GAME_HISTORY (
    ID INTEGER PRIMARY KEY AUTO_INCREMENT,
    PLAYER1_ID INTEGER,
    PLAYER2_ID INTEGER,
    WINNER_ID INTEGER,
    GAME_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    GAME_DURATION INTEGER,
    MOVES_COUNT INTEGER,
    PLAYER1_RATING_CHANGE INTEGER,
    PLAYER2_RATING_CHANGE INTEGER
);
```

## Game Rules

### Piece Hierarchy (Strongest to Weakest)
1. ELEPHANT (象)
2. LION (狮)
3. TIGER (虎)
4. PANTHER (豹)
5. WOLF (狼)
6. DOG (狗)
7. CAT (猫)
8. RAT (鼠)

### Special Rules
- RAT and ELEPHANT can capture each other (except when RAT is in river)
- LION and TIGER can jump across rivers
- Only RAT can enter river squares
- Any piece in a trap loses all power
- Winning condition: Entering opponent's sanctuary

## Contributing
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Testing
Run the test suite with:
```bash
mvn test
```

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments
- Original Jungle Game creators
- H2 Database team
- Java community for support and feedback

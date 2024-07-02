package dev.subscripted.database;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.sql.*;

/**
 * Diese Klasse verwaltet die Verbindung zu einer SQLite-Datenbank und führt Abfragen und Aktualisierungen durch.
 * Sie ermöglicht das Abrufen und Setzen von Spieler-XP und -Leveln.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XPSqlManager {
    Connection connection;

    /**
     * Konstruktor stellt eine Verbindung zur SQLite-Datenbank her und erstellt die Tabelle, falls sie nicht existiert.
     */
    public XPSqlManager() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:novibes_xp.db");
            createTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Führt eine SQL-Abfrage durch und gibt das ResultSet zurück.
     * @param qry Die SQL-Abfrage als String.
     * @return Das ResultSet der Abfrage.
     */
    @SneakyThrows
    public ResultSet query(String qry) {
        Statement st = connection.createStatement();
        return st.executeQuery(qry);
    }

    /**
     * Führt eine SQL-Aktualisierung durch und gibt true zurück, wenn mindestens eine Zeile betroffen war.
     * @param qry Die SQL-Aktualisierung als String.
     * @return true, wenn mindestens eine Zeile betroffen war, ansonsten false.
     */
    public boolean update(String qry) {
        try (Statement st = connection.createStatement()) {
            int rows = st.executeUpdate(qry);
            return (rows > 0);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Überprüft, ob ein Benutzer bereits in der Tabelle "players" existiert.
     * @param userId Die Benutzer-ID als String.
     * @return true, wenn der Benutzer existiert, ansonsten false.
     */
    private boolean userAlreadyExists(String userId) {
        String query = "SELECT user_id FROM players WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Erstellt die Tabelle "players" falls diese nicht existiert.
     */
    private void createTableIfNotExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS players (user_id VARCHAR(22) PRIMARY KEY, xp INTEGER, level INTEGER)";
        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gibt die XP eines Benutzers zurück. Falls der Benutzer nicht existiert, wird er mit 0 XP hinzugefügt.
     * @param userId Die Benutzer-ID als String.
     * @return Die XP des Benutzers als int.
     */
    @SneakyThrows
    public int getXP(String userId) {
        String query = "SELECT xp FROM players WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("xp");
            } else {
                setXP(userId, 0);
                return 0;
            }
        }
    }

    /**
     * Setzt die XP eines Benutzers. Falls der Benutzer nicht existiert, wird er hinzugefügt.
     * @param userId Die Benutzer-ID als String.
     * @param xp Die XP als int.
     */
    @SneakyThrows
    public void setXP(String userId, int xp) {
        if (!userAlreadyExists(userId)) {
            String query = "INSERT INTO players (user_id, xp) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, userId);
                statement.setInt(2, xp);
                statement.executeUpdate();
            }
        } else {
            String query = "UPDATE players SET xp = ? WHERE user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, xp);
                statement.setString(2, userId);
                statement.executeUpdate();
            }
        }
    }

    /**
     * Gibt das Level eines Benutzers zurück. Falls der Benutzer nicht existiert, wird er mit Level 1 hinzugefügt.
     * @param userId Die Benutzer-ID als String.
     * @return Das Level des Benutzers als int.
     */
    @SneakyThrows
    public int getLevel(String userId) {
        String query = "SELECT level FROM players WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("level");
            } else {
                setLevel(userId, 1);
                return 1;
            }
        }
    }

    /**
     * Setzt das Level eines Benutzers.
     * @param userId Die Benutzer-ID als String.
     * @param level Das Level als int.
     */
    @SneakyThrows
    public void setLevel(String userId, int level) {
        String query = "UPDATE players SET level = ? WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, level);
            statement.setString(2, userId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Erhöht die XP eines Benutzers um einen angegebenen Betrag.
     * @param userId Die Benutzer-ID als String.
     * @param amount Der Betrag, um den die XP erhöht werden sollen.
     */
    @SneakyThrows
    public void incrementXP(String userId, int amount) {
        int currentXP = getXP(userId);
        int newXP = currentXP + amount;
        setXP(userId, newXP);
    }
}

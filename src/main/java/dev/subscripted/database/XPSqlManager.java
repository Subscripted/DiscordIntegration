package dev.subscripted.database;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.sql.*;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class XPSqlManager {
    Connection connection;

    public XPSqlManager() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:novibes_xp.db");
            createTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public ResultSet query(String qry) {
        ResultSet rs = null;
        Statement st = connection.createStatement();
        rs = st.executeQuery(qry);
        return rs;

    }

    public boolean update(String qry) {
        try {
            Statement st = connection.createStatement();
            int rows = st.executeUpdate(qry);
            st.close();

            return (rows > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SneakyThrows
    private boolean userAlreadyExists(String user_id) {
        ResultSet rs = query("SELECT user_id FROM players WHERE user_id '" + user_id + "'");
        return rs.next();

    }

    @SneakyThrows
    private void createTableIfNotExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS players (user_id VARCHAR(22) PRIMARY KEY, xp INTEGER, level INTEGER)";
        Statement statement = connection.createStatement();
        statement.execute(createTableQuery);
    }

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
            update("UPDATE players SET xp = " + xp + " WHERE user_id = '" + userId + "'");
        }
    }

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

    @SneakyThrows
    public void incrementXP(String userId, int amount) {
        int currentXP = getXP(userId);
        int newXP = currentXP + amount;
        setXP(userId, newXP);
    }
}
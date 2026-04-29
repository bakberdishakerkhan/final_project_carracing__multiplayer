package database;
import model.GameResult;
import utils.Constants;

import java.sql.*;
import utils.Constants;

/**
 * DatabaseManager — подключение к MySQL через JDBC.
 *
 * Покрывает требование "JDBC + MySQL (15%)"
 *
 * Паттерн Singleton: одно подключение на всё приложение.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] MySQL driver not found — DB disabled. Add mysql-connector-java.jar to classpath.");
            connection = null;
            return;
        }
        try {
            connection = DriverManager.getConnection(
                Constants.DB_URL, Constants.DB_USER, Constants.DB_PASSWORD);
            System.out.println("[DB] Connected to MySQL");
            createTableIfNeeded();
        } catch (Exception e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            connection = null;
        }
    }

    /** Singleton — getInstance() */
    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public boolean isConnected() { return connection != null; }

    /** Создать таблицу если её нет */
    private void createTableIfNeeded() {
        String sql = """
            CREATE TABLE IF NOT EXISTS scores (
                id         INT AUTO_INCREMENT PRIMARY KEY,
                username   VARCHAR(50)  NOT NULL,
                score      INT          NOT NULL,
                duration   INT          NOT NULL,
                played_at  DATETIME     NOT NULL
            )
            """;
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(sql);
            System.out.println("[DB] Table 'scores' ready");
        } catch (SQLException e) {
            System.err.println("[DB] createTable error: " + e.getMessage());
        }
    }

    /** Сохранить результат через PreparedStatement */
    public void saveResult(GameResult result) {
        if (!isConnected()) return;
        String sql = "INSERT INTO scores (username, score, duration, played_at) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, result.getUsername());
            ps.setInt   (2, result.getScore());
            ps.setInt   (3, result.getDurationSec());
            ps.setString(4, result.getPlayedAt().toString());
            ps.executeUpdate();
            System.out.println("[DB] Saved result for " + result.getUsername());
        } catch (SQLException e) {
            System.err.println("[DB] saveResult error: " + e.getMessage());
        }
    }

    /** Загрузить топ-10 из БД */
    public java.util.List<GameResult> getTopScores() {
        java.util.List<GameResult> list = new java.util.ArrayList<>();
        if (!isConnected()) return list;
        String sql = "SELECT username, score, duration FROM scores ORDER BY score DESC LIMIT 10";
        try (Statement st  = connection.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new GameResult(
                    rs.getString("username"),
                    rs.getInt   ("score"),
                    rs.getInt   ("duration")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DB] getTopScores error: " + e.getMessage());
        }
        return list;
    }

    public void close() {
        try { if (connection != null) connection.close(); }
        catch (SQLException ignored) {}
    }
}
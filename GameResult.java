package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * GameResult — результат одной игры (POJO).
 * Используется для сохранения в файл и БД.
 */
public class GameResult {

    private String username;
    private int score;
    private int durationSec;
    private LocalDateTime playedAt;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public GameResult(String username, int score, int durationSec) {
        this.username    = username;
        this.score       = score;
        this.durationSec = durationSec;
        this.playedAt    = LocalDateTime.now();
    }

    // Геттеры
    public String        getUsername()    { return username; }
    public int           getScore()       { return score; }
    public int           getDurationSec() { return durationSec; }
    public LocalDateTime getPlayedAt()    { return playedAt; }

    /** Строка для записи в файл */
    public String toFileString() {
        return username + "|" + score + "|" + durationSec + "|" + playedAt.format(FMT);
    }

    /** Восстановление из строки файла */
    public static GameResult fromFileString(String line) {
        String[] p = line.split("\\|");
        if (p.length < 4) throw new IllegalArgumentException("Bad line: " + line);
        GameResult r = new GameResult(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        r.playedAt = LocalDateTime.parse(p[3], FMT);
        return r;
    }

    @Override
    public String toString() {
        return String.format("%-12s  %6d pts  %ds", username, score, durationSec);
    }
}

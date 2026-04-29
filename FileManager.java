package utils;
import model.GameResult;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * FileManager — читает и пишет результаты в scores.txt.
 *
 * Покрывает требование "Работа с файлами (10%)"
 */
public class FileManager {

    private FileManager() {}

    /** Добавить результат в конец файла */
    public static void saveResult(GameResult result) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(Constants.SCORES_FILE, true))) {
            bw.write(result.toFileString());
            bw.newLine();
            System.out.println("[FileManager] Saved: " + result);
        } catch (IOException e) {
            System.err.println("[FileManager] Save error: " + e.getMessage());
        }
    }

    /** Загрузить все результаты, отсортировать по очкам */
    public static List<GameResult> loadResults() {
        List<GameResult> list = new ArrayList<>();
        Path path = Paths.get(Constants.SCORES_FILE);
        if (!Files.exists(path)) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    try { list.add(GameResult.fromFileString(line)); }
                    catch (Exception ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("[FileManager] Load error: " + e.getMessage());
        }

        // Сортировка по убыванию очков
        list.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return list;
    }
}

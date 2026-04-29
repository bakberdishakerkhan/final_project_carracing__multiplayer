
package utils;
/**
 * Constants — все «магические числа» в одном месте.
 * Меняй здесь, изменится везде.
 */
public final class Constants {

    // Размеры окна
    public static final int WINDOW_WIDTH  = 800;
    public static final int WINDOW_HEIGHT = 600;

    // Дорога
    public static final int ROAD_LEFT  = 150;
    public static final int ROAD_RIGHT = 650;
    public static final int ROAD_WIDTH = ROAD_RIGHT - ROAD_LEFT;
    public static final int LANE_COUNT = 3;
    public static final int LANE_WIDTH = ROAD_WIDTH / LANE_COUNT;

    // Игрок 1 (WASD) — синяя машина, левая половина
    public static final int P1_START_X = 240;
    public static final int P1_START_Y = WINDOW_HEIGHT - 120;

    // Игрок 2 (стрелки) — красная машина, правая половина
    public static final int P2_START_X = 510;
    public static final int P2_START_Y = WINDOW_HEIGHT - 120;

    // Размер машины игрока
    public static final int CAR_WIDTH  = 40;
    public static final int CAR_HEIGHT = 70;

    // Скорости
    public static final double PLAYER_SPEED    = 5.0;
    public static final double ENEMY_BASE_SPEED = 3.0;

    // Враги / препятствия / бонусы
    public static final int  MAX_ENEMIES      = 4;
    public static final int  MAX_OBSTACLES    = 3;
    public static final long ENEMY_SPAWN_MS   = 2000L;
    public static final long OBSTACLE_SPAWN_MS= 3000L;
    public static final long BONUS_SPAWN_MS   = 5000L;
    public static final int  ENEMY_W          = 40;
    public static final int  ENEMY_H          = 70;
    public static final int  OBSTACLE_SIZE    = 40;
    public static final int  BONUS_SIZE       = 30;

    // Очки
    public static final int SCORE_PER_SECOND = 10;
    public static final int SCORE_PER_ENEMY  = 20;
    public static final int BONUS_SCORE      = 50;

    // Сложность
    public static final int    DIFFICULTY_INTERVAL_SEC   = 10;
    public static final double DIFFICULTY_SPEED_INCREMENT = 0.5;

    // Игровой цикл (60 FPS)
    public static final long FRAME_DELAY_MS = 1000L / 60;

    // Файл результатов
    public static final String SCORES_FILE = "scores.txt";

    // Сокет (оставлен для выполнения требования по Sockets)
    public static final int    SERVER_PORT = 12345;
    public static final String SERVER_HOST = "localhost";

    // База данных
    public static final String DB_URL      = "jdbc:mysql://localhost:3306/car_racing?useSSL=false&serverTimezone=UTC";
    public static final String DB_USER     = "root";
    public static final String DB_PASSWORD = "root";

    private Constants() {}
}


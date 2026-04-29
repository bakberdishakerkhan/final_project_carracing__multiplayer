package controller;
import model.*;
import network.*;

import utils.*;
import view.GamePanel;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * GameController — главный контроллер.
 *
 * Режимы:
 *   SINGLE     — один игрок (WASD)
 *   LOCAL_2P   — два игрока на одном ПК (WASD + стрелки)
 *   NETWORK_2P — два игрока через сокет
 */
public class GameController {

    public enum State { MENU, WAITING, PLAYING, PAUSED, GAME_OVER }
    public enum Mode  { SINGLE, LOCAL_2P, NETWORK_2P }

    private volatile State state = State.MENU;
    private Mode mode = Mode.SINGLE;

    private PlayerCar player1;
    private PlayerCar player2;

    private final List<EnemyCar> enemies   = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Bonus>    bonuses   = new ArrayList<>();

    private long   startTimeMs;
    private long   elapsedSec;
    private int    difficultyLevel = 1;
    private double currentSpeed   = Constants.ENEMY_BASE_SPEED;

    private long lastEnemySpawn, lastObstacleSpawn, lastBonusSpawn, lastScoreTick;
    private double roadOffset = 0;

    private String name1 = "Player1";
    private String name2 = "Player2";

    private GameClient client = null;
    private GameServer server = null;

    private volatile double  opponentX     = -999;
    private volatile double  opponentY     = -999;
    private volatile int     opponentScore = 0;
    private volatile boolean opponentDead  = false;
    private String resultMessage = "";

    private volatile boolean running = false;
    private GamePanel gamePanel;

   

    public GameController() {
        player1 = new PlayerCar(Constants.P1_START_X, Constants.P1_START_Y,
                                new Color(0x2196F3), "P1");
        player2 = new PlayerCar(Constants.P2_START_X, Constants.P2_START_Y,
                                new Color(0xF44336), "P2");
    }

    public void setGamePanel(GamePanel panel) { this.gamePanel = panel; }

    // ── Запуск режимов ───────────────────────────────────────────────────

    public void startSingle(String n1) {
        name1 = n1;
        mode  = Mode.SINGLE;
        startInternal();
    }

    public void startLocal2P(String n1, String n2) {
        name1 = n1;
        name2 = n2;
        mode  = Mode.LOCAL_2P;
        startInternal();
    }

    /** Хост: запускает сервер и подключается к нему */
    public void hostGame(String playerName) {
        name1  = playerName;
        mode   = Mode.NETWORK_2P;
        server = new GameServer(Constants.SERVER_PORT);
        server.startInBackground();
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        connectAsClient(playerName, Constants.SERVER_HOST);
    }

    /** Гость: подключается к серверу хоста */
    public void joinGame(String playerName, String host) {
        name1 = playerName;
        mode  = Mode.NETWORK_2P;
        connectAsClient(playerName, host);
    }

    private void connectAsClient(String playerName, String host) {
        client = new GameClient(host, Constants.SERVER_PORT);
        client.setListener(new GameClient.Listener() {
            @Override public void onWaiting() {
                state = State.WAITING;
                if (gamePanel != null) gamePanel.repaint();
            }
            @Override public void onGameStart(String n1, String n2) {
                name1 = n1; name2 = n2;
                startInternal();
            }
            @Override public void onOpponentMove(double x, double y, int score) {
                opponentX = x; opponentY = y; opponentScore = score;
            }
            @Override public void onOpponentDead(int finalScore) {
                opponentDead = true;
            }
            @Override public void onResult(String n1, int s1, String n2, int s2) {
                resultMessage = buildResult(n1, s1, n2, s2);
                if (gamePanel != null) gamePanel.repaint();
            }
            @Override public void onDisconnected() {
                opponentDead = true;
            }
        });

        if (client.connect()) {
            state = State.WAITING;
            client.join(playerName);
            if (gamePanel != null) gamePanel.repaint();
        }
    }

    // ── Внутренний запуск ────────────────────────────────────────────────

    private void startInternal() {
        reset();
        state       = State.PLAYING;
        startTimeMs = System.currentTimeMillis();
        lastScoreTick = startTimeMs;
        running     = true;
        startGameLoopThread();
        startEnemyThread();
        System.out.println("[Controller] Started " + mode);
    }

    // ── Потоки ───────────────────────────────────────────────────────────

    /** game-loop-thread — 60 FPS, логика + repaint */
    private void startGameLoopThread() {
        Thread t = new Thread(() -> {
            int frame = 0;
            while (running) {
                long start = System.currentTimeMillis();
                if (state == State.PLAYING) {
                    update();
                    if (mode == Mode.NETWORK_2P && client != null && client.isConnected()) {
                        if (++frame % 2 == 0)
                            client.sendPosition(player1.getX(), player1.getY(), player1.getScore());
                    }
                }
                if (gamePanel != null) gamePanel.repaint();
                long sleep = Constants.FRAME_DELAY_MS - (System.currentTimeMillis() - start);
                if (sleep > 0) {
                    try { Thread.sleep(sleep); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }
        }, "game-loop-thread");
        t.setDaemon(true);
        t.start();
    }

    /** enemy-thread — двигает врагов параллельно */
    private void startEnemyThread() {
        Thread t = new Thread(() -> {
            while (running) {
                if (state == State.PLAYING) {
                    synchronized (enemies) { enemies.forEach(EnemyCar::move); }
                }
                try { Thread.sleep(16); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, "enemy-thread");
        t.setDaemon(true);
        t.start();
    }

    // ── Логика ───────────────────────────────────────────────────────────

    private void update() {
        long now = System.currentTimeMillis();
        elapsedSec = (now - startTimeMs) / 1000;
        roadOffset = (roadOffset + currentSpeed) % 40;

        player1.move();
        if (mode == Mode.LOCAL_2P) player2.move();

        synchronized (obstacles) { obstacles.forEach(Obstacle::move); }
        synchronized (bonuses)   { bonuses.forEach(Bonus::move); }

        int newLevel = 1 + (int)(elapsedSec / Constants.DIFFICULTY_INTERVAL_SEC);
        if (newLevel > difficultyLevel) {
            difficultyLevel = newLevel;
            currentSpeed += Constants.DIFFICULTY_SPEED_INCREMENT;
        }

        if (now - lastScoreTick >= 1000) {
            player1.addScore(Constants.SCORE_PER_SECOND);
            if (mode == Mode.LOCAL_2P) player2.addScore(Constants.SCORE_PER_SECOND);
            lastScoreTick = now;
        }

        if (now - lastEnemySpawn    > Constants.ENEMY_SPAWN_MS)    { spawnEnemy();    lastEnemySpawn    = now; }
        if (now - lastObstacleSpawn > Constants.OBSTACLE_SPAWN_MS) { spawnObstacle(); lastObstacleSpawn = now; }
        if (now - lastBonusSpawn    > Constants.BONUS_SPAWN_MS)    { spawnBonus();    lastBonusSpawn    = now; }

        checkCollisions();
        cleanupOffScreen();
    }

    private void spawnEnemy() {
        synchronized (enemies) {
            if (enemies.size() < Constants.MAX_ENEMIES)
                enemies.add(new EnemyCar(currentSpeed));
        }
    }

    private void spawnObstacle() {
        synchronized (obstacles) {
            if (obstacles.size() < Constants.MAX_OBSTACLES)
                obstacles.add(new Obstacle(currentSpeed));
        }
    }

    private void spawnBonus() {
        synchronized (bonuses) { bonuses.add(new Bonus(currentSpeed)); }
    }

    private void cleanupOffScreen() {
        synchronized (enemies) {
            Iterator<EnemyCar> it = enemies.iterator();
            while (it.hasNext()) {
                EnemyCar e = it.next();
                if (e.isOffScreen()) {
                    if (!e.isScored()) {
                        player1.addScore(Constants.SCORE_PER_ENEMY);
                        if (mode == Mode.LOCAL_2P) player2.addScore(Constants.SCORE_PER_ENEMY);
                        e.setScored(true);
                    }
                    it.remove();
                }
            }
        }
        synchronized (obstacles) { obstacles.removeIf(Obstacle::isOffScreen); }
        synchronized (bonuses)   { bonuses.removeIf(b -> b.isOffScreen() || b.isCollected()); }
    }

    private void checkCollisions() {
        checkPlayer(player1, name1);
        if (mode == Mode.LOCAL_2P && player2.isAlive())
            checkPlayer(player2, name2);
    }

    private void checkPlayer(PlayerCar player, String name) {
        if (!player.isAlive()) return;
        Rectangle2D pb = player.getBounds();

        synchronized (enemies) {
            for (EnemyCar e : enemies)
                if (e.isAlive() && pb.intersects(e.getBounds())) {
                    player.setAlive(false);
                    onPlayerDead(player, name);
                    return;
                }
        }
        synchronized (obstacles) {
            for (Obstacle o : obstacles)
                if (pb.intersects(o.getBounds())) {
                    player.setAlive(false);
                    onPlayerDead(player, name);
                    return;
                }
        }
        synchronized (bonuses) {
            for (Bonus b : bonuses)
                if (!b.isCollected() && pb.intersects(b.getBounds())) {
                    b.collect();
                    player.addScore(b.getScoreValue());
                }
        }
    }

    private void onPlayerDead(PlayerCar player, String name) {
        System.out.println("[Controller] " + name + " died, score=" + player.getScore());
        if (mode == Mode.NETWORK_2P) {
            if (client != null) client.sendDead(player.getScore());
            gameOver();
            return;
        }
        if (mode == Mode.SINGLE) { gameOver(); return; }
        if (!player1.isAlive() && !player2.isAlive()) gameOver();
    }

    public void gameOver() {
        if (state == State.GAME_OVER) return;
        state   = State.GAME_OVER;
        running = false;
        saveResult(player1, name1);
        if (mode == Mode.LOCAL_2P) saveResult(player2, name2);
    }

    private void saveResult(PlayerCar player, String name) {
        int dur = (int)((System.currentTimeMillis() - startTimeMs) / 1000);
        GameResult r = new GameResult(name, player.getScore(), dur);
        FileManager.saveResult(r);
        
    }

    private String buildResult(String n1, int s1, String n2, int s2) {
        String winner = s1 > s2 ? n1 : (s2 > s1 ? n2 : "Draw");
        return String.format("%s: %d pts\n%s: %d pts\n\nWinner: %s",
            n1, s1, n2, s2, s1 == s2 ? "Draw!" : winner + "!");
    }

    public void togglePause() {
        if (state == State.PLAYING) state = State.PAUSED;
        else if (state == State.PAUSED) state = State.PLAYING;
    }

    public void goToMenu() {
        running = false;
        state   = State.MENU;
        if (client != null) { client.disconnect(); client = null; }
        if (server != null) { server.stop();       server = null; }
    }

    private void reset() {
        player1.reset();
        player2.reset();
        synchronized (enemies)   { enemies.clear(); }
        synchronized (obstacles) { obstacles.clear(); }
        synchronized (bonuses)   { bonuses.clear(); }
        currentSpeed    = Constants.ENEMY_BASE_SPEED;
        difficultyLevel = 1;
        roadOffset      = 0;
        elapsedSec      = 0;
        lastEnemySpawn = lastObstacleSpawn = lastBonusSpawn = 0;
        opponentX = opponentY = -999;
        opponentDead  = false;
        resultMessage = "";
    }

    // ── Геттеры ──────────────────────────────────────────────────────────

    public State         getState()         { return state; }
    public Mode          getMode()          { return mode; }
    public PlayerCar     getPlayer1()       { return player1; }
    public PlayerCar     getPlayer2()       { return player2; }
    public List<EnemyCar>  getEnemies()     { return enemies; }
    public List<Obstacle>  getObstacles()   { return obstacles; }
    public List<Bonus>     getBonuses()     { return bonuses; }
    public long          getElapsedSec()    { return elapsedSec; }
    public int           getDiffLevel()     { return difficultyLevel; }
    public double        getRoadOffset()    { return roadOffset; }
    public String        getName1()         { return name1; }
    public String        getName2()         { return name2; }
    public boolean       is2P()             { return mode == Mode.LOCAL_2P; }
    public boolean       isNetworkMode()    { return mode == Mode.NETWORK_2P; }
    public double        getOpponentX()     { return opponentX; }
    public double        getOpponentY()     { return opponentY; }
    public int           getOpponentScore() { return opponentScore; }
    public boolean       isOpponentDead()   { return opponentDead; }
    public String        getResultMessage() { return resultMessage; }
    public String        getOpponentName()  { return client != null ? client.getOpponentName() : name2; }
}
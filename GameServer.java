package network;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * GameServer — TCP сервер для двух игроков.
 *
 * Протокол:
 *   Клиент → Сервер:
 *     JOIN:имя        — подключиться
 *     POS:x:y:score   — позиция каждый кадр
 *     DEAD:score      — игрок умер
 *
 *   Сервер → Клиент:
 *     WELCOME:1 или 2        — твой ID
 *     WAIT                   — жди второго
 *     START:имя1:имя2        — игра началась
 *     OPPONENT:x:y:score     — позиция соперника
 *     OPPONENT_DEAD:score    — соперник умер
 *     RESULT:имя1:s1:имя2:s2 — итог
 */
public class GameServer implements Runnable {

    private final int port;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    private ClientHandler player1 = null;
    private ClientHandler player2 = null;
    private final Object lock = new Object();

    private String name1 = "Player1";
    private String name2 = "Player2";
    private final Map<Integer, Integer> finalScores = new HashMap<>();

    public GameServer(int port) {
        this.port = port;
    }

    public void startInBackground() {
        Thread t = new Thread(this, "game-server");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("[Server] Started on port " + port);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    socket.setTcpNoDelay(true);
                    assignPlayer(socket);
                } catch (SocketException e) {
                    if (running) System.err.println("[Server] " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] Cannot start: " + e.getMessage());
        }
    }

    private void assignPlayer(Socket socket) {
        synchronized (lock) {
            if (player1 == null) {
                player1 = new ClientHandler(socket, 1);
                new Thread(player1, "server-p1").start();
                System.out.println("[Server] Player 1 connected");
            } else if (player2 == null) {
                player2 = new ClientHandler(socket, 2);
                new Thread(player2, "server-p2").start();
                System.out.println("[Server] Player 2 connected");
            } else {
                try {
                    new PrintWriter(socket.getOutputStream(), true).println("FULL");
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private void sendTo(int id, String msg) {
        synchronized (lock) {
            ClientHandler h = (id == 1) ? player1 : player2;
            if (h != null) h.send(msg);
        }
    }

    private boolean bothConnected() {
        synchronized (lock) { return player1 != null && player2 != null; }
    }

    private void onPlayerDisconnected(int id) {
        synchronized (lock) {
            if (id == 1) player1 = null;
            else         player2 = null;
        }
        sendTo(id == 1 ? 2 : 1, "OPPONENT_DEAD:0");
        System.out.println("[Server] Player " + id + " disconnected");
    }

    public void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); }
        catch (IOException ignored) {}
    }

    // ── ClientHandler ────────────────────────────────────────────────────

    private class ClientHandler implements Runnable {
        final int id;
        final Socket socket;
        PrintWriter out;

        ClientHandler(Socket socket, int id) {
            this.socket = socket;
            this.id     = id;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

                send("WELCOME:" + id);
                send("WAIT");

                String line;
                while ((line = in.readLine()) != null) {
                    handle(line.trim());
                }
            } catch (IOException e) {
                System.out.println("[Server] P" + id + " error: " + e.getMessage());
            } finally {
                onPlayerDisconnected(id);
                close();
            }
        }

        private void handle(String cmd) {
            if (cmd.startsWith("JOIN:")) {
                String name = cmd.substring(5).trim();
                if (name.isEmpty()) name = "Player" + id;
                if (id == 1) name1 = name;
                else         name2 = name;
                System.out.println("[Server] P" + id + " name: " + name);

                if (bothConnected()) {
                    sendTo(1, "START:" + name1 + ":" + name2);
                    sendTo(2, "START:" + name1 + ":" + name2);
                    System.out.println("[Server] Game started: " + name1 + " vs " + name2);
                }

            } else if (cmd.startsWith("POS:")) {
                sendTo(id == 1 ? 2 : 1, "OPPONENT:" + cmd.substring(4));

            } else if (cmd.startsWith("DEAD:")) {
                int score = 0;
                try { score = Integer.parseInt(cmd.substring(5).trim()); }
                catch (NumberFormatException ignored) {}

                finalScores.put(id, score);
                sendTo(id == 1 ? 2 : 1, "OPPONENT_DEAD:" + score);

                if (finalScores.size() >= 2) {
                    int s1 = finalScores.getOrDefault(1, 0);
                    int s2 = finalScores.getOrDefault(2, 0);
                    sendTo(1, "RESULT:" + name1 + ":" + s1 + ":" + name2 + ":" + s2);
                    sendTo(2, "RESULT:" + name1 + ":" + s1 + ":" + name2 + ":" + s2);
                    finalScores.clear();
                }
            }
        }

        void send(String msg) {
            if (out != null) {
                out.println(msg);
                System.out.println("[Server] → P" + id + ": " + msg);
            }
        }

        void close() {
            try { if (!socket.isClosed()) socket.close(); }
            catch (IOException ignored) {}
        }
    }
}
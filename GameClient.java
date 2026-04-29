package network;

import java.io.*;
import java.net.*;

/**
 * GameClient — подключается к GameServer.
 * Отправляет позицию каждый кадр, получает позицию соперника.
 */
public class GameClient {

    private final String host;
    private final int    port;

    private Socket      socket;
    private PrintWriter out;
    private volatile boolean connected    = false;
    private volatile boolean gameStarted  = false;

    private volatile double  opponentX     = -999;
    private volatile double  opponentY     = -999;
    private volatile int     opponentScore = 0;
    private volatile boolean opponentDead  = false;

    private volatile int    myId  = 0;
    private volatile String name1 = "";
    private volatile String name2 = "";
    private volatile String resultMessage = "";

    public interface Listener {
        void onWaiting();
        void onGameStart(String n1, String n2);
        void onOpponentMove(double x, double y, int score);
        void onOpponentDead(int finalScore);
        void onResult(String n1, int s1, String n2, int s2);
        void onDisconnected();
    }

    private Listener listener;

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void setListener(Listener l) { this.listener = l; }

    public boolean connect() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 3000);
            socket.setTcpNoDelay(true);
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            System.out.println("[Client] Connected to " + host + ":" + port);
            startListening();
            return true;
        } catch (IOException e) {
            System.err.println("[Client] Cannot connect: " + e.getMessage());
            return false;
        }
    }

    private void startListening() {
        Thread t = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                String line;
                while (connected && (line = in.readLine()) != null) {
                    handleMessage(line.trim());
                }
            } catch (IOException e) {
                if (connected) System.err.println("[Client] Lost connection");
            } finally {
                connected = false;
                if (listener != null) listener.onDisconnected();
            }
        }, "client-listener");
        t.setDaemon(true);
        t.start();
    }

    private void handleMessage(String msg) {
        System.out.println("[Client] <- " + msg);

        if (msg.startsWith("WELCOME:")) {
            myId = Integer.parseInt(msg.substring(8).trim());

        } else if (msg.equals("WAIT")) {
            if (listener != null) listener.onWaiting();

        } else if (msg.startsWith("START:")) {
            String[] p = msg.split(":");
            name1 = p.length > 1 ? p[1] : "Player1";
            name2 = p.length > 2 ? p[2] : "Player2";
            gameStarted = true;
            if (listener != null) listener.onGameStart(name1, name2);

        } else if (msg.startsWith("OPPONENT:")) {
            String[] p = msg.split(":");
            if (p.length >= 4) {
                try {
                    opponentX = Double.parseDouble(p[1].replace(',', '.'));
                    opponentY = Double.parseDouble(p[2].replace(',', '.'));
                    opponentScore = Integer.parseInt(p[3]);
                    if (listener != null)
                        listener.onOpponentMove(opponentX, opponentY, opponentScore);
                } catch (NumberFormatException ignored) {}
            }

        } else if (msg.startsWith("OPPONENT_DEAD:")) {
            int score = 0;
            try { score = Integer.parseInt(msg.substring(14).trim()); }
            catch (NumberFormatException ignored) {}
            opponentDead = true;
            if (listener != null) listener.onOpponentDead(score);

        } else if (msg.startsWith("RESULT:")) {
            String[] p = msg.split(":");
            if (p.length >= 5) {
                try {
                    if (listener != null)
                        listener.onResult(p[1], Integer.parseInt(p[2]),
                                          p[3], Integer.parseInt(p[4]));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public void join(String name)                        { send("JOIN:" + name); }
   public void sendPosition(double x, double y, int s) {
    if (connected && gameStarted)
        send(String.format(java.util.Locale.US, "POS:%.1f:%.1f:%d", x, y, s));
}
    public void sendDead(int score)  { send("DEAD:" + score); }

    public void disconnect() {
        connected = false;
        try { if (socket != null) socket.close(); }
        catch (IOException ignored) {}
    }

    private void send(String msg) {
        if (out != null && connected) out.println(msg);
    }

    public boolean isConnected()      { return connected; }
    public boolean isGameStarted()    { return gameStarted; }
    public int     getMyId()          { return myId; }
    public String  getName1()         { return name1; }
    public String  getName2()         { return name2; }
    public double  getOpponentX()     { return opponentX; }
    public double  getOpponentY()     { return opponentY; }
    public int     getOpponentScore() { return opponentScore; }
    public boolean isOpponentDead()   { return opponentDead; }
    public String  getOpponentName()  { return myId == 1 ? name2 : name1; }
}
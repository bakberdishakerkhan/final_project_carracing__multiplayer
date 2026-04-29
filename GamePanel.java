package view;
import model.*;
import controller.GameController;
import utils.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * GamePanel — игровой холст.
 */
public class GamePanel extends JPanel {

    private final GameController controller;

    public GamePanel(GameController controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        GameController.State st = controller.getState();

        if (st == GameController.State.WAITING) {
            drawWaiting(g2d); g2d.dispose(); return;
        }
        if (st == GameController.State.MENU) {
            g2d.dispose(); return;
        }

        drawGrass(g2d);
        drawRoad(g2d);
        drawObjects(g2d);

        if (controller.getPlayer1().isAlive())
            controller.getPlayer1().draw(g2d);

        if (controller.is2P() && controller.getPlayer2().isAlive())
            controller.getPlayer2().draw(g2d);

        if (controller.isNetworkMode() && !controller.isOpponentDead()) {
            double ox = controller.getOpponentX();
            double oy = controller.getOpponentY();
            if (ox > -999) drawNetworkOpponent(g2d, ox, oy);
        }

        drawHUD(g2d);

        if (st == GameController.State.PAUSED)    drawPause(g2d);
        if (st == GameController.State.GAME_OVER) drawGameOver(g2d);

        g2d.dispose();
    }

    private void drawGrass(Graphics2D g2d) {
        g2d.setColor(new Color(0x2D5A1B));
        g2d.fillRect(0, 0, Constants.ROAD_LEFT, Constants.WINDOW_HEIGHT);
        g2d.fillRect(Constants.ROAD_RIGHT, 0,
                     Constants.WINDOW_WIDTH - Constants.ROAD_RIGHT, Constants.WINDOW_HEIGHT);
        g2d.setColor(new Color(0x1E4D0F));
        double off = controller.getRoadOffset();
        for (int y = 0; y < Constants.WINDOW_HEIGHT + 80; y += 80) {
            int yy = (int)((y + off * 2) % (Constants.WINDOW_HEIGHT + 80));
            g2d.fillRoundRect(20, yy, 28, 48, 8, 8);
            g2d.fillRoundRect(Constants.ROAD_RIGHT + 52, yy, 28, 48, 8, 8);
        }
    }

    private void drawRoad(Graphics2D g2d) {
        g2d.setColor(new Color(0x3C3C3C));
        g2d.fillRect(Constants.ROAD_LEFT, 0, Constants.ROAD_WIDTH, Constants.WINDOW_HEIGHT);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(Constants.ROAD_LEFT - 5, 0, 5, Constants.WINDOW_HEIGHT);
        g2d.fillRect(Constants.ROAD_RIGHT,    0, 5, Constants.WINDOW_HEIGHT);
        g2d.setColor(new Color(255, 255, 255, 160));
        double offset = controller.getRoadOffset();
        for (int lane = 1; lane < Constants.LANE_COUNT; lane++) {
            int lx = Constants.ROAD_LEFT + lane * Constants.LANE_WIDTH;
            for (double y = -40 + (offset % 40); y < Constants.WINDOW_HEIGHT + 40; y += 40)
                g2d.fillRect(lx - 1, (int)y, 2, 22);
        }
    }

    private void drawObjects(Graphics2D g2d) {
        synchronized (controller.getBonuses())   { controller.getBonuses().forEach(b -> b.draw(g2d)); }
        synchronized (controller.getObstacles()) { controller.getObstacles().forEach(o -> o.draw(g2d)); }
        synchronized (controller.getEnemies())   { controller.getEnemies().forEach(e -> e.draw(g2d)); }
    }

    private void drawNetworkOpponent(Graphics2D g2d, double ox, double oy) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        int ix = (int)ox, iy = (int)oy;
        g2d.setColor(new Color(0xF44336));
        g2d.fillRoundRect(ix, iy, Constants.CAR_WIDTH, Constants.CAR_HEIGHT, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 9));
        g2d.drawString("P2", ix + 11, iy + Constants.CAR_HEIGHT / 2 + 4);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.drawString(String.valueOf(controller.getOpponentScore()), ix + 8, iy - 4);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, Constants.WINDOW_WIDTH, 58);
        g2d.setColor(new Color(0xFFD700));
        g2d.fillRect(0, 58, Constants.WINDOW_WIDTH, 2);

        drawPlayerHUD(g2d, controller.getPlayer1(), controller.getName1(),
                      new Color(0x2196F3), 165);

        long sec = controller.getElapsedSec();
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(new Color(0xAAAAAA));
        g2d.drawString("TIME", 370, 20);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(new Color(0x90EE90));
        g2d.drawString(String.format("%02d:%02d", sec / 60, sec % 60), 355, 45);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(new Color(0xAAAAAA));
        g2d.drawString("LVL", 490, 20);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(new Color(0xFF8C00));
        g2d.drawString(String.valueOf(controller.getDiffLevel()), 490, 45);

        if (controller.is2P()) {
            drawPlayerHUD(g2d, controller.getPlayer2(), controller.getName2(),
                          new Color(0xF44336), 560);
        } else if (controller.isNetworkMode()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.setColor(new Color(0xAAAAAA));
            g2d.drawString(controller.getOpponentName(), 560, 18);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.setColor(new Color(0xF44336));
            g2d.drawString(String.valueOf(controller.getOpponentScore()), 560, 44);
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(new Color(0x555577));
        g2d.drawString("WASD | P=Pause | SPACE=Menu", 10, 20);
        if (controller.is2P())        g2d.drawString("P2: Arrows", 10, 35);
        if (controller.isNetworkMode()) g2d.drawString("Network mode", 10, 35);
    }

    private void drawPlayerHUD(Graphics2D g2d, PlayerCar player,
                                String name, Color color, int x) {
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(new Color(0xAAAAAA));
        g2d.drawString(name, x, 18);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(color);
        g2d.drawString(String.valueOf(player.getScore()), x, 44);
        if (!player.isAlive()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.setColor(new Color(0xF44336));
            g2d.drawString("CRASHED", x, 57);
        }
    }

    private void drawWaiting(Graphics2D g2d) {
        g2d.setColor(new Color(0x1A1A2E));
        g2d.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        g2d.setFont(new Font("Arial", Font.BOLD, 34));
        g2d.setColor(new Color(0xFFD700));
        drawCentered(g2d, "Waiting for opponent...", Constants.WINDOW_HEIGHT / 2 - 30);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(new Color(0x888888));
        drawCentered(g2d, "Port: " + Constants.SERVER_PORT + "  (use Join Game on other window)",
                     Constants.WINDOW_HEIGHT / 2 + 20);
        long dots = (System.currentTimeMillis() / 500) % 4;
        g2d.setColor(new Color(0x2196F3));
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        drawCentered(g2d, ".".repeat((int)dots), Constants.WINDOW_HEIGHT / 2 + 60);
    }

    private void drawPause(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        g2d.setColor(Color.WHITE);
        drawCentered(g2d, "PAUSED", Constants.WINDOW_HEIGHT / 2);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.setColor(new Color(0xAAAAAA));
        drawCentered(g2d, "Press P to resume", Constants.WINDOW_HEIGHT / 2 + 40);
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        g2d.setFont(new Font("Arial", Font.BOLD, 54));
        g2d.setColor(new Color(0xF44336));
        drawCentered(g2d, "GAME OVER", Constants.WINDOW_HEIGHT / 2 - 80);

        int cy = Constants.WINDOW_HEIGHT / 2;

        if (controller.isNetworkMode() && !controller.getResultMessage().isEmpty()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.setColor(Color.WHITE);
            String[] lines = controller.getResultMessage().split("\n");
            for (int i = 0; i < lines.length; i++)
                drawCentered(g2d, lines[i], cy + i * 30);
        } else {
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.setColor(new Color(0x2196F3));
            drawCentered(g2d, controller.getName1() + ": " +
                              controller.getPlayer1().getScore() + " pts", cy);
            if (controller.is2P()) {
                g2d.setColor(new Color(0xF44336));
                drawCentered(g2d, controller.getName2() + ": " +
                                  controller.getPlayer2().getScore() + " pts", cy + 36);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.setColor(new Color(0xFFD700));
                int s1 = controller.getPlayer1().getScore();
                int s2 = controller.getPlayer2().getScore();
                String w = s1 > s2 ? controller.getName1() + " wins!" :
                           s2 > s1 ? controller.getName2() + " wins!" : "Draw!";
                drawCentered(g2d, w, cy + 80);
            }
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(new Color(0x888888));
        drawCentered(g2d, "Press SPACE for menu", Constants.WINDOW_HEIGHT - 50);
    }

    private void drawCentered(Graphics2D g2d, String text, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int x = (Constants.WINDOW_WIDTH - fm.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }
}
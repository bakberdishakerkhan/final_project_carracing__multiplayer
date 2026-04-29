package model;
import utils.Constants;

import java.awt.*;

/**
 * PlayerCar — машина игрока.
 * Наследует Car, добавляет управление клавишами и счёт.
 *
 * Два игрока используют разные экземпляры этого класса:
 *  - Player 1: WASD, синяя машина
 *  - Player 2: стрелки, красная машина
 */
public class PlayerCar extends Car {

    private boolean movingLeft, movingRight, movingUp, movingDown;
    private int score;
    private final double startX, startY;
    private final String label;   // "P1" или "P2"

    public PlayerCar(double startX, double startY, Color color, String label) {
        super(startX, startY,
              Constants.PLAYER_SPEED,
              Constants.CAR_WIDTH,
              Constants.CAR_HEIGHT,
              color);
        this.startX = startX;
        this.startY = startY;
        this.label  = label;
        this.score  = 0;
    }

    // ── Movable ──────────────────────────────────────────────────────────

    @Override
    public void move() {
        if (movingLeft)  x -= speed;
        if (movingRight) x += speed;
        if (movingUp   && y > 60)                                  y -= speed * 0.6;
        if (movingDown && y < Constants.WINDOW_HEIGHT - height - 5) y += speed * 0.6;

        // Не выехать за пределы дороги
        if (x < Constants.ROAD_LEFT)           x = Constants.ROAD_LEFT;
        if (x + width > Constants.ROAD_RIGHT)  x = Constants.ROAD_RIGHT - width;
    }

    // ── Drawable ─────────────────────────────────────────────────────────

    @Override
    public void draw(Graphics2D g2d) {
        drawBody(g2d);
        int ix = (int) x, iy = (int) y;

        // Фары
        g2d.setColor(new Color(255, 255, 150, 200));
        g2d.fillOval(ix + 5,          iy + height - 10, 10, 8);
        g2d.fillOval(ix + width - 15, iy + height - 10, 10, 8);

        // Метка (P1 / P2)
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 9));
        g2d.drawString(label, ix + 13, iy + height / 2 + 4);
    }

    // ── Управление ───────────────────────────────────────────────────────

    public void setMovingLeft (boolean v) { movingLeft  = v; }
    public void setMovingRight(boolean v) { movingRight = v; }
    public void setMovingUp   (boolean v) { movingUp    = v; }
    public void setMovingDown (boolean v) { movingDown  = v; }

    // ── Счёт ─────────────────────────────────────────────────────────────

    public int  getScore()           { return score; }
    public void addScore(int delta)  { score += delta; }

    public void reset() {
        x     = startX;
        y     = startY;
        score = 0;
        alive = true;
        movingLeft = movingRight = movingUp = movingDown = false;
    }
}

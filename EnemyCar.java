package model;
import utils.Constants;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/**
 * EnemyCar — вражеская машина, едет сверху вниз.
 * Наследует Car, реализует move() и draw().
 */
public class EnemyCar extends Car {

    private static final Random RND = new Random();
    private boolean scored = false;

    // Случайные цвета врагов
    private static final Color[] COLORS = {
        new Color(0xF44336), new Color(0xFF9800),
        new Color(0x9C27B0), new Color(0x009688)
    };

    public EnemyCar(double speed) {
        super(randomLaneX(), -Constants.ENEMY_H,
              speed,
              Constants.ENEMY_W, Constants.ENEMY_H,
              COLORS[RND.nextInt(COLORS.length)]);
    }

    private static double randomLaneX() {
        int lane = new Random().nextInt(Constants.LANE_COUNT);
        return Constants.ROAD_LEFT + lane * Constants.LANE_WIDTH
               + (Constants.LANE_WIDTH - Constants.ENEMY_W) / 2.0;
    }

    @Override
    public void move() { y += speed; }

    @Override
    public void draw(Graphics2D g2d) {
        drawBody(g2d);
        // Фары спереди (вверху)
        g2d.setColor(new Color(255, 50, 50, 200));
        g2d.fillOval((int)x + 5,           (int)y + 2, 10, 8);
        g2d.fillOval((int)x + width - 15,  (int)y + 2, 10, 8);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x + 4, y + 4, width - 8, height - 8);
    }

    public boolean isOffScreen() { return y > Constants.WINDOW_HEIGHT + 10; }
    public boolean isScored()    { return scored; }
    public void setScored(boolean s) { scored = s; }
}

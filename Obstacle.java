package model;
import interfaces.Movable;
import interfaces.Drawable;
import utils.Constants;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/**
 * Obstacle — неподвижное препятствие (камень/конус).
 * Реализует Movable и Drawable напрямую (не наследует Car).
 */
public class Obstacle implements Movable, Drawable {

    private double x, y;
    private final double speed;
    private static final int SIZE = Constants.OBSTACLE_SIZE;
    private static final Random RND = new Random();

    public Obstacle(double speed) {
        this.speed = speed;
        int lane = RND.nextInt(Constants.LANE_COUNT);
        this.x = Constants.ROAD_LEFT + lane * Constants.LANE_WIDTH
                 + (Constants.LANE_WIDTH - SIZE) / 2.0;
        this.y = -SIZE;
    }

    @Override public void move() { y += speed; }
    @Override public double getX() { return x; }
    @Override public double getY() { return y; }

    @Override
    public void draw(Graphics2D g2d) {
        int ix = (int)x, iy = (int)y;

        // Оранжевый конус
        g2d.setColor(new Color(0xFF6F00));
        int[] px = { ix + SIZE/2, ix, ix + SIZE };
        int[] py = { iy, iy + SIZE, iy + SIZE };
        g2d.fillPolygon(px, py, 3);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(ix + SIZE/4, iy + SIZE*2/3, SIZE/2, SIZE/6);

        g2d.setColor(new Color(0xE65100));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawPolygon(px, py, 3);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x + 4, y + 4, SIZE - 8, SIZE - 8);
    }

    public boolean isOffScreen() { return y > Constants.WINDOW_HEIGHT + 10; }
}

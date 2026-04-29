package model;
import interfaces.Movable;
import interfaces.Drawable;
import utils.Constants;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

/**
 * Bonus — золотая звезда, даёт очки при подборе.
 * Реализует Movable и Drawable.
 */
public class Bonus implements Movable, Drawable {

    private double x, y;
    private final double speed;
    private boolean collected = false;
    private static final int SIZE = Constants.BONUS_SIZE;
    private static final Random RND = new Random();

    public Bonus(double speed) {
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
        if (collected) return;
        int ix = (int)x, iy = (int)y, s = SIZE;

        // Золотая монета
        g2d.setColor(new Color(0xFFD700));
        g2d.fillOval(ix, iy, s, s);
        g2d.setColor(new Color(0xFFA000));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(ix, iy, s, s);

        // Символ $
        g2d.setColor(new Color(0x7B4F00));
        g2d.setFont(new Font("Arial", Font.BOLD, s - 10));
        g2d.drawString("$", ix + s/2 - 5, iy + s/2 + 5);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x + 2, y + 2, SIZE - 4, SIZE - 4);
    }

    public void    collect()      { collected = true; }
    public boolean isCollected()  { return collected; }
    public boolean isOffScreen()  { return y > Constants.WINDOW_HEIGHT + 10; }
    public int     getScoreValue(){ return Constants.BONUS_SCORE; }
}

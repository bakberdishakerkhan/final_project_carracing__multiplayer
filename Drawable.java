package interfaces;
import java.awt.Graphics2D;

/**
 * Drawable — интерфейс для всего что умеет рисовать себя.
 * Реализуют: Car, Obstacle, Bonus.
 */
public interface Drawable {
    void draw(Graphics2D g2d);
}

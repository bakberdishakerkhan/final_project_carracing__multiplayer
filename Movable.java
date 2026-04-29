package interfaces;
/**
 * Movable — интерфейс для всего что умеет двигаться.
 * Реализуют: Car (и все подклассы), Obstacle, Bonus.
 */
public interface Movable {
    void move();
    double getX();
    double getY();
}

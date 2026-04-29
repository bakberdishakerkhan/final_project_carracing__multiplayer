package model;
import interfaces.Movable;
import interfaces.Drawable;


import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Car — абстрактный базовый класс машины.
 *
 * ООП:
 *  - инкапсуляция: поля protected, доступ через геттеры
 *  - наследование: PlayerCar и EnemyCar расширяют Car
 *  - полиморфизм: draw() абстрактный — каждый подкласс рисует себя по-своему
 *  - интерфейсы: implements Movable, Drawable
 */
public abstract class Car implements Movable, Drawable {

    protected double x, y;
    protected double speed;
    protected int width, height;
    protected Color color;
    protected boolean alive;

    protected Car(double x, double y, double speed,
                  int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.width = width;
        this.height = height;
        this.color = color;
        this.alive = true;
    }

    // Movable
    @Override public double getX() { return x; }
    @Override public double getY() { return y; }

    // Drawable — абстрактный, реализуется в подклассах
    @Override public abstract void draw(Graphics2D g2d);

    /** Прямоугольник для проверки столкновений */
    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    /**
     * Базовый рисунок корпуса — вызывается из PlayerCar и EnemyCar.
     * Это пример повторного использования кода через наследование.
     */
    protected void drawBody(Graphics2D g2d) {
        int ix = (int) x, iy = (int) y;

        // Корпус
        g2d.setColor(color);
        g2d.fillRoundRect(ix, iy, width, height, 10, 10);

        // Контур
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(ix, iy, width, height, 10, 10);

        // Лобовое стекло
        g2d.setColor(new Color(150, 200, 255, 180));
        g2d.fillRoundRect(ix + 6, iy + 8, width - 12, 18, 6, 6);

        // Заднее стекло
        g2d.setColor(new Color(150, 200, 255, 130));
        g2d.fillRoundRect(ix + 6, iy + height - 26, width - 12, 16, 6, 6);

        // Колёса
        g2d.setColor(Color.BLACK);
        g2d.fillRoundRect(ix - 5,         iy + 10,          10, 18, 4, 4);
        g2d.fillRoundRect(ix + width - 5, iy + 10,          10, 18, 4, 4);
        g2d.fillRoundRect(ix - 5,         iy + height - 28, 10, 18, 4, 4);
        g2d.fillRoundRect(ix + width - 5, iy + height - 28, 10, 18, 4, 4);
    }

    public boolean isAlive()      { return alive; }
    public void setAlive(boolean a){ this.alive = a; }
    public int  getWidth()        { return width; }
    public int  getHeight()       { return height; }
}

package controller;
import model.PlayerCar;

import java.awt.event.*;

/**
 * InputHandler — обрабатывает нажатия клавиш для двух игроков.
 *
 * Игрок 1 (WASD):   W=вверх, S=вниз, A=влево, D=вправо
 * Игрок 2 (стрелки): UP/DOWN/LEFT/RIGHT
 *
 * Общие: P=пауза, Escape=меню
 */
public class InputHandler extends KeyAdapter {

    private final GameController controller;

    public InputHandler(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Игрок 1 — WASD
        switch (key) {
            case KeyEvent.VK_A -> controller.getPlayer1().setMovingLeft(true);
            case KeyEvent.VK_D -> controller.getPlayer1().setMovingRight(true);
            case KeyEvent.VK_W -> controller.getPlayer1().setMovingUp(true);
            case KeyEvent.VK_S -> controller.getPlayer1().setMovingDown(true);
        }

        // Игрок 2 — стрелки (только в режиме 2P)
        if (controller.is2P()) {
            switch (key) {
                case KeyEvent.VK_LEFT  -> controller.getPlayer2().setMovingLeft(true);
                case KeyEvent.VK_RIGHT -> controller.getPlayer2().setMovingRight(true);
                case KeyEvent.VK_UP    -> controller.getPlayer2().setMovingUp(true);
                case KeyEvent.VK_DOWN  -> controller.getPlayer2().setMovingDown(true);
            }
        }

        // Общие
        if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
            if (controller.getState() == GameController.State.PLAYING ||
                controller.getState() == GameController.State.PAUSED) {
                controller.togglePause();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_A -> controller.getPlayer1().setMovingLeft(false);
            case KeyEvent.VK_D -> controller.getPlayer1().setMovingRight(false);
            case KeyEvent.VK_W -> controller.getPlayer1().setMovingUp(false);
            case KeyEvent.VK_S -> controller.getPlayer1().setMovingDown(false);
        }

        if (controller.is2P()) {
            switch (key) {
                case KeyEvent.VK_LEFT  -> controller.getPlayer2().setMovingLeft(false);
                case KeyEvent.VK_RIGHT -> controller.getPlayer2().setMovingRight(false);
                case KeyEvent.VK_UP    -> controller.getPlayer2().setMovingUp(false);
                case KeyEvent.VK_DOWN  -> controller.getPlayer2().setMovingDown(false);
            }
        }
    }
}

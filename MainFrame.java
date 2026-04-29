package view;
import controller.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainFrame — главное окно. CardLayout: menu / game / leaderboard.
 */
public class MainFrame extends JFrame {

    private final CardLayout     cardLayout = new CardLayout();
    private final JPanel         cardPanel  = new JPanel(cardLayout);
    private final GameController controller = new GameController();
    private final GamePanel      gamePanel  = new GamePanel(controller);

    public MainFrame() {
        super("Car Racing Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        cardPanel.add(new MainMenuPanel(controller, this), "menu");
        cardPanel.add(gamePanel, "game");

        add(cardPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        controller.setGamePanel(gamePanel);

        gamePanel.addKeyListener(new InputHandler(controller));
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE &&
                    controller.getState() == GameController.State.GAME_OVER) {
                    showMenu();
                }
            }
        });

        showMenu();
    }

    public void showMenu() {
        controller.goToMenu();
        cardLayout.show(cardPanel, "menu");
    }

    /**
     * @param name1   имя первого игрока
     * @param name2   имя второго (null если сетевой или одиночный)
     * @param isHost  true = запустить сервер
     * @param joinHost адрес хоста для подключения, null = не сетевой
     */
    public void showGame(String name1, String name2, boolean isHost, String joinHost) {
        if (isHost) {
            controller.hostGame(name1);
        } else if (joinHost != null) {
            controller.joinGame(name1, joinHost);
        } else if (name2 != null) {
            controller.startLocal2P(name1, name2);
        } else {
            controller.startSingle(name1);
        }
        cardLayout.show(cardPanel, "game");
        gamePanel.requestFocusInWindow();
    }

    public void showLeaderboard() {
        cardPanel.add(new LeaderboardPanel(this), "leaderboard");
        cardLayout.show(cardPanel, "leaderboard");
    }
}
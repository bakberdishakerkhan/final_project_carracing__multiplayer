import view.MainFrame;

import javax.swing.*;

/**
 * Main — точка входа приложения.
 */
public class Main {

    public static void main(String[] args) {
        // Устанавливаем системный Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[Main] L&F fallback: " + e.getMessage());
        }

        System.out.println("=================================");
        System.out.println("  CAR RACING GAME");
        System.out.println("  Java " + System.getProperty("java.version"));
        System.out.println("=================================");

        // Запускаем GUI в потоке Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Fatal error:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}

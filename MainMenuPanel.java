package view;
import controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainMenuPanel — главное меню.
 */
public class MainMenuPanel extends JPanel {

    private final GameController controller;
    private final MainFrame      mainFrame;
    private JTextField nameField1;
    private JTextField nameField2;

    public MainMenuPanel(GameController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame  = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(new Color(0x1A1A2E));
        buildUI();
    }

    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx  = 0;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0; add(label("CAR RACING", 40, new Color(0xFFD700)), gbc);
        gbc.gridy = 1; add(label("GAME", 20, new Color(0xF44336)), gbc);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x444466));
        sep.setPreferredSize(new Dimension(300, 2));
        gbc.gridy = 2; add(sep, gbc);

        gbc.gridy = 3; add(smallLabel("Your name:"), gbc);
        nameField1 = nameField("Player1");
        gbc.gridy = 4; add(nameField1, gbc);

        gbc.gridy = 5; add(smallLabel("Player 2 name (local only):"), gbc);
        nameField2 = nameField("Player2");
        gbc.gridy = 6; add(nameField2, gbc);

        JButton singleBtn = makeBtn("Single Player",      new Color(0x4CAF50));
        JButton localBtn  = makeBtn("2 Players (local)",  new Color(0x607D8B));
        JButton hostBtn   = makeBtn("Host Game (Server)", new Color(0x9C27B0));
        JButton joinBtn   = makeBtn("Join Game (Client)", new Color(0xFF9800));
        JButton lbBtn     = makeBtn("Leaderboard",        new Color(0x2196F3));
        JButton exitBtn   = makeBtn("Exit",               new Color(0xF44336));

        singleBtn.addActionListener(e -> mainFrame.showGame(name1(), null, false, null));
        localBtn .addActionListener(e -> mainFrame.showGame(name1(), name2(), false, null));
        hostBtn  .addActionListener(e -> mainFrame.showGame(name1(), null, true, null));
        joinBtn  .addActionListener(e -> onJoin());
        lbBtn    .addActionListener(e -> mainFrame.showLeaderboard());
        exitBtn  .addActionListener(e -> System.exit(0));

        gbc.gridy = 7;  add(singleBtn, gbc);
        gbc.gridy = 8;  add(localBtn,  gbc);
        gbc.gridy = 9;  add(hostBtn,   gbc);
        gbc.gridy = 10; add(joinBtn,   gbc);
        gbc.gridy = 11; add(lbBtn,     gbc);
        gbc.gridy = 12; add(exitBtn,   gbc);

        JLabel hint = new JLabel("P1: WASD  |  P2: Arrows  |  P = Pause",
                                  SwingConstants.CENTER);
        hint.setForeground(new Color(0x555577));
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        gbc.gridy = 13; add(hint, gbc);
    }

    private void onJoin() {
        String host = JOptionPane.showInputDialog(this, "Enter host address:", "localhost");
        if (host == null) return;
        host = host.trim();
        if (host.isEmpty()) host = "localhost";
        mainFrame.showGame(name1(), null, false, host);
    }

    private String name1() {
        String n = nameField1.getText().trim();
        return n.isEmpty() ? "Player1" : n;
    }

    private String name2() {
        String n = nameField2.getText().trim();
        return n.isEmpty() ? "Player2" : n;
    }

    private JLabel label(String text, int size, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(new Color(0xCCCCCC));
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        return l;
    }

    private JTextField nameField(String placeholder) {
        JTextField f = new JTextField(placeholder, 18);
        f.setFont(new Font("Arial", Font.PLAIN, 15));
        f.setHorizontalAlignment(JTextField.CENTER);
        f.setBackground(new Color(0x16213E));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x2196F3), 2),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(270, 42));
        btn.setOpaque(true);
        Color hover = bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }
}
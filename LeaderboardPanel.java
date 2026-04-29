package view;
import model.GameResult;
import utils.FileManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * LeaderboardPanel — показывает топ результатов из файла и БД.
 */
public class LeaderboardPanel extends JPanel {

    private final MainFrame mainFrame;

    public LeaderboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(new Color(0x1A1A2E));
        setLayout(new BorderLayout(10, 10));
        buildUI();
    }

    private void buildUI() {
        // Заголовок
        JLabel title = new JLabel("🏆  LEADERBOARD", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(new Color(0xFFD700));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Таблица
        String[] cols = { "#", "Name", "Score", "Duration" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<GameResult> results = FileManager.loadResults();
        int rank = 1;
        for (GameResult r : results) {
            model.addRow(new Object[]{
                rank++,
                r.getUsername(),
                r.getScore(),
                r.getDurationSec() + "s"
            });
            if (rank > 20) break;
        }

        JTable table = new JTable(model);
        table.setBackground(new Color(0x16213E));
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Arial", Font.PLAIN, 15));
        table.setRowHeight(28);
        table.setGridColor(new Color(0x333355));
        table.getTableHeader().setBackground(new Color(0x0D0D1A));
        table.getTableHeader().setForeground(new Color(0xFFD700));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        // Выравнивание по центру
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < cols.length; i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(new Color(0x1A1A2E));
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        add(scroll, BorderLayout.CENTER);

        // Кнопка назад
        JButton back = new JButton("← Back to Menu");
        back.setFont(new Font("Arial", Font.BOLD, 15));
        back.setBackground(new Color(0x2196F3));
        back.setForeground(Color.WHITE);
        back.setFocusPainted(false);
        back.setBorderPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setOpaque(true);
        back.addActionListener(e -> mainFrame.showMenu());

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(0x1A1A2E));
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }
}

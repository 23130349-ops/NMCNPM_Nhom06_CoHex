package view;

import javax.swing.*;
import java.awt.*;

/**
 * HexFrame – Cửa sổ chính chứa toàn bộ giao diện trò chơi.
 */
public class HexFrame extends JFrame {
    private final HexPanel panel;
    private final JButton undoButton;
    private final JTextArea historyArea;

    public HexFrame(int n) {
        setTitle("Hex Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 1000); // Mở rộng không gian hiển thị cho panel lịch sử nước đi
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Toolbar phía trên (Chứa nút điều khiển) ---
        JPanel controlPanel = new JPanel();
        undoButton = new JButton("Hoàn nước (Undo)");
        controlPanel.add(undoButton);
        add(controlPanel, BorderLayout.NORTH);

        // --- Bàn cờ trung tâm ---
        panel = new HexPanel(n);
        add(panel, BorderLayout.CENTER);

        // --- Thanh Panel bên phải: Hiển thị danh sách các nước đã đi ---
        historyArea = new JTextArea(20, 25);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lịch sử đánh"));
        add(scrollPane, BorderLayout.EAST);

        setVisible(true);
    }

    public HexPanel getPanel() { return panel; }
    public JButton getUndoButton() { return undoButton; }

    /** Cập nhật danh sách text hiển thị lịch sử */
    public void updateHistoryText(String text) {
        historyArea.setText(text);
    }

    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
}
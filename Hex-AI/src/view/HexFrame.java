package view;

import model.GameTimer;

import javax.swing.*;
import java.awt.*;

/**
 * HexFrame – Cửa sổ chính (JFrame) chứa toàn bộ giao diện trò chơi Hex.
 *
 * Liên quan đến Use Case:
 * UC-01 Khởi tạo ván mới – Tạo cửa sổ và HexPanel (bước 4)
 * UC-06 Hiển thị kết quả – showGameOver() hiển thị hộp thoại kết quả
 * (MỚI) Thêm giao diện cho nút Hoàn nước (Undo)
 */
public class HexFrame extends JFrame {
    private final HexPanel panel;

    // TÍNH NĂNG MỚI: Nút Hoàn nước
    private final JButton undoButton;

    private JLabel redLabel;
    private JLabel blueLabel;

    /**
     * UC-01 – Khởi tạo ván mới (Standard flow 4.1.1, bước 4)
     * UC-07 – Chọn chế độ chơi (bước 5): Tạo giao diện chính sau khi chọn xong chế độ.
     */
    public HexFrame(int n) {
        setTitle("Hex Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 1000);
        setLocationRelativeTo(null);

        // Sử dụng BorderLayout để chia không gian: Toolbar ở trên, Bàn cờ ở giữa
        setLayout(new BorderLayout());

        // --- Bắt đầu phần thêm giao diện Undo ---
        JPanel controlPanel = new JPanel();
        undoButton = new JButton("Hoàn nước (Undo)");
        controlPanel.add(undoButton);
        add(controlPanel, BorderLayout.NORTH); // Đặt thanh công cụ ở phía trên
        // --- Kết thúc phần thêm giao diện Undo ---

        // Tích hợp đồng hồ tính giờ
        blueLabel = new JLabel("BLUE: --:--   ");
        blueLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        blueLabel.setForeground(new Color(41, 128, 185));
        controlPanel.add(blueLabel, 0);
        redLabel = new JLabel("   RED: --:-- ");
        redLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        redLabel.setForeground(new Color(192, 41, 43));
        controlPanel.add(redLabel);

        // Khởi tạo và thêm HexPanel vào vùng trung tâm
        panel = new HexPanel(n);
        add(panel, BorderLayout.CENTER);

        setVisible(true);
    }

    /**
     * Trả về HexPanel để HexController điều khiển
     */
    public HexPanel getPanel() {
        return panel;
    }

    /**
     * TÍNH NĂNG MỚI: Trả về nút Undo để HexController có thể đăng ký sự kiện hoặc bật/tắt nút.
     */
    public JButton getUndoButton() {
        return undoButton;
    }

    /**
     * UC-06 – Hiển thị kết quả (Standard flow 4.1.15, bước 2)
     */
    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateTimerDisplay(int redSecs, int blueSecs, int activePlayer, GameTimer.Mode timerMode) {
        redLabel.setText(String.format("   RED: %s ", formatTime(redSecs)));
        blueLabel.setText(String.format(" BLUE: %s   ", formatTime(blueSecs)));
        int dangerThreshold = (timerMode == GameTimer.Mode.PER_MOVE) ? 10 : 30;
        // Cảnh báo nhấp nháy chữ khi sắp hết giờ suy nghĩ
        if (redSecs < dangerThreshold && activePlayer == 1) {
            redLabel.setForeground(redSecs % 2 == 0 ? Color.RED : Color.DARK_GRAY);
        } else {
            redLabel.setForeground(new Color(192, 41, 43));
        }
        if (blueSecs < dangerThreshold && activePlayer == 2) {
            blueLabel.setForeground(blueSecs % 2 == 0 ? Color.BLUE : Color.DARK_GRAY);
        } else {
            blueLabel.setForeground(new Color(41, 128, 185));
        }
    }
    public void setTimerUIActive(boolean active) {
        redLabel.setVisible(active);
        blueLabel.setVisible(active);
    }
    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}

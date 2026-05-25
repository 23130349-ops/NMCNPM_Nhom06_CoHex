package view;

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
}
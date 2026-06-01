package view;

import javax.swing.*;
import java.awt.*;

/**
 * HexFrame – Cửa sổ chính chứa toàn bộ giao diện trò chơi.
 * (CẬP NHẬT MỚI):
 * - Tích hợp nút bấm "Lưu Game" và "Tải Game" lên Toolbar.
 * - Bổ sung các JLabel hiển thị đồng hồ đếm ngược thời gian của hai người chơi Đỏ & Xanh.
 */
public class HexFrame extends JFrame {
    private final HexPanel panel;
    private final JButton undoButton;
    private final JTextArea historyArea;

    // Các thành phần giao diện phục vụ tính năng Lưu/Tải và Đếm thời gian
    private final JButton saveButton;
    private final JButton loadButton;
    private final JLabel lblTimeRed;
    private final JLabel lblTimeBlue;

    public HexFrame(int n) {
        setTitle("Hex Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 1000); // Mở rộng nhẹ chiều rộng để vừa các nút điều khiển mới mà không bị co dòng
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Toolbar phía trên (Chứa nút điều khiển) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        undoButton = new JButton("Hoàn nước (Undo)");
        controlPanel.add(undoButton);

        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));

        // Khởi tạo nút Lưu Game và Tải Game
        saveButton = new JButton("Lưu Game");
        loadButton = new JButton("Tải Game");
        controlPanel.add(saveButton);
        controlPanel.add(loadButton);

        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));

        // Khởi tạo các nhãn hiển thị thời gian đếm ngược
        controlPanel.add(new JLabel("Thời gian còn lại: "));

        lblTimeRed = new JLabel(" ĐỎ: 10:00 ");
        lblTimeRed.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTimeRed.setForeground(Color.RED);

        lblTimeBlue = new JLabel(" XANH: 10:00 ");
        lblTimeBlue.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTimeBlue.setForeground(Color.BLUE);

        controlPanel.add(lblTimeRed);
        controlPanel.add(lblTimeBlue);

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

    // Các hàm Getter để HexController có thể gắn sự kiện (Listeners) và cập nhật dữ liệu
    public JButton getBtnSave() { return saveButton; }
    public JButton getBtnLoad() { return loadButton; }
    public JLabel getLblTimeRed() { return lblTimeRed; }
    public JLabel getLblTimeBlue() { return lblTimeBlue; }

    /**
     * (NHIỆM VỤ NGƯỜI 2): Hàm public để Controller đẩy dữ liệu thời gian (tính bằng giây) xuống hiển thị trên UI
     */
    public void setTimerValues(int redSec, int blueSec) {
        lblTimeRed.setText(String.format(" ĐỎ: %02d:%02d ", redSec / 60, redSec % 60));
        lblTimeBlue.setText(String.format(" XANH: %02d:%02d ", blueSec / 60, blueSec % 60));
    }

    /** Cập nhật danh sách text hiển thị lịch sử */
    public void updateHistoryText(String text) {
        historyArea.setText(text);
    }

    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
}
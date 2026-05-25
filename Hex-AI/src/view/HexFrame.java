package view;

import javax.swing.*;

/**
 * HexFrame – Cửa sổ chính (JFrame) chứa toàn bộ giao diện trò chơi Hex.
 *
 * Liên quan đến Use Case:
 *   UC-01 Khởi tạo ván mới – Tạo cửa sổ và HexPanel (bước 4)
 *   UC-06 Hiển thị kết quả – showGameOver() hiển thị hộp thoại kết quả
 */
public class HexFrame extends JFrame {
    private final HexPanel panel;

    /**
     * UC-01 – Khởi tạo ván mới (Standard flow 4.1.1, bước 4)
     * UC-07 – Chọn chế độ chơi (bước 5): Tạo giao diện chính sau khi chọn xong chế độ.
     *
     * Khởi tạo cửa sổ Swing với kích thước cố định, tạo HexPanel bên trong,
     * rồi hiển thị lên màn hình.
     *
     * @param n kích thước bàn cờ n×n (truyền xuống HexPanel)
     */
    public HexFrame(int n) {
        setTitle("Hex Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 1000);
        setLocationRelativeTo(null);
        panel = new HexPanel(n);
        add(panel);
        setVisible(true);
    }

    /**
     * Trả về HexPanel để HexController có thể:
     * - Đăng ký CellClickListener (UC-02)
     * - Gọi setBoard / repaint / setThinking (UC-01, UC-04, UC-06)
     */
    public HexPanel getPanel() {
        return panel;
    }

    /**
     * UC-06 – Hiển thị kết quả (Standard flow 4.1.15, bước 2)
     * Hiển thị hộp thoại thông báo kết thúc trận đấu ("Victory" / "Defeat").
     *
     * Alternative flow 4.1.16 – Lỗi hiển thị giao diện:
     *   JOptionPane.showMessageDialog có thể thất bại nếu cửa sổ bị đóng;
     *   trong trường hợp đó JVM vẫn duy trì trạng thái kết thúc trận đấu.
     *
     * Lưu ý: Trong luồng hiện tại, HexController dùng JOptionPane.showConfirmDialog
     * trực tiếp thay vì gọi qua phương thức này; phương thức được giữ lại để mở rộng sau.
     *
     * @param message nội dung thông báo kết quả trận đấu
     */
    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
}
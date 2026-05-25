package view;

/**
 * CellClickListener – Interface callback nhận sự kiện click vào ô cờ.
 *
 * Liên quan đến Use Case:
 *   UC-02 Đặt quân cờ (Màu đỏ)      – Standard flow 4.1.3, bước 1-2
 *   UC-03 Kiểm tra nước đi hợp lệ   – Standard flow 4.1.6, bước 1-2
 *
 * HexPanel phát hiện click chuột, xác định ô tương ứng rồi gọi onClick().
 * HexController đăng ký listener này để xử lý logic đặt quân.
 *
 * Alternative flow 4.1.5 / 4.1.8 – Click ngoài phạm vi bàn cờ:
 *   HexPanel.findCell() trả về null → onClick() KHÔNG được gọi → hệ thống bỏ qua thao tác
 */
public interface CellClickListener {
    /**
     * Được gọi khi người chơi click vào một ô hợp lệ trên bàn cờ.
     *
     * @param row chỉ số hàng của ô được click (0-indexed)
     * @param col chỉ số cột của ô được click (0-indexed)
     */
    void onClick(int row, int col);
}
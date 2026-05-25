package model;

/**
 * HumanPlayer – Đại diện cho người chơi thật (điều khiển bằng chuột).
 *
 * Liên quan đến Use Case:
 *   UC-02 Đặt quân cờ (Màu đỏ) – Standard flow 4.1.3
 *   UC-07 Chọn chế độ chơi     – Được khởi tạo khi chế độ có Human
 */
public class HumanPlayer implements Player {
    private final int color;

    /**
     * UC-07 – Chọn chế độ chơi (Standard flow 4.1.18, bước 4)
     * Khởi tạo người chơi thật với màu quân được gán (RED hoặc BLUE).
     *
     * @param color HexGame.RED hoặc HexGame.BLUE
     */
    public HumanPlayer(int color) {
        this.color = color;
    }

    /** Trả về màu quân cờ của người chơi. */
    @Override
    public int getColor() {
        return color;
    }

    /**
     * UC-02 – Phân biệt Human với AI.
     * HexController kiểm tra isHuman() để cho phép xử lý sự kiện click chuột.
     * @return true – đây là người chơi thật
     */
    @Override
    public boolean isHuman() {
        return true;
    }

    /**
     * UC-02 – Nước đi của Human đến từ sự kiện click chuột (HexPanel),
     * không tính toán trong code → trả về null.
     * HexController sẽ bỏ qua giá trị trả về này với Human.
     */
    @Override
    public int[] chooseMove(HexGame game) {
        return null;
    }
}
package model;

/**
 * AIPlayer – Đại diện cho người chơi AI (tự động tính toán nước đi).
 *
 * Liên quan đến Use Case:
 *   UC-04 Tính toán và đặt quân (Màu xanh – AI) – Standard flow 4.1.9
 *   UC-07 Chọn chế độ chơi – Được khởi tạo khi chế độ có AI
 */
public class AIPlayer implements Player {
    private final int color;
    private final HexAI ai;

    /**
     * UC-07 – Chọn chế độ chơi (Standard flow 4.1.18, bước 4)
     * Khởi tạo người chơi AI với màu quân và engine tính toán.
     *
     * @param color HexGame.RED hoặc HexGame.BLUE
     * @param ai    Engine Minimax + Alpha-Beta dùng chung cho các AIPlayer
     */
    public AIPlayer(int color, HexAI ai) {
        this.color = color;
        this.ai    = ai;
    }

    /** Trả về màu quân cờ của AI. */
    @Override
    public int getColor() {
        return color;
    }

    /**
     * UC-04 – Phân biệt AI với Human.
     * HexController kiểm tra isHuman() để chuyển sang luồng tính toán AI thay vì chờ click.
     * @return false – đây là AI, không phải người thật
     */
    @Override
    public boolean isHuman() {
        return false;
    }

    /**
     * UC-04 – Tính toán và đặt quân (Standard flow 4.1.9, bước 2-4)
     * 2. AI phân tích các nước đi khả thi
     * 3. AI sử dụng thuật toán Minimax kết hợp Alpha-Beta (depth = 3)
     * 4. Trả về ô hợp lệ tối ưu [row, col]
     *
     * Alternative flow 4.1.10 – Không còn ô hợp lệ:
     *   HexAI.bestMove() trả về null → HexController không đặt quân → kết thúc trận
     *
     * Alternative flow 4.1.11 – AI vượt quá thời gian xử lý:
     *   depth = 3 giúp giới hạn thời gian tính toán trong mức chấp nhận được.
     *   Nếu bàn cờ lớn hoặc cần timeout cứng, có thể giảm depth hoặc thêm timer tại đây.
     *
     * @param game trạng thái bàn cờ hiện tại
     * @return tọa độ [row, col] tốt nhất, hoặc null nếu không còn ô trống
     */
    @Override
    public int[] chooseMove(HexGame game) {
        return ai.bestMove(game, 3);
    }
}
package model;

/**
 * Player – Interface chung cho tất cả loại người chơi.
 * Được cài đặt bởi HumanPlayer (UC-02) và AIPlayer (UC-04).
 */
public interface Player {
    /** Trả về màu quân cờ (RED hoặc BLUE). */
    int getColor();

    /**
     * UC-02 / UC-04 – Phân biệt người chơi và AI.
     * HexController dùng isHuman() để quyết định xử lý click chuột hay gọi AI.
     * @return true nếu là người chơi thật, false nếu là AI
     */
    boolean isHuman();

    /**
     * UC-02 / UC-04 – Chọn nước đi.
     * - HumanPlayer: trả về null (nước đi đến từ sự kiện click chuột)
     * - AIPlayer: trả về [row, col] tối ưu từ HexAI.bestMove()
     *
     * @param game trạng thái bàn cờ hiện tại
     * @return tọa độ [row, col] của nước đi, hoặc null nếu là người thật
     */
    int[] chooseMove(HexGame game);
}
package controller;

import model.*;
import view.*;
import javax.swing.*;

/**
 * HexController – Bộ điều khiển trung tâm của trò chơi Hex.
 * Liên quan đến các Use Case:
 *   UC-01 Khởi tạo ván mới
 *   UC-02 Đặt quân cờ (Màu đỏ)
 *   UC-04 Tính toán và đặt quân (Màu xanh – AI)
 *   UC-05 Kiểm tra thắng/thua
 *   UC-06 Hiển thị kết quả
 *   UC-07 Chọn chế độ chơi
 *   UC-08 Tùy chọn chơi lại
 */
public class HexController {
    private HexGame game;
    private Player redPlayer;
    private Player bluePlayer;
    private HexFrame frame;
    private HexPanel panel;
    private SetupDialog.Config config;
    private final HexAI ai = new HexAI();

    /**
     * UC-07 – Chọn chế độ chơi (Standard flow 4.1.18)
     * 1. Người chơi chọn chức năng "Khởi tạo ván mới"
     * 2. Hệ thống hiển thị danh sách chế độ chơi (SetupDialog)
     * 3. Người chơi chọn chế độ: Người vs AI / Người vs Người / AI vs AI
     * 4. Hệ thống ghi nhận chế độ chơi được chọn
     *
     * Alternative flow 4.1.19 – Người chơi không chọn chế độ chơi:
     *   Nếu người chơi đóng dialog (config == null) → System.exit(0)
     */
    public HexController() {
        // UC-07 bước 2: Hiện dialog chọn chế độ chơi
        config = new SetupDialog(null).showDialog();
        // UC-07 Alternative flow 4.1.19: Người chơi không chọn → thoát
        if (config == null) System.exit(0);

        // Tạo giao diện chính (1 lần duy nhất)
        frame = new HexFrame(config.size);
        panel = frame.getPanel();
        // UC-02: Đăng ký lắng nghe click từ người chơi
        panel.addCellClickListener(this::handleClick);

        // UC-01 bước 1: Khởi tạo ván đầu tiên
        initGame();
    }

    /**
     * UC-01 – Khởi tạo ván mới (Standard flow 4.1.1)
     * 1. Hệ thống khởi tạo bàn cờ Hex (new HexGame)
     * 2. Hệ thống thiết lập trạng thái ban đầu cho toàn bộ ô cờ
     * 3. Hệ thống xác định chế độ chơi và gán người chơi tương ứng
     * 4. Hệ thống cập nhật giao diện trò chơi
     * 5. Hệ thống thiết lập lượt đi đầu tiên cho Player (RED)
     * 6. Trò chơi bắt đầu hoạt động
     *
     * UC-07 – Chọn chế độ chơi (bước 3-6, Standard flow 4.1.18):
     *   Gán redPlayer / bluePlayer theo config.mode
     *
     * UC-08 – Tùy chọn chơi lại (Standard flow 4.1.21 bước 4-7):
     *   Phương thức này được gọi lại khi người chơi nhấn "Chơi lại"
     *
     * Alternative flow 4.1.2 – Lỗi khởi tạo bàn cờ:
     *   Nếu new HexGame ném ngoại lệ, hệ thống sẽ không khởi tạo được → cần xử lý ở tầng trên
     */
    private void initGame() {
        // UC-01 bước 2: Khởi tạo đối tượng game (toàn bộ ô ở trạng thái EMPTY, lượt đầu = RED)
        game = new HexGame(config.size);

        // UC-07 bước 3-4: Thiết lập người chơi theo chế độ được chọn
        switch (config.mode) {
            case HUMAN_VS_AI:
                // Người (Đỏ) vs Máy (Xanh)
                redPlayer = new HumanPlayer(HexGame.RED);
                bluePlayer = new AIPlayer(HexGame.BLUE, ai);
                break;
            case HUMAN_VS_HUMAN:
                // Người vs Người
                redPlayer = new HumanPlayer(HexGame.RED);
                bluePlayer = new HumanPlayer(HexGame.BLUE);
                break;
            case AI_VS_AI:
                // Máy vs Máy
                redPlayer = new AIPlayer(HexGame.RED, ai);
                bluePlayer = new AIPlayer(HexGame.BLUE, ai);
                break;
        }

        // UC-01 bước 4: Cập nhật bàn cờ lên giao diện
        panel.setBoard(game.getBoard());
        panel.setThinking(false);
        panel.repaint();

        // UC-01 bước 5-6: Bắt đầu lượt đi (lượt đầu tiên thuộc Player – RED)
        nextTurn();
    }

    /**
     * UC-02 – Đặt quân cờ màu đỏ (Standard flow 4.1.3)
     * UC-03 – Kiểm tra nước đi hợp lệ (Standard flow 4.1.6)
     *
     * Standard flow:
     * 1. Người chơi click chọn một ô trên bàn cờ (sự kiện mouseClicked từ HexPanel)
     * 2. Hệ thống xác định tọa độ ô được chọn (r, c)
     * 3. Hệ thống kiểm tra đây có phải lượt của người chơi không (!current.isHuman() → bỏ qua)
     * 4. UC-03: Kiểm tra ô hợp lệ: ô phải trống (isEmpty)
     * 5. Nếu hợp lệ, đặt quân màu đỏ lên ô tương ứng (game.place)
     * 6. Cập nhật giao diện (panel.repaint)
     * 7. UC-05: Kiểm tra điều kiện thắng/thua (isGameOver)
     * 8. Nếu chưa kết thúc, chuyển lượt cho AI (nextTurn)
     *
     * Alternative flow 4.1.4 – Ô đã có quân cờ:
     *   game.isEmpty(r, c) == false → return (bỏ qua, không đặt quân)
     *
     * Alternative flow 4.1.5 / 4.1.8 – Click ngoài phạm vi bàn cờ:
     *   findCell() trong HexPanel trả về null → listener không gọi handleClick
     */
    private void handleClick(int r, int c) {
        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        // Chỉ xử lý khi đến lượt người chơi (Human)
        if (!current.isHuman()) return;

        // UC-03 bước 3-4: Kiểm tra ô hợp lệ (chưa có quân)
        // Alternative flow 4.1.4: ô đã có quân → bỏ qua
        if (!game.isEmpty(r, c)) return;

        // UC-02 bước 4-5: Đặt quân đỏ và cập nhật giao diện
        game.place(r, c, current.getColor());
        panel.repaint();

        // UC-05 bước 2-6: Kiểm tra thắng/thua sau khi đặt quân
        if (isGameOver()) return;

        // UC-02 bước 7: Chuyển lượt cho AI
        nextTurn();
    }

    /**
     * Điều phối lượt chơi tiếp theo.
     *
     * UC-04 – Tính toán và đặt quân (Màu xanh – AI) (Standard flow 4.1.9)
     * 1. Hệ thống nhận trạng thái bàn cờ hiện tại
     * 2. AI phân tích các nước đi khả thi (getOrderedMoves)
     * 3. AI sử dụng thuật toán Minimax + Alpha-Beta để chọn nước đi tối ưu (bestMove)
     * 4. AI chọn ô hợp lệ và đặt quân xanh
     * 5. Hệ thống cập nhật giao diện
     * 6. UC-05: Kiểm tra điều kiện thắng/thua
     *
     * Alternative flow 4.1.10 – Không còn ô hợp lệ:
     *   bestMove() trả về null (move == null) → không đặt quân → isGameOver() xử lý kết thúc
     *
     * Alternative flow 4.1.11 – AI vượt quá thời gian xử lý:
     *   Chạy AI trong Thread riêng (Thread.sleep(400) làm UX mượt hơn);
     *   Nếu cần timeout, có thể giới hạn depth hoặc thời gian tại đây
     */
    private void nextTurn() {
        if (isGameOver()) return;

        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        // Hiển thị trạng thái "đang suy nghĩ" nếu là AI
        panel.setThinking(!current.isHuman());

        if (!current.isHuman()) {
            // UC-04 bước 2-5: Chạy AI trên luồng riêng để không đóng băng UI
            new Thread(() -> {
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                // UC-04 bước 3: Minimax + Alpha-Beta tính nước đi tối ưu
                int[] move = current.chooseMove(game);
                SwingUtilities.invokeLater(() -> {
                    // UC-04 bước 4-5: Đặt quân và cập nhật giao diện
                    // Alternative flow 4.1.10: move == null nếu không còn ô hợp lệ
                    if (move != null) {
                        game.place(move[0], move[1], current.getColor());
                    }
                    panel.repaint();
                    // UC-05: Kiểm tra thắng/thua sau khi AI đặt quân
                    nextTurn();
                });
            }).start();
        }
    }

    /**
     * UC-05 – Kiểm tra thắng/thua (Standard flow 4.1.12)
     * UC-06 – Hiển thị kết quả (Standard flow 4.1.15)
     * UC-08 – Tùy chọn chơi lại (Standard flow 4.1.21)
     *
     * Standard flow UC-05:
     * 1. Hệ thống nhận trạng thái bàn cờ hiện tại
     * 2. Hệ thống dùng DFS kiểm tra điều kiện nối cạnh (trong HexGame.hasPlayerWon)
     * 3. Nếu tồn tại đường nối hoàn chỉnh → xác nhận người thắng → gọi UC-06
     *
     * Alternative flow 4.1.13 – Chưa tồn tại đường nối:
     *   checkWinner() == EMPTY → return false → trò chơi tiếp tục
     *
     * Alternative flow 4.1.14 – Lỗi xử lý dữ liệu:
     *   Được bảo vệ bởi cấu trúc try-catch ở tầng Thread bên ngoài nếu cần
     *
     * Standard flow UC-06:
     * 2. Hiển thị thông báo "RED THẮNG!" hoặc "BLUE THẮNG!" (JOptionPane)
     * 3. Khóa thao tác click (panel.setThinking(true) ngầm hiểu qua luồng)
     * 4. Hiển thị tùy chọn "Chơi lại?"
     *
     * Standard flow UC-08 (4.1.21):
     * - YES → initGame() (khởi tạo lại ván mới)
     * - NO  → System.exit(0) (thoát)
     *
     * Alternative flow 4.1.22 – Không thể khởi tạo lại:
     *   Nếu initGame() ném ngoại lệ → hệ thống giữ nguyên và log lỗi
     *
     * Alternative flow 4.1.23 – Người chơi không chọn chơi lại:
     *   choice == NO_OPTION → System.exit(0)
     *
     * @return true nếu trận đấu đã kết thúc, false nếu vẫn tiếp tục
     */
    private boolean isGameOver() {
        int winner = game.checkWinner();
        if (winner != HexGame.EMPTY) {
            // UC-06 bước 2: Xác định thông báo kết quả
            String msg = (winner == HexGame.RED) ? "RED THẮNG!" : "BLUE THẮNG!";

            // UC-06 bước 3-4 / UC-08 bước 2: Hiển thị kết quả + tùy chọn chơi lại
            int choice = JOptionPane.showConfirmDialog(
                    frame,
                    msg + "\nBạn có muốn chơi lại cùng chế độ?",
                    "Kết thúc ván",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                // UC-08 Standard flow 4.1.21 bước 4-7: Khởi tạo ván mới, giữ nguyên chế độ
                initGame();
            } else {
                // UC-08 Alternative flow 4.1.23: Người chơi không chọn chơi lại → thoát
                System.exit(0);
            }
            return true;
        }
        // UC-05 Alternative flow 4.1.13: Chưa có người thắng → tiếp tục
        return false;
    }

    /**
     * Entry point – Khởi chạy ứng dụng trên Event Dispatch Thread của Swing.
     * Tương ứng với Pre-condition UC-01: "Ứng dụng đã được khởi chạy thành công".
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(HexController::new);
    }
}
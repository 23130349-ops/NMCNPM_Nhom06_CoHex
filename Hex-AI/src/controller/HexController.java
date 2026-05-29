package controller;

import model.*;
import view.*;
import javax.swing.*;
import java.util.ArrayList;

/**
 * HexController – Bộ điều khiển trung tâm của trò chơi Hex.
 * Liên quan đến các Use Case:
 * UC-01 Khởi tạo ván mới
 * UC-02 Đặt quân cờ (Màu đỏ)
 * UC-04 Tính toán và đặt quân (Màu xanh – AI)
 * UC-05 Kiểm tra thắng/thua
 * UC-06 Hiển thị kết quả
 * UC-07 Chọn chế độ chơi
 * UC-08 Tùy chọn chơi lại
 * (MỚI) Xử lý sự kiện Hoàn nước (Undo) - Chỉ áp dụng cho chế độ Người vs AI
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

        // Đăng ký sự kiện cho nút Undo
        frame.getUndoButton().addActionListener(e -> handleUndo());

        // UC-01 bước 1: Khởi tạo ván đầu tiên
        initGame();
    }

    /**
     * TÍNH NĂNG MỚI CẬP NHẬT: Xử lý sự kiện khi người chơi nhấn Undo
     * - CHỈ hoạt động ở chế độ HUMAN_VS_AI (Lùi 2 bước: bỏ nước AI, bỏ nước người chơi).
     * - Các chế độ khác nút sẽ bị khóa hoàn toàn từ giao diện, không thể tương tác.
     */
    public void handleUndo() {
        if (game == null) return;

        // Chỉ cho phép hoạt động ở chế độ Người vs AI
        if (config.mode == SetupDialog.Mode.HUMAN_VS_AI) {
            // Ngăn người chơi bấm Undo liên tục khi AI đang suy nghĩ (tránh lỗi luồng xử lý)
            Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
            if (!current.isHuman()) {
                return;
            }

            // Lùi đúng 2 bước (Xóa nước của AI -> Xóa nước của chính mình)
            game.undo();
            game.undo();

            // Cập nhật lại bàn cờ lên giao diện sau khi lùi nước
            panel.repaint();
        }
    }

    /**
     * UC-01 – Khởi tạo ván mới (Standard flow 4.1.1)
     */
    private void initGame() {
        game = new HexGame(config.size);

        switch (config.mode) {
            case HUMAN_VS_AI:
                redPlayer = new HumanPlayer(HexGame.RED);
                bluePlayer = new AIPlayer(HexGame.BLUE, ai);
// BẬT nút Undo trên giao diện nếu chơi với Máy
                frame.getUndoButton().setEnabled(true);
                break;
            case HUMAN_VS_HUMAN:
                redPlayer = new HumanPlayer(HexGame.RED);
                bluePlayer = new HumanPlayer(HexGame.BLUE);
                // TẮT/KHÓA nút Undo trên giao diện nếu chơi Người vs Người
                frame.getUndoButton().setEnabled(false);
                break;
            case AI_VS_AI:
                redPlayer = new AIPlayer(HexGame.RED, ai);
                bluePlayer = new AIPlayer(HexGame.BLUE, ai);
                // TẮT/KHÓA nút Undo trên giao diện nếu xem Máy vs Máy
                frame.getUndoButton().setEnabled(false);
                break;
        }

        panel.setBoard(game.getBoard());
        panel.setLastMove(-1, -1);
        panel.setWinningPath(new ArrayList<>());
        panel.setThinking(false);
        panel.repaint();

        nextTurn();
    }

    /**
     * UC-02 – Đặt quân cờ màu đỏ (Standard flow 4.1.3)
     */
    private void handleClick(int r, int c) {
        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        if (!current.isHuman()) return;

        if (!game.isEmpty(r, c)) {
            JOptionPane.showMessageDialog(frame, "Ô đã được đặt", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        game.place(r, c, current.getColor());
        panel.setLastMove(r, c);
        panel.repaint();

        if (isGameOver()) return;

        nextTurn();
    }

    /**
     * Điều phối lượt chơi tiếp theo (bao gồm logic luồng cho AI).
     */
    private void nextTurn() {
        if (isGameOver()) return;

        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        panel.setThinking(!current.isHuman());

        if (!current.isHuman()) {
            new Thread(() -> {
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}

                // Tránh trường hợp người chơi vừa ấn Undo làm rỗng bàn cờ nhưng AI vẫn tính toán
                if (game.getCurrent() != current.getColor()) return;

                int[] move = current.chooseMove(game);

                SwingUtilities.invokeLater(() -> {
                    // Kiểm tra lại lượt trong luồng UI phòng trường hợp Undo diễn ra cùng lúc
                    if (game.getCurrent() != current.getColor()) return;

                    if (move != null) {
                        game.place(move[0], move[1], current.getColor());
                        panel.setLastMove(move[0], move[1]);
                    }
                    panel.repaint();

                    nextTurn();
                });
            }).start();
        }
    }

    /**
     * UC-05 – Kiểm tra thắng/thua & UC-08 – Tùy chọn chơi lại
     */
    private boolean isGameOver() {
        int winner = game.checkWinner();
        if (winner != HexGame.EMPTY) {
            panel.setWinningPath(game.getWinningPath());
            panel.repaint();
            String msg = (winner == HexGame.RED) ? "RED THẮNG!" : "BLUE THẮNG!";

            int choice = JOptionPane.showConfirmDialog(
                    frame,
                    msg + "\nBạn có muốn chơi lại cùng chế độ?",
                    "Kết thúc ván",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                initGame();
            } else {
                System.exit(0);
            }
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HexController::new);
    }
}

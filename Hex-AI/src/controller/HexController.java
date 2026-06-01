package controller;

import model.*;
import view.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

/**
 * HexController – Bộ điều khiển trung tâm liên kết dữ liệu Model và View.
 */
public class HexController {
    private HexGame game;
    private Player redPlayer;
    private Player bluePlayer;
    private HexFrame frame;
    private HexPanel panel;
    private SetupDialog.Config config;
    private final HexAI ai = new HexAI();

    public HexController() {
        config = new SetupDialog(null).showDialog();
        if (config == null) System.exit(0);

        frame = new HexFrame(config.size);
        panel = frame.getPanel();

        panel.addCellClickListener(this::handleClick);
        frame.getUndoButton().addActionListener(e -> handleUndo());

        // Đăng ký sự kiện cho menu Save và Load
        frame.getSaveMenuItem().addActionListener(e -> handleSave());
        frame.getLoadMenuItem().addActionListener(e -> handleLoad());

        initGame();
    }

    /**
     * Xử lý sự kiện Người chơi nhấn "Save Game".
     * Mở hộp thoại chọn đường dẫn và ghi trạng thái bàn cờ ra file.
     */
    private void handleSave() {
        if (game == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu ván đấu");
        chooser.setSelectedFile(new File("savegame.txt"));
        int result = chooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                GameSaver.saveGame(game, chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(frame, "Lưu thành công!", "Save Game", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Save lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Xử lý sự kiện Người chơi nhấn "Load Game".
     * Mở hộp thoại chọn file, đọc và khôi phục lại ván đấu.
     */
    private void handleLoad() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Tải ván đấu");
        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                HexGame loadedGame = GameSaver.loadGame(chooser.getSelectedFile().getAbsolutePath());

                // Chỉ nạp được file có cùng kích thước bàn cờ
                if (loadedGame.getSize() != config.size) {
                    JOptionPane.showMessageDialog(frame,
                            "Kích thước bàn cờ không khớp! (File: " + loadedGame.getSize() + ", Hiện tại: " + config.size + ")",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                game = loadedGame;
                panel.setBoard(game.getBoard());
                panel.setLastMove(-1, -1);
                panel.setWinningPath(new ArrayList<>());
                panel.setThinking(false);
                panel.repaint();

                nextTurn();

                JOptionPane.showMessageDialog(frame, "Tải ván đấu thành công!", "Load Game", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Save lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Xử lý sự kiện khi ấn nút Undo.
     * Trong chế độ đánh với máy, lùi lại 2 nước để trả lượt về cho người chơi.
     */
    public void handleUndo() {
        if (game == null) return;

        if (config.mode == SetupDialog.Mode.HUMAN_VS_AI) {
            Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
            if (!current.isHuman()) return;

            game.undo();
            game.undo();

            syncUI();
        }
    }

    private void initGame() {
        game = new HexGame(config.size);

        switch (config.mode) {
            case HUMAN_VS_AI:
                redPlayer = new HumanPlayer(HexGame.RED);
                bluePlayer = new AIPlayer(HexGame.BLUE, ai);
                frame.getUndoButton().setEnabled(true);
                break;
            case HUMAN_VS_HUMAN:
                redPlayer = new HumanPlayer(HexGame.RED);
                bluePlayer = new HumanPlayer(HexGame.BLUE);
                frame.getUndoButton().setEnabled(false);
                break;
            case AI_VS_AI:
                redPlayer = new AIPlayer(HexGame.RED, ai);
                bluePlayer = new AIPlayer(HexGame.BLUE, ai);
                frame.getUndoButton().setEnabled(false);
                break;
        }

        panel.setWinningPath(null);
        syncUI();
        panel.setThinking(false);

        nextTurn();
    }

    private void handleClick(int r, int c) {
        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        if (!current.isHuman()) return;

        if (!game.isEmpty(r, c)) {
            JOptionPane.showMessageDialog(frame, "Ô đã được đặt", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        game.place(r, c, current.getColor());
        syncUI();

        if (isGameOver()) return;

        nextTurn();
    }

    private void nextTurn() {
        if (isGameOver()) return;

        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        panel.setThinking(!current.isHuman());

        if (!current.isHuman()) {
            new Thread(() -> {
                try { Thread.sleep(400); } catch (InterruptedException ignored) {}

                if (game.getCurrent() != current.getColor()) return;

                int[] move = current.chooseMove(game);

                SwingUtilities.invokeLater(() -> {
                    if (game.getCurrent() != current.getColor()) return;

                    if (move != null) {
                        game.place(move[0], move[1], current.getColor());
                    }
                    syncUI();

                    nextTurn();
                });
            }).start();
        }
    }

    /**
     * Đồng bộ hóa dữ liệu từ Model sang View:
     * - Cập nhật ma trận ô cờ
     * - Cập nhật văn bản vùng danh sách lịch sử đánh
     * - Cập nhật ô vừa đánh cuối cùng để vẽ viền highlight
     */
    private void syncUI() {
        panel.setBoard(game.getBoard());

        Stack<int[]> history = game.getMoveHistory();
        StringBuilder sb = new StringBuilder();
        int turnCount = 1;

        for (int i = 0; i < history.size(); i++) {
            int[] move = history.get(i);
            String playerStr = (i % 2 == 0) ? "ĐỎ" : "XANH";
            sb.append(String.format("Lượt %d: %s (%d, %d)\n", turnCount++, playerStr, move[0], move[1]));
        }
        frame.updateHistoryText(sb.toString());

        if (!history.isEmpty()) {
            int[] last = history.peek();
            panel.setLastMove(last[0], last[1]);
        } else {
            panel.setLastMove(-1, -1);
        }

        panel.repaint();
    }

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
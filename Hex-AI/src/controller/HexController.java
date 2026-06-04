package controller;

import model.*;
import view.*;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

/**
 * HexController – Bộ điều khiển trung tâm liên kết dữ liệu Model và View.
 * (CẬP NHẬT MỚI):
 * - Triển khai javax.swing.Timer xử lý đếm ngược thời gian theo thời gian thực.
 * - Xử lý Lưu/Tải trực tiếp vào file cố định "hex_save.dat" không qua
 * JFileChooser.
 * - Cập nhật đồng bộ nhãn hiển thị thời gian lên UI mỗi giây hoặc khi đổi trạng
 * thái game.
 * - Xử lý quay về Menu chính an toàn, không làm tràn RAM.
 */
public class HexController {
    private HexGame game;
    private Player redPlayer;
    private Player bluePlayer;
    private HexFrame frame;
    private HexPanel panel;
    private SetupDialog.Config config;
    private final HexAI ai = new HexAI();

    // Đối tượng điều khiển vòng lặp đếm ngược thời gian mỗi giây
    private Timer countdownTimer;

    // Tên file lưu trữ cố định ngay trong thư mục dự án
    private static final String SAVE_FILE_NAME = "hex_save.dat";

    public HexController() {
        config = new SetupDialog(null).showDialog();
        if (config == null) {
            System.exit(0);
        }

        frame = new HexFrame(config.size);
        panel = frame.getPanel();

        panel.addCellClickListener(this::handleClick);
        frame.getUndoButton().addActionListener(e -> handleUndo());

        // Đăng ký sự kiện cho menu Save và Load
        frame.getSaveMenuItem().addActionListener(e -> handleSave());
        frame.getLoadMenuItem().addActionListener(e -> handleLoad());

        // Đăng ký sự kiện lắng nghe cho bộ đôi nút Lưu và Tải game trực tiếp
        frame.getBtnSave().addActionListener(e -> handleSaveGame());
        frame.getBtnLoad().addActionListener(e -> handleLoadGame());

        // Đăng ký sự kiện cho nút Quay về Menu
        frame.getBtnBackToMenu().addActionListener(e -> handleBackToMenu());

        initGame();
    }

    /**
     * Xử lý quay về Menu chính khi đang trong ván đấu
     */
    private void handleBackToMenu() {
        int choice = JOptionPane.showConfirmDialog(
                frame,
                "Bạn có chắc muốn kết thúc ván đấu hiện tại và quay về Menu?",
                "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            // Dừng đồng hồ đếm ngược để chặn không cho nó chạy ngầm
            if (countdownTimer != null) {
                countdownTimer.stop();
            }

            // Tiêu hủy hoàn toàn cửa sổ trận đấu cũ
            frame.dispose();

            // Kích hoạt lại luồng Menu từ đầu
            SwingUtilities.invokeLater(HexController::new);
        }
    }

    /**
     * Khởi động luồng đếm ngược thời gian cho người chơi hiện tại
     */
    private void startCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        // Nếu người chơi chọn "Không dùng", ta thoát luôn hàm, không chạy Timer nữa.
        if (config.timerMode == GameTimer.Mode.NONE) {
            frame.setTimerValues(0, 0);
            return;
        }

        countdownTimer = new Timer(1000, (ActionEvent e) -> {
            if (game == null || isGameOverLogicCheck()) {
                countdownTimer.stop();
                return;
            }

            if (game.getCurrent() == HexGame.RED) {
                game.setRedTimeLeft(game.getRedTimeLeft() - 1);
            } else {
                game.setBlueTimeLeft(game.getBlueTimeLeft() - 1);
            }

            // Gọi hàm public của frame theo đúng yêu cầu đề bài thiết kế UI
            frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());

            if (game.getRedTimeLeft() <= 0) {
                countdownTimer.stop();
                panel.setThinking(true);
                JOptionPane.showMessageDialog(
                        frame,
                        "Người chơi ĐỎ đã hết thời gian! XANH giành chiến thắng.",
                        "Hết giờ",
                        JOptionPane.INFORMATION_MESSAGE);
                handleBackToMenu();
            } else if (game.getBlueTimeLeft() <= 0) {
                countdownTimer.stop();
                panel.setThinking(true);
                JOptionPane.showMessageDialog(
                        frame,
                        "Người chơi XANH đã hết thời gian! ĐỎ giành chiến thắng.",
                        "Hết giờ",
                        JOptionPane.INFORMATION_MESSAGE);
                handleBackToMenu();
            }
        });

        countdownTimer.start();
    }

    /**
     * Xử lý sự kiện Người chơi nhấn "Save Game" (Menu).
     * Mở hộp thoại chọn đường dẫn và ghi trạng thái bàn cờ ra file.
     */
    private void handleSave() {
        if (game == null)
            return;

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
     * Lưu trực tiếp vào file cố định "hex_save.dat" (Nút Quick Save)
     */
    private void handleSaveGame() {
        if (game == null)
            return;

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_NAME))) {
            oos.writeObject(game);
            JOptionPane.showMessageDialog(frame, "Đã lưu ván game hiện tại thành công!", "Lưu Game",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Lỗi hệ thống khi lưu file: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý sự kiện Người chơi nhấn "Load Game" (Menu).
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
                    JOptionPane.showMessageDialog(
                            frame,
                            "Kích thước bàn cờ không khớp! (File: " + loadedGame.getSize() + ", Hiện tại: "
                                    + config.size + ")",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                game = loadedGame;
                panel.setBoard(game.getBoard());
                panel.setLastMove(-1, -1);
                panel.setWinningPath(new ArrayList<>());
                panel.setThinking(false);
                panel.repaint();

                // Đồng bộ thời gian và danh sách nước đi
                syncUI();
                frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());
                startCountdown();

                nextTurn();

                JOptionPane.showMessageDialog(frame, "Tải ván đấu thành công!", "Load Game",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Save lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Tải trực tiếp dữ liệu từ file "hex_save.dat" có sẵn (Nút Quick Load)
     */
    private void handleLoadGame() {
        File file = new File(SAVE_FILE_NAME);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(frame, "Không tìm thấy dữ liệu ván đấu nào được lưu trước đó!", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            HexGame loadedGame = (HexGame) ois.readObject();
            this.game = loadedGame;

            syncUI();

            // Đẩy dữ liệu thời gian từ game vừa load xuống Frame qua hàm public
            frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());

            startCountdown();
            nextTurn();

            JOptionPane.showMessageDialog(frame, "Đã khôi phục lại ván game thành công!", "Tải Game",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Lỗi khi đọc dữ liệu file lưu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý sự kiện khi ấn nút Undo.
     * Trong chế độ đánh với máy, lùi lại 2 nước để trả lượt về cho người chơi.
     */
    public void handleUndo() {
        if (game == null)
            return;

        if (config.mode == SetupDialog.Mode.HUMAN_VS_AI) {
            Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;

            // Chỉ cho Undo khi đang tới lượt người chơi
            if (!current.isHuman()) {
                return;
            }

            // Người vs AI cần lùi 2 nước: nước của AI và nước của người chơi
            if (game.getMoveHistory().size() < 2) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Không còn đủ nước đi để hoàn tác!",
                        "Undo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            game.undo();
            game.undo();

            // Reset thời gian của người đang tới lượt sau khi Undo
            resetCurrentTurnTimerAfterUndo();

            panel.setThinking(false);
            panel.setWinningPath(null);

            syncUI();
            frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());
        }
    }

    private void resetCurrentTurnTimerAfterUndo() {
        if (config.timerMode != GameTimer.Mode.PER_MOVE) {
            return;
        }

        if (game.getCurrent() == HexGame.RED) {
            game.setRedTimeLeft(config.timerSeconds);
        } else if (game.getCurrent() == HexGame.BLUE) {
            game.setBlueTimeLeft(config.timerSeconds);
        }
    }

    private void initGame() {
        game = new HexGame(config.size);

        switch (config.mode) {
            case HUMAN_VS_AI:
                redPlayer = new HumanPlayer(HexGame.RED);
                bluePlayer = new AIPlayer(HexGame.BLUE, ai, config.depth);

                frame.getUndoButton().setEnabled(true);
                break;

            case HUMAN_VS_HUMAN:
                redPlayer = new HumanPlayer(HexGame.RED);
                bluePlayer = new HumanPlayer(HexGame.BLUE);

                frame.getUndoButton().setEnabled(false);
                break;

            case AI_VS_AI:
                redPlayer = new AIPlayer(HexGame.RED, ai, config.depth);
                bluePlayer = new AIPlayer(HexGame.BLUE, ai, config.depth);

                frame.getUndoButton().setEnabled(false);
                break;
        }

        panel.setWinningPath(null);
        syncUI();
        panel.setThinking(false);

        // Sử dụng config.timerSeconds thay vì số cố định
        game.setRedTimeLeft(config.timerSeconds);
        game.setBlueTimeLeft(config.timerSeconds);

        if (config.timerMode == GameTimer.Mode.NONE) {
            frame.setTimerValues(0, 0);
        } else {
            frame.setTimerValues(config.timerSeconds, config.timerSeconds);
        }

        startCountdown();
        nextTurn();
    }

    private void handleClick(int r, int c) {
        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        if (!current.isHuman()) {
            return;
        }

        if (!game.isEmpty(r, c)) {
            JOptionPane.showMessageDialog(frame, "Ô đã được đặt", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        game.place(r, c, current.getColor());
        syncUI();

        if (isGameOver()) {
            return;
        }

        nextTurn();
    }

    private void nextTurn() {
        if (isGameOver()) {
            return;
        }

        // Nếu là chế độ tính giờ mỗi nước, ta phải reset quỹ thời gian của 2 bên về ban
        // đầu
        if (config.timerMode == GameTimer.Mode.PER_MOVE) {
            game.setRedTimeLeft(config.timerSeconds);
            game.setBlueTimeLeft(config.timerSeconds);
            frame.setTimerValues(config.timerSeconds, config.timerSeconds);
        }

        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        panel.setThinking(!current.isHuman());

        if (!current.isHuman()) {
            new Thread(() -> {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ignored) {
                }

                // Tránh trường hợp người chơi vừa ấn Undo làm rỗng bàn cờ nhưng AI vẫn tính
                // toán
                if (game.getCurrent() != current.getColor()) {
                    return;
                }

                int[] move = current.chooseMove(game);

                SwingUtilities.invokeLater(() -> {
                    // Kiểm tra lại lượt trong luồng UI phòng trường hợp Undo diễn ra cùng lúc
                    if (game.getCurrent() != current.getColor()) {
                        return;
                    }

                    if (move != null) {
                        game.place(move[0], move[1], current.getColor());
                        syncUI();

                        if (isGameOver()) {
                            return;
                        }

                        nextTurn();
                    } else {
                        panel.setThinking(false);
                        syncUI();

                        JOptionPane.showMessageDialog(
                                frame,
                                "Không còn nước đi hợp lệ!",
                                "Kết thúc ván",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
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

    private boolean isGameOverLogicCheck() {
        return game != null && game.checkWinner() != HexGame.EMPTY;
    }

    private boolean isGameOver() {
        int winner = game.checkWinner();
        if (winner != HexGame.EMPTY) {
            if (countdownTimer != null) {
                countdownTimer.stop();
            }

            panel.setWinningPath(game.getWinningPath());
            panel.repaint();

            String msg = (winner == HexGame.RED) ? "RED THẮNG!" : "BLUE THẮNG!";
            int choice = JOptionPane.showConfirmDialog(
                    frame,
                    msg + "\nBạn có muốn chơi lại cùng chế độ?",
                    "Kết thúc ván",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                initGame();
            } else {
                if (countdownTimer != null) {
                    countdownTimer.stop();
                }

                frame.dispose();
                SwingUtilities.invokeLater(HexController::new);
            }
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HexController::new);
    }
}
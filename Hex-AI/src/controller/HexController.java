package controller;

import model.*;
import view.*;
import javax.swing.*;
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

    // Đối tượng điều khiển vòng lặp đếm ngược thời gian
    private GameTimer timer;

    // Trạng thái hiển thị Dialog lưu game để điều phối luồng AI
    private volatile boolean isDialogActive = false;

    // Đối tượng cờ huỷ luồng AI đang chạy nền.
    private boolean cancelCurrentAI = false;

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
        frame.getBtnSaveQuick().addActionListener(e -> handleSaveGameQuick());
        frame.getBtnLoadQuick().addActionListener(e -> handleLoadGameQuick());

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
            if (timer != null) {
                timer.pause();
            }

            // Tiêu hủy hoàn toàn cửa sổ trận đấu cũ
            frame.dispose();

            // Kích hoạt lại luồng Menu từ đầu
            SwingUtilities.invokeLater(HexController::new);
        }
    }



    /**
     * Xử lý sự kiện Người chơi nhấn "Save Game" (Menu).
     * Mở hộp thoại chọn đường dẫn và ghi trạng thái bàn cờ ra file.
     *
     */
    private void handleSave() {
        if (game == null) return;
        // Tạm dừng đồng hồ khi hộp thoại đang mở
        boolean wasRunning = (countdownTimer != null && countdownTimer.isRunning());
        if (wasRunning) countdownTimer.stop();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu ván đấu");
        chooser.setSelectedFile(new File(""));
        // Thêm bộ lọc để người dùng chỉ thấy file .txt
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Text Files (*.txt)", "txt")
        );
        int result = chooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            if (selectedFile != null) {
                // Tự động gắn .txt nếu người dùng không nhập đuôi
                String path = selectedFile.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".txt")) {
                    path += ".txt";
                }
                try {
                    GameSaver.saveGame(game, path);
                    JOptionPane.showMessageDialog(
                            frame,
                            "Lưu thành công!\nFile: " + path,
                            "Save Game",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Lưu lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        if (game == null)
            return;

        isDialogActive = true;
        if (timer != null) {
            timer.pause();
        }

        try {
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
        } finally {
            isDialogActive = false;
            if (timer != null) {
                Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
                if (!current.isHuman()) {
                    timer.resume();
                }
            }
        }
        // Khởi động lại đồng hồ sau khi hộp thoại đóng
        if (wasRunning) countdownTimer.start();
    }

    /**
     * Lưu trực tiếp vào file cố định "hex_save.dat" (Nút Quick Save)
     */
    private void handleSaveGameQuick() {
        if (game == null) return;

        // Tạm dừng đồng hồ khi hiển thị hộp thoại thông báo lưu game
        boolean wasRunning = (countdownTimer != null && countdownTimer.isRunning());
        if (wasRunning) countdownTimer.stop();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_NAME))) {
            oos.writeObject(game);
            JOptionPane.showMessageDialog(frame, "Đã lưu ván game hiện tại thành công!", "Lưu Game",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Lỗi hệ thống khi lưu file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Khởi động lại đồng hồ sau khi hộp thoại đóng
            if (wasRunning) countdownTimer.start();
        isDialogActive = true;
        if (timer != null) {
            timer.pause();
        }

        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_NAME))) {
                oos.writeObject(game);
                JOptionPane.showMessageDialog(frame, "Đã lưu ván game hiện tại thành công!", "Lưu Game", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Lỗi hệ thống khi lưu file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            isDialogActive = false;
            if (timer != null) {
                Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
                if (!current.isHuman()) {
                    timer.resume();
                }
            }
        }
    }

    /**
     * Xử lý sự kiện Người chơi nhấn "Load Game" (Menu).
     * Mở hộp thoại chọn file, đọc và khôi phục lại ván đấu.
     */
    private void handleLoad() {
        // Tạm dừng đồng hồ khi hộp thoại đang mở
        boolean wasRunning = (countdownTimer != null && countdownTimer.isRunning());
        if (wasRunning) countdownTimer.stop();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Tải ván đấu");
        // Thêm bộ lọc để người dùng chỉ chọn được file .txt
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Text Files (*.txt)", "txt")
        );
        int result = chooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            // Người dùng hủy → Tiếp tục ván đấu hiện tại
            if (wasRunning) countdownTimer.start();
            return;
        }

        // Kiểm tra null trước khi truy cập file
        File selectedFile = chooser.getSelectedFile();
        if (selectedFile == null) {
            if (wasRunning) countdownTimer.start();
            return;
        }

        try {
            HexGame loadedGame = GameSaver.loadGame(selectedFile.getAbsolutePath());
            // Chỉ nạp được file có cùng kích thước bàn cờ với cấu hình hiện tại để tránh lỗi giao diện
            if (loadedGame.getSize() != config.size) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Kích thước bàn cờ không khớp!\n(File: " + loadedGame.getSize()
                                + " × " + loadedGame.getSize()
                                + ", Hiện tại: " + config.size + " × " + config.size + ")",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                // Tải thất bại → Tiếp tục ván đấu hiện tại
                if (wasRunning) countdownTimer.start();
                return;
            }

            // Tải hợp lệ -> Tiến hành hủy luồng AI cũ của ván đấu hiện tại trước khi ghi đè
            cancelCurrentAI = true;

            // Khôi phục toàn bộ trạng thái game ──────────────────────────────
            game = loadedGame;
            panel.setBoard(game.getBoard());
            panel.setLastMove(-1, -1);
            panel.setWinningPath(new ArrayList<>());
            panel.setThinking(false);
                // Đồng bộ thời gian và danh sách nước đi
                syncUI();
                
                // Đồng bộ thời gian vào bộ đếm GameTimer
                if (timer != null) {
                    timer.setRemainingSeconds(game.getRedTimeLeft(), game.getBlueTimeLeft());
                } else {
                    frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());
                }

            // Đồng bộ bàn cờ lên giao diện
            syncUI();

            // Cập nhật thanh thời gian từ dữ liệu đã khôi phục
            frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());

            // Hiện thông báo thành công trước khi kích hoạt lại lượt chơi
            JOptionPane.showMessageDialog(frame, "Tải ván đấu thành công!", "Load Game", JOptionPane.INFORMATION_MESSAGE);

            // Bật lại trạng thái bình thường, kích hoạt đồng hồ và lượt chơi của ván mới
            cancelCurrentAI = false;
            startCountdown();
            nextTurn();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Tải lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            // Tải thất bại → Tiếp tục ván đấu hiện tại
            if (wasRunning) countdownTimer.start();
        }
    }


    /**
     * Tải trực tiếp dữ liệu từ file "hex_save.dat" có sẵn (Nút Quick Load)
     */
    private void handleLoadGameQuick() {
        File file = new File(SAVE_FILE_NAME);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(frame, "Không tìm thấy dữ liệu ván đấu nào được lưu trước đó!", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Tạm dừng đồng hồ khi đang đọc dữ liệu
        boolean wasRunning = (countdownTimer != null && countdownTimer.isRunning());
        if (wasRunning) countdownTimer.stop();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            HexGame loadedGame = (HexGame) ois.readObject();
            // Chỉ load được file có cùng kích thước bàn cờ với cấu hình hiện tại
            if (loadedGame.getSize() != config.size) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Kích thước bàn cờ không khớp!\n(File: " + loadedGame.getSize()
                                + " × " + loadedGame.getSize()
                                + ", Hiện tại: " + config.size + " × " + config.size + ")",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                // Tải thất bại → Tiếp tục ván đấu hiện tại
                if (wasRunning) countdownTimer.start();
                return;
            }
            // Tải hợp lệ -> Tiến hành hủy luồng AI cũ của ván đấu hiện tại trước khi ghi đè
            cancelCurrentAI = true;
            // Khôi phục trạng thái game
            this.game = loadedGame;
            panel.setBoard(game.getBoard());
            panel.setLastMove(-1, -1);
            panel.setWinningPath(new ArrayList<>());
            panel.setThinking(false);
            syncUI();
            // Đẩy dữ liệu thời gian từ game vừa load xuống Frame
            frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());
            JOptionPane.showMessageDialog(frame, "Đã khôi phục lại ván game thành công!", "Tải Game", JOptionPane.INFORMATION_MESSAGE);
            // Bật lại trạng thái bình thường, kích hoạt đồng hồ và lượt chơi của ván mới
            cancelCurrentAI = false;
            startCountdown();

            // Đồng bộ thời gian vào bộ đếm GameTimer
            if (timer != null) {
                timer.setRemainingSeconds(game.getRedTimeLeft(), game.getBlueTimeLeft());
            } else {
                frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());
            }

            nextTurn();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Lỗi khi đọc dữ liệu file lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            // Tải thất bại → Tiếp tục ván đấu hiện tại
            if (wasRunning) countdownTimer.start();
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

        timer = new GameTimer(config.timerMode, config.timerSeconds);
        timer.setListener(new GameTimer.Listener() {
            @Override
            public void onTick(int redSeconds, int blueSeconds) {
                game.setRedTimeLeft(redSeconds);
                game.setBlueTimeLeft(blueSeconds);
                frame.setTimerValues(redSeconds, blueSeconds);
            }

            @Override
            public void onTimeout(int player) {
                panel.setThinking(true);
                String msg = (player == HexGame.RED) ? 
                    "Người chơi ĐỎ đã hết thời gian! XANH giành chiến thắng." : 
                    "Người chơi XANH đã hết thời gian! ĐỎ giành chiến thắng.";
                
                int choice = JOptionPane.showConfirmDialog(
                        frame,
                        msg + "\nBạn có muốn chơi lại cùng chế độ?",
                        "Hết giờ",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    initGame();
                } else {
                    frame.dispose();
                    SwingUtilities.invokeLater(HexController::new);
                }
            }
        });
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

    /**
     * Điều phối lượt chơi tiếp theo.
     * Nếu là AI: chạy tính toán trên Thread nền để không đóng băng UI.
     */
    private void nextTurn() {
        if (isGameOver()) {
            return;
        }
        // Nếu là chế độ tính giờ mỗi nước, reset quỹ thời gian của 2 bên về ban đầu
        if (config.timerMode == GameTimer.Mode.PER_MOVE) {
            game.setRedTimeLeft(config.timerSeconds);
            game.setBlueTimeLeft(config.timerSeconds);
            frame.setTimerValues(config.timerSeconds, config.timerSeconds);
        }

        timer.switchTo(game.getCurrent());

        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        panel.setThinking(!current.isHuman());
        if (!current.isHuman()) {
            final boolean[] localCancel = {false};

            // Lưu tham chiếu của game tại thời điểm bắt đầu Thread AI
            HexGame gameAtStart = this.game;

            new Thread(() -> {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ignored) {}

                // Kiểm tra cờ hủy HOẶC đối tượng game đã bị thay đổi (do Load game khác) TRƯỚC KHI tính Minimax
                if (cancelCurrentAI || game != gameAtStart) {
                    localCancel[0] = true;
                // Tránh trường hợp người chơi vừa ấn Undo làm rỗng bàn cờ nhưng AI vẫn tính
                // toán
                // Chờ nếu đang hiển thị Dialog lưu game
                while (isDialogActive) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }

                // Tránh trường hợp người chơi vừa ấn Undo làm rỗng bàn cờ nhưng AI vẫn tính toán
                if (game.getCurrent() != current.getColor()) {
                    return;
                }

                // kiểm tra lượt chơi hợp lệ (cho trường hợp Undo)
                if (game.getCurrent() != current.getColor()) return;

                int[] move = current.chooseMove(game);

                // Chờ lại lần nữa trước khi cập nhật UI nếu trong quá trình tính toán người chơi bấm Save
                while (isDialogActive) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    // Kiểm tra cờ hủy lần cuối trên EDT trước khi đặt quân
                    // Ngăn AI cũ gọi game.place() lên game mới sau khi Load
                    if (cancelCurrentAI || localCancel[0] || game != gameAtStart) return;

                    // kiểm tra lượt chơi còn đúng không (cho Undo)
                    if (game.getCurrent() != current.getColor()) return;

                    if (move != null) {
                        game.place(move[0], move[1], current.getColor());
                        syncUI();
                        if (isGameOver()) return;
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
            if (timer != null) {
                timer.pause();
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
                if (timer != null) {
                    timer.pause();
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
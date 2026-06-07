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
 * - Triển khai GameTimer xử lý đếm ngược thời gian theo thời gian thực (Tổng giờ hoặc Mỗi nước).
 * - Xử lý Lưu/Tải trực tiếp vào file cố định "hex_save.dat" không qua JFileChooser.
 * - Sửa toàn bộ lỗi trùng lặp phương thức, sai dấu ngoặc và sạch bóng lỗi đỏ.
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

    // [NgocTrinh] UC-01: Khởi tạo ván mới - Gọi SetupDialog để hiển thị màn hình thiết lập và nhận cấu hình (Config) từ người chơi
    public HexController() {
        config = new SetupDialog(null).showDialog();
        if (config == null) {
            System.exit(0);
        }

        // [NgocTrinh] UC-01: Dựng khung giao diện HexFrame và HexPanel dựa trên kích thước bàn cờ đã chọn
        frame = new HexFrame(config.size);
        panel = frame.getPanel();

        panel.addCellClickListener(this::handleClick);
        // [NgocTrinh] UC-08: Hoàn nước - Đăng ký sự kiện lắng nghe cho nút Undo
        frame.getUndoButton().addActionListener(e -> handleUndo());

        // Đăng ký sự kiện cho menu Save và Load thường (qua JFileChooser)
        frame.getSaveMenuItem().addActionListener(e -> handleSave());
        frame.getLoadMenuItem().addActionListener(e -> handleLoad());

        // [Tran05] Đăng ký sự kiện lắng nghe cho bộ đôi nút Lưu và Tải game trực tiếp (Quick Save/Load)
        frame.getBtnSaveQuick().addActionListener(e -> handleSaveGameQuick());
        frame.getBtnLoadQuick().addActionListener(e -> handleLoadGameQuick());

        // [Tran05] Đăng ký sự kiện cho nút Quay về Menu để thoát ván đấu an toàn
        frame.getBtnBackToMenu().addActionListener(e -> handleBackToMenu());

        // [NgocTrinh] UC-01: Gọi phương thức điều phối để bắt đầu tiến trình sinh ván đấu mới
        initGame();
    }

    /**
     * [Tran05] Xử lý quay về Menu chính khi đang trong ván đấu (dừng đồng hồ, hủy cửa sổ).
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

            // [NgocTrinh] UC-01: Khởi động lại HexController từ đầu, mở lại SetupDialog
            SwingUtilities.invokeLater(HexController::new);
        }
    }

    /**
     * Xử lý sự kiện Người chơi nhấn "Save Game" (Menu).
     * Mở hộp thoại chọn đường dẫn và ghi trạng thái bàn cờ ra file text.
     * [Tran05] UC-07: SF1.2 - HexFrame tiếp nhận sự kiện lưu game qua Menu.
     */
    private void handleSave() {
        if (game == null) return;

        isDialogActive = true;
        if (timer != null) {
            timer.pause();
        }

        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Lưu ván đấu");
            chooser.setSelectedFile(new File("savegame.txt"));
            chooser.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("Text Files (*.txt)", "txt")
            );

            int result = chooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                if (selectedFile != null) {
                    String path = selectedFile.getAbsolutePath();
                    if (!path.toLowerCase().endsWith(".txt")) {
                        path += ".txt";
                    }
                    try {
                        // [Tran05] UC-07: SF1.3, SF1.4, SF1.5 - Ghi trạng thái game vào file văn bản
                        GameSaver.saveGame(game, path);
                        // [Tran05] UC-07: SF1.8 - Hiển thị Pop-up thông báo lưu thành công
                        JOptionPane.showMessageDialog(frame, "Lưu thành công!\nFile: " + path, "Save Game", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        // [Tran05] UC-07: AF1 - Lỗi Ghi File
                        JOptionPane.showMessageDialog(frame, "Lưu lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } finally {
            isDialogActive = false;
            if (timer != null) {
                timer.resume();
            }
        }
    }

    /**
     * [Tran05] Lưu trực tiếp trạng thái game vào file nhị phân cố định "hex_save.dat" (Quick Save)
     * UC-07: SF1.2 - HexFrame tiếp nhận sự kiện, gọi hàm và chuyển tiếp đến HexController.
     */
    private void handleSaveGameQuick() {
        // UC-07: Pre-conditions - Trò chơi đang diễn ra, chưa có người chiến thắng
        if (game == null || isGameOverLogicCheck()) return;

        isDialogActive = true;
        if (timer != null) {
            timer.pause();
        }

        try {
            // UC-07: SF1.3, SF1.4, SF1.5 - HexController yêu cầu trạng thái và ghi đè an toàn vào hex_save.dat
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_NAME))) {
                oos.writeObject(game);
                // UC-07: SF1.7 & SF1.8 - Gửi tín hiệu và hiển thị thông báo "Đã lưu ván đấu!"
                JOptionPane.showMessageDialog(frame, "Đã lưu ván game hiện tại thành công!", "Lưu Game", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                // UC-07: AF1 - Lỗi Ghi File (AF1.2: Bắt exception, AF1.3: Cảnh báo lỗi)
                JOptionPane.showMessageDialog(frame, "Lỗi hệ thống khi lưu file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            isDialogActive = false;
            if (timer != null) {
                timer.resume();
            }
            // AF1.4: Ván đấu hiện tại giữ nguyên trạng thái
        }
    }

    /**
     * Xử lý sự kiện Người chơi nhấn "Load Game" (Menu).
     * Mở hộp thoại chọn file text, đọc và khôi phục lại ván đấu.
     * [Tran05] UC-07: SF2.2 - HexFrame tiếp nhận sự kiện tải game qua Menu.
     */
    private void handleLoad() {
        if (timer != null) timer.pause();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Tải ván đấu");
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Text Files (*.txt)", "txt")
        );
        int result = chooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            if (timer != null) timer.resume();
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        if (selectedFile == null) {
            if (timer != null) timer.resume();
            return;
        }

        try {
            // [Tran05] UC-07: SF2.3, SF2.4 - Đọc dữ liệu từ file văn bản
            HexGame loadedGame = GameSaver.loadGame(selectedFile.getAbsolutePath());
            if (loadedGame.getSize() != config.size) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Kích thước bàn cờ không khớp!\n(File: " + loadedGame.getSize()
                                + " × " + loadedGame.getSize()
                                + ", Hiện tại: " + config.size + " × " + config.size + ")",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                if (timer != null) timer.resume();
                return;
            }

            cancelCurrentAI = true;

            // [Tran05] UC-07: SF2.5 - Ghi đè dữ liệu vào Model
            this.game = loadedGame;
            panel.setBoard(game.getBoard());
            panel.setLastMove(-1, -1);
            panel.setWinningPath(new ArrayList<>());
            panel.setThinking(false);

            // [Tran05] UC-07: SF2.6 - Đồng bộ giao diện
            syncUI();

            if (timer != null) {
                // [Tran05] UC-07: SF2.7 - setTimerValues
                timer.setRemainingSeconds(game.getRedTimeLeft(), game.getBlueTimeLeft());
                timer.resume();
            } else {
                frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());
            }

            // [Tran05] UC-07: SF2.11 - Thông báo khôi phục thành công
            JOptionPane.showMessageDialog(frame, "Tải ván đấu thành công!", "Load Game", JOptionPane.INFORMATION_MESSAGE);

            cancelCurrentAI = false;
            nextTurn();
        } catch (IOException ex) {
            // [Tran05] UC-07: AF2 - Lỗi Đọc File
            JOptionPane.showMessageDialog(frame, "Tải lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            if (timer != null) timer.resume();
        }
    }

    /**
     * [Tran05] Tải trực tiếp dữ liệu trạng thái game từ file nhị phân "hex_save.dat" (Quick Load)
     * UC-07: SF2.2 - HexFrame tiếp nhận sự kiện, gọi hàm và chuyển tiếp sang HexController.
     */
    private void handleLoadGameQuick() {
        // UC-07: SF2.3 - Hệ thống tìm file hex_save.dat
        File file = new File(SAVE_FILE_NAME);
        if (!file.exists()) {
            // UC-07: AF2 - Lỗi Đọc File (AF2.1: Không tìm thấy file, AF2.3: Hiển thị cảnh báo)
            JOptionPane.showMessageDialog(frame, "Không tìm thấy dữ liệu ván đấu nào được lưu trước đó!", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (timer != null) timer.pause();

        // UC-07: SF2.3 - Dùng ObjectInputStream đọc dữ liệu từ file
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // UC-07: SF2.4 - Đọc thành công, trả về đối tượng loadedGame
            HexGame loadedGame = (HexGame) ois.readObject();
            if (loadedGame.getSize() != config.size) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Kích thước bàn cờ không khớp!\n(File: " + loadedGame.getSize()
                                + " × " + loadedGame.getSize()
                                + ", Hiện tại: " + config.size + " × " + config.size + ")",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                if (timer != null) timer.resume();
                return;
            }

            cancelCurrentAI = true;

            // UC-07: SF2.5 - Ghi đè dữ liệu vừa đọc vào Model hiện tại (this.game = loadedGame)
            this.game = loadedGame;
            panel.setBoard(game.getBoard());
            panel.setLastMove(-1, -1);
            panel.setWinningPath(new ArrayList<>());
            panel.setThinking(false);

            // UC-07: SF2.6 - Gọi hàm nội bộ syncUI() để bắt đầu tiến trình đồng bộ giao diện
            syncUI();
            
            // UC-07: SF2.7 - setTimerValues(thời_gian_đã_lưu)
            frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());

            // UC-07: SF2.11 - Phát tín hiệu hiển thị thông báo "Khôi phục thành công!"
            JOptionPane.showMessageDialog(frame, "Đã khôi phục lại ván game thành công!", "Tải Game", JOptionPane.INFORMATION_MESSAGE);

            cancelCurrentAI = false;

            if (timer != null) {
                timer.setRemainingSeconds(game.getRedTimeLeft(), game.getBlueTimeLeft());
                timer.resume();
            } else {
                frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());
            }

            // UC-07: SF2.12 - Tiếp tục ván đấu từ thời điểm đã lưu
            nextTurn();
        } catch (Exception ex) {
            // UC-07: AF2 - Lỗi Đọc File (AF2.2: Bắt exception và hủy bỏ, AF2.3: Cảnh báo lỗi)
            JOptionPane.showMessageDialog(frame, "Lỗi khi đọc dữ liệu file lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            if (timer != null) timer.resume();
            // AF2.4: Ván đấu hiện tại giữ nguyên trạng thái
        }
    }

    /**
     * [Tran05] Xử lý sự kiện khi ấn nút Undo: Rút nước đi, cập nhật lại thời gian và vẽ lại UI.
     */
    /**
     * [NgocTrinh] UC-08: Xử lý sự kiện Hoàn nước (Undo). Rút lại nước đi của cả AI và người chơi.
     */
    public void handleUndo() {
        if (game == null)
            return;

        if (config.mode == SetupDialog.Mode.HUMAN_VS_AI) {
            Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;

            if (!current.isHuman()) {
                return;
            }

            // [NgocTrinh] UC-08: Bắt lỗi logic - Kiểm tra Stack phải có ít nhất 2 nước đi mới cho phép hoàn tác
            if (game.getMoveHistory().size() < 2) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Không còn đủ nước đi để hoàn tác!",
                        "Undo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // [Tran05] UC-09 Trigger (c) - Người chơi nhấn Undo

            game.undo();
            game.undo();

            // [NgocTrinh] UC-08: Phục hồi lại thời gian cho người chơi ở lượt hiện tại nếu chơi chế độ PER_MOVE
            resetCurrentTurnTimerAfterUndo();

            panel.setThinking(false);
            panel.setWinningPath(null);

            // [Tran05] UC-09: SF1.3 - Tiến hành đồng bộ giao diện sau khi thay đổi trạng thái
            syncUI();
            frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());
        }
    }

    // [Tran05] Đặt lại thời gian lượt đi sau khi Undo nếu đang chơi chế độ PER_MOVE
    private void resetCurrentTurnTimerAfterUndo() {
        if (config.timerMode != GameTimer.Mode.PER_MOVE) {
            return;
        }

        if (game.getCurrent() == HexGame.RED) {
            game.setRedTimeLeft(config.timerSeconds);
        } else if (game.getCurrent() == HexGame.BLUE) {
            game.setBlueTimeLeft(config.timerSeconds);
        }

        if (timer != null) {
            timer.setRemainingSeconds(game.getRedTimeLeft(), game.getBlueTimeLeft());
        }
    }

    private void initGame() {
        // [NgocTrinh] UC-01: Tạo đối tượng Model HexGame mới hoàn toàn với kích thước n x n
        game = new HexGame(config.size);

        // [NgocTrinh] UC-01: Phân loại Người chơi/AI và Bật/Tắt tính năng Undo tùy theo chế độ chơi (Mode)
        switch (config.mode) {
            case HUMAN_VS_AI:
                redPlayer = new HumanPlayer(HexGame.RED);
                bluePlayer = new AIPlayer(HexGame.BLUE, ai, config.depth);
                // [NgocTrinh] UC-08: Chức năng Hoàn nước chỉ khả dụng khi người đấu với máy
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

        // [Tran05] Khởi tạo GameTimer và xử lý callback cập nhật UI / kết thúc trận đấu do hết giờ.
        timer = new GameTimer(config.timerMode, config.timerSeconds);
        timer.setListener(new GameTimer.Listener() {
            @Override
            public void onTick(int redSeconds, int blueSeconds) {
                // [Tran05] UC-09 Trigger (e) / SF1.6 - Mỗi giây Timer hệ thống giảm, đọc giá trị thời gian còn lại của hai bên và cập nhật lên nhãn đếm ngược
                game.setRedTimeLeft(redSeconds);
                game.setBlueTimeLeft(blueSeconds);
                frame.setTimerValues(redSeconds, blueSeconds);
            }

            @Override
            public void onTimeout(int player) {
                // [Tran05] UC-09: AF1.1 - Nhận cờ kết thúc ván đấu do hết giờ
                // [Tran05] UC-09: AF1.7 - Khóa toàn bộ sự kiện click chuột trên HexPanel để không đặt thêm quân
                panel.setThinking(true);
                if (timer != null) timer.pause();

                String msg = (player == HexGame.RED) ?
                        "Người chơi ĐỎ đã hết thời gian! XANH giành chiến thắng." :
                        "Người chơi XANH đã hết thời gian! ĐỎ giành chiến thắng.";

                // [Tran05] UC-09: AF1.6 - Kích hoạt Pop-up / Dialog thông báo kết quả ván đấu
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

    /**
     * [Tran05] UC-09 Trigger (a) - Người chơi đặt quân lên bàn cờ.
     */
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
        
        // [Tran05] UC-09: SF1.3 - Gọi syncUI() đồng bộ giao diện
        syncUI();

        // [Tran05] UC-09: SF1.2 - Kiểm tra cờ trạng thái kết thúc ván đấu
        if (isGameOver()) {
            return;
        }

        nextTurn();
    }

    private void nextTurn() {
        if (isGameOver()) {
            return;
        }

        if (config.timerMode == GameTimer.Mode.PER_MOVE) {
            game.setRedTimeLeft(config.timerSeconds);
            game.setBlueTimeLeft(config.timerSeconds);
            frame.setTimerValues(config.timerSeconds, config.timerSeconds);
            if (timer != null) {
                timer.setRemainingSeconds(config.timerSeconds, config.timerSeconds);
            }
        }

        if (timer != null) {
            timer.switchTo(game.getCurrent());
        }

        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        panel.setThinking(!current.isHuman());

        if (!current.isHuman()) {
            final boolean[] localCancel = {false};
            HexGame gameAtStart = this.game;

            new Thread(() -> {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ignored) {}

                if (cancelCurrentAI || game != gameAtStart) {
                    localCancel[0] = true;
                    return;
                }

                while (isDialogActive) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                }

                if (game.getCurrent() != current.getColor()) {
                    return;
                }

                int[] move = current.chooseMove(game);

                while (isDialogActive) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                }

                SwingUtilities.invokeLater(() -> {
                    if (cancelCurrentAI || localCancel[0] || game != gameAtStart) return;
                    if (game.getCurrent() != current.getColor()) return;

                    if (move != null) {
                        // [Tran05] UC-09 Trigger (b) - AI hoàn thành tính toán và chọn nước đi
                        game.place(move[0], move[1], current.getColor());
                        
                        // [Tran05] UC-09: SF1.3 - Đồng bộ giao diện
                        syncUI();
                        
                        // [Tran05] UC-09: SF1.2 - Kiểm tra cờ trạng thái kết thúc
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
     * [Tran05] UC-09: SF1.3 - Tiến trình đồng bộ giao diện.
     * [Tran05] UC-07: SF2.6 - Gọi hàm nội bộ syncUI để bắt đầu tiến trình đồng bộ giao diện sau khi tải game.
     */
    private void syncUI() {
        // [Tran05] UC-07: SF2.9 & UC-09: SF1.4 & SF1.5 - setBoard và vẽ lại các quân cờ theo ma trận bàn cờ mới nhất
        panel.setBoard(game.getBoard());

        // [NgocTrinh] UC-08: Lưu và hiển thị lịch sử - Truy xuất ngăn xếp lịch sử từ Model
        Stack<int[]> history = game.getMoveHistory();
        StringBuilder sb = new StringBuilder();
        int turnCount = 1;

        // [Tran05] UC-07: SF2.8 & UC-09: SF1.7 - Biên dịch Stack lịch sử thành chuỗi văn bản và hiển thị lại lên HexFrame
        // [NgocTrinh] UC-08: Duyệt qua lịch sử nước đi, dịch tọa độ thành chuỗi văn bản và in ra JTextArea bên Sidebar
        for (int i = 0; i < history.size(); i++) {
            int[] move = history.get(i);
            String playerStr = (i % 2 == 0) ? "ĐỎ" : "XANH";
            sb.append(String.format("Lượt %d: %s (%d, %d)\n", turnCount++, playerStr, move[0], move[1]));
        }
        frame.updateHistoryText(sb.toString());

        // [Tran05] Xác định nước đi cuối cùng để HexPanel vẽ hiệu ứng "ô vừa đánh" (Highlight Last Move)
        // [NgocTrinh] UC-08: Lấy phần tử trên cùng (peek) của Stack để ra lệnh cho Panel vẽ Highlight "ô vừa đánh"
        if (!history.isEmpty()) {
            int[] last = history.peek();
            panel.setLastMove(last[0], last[1]);
        } else {
            panel.setLastMove(-1, -1);
        }

        // [Tran05] UC-07: SF2.10 & UC-09: SF1.3 (tiếp tục) - Gọi repaint() yêu cầu HexPanel vẽ lại toàn bộ bàn cờ
        panel.repaint();
    }

    private boolean isGameOverLogicCheck() {
        return game != null && game.checkWinner() != HexGame.EMPTY;
    }

    /**
     * [Tran05] UC-09: SF1.2 & AF1.1 - Kiểm tra cờ trạng thái kết thúc ván đấu.
     */
    private boolean isGameOver() {
        int winner = game.checkWinner();
        if (winner != HexGame.EMPTY) {
            // [Tran05] UC-09: AF1.1 - Nhận cờ kết thúc ván đấu (một bên đã kết nối chuỗi chiến thắng)
            if (timer != null) {
                timer.pause();
            }

            // [Tran05] UC-09: AF1.2 & AF1.3 - Truy xuất ma trận cuối cùng và repaint Highlight đường chiến thắng
            panel.setWinningPath(game.getWinningPath());
            panel.repaint();

            // [Tran05] UC-09: AF1.4 - Cập nhật lần cuối thời gian còn lại lên nhãn đếm ngược (đã đồng bộ qua pause/onTick)
            // [Tran05] UC-09: AF1.5 - Cập nhật lịch sử nước đi đầy đủ qua syncUI trước đó

            // [Tran05] UC-09: AF1.6 - Kích hoạt Pop-up / Dialog thông báo kết quả ván đấu
            String msg = (winner == HexGame.RED) ? "RED THẮNG!" : "BLUE THẮNG!";
            
            // [Tran05] UC-09: AF1.7 - Khóa toàn bộ sự kiện click chuột trên HexPanel (được kiểm soát gián tiếp vì không gọi nextTurn và kết thúc game)
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
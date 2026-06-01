package controller;

import model.*;
import view.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Stack;

/**
 * HexController – Bộ điều khiển trung tâm liên kết dữ liệu Model và View.
 * (CẬP NHẬT MỚI):
 * - Triển khai javax.swing.Timer xử lý đếm ngược thời gian theo thời gian thực.
 * - Xử lý Lưu/Tải trực tiếp vào file cố định "hex_save.dat" không qua JFileChooser.
 * - Cập nhật đồng bộ nhãn hiển thị thời gian lên UI mỗi giây hoặc khi đổi trạng thái game.
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
        if (config == null) System.exit(0);

        frame = new HexFrame(config.size);
        panel = frame.getPanel();

        panel.addCellClickListener(this::handleClick);
        frame.getUndoButton().addActionListener(e -> handleUndo());

        // Đăng ký sự kiện lắng nghe cho bộ đôi nút Lưu và Tải game trực tiếp
        frame.getBtnSave().addActionListener(e -> handleSaveGame());
        frame.getBtnLoad().addActionListener(e -> handleLoadGame());

        initGame();
    }

    /**
     * Khởi động luồng đếm ngược thời gian cho người chơi hiện tại
     */
    private void startCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
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

            // Gọi hàm public của frame theo đúng yêu cầu đề bài thiết kế UI cho người 2
            frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());

            if (game.getRedTimeLeft() <= 0) {
                countdownTimer.stop();
                panel.setThinking(true);
                JOptionPane.showMessageDialog(frame, "Người chơi ĐỎ đã hết thời gian! XANH giành chiến thắng.", "Hết giờ", JOptionPane.INFORMATION_MESSAGE);
            } else if (game.getBlueTimeLeft() <= 0) {
                countdownTimer.stop();
                panel.setThinking(true);
                JOptionPane.showMessageDialog(frame, "Người chơi XANH đã hết thời gian! ĐỎ giành chiến thắng.", "Hết giờ", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        countdownTimer.start();
    }

    /**
     * Lưu trực tiếp vào file cố định "hex_save.dat"
     */
    private void handleSaveGame() {
        if (game == null) return;

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_NAME))) {
            oos.writeObject(game);
            JOptionPane.showMessageDialog(frame, "Đã lưu ván game hiện tại thành công!", "Lưu Game", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Lỗi hệ thống khi lưu file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Tải trực tiếp dữ liệu từ file "hex_save.dat" có sẵn
     */
    private void handleLoadGame() {
        File file = new File(SAVE_FILE_NAME);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(frame, "Không tìm thấy dữ liệu ván đấu nào được lưu trước đó!", "Lỗi", JOptionPane.WARNING_MESSAGE);
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

            JOptionPane.showMessageDialog(frame, "Đã khôi phục lại ván game thành công!", "Tải Game", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Lỗi khi đọc dữ liệu file lưu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý sự kiện khi ấn nút Undo.
     */
    public void handleUndo() {
        if (game == null) return;

        if (config.mode == SetupDialog.Mode.HUMAN_VS_AI) {
            Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
            if (!current.isHuman()) return;

            game.undo();
            game.undo();

            syncUI();
            frame.setTimerValues(game.getRedTimeLeft(), game.getBlueTimeLeft());
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

        frame.setTimerValues(600, 600);
        startCountdown();

        nextTurn();
    }

    private void handleClick(int r, int c) {
        Player current = (game.getCurrent() == HexGame.RED) ? redPlayer : bluePlayer;
        if (!current.isHuman()) return;

        if (!game.isEmpty(r, c)) return;

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
     * Đồng bộ hóa dữ liệu từ Model sang View
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
package model;

import java.io.IOException;

/**
 * GameSaver - Lớp chịu trách nhiệm lưu và tải trạng thái ván đấu.
 * [Tran05] Thiết kế và triển khai cơ chế Lưu/Tải trạng thái ván đấu (bao gồm thông tin kích thước, lượt đi, thời gian, ma trận bàn cờ và lịch sử di chuyển).
 */
public class GameSaver {

    /**
     * [Tran05] Ghi dữ liệu trạng thái game hiện tại ra file text.
     * Lưu trạng thái ván đấu hiện tại ra file.
     */
    public static void saveGame(HexGame game, String filePath) throws IOException {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filePath))) {
            int n = game.getSize();
            writer.println(n);
            writer.println(game.getCurrent());
            // Lưu thời gian (nếu cần cho các yêu cầu trước của bạn)
            writer.println(game.getRedTimeLeft());
            writer.println(game.getBlueTimeLeft());

            // Lưu ma trận bàn cờ
            int[][] board = game.getBoard();
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    writer.print(board[r][c] + " ");
                }
                writer.println();
            }

            // Lưu lịch sử nước đi để phục vụ Undo
            java.util.Stack<int[]> history = game.getMoveHistory();
            writer.println(history.size());
            for (int[] move : history) {
                writer.println(move[0] + " " + move[1]);
            }
        }
    }

    /**
     * [Tran05] Đọc và phục hồi lại trạng thái game từ file text.
     * Đọc trạng thái ván đấu từ file và tái tạo lại đối tượng HexGame.
     */
    public static HexGame loadGame(String filePath) throws IOException {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
            int n = Integer.parseInt(reader.readLine().trim());
            int current = Integer.parseInt(reader.readLine().trim());
            int redTime = Integer.parseInt(reader.readLine().trim());
            int blueTime = Integer.parseInt(reader.readLine().trim());

            HexGame game = new HexGame(n);
            game.setRedTimeLeft(redTime);
            game.setBlueTimeLeft(blueTime);

            // Đọc trực tiếp ma trận bàn cờ vào board[][]
            int[][] board = game.getBoard();
            for (int r = 0; r < n; r++) {
                String[] cells = reader.readLine().trim().split("\\s+");
                for (int c = 0; c < n; c++) {
                    board[r][c] = Integer.parseInt(cells[c]);
                }
            }
            // Push trực tiếp tọa độ vào lịch sử
            int historySize = Integer.parseInt(reader.readLine().trim());
            java.util.Stack<int[]> history = game.getMoveHistory();
            for (int i = 0; i < historySize; i++) {
                String[] parts = reader.readLine().trim().split("\\s+");
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                history.push(new int[]{r, c});
            }
            game.setCurrent(current);
            return game;
        } catch (Exception e) {
            throw new IOException("Định dạng file lưu không hợp lệ!", e);
        }
    }
}
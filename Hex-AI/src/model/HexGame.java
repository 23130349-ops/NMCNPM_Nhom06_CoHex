package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * HexGame – Mô hình dữ liệu chính của trò chơi Hex.
 * Liên quan đến các Use Case:
 * UC-01 Khởi tạo ván mới
 * UC-02 Đặt quân cờ (Màu đỏ)
 * UC-03 Kiểm tra nước đi hợp lệ
 * UC-05 Kiểm tra thắng/thua
 * Tính năng Hoàn nước (Undo) và Lịch sử đánh
 */
public class HexGame {
    public static final int EMPTY = 0;
    public static final int RED   = 1;
    public static final int BLUE  = 2;

    private final int n;
    private final int[][] board;
    private int current;
    private List<int[]> winningPathList = new ArrayList<>();

    // Stack lưu trữ lịch sử các nước đã đi (Mỗi phần tử là mảng int[] chứa {r, c})
    private Stack<int[]> moveHistory;

    /**
     * UC-01 – Khởi tạo ván mới
     */
    public HexGame(int n) {
        this.n = n;
        this.board = new int[n][n]; // tất cả ô mặc định = 0 (EMPTY)
        this.current = RED;
        this.moveHistory = new Stack<>(); // Khởi tạo Stack
    }

    /**
     * Tạo bản sao sâu (deep copy) của trạng thái bàn cờ hiện tại.
     * Dùng trong HexAI.bestMove() để duyệt cây game mà không ảnh hưởng bàn cờ gốc.
     */
    public HexGame copy() {
        HexGame copy = new HexGame(n);
        for (int i = 0; i < n; i++) {
            System.arraycopy(board[i], 0, copy.board[i], 0, n);
        }
        copy.current = this.current;
        // Copy lịch sử nước đi cho bản clone
        copy.moveHistory.addAll(this.moveHistory);
        return copy;
    }

    /** Trả về kích thước bàn cờ n×n. */
    public int getSize() {
        return n;
    }

    /** Trả về mảng bàn cờ (dùng cho giao diện và AI). */
    public int[][] getBoard() {
        return board;
    }

    /** Trả về màu của người chơi đang đến lượt (RED hoặc BLUE). */
    public int getCurrent() {
        return current;
    }

    /** Trả về danh sách lịch sử đánh. */
    public Stack<int[]> getMoveHistory() {
        return moveHistory;
    }

    // Getter để HexController lấy danh sách ô thắng truyền qua cho HexPanel vẽ màu vàng
    public List<int[]> getWinningPath() {
        return winningPathList;
    }

    /**
     * UC-03 – Kiểm tra nước đi hợp lệ
     */
    public boolean isEmpty(int r, int c) {
        return board[r][c] == EMPTY;
    }

    /**
     * UC-02 & UC-04 – Đặt quân cờ
     */
    public boolean place(int r, int c, int player) {
        if (board[r][c] != EMPTY) {
            return false;
        }
        board[r][c] = player;

        // Đẩy tọa độ nước đi vào Stack lịch sử
        moveHistory.push(new int[]{r, c});

        // Chuyển lượt sau khi đặt quân thành công
        current = (player == RED) ? BLUE : RED;
        return true;
    }

    /**
     * Tính năng Hoàn nước (Undo)
     * Lấy nước đi cuối cùng ra khỏi Stack, đặt lại ô về EMPTY và lùi lượt chơi.
     * @return true nếu undo thành công, false nếu không còn nước nào để undo.
     */
    public boolean undo() {
        if (moveHistory.isEmpty()) {
            return false; // Không có gì để Undo
        }
        int[] lastMove = moveHistory.pop();
        int r = lastMove[0];
        int c = lastMove[1];

        // Set ô đó về EMPTY
        board[r][c] = EMPTY;

        // Lùi lượt chơi (đổi lại current)
        current = (current == RED) ? BLUE : RED;

        return true;
    }

    /**
     * UC-05 – Kiểm tra thắng/thua
     */
    public int checkWinner() {
        winningPathList.clear();
        if (hasPlayerWon(RED))  return RED;
        if (hasPlayerWon(BLUE)) return BLUE;
        return EMPTY;
    }

    /**
     * DFS Kiểm tra điều kiện nối cạnh.
     */
    private boolean hasPlayerWon(int player) {
        boolean[][] visited = new boolean[n][n];
        java.util.Deque<int[]> stack = new java.util.ArrayDeque<>();
        int[][][] parent = new int[n][n][2];
        int[][] dirs = {{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};

        if (player == RED) {
            for (int c = 0; c < n; c++) {
                if (board[0][c] == RED) {
                    visited[0][c] = true;
                    parent[0][c] = new int[]{-1, -1};
                    stack.push(new int[]{0, c});
                }
            }
            while (!stack.isEmpty()) {
                int[] p = stack.pop();
                int r = p[0], c = p[1];
                if (r == n - 1) {
                    reconstructPath(parent, r, c);
                    return true;
                }
                for (int[] d : dirs) {
                    int nr = r + d[0], nc = c + d[1];
                    if (nr >= 0 && nr < n && nc >= 0 && nc < n && !visited[nr][nc] && board[nr][nc] == RED) {
                        visited[nr][nc] = true;
                        parent[nr][nc] = new int[]{r, c};
                        stack.push(new int[]{nr, nc});
                    }
                }
            }
        } else {
            for (int r = 0; r < n; r++) {
                if (board[r][0] == BLUE) {
                    visited[r][0] = true;
                    parent[r][0] = new int[]{-1, -1};
                    stack.push(new int[]{r, 0});
                }
            }
            while (!stack.isEmpty()) {
                int[] p = stack.pop();
                int r = p[0], c = p[1];
                if (c == n - 1) {
                    reconstructPath(parent, r, c);
                    return true;
                }
                for (int[] d : dirs) {
                    int nr = r + d[0], nc = c + d[1];
                    if (nr >= 0 && nr < n && nc >= 0 && nc < n && !visited[nr][nc] && board[nr][nc] == BLUE) {
                        visited[nr][nc] = true;
                        parent[nr][nc] = new int[]{r, c};
                        stack.push(new int[]{nr, nc});
                    }
                }
            }
        }
        return false;
    }

    /**
     * Hàm phụ trợ truy vết ngược: Đi lùi từ ô đích về ô xuất phát ban đầu
     * để thu thập chính xác tập hợp tọa độ các ô nằm trên chuỗi chiến thắng.
     */
    private void reconstructPath(int[][][] parent, int targetR, int targetC) {
        int currR = targetR;
        int currC = targetC;

        while (currR != -1 && currC != -1) {
            // Thêm vào vị trí số 0 để thu được chuỗi đi xuôi từ Xuất phát -> Đích
            winningPathList.add(0, new int[]{currR, currC});

            // Lấy ngược tọa độ của ô cha trước đó
            int[] p = parent[currR][currC];
            currR = p[0];
            currC = p[1];
        }
    }
}
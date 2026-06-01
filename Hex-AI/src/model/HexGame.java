package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * HexGame – Mô hình dữ liệu chính của trò chơi Hex.
 * Liên quan đến các Use Case:
 * UC-01 Khởi tạo ván mới
 * UC-02 Đặt quân cờ
 * UC-03 Kiểm tra nước đi hợp lệ
 * UC-05 Kiểm tra thắng/thua
 * Tính năng Hoàn nước (Undo), Lịch sử đánh, Save/Load và thời gian.
 */
public class HexGame implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public static final int EMPTY = 0;
    public static final int RED   = 1;
    public static final int BLUE  = 2;

    private final int n;
    private final int[][] board;
    private int current;
    private List<int[]> winningPathList = new ArrayList<>();

    // Stack lưu trữ lịch sử các nước đã đi, mỗi phần tử là {r, c}
    private Stack<int[]> moveHistory;

    // Thời gian còn lại của mỗi người chơi, tính bằng giây
    private int redTimeLeft = 600;
    private int blueTimeLeft = 600;

    /**
     * UC-01 – Khởi tạo ván mới
     */
    public HexGame(int n) {
        this.n = n;
        this.board = new int[n][n];
        this.current = RED;
        this.moveHistory = new Stack<>();
    }

    /**
     * Tạo bản sao sâu của trạng thái bàn cờ hiện tại.
     * Dùng trong HexAI.bestMove() để duyệt cây game mà không ảnh hưởng bàn cờ gốc.
     */
    public HexGame copy() {
        HexGame copy = new HexGame(n);

        for (int i = 0; i < n; i++) {
            System.arraycopy(board[i], 0, copy.board[i], 0, n);
        }

        copy.current = this.current;
        copy.moveHistory.addAll(this.moveHistory);
        copy.redTimeLeft = this.redTimeLeft;
        copy.blueTimeLeft = this.blueTimeLeft;

        return copy;
    }

    /** Trả về kích thước bàn cờ n×n. */
    public int getSize() {
        return n;
    }

    /** Trả về mảng bàn cờ. */
    public int[][] getBoard() {
        return board;
    }

    /** Trả về màu của người chơi đang đến lượt. */
    public int getCurrent() {
        return current;
    }

    /** Trả về danh sách lịch sử đánh. */
    public Stack<int[]> getMoveHistory() {
        return moveHistory;
    }

    /** Trả về danh sách ô thuộc đường chiến thắng. */
    public List<int[]> getWinningPath() {
        return winningPathList;
    }

    public int getRedTimeLeft() {
        return redTimeLeft;
    }

    public void setRedTimeLeft(int redTimeLeft) {
        this.redTimeLeft = redTimeLeft;
    }

    public int getBlueTimeLeft() {
        return blueTimeLeft;
    }

    public void setBlueTimeLeft(int blueTimeLeft) {
        this.blueTimeLeft = blueTimeLeft;
    }

    /**
     * UC-03 – Kiểm tra nước đi hợp lệ.
     */
    public boolean isEmpty(int r, int c) {
        return board[r][c] == EMPTY;
    }

    /**
     * UC-02 & UC-04 – Đặt quân cờ.
     */
    public boolean place(int r, int c, int player) {
        if (board[r][c] != EMPTY) {
            return false;
        }

        board[r][c] = player;
        moveHistory.push(new int[]{r, c});

        current = (player == RED) ? BLUE : RED;

        return true;
    }

    /**
     * Hoàn nước.
     * @return true nếu undo thành công, false nếu không còn nước nào để undo.
     */
    public boolean undo() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        int[] lastMove = moveHistory.pop();
        int r = lastMove[0];
        int c = lastMove[1];

        board[r][c] = EMPTY;

        current = (current == RED) ? BLUE : RED;

        return true;
    }

    /**
     * UC-05 – Kiểm tra thắng/thua.
     */
    public int checkWinner() {
        winningPathList.clear();

        if (hasPlayerWon(RED)) {
            return RED;
        }

        if (hasPlayerWon(BLUE)) {
            return BLUE;
        }

        return EMPTY;
    }

    /**
     * DFS kiểm tra điều kiện nối cạnh.
     * RED nối từ trên xuống dưới.
     * BLUE nối từ trái sang phải.
     */
    private boolean hasPlayerWon(int player) {
        boolean[][] visited = new boolean[n][n];
        java.util.Deque<int[]> stack = new java.util.ArrayDeque<>();
        int[][][] parent = new int[n][n][2];
        int[][] dirs = {
                {-1, 0},
                {-1, 1},
                {0, -1},
                {0, 1},
                {1, -1},
                {1, 0}
        };

        // Khởi tạo parent = {-1, -1} để tránh truy vết bị lặp vô hạn
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                parent[r][c][0] = -1;
                parent[r][c][1] = -1;
            }
        }

        if (player == RED) {
            // RED nối từ trên xuống dưới
            for (int c = 0; c < n; c++) {
                if (board[0][c] == RED) {
                    visited[0][c] = true;
                    parent[0][c][0] = -1;
                    parent[0][c][1] = -1;
                    stack.push(new int[]{0, c});
                }
            }

            while (!stack.isEmpty()) {
                int[] p = stack.pop();
                int r = p[0];
                int c = p[1];

                if (r == n - 1) {
                    reconstructPath(parent, r, c);
                    return true;
                }

                for (int[] d : dirs) {
                    int nr = r + d[0];
                    int nc = c + d[1];

                    if (nr >= 0 && nr < n &&
                            nc >= 0 && nc < n &&
                            !visited[nr][nc] &&
                            board[nr][nc] == RED) {

                        visited[nr][nc] = true;

                        // Lưu ô cha để truy vết đường thắng
                        parent[nr][nc][0] = r;
                        parent[nr][nc][1] = c;

                        stack.push(new int[]{nr, nc});
                    }
                }
            }
        } else {
            // BLUE nối từ trái sang phải
            for (int r = 0; r < n; r++) {
                if (board[r][0] == BLUE) {
                    visited[r][0] = true;
                    parent[r][0][0] = -1;
                    parent[r][0][1] = -1;
                    stack.push(new int[]{r, 0});
                }
            }

            while (!stack.isEmpty()) {
                int[] p = stack.pop();
                int r = p[0];
                int c = p[1];

                if (c == n - 1) {
                    reconstructPath(parent, r, c);
                    return true;
                }

                for (int[] d : dirs) {
                    int nr = r + d[0];
                    int nc = c + d[1];

                    if (nr >= 0 && nr < n &&
                            nc >= 0 && nc < n &&
                            !visited[nr][nc] &&
                            board[nr][nc] == BLUE) {

                        visited[nr][nc] = true;

                        // Lưu ô cha để truy vết đường thắng
                        parent[nr][nc][0] = r;
                        parent[nr][nc][1] = c;

                        stack.push(new int[]{nr, nc});
                    }
                }
            }
        }

        return false;
    }

    /**
     * Truy vết ngược từ ô đích về ô xuất phát để lấy đường thắng.
     */
    private void reconstructPath(int[][][] parent, int targetR, int targetC) {
        int currR = targetR;
        int currC = targetC;

        while (currR != -1 && currC != -1) {
            winningPathList.add(0, new int[]{currR, currC});

            int[] p = parent[currR][currC];
            currR = p[0];
            currC = p[1];
        }
    }
}
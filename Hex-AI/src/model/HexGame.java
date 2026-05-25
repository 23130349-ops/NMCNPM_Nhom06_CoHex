package model;

/**
 * HexGame – Mô hình dữ liệu chính của trò chơi Hex.
 * Liên quan đến các Use Case:
 *   UC-01 Khởi tạo ván mới
 *   UC-02 Đặt quân cờ (Màu đỏ)
 *   UC-03 Kiểm tra nước đi hợp lệ
 *   UC-05 Kiểm tra thắng/thua
 */
public class HexGame {
    public static final int EMPTY = 0;
    public static final int RED   = 1;
    public static final int BLUE  = 2;

    private final int n;
    private final int[][] board;
    private int current;

    /**
     * UC-01 – Khởi tạo ván mới (Standard flow 4.1.1, bước 2-3)
     * 2. Hệ thống khởi tạo bàn cờ Hex kích thước n×n
     * 3. Hệ thống thiết lập trạng thái ban đầu: toàn bộ ô = EMPTY
     * 5. Lượt chơi đầu tiên thuộc về Player (RED)
     *
     * Post-condition UC-01:
     *   - Toàn bộ ô board[r][c] == EMPTY
     *   - current == RED (lượt đầu tiên là người chơi)
     */
    public HexGame(int n) {
        this.n = n;
        this.board = new int[n][n]; // tất cả ô mặc định = 0 (EMPTY)
        this.current = RED;
    }

    /**
     * UC-08 – Tùy chọn chơi lại / UC-04 – AI cần clone trạng thái để tính Minimax
     * Tạo bản sao sâu (deep copy) của trạng thái bàn cờ hiện tại.
     * Dùng trong HexAI.bestMove() để duyệt cây game mà không ảnh hưởng bàn cờ gốc.
     */
    public HexGame copy() {
        HexGame copy = new HexGame(n);
        for (int i = 0; i < n; i++) {
            System.arraycopy(board[i], 0, copy.board[i], 0, n);
        }
        copy.current = this.current;
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

    /**
     * UC-03 – Kiểm tra nước đi hợp lệ (Standard flow 4.1.6, bước 3-4)
     * Kiểm tra ô (r, c) có trống hay không.
     *
     * @return true nếu ô hợp lệ (chưa có quân), false nếu đã bị chiếm
     *
     * Alternative flow 4.1.7 / 4.1.4 – Ô đã có quân cờ:
     *   Trả về false → HexController.handleClick từ chối thao tác đặt quân
     */
    public boolean isEmpty(int r, int c) {
        return board[r][c] == EMPTY;
    }

    /**
     * UC-02 – Đặt quân cờ (Standard flow 4.1.3, bước 4)
     * UC-04 – Đặt quân AI (Standard flow 4.1.9, bước 5)
     *
     * Đặt quân của người chơi lên ô (r, c) nếu ô còn trống.
     * Sau khi đặt, chuyển lượt cho người chơi còn lại.
     *
     * Post-condition UC-02 / UC-04:
     *   - board[r][c] == player
     *   - current đổi sang người còn lại
     *
     * Alternative flow 4.1.4 – Ô đã có quân:
     *   board[r][c] != EMPTY → return false (không thay đổi trạng thái)
     *
     * @return true nếu đặt thành công, false nếu ô đã bị chiếm
     */
    public boolean place(int r, int c, int player) {
        // UC-03 Alternative flow 4.1.4: ô đã có quân → từ chối
        if (board[r][c] != EMPTY) {
            return false;
        }
        board[r][c] = player;
        // Chuyển lượt sau khi đặt quân thành công
        current = (player == RED) ? BLUE : RED;
        return true;
    }

    /**
     * UC-05 – Kiểm tra thắng/thua (Standard flow 4.1.12, bước 2-5)
     * Kiểm tra xem RED hoặc BLUE đã tạo đường nối chiến thắng chưa.
     *
     * @return RED nếu người chơi đỏ thắng,
     *         BLUE nếu AI/người chơi xanh thắng,
     *         EMPTY nếu chưa có người thắng
     *
     * Alternative flow 4.1.13 – Chưa tồn tại đường nối:
     *   Cả hai hasPlayerWon đều false → trả về EMPTY → trò chơi tiếp tục
     */
    public int checkWinner() {
        if (hasPlayerWon(RED))  return RED;
        if (hasPlayerWon(BLUE)) return BLUE;
        return EMPTY;
    }

    /**
     * UC-05 – Kiểm tra thắng/thua (Standard flow 4.1.12, bước 3-4)
     * Dùng DFS (Depth-First Search) để kiểm tra điều kiện nối cạnh.
     *
     * Luật thắng Hex:
     *   - RED thắng khi nối được cạnh Trên (hàng 0) đến cạnh Dưới (hàng n-1)
     *   - BLUE thắng khi nối được cạnh Trái (cột 0) đến cạnh Phải (cột n-1)
     *
     * Alternative flow 4.1.14 – Lỗi xử lý dữ liệu:
     *   visited[][] đảm bảo không duyệt lặp; board bounds được kiểm tra trước khi truy cập
     */
    private boolean hasPlayerWon(int player) {
        boolean[][] visited = new boolean[n][n];
        java.util.Deque<int[]> stack = new java.util.ArrayDeque<>();
        // 6 hướng liền kề trong lưới lục giác
        int[][] dirs = {{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};

        if (player == RED) {
            // UC-05 bước 3: Seed DFS từ toàn bộ ô RED ở hàng đầu (cạnh Trên)
            for (int c = 0; c < n; c++) {
                if (board[0][c] == RED) {
                    visited[0][c] = true;
                    stack.push(new int[]{0, c});
                }
            }
            while (!stack.isEmpty()) {
                int[] p = stack.pop();
                int r = p[0], c = p[1];
                // UC-05 bước 4: Đạt hàng cuối (cạnh Dưới) → RED thắng
                if (r == n - 1) return true;
                for (int[] d : dirs) {
                    int nr = r + d[0], nc = c + d[1];
                    if (nr >= 0 && nr < n && nc >= 0 && nc < n && !visited[nr][nc] && board[nr][nc] == RED) {
                        visited[nr][nc] = true;
                        stack.push(new int[]{nr, nc});
                    }
                }
            }
        } else {
            // UC-05 bước 3: Seed DFS từ toàn bộ ô BLUE ở cột đầu (cạnh Trái)
            for (int r = 0; r < n; r++) {
                if (board[r][0] == BLUE) {
                    visited[r][0] = true;
                    stack.push(new int[]{r, 0});
                }
            }
            while (!stack.isEmpty()) {
                int[] p = stack.pop();
                int r = p[0], c = p[1];
                // UC-05 bước 4: Đạt cột cuối (cạnh Phải) → BLUE thắng
                if (c == n - 1) return true;
                for (int[] d : dirs) {
                    int nr = r + d[0], nc = c + d[1];
                    if (nr >= 0 && nr < n && nc >= 0 && nc < n && !visited[nr][nc] && board[nr][nc] == BLUE) {
                        visited[nr][nc] = true;
                        stack.push(new int[]{nr, nc});
                    }
                }
            }
        }
        return false;
    }
}
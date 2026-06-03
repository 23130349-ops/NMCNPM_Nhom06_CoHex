package model;

import java.util.*;

/**
 * HexAI – Bộ máy tính toán nước đi của AI.
 * Liên quan đến Use Case:
 *   UC-04 Tính toán và đặt quân (Màu xanh – AI) (Standard flow 4.1.9)
 */
public class HexAI {
    private final int INF = 1_000_000_000;
    private final int WIN = 100_000;
    
    private long startTime;
    private long timeLimitMs;
    private boolean timeout;
    private int[] bestMoveSoFar;
    /**
     * UC-04 – Standard flow 4.1.9, bước 2-4
     * 2. AI phân tích các nước đi khả thi (getOrderedMoves – ưu tiên ô trung tâm)
     * 3. AI sử dụng thuật toán Minimax kết hợp Alpha-Beta để chọn nước đi tối ưu
     * 4. Trả về ô hợp lệ tốt nhất [row, col] cho AI (BLUE)
     *
     * Alternative flow 4.1.10 – Không còn ô hợp lệ:
     *   getOrderedMoves() trả về danh sách rỗng → vòng for không chạy → best == null
     *   → HexController nhận null, không đặt quân, kết thúc trận
     *
     * Alternative flow 4.1.11 – AI vượt quá thời gian xử lý:
     *   Giới hạn độ sâu (depth) giúp kiểm soát thời gian tính toán.
     *   Nếu cần hard-timeout, có thể thêm kiểm tra System.currentTimeMillis() tại đây.
     *
     * @param game  trạng thái bàn cờ hiện tại
     * @param depth độ sâu tìm kiếm Minimax
     * @return nước đi tối ưu [row, col], hoặc null nếu không còn ô nào
     */
    private boolean isTimeAlmostUp() {
    return System.currentTimeMillis() - startTime >= timeLimitMs * 0.9;
    }
    public int[] bestMove(HexGame game, int depth) {
    startTime = System.currentTimeMillis();
    timeLimitMs = 15_000; // AI tối đa 15 giây
    timeout = false;
    bestMoveSoFar = null;

    int bestVal = -INF;
    int[] best = null;
    int alpha = -INF;
    int beta = INF;

    for (int[] move : getOrderedMoves(game)) {
        if (isTimeAlmostUp()) {
            timeout = true;
            break;
        }

        HexGame child = game.copy();
        child.place(move[0], move[1], HexGame.BLUE);

        int val = minimax(child, false, depth - 1, alpha, beta);

        if (timeout) {
            break;
        }

        if (val > bestVal) {
            bestVal = val;
            best = move;
            bestMoveSoFar = move;
        }

        alpha = Math.max(alpha, bestVal);
    }

    if (best != null) {
        return best;
    }

    if (bestMoveSoFar != null) {
        return bestMoveSoFar;
    }

    List<int[]> moves = getOrderedMoves(game);
    if (!moves.isEmpty()) {
        return moves.get(0);
    }

    return null;
}

    /**
     * UC-04 – Standard flow 4.1.9, bước 3
     * Thuật toán Minimax kết hợp cắt tỉa Alpha-Beta.
     *
     * - maximizing = true  → lượt BLUE (AI) cố tối đa hóa điểm
     * - maximizing = false → lượt RED (người) cố tối thiểu hóa điểm của AI
     * - Khi depth == 0: đánh giá bằng hàm heuristic (khoảng cách đường đi ngắn nhất)
     *
     * Điều kiện kết thúc sớm:
     *   BLUE thắng  → +WIN + depth (thắng sớm được ưu tiên hơn)
     *   RED thắng   → -WIN - depth (thua sớm bị phạt nặng hơn)
     */
    private int minimax(HexGame state, boolean maximizing, int depth, int alpha, int beta) {
        int winner = state.checkWinner();
        // UC-05 được gọi gián tiếp: kiểm tra điều kiện thắng/thua trong cây tìm kiếm
        if (winner == HexGame.BLUE) return  WIN + depth;
        if (winner == HexGame.RED)  return -WIN - depth;
        if (depth == 0) return heuristic(state);

        if (maximizing) {
            // Lượt BLUE: tối đa hóa
            int value = -INF;
            for (int[] m : getOrderedMoves(state)) {
                HexGame child = state.copy();
                child.place(m[0], m[1], HexGame.BLUE);
                value = Math.max(value, minimax(child, false, depth - 1, alpha, beta));
                alpha = Math.max(alpha, value);
                // Cắt tỉa Alpha-Beta: nhánh này không cần duyệt tiếp
                if (beta <= alpha) break;
            }
            return value;
        } else {
            // Lượt RED: tối thiểu hóa
            int value = INF;
            for (int[] m : getOrderedMoves(state)) {
                HexGame child = state.copy();
                child.place(m[0], m[1], HexGame.RED);
                value = Math.min(value, minimax(child, true, depth - 1, alpha, beta));
                beta = Math.min(beta, value);
                // Cắt tỉa Alpha-Beta
                if (beta <= alpha) break;
            }
            return value;
        }
    }

    /**
     * UC-04 – Hàm đánh giá heuristic cho trạng thái bàn cờ.
     * Điểm = (đường đi ngắn nhất của RED) − (đường đi ngắn nhất của BLUE)
     * AI (BLUE) muốn tối đa hóa giá trị này → đường đi của RED dài, của BLUE ngắn.
     */
    private int heuristic(HexGame s) {
        int blueDist = getShortestPath(s, HexGame.BLUE);
        int redDist  = getShortestPath(s, HexGame.RED);
        if (blueDist == INF) blueDist = 1000;
        if (redDist  == INF) redDist  = 1000;
        return (redDist - blueDist) * 1000;
    }

    /**
     * UC-04 – Tính đường đi ngắn nhất từ cạnh xuất phát đến cạnh đích của người chơi.
     * Dùng thuật toán Dijkstra trên lưới lục giác:
     *   - Ô đã chiếm (cùng màu): chi phí 0
     *   - Ô trống: chi phí 1
     *   - Ô đối thủ: chi phí vô cùng (không đi qua)
     *
     * RED: xuất phát hàng 0, đích hàng n-1
     * BLUE: xuất phát cột 0, đích cột n-1
     */
    private int getShortestPath(HexGame state, int player) {
        int n        = state.getSize();
        int[][] board = state.getBoard();
        int[][] dist  = new int[n][n];
        for (int[] row : dist) Arrays.fill(row, INF);

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[2]));

        // Khởi tạo seed từ cạnh xuất phát
        for (int i = 0; i < n; i++) {
            int r    = (player == HexGame.RED) ? 0 : i;
            int c    = (player == HexGame.RED) ? i : 0;
            int cost = (board[r][c] == player) ? 0 : (board[r][c] == HexGame.EMPTY ? 1 : INF);
            if (cost != INF) {
                dist[r][c] = cost;
                pq.add(new int[]{r, c, cost});
            }
        }

        int[][] dirs = {{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};
        while (!pq.isEmpty()) {
            int[] cur  = pq.poll();
            int r = cur[0], c = cur[1], cost = cur[2];
            if (cost > dist[r][c]) continue;
            // Kiểm tra đã đến cạnh đích chưa
            if ((player == HexGame.RED && r == n - 1) || (player == HexGame.BLUE && c == n - 1)) {
                return cost;
            }
            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (nr >= 0 && nr < n && nc >= 0 && nc < n) {
                    int w = (board[nr][nc] == player) ? 0 : (board[nr][nc] == HexGame.EMPTY ? 1 : INF);
                    if (w != INF && cost + w < dist[nr][nc]) {
                        dist[nr][nc] = cost + w;
                        pq.add(new int[]{nr, nc, dist[nr][nc]});
                    }
                }
            }
        }
        return INF;
    }

    /**
     * UC-04 – Lấy danh sách nước đi khả thi, sắp xếp ưu tiên ô gần trung tâm.
     * Ô trung tâm thường có giá trị chiến lược cao hơn trong cờ Hex.
     *
     * Alternative flow 4.1.10 – Không còn ô hợp lệ:
     *   Nếu tất cả ô đã bị chiếm → trả về danh sách rỗng
     */
    private List<int[]> getOrderedMoves(HexGame s) {
        List<int[]> moves = new ArrayList<>();
        int n      = s.getSize();
        int center = n / 2;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                // UC-03 bước 3: Chỉ xem xét ô còn trống
                if (s.isEmpty(r, c)) {
                    moves.add(new int[]{r, c});
                }
            }
        }
        // Sắp xếp: ô gần trung tâm nhất được ưu tiên duyệt trước
        moves.sort(Comparator.comparingInt(a -> Math.abs(a[0] - center) + Math.abs(a[1] - center)));
        return moves;
    }
}
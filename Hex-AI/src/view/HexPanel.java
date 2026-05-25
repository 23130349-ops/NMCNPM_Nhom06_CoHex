package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import model.HexGame;

/**
 * HexPanel – Panel vẽ bàn cờ và xử lý sự kiện click chuột của người chơi.
 *
 * Liên quan đến Use Case:
 *   UC-01 Khởi tạo ván mới        – setBoard() + repaint() hiển thị bàn cờ mới
 *   UC-02 Đặt quân cờ (Màu đỏ)   – mouseClicked → findCell → CellClickListener.onClick
 *   UC-03 Kiểm tra nước đi hợp lệ – findCell() xác định ô, trả null nếu ngoài phạm vi
 *   UC-04 Đặt quân AI (Màu xanh)  – setThinking(true) khóa tương tác khi AI đang tính
 *   UC-06 Hiển thị kết quả        – repaint() cập nhật giao diện sau mỗi lượt
 */
public class HexPanel extends JPanel {
    private int[][] board;
    private Polygon[][] hexes;
    private final int n;
    private final int cellSize = 60;
    private final int OFFSET_X = 80, OFFSET_Y = 80;
    private CellClickListener listener;
    private boolean thinking = false;

    /**
     * UC-01 – Khởi tạo ván mới (bước 4): chuẩn bị panel hiển thị bàn cờ.
     * UC-02 – Đặt quân cờ (Standard flow 4.1.3, bước 1):
     *   Đăng ký MouseListener để nhận sự kiện click chuột từ người chơi.
     *
     * @param n kích thước bàn cờ n×n
     */
    public HexPanel(int n) {
        this.n    = n;
        hexes     = new Polygon[n][n];
        setBackground(new Color(245, 245, 245));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // UC-04 Alternative flow 4.1.11: Khóa click khi AI đang tính toán
                // UC-06 bước 3: Sau khi kết thúc, thinking = true để khóa thao tác
                if (thinking || listener == null) return;

                // UC-02 bước 2 / UC-03 bước 1-2: Xác định ô tương ứng với tọa độ click
                int[] pos = findCell(e.getX(), e.getY());

                // UC-03 / Alternative flow 4.1.5 / 4.1.8: Click ngoài bàn cờ → pos == null → bỏ qua
                if (pos != null) listener.onClick(pos[0], pos[1]);
            }
        });
    }

    /**
     * UC-01 bước 4 / UC-08 bước 7: Cập nhật tham chiếu bàn cờ để vẽ lại.
     * Gọi sau initGame() để hiển thị trạng thái mới nhất.
     */
    public void setBoard(int[][] board) { this.board = board; }

    /**
     * UC-04 – Điều khiển trạng thái khóa tương tác.
     * setThinking(true)  → khóa click khi AI đang tính toán (Standard flow 4.1.9)
     * setThinking(false) → mở lại sau khi AI đặt quân xong
     *
     * UC-06 bước 3: Dùng để khóa bàn cờ khi trận đấu kết thúc.
     */
    public void setThinking(boolean t) { this.thinking = t; }

    /**
     * UC-02 bước 1: Đăng ký listener để nhận sự kiện click từ người chơi.
     * HexController truyền vào lambda this::handleClick.
     */
    public void addCellClickListener(CellClickListener l) { this.listener = l; }

    /**
     * UC-01 bước 4 / UC-02 bước 5 / UC-04 bước 6 / UC-06 bước 2:
     * Vẽ lại toàn bộ bàn cờ với trạng thái hiện tại.
     * - Ô EMPTY  → xám nhạt
     * - Ô RED    → đỏ
     * - Ô BLUE   → xanh
     * Sau đó vẽ viền màu sắc cho hai cạnh mục tiêu của mỗi bên.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int dx = cellSize;
        int dy = (int)(cellSize * Math.sqrt(3) / 2);

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                // Tính tọa độ pixel của ô (r, c) theo lưới lục giác lệch
                int x = c * dx + r * dx / 2 + OFFSET_X;
                int y = r * dy + OFFSET_Y;
                Polygon hex = createHex(x, y, cellSize / 2);
                hexes[r][c] = hex;

                // UC-02 bước 5 / UC-04 bước 6: Tô màu quân cờ vừa đặt
                if      (board[r][c] == HexGame.RED)  g2.setColor(Color.RED);
                else if (board[r][c] == HexGame.BLUE) g2.setColor(Color.BLUE);
                else                                   g2.setColor(Color.LIGHT_GRAY);

                g2.fillPolygon(hex);
                g2.setColor(Color.BLACK);
                g2.drawPolygon(hex);
            }
        }
        // Vẽ viền màu đỏ (cạnh Trên/Dưới) và xanh (cạnh Trái/Phải)
        drawBorders(g2);
    }

    /**
     * Tạo đa giác lục giác đều tại tâm (x, y) với bán kính r.
     * Góc bắt đầu π/6 để lục giác nằm theo kiểu "flat-top" (cạnh trên nằm ngang).
     */
    private Polygon createHex(int x, int y, int r) {
        Polygon p = new Polygon();
        for (int i = 0; i < 6; i++) {
            double ang = Math.PI / 3 * i + Math.PI / 6;
            p.addPoint(
                (int) Math.round(x + r * Math.cos(ang)),
                (int) Math.round(y + r * Math.sin(ang))
            );
        }
        return p;
    }

    /**
     * UC-03 – Kiểm tra nước đi hợp lệ (Standard flow 4.1.6, bước 2)
     * Xác định ô trên bàn cờ tương ứng với tọa độ pixel (mx, my) của chuột.
     *
     * Alternative flow 4.1.5 / 4.1.8 – Click ngoài phạm vi bàn cờ:
     *   Không ô nào chứa điểm (mx, my) → trả về null
     *   → mouseClicked bỏ qua, không gọi listener → trạng thái game không thay đổi
     *
     * @return [row, col] nếu tìm thấy ô, null nếu click ngoài bàn cờ
     */
    private int[] findCell(int mx, int my) {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (hexes[r][c] != null && hexes[r][c].contains(mx, my)) {
                    return new int[]{r, c};
                }
            }
        }
        // Alternative flow 4.1.5 / 4.1.8: Click ngoài phạm vi → null
        return null;
    }

    /**
     * Vẽ viền màu cho hai cạnh mục tiêu của mỗi người chơi:
     *   - Viền đỏ: cạnh Trên (hàng 0) và cạnh Dưới (hàng n-1) → mục tiêu của RED
     *   - Viền xanh: cạnh Trái (cột 0) và cạnh Phải (cột n-1) → mục tiêu của BLUE
     * Giúp người chơi nhận biết hướng cần nối (liên quan UC-05 điều kiện thắng).
     */
    private void drawBorders(Graphics2D g2) {
        g2.setStroke(new BasicStroke(5f));

        // Viền đỏ – cạnh Trên và Dưới (mục tiêu của RED)
        g2.setColor(new Color(220, 40, 40));
        for (int c = 0; c < n; c++) {
            Polygon top = hexes[0][c];
            if (top != null) {
                g2.drawLine(top.xpoints[3], top.ypoints[3], top.xpoints[4], top.ypoints[4]);
                g2.drawLine(top.xpoints[4], top.ypoints[4], top.xpoints[5], top.ypoints[5]);
            }
            Polygon bot = hexes[n - 1][c];
            if (bot != null) {
                g2.drawLine(bot.xpoints[0], bot.ypoints[0], bot.xpoints[1], bot.ypoints[1]);
                g2.drawLine(bot.xpoints[1], bot.ypoints[1], bot.xpoints[2], bot.ypoints[2]);
            }
        }

        // Viền xanh – cạnh Trái và Phải (mục tiêu của BLUE)
        g2.setColor(new Color(50, 50, 220));
        for (int r = 0; r < n; r++) {
            Polygon left = hexes[r][0];
            if (left != null) {
                g2.drawLine(left.xpoints[2], left.ypoints[2], left.xpoints[3], left.ypoints[3]);
                g2.drawLine(left.xpoints[1], left.ypoints[1], left.xpoints[2], left.ypoints[2]);
            }
            Polygon right = hexes[r][n - 1];
            if (right != null) {
                g2.drawLine(right.xpoints[5], right.ypoints[5], right.xpoints[0], right.ypoints[0]);
                g2.drawLine(right.xpoints[4], right.ypoints[4], right.xpoints[5], right.ypoints[5]);
            }
        }
    }
}
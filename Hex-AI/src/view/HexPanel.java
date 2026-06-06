package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import model.HexGame;

/**
 * HexPanel – Panel vẽ bàn cờ và xử lý sự kiện click chuột của người chơi.
 */
public class HexPanel extends JPanel {

    // Bảng màu đồng bộ
    private static final Color ACCENT_BLUE    = new Color(55,  130, 230);
    private static final Color ACCENT_CORAL   = new Color(230,  85,  75);
    private static final Color HEX_HOVER      = new Color(225, 228, 235);
    private static final Color HEX_BORDER     = new Color(200, 205, 215);
    private static final Color WIN_HIGHLIGHT  = new Color(255, 215, 50);

    private int[][] board;
    private Polygon[][] hexes;
    private final int n;
    private final int cellSize = 60;

    // Tăng tọa độ OFFSET để khi vẽ Full-screen bàn cờ nằm thụt vào đẹp hơn
    private final int OFFSET_X = 120, OFFSET_Y = 100;

    private CellClickListener listener;
    private boolean thinking = false;
    private int hoverRow = -1, hoverCol = -1;
    // [Tran05] Vị trí nước đi cuối cùng được cập nhật để vẽ viền sáng (Highlight Last Move)
    private int lastRow = -1, lastCol = -1;
    // [Tran05] Danh sách tọa độ các ô cờ trên đường chiến thắng thu được từ thuật toán DFS để tô màu nổi bật
    private List<int[]> winningPath = new ArrayList<>();

    public HexPanel(int n) {
        this.n    = n;
        hexes     = new Polygon[n][n];

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (thinking || listener == null) return;
                // [Tran05] UC-09 Trigger (a) - Người chơi click chọn ô cờ để đặt quân
                int[] pos = findCell(e.getX(), e.getY());
                if (pos != null) listener.onClick(pos[0], pos[1]);
            }
            @Override public void mouseExited(MouseEvent e) {
                hoverRow = -1; hoverCol = -1; repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                if (thinking) return;
                int[] pos = findCell(e.getX(), e.getY());
                if (pos != null) {
                    if (hoverRow != pos[0] || hoverCol != pos[1]) {
                        hoverRow = pos[0]; hoverCol = pos[1]; repaint();
                    }
                } else {
                    if (hoverRow != -1 || hoverCol != -1) {
                        hoverRow = -1; hoverCol = -1; repaint();
                    }
                }
            }
        });
    }

    // [Tran05] Cập nhật tọa độ nước đi cuối cùng để repaint bàn cờ
    public void setLastMove(int r, int c) { this.lastRow = r; this.lastCol = c; repaint(); }
    // [Tran05] Nhận danh sách đường thắng từ game để thực hiện Highlight
    public void setWinningPath(List<int[]> path) { this.winningPath = (path != null) ? path : new ArrayList<>(); repaint(); }
    // [Tran05] UC-07: SF2.9 & UC-09: SF1.5 - Nhận ma trận cờ để chuẩn bị vẽ các quân cờ
    public void setBoard(int[][] board) { this.board = board; }
    public void setThinking(boolean t) { this.thinking = t; }
    public void addCellClickListener(CellClickListener l) { this.listener = l; }

    // [Tran05] UC-07: SF2.10 & UC-09: SF1.3 - Nhận yêu cầu vẽ lại toàn bộ giao diện bàn cờ
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
                int x = c * dx + r * dx / 2 + OFFSET_X;
                int y = r * dy + OFFSET_Y;
                Polygon hex = createHex(x, y, cellSize / 2);
                hexes[r][c] = hex;

                boolean isWinningCell = false;
                for (int[] p : winningPath) {
                    if (p[0] == r && p[1] == c) { isWinningCell = true; break; }
                }

                // [Tran05] Nếu ô nằm trong đường thắng, tô màu vàng nổi bật (WIN_HIGHLIGHT)
                if (isWinningCell) {
                    g2.setColor(WIN_HIGHLIGHT);
                } else if (board[r][c] == HexGame.RED) {
                    g2.setColor(ACCENT_CORAL);
                } else if (board[r][c] == HexGame.BLUE) {
                    g2.setColor(ACCENT_BLUE);
                } else if (r == hoverRow && c == hoverCol && board[r][c] == HexGame.EMPTY) {
                    g2.setColor(HEX_HOVER);
                } else {
                    g2.setColor(Color.WHITE); // Trắng thanh lịch
                }
                g2.fillPolygon(hex);

                // Vẽ viền ô cờ
                g2.setColor(HEX_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawPolygon(hex);

                // [Tran05] Hiệu ứng "ô vừa đánh" (Highlight Last Move) - Vẽ dấu chấm tròn phát sáng ở giữa ô
                if (r == lastRow && c == lastCol) {
                    g2.setColor(new Color(255, 255, 255, 200)); // Màu trắng trong suốt
                    g2.fillOval(x - 8, y - 8, 16, 16);
                }
            }
        }
        drawBorders(g2);
    }

    private Polygon createHex(int x, int y, int r) {
        Polygon p = new Polygon();
        for (int i = 0; i < 6; i++) {
            double ang = Math.PI / 3 * i + Math.PI / 6;
            p.addPoint((int) Math.round(x + r * Math.cos(ang)), (int) Math.round(y + r * Math.sin(ang)));
        }
        return p;
    }

    private int[] findCell(int mx, int my) {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (hexes[r][c] != null && hexes[r][c].contains(mx, my)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private void drawBorders(Graphics2D g2) {
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Viền đỏ – Cạnh Trên và Dưới
        g2.setColor(ACCENT_CORAL);
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

        // Viền xanh – Cạnh Trái và Phải
        g2.setColor(ACCENT_BLUE);
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
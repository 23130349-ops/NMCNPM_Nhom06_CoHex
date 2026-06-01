package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * HexFrame – Cửa sổ chính chứa toàn bộ giao diện trò chơi.
 */
public class HexFrame extends JFrame {

    // Bảng màu đồng bộ từ SetupDialog
    private static final Color BG_PAGE        = new Color(245, 247, 250);
    private static final Color BG_CARD        = Color.WHITE;
    private static final Color ACCENT_BLUE    = new Color(55,  130, 230);
    private static final Color ACCENT_CORAL   = new Color(230,  85,  75);
    private static final Color ACCENT_GREEN   = new Color( 34, 180,  95);
    private static final Color TEXT_TITLE     = new Color( 30,  34,  45);
    private static final Color TEXT_BODY      = new Color( 85,  90, 105);
    private static final Color TEXT_MUTED     = new Color(145, 150, 165);
    private static final Color BORDER_DEFAULT = new Color(225, 228, 235);

    private final HexPanel panel;
    private final JButton undoButton;
    private final JButton btnSave;
    private final JButton btnLoad;
    private final JButton btnBackToMenu; // NÚT MỚI: Về Menu

    private final JMenuItem saveMenuItem;
    private final JMenuItem loadMenuItem;
    private final TimerLabel redLabel;
    private final TimerLabel blueLabel;
    private final JTextArea historyArea;

    public HexFrame(int n) {
        setTitle("Hex Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true); // Full-screen không viền
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        setLocation(0, 0);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PAGE);
        setContentPane(root);

        // ─── HEADER (Thanh trên cùng) ───
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 40, 15, 40));

        JLabel titleLbl = new JLabel("HEX GAME");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLbl.setForeground(TEXT_TITLE);
        JLabel hexIcon = new JLabel("\u2B21");
        hexIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 36));
        hexIcon.setForeground(ACCENT_BLUE);
        hexIcon.setBorder(new EmptyBorder(0,0,0,15));

        JPanel leftGroup = new JPanel(new BorderLayout());
        leftGroup.setOpaque(false);
        leftGroup.add(hexIcon, BorderLayout.WEST);
        leftGroup.add(titleLbl, BorderLayout.CENTER);

        // Đồng hồ tính giờ
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        timerPanel.setOpaque(false);
        redLabel = new TimerLabel("RED: 10:00", ACCENT_CORAL);
        blueLabel = new TimerLabel("BLUE: 10:00", ACCENT_BLUE);
        JLabel vsLabel = new JLabel("VS");
        vsLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        vsLabel.setForeground(TEXT_MUTED);
        timerPanel.add(redLabel);
        timerPanel.add(vsLabel);
        timerPanel.add(blueLabel);

        // Nút VỀ MENU
        btnBackToMenu = new JButton("🔙 VỀ MENU") {
            boolean hov = false;
            {
                setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font("Segoe UI", Font.BOLD, 16));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hov) {
                    g2.setColor(new Color(230, 85, 75, 25)); // Đổ nền đỏ nhạt khi hover
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                    setForeground(ACCENT_CORAL); // Chữ đỏ
                } else setForeground(TEXT_MUTED); // Chữ xám
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btnBackToMenu.setPreferredSize(new Dimension(140, 45));
        // LƯU Ý: Không gán System.exit() ở đây nữa để nhường Controller quản lý

        header.add(leftGroup, BorderLayout.WEST);
        header.add(timerPanel, BorderLayout.CENTER);
        header.add(btnBackToMenu, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ─── BÀN CỜ TRUNG TÂM ───
        panel = new HexPanel(n);
        panel.setOpaque(false);
        root.add(panel, BorderLayout.CENTER);

        // ─── SIDEBAR (Thanh công cụ bên phải) ───
        JPanel sidebarWrapper = new JPanel(new BorderLayout());
        sidebarWrapper.setOpaque(false);
        sidebarWrapper.setBorder(new EmptyBorder(0, 20, 40, 40));

        JPanel sidebarCard = new JPanel(new BorderLayout(0, 15)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
        };
        sidebarCard.setOpaque(false);
        sidebarCard.setBorder(new EmptyBorder(25, 25, 25, 25));
        sidebarCard.setPreferredSize(new Dimension(360, 0));

        JLabel historyTitle = new JLabel("LỊCH SỬ ĐÁNH");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        historyTitle.setForeground(TEXT_MUTED);
        historyTitle.setHorizontalAlignment(SwingConstants.CENTER);
        sidebarCard.add(historyTitle, BorderLayout.NORTH);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        historyArea.setBackground(BG_CARD);
        historyArea.setForeground(TEXT_BODY);
        historyArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_DEFAULT));
        scrollPane.getViewport().setBackground(BG_CARD);
        sidebarCard.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(4, 1, 0, 12));
        actionPanel.setOpaque(false);

        undoButton = createPillButton("Hoàn nước (Undo)", TEXT_TITLE, BG_PAGE, BORDER_DEFAULT);
        btnSave    = createPillButton("Lưu nhanh (Quick Save)", ACCENT_GREEN, new Color(ACCENT_GREEN.getRed(), ACCENT_GREEN.getGreen(), ACCENT_GREEN.getBlue(), 20), ACCENT_GREEN);
        btnLoad    = createPillButton("Tải nhanh (Quick Load)", ACCENT_BLUE, new Color(ACCENT_BLUE.getRed(), ACCENT_BLUE.getGreen(), ACCENT_BLUE.getBlue(), 20), ACCENT_BLUE);

        JPopupMenu fileMenu = new JPopupMenu();
        saveMenuItem = new JMenuItem("Lưu Game (Chọn File)...");
        loadMenuItem = new JMenuItem("Tải Game (Chọn File)...");
        saveMenuItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadMenuItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fileMenu.add(saveMenuItem);
        fileMenu.add(loadMenuItem);

        JButton btnMenu = createPillButton("Tùy chọn File...", TEXT_MUTED, BG_PAGE, BORDER_DEFAULT);
        btnMenu.addActionListener(e -> fileMenu.show(btnMenu, 0, btnMenu.getHeight() + 5));

        actionPanel.add(undoButton);
        actionPanel.add(btnSave);
        actionPanel.add(btnLoad);
        actionPanel.add(btnMenu);

        sidebarCard.add(actionPanel, BorderLayout.SOUTH);
        sidebarWrapper.add(sidebarCard, BorderLayout.CENTER);
        root.add(sidebarWrapper, BorderLayout.EAST);

        setVisible(true);
    }

    private JButton createPillButton(String text, Color fg, Color bg, Color border) {
        JButton btn = new JButton(text) {
            boolean hov = false;
            {
                setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font("Segoe UI", Font.BOLD, 15));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = getHeight();

                g2.setColor(hov ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);

                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(300, 48));
        return btn;
    }

    private class TimerLabel extends JLabel {
        private final Color bgColor;
        TimerLabel(String text, Color bg) {
            super(text, SwingConstants.CENTER);
            this.bgColor = bg;
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 22));
            setPreferredSize(new Dimension(180, 50));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // Các Getter public cho Controller sử dụng
    public HexPanel getPanel() { return panel; }
    public JButton getUndoButton() { return undoButton; }
    public JButton getBtnSave() { return btnSave; }
    public JButton getBtnLoad() { return btnLoad; }
    public JButton getBtnBackToMenu() { return btnBackToMenu; } // Getter Nút Về Menu

    public JMenuItem getSaveMenuItem() { return saveMenuItem; }
    public JMenuItem getLoadMenuItem() { return loadMenuItem; }
    public void updateHistoryText(String text) { historyArea.setText(text); }
    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
    public void setTimerValues(int redSecs, int blueSecs) {
        redLabel.setText(String.format("RED: %02d:%02d", redSecs / 60, redSecs % 60));
        blueLabel.setText(String.format("BLUE: %02d:%02d", blueSecs / 60, blueSecs % 60));
    }
}
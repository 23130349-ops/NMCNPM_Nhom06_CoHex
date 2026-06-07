package view;

import model.GameTimer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * SetupDialog – Hộp thoại chọn chế độ chơi trước khi bắt đầu ván mới.
 * Thiết kế giao diện toàn màn hình, có chọn chế độ chơi, kích thước bàn cờ,
 * độ khó AI và thời gian.
 * * [NgocTrinh] Đảm nhiệm UC-01 (Khởi tạo ván mới): Xây dựng màn hình thiết lập
 * để thu thập các tham số đầu vào của người chơi trước khi sinh ra bàn cờ.
 */
public class SetupDialog extends JDialog {

    // ═══════════════════════════════════════════════════════════
    //  Bảng màu
    // ═══════════════════════════════════════════════════════════
    private static final Color BG_PAGE        = new Color(245, 247, 250);
    private static final Color BG_CARD        = Color.WHITE;
    private static final Color BG_CARD_HOVER  = new Color(244, 248, 255);

    private static final Color ACCENT_BLUE    = new Color(55, 130, 230);
    private static final Color ACCENT_CORAL   = new Color(230, 85, 75);
    private static final Color ACCENT_PURPLE  = new Color(140, 90, 220);
    private static final Color ACCENT_GREEN   = new Color(34, 180, 95);
    private static final Color ACCENT_GREEN_H = new Color(28, 160, 82);

    private static final Color TEXT_TITLE     = new Color(30, 34, 45);
    private static final Color TEXT_MUTED     = new Color(145, 150, 165);
    private static final Color BORDER_DEFAULT = new Color(225, 228, 235);
    private static final Color BORDER_FOCUS   = new Color(180, 200, 245);

    // ═══════════════════════════════════════════════════════════
    //  Enum & Config
    // ═══════════════════════════════════════════════════════════
    public enum Mode {
        HUMAN_VS_AI,
        HUMAN_VS_HUMAN,
        AI_VS_AI
    }

    /**
     * [NgocTrinh] UC-01: Lớp Data Transfer Object (DTO) lưu trữ toàn bộ cấu hình ván mới
     * để truyền dữ liệu từ View (SetupDialog) sang Controller.
     */
    public static class Config {
        public int size;
        public Mode mode;
        public int depth; // 1 dễ, 2 vừa, 3 khó
        public GameTimer.Mode timerMode = GameTimer.Mode.NONE;
        public int timerSeconds = 300;
    }

    // ═══════════════════════════════════════════════════════════
    //  Fields quản lý trạng thái
    // ═══════════════════════════════════════════════════════════
    private int selectedSize = 11;
    private int selectedDepth = 3;

    private GameTimer.Mode currentTimerMode = GameTimer.Mode.NONE;
    private int currentTimerSeconds = 0;

    private ModeCard selectedCard = null;
    private final ModeCard card1;
    private final ModeCard card2;
    private final ModeCard card3;

    private boolean ok = false;

    private JPanel timeOptionsContainer;
    private final JPanel root;

    // ═══════════════════════════════════════════════════════════
    //  Custom Button
    // ═══════════════════════════════════════════════════════════
    private abstract class PillButton extends JButton {
        boolean hovered = false;

        PillButton(String text, int width, int height) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(width, height));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    onClick();
                }
            });
        }

        protected abstract boolean checkSelected();

        protected abstract void onClick();

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            boolean isSel = checkSelected();

            int w = getWidth();
            int h = getHeight();
            int arc = 24;

            if (isSel) {
                g2.setColor(ACCENT_BLUE);
            } else if (hovered) {
                g2.setColor(BG_CARD_HOVER);
            } else {
                g2.setColor(Color.WHITE);
            }

            g2.fillRoundRect(0, 0, w, h, arc, arc);

            if (isSel) {
                g2.setColor(ACCENT_BLUE);
            } else {
                g2.setColor(hovered ? BORDER_FOCUS : BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            }

            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));

            FontMetrics fm = g2.getFontMetrics();
            String txt = getText();

            g2.setColor(isSel ? Color.WHITE : TEXT_TITLE);
            g2.drawString(
                    txt,
                    (w - fm.stringWidth(txt)) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2
            );

            g2.dispose();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Size Button
    // ═══════════════════════════════════════════════════════════
    private class SizeButton extends PillButton {
        private final int sizeVal;

        SizeButton(int val) {
            super(val + " × " + val, 130, 50);
            this.sizeVal = val;
        }

        @Override
        protected boolean checkSelected() {
            return selectedSize == sizeVal;
        }

        @Override
        protected void onClick() {
            selectedSize = sizeVal;
            root.repaint();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Difficulty Button
    // ═══════════════════════════════════════════════════════════
    private class DifficultyButton extends PillButton {
        private final int depthVal;

        DifficultyButton(String text, int depthVal) {
            super(text, 150, 50);
            this.depthVal = depthVal;
        }

        @Override
        protected boolean checkSelected() {
            return selectedDepth == depthVal;
        }

        @Override
        protected void onClick() {
            selectedDepth = depthVal;
            root.repaint();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Timer Mode Button
    // ═══════════════════════════════════════════════════════════
    private class TimerModeButton extends PillButton {
        private final GameTimer.Mode mode;

        TimerModeButton(String text, GameTimer.Mode mode) {
            super(text, 160, 50);
            this.mode = mode;
        }

        @Override
        protected boolean checkSelected() {
            return currentTimerMode == mode;
        }

        @Override
        protected void onClick() {
            currentTimerMode = mode;

            if (mode == GameTimer.Mode.TOTAL_GAME) {
                currentTimerSeconds = 300;
            } else if (mode == GameTimer.Mode.PER_MOVE) {
                currentTimerSeconds = 30;
            } else {
                currentTimerSeconds = 0;
            }

            updateTimeOptionsPanel();
            root.revalidate();
            root.repaint();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Time Value Button
    // ═══════════════════════════════════════════════════════════
    private class TimeValueButton extends PillButton {
        private final int seconds;

        TimeValueButton(String text, int seconds) {
            super(text, 130, 45);
            this.seconds = seconds;
        }

        @Override
        protected boolean checkSelected() {
            return currentTimerSeconds == seconds;
        }

        @Override
        protected void onClick() {
            currentTimerSeconds = seconds;
            root.repaint();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Mode Card
    // ═══════════════════════════════════════════════════════════
    private class ModeCard extends JPanel {
        final Mode mode;
        final String iconText;
        final String titleText;
        final String sub;
        final Color accent;

        boolean selected = false;
        boolean hovered = false;

        ModeCard(Mode mode, String iconText, String titleText, String sub, Color accent) {
            this.mode = mode;
            this.iconText = iconText;
            this.titleText = titleText;
            this.sub = sub;
            this.accent = accent;

            setPreferredSize(new Dimension(240, 200));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    selectCard(ModeCard.this);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = getWidth();
            int h = getHeight();
            int arc = 36;

            g2.setColor(selected ? lighten(accent, 0.92f) : (hovered ? BG_CARD_HOVER : BG_CARD));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            if (selected) {
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            } else {
                g2.setColor(hovered ? BORDER_FOCUS : BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);
            }

            g2.setFont(resolveEmojiFont(52));

            FontMetrics fmi = g2.getFontMetrics();
            g2.setColor(selected ? accent : TEXT_MUTED);
            g2.drawString(iconText, (w - fmi.stringWidth(iconText)) / 2, 85);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));

            FontMetrics fmt = g2.getFontMetrics();
            g2.setColor(selected ? darken(accent, 0.15f) : TEXT_TITLE);
            g2.drawString(titleText, (w - fmt.stringWidth(titleText)) / 2, 130);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));

            FontMetrics fms = g2.getFontMetrics();
            g2.setColor(selected ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200) : TEXT_MUTED);
            g2.drawString(sub, (w - fms.stringWidth(sub)) / 2, 160);

            if (selected) {
                int cx = w / 2;
                int cy = 180;
                int r = 10;

                g2.setColor(accent);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 4, cy, cx - 1, cy + 4);
                g2.drawLine(cx - 1, cy + 4, cx + 5, cy - 3);
            }

            g2.dispose();
        }

        private Color lighten(Color c, float f) {
            int r = (int) (c.getRed() + (255 - c.getRed()) * f);
            int g = (int) (c.getGreen() + (255 - c.getGreen()) * f);
            int b = (int) (c.getBlue() + (255 - c.getBlue()) * f);

            return new Color(
                    Math.min(r, 255),
                    Math.min(g, 255),
                    Math.min(b, 255)
            );
        }

        private Color darken(Color c, float amount) {
            return c.darker();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Constructor
    // ═══════════════════════════════════════════════════════════
    public SetupDialog(Frame owner) {
        super(owner, "Hex – Thiết lập ván đấu", true);

        setUndecorated(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        setLocation(0, 0);

        root = new JPanel(new BorderLayout());
        root.setBackground(BG_PAGE);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 50, 40, 50));

        body.add(Box.createVerticalGlue());

        // 1. Chọn chế độ chơi
        body.add(centerComponent(sectionLabel("CHỌN CHẾ ĐỘ CHƠI")));
        body.add(vgap(20));

        card1 = new ModeCard(Mode.HUMAN_VS_AI, "🤖", "Người vs Máy", "Bạn đấu với AI", ACCENT_BLUE);
        card2 = new ModeCard(Mode.HUMAN_VS_HUMAN, "👥", "Người vs Người", "2 người chơi", ACCENT_CORAL);
        card3 = new ModeCard(Mode.AI_VS_AI, "⚡", "Máy vs Máy", "AI tự đấu", ACCENT_PURPLE);

        card1.selected = true;
        selectedCard = card1;

        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        cardsRow.setOpaque(false);
        cardsRow.add(card1);
        cardsRow.add(card2);
        cardsRow.add(card3);

        body.add(cardsRow);

        body.add(Box.createVerticalGlue());

        // [NgocTrinh] UC-01: Khu vực chọn Kích thước bàn cờ
        body.add(centerComponent(sectionLabel("KÍCH THƯỚC BÀN CỜ")));
        body.add(vgap(20));
        body.add(centerComponent(buildSizeSelector()));

        body.add(Box.createVerticalGlue());

        // [NgocTrinh] UC-01: Khu vực chọn Độ khó của AI
        body.add(centerComponent(sectionLabel("ĐỘ KHÓ AI")));
        body.add(vgap(20));
        body.add(centerComponent(buildDifficultySelector()));

        body.add(Box.createVerticalGlue());

        // [NgocTrinh] UC-01: Khu vực thiết lập Thời gian đếm ngược
        body.add(centerComponent(sectionLabel("CÀI ĐẶT THỜI GIAN")));
        body.add(vgap(20));
        body.add(centerComponent(buildTimerModeRow()));
        body.add(vgap(15));
        body.add(centerComponent(buildTimeConfigRow()));

        body.add(Box.createVerticalGlue());

        // [NgocTrinh] UC-01: Nút xác nhận bắt đầu tạo ván
        body.add(centerComponent(buildStartButton()));
        body.add(Box.createVerticalStrut(50));

        root.add(body, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════
    //  Builder UI
    // ═══════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(30, 40, 0, 40));

        JLabel titleLbl = new JLabel("HEX GAME");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLbl.setForeground(TEXT_TITLE);

        JLabel hexIcon = new JLabel("\u2B21");
        hexIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 36));
        hexIcon.setForeground(ACCENT_BLUE);
        hexIcon.setBorder(new EmptyBorder(0, 0, 0, 15));

        JPanel leftGroup = new JPanel(new BorderLayout());
        leftGroup.setOpaque(false);
        leftGroup.add(hexIcon, BorderLayout.WEST);
        leftGroup.add(titleLbl, BorderLayout.CENTER);

        JButton closeBtn = new JButton("✕ THOÁT") {
            boolean hov = false;

            {
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font("Segoe UI", Font.BOLD, 18));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hov = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hov = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (hov) {
                    g2.setColor(new Color(240, 75, 75, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                    setForeground(new Color(220, 50, 50));
                } else {
                    setForeground(TEXT_MUTED);
                }

                super.paintComponent(g2);
                g2.dispose();
            }
        };

        closeBtn.setPreferredSize(new Dimension(140, 50));

        closeBtn.addActionListener(e -> {
            ok = false;
            dispose();
        });

        p.add(leftGroup, BorderLayout.WEST);
        p.add(closeBtn, BorderLayout.EAST);

        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private JPanel centerComponent(Component c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private JPanel buildSizeSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setOpaque(false);

        panel.add(new SizeButton(7));
        panel.add(new SizeButton(9));
        panel.add(new SizeButton(11));
        panel.add(new SizeButton(14));

        return panel;
    }

    private JPanel buildDifficultySelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setOpaque(false);

        panel.add(new DifficultyButton("Dễ", 1));
        panel.add(new DifficultyButton("Vừa", 2));
        panel.add(new DifficultyButton("Khó", 3));

        return panel;
    }

    private JPanel buildTimerModeRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setOpaque(false);

        panel.add(new TimerModeButton("Không dùng", GameTimer.Mode.NONE));
        panel.add(new TimerModeButton("Tổng ván", GameTimer.Mode.TOTAL_GAME));
        panel.add(new TimerModeButton("Mỗi nước", GameTimer.Mode.PER_MOVE));

        return panel;
    }

    private JPanel buildTimeConfigRow() {
        timeOptionsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        timeOptionsContainer.setOpaque(false);

        updateTimeOptionsPanel();

        return timeOptionsContainer;
    }

    private void updateTimeOptionsPanel() {
        timeOptionsContainer.removeAll();

        if (currentTimerMode == GameTimer.Mode.TOTAL_GAME) {
            timeOptionsContainer.add(new TimeValueButton("1 Phút", 60));
            timeOptionsContainer.add(new TimeValueButton("3 Phút", 180));
            timeOptionsContainer.add(new TimeValueButton("5 Phút", 300));
            timeOptionsContainer.add(new TimeValueButton("10 Phút", 600));
            timeOptionsContainer.setVisible(true);
        } else if (currentTimerMode == GameTimer.Mode.PER_MOVE) {
            timeOptionsContainer.add(new TimeValueButton("15 Giây", 15));
            timeOptionsContainer.add(new TimeValueButton("30 Giây", 30));
            timeOptionsContainer.add(new TimeValueButton("60 Giây", 60));
            timeOptionsContainer.add(new TimeValueButton("90 Giây", 90));
            timeOptionsContainer.setVisible(true);
        } else {
            timeOptionsContainer.setVisible(false);
        }
    }

    private JButton buildStartButton() {
        JButton btn = new JButton("BẮT ĐẦU TRẬN ĐẤU") {
            boolean hov = false;

            {
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hov = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hov = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int h = getHeight();

                g2.setColor(hov ? ACCENT_GREEN_H : ACCENT_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), h, h, h);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));

                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();

                g2.setColor(Color.WHITE);
                g2.drawString(
                        txt,
                        (getWidth() - fm.stringWidth(txt)) / 2,
                        (h + fm.getAscent() - fm.getDescent()) / 2
                );

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(450, 70);
            }
        };

        btn.addActionListener(e -> {
            ok = true;
            dispose();
        });

        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════
    private void selectCard(ModeCard card) {
        if (selectedCard != null) {
            selectedCard.selected = false;
            selectedCard.repaint();
        }

        card.selected = true;
        selectedCard = card;
        card.repaint();
    }

    private static Component vgap(int px) {
        return Box.createRigidArea(new Dimension(0, px));
    }

    private static Font resolveEmojiFont(int size) {
        String[] candidates = {
                "Segoe UI Emoji",
                "Apple Color Emoji",
                "Noto Emoji",
                Font.DIALOG
        };

        for (String name : candidates) {
            Font f = new Font(name, Font.PLAIN, size);

            if (!f.getFamily().equals("Dialog") || name.equals(Font.DIALOG)) {
                return f;
            }
        }

        return new Font(Font.DIALOG, Font.PLAIN, size);
    }

    /**
     * [NgocTrinh] UC-01: Hàm public duy nhất được gọi từ HexController.
     * Hàm này sẽ hiển thị UI, block luồng xử lý chờ người chơi cấu hình,
     * sau đó đóng gói mọi thông số vào Config và trả về.
     */
    public Config showDialog() {
        setVisible(true);

        if (!ok) {
            return null;
        }

        // [NgocTrinh] UC-01: Khởi tạo và ghi nhận toàn bộ giá trị đã chọn vào đối tượng DTO
        Config cfg = new Config();

        cfg.size = selectedSize;
        cfg.depth = selectedDepth;

        if (card1.selected) {
            cfg.mode = Mode.HUMAN_VS_AI;
        } else if (card2.selected) {
            cfg.mode = Mode.HUMAN_VS_HUMAN;
        } else {
            cfg.mode = Mode.AI_VS_AI;
        }

        cfg.timerMode = currentTimerMode;
        cfg.timerSeconds = currentTimerSeconds;

        return cfg; // [NgocTrinh] Trả kết quả về cho Controller để thực thi tạo ván đấu
    }
}
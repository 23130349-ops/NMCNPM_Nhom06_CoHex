package view;

import model.GameTimer;

import javax.swing.*;
import java.awt.*;

/**
 * SetupDialog – Hộp thoại chọn chế độ chơi trước khi bắt đầu ván mới.
 *
 * Liên quan đến Use Case:
 *   UC-07 Chọn chế độ chơi – Toàn bộ Standard flow 4.1.18 và Alternative flows
 *   UC-01 Khởi tạo ván mới – Pre-condition: người chơi đã chọn chế độ xong
 */
public class SetupDialog extends JDialog {

    /**
     * UC-07 – Các chế độ chơi được hỗ trợ (Standard flow 4.1.18, bước 3):
     *   HUMAN_VS_AI    – Người (Đỏ) vs Máy (Xanh)
     *   HUMAN_VS_HUMAN – Người vs Người
     *   AI_VS_AI       – Máy vs Máy
     */
    public enum Mode { HUMAN_VS_AI, HUMAN_VS_HUMAN, AI_VS_AI }

    /**
     * UC-07 – Cấu hình trả về sau khi người chơi xác nhận (Standard flow 4.1.18, bước 4).
     * HexController dùng Config để khởi tạo game và gán người chơi phù hợp.
     */
    public static class Config {
        public int  size; // Kích thước bàn cờ (7 / 9 / 11 / 13)
        public Mode mode; // Chế độ chơi được chọn
        public GameTimer.Mode timerMode = GameTimer.Mode.NONE;
        public int timerSeconds = 300; // Mặc định 5 phút
    }

    private final JComboBox<Integer> sizeBox;
    private final JRadioButton rb1, rb2, rb3;
    private boolean ok = false;

    // Giao diện chọn Timer
    private JRadioButton rbTimerNone;
    private JRadioButton rbTimerTotal;
    private JRadioButton rbTimerPerMove;
    private JComboBox<String> timeValueBox;
    private JPanel timeConfigSubPanel;

    /**
     * UC-07 – Chọn chế độ chơi (Standard flow 4.1.18, bước 2):
     * Hệ thống hiển thị danh sách chế độ chơi và kích thước bàn cờ.
     *
     * Giao diện gồm:
     *   - ComboBox chọn kích thước bàn: 7×7, 9×9, 11×11 (mặc định), 13×13
     *   - RadioButton chọn chế độ: Người vs AI / Người vs Người / Máy vs Máy
     *   - Nút "Bắt đầu" để xác nhận
     *
     * @param owner cửa sổ cha (null nếu chưa có frame)
     */
    public SetupDialog(Frame owner) {
        super(owner, "Chọn chế độ chơi", true);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // UC-07: Chọn kích thước bàn cờ
        sizeBox = new JComboBox<>(new Integer[]{7, 9, 11, 13});
        sizeBox.setSelectedItem(11); // Mặc định 11×11
        panel.add(new JLabel("Kích thước bàn:"));
        panel.add(sizeBox);

        // UC-07 bước 3: Các lựa chọn chế độ chơi
        rb1 = new JRadioButton("Người (Đỏ) vs Máy (Xanh)", true); // Mặc định
        rb2 = new JRadioButton("Người vs Người");
        rb3 = new JRadioButton("Máy vs Máy");
        ButtonGroup group = new ButtonGroup();
        group.add(rb1);
        group.add(rb2);
        group.add(rb3);

        panel.add(new JLabel("Chế độ:"));
        panel.add(rb1);
        panel.add(rb2);
        panel.add(rb3);

        // Tích hợp bộ chọn Timer
        panel.add(new JLabel("Đồng hồ tính giờ:"));

        rbTimerNone = new JRadioButton("Không dùng", true);
        rbTimerTotal = new JRadioButton("Tổng ván");
        rbTimerPerMove = new JRadioButton("Mỗi nước");
        ButtonGroup timerGroup = new ButtonGroup();
        timerGroup.add(rbTimerNone);
        timerGroup.add(rbTimerTotal);
        timerGroup.add(rbTimerPerMove);

        JPanel timerRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timerRadioPanel.add(rbTimerNone);
        timerRadioPanel.add(rbTimerTotal);
        timerRadioPanel.add(rbTimerPerMove);
        panel.add(timerRadioPanel);

        timeConfigSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeConfigSubPanel.add(new JLabel("Thời gian:"));
        timeValueBox = new JComboBox<>();
        timeConfigSubPanel.add(timeValueBox);
        timeConfigSubPanel.setVisible(false);
        panel.add(timeConfigSubPanel);

        rbTimerNone.addActionListener(e -> {
            timeConfigSubPanel.setVisible(false);
            pack();
        });
        rbTimerTotal.addActionListener(e -> {
            timeConfigSubPanel.setVisible(true);
            timeValueBox.setModel(new DefaultComboBoxModel<>(new String[]{
                    "1 phút", "3 phút", "5 phút (Mặc định)", "10 phút"
            }));
            timeValueBox.setSelectedIndex(2);
            pack();
        });
        rbTimerPerMove.addActionListener(e -> {
            timeConfigSubPanel.setVisible(true);
            timeValueBox.setModel(new DefaultComboBoxModel<>(new String[]{
                    "15 giây", "30 giây (Mặc định)", "60 giây", "90 giây"
            }));
            timeValueBox.setSelectedIndex(1);
            pack();
        });

        add(panel, BorderLayout.CENTER);

        // UC-07 bước 4: Nút "Bắt đầu" ghi nhận lựa chọn và đóng dialog
        JButton okBtn = new JButton("Bắt đầu");
        okBtn.addActionListener(e -> {
            ok = true;
            dispose();
        });
        add(okBtn, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * UC-07 – Hiển thị dialog và trả về cấu hình người chơi đã chọn.
     *
     * Standard flow 4.1.18:
     *   - Bước 3: Người chơi chọn chế độ và bấm "Bắt đầu" → ok = true
     *   - Bước 4: Trả về Config với size và mode đã chọn
     *
     * Alternative flow 4.1.19 – Người chơi không chọn chế độ (đóng dialog):
     *   ok vẫn = false → trả về null
     *   → HexController nhận null → System.exit(0)
     *
     * Alternative flow 4.1.20 – Người chơi thay đổi chế độ chơi:
     *   Người chơi có thể thay đổi RadioButton bất kỳ lúc nào trước khi bấm "Bắt đầu"
     *   → showDialog() luôn đọc giá trị RadioButton tại thời điểm bấm OK
     *
     * @return Config với kích thước và chế độ chơi, hoặc null nếu người dùng hủy
     */
    public Config showDialog() {
        setVisible(true);
        // Alternative flow 4.1.19: Đóng dialog mà không bấm "Bắt đầu"
        if (!ok) return null;

        // UC-07 bước 4: Đọc và trả về cấu hình đã chọn
        Config cfg  = new Config();
        cfg.size    = (Integer) sizeBox.getSelectedItem();
        if      (rb1.isSelected()) cfg.mode = Mode.HUMAN_VS_AI;
        else if (rb2.isSelected()) cfg.mode = Mode.HUMAN_VS_HUMAN;
        else                       cfg.mode = Mode.AI_VS_AI;

        // Đăng ký cấu hình Timer
        if (rbTimerNone.isSelected()) {
            cfg.timerMode = GameTimer.Mode.NONE;
        } else if (rbTimerTotal.isSelected()) {
            cfg.timerMode = GameTimer.Mode.TOTAL_GAME;
            String selected = (String) timeValueBox.getSelectedItem();
            if (selected.contains("1 phút")) cfg.timerSeconds = 60;
            else if (selected.contains("3 phút")) cfg.timerSeconds = 180;
            else if (selected.contains("5 phút")) cfg.timerSeconds = 300;
            else if (selected.contains("10 phút")) cfg.timerSeconds = 600;
        } else if (rbTimerPerMove.isSelected()) {
            cfg.timerMode = GameTimer.Mode.PER_MOVE;
            String selected = (String) timeValueBox.getSelectedItem();
            if (selected.contains("15 giây")) cfg.timerSeconds = 15;
            else if (selected.contains("30 giây")) cfg.timerSeconds = 30;
            else if (selected.contains("60 giây")) cfg.timerSeconds = 60;
            else if (selected.contains("90 giây")) cfg.timerSeconds = 90;
        }

        return cfg;
    }
}
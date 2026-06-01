package view;

import javax.swing.*;
import java.awt.*;

/**
 * SetupDialog – Hộp thoại chọn chế độ chơi trước khi bắt đầu ván mới.
 *
 * Liên quan đến Use Case:
 * UC-07 Chọn chế độ chơi – Toàn bộ Standard flow 4.1.18 và Alternative flows
 * UC-01 Khởi tạo ván mới – Pre-condition: người chơi đã chọn chế độ xong
 */
public class SetupDialog extends JDialog {

    /**
     * UC-07 – Các chế độ chơi được hỗ trợ (Standard flow 4.1.18, bước 3):
     * HUMAN_VS_AI – Người (Đỏ) vs Máy (Xanh)
     * HUMAN_VS_HUMAN – Người vs Người
     * AI_VS_AI – Máy vs Máy
     */
    public enum Mode {
        HUMAN_VS_AI, HUMAN_VS_HUMAN, AI_VS_AI
    }

    /**
     * UC-07 – Cấu hình trả về sau khi người chơi xác nhận (Standard flow 4.1.18,
     * bước 4).
     * HexController dùng Config để khởi tạo game và gán người chơi phù hợp.
     */
    public static class Config {
        public int size; // Kích thước bàn cờ
        public Mode mode; // Chế độ chơi được chọn
        public int depth; // Độ khó AI: 1 dễ, 2 vừa, 3 khó
    }

    private final JComboBox<Integer> sizeBox;
    private final JComboBox<String> difficultyBox;
    private final JRadioButton rb1, rb2, rb3;
    private boolean ok = false;

    /**
     * UC-07 – Chọn chế độ chơi (Standard flow 4.1.18, bước 2):
     * Hệ thống hiển thị danh sách chế độ chơi và kích thước bàn cờ.
     *
     * Giao diện gồm:
     * - ComboBox chọn kích thước bàn: 7×7, 9×9, 11×11 (mặc định), 13×13
     * - RadioButton chọn chế độ: Người vs AI / Người vs Người / Máy vs Máy
     * - Nút "Bắt đầu" để xác nhận
     *
     * @param owner cửa sổ cha (null nếu chưa có frame)
     */
    public SetupDialog(Frame owner) {
        super(owner, "Chọn chế độ chơi", true);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // UC-07: Chọn kích thước bàn cờ
        sizeBox = new JComboBox<>(new Integer[] { 7, 9, 11, 14 });
        sizeBox.setSelectedItem(11); // Mặc định 11×11
        panel.add(new JLabel("Kích thước bàn:"));
        panel.add(sizeBox);

        difficultyBox = new JComboBox<>(new String[] {
                "Dễ - depth = 1",
                "Vừa - depth = 2",
                "Khó - depth = 3"
        });
        difficultyBox.setSelectedIndex(2); // Mặc định Khó

        panel.add(new JLabel("Độ khó AI:"));
        panel.add(difficultyBox);

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
     * - Bước 3: Người chơi chọn chế độ và bấm "Bắt đầu" → ok = true
     * - Bước 4: Trả về Config với size và mode đã chọn
     *
     * Alternative flow 4.1.19 – Người chơi không chọn chế độ (đóng dialog):
     * ok vẫn = false → trả về null
     * → HexController nhận null → System.exit(0)
     *
     * Alternative flow 4.1.20 – Người chơi thay đổi chế độ chơi:
     * Người chơi có thể thay đổi RadioButton bất kỳ lúc nào trước khi bấm "Bắt đầu"
     * → showDialog() luôn đọc giá trị RadioButton tại thời điểm bấm OK
     *
     * @return Config với kích thước và chế độ chơi, hoặc null nếu người dùng hủy
     */
    public Config showDialog() {
        setVisible(true);
        // Alternative flow 4.1.19: Đóng dialog mà không bấm "Bắt đầu"
        if (!ok)
            return null;

        // UC-07 bước 4: Đọc và trả về cấu hình đã chọn
        Config cfg = new Config();

        cfg.size = (Integer) sizeBox.getSelectedItem();

        if (rb1.isSelected())
            cfg.mode = Mode.HUMAN_VS_AI;
        else if (rb2.isSelected())
            cfg.mode = Mode.HUMAN_VS_HUMAN;
        else
            cfg.mode = Mode.AI_VS_AI;

        int selectedDifficulty = difficultyBox.getSelectedIndex();

        if (selectedDifficulty == 0) {
            cfg.depth = 1; // Dễ
        } else if (selectedDifficulty == 1) {
            cfg.depth = 2; // Vừa
        } else {
            cfg.depth = 3; // Khó
        }

        return cfg;
    }
}
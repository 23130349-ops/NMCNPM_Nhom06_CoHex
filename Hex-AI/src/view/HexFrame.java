package view;

import javax.swing.*;
import java.awt.*;

/**
 * HexFrame – Cửa sổ chính chứa toàn bộ giao diện trò chơi.
 */
public class HexFrame extends JFrame {
    private final HexPanel panel;
    private final JButton undoButton;
    private final JTextArea historyArea;

    // TÍNH NĂNG MỚI: Menu File – Save / Load
    private final JMenuItem saveMenuItem;
    private final JMenuItem loadMenuItem;

    private JLabel redLabel;
    private JLabel blueLabel;
    
    // Nút lưu/tải của nhóm
    private JButton btnSave;
    private JButton btnLoad;

    public HexFrame(int n) {
        setTitle("Hex Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 1000); // Mở rộng không gian hiển thị cho panel lịch sử nước đi
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Tạo thanh JMenuBar với menu File ---
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        saveMenuItem = new JMenuItem("Save Game (File)");
        loadMenuItem = new JMenuItem("Load Game (File)");
        fileMenu.add(saveMenuItem);
        fileMenu.add(loadMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        // --- Kết thúc phần Menu ---

        // --- Toolbar phía trên (Chứa nút điều khiển) ---
        JPanel controlPanel = new JPanel();
        undoButton = new JButton("Hoàn nước (Undo)");
        btnSave = new JButton("Lưu nhanh (Quick Save)");
        btnLoad = new JButton("Tải nhanh (Quick Load)");
        
        controlPanel.add(undoButton);
        controlPanel.add(btnSave);
        controlPanel.add(btnLoad);

        // Hiển thị thời gian
        redLabel = new JLabel("RED: 10:00");
        redLabel.setForeground(Color.RED);
        blueLabel = new JLabel("BLUE: 10:00");
        blueLabel.setForeground(Color.BLUE);
        controlPanel.add(new JLabel("  |  "));
        controlPanel.add(redLabel);
        controlPanel.add(new JLabel("  -  "));
        controlPanel.add(blueLabel);
        
        add(controlPanel, BorderLayout.NORTH);

        // --- Bàn cờ trung tâm ---
        panel = new HexPanel(n);
        add(panel, BorderLayout.CENTER);

        // --- Thanh Panel bên phải: Hiển thị danh sách các nước đã đi ---
        historyArea = new JTextArea(20, 25);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lịch sử đánh"));
        add(scrollPane, BorderLayout.EAST);

        setVisible(true);
    }

    public HexPanel getPanel() { return panel; }
    public JButton getUndoButton() { return undoButton; }
    
    public JButton getBtnSave() { return btnSave; }
    public JButton getBtnLoad() { return btnLoad; }

    /** Cập nhật danh sách text hiển thị lịch sử */
    public void updateHistoryText(String text) {
        historyArea.setText(text);
    }

    /**
     * TÍNH NĂNG MỚI: Trả về menu item Save để HexController đăng ký sự kiện.
     */
    public JMenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    /**
     * TÍNH NĂNG MỚI: Trả về menu item Load để HexController đăng ký sự kiện.
     */
    public JMenuItem getLoadMenuItem() {
        return loadMenuItem;
    }

    public void setTimerValues(int redSecs, int blueSecs) {
        redLabel.setText(String.format("RED: %02d:%02d", redSecs / 60, redSecs % 60));
        blueLabel.setText(String.format("BLUE: %02d:%02d", blueSecs / 60, blueSecs % 60));
    }

    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
}
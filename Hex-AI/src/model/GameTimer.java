package model;

import javax.swing.Timer;

/**
 * GameTimer – Bộ máy đếm ngược thời gian.
 */
public class GameTimer {

    /**
     * Các chế độ tính giờ được hỗ trợ:
     *   NONE       – Không áp dụng giới hạn thời gian.
     *   TOTAL_GAME – Tổng thời gian cho toàn bộ ván đấu.
     *   PER_MOVE   – Giới hạn thời gian tối đa cho mỗi nước đi.
     */
    public enum Mode { NONE, TOTAL_GAME, PER_MOVE }

    // Interface nhận sự kiện phản hồi từ Timer về Controller.
    public interface Listener {
        /** Gọi mỗi giây để cập nhật nhãn thời gian trên UI. */
        void onTick(int redSeconds, int blueSeconds);

        /**
         * Kích hoạt khi thời gian của một người chơi về 0.
         * Khởi động luồng báo thua cuộc.
         */
        void onTimeout(int player);
    }

    private final Mode mode;
    private final int initialSeconds;
    private int redSeconds;
    private int blueSeconds;
    private int activePlayer = -1;
    private final Timer swingTimer;
    private Listener listener;
    private boolean skipNextReset = false;

    /**
     * Khởi tạo bộ đếm ngược thời gian.
     *
     * @param mode           Chế độ tính giờ
     * @param initialSeconds Quỹ thời gian ban đầu của mỗi bên (giây)
     */
    public GameTimer(Mode mode, int initialSeconds) {
        this.mode = mode;
        this.initialSeconds = initialSeconds;
        this.redSeconds = initialSeconds;
        this.blueSeconds = initialSeconds;

        // Tần suất 1 giây (1000ms) trên Event Dispatch Thread (EDT)
        this.swingTimer = new Timer(1000, e -> tick());
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Chuyển đổi trạng thái đếm ngược khi đổi lượt.
     */
    public void switchTo(int player) {
        if (mode == Mode.NONE) return;

        if (mode == Mode.PER_MOVE) {
            if (skipNextReset) {
                skipNextReset = false;
            } else {
                if (player == HexGame.RED) {
                    redSeconds = initialSeconds;
                } else {
                    blueSeconds = initialSeconds;
                }
            }
            if (listener != null) {
                listener.onTick(redSeconds, blueSeconds);
            }
        }

        this.activePlayer = player;
        if (!swingTimer.isRunning()) {
            swingTimer.start();
        }
    }

    /** Tạm dừng đếm giờ (khi hiện các Dialog kết thúc game, chọn chế độ chơi lại). */
    public void pause() {
        swingTimer.stop();
    }

    /** Đặt lại thời gian về trạng thái ban đầu khi bắt đầu ván mới. */
    public void reset() {
        swingTimer.stop();
        this.activePlayer = -1;
        this.redSeconds = initialSeconds;
        this.blueSeconds = initialSeconds;
        if (listener != null) {
            listener.onTick(redSeconds, blueSeconds);
        }
    }

    public int getRedSeconds() { return redSeconds; }
    public int getBlueSeconds() { return blueSeconds; }
    public Mode getMode() { return mode; }

    /**
     * Cập nhật lại thời gian còn lại của hai người chơi (sử dụng khi tải/load game).
     */
    public void setRemainingSeconds(int redSeconds, int blueSeconds) {
        this.redSeconds = redSeconds;
        this.blueSeconds = blueSeconds;
        this.skipNextReset = true;
        if (listener != null) {
            listener.onTick(redSeconds, blueSeconds);
        }
    }

    /** Khôi phục đếm giờ sau khi tạm dừng (dùng sau khi đóng Dialog lưu game). */
    public void resume() {
        if (mode == Mode.NONE || activePlayer == -1) return;
        if (!swingTimer.isRunning()) {
            swingTimer.start();
        }
    }

    /**
     * Trừ thời gian sau mỗi giây trôi qua.
     * Một bên hết giờ (Timeout).
     */
    private void tick() {
        if (activePlayer == HexGame.RED) {
            redSeconds--;
            if (redSeconds <= 0) {
                redSeconds = 0;
                swingTimer.stop();
                if (listener != null) {
                    listener.onTick(redSeconds, blueSeconds);
                    listener.onTimeout(HexGame.RED);
                }
            }
        } else if (activePlayer == HexGame.BLUE) {
            blueSeconds--;
            if (blueSeconds <= 0) {
                blueSeconds = 0;
                swingTimer.stop();
                if (listener != null) {
                    listener.onTick(redSeconds, blueSeconds);
                    listener.onTimeout(HexGame.BLUE);
                }
            }
        }

        if (listener != null && swingTimer.isRunning()) {
            listener.onTick(redSeconds, blueSeconds);
        }
    }
}

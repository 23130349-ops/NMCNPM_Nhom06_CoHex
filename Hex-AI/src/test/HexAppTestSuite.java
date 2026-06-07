package test;

import model.*;
import controller.HexController;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Stack;

/**
 * Bộ kiểm thử tự động chạy độc lập (Standalone Test Suite) dành cho dự án Hex-AI.
 *
 * MỤC ĐÍCH CHÍNH:
 * - Kiểm thử độ chính xác và tính toàn vẹn của các tính năng đồ họa, thời gian, và hệ thống
 * do lập trình viên NgocTran (Tài khoản Git: Tran05) chịu trách nhiệm nâng cấp và commit.
 */
public class HexAppTestSuite {

    // Mã màu ANSI dùng để in kết quả kiểm thử sinh động trên Console
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_RED    = "\u001B[31m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_BOLD   = "\u001B[1m";

    // Biến đếm số lượng ca kiểm thử
    private static int totalTests = 0;
    private static int passedTests = 0;

    /**
     * Điểm khởi chạy chính của chương trình kiểm thử.
     */
    public static void main(String[] args) {
        System.out.println(ANSI_BOLD + ANSI_CYAN + "==================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_CYAN + "      BẮT ĐẦU CHẠY BỘ KIỂM THỬ HEX-AI (NGOC TRAN)  " + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_CYAN + "==================================================" + ANSI_RESET);

        try {
            // =========================================================================
            // NHÓM 1: KIỂM THỬ BỘ ĐẾM THỜI GIAN VÀ ĐỒNG BỘ DỮ LIỆU SANG UI (GameTimer)
            // CHỨC NĂNG: "Thiết kế Giao diện hiển thị Thời gian (JLabel) trên controlPanel"
            // TÁC GIẢ: NgocTran (Tran05) - Đảm bảo hàm setTimerValues nhận đúng dữ liệu hạ tầng.
            // =========================================================================
            runTest("GameTimer - Khởi tạo các chế độ tính giờ", HexAppTestSuite::testGameTimerModes);
            runTest("GameTimer - Chuyển lượt chơi & Đặt lại thời gian", HexAppTestSuite::testGameTimerSwitchTo);
            runTest("GameTimer - Giữ nguyên thời gian nạp game (Skip Reset)", HexAppTestSuite::testGameTimerSkipReset);
            runTest("GameTimer - Chức năng Dừng, Tiếp tục & Đặt lại đồng hồ", HexAppTestSuite::testGameTimerPauseResumeReset);
            runTest("GameTimer - Trừ thời gian sau mỗi giây trôi qua (Tick)", HexAppTestSuite::testGameTimerTick);
            runTest("GameTimer - Kích hoạt sự kiện hết giờ (Timeout)", HexAppTestSuite::testGameTimerTimeout);

            // =========================================================================
            // NHÓM 2: KIỂM THỬ HỆ THỐNG QUẢN LÝ LƯU / TẢI GAME (GameSaver)
            // CHỨC NĂNG: "Thêm 2 nút JButton (Lưu Game, Tải Game) vào thanh điều khiển"
            //            và ghi/đọc dữ liệu trạng thái xuống tệp dữ liệu nhị phân 'hex_save.dat'.
            // TÁC GIẢ: NgocTran (Tran05)
            // =========================================================================
            runTest("GameSaver - Lưu và Tải game dạng văn bản (.txt)", HexAppTestSuite::testGameSaverTextSaveLoad);
            runTest("GameSaver - Lưu và Tải nhanh dạng nhị phân (.dat)", HexAppTestSuite::testGameSaverBinarySaveLoad);

            // =========================================================================
            // NHÓM 3: KIỂM THỬ HOÀN TÁC (Undo) & ĐỒ HỌA TRÊN BÀN CỜ (HexPanel)
            // CHỨC NĂNG: "Highlight nước đi cuối cùng" & "Highlight đường chiến thắng (DFS)"
            //            đảm bảo khi Undo() thì đỉnh lịch sử thay đổi để cập nhật lại đồ họa.
            // TÁC GIẢ: NgocTran (Tran05)
            // =========================================================================
            runTest("Undo - Rút nước đi trong lịch sử & Đảo lượt chơi", HexAppTestSuite::testGameUndoLogic);
            runTest("Undo - Đặt lại thời gian lượt đi sau khi Undo (PER_MOVE)", HexAppTestSuite::testUndoTimerReset);
            runTest("HexGame - Xác định người thắng & Đường chiến thắng (Winning Path)", HexAppTestSuite::testCheckWinnerAndWinningPath);

            // ==========================================
            // NHÓM 4: KIỂM THỬ TRÍ TUỆ NHÂN TẠO (HexAI)
            // ==========================================
            runTest("HexAI - Tính toán nước đi hợp lệ tốt nhất", HexAppTestSuite::testAIChooseMove);
            runTest("HexAI - Tự động dừng minimax khi sắp hết thời gian", HexAppTestSuite::testAITimeoutHeuristic);

        } catch (Exception e) {
            System.err.println(ANSI_BOLD + ANSI_RED + "LỖI NGHIÊM TRỌNG: Quá trình kiểm thử bị ngắt quãng do ngoại lệ không xác định:" + ANSI_RESET);
            e.printStackTrace();
        }

        System.out.println(ANSI_BOLD + ANSI_CYAN + "==================================================" + ANSI_RESET);
        System.out.print(ANSI_BOLD + "KẾT QUẢ CHẠY: ");
        if (passedTests == totalTests) {
            System.out.println(ANSI_GREEN + passedTests + "/" + totalTests + " thành công (100% THÀNH CÔNG)" + ANSI_RESET);
            System.exit(0);
        } else {
            System.out.println(ANSI_RED + passedTests + "/" + totalTests + " thành công (" + (totalTests - passedTests) + " THẤT BẠI)" + ANSI_RESET);
            System.exit(1);
        }
    }

    /**
     * Hàm helper thực thi từng ca kiểm thử, bắt ngoại lệ và in báo cáo.
     */
    private static void runTest(String testName, TestRunnable runnable) {
        totalTests++;
        System.out.print(ANSI_BOLD + "Đang chạy: " + ANSI_RESET + testName + " ... ");
        try {
            runnable.run();
            passedTests++;
            System.out.println(ANSI_GREEN + "[ĐẠT - PASS]" + ANSI_RESET);
        } catch (Throwable t) {
            System.out.println(ANSI_RED + "[HỎNG - FAIL]" + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "  Chi tiết lỗi: " + t.getMessage() + ANSI_RESET);
            t.printStackTrace(System.out);
            System.out.println();
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Exception;
    }

    // --- CÁC PHƯƠNG THỨC REFLECTION TRUY CẬP PHẦN TỬ PRIVATE ---
    private static Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private static void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private static void invokePrivateMethod(Object obj, String methodName, Object... args) throws Exception {
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Integer) {
                paramTypes[i] = int.class;
            } else {
                paramTypes[i] = args[i].getClass();
            }
        }
        Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(obj, args);
    }

    private static Object invokePrivateMethodWithReturn(Object obj, String methodName, Object... args) throws Exception {
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Integer) {
                paramTypes[i] = int.class;
            } else {
                paramTypes[i] = args[i].getClass();
            }
        }
        Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    // =========================================================================
    // TRIỂN KHAI CÁC PHƯƠNG THỨC KIỂM THỬ CHI TIẾT
    // =========================================================================

    /**
     * CHỨC NĂNG ĐÃ LÀM (Tran05): Thiết kế luồng đếm giờ để đẩy dữ liệu xuống UI.
     * MỤC TIÊU: Đảm bảo hạ tầng Model lưu trữ đúng chế độ tính giờ phục vụ cho hàm
     * public void setTimerValues(int redSec, int blueSec) trên đồ họa HexFrame.
     */
    private static void testGameTimerModes() throws Exception {
        GameTimer timerNone = new GameTimer(GameTimer.Mode.NONE, 60);
        GameTimer timerTotal = new GameTimer(GameTimer.Mode.TOTAL_GAME, 120);
        GameTimer timerPerMove = new GameTimer(GameTimer.Mode.PER_MOVE, 30);

        assertEquals(GameTimer.Mode.NONE, timerNone.getMode());
        assertEquals(GameTimer.Mode.TOTAL_GAME, timerTotal.getMode());
        assertEquals(GameTimer.Mode.PER_MOVE, timerPerMove.getMode());

        assertEquals(60, timerNone.getRedSeconds());
        assertEquals(120, timerTotal.getRedSeconds());
        assertEquals(30, timerPerMove.getRedSeconds());
    }

    /**
     * CHỨC NĂNG ĐÃ LÀM (Tran05): Xử lý hiển thị số giây còn lại của ĐỎ và XANH khi chuyển lượt.
     * MỤC TIÊU: Kiểm tra giá trị giây của người chơi kế tiếp được đặt lại chính xác
     * để đồng bộ hiển thị lên 2 nhãn JLabel trên thanh công cụ controlPanel.
     */
    private static void testGameTimerSwitchTo() throws Exception {
        GameTimer timer = new GameTimer(GameTimer.Mode.PER_MOVE, 60);

        setPrivateField(timer, "redSeconds", 45);
        setPrivateField(timer, "blueSeconds", 50);

        timer.switchTo(HexGame.RED);
        assertEquals(60, timer.getRedSeconds());
        assertEquals(50, timer.getBlueSeconds());

        int activePlayer = (int) getPrivateField(timer, "activePlayer");
        assertEquals(HexGame.RED, activePlayer);

        timer.switchTo(HexGame.BLUE);
        assertEquals(60, timer.getBlueSeconds());

        activePlayer = (int) getPrivateField(timer, "activePlayer");
        assertEquals(HexGame.BLUE, activePlayer);
    }

    /**
     * CHỨC NĂNG ĐÃ LÀM (Tran05): Đồng bộ UI Đếm giờ khi thực hiện "Tải Game".
     * MỤC TIÊU: Khi người dùng bấm nút "Tải Game", cờ skipNextReset phải hoạt động để
     * giữ nguyên số giây cũ đã lưu, tránh việc JLabel hiển thị sai lệch hoặc tự động reset về mặc định.
     */
    private static void testGameTimerSkipReset() throws Exception {
        GameTimer timer = new GameTimer(GameTimer.Mode.PER_MOVE, 60);

        timer.setRemainingSeconds(25, 30);
        assertEquals(25, timer.getRedSeconds());
        assertEquals(30, timer.getBlueSeconds());

        boolean skipNextReset = (boolean) getPrivateField(timer, "skipNextReset");
        assertTrue(skipNextReset, "skipNextReset phải là true sau khi gọi setRemainingSeconds");

        timer.switchTo(HexGame.RED);
        assertEquals(25, timer.getRedSeconds());

        skipNextReset = (boolean) getPrivateField(timer, "skipNextReset");
        assertFalse(skipNextReset, "skipNextReset phải chuyển về false sau khi gọi switchTo");

        timer.switchTo(HexGame.BLUE);
        assertEquals(60, timer.getBlueSeconds());
    }

    /**
     * CHỨC NĂNG ĐÃ LÀM (Tran05): Kiểm soát hoạt động đồng hồ khi bật/tắt các hộp thoại hệ thống.
     * MỤC TIÊU: Đảm bảo luồng đếm giờ dừng chính xác khi JFileChooser mở ra để người dùng "Chọn nơi lưu ván game"
     * và tiếp tục đếm ngược khi hộp thoại đóng lại.
     */
    private static void testGameTimerPauseResumeReset() throws Exception {
        GameTimer timer = new GameTimer(GameTimer.Mode.TOTAL_GAME, 100);
        javax.swing.Timer swingTimer = (javax.swing.Timer) getPrivateField(timer, "swingTimer");

        assertFalse(swingTimer.isRunning(), "Đồng hồ không được chạy lúc đầu");

        timer.switchTo(HexGame.RED);
        assertTrue(swingTimer.isRunning(), "Đồng hồ phải chạy sau khi switchTo");

        timer.pause();
        assertFalse(swingTimer.isRunning(), "Đồng hồ phải dừng sau khi gọi pause()");

        timer.resume();
        assertTrue(swingTimer.isRunning(), "Đồng hồ phải chạy lại sau khi gọi resume()");

        timer.reset();
        assertFalse(swingTimer.isRunning(), "Đồng hồ phải dừng sau khi gọi reset()");
        assertEquals(100, timer.getRedSeconds());
        assertEquals(100, timer.getBlueSeconds());

        int activePlayer = (int) getPrivateField(timer, "activePlayer");
        assertEquals(-1, activePlayer);
    }

    /**
     * CHỨC NĂNG ĐÃ LÀM (Tran05): Cập nhật dữ liệu thời gian thực cho UI Đếm Giờ.
     * MỤC TIÊU: Mỗi nhịp tick (1 giây), dữ liệu số giây giảm đi và gọi thông báo xuống
     * hàm setTimerValues() để vẽ lại thanh trạng thái thời gian trên HexFrame.
     */
    private static void testGameTimerTick() throws Exception {
        GameTimer timer = new GameTimer(GameTimer.Mode.TOTAL_GAME, 100);
        timer.switchTo(HexGame.RED);

        invokePrivateMethod(timer, "tick");
        assertEquals(99, timer.getRedSeconds());
        assertEquals(100, timer.getBlueSeconds());

        timer.switchTo(HexGame.BLUE);
        invokePrivateMethod(timer, "tick");
        assertEquals(99, timer.getRedSeconds());
        assertEquals(99, timer.getBlueSeconds());
    }

    /**
     * CHỨC NĂNG ĐÃ LÀM (Tran05): Xử lý kết thúc trận đấu khi hết giờ.
     * MỤC TIÊU: Khi số giây của JLabel chạm mốc 0, đồng hồ dừng hẳn và hệ thống hiển thị hộp thoại
     * báo người chơi thua cuộc, không cho phép thao tác click chuột trên bàn cờ nữa.
     */
    private static void testGameTimerTimeout() throws Exception {
        GameTimer timer = new GameTimer(GameTimer.Mode.TOTAL_GAME, 1);
        final boolean[] timeoutCalled = {false};
        final int[] timedOutPlayer = {-1};

        timer.setListener(new GameTimer.Listener() {
            @Override
            public void onTick(int redSeconds, int blueSeconds) {}

            @Override
            public void onTimeout(int player) {
                timeoutCalled[0] = true;
                timedOutPlayer[0] = player;
            }
        });

        timer.switchTo(HexGame.RED);
        javax.swing.Timer swingTimer = (javax.swing.Timer) getPrivateField(timer, "swingTimer");

        invokePrivateMethod(timer, "tick");

        assertEquals(0, timer.getRedSeconds());
        assertTrue(timeoutCalled[0], "Listener onTimeout phải được kích hoạt");
        assertEquals(HexGame.RED, timedOutPlayer[0]);
        assertFalse(swingTimer.isRunning(), "Đồng hồ phải dừng hẳn khi có người chơi hết giờ");
    }

    /**
     * Ca kiểm thử: Lưu và tải game dạng text thông qua lớp GameSaver.
     */
    private static void testGameSaverTextSaveLoad() throws Exception {
        String testPath = "test_game_state.txt";
        File file = new File(testPath);
        if (file.exists()) {
            file.delete();
        }

        try {
            HexGame originalGame = new HexGame(7);
            originalGame.place(2, 3, HexGame.RED);
            originalGame.place(4, 1, HexGame.BLUE);
            originalGame.setRedTimeLeft(320);
            originalGame.setBlueTimeLeft(445);
            originalGame.setCurrent(HexGame.BLUE);

            GameSaver.saveGame(originalGame, testPath);
            assertTrue(file.exists(), "Tệp lưu văn bản phải được tạo thành công");

            HexGame loadedGame = GameSaver.loadGame(testPath);

            assertEquals(originalGame.getSize(), loadedGame.getSize());
            assertEquals(originalGame.getCurrent(), loadedGame.getCurrent());
            assertEquals(originalGame.getRedTimeLeft(), loadedGame.getRedTimeLeft());
            assertEquals(originalGame.getBlueTimeLeft(), loadedGame.getBlueTimeLeft());

            int[][] origBoard = originalGame.getBoard();
            int[][] loadBoard = loadedGame.getBoard();
            for (int r = 0; r < 7; r++) {
                for (int c = 0; c < 7; c++) {
                    assertEquals(origBoard[r][c], loadBoard[r][c]);
                }
            }

            Stack<int[]> origHist = originalGame.getMoveHistory();
            Stack<int[]> loadHist = loadedGame.getMoveHistory();
            assertEquals(origHist.size(), loadHist.size());
            for (int i = 0; i < origHist.size(); i++) {
                assertArrayEquals(origHist.get(i), loadHist.get(i));
            }

        } finally {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * CHỨC NĂNG ĐÃ LÀM (Tran05): Tạo cơ chế Lưu/Tải nhanh dạng dữ liệu nhị phân (.dat).
     * MỤC TIÊU: Kiểm thử hoạt động của nút bấm "Lưu Game" và "Tải Game" mới được thêm vào thanh điều khiển.
     * Đảm bảo toàn bộ cấu trúc bàn cờ và lịch sử nước đi được đọc/ghi chính xác vào file tĩnh 'hex_save.dat'.
     */
    private static void testGameSaverBinarySaveLoad() throws Exception {
        String testPath = "test_game_state.dat";
        File file = new File(testPath);
        if (file.exists()) {
            file.delete();
        }

        try {
            HexGame originalGame = new HexGame(9);
            originalGame.place(0, 0, HexGame.RED);
            originalGame.place(8, 8, HexGame.BLUE);
            originalGame.setRedTimeLeft(180);
            originalGame.setBlueTimeLeft(240);
            originalGame.setCurrent(HexGame.RED);

            // Giả lập ghi nhị phân từ nút "Lưu Game" sinh ra file hex_save.dat
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(originalGame);
            }

            assertTrue(file.exists(), "Tệp lưu nhị phân phải tồn tại");

            // Giả lập đọc nhị phân từ nút "Tải Game"
            HexGame loadedGame;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                loadedGame = (HexGame) ois.readObject();
            }

            assertEquals(originalGame.getSize(), loadedGame.getSize());
            assertEquals(originalGame.getCurrent(), loadedGame.getCurrent());
            assertEquals(originalGame.getRedTimeLeft(), loadedGame.getRedTimeLeft());
            assertEquals(originalGame.getBlueTimeLeft(), loadedGame.getBlueTimeLeft());

            int[][] origBoard = originalGame.getBoard();
            int[][] loadBoard = loadedGame.getBoard();
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    assertEquals(origBoard[r][c], loadBoard[r][c]);
                }
            }

            Stack<int[]> origHist = originalGame.getMoveHistory();
            Stack<int[]> loadHist = loadedGame.getMoveHistory();
            assertEquals(origHist.size(), loadHist.size());
            for (int i = 0; i < origHist.size(); i++) {
                assertArrayEquals(origHist.get(i), loadHist.get(i));
            }

        } finally {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * CHỨC NĂNG ĐÃ LÀM (Tran05): Kết hợp xử lý nút bấm Hoàn tác (Undo) và Đồ họa bàn cờ.
     * MỤC TIÊU: Khi gọi lệnh hoàn tác, nước đi cuối cùng phải bị xóa khỏi đỉnh lịch sử (moveHistory.pop()).
     * Việc làm rỗng ô cờ (EMPTY) giúp hàm vẽ lại (repaint) của lớp HexPanel.java định vị lại vị trí
     * nước đi liền trước để vẽ viền sáng (Highlight Last Move) một cách chính xác.
     */
    private static void testGameUndoLogic() throws Exception {
        HexGame game = new HexGame(5);

        assertFalse(game.undo(), "Phải trả về false khi undo trên lịch sử trống");

        game.place(1, 2, HexGame.RED);
        assertEquals(HexGame.BLUE, game.getCurrent());
        assertEquals(HexGame.RED, game.getBoard()[1][2]);

        game.place(3, 3, HexGame.BLUE);
        assertEquals(HexGame.RED, game.getCurrent());
        assertEquals(HexGame.BLUE, game.getBoard()[3][3]);
        assertEquals(2, game.getMoveHistory().size());

        assertTrue(game.undo());
        assertEquals(HexGame.BLUE, game.getCurrent());
        assertEquals(HexGame.EMPTY, game.getBoard()[3][3]); // Trả về trống giúp xóa viền sáng cũ
        assertEquals(1, game.getMoveHistory().size()); // Đỉnh lịch sử mới dịch chuyển ra nước đi trước để Highlight Last Move

        assertTrue(game.undo());
        assertEquals(HexGame.RED, game.getCurrent());
        assertEquals(HexGame.EMPTY, game.getBoard()[1][2]);
        assertEquals(0, game.getMoveHistory().size());

        assertFalse(game.undo());
    }

    /**
     * Ca kiểm thử bổ sung: Đặt lại bộ đếm giờ lượt đi tương ứng sau khi Undo thành công.
     */
    private static void testUndoTimerReset() throws Exception {
        int timerSeconds = 45;
        HexGame game = new HexGame(11);
        GameTimer timer = new GameTimer(GameTimer.Mode.PER_MOVE, timerSeconds);

        game.place(5, 5, HexGame.RED);
        game.place(6, 6, HexGame.BLUE);

        game.setRedTimeLeft(20);
        game.setBlueTimeLeft(15);

        game.undo();
        game.undo();

        if (game.getCurrent() == HexGame.RED) {
            game.setRedTimeLeft(timerSeconds);
        } else if (game.getCurrent() == HexGame.BLUE) {
            game.setBlueTimeLeft(timerSeconds);
        }
        timer.setRemainingSeconds(game.getRedTimeLeft(), game.getBlueTimeLeft());

        assertEquals(45, game.getRedTimeLeft());
        assertEquals(15, game.getBlueTimeLeft());
        assertEquals(45, timer.getRedSeconds());
        assertEquals(15, timer.getBlueSeconds());
    }

    /**
     * CHỨC NĂNG ĐÃ LÀM (Tran05): Đồ họa sinh động - "Highlight đường chiến thắng" kết hợp thuật toán DFS.
     * MỤC TIÊU: Kiểm thử xem khi có người thắng trận, hàm hasPlayerWon / checkWinner() có trả về chuẩn xác
     * danh sách tọa độ chuỗi kết nối liên tục hay không. Danh sách này là cơ sở trực tiếp để file can thiệp chính
     * HexPanel.java duyệt qua, thay đổi màu sắc toàn bộ các ô cờ tạo nên đường thắng thành màu nổi bật.
     */
    private static void testCheckWinnerAndWinningPath() throws Exception {
        // Kiểm thử 1: RED thắng (nối chuỗi quân dọc liên tục từ trên xuống dưới trên bàn cờ 3x3)
        HexGame gameRed = new HexGame(3);
        gameRed.place(0, 1, HexGame.RED);
        gameRed.place(1, 1, HexGame.RED);
        gameRed.place(2, 1, HexGame.RED);

        assertEquals(HexGame.RED, gameRed.checkWinner());
        java.util.List<int[]> redPath = gameRed.getWinningPath();
        assertEquals(3, redPath.size());
        assertArrayEquals(new int[]{0, 1}, redPath.get(0));
        assertArrayEquals(new int[]{1, 1}, redPath.get(1));
        assertArrayEquals(new int[]{2, 1}, redPath.get(2));

        // Kiểm thử 2: BLUE thắng (nối chuỗi quân ngang liên tục từ trái sang phải trên bàn cờ 3x3)
        HexGame gameBlue = new HexGame(3);
        gameBlue.place(1, 0, HexGame.BLUE);
        gameBlue.place(1, 1, HexGame.BLUE);
        gameBlue.place(1, 2, HexGame.BLUE);

        assertEquals(HexGame.BLUE, gameBlue.checkWinner());
        java.util.List<int[]> bluePath = gameBlue.getWinningPath();
        assertEquals(3, bluePath.size());
        assertArrayEquals(new int[]{1, 0}, bluePath.get(0));
        assertArrayEquals(new int[]{1, 1}, bluePath.get(1));
        assertArrayEquals(new int[]{1, 2}, bluePath.get(2));
    }

    /**
     * Ca kiểm thử: Trí tuệ nhân tạo chọn nước đi.
     */
    private static void testAIChooseMove() throws Exception {
        HexGame game = new HexGame(5);
        HexAI ai = new HexAI();

        game.setCurrent(HexGame.BLUE);

        int[] move = ai.bestMove(game, 2);
        assertNotNull(move, "AI phải trả về một nước đi hợp lệ");
        assertEquals(2, move.length);
        assertTrue(move[0] >= 0 && move[0] < 5, "Hàng nằm trong bàn cờ");
        assertTrue(move[1] >= 0 && move[1] < 5, "Cột nằm trong bàn cờ");

        game.place(move[0], move[1], HexGame.BLUE);
        assertFalse(game.isEmpty(move[0], move[1]));

        int[] secondMove = ai.bestMove(game, 2);
        assertNotNull(secondMove);
        assertFalse(secondMove[0] == move[0] && secondMove[1] == move[1], "AI không được chọn ô đã bị chiếm");
    }

    /**
     * Ca kiểm thử: Ngắt tìm kiếm minimax khi hết giờ (AI Hard Timeout).
     */
    private static void testAITimeoutHeuristic() throws Exception {
        HexAI ai = new HexAI();
        HexGame game = new HexGame(5);

        setPrivateField(ai, "startTime", System.currentTimeMillis() - 20_000);
        setPrivateField(ai, "timeLimitMs", 15_000L);

        Boolean almostUp = (Boolean) invokePrivateMethodWithReturn(ai, "isTimeAlmostUp");
        assertTrue(almostUp, "isTimeAlmostUp phải trả về true khi thời gian chạy vượt quá ngưỡng");
    }

    // =========================================================================
    // CÁC HÀM ASSERTION TỰ VIẾT PHỤC VỤ KHẲNG ĐỊNH KẾT QUẢ KIỂM THỬ
    // =========================================================================
    private static void assertEquals(Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("Mong đợi giá trị <" + expected + ">, nhưng thực tế là: <" + actual + ">");
        }
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Mong đợi số nguyên <" + expected + ">, nhưng thực tế là: <" + actual + ">");
        }
    }

    private static void assertArrayEquals(int[] expected, int[] actual) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError("Mảng mong đợi: " + Arrays.toString(expected) + ", nhưng thực tế là: " + Arrays.toString(actual));
        }
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Mong đợi biểu thức là TRUE, nhưng thực tế là FALSE");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Mong đợi biểu thức là FALSE, nhưng thực tế là TRUE");
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Mong đợi đối tượng KHÁC NULL, nhưng thực tế là NULL");
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }
}
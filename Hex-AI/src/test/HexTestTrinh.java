package test;

import model.HexGame;

import java.util.Arrays;
import java.util.Stack;

/**
 * Bộ kiểm thử tự động chạy độc lập (Standalone Test Suite) dành cho dự án Hex-AI.
 * * MỤC ĐÍCH CHÍNH:
 * - [NgocTrinh] Đảm bảo logic hạ tầng cho UC-01 (Khởi tạo ván mới) hoạt động chính xác.
 * - [NgocTrinh] Kiểm tra thuật toán UC-08 (Lưu lịch sử đánh và Hoàn tác - Undo).
 */
public class HexTestTrinh {

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
        System.out.println(ANSI_BOLD + ANSI_CYAN + "      BẮT ĐẦU CHẠY BỘ KIỂM THỬ HEX-AI (NGOC TRINH)" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_CYAN + "==================================================" + ANSI_RESET);

        try {
            // =========================================================================
            // NHÓM 1: KIỂM THỬ KHỞI TẠO VÁN MỚI (UC-01)
            // TÁC GIẢ: NgocTrinh
            // =========================================================================
            runTest("[NgocTrinh] UC-01 - Khởi tạo kích thước bàn cờ và lượt đi", HexTestTrinh::testUC01_InitGame_SizeAndState);

            // =========================================================================
            // NHÓM 2: KIỂM THỬ LƯU LỊCH SỬ VÀ HOÀN TÁC (UC-08)
            // TÁC GIẢ: NgocTrinh
            // =========================================================================
            runTest("[NgocTrinh] UC-08 - Lưu tọa độ vào Stack khi đặt cờ", HexTestTrinh::testUC08_PlacePiece_ShouldSaveHistory);
            runTest("[NgocTrinh] UC-08 - Hoàn tác thành công 1 nước đi", HexTestTrinh::testUC08_UndoMove_Success);
            runTest("[NgocTrinh] UC-08 - Từ chối hoàn tác khi bàn cờ trống", HexTestTrinh::testUC08_UndoMove_EmptyHistory_ShouldFail);
            runTest("[NgocTrinh] UC-08 - Hoàn tác nhiều bước liên tiếp", HexTestTrinh::testUC08_MultipleUndo);

        } catch (Exception e) {
            System.err.println(ANSI_BOLD + ANSI_RED + "LỖI NGHIÊM TRỌNG: Quá trình kiểm thử bị ngắt quãng:" + ANSI_RESET);
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

    // =========================================================================
    // TRIỂN KHAI CÁC PHƯƠNG THỨC KIỂM THỬ CHI TIẾT CỦA [NGOCTRINH]
    // =========================================================================

    /**
     * [NgocTrinh] UC-01: Kiểm thử khởi tạo ván mới.
     * Đảm bảo cấu hình truyền từ SetupDialog xuống Model tạo ra một bàn cờ sạch sẽ,
     * đúng kích thước, đúng lượt đi đầu tiên và lịch sử rỗng.
     */
    private static void testUC01_InitGame_SizeAndState() throws Exception {
        int boardSize = 11;
        HexGame game = new HexGame(boardSize);

        // [NgocTrinh] Kích thước bàn cờ phải chuẩn xác
        assertEquals(boardSize, game.getSize());

        // [NgocTrinh] Lượt đi đầu tiên luôn là ĐỎ (RED)
        assertEquals(HexGame.RED, game.getCurrent());

        // [NgocTrinh] Lịch sử đánh ban đầu phải rỗng (Stack empty)
        assertTrue(game.getMoveHistory().isEmpty(), "Lịch sử đánh ban đầu phải rỗng");

        // [NgocTrinh] Toàn bộ các ô trên bàn cờ phải mang giá trị trống (EMPTY)
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                assertTrue(game.isEmpty(r, c), "Ô (" + r + "," + c + ") phải trống khi khởi tạo");
            }
        }
    }

    /**
     * [NgocTrinh] UC-08: Kiểm thử lưu lịch sử.
     * Khi người chơi đánh một nước cờ hợp lệ, tọa độ đó phải được push vào đỉnh Stack.
     */
    private static void testUC08_PlacePiece_ShouldSaveHistory() throws Exception {
        HexGame game = new HexGame(11);

        // [NgocTrinh] Giả lập Đỏ đặt quân cờ hợp lệ tại tọa độ (5, 5)
        boolean isPlaced = game.place(5, 5, HexGame.RED);

        assertTrue(isPlaced, "Đặt cờ vào ô trống phải trả về true");
        assertEquals(HexGame.BLUE, game.getCurrent()); // Lượt chuyển sang Xanh

        // [NgocTrinh] Stack lịch sử phải tăng lên 1 và lưu đúng tọa độ (5, 5)
        assertEquals(1, game.getMoveHistory().size());
        int[] lastMove = game.getMoveHistory().peek();
        assertArrayEquals(new int[]{5, 5}, lastMove);
    }

    /**
     * [NgocTrinh] UC-08: Kiểm thử chức năng Undo cơ bản.
     * Rút nước cờ trên cùng ra khỏi Stack, trả lại lượt và làm rỗng ô cờ.
     */
    private static void testUC08_UndoMove_Success() throws Exception {
        HexGame game = new HexGame(11);

        // [NgocTrinh] Setup 1 nước đi
        game.place(3, 3, HexGame.RED);

        // [NgocTrinh] Thực hiện Undo
        boolean undoResult = game.undo();

        assertTrue(undoResult, "Undo phải thành công khi có lịch sử");
        assertTrue(game.getMoveHistory().isEmpty(), "Lịch sử phải rỗng sau khi Undo 1 nước đi duy nhất");
        assertEquals(HexGame.RED, game.getCurrent()); // Lượt đi trả về Đỏ
        assertTrue(game.isEmpty(3, 3), "Ô (3,3) phải trở lại trạng thái trống");
    }

    /**
     * [NgocTrinh] UC-08: Kiểm thử ngoại lệ khi Undo.
     * Chặn lỗi khi người dùng cố tình bấm Undo lúc bàn cờ chưa có ai đánh.
     */
    private static void testUC08_UndoMove_EmptyHistory_ShouldFail() throws Exception {
        HexGame game = new HexGame(11);

        // [NgocTrinh] Cố tình gọi Undo ngay khi bàn cờ mới tinh
        boolean undoResult = game.undo();

        assertFalse(undoResult, "Undo phải trả về false nếu không có nước đi nào trong lịch sử");
        assertEquals(HexGame.RED, game.getCurrent()); // Lượt đi vẫn phải giữ nguyên là cờ Đỏ
    }

    /**
     * [NgocTrinh] UC-08: Kiểm thử Undo theo chuỗi (nhiều bước).
     * Đảm bảo thứ tự LIFO (Vào sau ra trước) của Stack hoạt động chuẩn xác.
     */
    private static void testUC08_MultipleUndo() throws Exception {
        HexGame game = new HexGame(11);

        // [NgocTrinh] Giả lập 2 nước đi liên tiếp
        game.place(0, 0, HexGame.RED);   // Lượt 1
        game.place(0, 1, HexGame.BLUE);  // Lượt 2

        assertEquals(2, game.getMoveHistory().size());

        // [NgocTrinh] Undo lần 1 (Xóa nước của Xanh)
        game.undo();
        assertEquals(1, game.getMoveHistory().size());
        assertEquals(HexGame.BLUE, game.getCurrent()); // Lượt trả về cho Xanh
        assertTrue(game.isEmpty(0, 1)); // Ô của Xanh mất
        assertFalse(game.isEmpty(0, 0)); // Ô của Đỏ vẫn còn

        // [NgocTrinh] Undo lần 2 (Xóa nước của Đỏ)
        game.undo();
        assertTrue(game.getMoveHistory().isEmpty());
        assertEquals(HexGame.RED, game.getCurrent()); // Lượt trả về cho Đỏ
        assertTrue(game.isEmpty(0, 0)); // Ô của Đỏ đã mất
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
}
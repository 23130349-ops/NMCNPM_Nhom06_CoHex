package main;

import controller.HexController;

/**
 * Main – Điểm khởi chạy ứng dụng cờ Hex.
 *
 * Pre-condition của UC-01 (Khởi tạo ván mới):
 *   "Ứng dụng đã được khởi chạy thành công"
 *
 * Phương thức này ủy quyền cho HexController.main() để đảm bảo
 * toàn bộ Swing UI được khởi tạo trên Event Dispatch Thread (EDT).
 */
public class Main {
    public static void main(String[] args) {
        // Khởi chạy HexController trên EDT → UC-07 (chọn chế độ) → UC-01 (tạo ván mới)
        HexController.main(args);
    }
}
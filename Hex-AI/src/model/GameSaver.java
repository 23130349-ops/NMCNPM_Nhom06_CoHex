package model;

import java.io.IOException;

/**
 * GameSaver - Lớp chịu trách nhiệm lưu và tải trạng thái ván đấu.
 * Chức năng này sẽ được triển khai dần trong các commit tiếp theo.
 */
public class GameSaver {

    /**
     * Lưu trạng thái ván đấu hiện tại ra file.
     * 
     * @param game Trạng thái game cần lưu
     * @param filePath Đường dẫn file lưu
     * @throws IOException Nếu có lỗi ghi file
     */
    public static void saveGame(HexGame game, String filePath) throws IOException {
        throw new UnsupportedOperationException("");
    }

    /**
     * Đọc trạng thái ván đấu từ file và tái tạo lại đối tượng HexGame.
     * 
     * @param filePath Đường dẫn file cần đọc
     * @return Đối tượng HexGame đã được phục hồi
     * @throws IOException Nếu có lỗi đọc file
     */
    public static HexGame loadGame(String filePath) throws IOException {
        throw new UnsupportedOperationException("");
    }
}

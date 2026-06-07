# Test Cases - Quach Hong Nhan - 23130216

## Use Case được kiểm thử

- UC-04: Tính toán và đặt quân màu xanh
- UC-08: Hoàn nước và xem lịch sử

## Người thực hiện

Quách Hồng Nhân - 23130216

---

# 1. Test case cho UC-04: Tính toán và đặt quân màu xanh

## TC_UC04_01: AI trả về nước đi hợp lệ

### Mục tiêu
Kiểm tra AI có thể tính toán và trả về một nước đi hợp lệ.

### Điều kiện trước
- Game đã được khởi tạo.
- Chế độ Human vs AI được chọn.
- Bàn cờ còn ô trống.
- Đến lượt AI màu xanh.

### Bước thực hiện
1. Chạy chương trình.
2. Chọn chế độ Human vs AI.
3. Người chơi đặt một quân hợp lệ.
4. Chờ AI tính toán và đặt quân.

### Kết quả mong đợi
- AI chọn một ô trống hợp lệ.
- Quân màu xanh được đặt lên bàn cờ.
- Bàn cờ và giao diện được cập nhật đúng.

### Kết quả thực tế
- AI đặt quân hợp lệ.
- Game tiếp tục hoạt động bình thường.

### Trạng thái
PASS

---

## TC_UC04_02: AI không làm treo giao diện

### Mục tiêu
Kiểm tra AI không làm giao diện bị đứng khi tính toán nước đi.

### Điều kiện trước
- Game chạy ở chế độ Human vs AI.
- Bàn cờ kích thước lớn như 11x11 hoặc 14x14.

### Bước thực hiện
1. Chạy chương trình.
2. Chọn bàn cờ lớn.
3. Đánh nhiều lượt với AI.
4. Quan sát thời gian phản hồi và trạng thái giao diện.

### Kết quả mong đợi
- Giao diện vẫn phản hồi.
- AI không xử lý quá lâu.
- Người chơi có thể tiếp tục thao tác sau lượt AI.

### Kết quả thực tế
- Giao diện không bị treo.
- AI đặt quân và game tiếp tục bình thường.

### Trạng thái
PASS

---

## TC_UC04_03: Kiểm tra cơ chế hard-timeout của AI

### Mục tiêu
Kiểm tra AI dừng tìm kiếm khi gần vượt quá thời gian xử lý cho phép.

### Điều kiện trước
- AI sử dụng thuật toán Minimax.
- HexAI có cơ chế kiểm tra thời gian bằng startTime, timeLimitMs và timeout.

### Bước thực hiện
1. Chạy game ở chế độ Human vs AI.
2. Chọn bàn cờ lớn hoặc trạng thái bàn cờ phức tạp.
3. Để AI tính toán nước đi.
4. Quan sát việc AI có trả về nước đi trong thời gian hợp lý không.

### Kết quả mong đợi
- Khi gần vượt ngưỡng thời gian, AI dừng tìm kiếm sâu hơn.
- AI trả về bestMoveSoFar hoặc nước đi hợp lệ gần nhất.
- Game không bị đứng.

### Kết quả thực tế
- AI trả về nước đi hợp lệ.
- Giao diện không bị treo.

### Trạng thái
PASS

---

## TC_UC04_04: AI không tìm được nước đi

### Mục tiêu
Kiểm tra hệ thống xử lý an toàn khi AI không còn nước đi hợp lệ.

### Điều kiện trước
- Bàn cờ không còn ô trống hợp lệ hoặc AI không tìm thấy nước đi.

### Bước thực hiện
1. Giả lập trạng thái bàn cờ gần đầy.
2. Đến lượt AI.
3. Gọi chức năng AI tính toán nước đi.

### Kết quả mong đợi
- AI trả về null hoặc không chọn nước đi sai.
- Hệ thống không crash.
- Trạng thái bàn cờ không bị sai lệch.

### Kết quả thực tế
- Hệ thống xử lý an toàn.
- Không phát sinh lỗi nghiêm trọng.

### Trạng thái
PASS

---

# 2. Test case cho UC-08: Hoàn nước và xem lịch sử

## TC_UC08_01: Undo khi đủ nước trong Human vs AI

### Mục tiêu
Kiểm tra chức năng Undo hoàn tác đúng nước đi trong chế độ Human vs AI.

### Điều kiện trước
- Game đang ở chế độ Human vs AI.
- Người chơi đã đánh một nước.
- AI đã đánh một nước.

### Bước thực hiện
1. Chạy game.
2. Chọn chế độ Human vs AI.
3. Người chơi đặt một quân.
4. Chờ AI đặt quân.
5. Bấm nút Undo.

### Kết quả mong đợi
- Hệ thống hoàn tác 2 nước gần nhất.
- Bàn cờ quay về trạng thái trước lượt người chơi.
- Lượt chơi trả về cho người chơi.

### Kết quả thực tế
- Undo hoạt động đúng.
- Bàn cờ được cập nhật lại chính xác.

### Trạng thái
PASS

---

## TC_UC08_02: Undo khi chưa đủ nước

### Mục tiêu
Kiểm tra hệ thống không lỗi khi người chơi bấm Undo nhưng chưa đủ nước đi.

### Điều kiện trước
- Game mới bắt đầu hoặc chưa đủ 2 nước trong chế độ Human vs AI.

### Bước thực hiện
1. Chạy game.
2. Chọn chế độ Human vs AI.
3. Bấm Undo khi chưa đánh đủ lượt.

### Kết quả mong đợi
- Hệ thống không crash.
- Hiển thị thông báo không đủ nước để hoàn tác.
- Bàn cờ, lịch sử và timer giữ nguyên.

### Kết quả thực tế
- Game không bị lỗi.
- Trạng thái bàn cờ giữ nguyên.

### Trạng thái
PASS

---

## TC_UC08_03: Reset Timer sau Undo

### Mục tiêu
Kiểm tra timer được reset sau khi Undo trong chế độ tính giờ theo lượt.

### Điều kiện trước
- Game chạy ở chế độ Human vs AI.
- Timer mode là PER_MOVE.
- Người chơi và AI đã có nước đi.

### Bước thực hiện
1. Chạy game.
2. Chọn chế độ PER_MOVE.
3. Người chơi đánh một nước.
4. Chờ AI đánh.
5. Đợi timer giảm vài giây.
6. Bấm Undo.

### Kết quả mong đợi
- Timer của lượt hiện tại được reset về thời gian ban đầu.
- Người chơi không bị mất thời gian oan sau khi hoàn tác.

### Kết quả thực tế
- Timer được cập nhật lại đúng.
- Game tiếp tục hoạt động bình thường.

### Trạng thái
PASS

---

## TC_UC08_04: Cập nhật lịch sử sau Undo

### Mục tiêu
Kiểm tra lịch sử nước đi được cập nhật đúng sau khi hoàn tác.

### Điều kiện trước
- Game đã có nhiều nước đi trong moveHistory.

### Bước thực hiện
1. Chạy game.
2. Đánh vài lượt.
3. Quan sát lịch sử nước đi.
4. Bấm Undo.
5. Kiểm tra lại lịch sử.

### Kết quả mong đợi
- Các nước đi bị hoàn tác được xóa khỏi lịch sử.
- Lịch sử hiển thị khớp với trạng thái bàn cờ hiện tại.

### Kết quả thực tế
- Lịch sử nước đi được cập nhật đúng.

### Trạng thái
PASS

---

## TC_UC08_05: Undo khi AI đang xử lý

### Mục tiêu
Kiểm tra hệ thống không bị sai trạng thái nếu người chơi bấm Undo khi AI đang tính toán.

### Điều kiện trước
- Game đang ở chế độ Human vs AI.
- AI đang trong quá trình tính toán nước đi.

### Bước thực hiện
1. Chạy game.
2. Người chơi đặt quân.
3. Khi AI đang xử lý, thử bấm Undo.

### Kết quả mong đợi
- Hệ thống từ chối thao tác Undo hoặc chờ trạng thái hợp lệ.
- Bàn cờ không bị sai lệch.
- Game không crash.

### Kết quả thực tế
- Hệ thống xử lý an toàn.
- Không phát sinh lỗi sai trạng thái.

### Trạng thái
PASS

---

# Kết luận

Các test case cho UC-04 và UC-08 đều đạt kết quả PASS. Chức năng AI hard-timeout giúp hạn chế tình trạng AI tính toán quá lâu. Chức năng Undo khôi phục đúng trạng thái bàn cờ, cập nhật lịch sử nước đi và reset Timer trong chế độ PER_MOVE.
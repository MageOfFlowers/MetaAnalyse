package api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.AiIntent;
import dto.ChatResponse;
import repository.AnalysisRepository;
import service.ChatService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired 
    private ChatService chatService;

    @Autowired 
    private AnalysisRepository repository;

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody Map<String, String> request) {
        try {
            String userMsg = request.get("message");
            if (userMsg == null || userMsg.isEmpty()) {
                return ResponseEntity.badRequest().body("Tin nhắn không được để trống");
            }

            // 1. Lấy chuỗi JSON thô từ Gemini
            String rawJson = chatService.getRawAiResponse(userMsg);
            
            // 2. Parse JSON sang AiIntent
            ObjectMapper mapper = new ObjectMapper();
            AiIntent intent;
            try {
                intent = mapper.readValue(rawJson, AiIntent.class);
            } catch (Exception e) {
                return ResponseEntity.ok(new ChatResponse(
                    "Xin lỗi, tôi không thể phân tích yêu cầu này. Hãy thử hỏi về 'top card', 'winrate' hoặc 'tỷ lệ meta' nhé.", 
                    null
                ));
            }

            // 3. Thực thi truy vấn SQL dựa trên action và các filters (region, date, color...)
            List<Map<String, Object>> dbData = repository.executeAction(intent.action, intent.params);
            
            // 4. Tạo phản hồi tự nhiên dựa trên kết quả thực tế
            String naturalReply = generateReply(intent.action, intent.params, dbData);
            
            return ResponseEntity.ok(new ChatResponse(naturalReply, dbData));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    private String generateReply(String action, Map<String, Object> params, List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "Tôi đã tra cứu nhưng không tìm thấy dữ liệu phù hợp trong hệ thống.";
        }
        
        switch (action) {
            case "GET_TOP_CARDS":
                return "Đây là danh sách các lá bài hàng đầu phù hợp với yêu cầu của bạn.";
            case "GET_USAGE_STATS":
                String mode = "rarity".equals(params.get("view_mode")) ? "độ hiếm" : "màu sắc";
                return "Đây là biểu đồ phân bổ tỷ lệ Meta theo " + mode + " mà bạn cần.";
            case "GET_DECK_RANKINGS":
                return "Dưới đây là bảng xếp hạng các bộ bài (Decks) có hiệu suất tốt nhất.";
            case "ANALYZE_CARD_DETAIL":
                return "Phân tích xong! Lá bài '" + params.get("card_name") + "' có hiệu số winrate thay đổi theo số lượng bản copy như sau:";
            case "GET_PLAYER_RANKINGS":
                return "Đây là danh sách những người chơi xuất sắc nhất dựa trên dữ liệu giải đấu.";
            default:
                return "Kết quả tra cứu dữ liệu Meta của bạn đã sẵn sàng:";
        }
    }
}
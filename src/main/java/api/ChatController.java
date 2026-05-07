package api;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import dto.ActionConfig;
import dto.AiIntent;
import dto.ChatResponse;
import repository.AnalysisRepository;
import service.ActionConfigService;
import service.ChatService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired 
    private ChatService chatService;

    @Autowired 
    private AnalysisRepository repository;

    @Autowired
    private ActionConfigService actionConfigService;

    @PostMapping(value = "/ask", produces = "application/json; charset=UTF-8")
public ResponseEntity<?> ask(@RequestBody Map<String, String> request) {
    try {
        String userMsg = request.get("message");
        
        // 1. Check Cache (Dành cho các câu hỏi khớp keyword trong CSV)
        ActionConfig staticMatch = actionConfigService.findStaticMatch(userMsg);
        if (staticMatch != null) {
            // LỖI Ở ĐÂY: Trước đó bạn trả về null. 
            // SỬA THÀNH: Gọi repository để lấy static_data đóng gói vào list
            List<Map<String, Object>> staticData = repository.executeAction(staticMatch.getAction(), null);
            return ResponseEntity.ok(new ChatResponse(staticMatch.getReply(), staticData));
        }

        // 2. Gọi Gemini (Giữ nguyên)
        String rawJson = chatService.getRawAiResponse(userMsg);
        ObjectMapper mapper = new ObjectMapper();
        AiIntent intent = mapper.readValue(rawJson, AiIntent.class);

        // 3. Xử lý LEARN_KNOWLEDGE (Giữ nguyên logic lưu)
        if ("LEARN_KNOWLEDGE".equals(intent.action)) {
            String answer = (String) intent.params.get("answer");
            String topic = (String) intent.params.get("topic");
            String keywords = (String) intent.params.get("keywords");
            actionConfigService.learnNewAction(topic, answer, true, null, keywords);
            
            return ResponseEntity.ok(new ChatResponse(answer, null));
        }

        // 4. Các action Dynamic (SQL) VÀ Action Tĩnh được AI nhận diện (như YOUTUBE_CHANNEL)
        List<Map<String, Object>> dbData = repository.executeAction(intent.action, intent.params);
        
        // Lấy reply từ file CSV dựa trên action mà AI trả về
        ActionConfig config = actionConfigService.getConfig(intent.action);
        String naturalReply = (config != null) ? generateReply(intent.action, intent.params, dbData) : "Tôi đã tìm thấy kết quả:";
        
        return ResponseEntity.ok(new ChatResponse(naturalReply, dbData));

    } catch (Exception e) {
        return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
    }
}

    private String generateReply(String action, Map<String, Object> params, List<Map<String, Object>> data) {
    ActionConfig config = actionConfigService.getConfig(action);
    if (config == null) return "Đây là kết quả tôi tìm được:";

    // Nếu là action động (isStatic = false) mà data rỗng thì mới báo không thấy
    if (!config.isStatic() && (data == null || data.isEmpty())) {
        return "Tôi đã tra cứu nhưng không tìm thấy dữ liệu phù hợp trong hệ thống.";
    }
    
    // Xử lý template String cho ANALYZE_CARD_DETAIL
    if ("ANALYZE_CARD_DETAIL".equals(action) && params != null && params.containsKey("card_name")) {
        return String.format(config.getReply(), params.get("card_name"));
    }

    return config.getReply();
}
}
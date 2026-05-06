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
        
        // 1. Check Cache trước
        ActionConfig staticMatch = actionConfigService.findStaticMatch(userMsg);
        if (staticMatch != null) {
            return ResponseEntity.ok(new ChatResponse(staticMatch.getReply(), null));
        }

        // 2. Gọi Gemini
        String rawJson = chatService.getRawAiResponse(userMsg);
        ObjectMapper mapper = new ObjectMapper();
        AiIntent intent = mapper.readValue(rawJson, AiIntent.class);

        // 3. Xử lý LEARN_KNOWLEDGE
        if ("LEARN_KNOWLEDGE".equals(intent.action)) {
            // Lấy dữ liệu từ params của intent
            String answer = (String) intent.params.get("answer");
            String topic = (String) intent.params.get("topic");
            String keywords = (String) intent.params.get("keywords");

            // LƯU VÀO CSV: Đảm bảo gọi hàm này
            actionConfigService.learnNewAction(topic, answer, true, null, keywords);

            // TRẢ VỀ CHO FRONTEND
            return ResponseEntity.ok(new ChatResponse(answer, null));
        }

        // 4. Các action Dynamic khác (SQL)
        List<Map<String, Object>> dbData = repository.executeAction(intent.action, intent.params);
        String naturalReply = generateReply(intent.action, intent.params, dbData);
        
        return ResponseEntity.ok(new ChatResponse(naturalReply, dbData));

    } catch (Exception e) {
        return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
    }
}

    private String generateReply(String action, Map<String, Object> params, List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "Tôi đã tra cứu nhưng không tìm thấy dữ liệu phù hợp trong hệ thống.";
        }
        
        ActionConfig config = actionConfigService.getConfig(action);
        
        // Xử lý riêng cho hành động xem chi tiết thẻ bài
        if ("ANALYZE_CARD_DETAIL".equals(action) && params.containsKey("card_name")) {
            return String.format(config.getReply(), params.get("card_name"));
        }

        return config.getReply();
    }
}
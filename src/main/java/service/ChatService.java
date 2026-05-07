package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChatService {

    @Value("${cohere.api.key}")
    private String apiKey;

    private final String apiUrl = "https://api.cohere.ai/v1/chat";
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatService() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters()
            .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public String getRawAiResponse(String userMessage) {
        // Cấu trúc Instruction đầy đủ, kiểm tra kỹ dấu ngoặc kép và nối chuỗi
        String preamble = "Bạn là chuyên gia phân tích dữ liệu card game Thần Tích. " +
            "Nhiệm vụ: Chuyển câu hỏi người dùng thành JSON. " +
            "Các Action: GET_TOP_CARDS, GET_USAGE_STATS, GET_DECK_RANKINGS, ANALYZE_CARD_DETAIL, GET_PLAYER_RANKINGS, YOUTUBE_CHANNEL, HOW_TO_BUILD, HOW_TO_COUNTER. " +
            "Tham số: limit (int), color (R, G, P, Y), region (N, S), card_name (string), sort_by (winrate/usage). " +
            "Nếu người dùng hỏi 'cách chơi/xây dựng bộ bài [tên bài(nếu không có tên thì trả về '']', trả về action: HOW_TO_BUILD. " +
            "Nếu người dùng hỏi 'cách khắc chế/đối đầu [tên bài(nếu không có tên thì trả về '']', trả về action: HOW_TO_COUNTER. " +
            "Nếu người dùng hỏi về luật chơi hoặc kiến thức tổng quát, hãy trả về: " +
            "{ \"action\": \"LEARN_KNOWLEDGE\", \"params\": { \"topic\": \"TEN_TOPIC\", \"answer\": \"Câu trả lời tiếng Việt có dấu...\", \"keywords\": \"từ_khóa_1|từ_khóa_2\" } } " +
            "Lưu ý quan trọng: " +
            "1. Trường 'params' phải chứa 'keywords' là các cụm từ đặc trưng, tránh từ đơn chung chung. " +
            "2. Trả về tiếng Việt có dấu chuẩn UTF-8. " +
            "3. CHỈ TRẢ VỀ JSON, KHÔNG GIẢI THÍCH. " +
            "4. Nếu nói đến cao nhất, thấp nhất mà không có số đi kèm thì là limit 1";

        Map<String, Object> requestBody = new HashMap<>();
        // CẬP NHẬT: Sử dụng model command-r-v2 (phiên bản ổn định năm 2026)
        // Nếu vẫn lỗi, bạn có thể thử "command-r-08-2024"
        requestBody.put("model", "command-r-v2"); 
        requestBody.put("message", userMessage);
        requestBody.put("preamble", preamble);
        requestBody.put("response_format", Map.of("type", "json_object"));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey.trim());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = mapper.valueToTree(response.getBody());
                String rawContent = root.path("text").asText().trim();

                // Làm sạch JSON Markdown
                String cleanedJson = rawContent.replaceAll("(?i)```json", "")
                                               .replaceAll("```", "")
                                               .trim();
                
                return new String(cleanedJson.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi tại ChatService: " + e.getMessage());
            // Log chi tiết response lỗi từ server để dễ xử lý
            if (e.getMessage().contains("404")) {
                 System.err.println("Gợi ý: Kiểm tra lại tên model tại https://docs.cohere.com/docs/models");
            }
        }

        return "{\"action\": \"ERROR\", \"params\": {\"message\": \"Hệ thống gặp sự cố kết nối AI (Model Not Found).\"}}";
    }
}
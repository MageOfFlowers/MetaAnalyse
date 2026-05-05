package service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.List;

@Service
public class ChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public String getRawAiResponse(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();
        // URL đã bao gồm Key từ application.yml
        String url = apiUrl + "?key=" + apiKey;

        // System Instruction cực kỳ quan trọng để định nghĩa hành vi của AI
        String systemInstruction = "Bạn là chuyên gia phân tích dữ liệu card game. " +
            "Nhiệm vụ: Chuyển câu hỏi người dùng thành JSON. " +
            "Các Action: GET_TOP_CARDS, GET_USAGE_STATS, GET_DECK_RANKINGS, ANALYZE_CARD_DETAIL, GET_PLAYER_RANKINGS. " +
            "Tham số: limit (int), color (R, G, P, Y), region (N, S), card_name (string), sort_by (winrate/usage), view_mode (color/rarity). " +
            "Đặc biệt: Nếu câu hỏi liên quan đến Youtube, gameplay tham khảo,... thì action là YOUTUBE_CHANNEL." +
            "LƯU Ý: CHỈ TRẢ VỀ JSON, KHÔNG GIẢI THÍCH.";

        // Cấu trúc Request Body theo chuẩn Google Gemini API
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", systemInstruction + "\n\nCâu hỏi người dùng: " + userMessage)
                ))
            )
        );

        try {
            // Gửi Request POST
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.valueToTree(response);
            
            // Trích xuất chuỗi text từ cấu trúc JSON của Gemini
            String rawContent = root.path("candidates").get(0)
                                    .path("content").path("parts").get(0)
                                    .path("text").asText();

            // 1. Làm sạch Markdown nếu AI trả về kèm ký tự bọc code
            String cleanedJson = rawContent.replaceAll("```json", "")
                                           .replaceAll("```", "")
                                           .trim();
            
            // 2. Log ra Console để bạn kiểm tra AI đang trả về gì
            System.out.println("AI Response Cleaned: " + cleanedJson);
            
            return cleanedJson;
        } catch (Exception e) {
            System.err.println("Lỗi gọi Gemini API: " + e.getMessage());
            // Trả về một chuỗi JSON lỗi hợp lệ để Controller không bị crash khi parse
            return "{\"action\": \"ERROR\", \"params\": {\"error\": \"" + e.getMessage() + "\"}}";
        }
    }
}
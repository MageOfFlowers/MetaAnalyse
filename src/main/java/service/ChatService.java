package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    @Value("${gemini.api.keys}")
    private List<String> apiKeys;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private int currentKeyIndex = 0;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatService() {
        this.restTemplate = new RestTemplate();
        // Cấu hình RestTemplate ưu tiên xử lý UTF-8 để tránh lỗi font tiếng Việt
        this.restTemplate.getMessageConverters()
            .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public String getRawAiResponse(String userMessage) {
        int attempts = 0;
        int totalKeys = apiKeys.size();

        while (attempts < totalKeys) {
            String currentKey = apiKeys.get(currentKeyIndex).trim();
            String url = apiUrl + "?key=" + currentKey;

            // System Instruction tối ưu: Ép định dạng JSON, yêu cầu keywords chất lượng và UTF-8
            String systemInstruction = "Bạn là chuyên gia phân tích dữ liệu card game Thần Tích. " +
                "Nhiệm vụ: Chuyển câu hỏi người dùng thành JSON. " +
                "Các Action: GET_TOP_CARDS, GET_USAGE_STATS, GET_DECK_RANKINGS, ANALYZE_CARD_DETAIL, GET_PLAYER_RANKINGS, YOUTUBE_CHANNEL, HOW_TO_BUILD, HOW_TO_COUNTER. " +
                "Tham số: limit (int), color (R, G, P, Y), region (N, S), card_name (string), sort_by (winrate/usage). " +
                "Nếu người dùng hỏi 'cách chơi/xây dựng bộ bài [tên bài(nếu không có tên thì trả về '']', trả về action: HOW_TO_BUILD. " +
                "Nếu người dùng hỏi 'cách khắc chế/đối đầu [tên bài(nếu không có tên thì trả về '']', trả về action: HOW_TO_COUNTER. " +
                "Nếu người dùng hỏi về luật chơi hoặc kiến thức tổng quát, hãy trả về:\n" +
                "{ \"action\": \"LEARN_KNOWLEDGE\", \"params\": { \"topic\": \"TEN_TOPIC\", \"answer\": \"Câu trả lời tiếng Việt có dấu...\", \"keywords\": \"từ_khóa_1|từ_khóa_2\" } }\n" +
                "Lưu ý quan trọng: " +
                "1. Trường 'params' phải chứa 'keywords' là các cụm từ đặc trưng, tránh từ đơn chung chung. " +
                "2. Trả về tiếng Việt có dấu chuẩn UTF-8. " +
                "3. CHỈ TRẢ VỀ JSON, KHÔNG GIẢI THÍCH." +
                "4. Nếu nói đến cao nhất, thấp nhất mà không có số đi kèm thì là limit 1";

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", systemInstruction + "\n\nCâu hỏi: " + userMessage)
                    ))
                )
            );

            try {
                // Gọi API Gemini
                Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
                JsonNode root = mapper.valueToTree(response);
                
                JsonNode candidates = root.path("candidates");
             // Trong ChatService.java, đoạn xử lý kết quả từ Gemini
                if (candidates.isArray() && !candidates.isEmpty()) {
                    String rawContent = candidates.get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText();

                    // 1. Làm sạch Markdown nhưng giữ nguyên khoảng trắng và ngắt dòng
                    String cleanedJson = rawContent.replaceAll("(?i)```json", "")
                                                   .replaceAll("```", "")
                                                   .trim();

                    // 2. Ép kiểu UTF-8 chuẩn để tránh lỗi font tiếng Việt
                    cleanedJson = new String(cleanedJson.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    
                    return cleanedJson;
                }
                
                throw new RuntimeException("Dữ liệu trả về từ Gemini không đúng định dạng.");

            } catch (HttpClientErrorException.TooManyRequests e) {
                // Xoay vòng key nếu hết quota (Error 429)
                System.out.println("Key " + currentKeyIndex + " hết hạn mức. Đang chuyển sang Key tiếp theo...");
                currentKeyIndex = (currentKeyIndex + 1) % totalKeys;
                attempts++;
            } catch (Exception e) {
                System.err.println("Lỗi tại ChatService: " + e.getMessage());
                break; 
            }
        }

        return "{\"action\": \"ERROR\", \"params\": {\"message\": \"Hệ thống hết hạn mức. Vui lòng quay lại sau.\"}}";
    }
}
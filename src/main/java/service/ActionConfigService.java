package service;

import dto.ActionConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class ActionConfigService {

    private final Map<String, ActionConfig> configMap = new HashMap<>();

    @PostConstruct
    public void init() {
        loadConfigs();
    }

    public void loadConfigs() {
        configMap.clear();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                    getClass().getResourceAsStream("/actions.csv"), 
                    StandardCharsets.UTF_8))) {
            
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                
                // Sử dụng regex để split CSV (tránh lỗi khi trong nội dung có dấu phẩy)
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                if (values.length >= 3) {
                    String action = values[0].trim();
                    // Loại bỏ dấu ngoặc kép thừa nếu có
                    String reply = values[1].replace("\"", "").trim();
                    boolean isStatic = Boolean.parseBoolean(values[2].trim());
                    String staticData = (values.length > 3) ? values[3].replace("\"", "").trim() : "";

                    configMap.put(action, new ActionConfig(action, reply, isStatic, staticData));
                }
            }
            System.out.println("--- Đã tải " + configMap.size() + " cấu trúc chatbot từ CSV ---");
        } catch (Exception e) {
            System.err.println("Lỗi đọc file actions.csv: " + e.getMessage());
            // Tạo cấu hình lỗi mặc định để tránh crash
            configMap.put("ERROR", new ActionConfig("ERROR", "Hệ thống đang bảo trì, vui lòng thử lại sau.", true, ""));
        }
    }

    public ActionConfig getConfig(String action) {
        return configMap.getOrDefault(action, configMap.get("ERROR"));
    }
}
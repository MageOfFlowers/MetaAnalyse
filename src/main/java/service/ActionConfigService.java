package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import dto.ActionConfig;

@Service
public class ActionConfigService {

    private final Map<String, ActionConfig> configMap = new HashMap<>();
    // Đường dẫn tuyệt đối để ghi file (Learn) trên Render
    private final Path CSV_WRITE_PATH = Paths.get(System.getProperty("user.dir"), "config", "action.csv");

    private final List<String> DYNAMIC_KEYWORDS = List.of(
        "tỉ lệ", "thắng", "winrate", "top", "build", "khắc chế", "đối đầu", "chơi thế nào", "xây dựng"
    );

    @PostConstruct
    public void init() {
        loadConfigs();
    }

    /**
     * Load cấu hình từ file CSV. 
     * Ưu tiên đọc từ resource để đảm bảo khi deploy luôn có dữ liệu gốc.
     */
    public void loadConfigs() {
        configMap.clear();
        try {
            // Sử dụng ClassPathResource để đọc file từ src/main/resources/config/action.csv
            ClassPathResource resource = new ClassPathResource("config/action.csv");
            
            if (!resource.exists()) {
                System.err.println(">>> ERROR: Không tìm thấy file action.csv trong resources!");
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean isHeader = true;
                while ((line = br.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; }
                    
                    // Regex xử lý CSV có chứa dấu phẩy trong ngoặc kép
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (parts.length >= 2) {
                        String action = parts[0].trim();
                        String reply = parts[1].replace("\"", "").trim();
                        
                        // Sửa lỗi: Trim và xử lý ký tự xuống dòng của Windows (\r)
                        boolean isStatic = parts.length > 2 && Boolean.parseBoolean(parts[2].trim());
                        String staticData = parts.length > 3 ? parts[3].replace("\"", "").trim() : "";
                        String keywords = parts.length > 4 ? parts[4].replace("\"", "").replace("\r", "").trim() : "";
                        
                        configMap.put(action, new ActionConfig(action, reply, isStatic, staticData, keywords));
                    }
                }
                System.out.println(">>> SUCCESS: Đã nạp " + configMap.size() + " cấu hình từ CSV.");
            }
        } catch (IOException e) {
            System.err.println(">>> ERROR: Lỗi đọc file cấu hình: " + e.getMessage());
        }
    }

    public ActionConfig findStaticMatch(String userMessage) {
        if (userMessage == null) return null;
        String msg = userMessage.toLowerCase();
        
        // BƯỚC 1: Kiểm tra từ khóa động (Nếu dính từ khóa này thì bắt buộc gọi Gemini)
        for (String actionKw : DYNAMIC_KEYWORDS) {
            if (msg.contains(actionKw)) return null;
        }

        // BƯỚC 2: Kiểm tra Static Match
        for (ActionConfig config : configMap.values()) {
            if (config.isStatic() && config.getKeywords() != null && !config.getKeywords().isEmpty()) {
                String[] kws = config.getKeywords().split("\\|");
                for (String kw : kws) {
                    String trimmedKw = kw.trim().toLowerCase();
                    if (!trimmedKw.isEmpty() && msg.contains(trimmedKw)) {
                        return config;
                    }
                }
            }
        }
        return null;
    }

    public synchronized void learnNewAction(String action, String reply, boolean isStatic, String staticData, String keywords) {
        configMap.put(action, new ActionConfig(action, reply, isStatic, staticData, keywords));

        try {
            if (!Files.exists(CSV_WRITE_PATH.getParent())) {
                Files.createDirectories(CSV_WRITE_PATH.getParent());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(CSV_WRITE_PATH, 
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                
                String safeReply = reply.replace("\"", "'").replace("\n", " "); 
                String safeKeywords = (keywords != null) ? keywords.replace("\"", "'") : "";
                
                String line = String.format("%s,\"%s\",%b,\"%s\",\"%s\"", 
                        action, safeReply, isStatic, (staticData == null) ? "" : staticData, safeKeywords);
                
                writer.write(line);
                writer.newLine();
                writer.flush(); 
                System.out.println(">>> LEARN: Đã lưu hành động mới: " + action);
            }
        } catch (IOException e) {
            System.err.println(">>> ERROR: Không thể ghi file CSV: " + e.getMessage());
        }
    }

    public ActionConfig getConfig(String action) {
        return configMap.getOrDefault(action, new ActionConfig("ERROR", "Lỗi phân tích hành động '" + action + "'", true, ""));
    }
}
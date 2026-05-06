package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import dto.ActionConfig;

@Service
public class ActionConfigService {

    private final Map<String, ActionConfig> configMap = new HashMap<>();
    private final Path CSV_PATH = Paths.get("config", "action.csv");

    @PostConstruct
    public void init() {
        loadConfigs();
    }

    public void loadConfigs() {
        configMap.clear();
        if (!Files.exists(CSV_PATH)) return;

        try (BufferedReader br = Files.newBufferedReader(CSV_PATH, StandardCharsets.UTF_8)) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length >= 2) {
                    String action = parts[0].trim();
                    String reply = parts[1].replace("\"", "").trim();
                    boolean isStatic = parts.length > 2 && Boolean.parseBoolean(parts[2].trim());
                    String staticData = parts.length > 3 ? parts[3].replace("\"", "").trim() : "";
                    // Đọc keywords từ cột thứ 5 (nếu có)
                    String keywords = parts.length > 4 ? parts[4].replace("\"", "").trim() : "";
                    
                    configMap.put(action, new ActionConfig(action, reply, isStatic, staticData, keywords));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

 // Trong ActionConfigService.java

    private final List<String> DYNAMIC_KEYWORDS = List.of(
    	    "tỉ lệ", "thắng", "winrate", "top", "build", "khắc chế", "đối đầu", "chơi thế nào", "xây dựng"
    	);

    public ActionConfig findStaticMatch(String userMessage) {
        String msg = userMessage.toLowerCase();
        
        // BƯỚC 1: KIỂM TRA XEM CÓ TỪ KHÓA HÀNH ĐỘNG KHÔNG
        // Nếu có, đây chắc chắn là câu hỏi cần truy vấn Database -> Bỏ qua Static
        for (String actionKw : DYNAMIC_KEYWORDS) {
            if (msg.contains(actionKw)) {
                return null; // Trả về null để đi xuống bước gọi Gemini
            }
        }

        // BƯỚC 2: NẾU KHÔNG CÓ TỪ KHÓA HÀNH ĐỘNG, MỚI CHECK STATIC NHƯ CŨ
        for (ActionConfig config : configMap.values()) {
            if (config.isStatic() && config.getKeywords() != null && !config.getKeywords().isEmpty()) {
                String[] kws = config.getKeywords().split("\\|");
                for (String kw : kws) {
                    String trimmedKw = kw.trim().toLowerCase();
                    // Chỉ khớp nếu từ khóa tĩnh xuất hiện trong câu
                    if (!trimmedKw.isEmpty() && msg.contains(trimmedKw)) {
                        return config;
                    }
                }
            }
        }
        return null;
    }

    // Trong ActionConfigService.java
public synchronized void learnNewAction(String action, String reply, boolean isStatic, String staticData, String keywords) {
    configMap.put(action, new ActionConfig(action, reply, isStatic, staticData, keywords));

    try {
        if (!Files.exists(CSV_PATH.getParent())) Files.createDirectories(CSV_PATH.getParent());

        // Sử dụng APPEND và UTF_8 để ghi file
        try (BufferedWriter writer = Files.newBufferedWriter(CSV_PATH, 
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            
            // Thay dấu " thành ' để không làm hỏng cấu trúc CSV
            String safeReply = reply.replace("\"", "'").replace("\n", " "); 
            String safeKeywords = (keywords != null) ? keywords.replace("\"", "'") : "";
            
            String line = String.format("%s,\"%s\",%b,\"%s\",\"%s\"", 
                    action, safeReply, isStatic, (staticData == null) ? "" : staticData, safeKeywords);
            
            writer.write(line);
            writer.newLine();
            writer.flush(); 
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public ActionConfig getConfig(String action) {
        return configMap.getOrDefault(action, new ActionConfig("ERROR", "Lỗi phân tích...", true, ""));
    }
}
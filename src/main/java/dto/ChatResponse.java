package dto;

import java.util.List;
import java.util.Map;

public class ChatResponse {
    private String reply;
    private Object data; // Chứa kết quả từ Database (List, Map, hoặc Object)

    // Constructor đã sửa để gán giá trị
    public ChatResponse(String reply, Object data) {
        this.reply = reply;
        this.data = data;
    }

    // Getter và Setter cho Reply
    public String getReply() { 
        return reply; 
    }
    
    public void setReply(String reply) { 
        this.reply = reply; 
    }

    // Getter và Setter cho Data (Đã sửa kiểu dữ liệu tham số từ String thành Object)
    public Object getData() { 
        return data; 
    }
    
    public void setData(Object data) { 
        this.data = data; 
    }
}
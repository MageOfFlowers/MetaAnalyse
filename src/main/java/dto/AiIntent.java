package dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiIntent {
    public String action;
    
    @JsonProperty("parameters") // Map trường parameters từ AI vào biến params
    public Map<String, Object> params;

    // Getter và Setter
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }
}
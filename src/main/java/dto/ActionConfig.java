package dto;

public class ActionConfig {
    private String action;
    private String reply;
    private boolean isStatic;
    private String staticData;
    private String keywords;

    public ActionConfig(String action, String reply, boolean isStatic, String staticData, String keywords) {
        this.action = action;
        this.reply = reply;
        this.isStatic = isStatic;
        this.staticData = staticData;
        this.keywords = keywords;
    }
    
    public ActionConfig(String action, String reply, boolean isStatic, String staticData) {
        this.action = action;
        this.reply = reply;
        this.isStatic = isStatic;
        this.staticData = staticData;
    }

    // Getters
    public String getAction() { return action; }
    public String getReply() { return reply; }
    public boolean isStatic() { return isStatic; }
    public String getStaticData() { return staticData; }
    public String getKeywords() { return keywords; }
}
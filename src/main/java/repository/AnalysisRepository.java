package repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import dto.ActionConfig;
import service.ActionConfigService;

@Repository
public class AnalysisRepository {
    @Autowired
    private ActionConfigService actionConfigService;
    
    @Autowired
    private JdbcTemplate db;

    public List<Map<String, Object>> executeAction(String action, Map<String, Object> params) {
        ActionConfig config = actionConfigService.getConfig(action);
        
        // 1. Xử lý Action tĩnh (is_static) từ cấu hình CSV
        // Sửa đổi: Đóng gói static_data vào List<Map> để tránh trả về null hoặc rỗng sai cấu trúc
        if (config != null && config.isStatic()) {
            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            
            // Lấy dữ liệu tĩnh từ cột static_data trong CSV
            String staticContent = config.getStaticData();
            
            // Nếu có dữ liệu thì đóng gói vào key "link" hoặc "content" tùy bạn quy định
            if (staticContent != null && !staticContent.isEmpty()) {
                map.put("link", staticContent); 
                map.put("value", staticContent); // Backup thêm key value để linh hoạt
                result.add(map);
            }
            return result; 
        }

        if (params == null) params = new HashMap<>();
        
        // 2. Xử lý bộ lọc chung cho các truy vấn động
        StringBuilder commonFilter = new StringBuilder(" WHERE 1=1 ");
        List<Object> queryParams = new ArrayList<>();

        if (params.get("region") != null && !"all".equalsIgnoreCase(params.get("region").toString())) {
            commonFilter.append(" AND comp.region = ? ");
            queryParams.add(params.get("region").toString());
        }
        
        if (params.get("startDate") != null) {
            commonFilter.append(" AND comp.competition_date >= ?::date ");
            queryParams.add(params.get("startDate"));
        }
        if (params.get("endDate") != null) {
            commonFilter.append(" AND comp.competition_date <= ?::date ");
            queryParams.add(params.get("endDate"));
        }

        System.out.println("--- Thực thi Action: " + action + " ---");

        switch (action) {
            case "GET_TOP_CARDS":
                return getTopCards(params, commonFilter.toString(), queryParams);

            case "GET_USAGE_STATS":
                return getUsageStats(params, commonFilter.toString(), queryParams);

            case "GET_DECK_RANKINGS":
                return getDeckRankings(params, commonFilter.toString(), queryParams);

            case "ANALYZE_CARD_DETAIL":
                return analyzeCardDetail(params);

            case "GET_PLAYER_RANKINGS":
                return getPlayerRankings(params, commonFilter.toString(), queryParams);

            case "HOW_TO_BUILD":
                return howToBuild(params);

            case "HOW_TO_COUNTER":
                return howToCounter(params);

            default:
                // Trả về list rỗng thay vì null để đảm bảo an toàn cho JSON response
                return new ArrayList<>();
        }
    }

    private int getSafeLimit(Map<String, Object> params) {
        try {
            Object limitObj = params.get("limit");
            if (limitObj == null) return 10;
            return Integer.parseInt(limitObj.toString());
        } catch (Exception e) {
            return 10;
        }
    }

    // --- CÁC HÀM TRUY VẤN DATABASE ---

    private List<Map<String, Object>> howToBuild(Map<String, Object> params) {
        String cardName = (String) params.getOrDefault("card_name", "");
        String sql = "SELECT name, details FROM guide WHERE type = 'Build' AND name ILIKE ?";
        return db.queryForList(sql, "%" + cardName + "%");
    }

    private List<Map<String, Object>> howToCounter(Map<String, Object> params) {
        String cardName = (String) params.getOrDefault("card_name", "");
        String sql = "SELECT name, details FROM guide WHERE type = 'Counter' AND name ILIKE ?";
        return db.queryForList(sql, "%" + cardName + "%");
    }

    private List<Map<String, Object>> getTopCards(Map<String, Object> params, String commonFilter, List<Object> p) {
        String sortBy = "usage".equals(params.get("sort_by")) ? "COUNT(cu.id)" : "AVG(cu.winrate)";
        String colorFilter = "";
        if (params.get("color") != null && !"all".equalsIgnoreCase(params.get("color").toString())) {
            colorFilter = " AND c.color = ? ";
        }

        String sql = "SELECT c.name, c.color, c.rarity, AVG(cu.winrate) as winrate, COUNT(cu.id) as use_count " +
                     "FROM card c " +
                     "JOIN deck_info di ON c.id = di.cardid " +
                     "JOIN competition_use cu ON di.deckid = cu.deckid " +
                     "JOIN competition comp ON cu.competitionid = comp.id " +
                     commonFilter + colorFilter +
                     " GROUP BY c.id, c.name, c.color, c.rarity " +
                     " ORDER BY " + sortBy + " DESC " +
                     " LIMIT " + getSafeLimit(params);
        
        List<Object> args = new ArrayList<>(p);
        if (!colorFilter.isEmpty()) args.add(params.get("color").toString());
        return db.queryForList(sql, args.toArray());
    }

    private List<Map<String, Object>> getUsageStats(Map<String, Object> params, String commonFilter, List<Object> p) {
        String groupBy = "rarity".equals(params.get("view_mode")) ? "c.rarity" : "c.color";
        String measure = "total".equals(params.get("calc_mode")) ? "SUM(di.quantity)" : "COUNT(DISTINCT cu.deckid)";

        String sql = "SELECT " + groupBy + " as label, " + measure + " as value " +
                     "FROM card c " +
                     "JOIN deck_info di ON c.id = di.cardid " +
                     "JOIN competition_use cu ON di.deckid = cu.deckid " +
                     "JOIN competition comp ON cu.competitionid = comp.id " +
                     commonFilter + " GROUP BY " + groupBy;
        
        return db.queryForList(sql, p.toArray());
    }

    private List<Map<String, Object>> getDeckRankings(Map<String, Object> params, String commonFilter, List<Object> p) {
        String sortParam = (String) params.getOrDefault("sort_by", "usage");
        String orderBy = "winrate".equals(sortParam) ? "AVG(cu.winrate) DESC" : 
                         "rank".equals(sortParam) ? "AVG(cu.rank) ASC" : "COUNT(*) DESC";

        String sql = "SELECT d.name, AVG(cu.winrate) as avg_winrate, AVG(cu.rank) as avg_rank, COUNT(*) as usage_count " +
                     "FROM deck d " +
                     "JOIN competition_use cu ON d.id = cu.deckid " +
                     "JOIN competition comp ON cu.competitionid = comp.id " +
                     commonFilter + " GROUP BY d.id, d.name " +
                     " ORDER BY " + orderBy + 
                     " LIMIT " + getSafeLimit(params);
        
        return db.queryForList(sql, p.toArray());
    }

    private List<Map<String, Object>> analyzeCardDetail(Map<String, Object> params) {
        String cardName = (String) params.getOrDefault("card_name", "");
        String sql = "SELECT di.quantity, AVG(cu.winrate) as winrate, COUNT(*) as sample_size, MAX(c.url) as url " +
                     "FROM card c " + 
                     "JOIN deck_info di ON c.id = di.cardid " +
                     "JOIN competition_use cu ON di.deckid = cu.deckid " +
                     "WHERE c.name ILIKE ? " +
                     " GROUP BY di.quantity ORDER BY di.quantity";
        
        return db.queryForList(sql, "%" + cardName + "%");
    }

    private List<Map<String, Object>> getPlayerRankings(Map<String, Object> params, String commonFilter, List<Object> p) {
        String sortParam = (String) params.getOrDefault("sort_by", "winrate");
        String orderBy = "winrate".equals(sortParam) ? "AVG(cu.winrate) DESC" : "AVG(cu.rank) ASC";

        String sql = "SELECT u.username, AVG(cu.winrate) as winrate, AVG(cu.rank) as avg_rank, COUNT(*) as match_count " +
                     "FROM \"user\" u " + 
                     "JOIN competition_use cu ON u.id = cu.userid " +
                     "JOIN competition comp ON cu.competitionid = comp.id " +
                     commonFilter + " GROUP BY u.id, u.username " +
                     " ORDER BY " + orderBy + 
                     " LIMIT " + getSafeLimit(params);
        
        return db.queryForList(sql, p.toArray());
    }
}
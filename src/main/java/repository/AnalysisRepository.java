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
        
        // Nếu là action tĩnh, trả về dữ liệu từ CSV luôn, không vào Database
        if (config.isStatic()) {
            List<Map<String, Object>> result = new ArrayList<>();
            if (config.getStaticData() != null && !config.getStaticData().isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put("link", config.getStaticData()); // Trả về dạng JSON {"link": "..."}
                result.add(map);
            }
            return result;
        }
    	if (params == null) params = new HashMap<>();
        
        // Xử lý bộ lọc chung (Lấy trực tiếp từ params vì AI trả về phẳng)
        StringBuilder commonFilter = new StringBuilder(" WHERE 1=1 ");
        List<Object> queryParams = new ArrayList<>();

        // 1. Lọc theo vùng miền (N, S)
        if (params.get("region") != null && !"all".equals(params.get("region").toString())) {
            commonFilter.append(" AND comp.region = ? ");
            queryParams.add(params.get("region"));
        }
        
        // 2. Lọc theo ngày (Sử dụng đúng tên trường competition_date)
        if (params.get("startDate") != null) {
            commonFilter.append(" AND comp.competition_date >= ?::date ");
            queryParams.add(params.get("startDate"));
        }
        if (params.get("endDate") != null) {
            commonFilter.append(" AND comp.competition_date <= ?::date ");
            queryParams.add(params.get("endDate"));
        }

        System.out.println("--- Thực thi Action: " + action + " ---");
        System.out.println("Tham số nhận được: " + params);

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
            case "YOUTUBE_CHANNEL":
            	return getYoutubeLink();
            default:
                return new ArrayList<>();
        }
    }

    // 1. Top Card: Phân tích theo winrate hoặc số lượt dùng
    private List<Map<String, Object>> getTopCards(Map<String, Object> params, String commonFilter, List<Object> p) {
        String sortBy = "winrate".equals(params.get("sort_by")) ? "AVG(cu.winrate)" : "COUNT(cu.id)";
        
        String colorFilter = "";
        if (params.get("color") != null && !"all".equals(params.get("color").toString())) {
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
                     " LIMIT " + params.getOrDefault("limit", 10);
        
        List<Object> args = new ArrayList<>(p);
        if (!colorFilter.isEmpty()) args.add(params.get("color"));
        return db.queryForList(sql, args.toArray());
    }

    // 2. Tỷ lệ Meta: Theo Màu hoặc Độ hiếm
    private List<Map<String, Object>> getUsageStats(Map<String, Object> params, String commonFilter, List<Object> p) {
        String groupBy = "rarity".equals(params.get("view_mode")) ? "c.rarity" : "c.color";
        // 'total' tính theo tổng số bản copy, 'deck' tính theo số bộ bài xuất hiện
        String measure = "total".equals(params.get("calc_mode")) ? "SUM(di.quantity)" : "COUNT(DISTINCT cu.deckid)";

        String sql = "SELECT " + groupBy + " as label, " + measure + " as value " +
                     "FROM card c " +
                     "JOIN deck_info di ON c.id = di.cardid " +
                     "JOIN competition_use cu ON di.deckid = cu.deckid " +
                     "JOIN competition comp ON cu.competitionid = comp.id " +
                     commonFilter + " GROUP BY " + groupBy;
        
        return db.queryForList(sql, p.toArray());
    }

    // 3. Xếp hạng Bộ bài: Khớp sort_by từ AI
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
                     " LIMIT " + params.getOrDefault("limit", 10);
        
        return db.queryForList(sql, p.toArray());
    }

    // 4. Phân tích số lượng bản copy (1, 2, 3 bản)
    private List<Map<String, Object>> analyzeCardDetail(Map<String, Object> params) {
        String cardName = (String) params.getOrDefault("card_name", "");
        String sql = "SELECT di.quantity, AVG(cu.winrate) as winrate, COUNT(*) as sample_size, c.url " +
                     "FROM card c " +
                     "JOIN deck_info di ON c.id = di.cardid " +
                     "JOIN competition_use cu ON di.deckid = cu.deckid " +
                     "WHERE c.name ILIKE ? " +
                     " GROUP BY di.quantity ORDER BY di.quantity";
        
        return db.queryForList(sql, "%" + cardName + "%");
    }

    // 5. Xếp hạng người chơi (Sử dụng username)
    private List<Map<String, Object>> getPlayerRankings(Map<String, Object> params, String commonFilter, List<Object> p) {
        String sortParam = (String) params.getOrDefault("sort_by", "winrate");
        String orderBy = "winrate".equals(sortParam) ? "AVG(cu.winrate) DESC" : "AVG(cu.rank) ASC";

        String sql = "SELECT u.username, AVG(cu.winrate) as winrate, AVG(cu.rank) as avg_rank, COUNT(*) as match_count " +
                     "FROM \"user\" u " + // user là từ khóa cấm trong Postgres nên cần để trong dấu nháy
                     "JOIN competition_use cu ON u.id = cu.userid " +
                     "JOIN competition comp ON cu.competitionid = comp.id " +
                     commonFilter + " GROUP BY u.id, u.username " +
                     " ORDER BY " + orderBy + 
                     " LIMIT " + params.getOrDefault("limit", 10);
        
        return db.queryForList(sql, p.toArray());
    }
    
 // 6. Link Youtube
    private List<Map<String, Object>> getYoutubeLink() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> linkMap = new HashMap<>();
        
        linkMap.put("link", "https://www.youtube.com/@bb449-m2y");
        result.add(linkMap);
        
        return result;
    }
}
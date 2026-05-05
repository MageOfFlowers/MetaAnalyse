package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import model.Guide;

import java.util.List;

@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {
    
    // Tìm kiếm các guide theo loại (ví dụ lấy tất cả 'counter')
    List<Guide> findByType(String type);
    
    // Bạn có thể thêm các query tùy chỉnh ở đây
}

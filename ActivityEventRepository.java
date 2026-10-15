package com.focusflow.repository;
import com.focusflow.model.ActivityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ActivityEventRepository extends JpaRepository<ActivityEvent,Long> {
    List<ActivityEvent> findByClientIdAndStartedAtAfter(String clientId, LocalDateTime after);
    List<ActivityEvent> findByStartedAtAfter(LocalDateTime after);
    List<ActivityEvent> findTop100ByClientIdOrderByStartedAtDesc(String clientId);
    List<ActivityEvent> findTop100ByOrderByStartedAtDesc();
}

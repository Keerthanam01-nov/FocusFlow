package com.focusflow.repository;
import com.focusflow.model.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface FocusSessionRepository extends JpaRepository<FocusSession,Long> {
    List<FocusSession> findByClientIdAndStartedAtAfter(String clientId, LocalDateTime after);
    List<FocusSession> findByStartedAtAfter(LocalDateTime after);
}

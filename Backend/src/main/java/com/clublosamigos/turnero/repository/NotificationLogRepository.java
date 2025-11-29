package com.clublosamigos.turnero.repository;

import com.clublosamigos.turnero.model.NotificationLog;
import com.clublosamigos.turnero.model.NotificationLog.NotificationEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    boolean existsByEventTypeAndUserIdAndTrainingSessionId(NotificationEventType eventType, Long userId, Long trainingSessionId);
}

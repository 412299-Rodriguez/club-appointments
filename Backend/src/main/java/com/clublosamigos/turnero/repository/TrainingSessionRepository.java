package com.clublosamigos.turnero.repository;

import com.clublosamigos.turnero.model.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    List<TrainingSession> findByIsDeletedFalse();

    List<TrainingSession> findByIsDeletedFalseOrderByDateAscStartTimeAsc();

    List<TrainingSession> findByDateAndIsDeletedFalse(LocalDate date);

    List<TrainingSession> findByTrainerIdAndIsDeletedFalse(Long trainerId);

    List<TrainingSession> findByDateBetweenAndIsDeletedFalse(LocalDate startDate, LocalDate endDate);

    Optional<TrainingSession> findByIdAndIsDeletedFalse(Long id);

    @Query("""
            SELECT ts FROM TrainingSession ts
            WHERE ts.isDeleted = false
              AND ts.status = 'ACTIVE'
              AND ts.date >= :currentDate
            ORDER BY ts.date ASC, ts.startTime ASC
            """)
    List<TrainingSession> findUpcomingSessions(LocalDate currentDate);

    @Query("""
            SELECT ts FROM TrainingSession ts WHERE ts.isDeleted = false
              AND (LOWER(ts.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(ts.location) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                OR LOWER(ts.trainer.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            """)
    List<TrainingSession> searchTrainingSessions(String searchTerm);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.trainingSession.id = :sessionId
              AND b.status = 'CONFIRMED'
              AND b.isDeleted = false
            """)
    Long countCurrentParticipants(Long sessionId);
}

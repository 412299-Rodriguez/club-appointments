package com.clublosamigos.turnero.repository;

import com.clublosamigos.turnero.model.Booking;
import com.clublosamigos.turnero.model.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByUserIdAndTrainingSessionIdAndIsDeletedFalse(Long userId, Long trainingSessionId);

    List<Booking> findByIsDeletedFalse();

    Optional<Booking> findByIdAndIsDeletedFalse(Long id);

    List<Booking> findByUserIdAndIsDeletedFalse(Long userId);

    List<Booking> findByTrainingSessionIdAndIsDeletedFalse(Long trainingSessionId);

    List<Booking> findByTrainingSessionIdAndStatusAndIsDeletedFalse(Long trainingSessionId, BookingStatus status);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.user.id = :userId
              AND b.isDeleted = false
              AND b.status = 'CONFIRMED'
              AND b.trainingSession.date >= CURRENT_DATE
            ORDER BY b.trainingSession.date ASC, b.trainingSession.startTime ASC
            """)
    List<Booking> findUpcomingBookingsByUser(Long userId);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.trainingSession.id = :trainingSessionId
              AND b.status = 'CONFIRMED'
              AND b.isDeleted = false
            """)
    Long countConfirmedBookings(Long trainingSessionId);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b
            WHERE b.user.id = :userId
              AND b.isDeleted = false
              AND b.status = 'CONFIRMED'
              AND b.trainingSession.date = :date
              AND b.trainingSession.startTime < :endTime
              AND b.trainingSession.endTime > :startTime
            """)
    boolean hasOverlappingBooking(Long userId, java.time.LocalDate date, java.time.LocalTime startTime, java.time.LocalTime endTime);

    List<Booking> findByTrainingSession_DateAndStatusAndIsDeletedFalse(LocalDate date, BookingStatus status);
}

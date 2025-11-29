#!/bin/bash

# Create all Response DTOs
cat > src/main/java/com/clublosamigos/turnero/dto/response/UserResponse.java << 'EOF'
package com.clublosamigos.turnero.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
}
EOF

cat > src/main/java/com/clublosamigos/turnero/dto/response/TrainingSessionResponse.java << 'EOF'
package com.clublosamigos.turnero.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSessionResponse {
    private Long id;
    private String name;
    private String description;
    private UserResponse trainer;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String status;
    private List<BookingResponse> bookings;
}
EOF

cat > src/main/java/com/clublosamigos/turnero/dto/response/BookingResponse.java << 'EOF'
package com.clublosamigos.turnero.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private UserResponse user;
    private String status;
    private LocalDateTime createdAt;
}
EOF

echo "Response DTOs created"

# Create Repositories
cat > src/main/java/com/clublosamigos/turnero/repository/UserRepository.java << 'EOF'
package com.clublosamigos.turnero.repository;

import com.clublosamigos.turnero.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    boolean existsByEmail(String email);
}
EOF

cat > src/main/java/com/clublosamigos/turnero/repository/TrainingSessionRepository.java << 'EOF'
package com.clublosamigos.turnero.repository;

import com.clublosamigos.turnero.model.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    List<TrainingSession> findByIsDeletedFalseOrderByDateAscStartTimeAsc();
    List<TrainingSession> findByDateAndIsDeletedFalseOrderByStartTimeAsc(LocalDate date);
    
    @Query("SELECT ts FROM TrainingSession ts WHERE ts.isDeleted = false " +
           "AND (LOWER(ts.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(ts.location) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(ts.trainer.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<TrainingSession> searchTrainingSessions(String searchTerm);
}
EOF

cat > src/main/java/com/clublosamigos/turnero/repository/BookingRepository.java << 'EOF'
package com.clublosamigos.turnero.repository;

import com.clublosamigos.turnero.model.Booking;
import com.clublosamigos.turnero.model.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdAndIsDeletedFalseOrderByTrainingSession_DateDesc(Long userId);
    List<Booking> findByTrainingSessionIdAndStatusAndIsDeletedFalse(Long trainingSessionId, BookingStatus status);
    Optional<Booking> findByUserIdAndTrainingSessionIdAndIsDeletedFalse(Long userId, Long trainingSessionId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.trainingSession.id = :trainingSessionId " +
           "AND b.status = 'CONFIRMED' AND b.isDeleted = false")
    Long countConfirmedBookingsByTrainingSessionId(Long trainingSessionId);
}
EOF

cat > src/main/java/com/clublosamigos/turnero/repository/SlotConfigurationRepository.java << 'EOF'
package com.clublosamigos.turnero.repository;

import com.clublosamigos.turnero.model.SlotConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotConfigurationRepository extends JpaRepository<SlotConfiguration, Long> {
}
EOF

echo "Repositories created"

echo "All backend infrastructure files created!"

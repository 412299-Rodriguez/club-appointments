package com.clublosamigos.turnero.dto.response;

import com.clublosamigos.turnero.model.TrainingSession.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private SessionStatus status;
    private List<BookingResponse> bookings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;
    private Long slotConfigurationId;
}

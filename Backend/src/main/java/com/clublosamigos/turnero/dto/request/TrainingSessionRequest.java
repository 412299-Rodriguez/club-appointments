package com.clublosamigos.turnero.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Training session create/update request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSessionRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 5, max = 100, message = "Name must be between 5 and 100 characters")
    private String name;

    private String description;

    @NotNull(message = "Trainer ID is required")
    private Long trainerId;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date must be in the present or future")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Max participants is required")
    @Min(value = 1, message = "Max participants must be at least 1")
    @Max(value = 8, message = "Max participants cannot exceed 8")
    private Integer maxParticipants;

    private Long slotConfigId;
}

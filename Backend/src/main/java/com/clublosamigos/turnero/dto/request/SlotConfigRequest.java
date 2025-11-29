package com.clublosamigos.turnero.dto.request;

import com.clublosamigos.turnero.model.SlotConfiguration.RecurrenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating and updating slot configurations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotConfigRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Recurrence type is required")
    private RecurrenceType recurrenceType;

    @Size(max = 20, message = "Days of week must not exceed 20 characters")
    private String daysOfWeek; // Comma-separated: "1,3,5" = Mon, Wed, Fri

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}

package com.clublosamigos.turnero.service;

import com.clublosamigos.turnero.dto.request.SlotConfigRequest;
import com.clublosamigos.turnero.dto.request.SlotGenerationRequest;
import com.clublosamigos.turnero.dto.request.TrainingSessionRequest;
import com.clublosamigos.turnero.dto.response.TrainingSessionResponse;
import com.clublosamigos.turnero.exception.BadRequestException;
import com.clublosamigos.turnero.exception.ResourceNotFoundException;
import com.clublosamigos.turnero.model.SlotConfiguration;
import com.clublosamigos.turnero.model.SlotConfiguration.RecurrenceType;
import com.clublosamigos.turnero.repository.SlotConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for slot configuration management operations
 */
@Service
@RequiredArgsConstructor
public class SlotConfigurationService {

    private final SlotConfigurationRepository slotConfigurationRepository;
    private final TrainingSessionService trainingSessionService;

    /**
     * Create a new slot configuration
     *
     * @param request SlotConfigRequest
     * @return SlotConfiguration
     * @throws BadRequestException if validation fails
     */
    @Transactional
    public SlotConfiguration createSlotConfiguration(SlotConfigRequest request) {
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        // Create slot configuration
        SlotConfiguration slotConfig = SlotConfiguration.builder()
                .name(request.getName())
                .recurrenceType(request.getRecurrenceType())
                .daysOfWeek(request.getDaysOfWeek())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isDeleted(false)
                .build();

        return slotConfigurationRepository.save(slotConfig);
    }

    /**
     * Update an existing slot configuration
     *
     * @param id Slot configuration ID
     * @param request SlotConfigRequest
     * @return Updated SlotConfiguration
     * @throws ResourceNotFoundException if configuration not found
     * @throws BadRequestException if validation fails
     */
    @Transactional
    public SlotConfiguration updateSlotConfiguration(Long id, SlotConfigRequest request) {
        SlotConfiguration slotConfig = slotConfigurationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot configuration not found with id: " + id));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        // Update configuration
        slotConfig.setName(request.getName());
        slotConfig.setRecurrenceType(request.getRecurrenceType());
        slotConfig.setDaysOfWeek(request.getDaysOfWeek());
        slotConfig.setStartDate(request.getStartDate());
        slotConfig.setEndDate(request.getEndDate());

        return slotConfigurationRepository.save(slotConfig);
    }

    /**
     * Get all slot configurations
     *
     * @return List of SlotConfiguration
     */
    @Transactional(readOnly = true)
    public List<SlotConfiguration> getAllSlotConfigurations() {
        return slotConfigurationRepository.findByIsDeletedFalse();
    }

    /**
     * Get slot configuration by ID
     *
     * @param id Slot configuration ID
     * @return SlotConfiguration
     * @throws ResourceNotFoundException if configuration not found
     */
    @Transactional(readOnly = true)
    public SlotConfiguration getSlotConfigurationById(Long id) {
        return slotConfigurationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot configuration not found with id: " + id));
    }

    /**
     * Soft delete a slot configuration
     *
     * @param id Slot configuration ID
     * @throws ResourceNotFoundException if configuration not found
     */
    @Transactional
    public void deleteSlotConfiguration(Long id) {
        SlotConfiguration slotConfig = slotConfigurationRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot configuration not found with id: " + id));
        slotConfig.setIsDeleted(true);
        slotConfigurationRepository.save(slotConfig);
    }

    /**
     * Generate training sessions based on slot configuration recurrence.
     */
    @Transactional
    public List<TrainingSessionResponse> generateTrainingSessions(Long slotConfigId, SlotGenerationRequest templateRequest) {
        SlotConfiguration slotConfiguration = slotConfigurationRepository.findByIdAndIsDeletedFalse(slotConfigId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot configuration not found with id: " + slotConfigId));

        if (slotConfiguration.getStartDate().isAfter(slotConfiguration.getEndDate())) {
            throw new BadRequestException("Slot configuration dates are invalid");
        }

        List<LocalDate> dates = computeDates(slotConfiguration);
        List<TrainingSessionResponse> createdSessions = new ArrayList<>();

        for (LocalDate date : dates) {
            TrainingSessionRequest sessionRequest = TrainingSessionRequest.builder()
                    .name(templateRequest.getName())
                    .description(templateRequest.getDescription())
                    .trainerId(templateRequest.getTrainerId())
                    .date(date)
                    .startTime(templateRequest.getStartTime())
                    .endTime(templateRequest.getEndTime())
                    .location(templateRequest.getLocation())
                    .maxParticipants(templateRequest.getMaxParticipants())
                    .slotConfigId(slotConfigId)
                    .build();
            try {
                createdSessions.add(trainingSessionService.createTrainingSession(sessionRequest));
            } catch (BadRequestException ex) {
                // Skip conflicting sessions but continue generating others
            }
        }

        return createdSessions;
    }

    private List<LocalDate> computeDates(SlotConfiguration slotConfiguration) {
        List<LocalDate> dates = new ArrayList<>();
        Set<Integer> dayFilters = parseDays(slotConfiguration.getDaysOfWeek());

        LocalDate current = slotConfiguration.getStartDate();
        while (!current.isAfter(slotConfiguration.getEndDate())) {
            if (matchesRecurrence(slotConfiguration.getRecurrenceType(), current, dayFilters)) {
                dates.add(current);
            }
            current = current.plusDays(1);
        }
        return dates;
    }

    private Set<Integer> parseDays(String daysOfWeek) {
        Set<Integer> result = new HashSet<>();
        if (daysOfWeek == null || daysOfWeek.isBlank()) {
            return result;
        }

        String[] tokens = daysOfWeek.split(",");
        for (String token : tokens) {
            try {
                result.add(Integer.parseInt(token.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    private boolean matchesRecurrence(RecurrenceType recurrenceType, LocalDate date, Set<Integer> dayFilters) {
        if (recurrenceType == RecurrenceType.MONTHLY) {
            if (dayFilters.isEmpty()) {
                return true;
            }
            return dayFilters.contains(date.getDayOfMonth());
        }

        if (dayFilters.isEmpty()) {
            return true;
        }

        DayOfWeek dow = date.getDayOfWeek();
        return dayFilters.contains(dow.getValue());
    }
}

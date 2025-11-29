package com.clublosamigos.turnero.service;

import com.clublosamigos.turnero.dto.request.TrainingSessionRequest;
import com.clublosamigos.turnero.dto.response.TrainingSessionResponse;
import com.clublosamigos.turnero.exception.BadRequestException;
import com.clublosamigos.turnero.exception.ResourceNotFoundException;
import com.clublosamigos.turnero.model.TrainingSession;
import com.clublosamigos.turnero.model.TrainingSession.SessionStatus;
import com.clublosamigos.turnero.model.SlotConfiguration;
import com.clublosamigos.turnero.model.User;
import com.clublosamigos.turnero.service.NotificationService;
import com.clublosamigos.turnero.repository.SlotConfigurationRepository;
import com.clublosamigos.turnero.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for training session management operations
 */
@Service
@RequiredArgsConstructor
public class TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final SlotConfigurationRepository slotConfigurationRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * Create a new training session
     *
     * @param request TrainingSessionRequest
     * @return TrainingSessionResponse
     * @throws ResourceNotFoundException if trainer not found
     * @throws BadRequestException if validation fails
     */
    @Transactional
    public TrainingSessionResponse createTrainingSession(TrainingSessionRequest request) {
        // Validate trainer
        User trainer = userService.getUserEntityById(request.getTrainerId());

        if (trainer.getRole() != User.UserRole.ENTRENADOR && trainer.getRole() != User.UserRole.SUPER_ADMIN) {
            throw new BadRequestException("Only trainers can create training sessions");
        }

        validateSchedule(request.getDate(), request.getStartTime(), request.getEndTime());

        SlotConfiguration slotConfiguration = resolveSlotConfiguration(request.getSlotConfigId());

        TrainingSession session = TrainingSession.builder()
                .name(request.getName())
                .description(request.getDescription())
                .trainer(trainer)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .maxParticipants(request.getMaxParticipants())
                .slotConfiguration(slotConfiguration)
                .status(SessionStatus.ACTIVE)
                .isDeleted(false)
                .build();

        session = trainingSessionRepository.save(session);
        notificationService.sendSessionModified(session);
        return convertToResponse(session);
    }

    /**
     * Update an existing training session
     *
     * @param id Training session ID
     * @param request TrainingSessionRequest
     * @return Updated TrainingSessionResponse
     * @throws ResourceNotFoundException if session not found
     * @throws BadRequestException if validation fails
     */
    @Transactional
    public TrainingSessionResponse updateTrainingSession(Long id, TrainingSessionRequest request) {
        TrainingSession session = trainingSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));

        // Validate trainer if changed
        if (request.getTrainerId() != null && !request.getTrainerId().equals(session.getTrainer().getId())) {
            User trainer = userService.getUserEntityById(request.getTrainerId());
            if (trainer.getRole() != User.UserRole.ENTRENADOR && trainer.getRole() != User.UserRole.SUPER_ADMIN) {
                throw new BadRequestException("Only trainers can be assigned to training sessions");
            }
            session.setTrainer(trainer);
        }

        validateSchedule(request.getDate(), request.getStartTime(), request.getEndTime());

        SlotConfiguration slotConfiguration = resolveSlotConfiguration(request.getSlotConfigId());

        // Update session
        session.setName(request.getName());
        session.setDescription(request.getDescription());
        session.setDate(request.getDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setLocation(request.getLocation());
        session.setMaxParticipants(request.getMaxParticipants());
        session.setSlotConfiguration(slotConfiguration);

        session = trainingSessionRepository.save(session);
        notificationService.sendSessionModified(session);
        return convertToResponse(session);
    }

    /**
     * Get all training sessions
     *
     * @return List of TrainingSessionResponse
     */
    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> getAllTrainingSessions() {
        return trainingSessionRepository.findByIsDeletedFalse().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get training session by ID
     *
     * @param id Training session ID
     * @return TrainingSessionResponse
     * @throws ResourceNotFoundException if session not found
     */
    @Transactional(readOnly = true)
    public TrainingSessionResponse getTrainingSessionById(Long id) {
        TrainingSession session = trainingSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));
        return convertToResponse(session);
    }

    /**
     * Search training sessions by term
     */
    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> searchTrainingSessions(String term) {
        return trainingSessionRepository.searchTrainingSessions(term).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get training session entity by ID (for internal use)
     *
     * @param id Training session ID
     * @return TrainingSession entity
     * @throws ResourceNotFoundException if session not found
     */
    @Transactional(readOnly = true)
    public TrainingSession getTrainingSessionEntityById(Long id) {
        return trainingSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));
    }

    /**
     * Get training sessions by trainer ID
     *
     * @param trainerId Trainer ID
     * @return List of TrainingSessionResponse
     */
    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> getTrainingSessionsByTrainer(Long trainerId) {
        return trainingSessionRepository.findByTrainerIdAndIsDeletedFalse(trainerId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get training sessions by date
     *
     * @param date Date
     * @return List of TrainingSessionResponse
     */
    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> getTrainingSessionsByDate(LocalDate date) {
        return trainingSessionRepository.findByDateAndIsDeletedFalse(date).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get training sessions by date range
     *
     * @param startDate Start date
     * @param endDate End date
     * @return List of TrainingSessionResponse
     */
    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> getTrainingSessionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return trainingSessionRepository.findByDateBetweenAndIsDeletedFalse(startDate, endDate).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming training sessions
     *
     * @return List of TrainingSessionResponse
     */
    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> getUpcomingTrainingSessions() {
        return trainingSessionRepository.findUpcomingSessions(LocalDate.now()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel a training session
     *
     * @param id Training session ID
     * @return Updated TrainingSessionResponse
     * @throws ResourceNotFoundException if session not found
     */
    @Transactional
    public TrainingSessionResponse cancelTrainingSession(Long id) {
        TrainingSession session = trainingSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));
        session.setStatus(SessionStatus.CANCELLED);
        session = trainingSessionRepository.save(session);
        notificationService.sendSessionCancellationToParticipants(session);
        return convertToResponse(session);
    }

    /**
     * Soft delete a training session
     *
     * @param id Training session ID
     * @throws ResourceNotFoundException if session not found
     */
    @Transactional
    public void deleteTrainingSession(Long id) {
        TrainingSession session = trainingSessionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));
        session.setIsDeleted(true);
        trainingSessionRepository.save(session);
        notificationService.sendSessionCancellationToParticipants(session);
    }

    /**
     * Convert TrainingSession entity to TrainingSessionResponse DTO
     *
     * @param session TrainingSession entity
     * @return TrainingSessionResponse DTO
     */
    private TrainingSessionResponse convertToResponse(TrainingSession session) {
        Long currentParticipants = trainingSessionRepository.countCurrentParticipants(session.getId());
        int participantCount = currentParticipants != null ? currentParticipants.intValue() : 0;

        return TrainingSessionResponse.builder()
                .id(session.getId())
                .name(session.getName())
                .description(session.getDescription())
                .trainer(userService.getUserById(session.getTrainer().getId()))
                .date(session.getDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .location(session.getLocation())
                .maxParticipants(session.getMaxParticipants())
                .currentParticipants(participantCount)
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .deleted(Boolean.TRUE.equals(session.getIsDeleted()))
                .slotConfigurationId(session.getSlotConfiguration() != null ? session.getSlotConfiguration().getId() : null)
                .build();
    }

    private void validateSchedule(LocalDate date, java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new BadRequestException("End time must be after start time");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Date cannot be in the past");
        }
    }

    private SlotConfiguration resolveSlotConfiguration(Long slotConfigId) {
        if (slotConfigId == null) {
            return null;
        }
        return slotConfigurationRepository.findByIdAndIsDeletedFalse(slotConfigId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot configuration not found with id: " + slotConfigId));
    }
}

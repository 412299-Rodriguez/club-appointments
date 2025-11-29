package com.clublosamigos.turnero.controller;

import com.clublosamigos.turnero.dto.request.TrainingSessionRequest;
import com.clublosamigos.turnero.dto.response.TrainingSessionResponse;
import com.clublosamigos.turnero.service.TrainingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for training session management operations
 */
@RestController
@RequestMapping("/api/training-sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TrainingSessionController {

    private final TrainingSessionService trainingSessionService;

    /**
     * Get all training sessions (PUBLIC - no authentication required)
     *
     * @param search Optional search term
     * @return List of TrainingSessionResponse
     */
    @GetMapping
    public ResponseEntity<List<TrainingSessionResponse>> getAllTrainingSessions(
            @RequestParam(required = false) String search) {
        List<TrainingSessionResponse> sessions;
        if (search != null && !search.isEmpty()) {
            sessions = trainingSessionService.searchTrainingSessions(search);
        } else {
            sessions = trainingSessionService.getAllTrainingSessions();
        }
        return ResponseEntity.ok(sessions);
    }

    /**
     * Search training sessions (PUBLIC - accessible from /training-sessions?search=term)
     * This endpoint is for explicit /search path if needed
     *
     * @param term Search term
     * @return List of TrainingSessionResponse
     */
    @GetMapping("/search")
    public ResponseEntity<List<TrainingSessionResponse>> searchTrainingSessions(
            @RequestParam String term) {
        List<TrainingSessionResponse> sessions = trainingSessionService.searchTrainingSessions(term);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get training session by ID (PUBLIC for viewing)
     *
     * @param id Training session ID
     * @return TrainingSessionResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrainingSessionResponse> getTrainingSessionById(@PathVariable Long id) {
        TrainingSessionResponse session = trainingSessionService.getTrainingSessionById(id);
        return ResponseEntity.ok(session);
    }

    /**
     * Create a new training session (SUPER_ADMIN and ENTRENADOR only)
     *
     * @param request TrainingSessionRequest
     * @return Created TrainingSessionResponse
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<TrainingSessionResponse> createTrainingSession(@Valid @RequestBody TrainingSessionRequest request) {
        TrainingSessionResponse response = trainingSessionService.createTrainingSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing training session (SUPER_ADMIN and ENTRENADOR only)
     *
     * @param id Training session ID
     * @param request TrainingSessionRequest
     * @return Updated TrainingSessionResponse
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<TrainingSessionResponse> updateTrainingSession(@PathVariable Long id, @Valid @RequestBody TrainingSessionRequest request) {
        TrainingSessionResponse response = trainingSessionService.updateTrainingSession(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get training sessions by trainer ID (requires authentication)
     *
     * @param trainerId Trainer ID
     * @return List of TrainingSessionResponse
     */
    @GetMapping("/trainer/{trainerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrainingSessionResponse>> getTrainingSessionsByTrainer(@PathVariable Long trainerId) {
        List<TrainingSessionResponse> sessions = trainingSessionService.getTrainingSessionsByTrainer(trainerId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get training sessions by date (requires authentication)
     *
     * @param date Date in format yyyy-MM-dd
     * @return List of TrainingSessionResponse
     */
    @GetMapping("/date/{date}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrainingSessionResponse>> getTrainingSessionsByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TrainingSessionResponse> sessions = trainingSessionService.getTrainingSessionsByDate(date);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get training sessions by date range (requires authentication)
     *
     * @param startDate Start date in format yyyy-MM-dd
     * @param endDate End date in format yyyy-MM-dd
     * @return List of TrainingSessionResponse
     */
    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrainingSessionResponse>> getTrainingSessionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TrainingSessionResponse> sessions = trainingSessionService.getTrainingSessionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get upcoming training sessions (requires authentication)
     *
     * @return List of TrainingSessionResponse
     */
    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrainingSessionResponse>> getUpcomingTrainingSessions() {
        List<TrainingSessionResponse> sessions = trainingSessionService.getUpcomingTrainingSessions();
        return ResponseEntity.ok(sessions);
    }

    /**
     * Cancel a training session (SUPER_ADMIN and ENTRENADOR only)
     *
     * @param id Training session ID
     * @return Updated TrainingSessionResponse
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<TrainingSessionResponse> cancelTrainingSession(@PathVariable Long id) {
        TrainingSessionResponse response = trainingSessionService.cancelTrainingSession(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a training session (SUPER_ADMIN only)
     *
     * @param id Training session ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> deleteTrainingSession(@PathVariable Long id) {
        trainingSessionService.deleteTrainingSession(id);
        return ResponseEntity.ok("Training session deleted successfully");
    }
}

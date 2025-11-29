package com.clublosamigos.turnero.controller;

import com.clublosamigos.turnero.dto.request.SlotConfigRequest;
import com.clublosamigos.turnero.dto.request.SlotGenerationRequest;
import com.clublosamigos.turnero.dto.response.TrainingSessionResponse;
import com.clublosamigos.turnero.model.SlotConfiguration;
import com.clublosamigos.turnero.service.SlotConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for slot configuration management operations
 */
@RestController
@RequestMapping("/api/slot-configurations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SlotConfigurationController {

    private final SlotConfigurationService slotConfigurationService;

    /**
     * Create a new slot configuration (SUPER_ADMIN and ENTRENADOR only)
     *
     * @param request SlotConfigRequest
     * @return Created SlotConfiguration
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<SlotConfiguration> createSlotConfiguration(@Valid @RequestBody SlotConfigRequest request) {
        SlotConfiguration response = slotConfigurationService.createSlotConfiguration(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing slot configuration (SUPER_ADMIN and ENTRENADOR only)
     *
     * @param id Slot configuration ID
     * @param request SlotConfigRequest
     * @return Updated SlotConfiguration
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<SlotConfiguration> updateSlotConfiguration(@PathVariable Long id, @Valid @RequestBody SlotConfigRequest request) {
        SlotConfiguration response = slotConfigurationService.updateSlotConfiguration(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all slot configurations
     *
     * @return List of SlotConfiguration
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<List<SlotConfiguration>> getAllSlotConfigurations() {
        List<SlotConfiguration> configurations = slotConfigurationService.getAllSlotConfigurations();
        return ResponseEntity.ok(configurations);
    }

    /**
     * Get slot configuration by ID
     *
     * @param id Slot configuration ID
     * @return SlotConfiguration
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<SlotConfiguration> getSlotConfigurationById(@PathVariable Long id) {
        SlotConfiguration configuration = slotConfigurationService.getSlotConfigurationById(id);
        return ResponseEntity.ok(configuration);
    }

    /**
     * Generate training sessions from a slot configuration
     *
     * @param id Slot configuration ID
     * @param request Template for training sessions
     * @return List of generated TrainingSessionResponse
     */
    @PostMapping("/{id}/generate")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<List<TrainingSessionResponse>> generateTrainingSessions(
            @PathVariable Long id,
            @Valid @RequestBody SlotGenerationRequest request) {
        List<TrainingSessionResponse> sessions = slotConfigurationService.generateTrainingSessions(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sessions);
    }

    /**
     * Delete a slot configuration (SUPER_ADMIN only)
     *
     * @param id Slot configuration ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> deleteSlotConfiguration(@PathVariable Long id) {
        slotConfigurationService.deleteSlotConfiguration(id);
        return ResponseEntity.ok("Slot configuration deleted successfully");
    }
}

package com.clublosamigos.turnero.controller;

import com.clublosamigos.turnero.dto.request.BookingRequest;
import com.clublosamigos.turnero.dto.response.BookingResponse;
import com.clublosamigos.turnero.service.BookingService;
import com.clublosamigos.turnero.security.CustomUserDetailsService;
import com.clublosamigos.turnero.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for booking management operations
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Create a new booking
     *
     * @param request BookingRequest
     * @return Created BookingResponse
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        Long userId = getUserIdFromAuthentication();
        BookingResponse response = bookingService.createBooking(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all bookings (SUPER_ADMIN and ENTRENADOR only)
     *
     * @return List of BookingResponse
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get booking by ID
     *
     * @param id Booking ID
     * @return BookingResponse
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        BookingResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    /**
     * Get bookings by user ID
     *
     * @param userId User ID
     * @return List of BookingResponse
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable Long userId) {
        List<BookingResponse> bookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get my bookings (authenticated user's bookings)
     *
     * @return List of BookingResponse
     */
    @GetMapping("/my-bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        Long userId = getUserIdFromAuthentication();
        List<BookingResponse> bookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get upcoming bookings for authenticated user
     *
     * @return List of BookingResponse
     */
    @GetMapping("/my-upcoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponse>> getMyUpcomingBookings() {
        Long userId = getUserIdFromAuthentication();
        List<BookingResponse> bookings = bookingService.getUpcomingBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get bookings by training session ID
     *
     * @param sessionId Training session ID
     * @return List of BookingResponse
     */
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ENTRENADOR')")
    public ResponseEntity<List<BookingResponse>> getBookingsByTrainingSession(@PathVariable Long sessionId) {
        List<BookingResponse> bookings = bookingService.getBookingsByTrainingSession(sessionId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cancel a booking
     *
     * @param id Booking ID
     * @return Updated BookingResponse
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        Long userId = getUserIdFromAuthentication();
        BookingResponse response = bookingService.cancelBooking(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a booking (SUPER_ADMIN only)
     *
     * @param id Booking ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    /**
     * Helper method to extract user ID from authentication context
     * Gets the email from the JWT token and loads the user entity
     *
     * @return User ID from authenticated user
     */
    private Long getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Username is the email
        User user = userDetailsService.loadUserEntityByEmail(email);
        return user.getId();
    }
}

package com.clublosamigos.turnero.service;

import com.clublosamigos.turnero.dto.request.BookingRequest;
import com.clublosamigos.turnero.dto.response.BookingResponse;
import com.clublosamigos.turnero.exception.BadRequestException;
import com.clublosamigos.turnero.exception.ResourceNotFoundException;
import com.clublosamigos.turnero.model.Booking;
import com.clublosamigos.turnero.model.Booking.BookingStatus;
import com.clublosamigos.turnero.model.TrainingSession;
import com.clublosamigos.turnero.model.User;
import com.clublosamigos.turnero.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for booking management operations
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final int CANCELLATION_LIMIT_HOURS = 2;

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final TrainingSessionService trainingSessionService;
    private final NotificationService notificationService;

    /**
     * Create a new booking
     *
     * @param userId User ID making the booking
     * @param request BookingRequest
     * @return BookingResponse
     * @throws ResourceNotFoundException if user or session not found
     * @throws BadRequestException if booking validation fails
     */
    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        // Get user and training session
        User user = userService.getUserEntityById(userId);
        TrainingSession session = trainingSessionService.getTrainingSessionEntityById(request.getTrainingSessionId());

        validateBookingRules(user, session);

        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .trainingSession(session)
                .status(BookingStatus.CONFIRMED)
                .isDeleted(false)
                .build();

        booking = bookingRepository.save(booking);

        notificationService.sendBookingConfirmation(user, session);

        return convertToResponse(booking);
    }

    /**
     * Get all bookings
     *
     * @return List of BookingResponse
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findByIsDeletedFalse().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get booking by ID
     *
     * @param id Booking ID
     * @return BookingResponse
     * @throws ResourceNotFoundException if booking not found
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return convertToResponse(booking);
    }

    /**
     * Get bookings by user ID
     *
     * @param userId User ID
     * @return List of BookingResponse
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserIdAndIsDeletedFalse(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get bookings by training session ID
     *
     * @param sessionId Training session ID
     * @return List of BookingResponse
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByTrainingSession(Long sessionId) {
        return bookingRepository.findByTrainingSessionIdAndIsDeletedFalse(sessionId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming bookings for a user
     *
     * @param userId User ID
     * @return List of BookingResponse
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookingsByUser(Long userId) {
        return bookingRepository.findUpcomingBookingsByUser(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel a booking
     *
     * @param id Booking ID
     * @param userId User ID requesting cancellation
     * @return Updated BookingResponse
     * @throws ResourceNotFoundException if booking not found
     * @throws BadRequestException if user is not authorized to cancel
     */
    @Transactional
    public BookingResponse cancelBooking(Long id, Long userId) {
        Booking booking = bookingRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        // Verify user owns this booking
        if (!booking.getUser().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to cancel this booking");
        }

        ensureCancellationWindow(booking.getTrainingSession());

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        // Send notification
        notificationService.sendBookingCancellation(booking.getUser(), booking.getTrainingSession());

        return convertToResponse(booking);
    }

    /**
     * Soft delete a booking
     *
     * @param id Booking ID
     * @throws ResourceNotFoundException if booking not found
     */
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        booking.setIsDeleted(true);
        bookingRepository.save(booking);
    }

    /**
     * Convert Booking entity to BookingResponse DTO
     *
     * @param booking Booking entity
     * @return BookingResponse DTO
     */
    private BookingResponse convertToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .user(userService.getUserById(booking.getUser().getId()))
                .trainingSession(trainingSessionService.getTrainingSessionById(booking.getTrainingSession().getId()))
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    /**
     * Apply booking business rules before persisting a booking.
     */
    private void validateBookingRules(User user, TrainingSession session) {
        if (session.getStatus() != TrainingSession.SessionStatus.ACTIVE || Boolean.TRUE.equals(session.getIsDeleted())) {
            throw new BadRequestException("Cannot book a session that is not active");
        }

        LocalDate today = LocalDate.now();
        if (session.getDate().isBefore(today)) {
            throw new BadRequestException("Cannot book a training session that already happened");
        }

        LocalDateTime sessionStart = session.getDate().atTime(session.getStartTime());
        if (sessionStart.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot book a training session that already started");
        }

        if (bookingRepository.existsByUserIdAndTrainingSessionIdAndIsDeletedFalse(user.getId(), session.getId())) {
            throw new BadRequestException("You have already booked this training session");
        }

        if (bookingRepository.hasOverlappingBooking(user.getId(), session.getDate(), session.getStartTime(), session.getEndTime())) {
            throw new BadRequestException("You already have another booking for this time slot");
        }

        Long currentBookings = bookingRepository.countConfirmedBookings(session.getId());
        int maxParticipants = session.getMaxParticipants() != null ? session.getMaxParticipants() : 8;
        if (currentBookings >= maxParticipants) {
            throw new BadRequestException("Training session is full");
        }
    }

    /**
     * Ensures the cancellation is performed at least 2 hours prior to the start.
     */
    private void ensureCancellationWindow(TrainingSession session) {
        LocalDateTime sessionStart = session.getDate().atTime(session.getStartTime());
        if (LocalDateTime.now().isAfter(sessionStart.minusHours(CANCELLATION_LIMIT_HOURS))) {
            throw new BadRequestException("Cancellations must be made at least " + CANCELLATION_LIMIT_HOURS + " hours in advance");
        }
    }
}

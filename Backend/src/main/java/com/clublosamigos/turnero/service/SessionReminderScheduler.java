package com.clublosamigos.turnero.service;

import com.clublosamigos.turnero.model.Booking;
import com.clublosamigos.turnero.model.NotificationLog.NotificationEventType;
import com.clublosamigos.turnero.repository.BookingRepository;
import com.clublosamigos.turnero.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionReminderScheduler {

    private final BookingRepository bookingRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationService notificationService;

    @Value("${notifications.reminder.lead-hours:24}")
    private long reminderLeadHours;

    @Value("${notifications.reminder.window-minutes:60}")
    private long reminderWindowMinutes;

    @Scheduled(cron = "${notifications.reminder.cron:0 0 * * * *}")
    public void dispatchReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        List<Booking> bookings = bookingRepository.findByTrainingSession_DateAndStatusAndIsDeletedFalse(
                targetDate,
                Booking.BookingStatus.CONFIRMED
        );

        LocalDateTime now = LocalDateTime.now();

        for (Booking booking : bookings) {
            LocalDateTime sessionStart = booking.getTrainingSession()
                    .getDate()
                    .atTime(booking.getTrainingSession().getStartTime());
            long minutesUntilSession = Duration.between(now, sessionStart).toMinutes();

            long targetMinutes = reminderLeadHours * 60;
            long window = reminderWindowMinutes / 2;

            if (minutesUntilSession >= (targetMinutes - window)
                    && minutesUntilSession <= (targetMinutes + window)) {
                Long userId = booking.getUser().getId();
                Long sessionId = booking.getTrainingSession().getId();
                boolean alreadySent = notificationLogRepository.existsByEventTypeAndUserIdAndTrainingSessionId(
                        NotificationEventType.REMINDER_24H,
                        userId,
                        sessionId
                );
                if (!alreadySent) {
                    log.info("Sending reminder for booking {} to user {}", booking.getId(), userId);
                    notificationService.sendSessionReminder(booking.getUser(), booking.getTrainingSession());
                }
            }
        }
    }
}

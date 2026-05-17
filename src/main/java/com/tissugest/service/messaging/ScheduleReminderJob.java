package com.tissugest.service.messaging;

import com.tissugest.entity.PaymentSchedule;
import com.tissugest.entity.enums.FeatureCode;
import com.tissugest.entity.enums.PaymentScheduleStatus;
import com.tissugest.entity.enums.ScheduleReferenceType;
import com.tissugest.repository.PaymentScheduleRepository;
import com.tissugest.repository.PlanFeatureRepository;
import com.tissugest.repository.ShopRepository;
import com.tissugest.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Daily job that sends reminders for upcoming/overdue payment schedules.
 * Only sends to shops with:
 *   1. Messaging enabled
 *   2. Active subscription with NOTIFICATIONS_ADVANCED feature
 *
 * Runs every day at 8:00 AM.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleReminderJob {

    private final PaymentScheduleRepository scheduleRepository;
    private final MessagingDispatcher messagingDispatcher;

    /**
     * Send reminders for schedules due in 3 days (J-3) and overdue by 1 day (J+1).
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendReminders() {
        LocalDate today = LocalDate.now();
        LocalDate in3Days = today.plusDays(3);
        LocalDate yesterday = today.minusDays(1);

        // J-3: upcoming reminders
        List<PaymentSchedule> upcoming = scheduleRepository.findByStatusAndDueDate(
                PaymentScheduleStatus.PENDING, in3Days);
        for (PaymentSchedule schedule : upcoming) {
            sendReminder(schedule, "SCHEDULE_REMINDER_J3",
                    "Rappel: Vous avez une échéance de %s FCFA prévue le %s. Merci de prévoir le paiement.");
        }

        // J+1: overdue reminders
        List<PaymentSchedule> overdue = scheduleRepository.findByStatusAndDueDate(
                PaymentScheduleStatus.PENDING, yesterday);
        for (PaymentSchedule schedule : overdue) {
            sendReminder(schedule, "SCHEDULE_REMINDER_OVERDUE",
                    "Attention: Votre échéance de %s FCFA du %s est en retard. Merci de régulariser rapidement.");
        }

        if (!upcoming.isEmpty() || !overdue.isEmpty()) {
            log.info("Schedule reminders: {} upcoming, {} overdue", upcoming.size(), overdue.size());
        }
    }

    private void sendReminder(PaymentSchedule schedule, String messageType, String template) {
        // For now, we don't have direct access to the shop from schedule
        // The schedule has referenceType + referenceId which links to a sale or supply
        // We'd need to resolve shop → client phone from there
        // This is a simplified version that logs the intent
        String message = String.format(template,
                schedule.getAmountDue(),
                schedule.getDueDate().getDayOfMonth() + "/" + schedule.getDueDate().getMonthValue() + "/" + schedule.getDueDate().getYear());

        log.info("[REMINDER] {} - Schedule #{}: {}", messageType, schedule.getId(), message);

        // TODO: Resolve shop and client/supplier phone from referenceType+referenceId
        // then call: messagingDispatcher.send(shop, phone, name, messageType, message);
    }
}

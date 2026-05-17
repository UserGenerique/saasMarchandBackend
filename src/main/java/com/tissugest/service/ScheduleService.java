package com.tissugest.service;

import com.tissugest.entity.PaymentSchedule;
import com.tissugest.entity.enums.PaymentScheduleStatus;
import com.tissugest.entity.enums.ScheduleReferenceType;
import com.tissugest.repository.PaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final PaymentScheduleRepository paymentScheduleRepository;

    /**
     * Liste les échéances par référence (vente ou approvisionnement).
     */
    public List<PaymentSchedule> listByReference(ScheduleReferenceType type, Long referenceId) {
        return paymentScheduleRepository.findByReferenceTypeAndReferenceId(type, referenceId);
    }

    /**
     * Liste toutes les échéances en attente ou en retard.
     */
    public List<PaymentSchedule> listPendingAndOverdue() {
        return paymentScheduleRepository.findPendingAndOverdue();
    }

    /**
     * Marque une échéance comme payée.
     */
    @Transactional
    public PaymentSchedule markAsPaid(Long scheduleId) {
        PaymentSchedule schedule = paymentScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Échéance non trouvée"));
        schedule.setStatus(PaymentScheduleStatus.PAID);
        return paymentScheduleRepository.save(schedule);
    }

    /**
     * Job quotidien: marque les échéances PENDING dont la date est dépassée comme OVERDUE.
     * S'exécute tous les jours à 1h du matin.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void markOverdueSchedules() {
        LocalDate today = LocalDate.now();
        int count = paymentScheduleRepository.markOverdueSchedules(today);
        if (count > 0) {
            log.info("Job échéances: {} échéance(s) marquée(s) en retard", count);
        }
    }
}

package com.tissugest.repository;

import com.tissugest.entity.PaymentSchedule;
import com.tissugest.entity.enums.PaymentScheduleStatus;
import com.tissugest.entity.enums.ScheduleReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PaymentScheduleRepository extends JpaRepository<PaymentSchedule, Long> {
    List<PaymentSchedule> findByReferenceTypeAndReferenceId(ScheduleReferenceType type, Long referenceId);
    List<PaymentSchedule> findByStatus(PaymentScheduleStatus status);
    List<PaymentSchedule> findByStatusAndDueDateBefore(PaymentScheduleStatus status, LocalDate date);

    @Modifying
    @Query("UPDATE PaymentSchedule ps SET ps.status = 'OVERDUE', ps.updatedAt = CURRENT_TIMESTAMP WHERE ps.status = 'PENDING' AND ps.dueDate < :today")
    int markOverdueSchedules(LocalDate today);

    @Query("SELECT ps FROM PaymentSchedule ps WHERE ps.status IN ('PENDING', 'OVERDUE') ORDER BY ps.dueDate ASC")
    List<PaymentSchedule> findPendingAndOverdue();

    List<PaymentSchedule> findByStatusAndDueDate(PaymentScheduleStatus status, LocalDate dueDate);
}

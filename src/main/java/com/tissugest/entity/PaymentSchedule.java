package com.tissugest.entity;

import com.tissugest.entity.enums.PaymentScheduleStatus;
import com.tissugest.entity.enums.ScheduleReferenceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_schedules")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, columnDefinition = "schedule_reference_type")
    private ScheduleReferenceType referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "amount_due", nullable = false)
    private Long amountDue;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "payment_schedule_status")
    @Builder.Default
    private PaymentScheduleStatus status = PaymentScheduleStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

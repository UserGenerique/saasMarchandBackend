package com.tissugest.service;

import com.tissugest.entity.Notification;
import com.tissugest.entity.User;
import com.tissugest.entity.enums.NotificationType;
import com.tissugest.repository.NotificationRepository;
import com.tissugest.repository.UserRepository;
import com.tissugest.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;

    public List<Notification> listForCurrentUser() {
        Long userId = securityHelper.getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> listUnread() {
        Long userId = securityHelper.getCurrentUserId();
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long countUnread() {
        Long userId = securityHelper.getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead() {
        Long userId = securityHelper.getCurrentUserId();
        return notificationRepository.markAllAsRead(userId);
    }

    /**
     * Crée une notification pour un utilisateur donné.
     */
    @Transactional
    public Notification createNotification(Long userId, NotificationType type, String title, String body) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .sentAt(LocalDateTime.now())
                .build();
        notification = notificationRepository.save(notification);
        log.debug("Notification créée: user={}, type={}, title={}", userId, type, title);
        return notification;
    }
}

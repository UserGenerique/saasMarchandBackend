package com.tissugest.controller;

import com.tissugest.entity.Notification;
import com.tissugest.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> list() {
        return ResponseEntity.ok(notificationService.listForCurrentUser());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> unread() {
        return ResponseEntity.ok(notificationService.listUnread());
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countUnread() {
        return ResponseEntity.ok(Map.of("unread", notificationService.countUnread()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead() {
        int count = notificationService.markAllAsRead();
        return ResponseEntity.ok(Map.of("marked", count));
    }
}

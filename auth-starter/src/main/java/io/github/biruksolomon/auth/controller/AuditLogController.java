package io.github.biruksolomon.auth.controller;

import io.github.biruksolomon.auth.domain.AuditLog;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditService auditService;

    @GetMapping("/my-logs")
    public ResponseEntity<Page<AuditLog>> getMyAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Page<AuditLog> logs = auditService.getUserAuditLogs(user, PageRequest.of(page, size));
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/by-action")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAction(
            @RequestParam String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> logs = auditService.getAuditLogsByAction(action, PageRequest.of(page, size));
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/failed")
    public ResponseEntity<Page<AuditLog>> getFailedActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> logs = auditService.getFailedActions(PageRequest.of(page, size));
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/date-range")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> logs = auditService.getAuditLogsByDateRange(start, end, PageRequest.of(page, size));
        return ResponseEntity.ok(logs);
    }
}
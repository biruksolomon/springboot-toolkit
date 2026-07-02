package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.AuditLog;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void logAction(User user, String action, String resourceType, String resourceId,
                          AuditLog.Status status, String details, String clientIp, String userAgent) {
        AuditLog logs = AuditLog.builder()
                .user(user)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .status(status)
                .details(details)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(logs);
        log.info("Audit logged: {} - {} - {} - {}", action, resourceType, resourceId, status);
    }

    @Async
    @Transactional
    public void logSystemAction(String action, String resourceType, String resourceId,
                                AuditLog.Status status, String details) {
        logAction(null, action, resourceType, resourceId, status, details, null, null);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getUserAuditLogs(User user, Pageable pageable) {
        return auditLogRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getFailedActions(Pageable pageable) {
        return auditLogRepository.findByStatus(AuditLog.Status.FAILURE, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(start, end, pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getUserRecentActions(User user, LocalDateTime since) {
        return auditLogRepository.findByUserAndTimestampAfter(user, since);
    }

    @Async
    @Transactional
    public void deleteOldAuditLogs(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        // This would need a custom query to delete based on timestamp
        log.info("Audit logs before {} would be deleted", cutoff);
    }
}
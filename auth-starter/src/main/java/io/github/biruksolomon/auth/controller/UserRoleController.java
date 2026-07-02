package io.github.biruksolomon.auth.controller;

import io.github.biruksolomon.auth.dto.RoleResponse;
import io.github.biruksolomon.auth.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getUserRoles(@PathVariable Long userId) {
        return ResponseEntity.ok(userRoleService.getUserRoles(userId));
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<String>> getUserPermissions(@PathVariable Long userId) {
        return ResponseEntity.ok(userRoleService.getUserPermissions(userId));
    }

    @PostMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        userRoleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        userRoleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/replace")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> replaceRoles(
            @PathVariable Long userId,
            @RequestBody List<Long> roleIds) {
        userRoleService.replaceUserRoles(userId, roleIds);
        return ResponseEntity.ok().build();
    }
}

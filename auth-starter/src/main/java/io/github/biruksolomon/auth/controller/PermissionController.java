package io.github.biruksolomon.auth.controller;

import io.github.biruksolomon.auth.dto.PermissionCreateRequest;
import io.github.biruksolomon.auth.dto.PermissionResponse;
import io.github.biruksolomon.auth.service.PermissionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionManagementService permissionService;

    @PostMapping
    public ResponseEntity<PermissionResponse> createPermission(@RequestBody PermissionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(permissionService.createPermission(request));
    }

    @GetMapping("/{permissionId}")
    public ResponseEntity<PermissionResponse> getPermission(@PathVariable Long permissionId) {
        return ResponseEntity.ok(permissionService.getPermissionById(permissionId));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<PermissionResponse> getPermissionByName(@PathVariable String name) {
        return ResponseEntity.ok(permissionService.getPermissionByName(name));
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/module/{module}")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByModule(@PathVariable String module) {
        return ResponseEntity.ok(permissionService.getPermissionsByModule(module));
    }

    @PutMapping("/{permissionId}")
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable Long permissionId,
            @RequestBody PermissionCreateRequest request) {
        return ResponseEntity.ok(permissionService.updatePermission(permissionId, request));
    }

    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long permissionId) {
        permissionService.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }
}

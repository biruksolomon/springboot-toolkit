package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.Permission;
import io.github.biruksolomon.auth.dto.PermissionCreateRequest;
import io.github.biruksolomon.auth.dto.PermissionResponse;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermissionManagementService {

    private final PermissionRepository permissionRepository;

    public PermissionResponse createPermission(PermissionCreateRequest request) {
        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new AuthException.PermissionAlreadyExists(request.getName());
        }

        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .module(request.getModule())
                .build();

        permission = permissionRepository.save(permission);
        log.info("Permission created: {}", permission.getName());
        return mapToResponse(permission);
    }

    public PermissionResponse updatePermission(Long permissionId, PermissionCreateRequest request) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AuthException.PermissionNotFound(permissionId));

        permission.setDescription(request.getDescription());
        permission.setModule(request.getModule());

        permission = permissionRepository.save(permission);
        log.info("Permission updated: {}", permission.getName());
        return mapToResponse(permission);
    }

    public PermissionResponse getPermissionById(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AuthException.PermissionNotFound(permissionId));
        return mapToResponse(permission);
    }

    public PermissionResponse getPermissionByName(String name) {
        Permission permission = permissionRepository.findByName(name)
                .orElseThrow(() -> new AuthException.PermissionNotFound(name));
        return mapToResponse(permission);
    }

    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PermissionResponse> getPermissionsByModule(String module) {
        return permissionRepository.findByModule(module).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deletePermission(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AuthException.PermissionNotFound(permissionId));
        permissionRepository.delete(permission);
        log.info("Permission deleted: {}", permission.getName());
    }

    private PermissionResponse mapToResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .module(permission.getModule())
                .createdAt(permission.getCreatedAt())
                .build();
    }
}

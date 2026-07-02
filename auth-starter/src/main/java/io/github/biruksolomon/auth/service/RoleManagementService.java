package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.Permission;
import io.github.biruksolomon.auth.domain.Role;
import io.github.biruksolomon.auth.dto.RoleCreateRequest;
import io.github.biruksolomon.auth.dto.RoleResponse;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.repository.PermissionRepository;
import io.github.biruksolomon.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleResponse createRole(RoleCreateRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new AuthException.RoleAlreadyExists(request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // Assign permissions if provided
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Permission> permissionList = permissionRepository.findAllById(request.getPermissionIds());
            Set<Permission> permissions = new HashSet<>(permissionList);
            role.setPermissions(permissions);
        }

        role = roleRepository.save(role);
        log.info("Role created: {}", role.getName());
        return mapToResponse(role);
    }

    public RoleResponse updateRole(Long roleId, RoleCreateRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException.RoleNotFound(roleId));

        role.setDescription(request.getDescription());

        // Update permissions if provided
        if (request.getPermissionIds() != null) {
            List<Permission> permissionList = permissionRepository.findAllById(request.getPermissionIds());
            Set<Permission> permissions = new HashSet<>(permissionList);
            role.setPermissions(permissions);
        }

        role = roleRepository.save(role);
        log.info("Role updated: {}", role.getName());
        return mapToResponse(role);
    }

    public RoleResponse getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException.RoleNotFound(roleId));
        return mapToResponse(role);
    }

    public RoleResponse getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new AuthException.RoleNotFound(name));
        return mapToResponse(role);
    }

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException.RoleNotFound(roleId));
        roleRepository.delete(role);
        log.info("Role deleted: {}", role.getName());
    }

    public void addPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException.RoleNotFound(roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AuthException.PermissionNotFound(permissionId));

        if (!role.getPermissions().contains(permission)) {
            role.getPermissions().add(permission);
            roleRepository.save(role);
            log.info("Permission {} added to role {}", permission.getName(), role.getName());
        }
    }

    public void removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException.RoleNotFound(roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AuthException.PermissionNotFound(permissionId));

        if (role.getPermissions().remove(permission)) {
            roleRepository.save(role);
            log.info("Permission {} removed from role {}", permission.getName(), role.getName());
        }
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream()
                        .map(p -> p.getName())
                        .collect(Collectors.toList()))
                .createdAt(role.getCreatedAt())
                .build();
    }
}

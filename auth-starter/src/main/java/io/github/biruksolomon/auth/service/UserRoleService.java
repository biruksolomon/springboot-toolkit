package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.Permission;
import io.github.biruksolomon.auth.domain.Role;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.dto.RoleResponse;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.repository.RoleRepository;
import io.github.biruksolomon.auth.repository.UserRepository;
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
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public void assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFound(userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException.RoleNotFound(roleId));

        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
            log.info("Role {} assigned to user {}", role.getName(), user.getEmail());
        }
    }

    public void removeRoleFromUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFound(userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthException.RoleNotFound(roleId));

        if (user.getRoles().remove(role)) {
            userRepository.save(user);
            log.info("Role {} removed from user {}", role.getName(), user.getEmail());
        }
    }

    public List<RoleResponse> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFound(userId));

        return user.getRoles().stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .permissions(role.getPermissions().stream()
                                .map(Permission::getName)
                                .collect(Collectors.toList()))
                        .createdAt(role.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public List<String> getUserPermissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFound(userId));

        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean hasPermission(Long userId, String permissionName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFound(userId));

        return user.getRoles().stream()
                .anyMatch(role -> role.getPermissions().stream()
                        .anyMatch(perm -> perm.getName().equals(permissionName)));
    }

    public boolean hasRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFound(userId));

        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    public void replaceUserRoles(Long userId, List<Long> roleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFound(userId));

        user.getRoles().clear();
        List<Role> roles = roleRepository.findAllById(roleIds);
        user.getRoles().addAll(roles);
        userRepository.save(user);
        log.info("User {} roles updated to: {}", user.getEmail(), roleIds);
    }
}

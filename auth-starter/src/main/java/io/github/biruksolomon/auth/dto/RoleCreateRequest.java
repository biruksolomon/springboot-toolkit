package io.github.biruksolomon.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleCreateRequest {
    private String name;
    private String description;
    private List<Long> permissionIds;
}

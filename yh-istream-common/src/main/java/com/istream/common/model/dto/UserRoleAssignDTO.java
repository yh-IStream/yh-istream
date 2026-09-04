package com.istream.common.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserRoleAssignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}
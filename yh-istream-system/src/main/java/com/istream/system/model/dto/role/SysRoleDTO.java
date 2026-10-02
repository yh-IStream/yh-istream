package com.istream.system.model.dto.role;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色信息 DTO（用于列表/详情返回）
 *
 * @author istream
 * @since 2026-09-15
 */
@Data
public class SysRoleDTO {

    private Long id;

    private String roleName;

    private String roleKey;

    private Integer roleSort;

    private Integer dataScope;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}
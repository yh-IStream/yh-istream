package com.istream.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜单新增 DTO
 *
 * <p>仅包含新增菜单时允许传入的字段</p>
 *
 * @author istream
 * @since 2026-09-08
 */
@Data
public class SysMenuCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50")
    private String menuName;

    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    @Size(max = 200, message = "路由地址长度不能超过200")
    private String path;

    @Size(max = 255, message = "组件路径长度不能超过255")
    private String component;

    @Size(max = 255, message = "路由参数长度不能超过255")
    private String query;

    @Size(max = 100, message = "权限标识长度不能超过100")
    private String permission;

    @Size(max = 100, message = "菜单图标长度不能超过100")
    private String icon;

    private Integer orderNum;

    private Integer visible;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
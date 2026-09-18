package com.istream.system.model.dto.menu;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单信息 DTO（用于列表/详情返回）
 *
 * @author istream
 * @since 2026-09-15
 */
@Data
public class SysMenuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private Long parentId;

    private String menuName;

    private String menuType;

    private String path;

    private String component;

    private String query;

    private String permission;

    private String icon;

    private Integer orderNum;

    private Integer visible;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private List<SysMenuDTO> children = new ArrayList<>();
}
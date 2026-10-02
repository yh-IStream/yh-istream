package com.istream.system.model.dto.dept;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门信息 DTO（用于列表/详情返回）
 *
 * @author istream
 * @since 2026-09-15
 */
@Data
public class SysDeptDTO {

    private Long id;

    private Long parentId;

    private String ancestors;

    private String deptName;

    private Integer orderNum;

    private String leader;

    private String phone;

    private String email;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private List<SysDeptDTO> children = new ArrayList<>();
}
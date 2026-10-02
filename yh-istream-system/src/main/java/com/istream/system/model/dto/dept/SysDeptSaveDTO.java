package com.istream.system.model.dto.dept;

import com.istream.common.validation.Groups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门新增/修改 DTO
 *
 * <p>通过 {@link Groups} 区分校验规则。</p>
 *
 * @author istream
 * @since 2026-09-17
 */
@Data
public class SysDeptSaveDTO {

    @NotNull(groups = Groups.Update.class, message = "部门ID不能为空")
    private Long id;

    private Long parentId;

    @NotBlank(groups = Groups.Create.class, message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100")
    private String deptName;

    private Integer orderNum;

    @Size(max = 50, message = "负责人长度不能超过50")
    private String leader;

    @Size(max = 20, message = "联系电话长度不能超过20")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    private Integer status;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
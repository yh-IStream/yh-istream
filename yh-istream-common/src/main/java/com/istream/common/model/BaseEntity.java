package com.istream.common.model;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 *
 * <p>所有数据库实体的公共基类，包含主键、租户ID、审计字段、逻辑删除标记和备注。</p>
 * <p>主键采用雪花算法（ASSIGN_ID），Jackson 序列化时 Long 转 String 防止前端精度丢失。</p>
 * <p>tenantId 字段为 SaaS 多租户预留，通过 TenantInterceptor 自动填充和隔离。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID（雪花算法） */
    @ExcelProperty("ID")
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /** 租户ID（SaaS 多租户隔离，默认0表示非租户模式） */
    @ExcelIgnore
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /** 创建者ID */
    @ExcelIgnore
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新者ID */
    @ExcelIgnore
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 更新时间 */
    @ExcelIgnore
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记（0=正常 1=删除） */
    @ExcelIgnore
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    @ExcelProperty("备注")
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String remark;
}
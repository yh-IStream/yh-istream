package com.istream.system.model.dto.dict;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据 DTO（用于列表/详情返回）
 *
 * @author istream
 * @since 2026-09-15
 */
@Data
public class SysDictDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictType;

    private String dictLabel;

    private String dictValue;

    private String cssClass;

    private String listClass;

    private Integer isDefault;

    private Integer orderNum;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}
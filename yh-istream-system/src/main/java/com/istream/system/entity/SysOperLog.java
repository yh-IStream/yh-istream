package com.istream.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private Integer businessType;

    private String method;

    private String requestMethod;

    private String operUrl;

    private String operIp;

    private String operLocation;

    private String operParam;

    private String jsonResult;

    private Integer status;

    private String errorMsg;

    private Long costTime;

    private Long operBy;

    private String operName;

    private LocalDateTime operTime;
}
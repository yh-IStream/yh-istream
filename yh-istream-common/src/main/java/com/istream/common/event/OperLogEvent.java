package com.istream.common.event;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OperLogEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;

    private Integer businessType;

    private String method;

    private String requestMethod;

    private String operUrl;

    private String operIp;

    private String operParam;

    private String jsonResult;

    private Integer status;

    private String errorMsg;

    private Long costTime;

    private Long operBy;

    private String operName;

    private LocalDateTime operTime;
}
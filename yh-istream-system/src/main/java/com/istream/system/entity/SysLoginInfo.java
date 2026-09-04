package com.istream.system.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_login_info")
public class SysLoginInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("IP地址")
    private String ipAddress;

    @ExcelProperty("登录地点")
    private String loginLocation;

    @ExcelProperty("浏览器")
    private String browser;

    @ExcelProperty("操作系统")
    private String os;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelProperty("消息")
    private String msg;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("登录时间")
    private LocalDateTime loginTime;
}
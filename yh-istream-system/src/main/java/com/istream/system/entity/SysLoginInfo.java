package com.istream.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_login_info")
public class SysLoginInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String ipAddress;

    private String loginLocation;

    private String browser;

    private String os;

    private Integer status;

    private String msg;

    private LocalDateTime loginTime;
}
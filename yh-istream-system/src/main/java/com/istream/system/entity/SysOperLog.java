package com.istream.system.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "id")
@TableName("sys_oper_log")
public class SysOperLog {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("操作标题")
    private String title;

    @ExcelProperty("业务类型")
    private Integer businessType;

    @ExcelProperty("方法")
    private String method;

    @ExcelProperty("请求方式")
    private String requestMethod;

    @ExcelProperty("操作URL")
    private String operUrl;

    @ExcelProperty("操作IP")
    private String operIp;

    @ExcelProperty("操作地点")
    private String operLocation;

    @ExcelIgnore
    private String operParam;

    @ExcelIgnore
    private String jsonResult;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelIgnore
    private String errorMsg;

    @ExcelProperty("耗时(ms)")
    private Long costTime;

    @ExcelIgnore
    private Long operBy;

    @ExcelProperty("操作人")
    private String operName;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("操作时间")
    private LocalDateTime operTime;
}
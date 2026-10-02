package com.istream.common.event;

import java.time.LocalDateTime;

/**
 * 操作日志事件
 *
 * <p>由 {@code @OperLog} AOP 切面在 Controller 方法执行后构建，
 * 通过 Spring 事件总线异步消费，持久化到 {@code sys_oper_log} 表。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
public record OperLogEvent(
        // 操作模块标题
        String title,

        // 业务类型（0=其他 1=新增 2=修改 3=删除 ...）
        Integer businessType,

        // 请求方法全路径
        String method,

        // HTTP 请求方法（GET/POST/PUT/DELETE）
        String requestMethod,

        // 请求 URL
        String operUrl,

        // 操作者 IP
        String operIp,

        // 操作者地理位置
        String operLocation,

        // 请求参数（JSON）
        String operParam,

        // 返回结果（JSON）
        String jsonResult,

        // 操作状态（0=正常 1=异常）
        Integer status,

        // 错误消息
        String errorMsg,

        // 耗时（毫秒）
        Long costTime,

        // 操作者ID
        Long operBy,

        // 操作者姓名
        String operName,

        // 操作时间
        LocalDateTime operTime
) {}
package com.istream.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用响应状态码枚举
 *
 * <p>按错误类型分段编码：HTTP 标准(200-415)、业务错误(1xxxx)、数据校验(2xxxx)、
 * 文件相关(3xxxx)、系统限制(4xxxx)、租户相关(5xxxx)。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 客户端错误 */
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "请求资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据冲突"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),

    /** 服务端错误 */
    ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    /** 业务错误码（1xxxx） */
    USER_NOT_EXIST(10001, "用户不存在"),
    USER_PASSWORD_ERROR(10002, "用户名或密码错误"),
    USER_DISABLED(10003, "用户已被禁用"),
    USER_LOCKED(10004, "用户已被锁定"),
    ROLE_NOT_EXIST(10101, "角色不存在"),
    MENU_NOT_EXIST(10102, "菜单不存在"),
    DEPT_NOT_EXIST(10103, "部门不存在"),
    DICT_NOT_EXIST(10104, "字典不存在"),

    /** 数据校验错误码（2xxxx） */
    PARAM_VALID_ERROR(20001, "参数校验失败"),
    DATA_DUPLICATE(20002, "数据已存在"),
    DATA_NOT_EXIST(20003, "数据不存在"),
    DATA_SCOPE_ERROR(20004, "数据权限不足"),
    HAS_CHILDREN(20005, "存在子节点，无法删除"),
    HAS_USERS(20006, "存在关联用户，无法删除"),
    HAS_ROLES(20007, "存在关联角色，无法删除"),
    SUPER_ADMIN_PROTECT(20008, "超级管理员不允许删除"),
    DEPT_CYCLE_REFERENCE(20009, "上级部门不能是自己的子部门，存在循环引用"),
    MENU_CYCLE_REFERENCE(20010, "上级菜单不能是自己的子菜单，存在循环引用"),
    USER_USERNAME_DUPLICATE(20011, "用户名已存在"),
    USER_ID_REQUIRED(20012, "用户ID不能为空"),
    USER_ROLE_INVALID(20013, "存在无效的角色ID"),
    CAPTCHA_EXPIRED(20014, "验证码已过期"),
    CAPTCHA_ERROR(20015, "验证码错误"),
    DEPT_HAS_CHILDREN(20016, "存在子部门，不允许删除"),
    DEPT_HAS_USERS(20017, "部门下存在用户，不允许删除"),

    /** 文件相关错误码（3xxxx） */
    FILE_UPLOAD_ERROR(30001, "文件上传失败"),
    FILE_DOWNLOAD_ERROR(30002, "文件下载失败"),
    FILE_SIZE_EXCEED(30003, "文件大小超出限制"),
    FILE_TYPE_NOT_SUPPORT(30004, "文件类型不支持"),

    /** 系统限制错误码（4xxxx） */
    RATE_LIMIT(40001, "请求过于频繁，请稍后再试"),
    REPEAT_SUBMIT(40002, "请勿重复提交"),

    /** 租户相关错误码（5xxxx） */
    TENANT_NOT_EXIST(50001, "租户不存在"),
    TENANT_DISABLED(50002, "租户已被禁用"),
    TENANT_EXPIRED(50003, "租户已过期"),
    TENANT_ACCESS_DENIED(50004, "无权访问该租户资源"),
    ;

    private final Integer code;
    private final String msg;
}
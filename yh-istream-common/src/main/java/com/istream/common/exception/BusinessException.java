package com.istream.common.exception;

import com.istream.common.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * <p>用于业务逻辑中可预期的错误场景，携带错误码和消息，
 * 由 {@link com.istream.framework.web.GlobalExceptionHandler} 统一拦截并转换为标准响应体。</p>
 *
 * @author istream
 * @since 2026-08-17
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.code = resultCode.getCode();
    }
}
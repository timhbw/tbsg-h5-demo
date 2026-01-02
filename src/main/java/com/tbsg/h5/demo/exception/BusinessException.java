package com.tbsg.h5.demo.exception;

import lombok.Getter;

/**
 * 业务异常基类
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "FAIL";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
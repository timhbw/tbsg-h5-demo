package com.tbsg.h5.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 *
 * @author demo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT 令牌
     */
    private String jwt;

    /**
     * 状态
     */
    private String status;

    /**
     * 错误信息（可选）
     */
    private String errMessage;

    public LoginResponse(String jwt, String status) {
        this.jwt = jwt;
        this.status = status;
    }
}

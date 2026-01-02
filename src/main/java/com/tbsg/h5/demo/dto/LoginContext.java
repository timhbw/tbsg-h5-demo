package com.tbsg.h5.demo.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录上下文
 *
 * @author demo
 */
@Data
public class LoginContext {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    /**
     * 密码（可选，demo 中不验证密码）
     */
    private String password;
}

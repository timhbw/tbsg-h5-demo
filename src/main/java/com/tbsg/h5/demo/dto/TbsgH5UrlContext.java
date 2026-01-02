package com.tbsg.h5.demo.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取淘宝闪购 H5 链接上下文
 *
 * @author demo
 */
@Data
public class TbsgH5UrlContext {

    /**
     * 环境：PROD-生产环境, PPE-预发环境
     */
    @NotBlank(message = "环境参数不能为空")
    private String env;
}

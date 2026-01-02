package com.tbsg.h5.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 淘宝闪购 H5 链接响应
 *
 * @author demo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbsgH5UrlResponse {

    /**
     * 淘宝闪购 H5 链接
     */
    private String tbsgH5Url;

    /**
     * 状态
     */
    private String status;

    /**
     * JWT 令牌（可能已刷新）
     */
    private String jwt;

    /**
     * 错误信息（可选）
     */
    private String errMessage;

    public TbsgH5UrlResponse(String tbsgH5Url, String status, String jwt) {
        this.tbsgH5Url = tbsgH5Url;
        this.status = status;
        this.jwt = jwt;
    }
}

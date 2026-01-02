package com.tbsg.h5.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * 收银台控制器
 * 
 * 这是一个模拟的第三方收银台，用于演示支付流程：
 * 1. 接收机构透传的订单信息
 * 2. 展示收银台页面
 * 3. 用户点击支付后，模拟支付成功
 * 4. 回调机构的 notifyUrl
 * 5. 重定向到淘宝闪购订单详情页
 *
     * @author demo
 */
@Slf4j
@Controller
@RequestMapping("/cashier")
public class CashierPayController {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${tbsg.pay.cashierUrl}")
    private String cashierUrl;

    /**
     * 收银台页面
     * 
     * 接收机构透传的订单信息，重定向到静态 HTML 页面
     *
     * @param transactionId 交易ID
     * @param payAmount     支付金额（分）
     * @param subject       订单标题
     * @param notifyUrl     机构回调地址
     * @param redirectUrl   支付成功后的重定向地址（淘宝闪购订单详情页）
     * @param response      HttpServletResponse
     * @throws IOException  IO异常
     */
    @GetMapping
    public void cashierPay(
            @RequestParam("transactionId") String transactionId,
            @RequestParam("payAmount") String payAmount,
            @RequestParam("subject") String subject,
            @RequestParam("notifyUrl") String notifyUrl,
            @RequestParam("redirectUrl") String redirectUrl,
            @RequestParam(value = "uid", required = false) String uid,
            @RequestParam(value = "body", required = false) String body,
            HttpServletResponse response) throws IOException {

        log.info("【收银台】收到支付请求，transactionId: {}, payAmount: {}, subject: {}",
                transactionId, payAmount, subject);

        // 1. 验证输入参数
        if (!StringUtils.hasText(transactionId) ||
            !StringUtils.hasText(payAmount) ||
            !StringUtils.hasText(subject) ||
            !StringUtils.hasText(notifyUrl) ||
            !StringUtils.hasText(redirectUrl)) {

            log.error("【收银台】参数不完整");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "参数不完整");
            return;
        }

        // 2. 构建重定向 URL（指向静态 HTML 页面）
        // 使用配置文件中的 cashierUrl 作为基础 URL
        StringBuilder sb = new StringBuilder();
        sb.append(cashierUrl).append("?");

        sb.append("transactionId=").append(URLEncoder.encode(transactionId, "UTF-8"));
        sb.append("&payAmount=").append(URLEncoder.encode(payAmount, "UTF-8"));
        sb.append("&subject=").append(URLEncoder.encode(subject, "UTF-8"));
        sb.append("&notifyUrl=").append(URLEncoder.encode(notifyUrl, "UTF-8"));
        sb.append("&redirectUrl=").append(URLEncoder.encode(redirectUrl, "UTF-8"));

        if (StringUtils.hasText(uid)) {
            sb.append("&uid=").append(URLEncoder.encode(uid, "UTF-8"));
        }
        if (StringUtils.hasText(body)) {
            sb.append("&body=").append(URLEncoder.encode(body, "UTF-8"));
        }
        // 传递 contextPath 给前端 JS 使用
        if (StringUtils.hasText(contextPath)) {
            sb.append("&contextPath=").append(URLEncoder.encode(contextPath, "UTF-8"));
        }

        String redirectLocation = sb.toString();
        log.info("【收银台】重定向到静态页面: {}", redirectLocation);

        // 3. 执行重定向
        response.sendRedirect(redirectLocation);
    }
}
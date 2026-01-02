package com.tbsg.h5.demo.controller;

import com.tbsg.h5.demo.dto.LoginContext;
import com.tbsg.h5.demo.dto.LoginResponse;
import com.tbsg.h5.demo.dto.TbsgH5UrlContext;
import com.tbsg.h5.demo.dto.TbsgH5UrlResponse;
import com.tbsg.h5.demo.service.LoginService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 淘宝闪购登录控制器
 *
 * @author demo
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tbsg/channel")
@CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
public class TbsgLoginController {

    @Autowired
    private LoginService loginService;

    /**
     * 用户登录接口
     *
     * @param context 登录上下文
     * @return 登录响应
     */
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginContext context) {
        log.info("【登录接口】收到登录请求，手机号: {}", context.getMobile());
        
        LoginResponse response = loginService.login(context);
        
        if ("SUCCESS".equals(response.getStatus())) {
            log.info("【登录接口】登录成功，手机号: {}", context.getMobile());
            return ResponseEntity.ok(response);
        } else {
            log.warn("【登录接口】登录失败，手机号: {}, 原因: {}", context.getMobile(), response.getErrMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取淘宝闪购 H5 链接接口
     *
     * @param context 上下文
     * @param request HTTP 请求
     * @return H5 链接响应
     */
    @PostMapping(value = "/getTbsgH5Url", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TbsgH5UrlResponse> getTbsgH5Url(@Valid @RequestBody TbsgH5UrlContext context,
                                                            HttpServletRequest request) {
        log.info("【获取H5链接】收到请求，环境: {}", context.getEnv());

        // 从请求头中获取 Authorization
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("【获取H5链接】Authorization 头缺失或格式不正确");
            TbsgH5UrlResponse response = new TbsgH5UrlResponse("", "FAIL", "", "Authorization 头缺失或格式不正确");
            return ResponseEntity.badRequest().body(response);
        }

        TbsgH5UrlResponse response = loginService.generateTbsgH5Url(context, authorization);

        if ("SUCCESS".equals(response.getStatus())) {
            log.info("【获取H5链接】生成成功，环境: {}", context.getEnv());
            return ResponseEntity.ok(response);
        } else {
            log.warn("【获取H5链接】生成失败，环境: {}, 原因: {}", context.getEnv(), response.getErrMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

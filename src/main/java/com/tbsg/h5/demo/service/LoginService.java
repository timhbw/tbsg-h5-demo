package com.tbsg.h5.demo.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tbsg.h5.demo.dto.LoginContext;
import com.tbsg.h5.demo.dto.LoginResponse;
import com.tbsg.h5.demo.dto.TbsgH5UrlContext;
import com.tbsg.h5.demo.dto.TbsgH5UrlResponse;
import com.tbsg.h5.demo.constants.PaymentConstants;
import eleme.openapi.h5.sdk.login.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录服务
 *
 * @author demo
 */
@Slf4j
@Service
public class LoginService {

    @Value("${tbsg.login.openSiteSourceCode}")
    private String openSiteSourceCode;

    @Value("${tbsg.login.from}")
    private String from;

    @Value("${tbsg.login.welfare3pp}")
    private String welfare3pp;

    @Value("${tbsg.login.consumerSecret}")
    private String consumerSecret;

    @Value("#{'${tbsg.login.allowedMobiles}'.split(',')}")
    private List<String> allowedMobiles;

    /**
     * 用户登录
     *
     * @param context 登录上下文
     * @return 登录响应
     */
    public LoginResponse login(LoginContext context) {
        String mobile = context.getMobile();

        log.info("【登录】用户提交登录请求。Mobile: {}", mobile);

        // 验证手机号是否在白名单中
        if (!allowedMobiles.contains(mobile)) {
            log.warn("【登录】尝试使用非授权账户登录。Mobile: {}", mobile);
            return new LoginResponse("", PaymentConstants.FAIL, "该账户未授权，请联系管理员");
        }

        // 生成 JWT 令牌
        Map<String, Object> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("open_id", "20882322569256751123888");
        params.put("source", openSiteSourceCode);

        log.info("【登录】用户登录校验通过，生成 JWT 令牌。Params: {}", params);
        String token = JWTUtil.buildToken(params, consumerSecret);

        return new LoginResponse(PaymentConstants.BEARER_PREFIX + token, PaymentConstants.SUCCESS);
    }

    /**
     * 生成淘宝闪购 H5 链接
     *
     * @param context 上下文
     * @param jwt     JWT 令牌
     * @return H5 链接响应
     */
    public TbsgH5UrlResponse generateTbsgH5Url(TbsgH5UrlContext context, String jwt) {
        // 去除 "Bearer " 前缀
        String token = jwt.startsWith(PaymentConstants.BEARER_PREFIX) ? jwt.substring(7) : jwt;

        // 验证并解析 JWT 令牌
        DecodedJWT decodedJWT = verifyAndDecodeToken(token);
        if (decodedJWT == null) {
            log.warn("【登录】JWT 令牌无效或已过期。Token: {}", token);
            return new TbsgH5UrlResponse("", PaymentConstants.FAIL, "", "JWT令牌已失效，请重新登录");
        }

        // 检查令牌是否即将过期（1小时内）
        String finalToken = token;
        long currentTime = System.currentTimeMillis();
        long expirationTime = decodedJWT.getExpiresAt().getTime();
        long timeUntilExpiration = expirationTime - currentTime;
        long threshold = 3600000; // 1 小时

        if (timeUntilExpiration <= threshold) {
            // 令牌即将过期，重新生成令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("mobile", decodedJWT.getClaim("mobile").asString());
            claims.put("open_id", decodedJWT.getClaim("open_id").asString());
            claims.put("source", decodedJWT.getClaim("source").asString());

            log.info("【登录】令牌即将过期，重新生成 JWT 令牌。Claims: {}", claims);
            finalToken = JWTUtil.buildToken(claims, consumerSecret);
        }

        // 根据环境生成 URL
        String url;
        String env = context.getEnv();
        if (PaymentConstants.ENV_PROD.equals(env)) {
            url = buildTbsgH5Url(finalToken, false);
        } else if (PaymentConstants.ENV_PPE.equals(env)) {
            url = buildTbsgH5Url(finalToken, true);
        } else {
            log.warn("【登录】未知的环境参数: {}", env);
            return new TbsgH5UrlResponse("", PaymentConstants.FAIL, "", "未知的环境参数");
        }

        log.info("【登录】生成淘宝闪购 H5 URL 成功。Env: {}, URL: {}", env, url);
        return new TbsgH5UrlResponse(url, PaymentConstants.SUCCESS, PaymentConstants.BEARER_PREFIX + finalToken);
    }

    /**
     * 验证并解码 JWT 令牌
     *
     * @param token JWT 令牌
     * @return 解码后的 JWT，验证失败返回 null
     */
    private DecodedJWT verifyAndDecodeToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(consumerSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withAudience("ele.me")
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            log.error("【登录】JWT 验证失败: {}", exception.getMessage());
            return null;
        }
    }

    /**
     * 构建淘宝闪购 H5 URL
     *
     * @param token JWT 令牌
     * @param isPPE 是否为预发环境
     * @return H5 URL
     */
    private String buildTbsgH5Url(String token, boolean isPPE) {
        String baseUrl = isPPE ? "https://ppe-h5.ele.me/minisite/?" : "https://h5.ele.me/minisite/?";

        StringBuilder urlBuilder = new StringBuilder(baseUrl)
                .append("from=").append(from)
                .append("&opensite_source=").append(openSiteSourceCode)
                .append("&welfare_3pp=").append(welfare3pp)
                .append("&latitude=").append("22.123025")  // 默认经纬度（石排郊野湾公园）
                .append("&longitude=").append("113.562928")
                .append("&jwt=").append(token);

        if (isPPE) {
            urlBuilder.append("&env=ppe");
        }

        return urlBuilder.toString();
    }
}
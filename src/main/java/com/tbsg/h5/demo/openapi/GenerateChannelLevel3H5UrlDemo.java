package com.tbsg.h5.demo.openapi;

import com.alibaba.fastjson.JSON;
import eleme.openapi.h5.sdk.login.utils.JWTUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成三级渠道 H5 首页链接 Demo
 *
 * <p>三级渠道淘宝闪购 H5 首页需要在二级渠道的基础上再拼接 channelInfo 字段。</p>
 *
 * <p>链接拼接规则：</p>
 * <pre>
 * https://h5.ele.me/minisite/?from=二级渠道&opensite_source=登录source&welfare_3pp=支付code&jwt=登录token&channelInfo=UrlEncode(一、二、三级渠道信息)
 * </pre>
 *
 * <p>channelInfo 格式示例：</p>
 * <pre>
 * {"channel":"mobile","subChannel":"mobile.xxxx","subSubChannel":"mobile.xxxx.xxxx"}
 * </pre>
 *
 * <p>注意：一定要先创建三级渠道，再拼接三级渠道信息</p>
 *
 * @see <a href="https://open.shop.ele.me/base/documents/openh5">H5渠道标准版集成场景(仅定向合作)</a>
 */
public class GenerateChannelLevel3H5UrlDemo {

    public static void main(String[] args) {
        // ==================== 1. 配置参数 ====================
        // 登录相关配置（从保障宝中获取）
        // 登录 source
        String openSiteSourceCode = "sdkdemo";
        // 支付 code
        String welfare3pp = "SDKDEMO_OPENPAY";
        // 用于生成 JWT 的密钥
        String consumerSecret = "xxxxx";

        // 渠道配置
        // 一级渠道（固定为 mobile）
        String channelLevel1 = "mobile";
        // 二级渠道（联系平台获取）
        String channelLevel2 = "mobile.sdkdemo";
        // 三级渠道（需先通过接口创建）
        String channelLevel3 = "mobile.sdkdemo.three_channel_test_dinner";

        // 用户信息（用于生成 JWT）
        // 用户手机号
        String mobile = "11405196411";
        // 用户唯一标识
        String openId = "user_open_id_11405196411";

        // 定位信息（可选）
        // 纬度（高德坐标系）
//        String latitude = "31.23084";
        // 经度（高德坐标系）
//        String longitude = "121.412851";

        // ==================== 2. 生成 JWT 令牌 ====================
        String jwt = generateJwtToken(mobile, openId, openSiteSourceCode, consumerSecret);
        System.out.println("生成的 JWT 令牌: " + jwt);

        // ==================== 3. 构建 channelInfo ====================
        String channelInfo = buildChannelInfo(channelLevel1, channelLevel2, channelLevel3);
        System.out.println("channelInfo (原始): " + channelInfo);

        String encodedChannelInfo = urlEncode(channelInfo);
        System.out.println("channelInfo (URL编码后): " + encodedChannelInfo);

        // ==================== 4. 拼接完整 URL ====================
        String h5Url = buildChannelLevel3H5Url(
                channelLevel2,
                openSiteSourceCode,
                welfare3pp,
                jwt,
                encodedChannelInfo,
                null,
                null
        );

        System.out.println("--------------------------------------------------");
        System.out.println("三级渠道 H5 首页链接:");
        System.out.println(h5Url);
        System.out.println("--------------------------------------------------");
    }

    /**
     * 生成 JWT 令牌
     *
     * @param mobile             用户手机号
     * @param openId             用户唯一标识
     * @param openSiteSourceCode 登录 source
     * @param consumerSecret     密钥
     * @return JWT 令牌
     */
    private static String generateJwtToken(String mobile, String openId, String openSiteSourceCode, String consumerSecret) {
        Map<String, Object> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("open_id", openId);
        params.put("source", openSiteSourceCode);

        return JWTUtil.buildToken(params, consumerSecret);
    }

    /**
     * 构建 channelInfo JSON 字符串
     *
     * <p>格式：{"channel":"一级渠道","subChannel":"二级渠道","subSubChannel":"三级渠道"}</p>
     *
     * @param channelLevel1 一级渠道（固定为 mobile）
     * @param channelLevel2 二级渠道
     * @param channelLevel3 三级渠道
     * @return channelInfo JSON 字符串
     */
    private static String buildChannelInfo(String channelLevel1, String channelLevel2, String channelLevel3) {
        Map<String, String> channelInfoMap = new HashMap<>();
        channelInfoMap.put("channel", channelLevel1);
        channelInfoMap.put("subChannel", channelLevel2);
        channelInfoMap.put("subSubChannel", channelLevel3);

        return JSON.toJSONString(channelInfoMap);
    }

    /**
     * URL 编码
     *
     * @param value 原始字符串
     * @return URL 编码后的字符串
     */
    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建三级渠道 H5 首页 URL
     *
     * <p>链接拼接规则：</p>
     * <pre>
     * https://h5.ele.me/minisite/?from=二级渠道&opensite_source=登录source&welfare_3pp=支付code&jwt=登录token&channelInfo=UrlEncode(渠道信息)
     * </pre>
     *
     * @param channelLevel2      二级渠道
     * @param openSiteSourceCode 登录 source
     * @param welfare3pp         支付 code
     * @param jwt                JWT 令牌
     * @param encodedChannelInfo URL 编码后的 channelInfo
     * @param latitude           纬度（可选）
     * @param longitude          经度（可选）
     * @return 完整的 H5 URL
     */
    private static String buildChannelLevel3H5Url(String channelLevel2,
                                                  String openSiteSourceCode,
                                                  String welfare3pp,
                                                  String jwt,
                                                  String encodedChannelInfo,
                                                  String latitude,
                                                  String longitude
    ) {
        String baseUrl = "https://h5.ele.me/minisite/?";

        StringBuilder urlBuilder = new StringBuilder(baseUrl)
                .append("from=").append(channelLevel2)
                .append("&opensite_source=").append(openSiteSourceCode)
                .append("&welfare_3pp=").append(welfare3pp)
                .append("&jwt=").append(jwt)
                .append("&channelInfo=").append(encodedChannelInfo);

        // 添加定位信息（可选）
        if (latitude != null && longitude != null) {
            urlBuilder.append("&latitude=").append(latitude)
                    .append("&longitude=").append(longitude);
        }

        return urlBuilder.toString();
    }
}

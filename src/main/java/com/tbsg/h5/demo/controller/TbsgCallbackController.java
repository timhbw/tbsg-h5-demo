package com.tbsg.h5.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbsg.h5.demo.entity.PaymentOrder;
import com.tbsg.h5.demo.service.PaymentService;
import com.tbsg.h5.demo.constants.PaymentConstants;
import eleme.openapi.h5.sdk.pay.model.request.PayCallbackRequest;
import eleme.openapi.h5.sdk.pay.model.request.RefundCallbackRequest;
import eleme.openapi.h5.sdk.pay.service.base.BaseCallbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static eleme.openapi.sdk.utils.WebUtils.buildQuery;

/**
 * 淘宝闪购回调控制器
 * 继承 BaseCallbackService，SDK 会自动处理签名验证
 *
 * 回调流程：
 * 1. 用户在收银台完成支付
 * 2. 收银台回调机构服务端（本接口）
 * 3. 机构更新订单状态
 * 4. 机构调用 SDK 的 payCallback() 通知淘宝闪购
 * 5. 重定向到中间页，最终跳转到淘宝闪购订单详情页
 *
 * @author demo
 */
@Slf4j
@Service
@RestController
@RequestMapping("/tbsg/channel/notify")
public class TbsgCallbackController extends BaseCallbackService {

    @Autowired
    private PaymentService paymentService;

    @Value("${tbsg.pay.code}")
    private String payCode;

    @Value("${tbsg.pay.intermediateUrl}")
    private String intermediateUrl;

    @Value("${tbsg.pay.merchantPrivateKey}")
    private String merchantPrivateKey;

    /**
     * 获取机构（商户）私钥
     * SDK 使用此私钥为回调请求添加签名
     *
     * @return 机构私钥
     */
    @Override
    public String getPrivateKey() {
        return merchantPrivateKey;
    }

    /**
     * 接收收银台支付成功回调，并回调通知淘宝闪购
     *
     * 流程：
     * 1. 收银台回调机构，通知支付结果
     * 2. 机构更新订单状态
     * 3. 机构调用 SDK 的 payCallback() 通知淘宝闪购
     * 4. 返回重定向 URL，跳转到中间页
     *
     * @param responseBody 收银台回调的请求体
     * @param httpServletRequest HTTP 请求
     * @param httpServletResponse HTTP 响应
     * @return 包含重定向 URL 的响应
     */
    @CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
    @PostMapping("/paycallback")
    public ResponseEntity<Map<String, String>> cashierPayCallback(
            @RequestBody String responseBody,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {

        log.info("【支付回调】收到收银台回调，requestBody: {}", responseBody);

        try {
            // 1. 解析收银台回调的响应体
            Map<String, Object> responseMap = parseResponseBody(responseBody);
            String status = (String) responseMap.get("status");

            log.info("【支付回调】收银台回调状态: {}", status);

            // 卫语句：如果支付未成功，直接返回
            if (!PaymentConstants.SUCCESS.equals(status)) {
                log.warn("【支付回调】支付失败，status: {}", status);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "支付失败"));
            }

            // 2. 提取回调参数
            String notifyUrl = (String) responseMap.get("notifyUrl");
            String transactionId = (String) responseMap.get("transactionId");
            String payAmountStr = (String) responseMap.get("payAmount");
            Integer payAmount = Integer.parseInt(payAmountStr);
            String redirectUrl = (String) responseMap.get("redirectUrl");

            log.info("【支付回调】支付成功，transactionId: {}, payAmount: {}, notifyUrl: {}",
                    transactionId, payAmount, notifyUrl);

            // 3. 更新订单状态为支付成功，并获取最新订单信息（包含正确的 outTradeNo）
            PaymentOrder order = paymentService.updateOrderStatus(transactionId, PaymentConstants.SUCCESS);

            // 4. 创建支付回调请求对象
            PayCallbackRequest request = new PayCallbackRequest();
            request.setPayCode(payCode);
            request.setTransactionId(transactionId);
            request.setOutTradeNo(order.getOutTradeNo()); // 使用订单中原有的机构订单号
            request.setPayAmount(payAmount);
            request.setPayStatus(PaymentConstants.SUCCESS);

            log.info("【支付回调】准备回调通知淘宝闪购，request: {}", request);

            // 5. 调用 SDK 的 payCallback() 通知淘宝闪购
            boolean callbackResult = payCallback(request, notifyUrl);

            if (!callbackResult) {
                log.error("【支付回调】回调淘宝闪购失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("message", "回调淘宝闪购失败"));
            }

            // 6. 回调成功，准备重定向数据
            Map<String, String> payData = new HashMap<>();
            payData.put("notifyUrl", notifyUrl);
            payData.put("transactionId", transactionId);
            payData.put("payAmount", payAmountStr);
            payData.put("redirectUrl", redirectUrl);

            // 7. 构建重定向 URL（跳转到中间页）
            String newRedirectUrl = intermediateUrl + "?" + buildQuery(payData, "utf-8");

            log.info("【支付回调】回调淘宝闪购成功，重定向到中间页: {}", newRedirectUrl);

            // 8. 返回重定向 URL
            Map<String, String> response = new HashMap<>();
            response.put("redirectUrl", newRedirectUrl);
            return ResponseEntity.ok(response);

        } catch (NullPointerException | IllegalArgumentException e) {
            log.error("【支付回调】参数错误", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "参数错误: " + e.getMessage()));
        } catch (Exception e) {
            log.error("【支付回调】处理回调失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "处理回调失败: " + e.getMessage()));
        }
    }

    /**
     * 退款回调（由 PaymentService 调用）
     *
     * 当机构处理完退款后，调用此方法通知淘宝闪购退款结果
     *
     * @param refundCallbackRequest 退款回调请求
     * @param notifyUrl 淘宝闪购的回调地址
     * @return 是否回调成功
     */
    public boolean notifyRefundCallback(RefundCallbackRequest refundCallbackRequest, String notifyUrl) {
        try {
            log.info("【退款回调】准备回调通知淘宝闪购，request: {}, notifyUrl: {}",
                    refundCallbackRequest, notifyUrl);

            // 调用 SDK 的 refundCallback() 通知淘宝闪购
            boolean result = refundCallback(refundCallbackRequest, notifyUrl);

            if (result) {
                log.info("【退款回调】回调淘宝闪购成功");
            } else {
                log.error("【退款回调】回调淘宝闪购失败");
            }

            return result;
        } catch (Exception e) {
            log.error("【退款回调】回调淘宝闪购异常", e);
            return false;
        }
    }

    /**
     * 解析收银台回调的 JSON 响应体
     *
     * @param responseBody JSON 字符串
     * @return 解析后的 Map
     * @throws IOException 解析失败
     */
    private Map<String, Object> parseResponseBody(String responseBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseBody, Map.class);
    }
}
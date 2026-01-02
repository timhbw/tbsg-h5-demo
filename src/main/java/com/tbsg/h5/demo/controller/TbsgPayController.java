package com.tbsg.h5.demo.controller;

import com.tbsg.h5.demo.service.BillService;
import com.tbsg.h5.demo.service.OssService;
import com.tbsg.h5.demo.service.PaymentService;
import com.tbsg.h5.demo.constants.PaymentConstants;
import eleme.openapi.h5.sdk.pay.model.request.*;
import eleme.openapi.h5.sdk.pay.model.response.*;
import eleme.openapi.h5.sdk.pay.service.base.BasePayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 淘宝闪购支付控制器
 * 继承 BasePayService，SDK 会自动处理签名验证和响应签名
 *
 * @author demo
 */
@Slf4j
@Service
@RestController
@RequestMapping("/tbsg/channel")
public class TbsgPayController extends BasePayService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BillService billService;

    @Autowired
    private OssService ossService;

    @Value("${tbsg.pay.platformPublicKey}")
    private String platformPublicKey;

    @Value("${tbsg.pay.merchantPrivateKey}")
    private String merchantPrivateKey;

    @Value("${tbsg.pay.code}")
    private String payCode;

    /**
     * 获取淘宝闪购平台公钥
     * SDK 使用此公钥验证淘宝闪购发来的请求签名
     *
     * @return 淘宝闪购平台公钥
     */
    @Override
    public String getElemePublicKey() {
        return platformPublicKey;
    }

    /**
     * 获取机构（商户）私钥
     * SDK 使用此私钥为响应添加签名
     *
     * @return 机构私钥
     */
    @Override
    public String getPrivateKey() {
        return merchantPrivateKey;
    }
    /**
     * 支付接口
     * SDK 自动验证请求签名，并将参数解析为 PayRequest 对象后调用此方法
     *
     * @param payRequest         支付请求（SDK 已验证签名并解析）
     * @param httpServletRequest HTTP 请求
     * @param httpServletResponse HTTP 响应
     * @return null（重定向到收银台）
     */
    @Override
    public String pay(PayRequest payRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        log.info("【支付接口-pay】收到支付请求，transactionId: {}, payAmount: {}",
                payRequest.getTransactionId(), payRequest.getPayAmount());

        try {
            // 调用 PaymentService 处理业务逻辑
            String cashierUrl = paymentService.processPay(payRequest);

            // 重定向到收银台
            log.info("【支付接口-pay】重定向到收银台，URL: {}", cashierUrl);
            httpServletResponse.sendRedirect(cashierUrl);

            return null; // 重定向后返回 null
        } catch (IOException e) {
            log.error("【支付接口-pay】重定向失败", e);
            throw new RuntimeException("重定向到收银台失败", e);
        }
    }

    /**
     * 查询支付状态接口
     * SDK 自动验证请求签名，自动为响应添加签名
     *
     * @param request 查询请求（SDK 已验证签名）
     * @return 查询响应（SDK 会自动添加 sign 和 nonceStr）
     */
    @Override
    public QueryPayResponse queryPay(QueryPayRequest request) {
        log.info("【查询支付-queryPay】收到查询请求，transactionId: {}", request.getTransactionId());

        try {
            QueryPayResponse response = paymentService.queryPay(request);
            // PaymentService 已有详细日志，此处仅记录简要结果
            log.info("【查询支付-queryPay】查询完成，transactionId: {}, returnCode: {}",
                    request.getTransactionId(), response.getReturnCode());
            return response;
        } catch (Exception e) {
            log.error("【查询支付-queryPay】查询异常，transactionId: {}", request.getTransactionId(), e);
            QueryPayResponse response = new QueryPayResponse();
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 退款接口
     * SDK 自动验证请求签名，自动为响应添加签名
     *
     * @param request 退款请求（SDK 已验证签名）
     * @return 退款响应（SDK 会自动添加 sign 和 nonceStr）
     */
    @Override
    public RefundResponse refund(RefundRequest request) {
        log.info("【退款接口-refund】收到退款请求，refundNo: {}, transactionId: {}, refundAmount: {}",
                request.getRefundNo(), request.getTransactionId(), request.getRefundAmount());

        try {
            RefundResponse response = paymentService.processRefund(request);
            log.info("【退款接口-refund】退款请求处理完成，refundNo: {}, returnCode: {}",
                    request.getRefundNo(), response.getReturnCode());
            return response;
        } catch (Exception e) {
            log.error("【退款接口-refund】退款处理异常，refundNo: {}", request.getRefundNo(), e);
            RefundResponse response = new RefundResponse();
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("退款失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 查询退款状态接口
     * SDK 自动验证请求签名，自动为响应添加签名
     *
     * @param request 查询请求（SDK 已验证签名）
     * @return 查询响应（SDK 会自动添加 sign 和 nonceStr）
     */
    @Override
    public QueryRefundResponse queryRefund(QueryRefundRequest request) {
        log.info("【查询退款-refund】收到查询请求，refundNo: {}", request.getRefundNo());

        try {
            QueryRefundResponse response = paymentService.queryRefund(request);
            log.info("【查询退款-refund】查询完成，refundNo: {}, returnCode: {}",
                    request.getRefundNo(), response.getReturnCode());
            return response;
        } catch (Exception e) {
            log.error("【查询退款-refund】查询异常，refundNo: {}", request.getRefundNo(), e);
            QueryRefundResponse response = new QueryRefundResponse();
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("查询失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 关闭订单接口
     * SDK 自动验证请求签名，自动为响应添加签名
     *
     * @param request 关闭请求（SDK 已验证签名）
     * @return 关闭响应（SDK 会自动添加 sign 和 nonceStr）
     */
    @Override
    public CloseResponse close(CloseRequest request) {
        log.info("【关闭订单-close】收到关闭请求，transactionId: {}", request.getTransactionId());

        try {
            CloseResponse response = paymentService.closeOrder(request);
            log.info("【关闭订单-close】处理完成，transactionId: {}, returnCode: {}",
                    request.getTransactionId(), response.getReturnCode());
            return response;
        } catch (Exception e) {
            log.error("【关闭订单-close】处理异常，transactionId: {}", request.getTransactionId(), e);
            CloseResponse response = new CloseResponse();
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("关闭失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 下载账单接口
     * SDK 自动验证请求签名，自动为响应添加签名
     *
     * 生成符合淘宝闪购规范的账单 CSV 文件（18个字段）
     *
     * @param request 下载请求（SDK 已验证签名）
     * @return 下载响应（SDK 会自动添加 sign 和 nonceStr）
     */
    @Override
    public DownloadBillResponse downloadBill(DownloadBillRequest request) {
        String billDate = request.getBillDate();
        log.info("【下载账单-downloadBill】收到下载账单请求，request: {}", request);

        DownloadBillResponse response = new DownloadBillResponse();

        try {
            // 1. 验证账单日期格式
            if (!billService.isValidBillDate(billDate)) {
                log.warn("【下载账单-downloadBill】日期格式错误，billDate: {}", billDate);
                response.setReturnCode(PaymentConstants.FAIL);
                response.setReturnMsg("账单日期格式错误，正确格式：yyyy-MM-dd");
                return response;
            }

            // 2. 验证日期范围（只能下载 T-1 及之前的账单）
            java.time.LocalDate requestDate = java.time.LocalDate.parse(billDate);
            java.time.LocalDate today = java.time.LocalDate.now();
            if (!requestDate.isBefore(today)) {
                log.warn("【下载账单-downloadBill】日期无效（只能下载历史账单），billDate: {}", billDate);
                response.setReturnCode(PaymentConstants.FAIL);
                response.setReturnMsg("只能下载 T-1 及之前的账单");
                return response;
            }

            // 3. 获取 OSS 预签名 URL
            // 文件名格式：{payCode}_bill_{date}.csv，OSS 路径：bills/{payCode}_bill_{date}.csv
            String fileName = payCode + "_bill_" + billDate + ".csv";
            String objectName = "bills/" + fileName;

            // 检查 OSS 文件是否存在，不存在则实时生成
            if (!ossService.doesObjectExist(objectName)) {
                log.warn("【下载账单-downloadBill】OSS 文件不存在，尝试实时生成，objectName: {}", objectName);
                billService.generateAndUploadBill(billDate);
            }

            String billUrl = ossService.getPresignedUrl(objectName);

            // 4. 构建响应
            response.setBillUrl(billUrl);
            response.setReturnCode(PaymentConstants.SUCCESS);
            response.setReturnMsg(PaymentConstants.SUCCESS);

            log.info("【下载账单-downloadBill】成功，billDate: {}, billUrl: {}", billDate, billUrl);

            return response;
        } catch (Exception e) {
            log.error("【下载账单-downloadBill】处理异常，billDate: {}", billDate, e);
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("下载账单失败: " + e.getMessage());
            return response;
        }
    }
}
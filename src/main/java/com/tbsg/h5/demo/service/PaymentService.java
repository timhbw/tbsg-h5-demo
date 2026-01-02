package com.tbsg.h5.demo.service;

import com.tbsg.h5.demo.entity.BillRecord;
import com.tbsg.h5.demo.entity.PaymentOrder;
import com.tbsg.h5.demo.entity.RefundOrder;
import com.tbsg.h5.demo.enums.TransType;
import com.tbsg.h5.demo.constants.PaymentConstants;
import eleme.openapi.h5.sdk.pay.enums.PayStatus;
import eleme.openapi.h5.sdk.pay.enums.RefundStatus;
import com.tbsg.h5.demo.mapper.BillRecordMapper;
import com.tbsg.h5.demo.mapper.PaymentOrderMapper;
import com.tbsg.h5.demo.mapper.RefundOrderMapper;
import eleme.openapi.h5.sdk.pay.model.request.*;
import eleme.openapi.h5.sdk.pay.model.response.*;
import eleme.openapi.sdk.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 支付业务逻辑服务
 *
 * @author demo
 */
@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private RefundOrderMapper refundOrderMapper;

    @Autowired
    private BillRecordMapper billRecordMapper;

    @Value("${tbsg.pay.code}")
    private String payCode;

    @Value("${tbsg.pay.cashierUrl}")
    private String cashierUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 处理支付请求
     *
     * 业务逻辑：
     * 1. 幂等性检查：根据 transactionId 查询订单
     *    - 如果订单已存在且金额一致，直接返回已有收银台 URL（支持重复扫码）
     *    - 如果订单已存在但金额不一致，抛出异常（防止金额篡改）
     *    - 如果订单已支付成功或已关闭，抛出异常
     * 2. 创建新订单：生成机构侧流水号，初始化状态为 NOTPAY
     * 3. 构建收银台 URL：包含签名参数，用于前端跳转
     *
     * @param payRequest 支付请求（SDK 已验证签名）
     * @return 收银台 URL
     */
    @Transactional(rollbackFor = Exception.class)
    public String processPay(PayRequest payRequest) {
        String transactionId = payRequest.getTransactionId();

        log.info("【支付】开始处理支付请求，transactionId: {}, amount: {}", transactionId, payRequest.getPayAmount());

        // 1. 幂等性检查
        PaymentOrder existingOrder = checkExistingOrder(transactionId, payRequest);
        if (existingOrder != null) {
            return buildCashierUrl(existingOrder);
        }

        // 2. 创建新订单
        PaymentOrder order = createPaymentOrder(payRequest);

        // 3. 构建收银台 URL
        return buildCashierUrl(order);
    }

    /**
     * 检查订单是否已存在（幂等性检查）
     *
     * @param transactionId 交易ID
     * @param payRequest 支付请求
     * @return 已存在的订单，如果不存在返回 null
     */
    private PaymentOrder checkExistingOrder(String transactionId, PayRequest payRequest) {
        PaymentOrder existingOrder = paymentOrderMapper.selectByTransactionId(transactionId);
        if (existingOrder == null) {
            return null;
        }

        // 校验金额一致性
        if (!existingOrder.getPayAmount().equals(payRequest.getPayAmount())) {
            log.error("【支付】订单金额不一致，transactionId: {}, 原金额: {}, 请求金额: {}",
                    transactionId, existingOrder.getPayAmount(), payRequest.getPayAmount());
            throw new IllegalArgumentException("订单已存在但金额不一致");
        }

        // 检查订单状态
        if (PayStatus.SUCCESS.getCode().equals(existingOrder.getPayStatus())) {
            log.info("【支付】订单已支付成功，无需重复支付，transactionId: {}", transactionId);
            throw new IllegalArgumentException("订单已支付成功，请勿重复支付");
        }
        if (PayStatus.CLOSED.getCode().equals(existingOrder.getPayStatus())) {
            log.info("【支付】订单已关闭，无法支付，transactionId: {}", transactionId);
            throw new IllegalArgumentException("订单已关闭");
        }

        log.info("【支付】订单已存在且状态正常，触发幂等逻辑，返回已有收银台URL，transactionId: {}", transactionId);
        return existingOrder;
    }

    /**
     * 创建支付订单
     *
     * @param payRequest 支付请求
     * @return 新创建的订单
     */
    private PaymentOrder createPaymentOrder(PayRequest payRequest) {
        String transactionId = payRequest.getTransactionId();

        // 生成机构侧支付流水号
        String outTradeNo = generateOutTradeNo();

        // 创建支付订单对象
        PaymentOrder order = new PaymentOrder();
        order.setTransactionId(transactionId);
        order.setOutTradeNo(outTradeNo);
        order.setPayAmount(payRequest.getPayAmount());
        order.setSubject(payRequest.getSubject());
        order.setBody(payRequest.getBody());
        order.setUid(payRequest.getUid());
        order.setPayStatus(PayStatus.NOTPAY.getCode());
        order.setNotifyUrl(payRequest.getNotifyUrl());
        order.setRedirectUrl(payRequest.getRedirectUrl());
        order.setRequestTime(LocalDateTime.now());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        paymentOrderMapper.insert(order);
        log.info("【支付】订单创建成功，transactionId: {}, outTradeNo: {}", transactionId, outTradeNo);

        return order;
    }

    /**
     * 查询支付状态
     *
     * @param request 查询请求
     * @return 查询响应
     */
    public QueryPayResponse queryPay(QueryPayRequest request) {
        String transactionId = request.getTransactionId();

        log.info("【支付查询】查询支付状态，transactionId: {}", transactionId);

        // 从数据库查询订单
        PaymentOrder order = paymentOrderMapper.selectByTransactionId(transactionId);
        if (order == null) {
            log.error("【支付查询】订单不存在，transactionId: {}", transactionId);
            QueryPayResponse response = new QueryPayResponse();
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("订单不存在");
            return response;
        }

        // 构建响应
        QueryPayResponse response = new QueryPayResponse();
        response.setTransactionId(transactionId);
        response.setOutTradeNo(order.getOutTradeNo());
        response.setPayAmount(order.getPayAmount());
        response.setPayStatus(order.getPayStatus());
        response.setReturnCode(PaymentConstants.SUCCESS);
        response.setReturnMsg(PaymentConstants.SUCCESS);

        log.info("【支付查询】查询成功，transactionId: {}, status: {}", transactionId, order.getPayStatus());
        return response;
    }

    /**
     * 处理退款请求
     *
     * 业务逻辑：
     * 1. 幂等性检查：根据 refundNo 查询退款订单，如果存在则直接返回
     * 2. 原订单校验：检查原支付订单是否存在
     * 3. 金额校验：退款金额不能超过原支付金额（实际业务中应校验剩余可退金额）
     * 4. 创建退款订单：生成退款流水号，保存退款记录
     * 5. 写入账单：退款成功后记录账单
     *
     * @param request 退款请求
     * @return 退款响应
     */
    @Transactional(rollbackFor = Exception.class)
    public RefundResponse processRefund(RefundRequest request) {
        String refundNo = request.getRefundNo();
        String transactionId = request.getTransactionId();
        Integer refundAmount = request.getRefundAmount();

        log.info("【退款】处理退款请求，refundNo: {}, transactionId: {}, refundAmount: {}",
                refundNo, transactionId, refundAmount);

        // 检查退款订单是否已存在（幂等性）
        RefundOrder existingRefund = refundOrderMapper.selectByRefundNo(refundNo);
        if (existingRefund != null) {
            log.info("【退款】退款订单已存在，返回已有结果，refundNo: {}", refundNo);
            return buildRefundResponse(existingRefund);
        }

        // 查询原支付订单
        PaymentOrder paymentOrder = paymentOrderMapper.selectByTransactionId(transactionId);
        if (paymentOrder == null) {
            log.error("【退款】原支付订单不存在，transactionId: {}", transactionId);
            RefundResponse response = new RefundResponse();
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("原支付订单不存在");
            return response;
        }

        // 校验退款金额
        if (refundAmount > paymentOrder.getPayAmount()) {
            log.error("【退款】退款金额超过支付金额，refundAmount: {}, payAmount: {}",
                    refundAmount, paymentOrder.getPayAmount());
            RefundResponse response = new RefundResponse();
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("退款金额超过支付金额");
            return response;
        }

        // 生成机构侧退款流水号
        String outRefundNo = generateOutRefundNo();

        // 创建退款订单
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundNo(refundNo);
        refundOrder.setTransactionId(transactionId);
        refundOrder.setOutRefundNo(outRefundNo);
        refundOrder.setPayAmount(paymentOrder.getPayAmount());
        refundOrder.setRefundAmount(refundAmount);
        refundOrder.setRefundStatus(RefundStatus.SUCCESS.getCode());  // Demo 中直接设置为成功
        refundOrder.setRequestTime(LocalDateTime.now());
        refundOrder.setSuccessTime(LocalDateTime.now());
        refundOrder.setCreateTime(LocalDateTime.now());
        refundOrder.setUpdateTime(LocalDateTime.now());

        refundOrderMapper.insert(refundOrder);
        log.info("【退款】退款订单创建成功，refundNo: {}, outRefundNo: {}", refundNo, outRefundNo);

        // 写入账单记录
        saveToBillRecord(refundOrder, paymentOrder);

        return buildRefundResponse(refundOrder);
    }

    /**
     * 查询退款状态
     *
     * @param request 查询请求
     * @return 查询响应
     */
    public QueryRefundResponse queryRefund(QueryRefundRequest request) {
        String refundNo = request.getRefundNo();

        log.info("【退款查询】查询退款状态，refundNo: {}", refundNo);

        // 从数据库查询退款订单
        RefundOrder refundOrder = refundOrderMapper.selectByRefundNo(refundNo);
        if (refundOrder == null) {
            log.error("【退款查询】退款订单不存在，refundNo: {}", refundNo);
            QueryRefundResponse response = new QueryRefundResponse();
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("退款订单不存在");
            return response;
        }

        // 构建响应
        QueryRefundResponse response = new QueryRefundResponse();
        response.setRefundNo(refundNo);
        response.setOutRefundNo(refundOrder.getOutRefundNo());
        response.setRefundAmount(refundOrder.getRefundAmount());
        response.setRefundStatus(refundOrder.getRefundStatus());
        response.setReturnCode(PaymentConstants.SUCCESS);
        response.setReturnMsg(PaymentConstants.SUCCESS);

        log.info("【退款查询】查询成功，refundNo: {}, status: {}", refundNo, refundOrder.getRefundStatus());
        return response;
    }

    /**
     * 关闭订单
     *
     * 业务逻辑：
     * 1. 检查订单是否存在
     * 2. 检查订单状态：已支付成功的订单不允许关闭
     * 3. 更新状态：将订单状态更新为 CLOSED
     *
     * @param request 关闭请求
     * @return 关闭响应
     */
    @Transactional(rollbackFor = Exception.class)
    public CloseResponse closeOrder(CloseRequest request) {
        String transactionId = request.getTransactionId();

        log.info("【关闭订单】处理关闭订单请求，transactionId: {}", transactionId);

        // 查询订单
        PaymentOrder order = paymentOrderMapper.selectByTransactionId(transactionId);
        if (order == null) {
            log.error("【关闭订单】订单不存在，transactionId: {}", transactionId);
            CloseResponse response = new CloseResponse();
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("订单不存在");
            return response;
        }

        // 检查订单状态
        if (PayStatus.SUCCESS.getCode().equals(order.getPayStatus())) {
            log.error("【关闭订单】订单已支付成功，不允许关闭，transactionId: {}", transactionId);
            CloseResponse response = new CloseResponse();
            response.setReturnCode(PaymentConstants.FAIL);
            response.setReturnMsg("订单已支付成功，不允许关闭");
            return response;
        }

        // 更新订单状态为已关闭
        order.setPayStatus(PayStatus.CLOSED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        paymentOrderMapper.updateById(order);

        log.info("【关闭订单】订单关闭成功，transactionId: {}", transactionId);

        CloseResponse response = new CloseResponse();
        response.setReturnCode(PaymentConstants.SUCCESS);
        response.setReturnMsg(PaymentConstants.SUCCESS);
        return response;
    }

    /**
     * 更新订单状态（支付回调时调用）
     *
     * 业务逻辑：
     * 1. 幂等性检查：如果订单已是 SUCCESS 状态，则忽略本次更新
     * 2. 状态更新：更新订单状态、成功时间
     * 3. 账单记录：如果支付成功，将订单写入账单记录表
     *
     * @param transactionId 交易ID
     * @param status        支付状态
     * @return 更新后的订单对象
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentOrder updateOrderStatus(String transactionId, String status) {
        log.info("【更新订单状态】收到回调更新请求，transactionId: {}, status: {}", transactionId, status);

        PaymentOrder order = paymentOrderMapper.selectByTransactionId(transactionId);
        if (order == null) {
            log.error("【更新订单状态】订单不存在，transactionId: {}", transactionId);
            throw new IllegalArgumentException("订单不存在");
        }

        // 幂等性检查：如果订单已经是成功状态，不再重复处理
        if (PayStatus.SUCCESS.getCode().equals(order.getPayStatus())) {
            log.info("【更新订单状态】订单已是成功状态，跳过更新，transactionId: {}", transactionId);
            return order;
        }

        order.setPayStatus(status);
        // outTradeNo 在创建订单时已生成，此处不应修改，防止数据不一致
        if (PayStatus.SUCCESS.getCode().equals(status)) {
            order.setSuccessTime(LocalDateTime.now());
        }
        order.setUpdateTime(LocalDateTime.now());

        paymentOrderMapper.updateById(order);
        log.info("【更新订单状态】订单状态更新成功，transactionId: {}", transactionId);

        // 如果支付成功，写入账单记录
        if (PayStatus.SUCCESS.getCode().equals(status)) {
            saveToBillRecord(order);
        }

        return order;
    }

    /**
     * 保存支付订单到账单记录表
     *
     * @param order 支付订单
     */
    private void saveToBillRecord(PaymentOrder order) {
        BillRecord billRecord = new BillRecord();
        billRecord.setBillDate(order.getSuccessTime().format(DATE_FORMATTER));
        billRecord.setPayCode(payCode);
        billRecord.setTransType(TransType.PAY.getCode());
        billRecord.setRequestTime(order.getRequestTime().format(DATETIME_FORMATTER));
        billRecord.setSuccessTime(order.getSuccessTime().format(DATETIME_FORMATTER));
        billRecord.setTransactionId(order.getTransactionId());
        billRecord.setOutTransactionId(order.getOutTradeNo());
        billRecord.setTransStatus("S");
        billRecord.setTransAmount(order.getPayAmount());
        billRecord.setUserTransRealAmount(order.getPayAmount());
        billRecord.setSettleAmount(0);
        billRecord.setMarketingAmount(0);
        billRecord.setMarketingType("");
        billRecord.setMarketingFee(0);
        billRecord.setOriginTransactionId("");
        billRecord.setRate(0);
        billRecord.setFee(0);
        billRecord.setCreateTime(LocalDateTime.now());

        billRecordMapper.insert(billRecord);
        log.info("【账单记录】支付订单写入账单成功，transactionId: {}", order.getTransactionId());
    }

    /**
     * 保存退款订单到账单记录表
     *
     * @param refundOrder   退款订单
     * @param paymentOrder  原支付订单
     */
    private void saveToBillRecord(RefundOrder refundOrder, PaymentOrder paymentOrder) {
        BillRecord billRecord = new BillRecord();
        billRecord.setBillDate(refundOrder.getSuccessTime().format(DATE_FORMATTER));
        billRecord.setPayCode(payCode);
        billRecord.setTransType(TransType.REFUND.getCode());
        billRecord.setRequestTime(refundOrder.getRequestTime().format(DATETIME_FORMATTER));
        billRecord.setSuccessTime(refundOrder.getSuccessTime().format(DATETIME_FORMATTER));
        billRecord.setTransactionId(refundOrder.getRefundNo());
        billRecord.setOutTransactionId(refundOrder.getOutRefundNo());
        billRecord.setTransStatus("S");
        billRecord.setTransAmount(refundOrder.getRefundAmount());
        billRecord.setUserTransRealAmount(refundOrder.getRefundAmount());
        billRecord.setSettleAmount(0);
        billRecord.setMarketingAmount(0);
        billRecord.setMarketingType("");
        billRecord.setMarketingFee(0);
        billRecord.setOriginTransactionId(paymentOrder.getTransactionId());
        billRecord.setRate(0);
        billRecord.setFee(0);
        billRecord.setCreateTime(LocalDateTime.now());

        billRecordMapper.insert(billRecord);
        log.info("【账单记录】退款订单写入账单成功，refundNo: {}", refundOrder.getRefundNo());
    }

    /**
     * 构建收银台 URL
     *
     * @param order 支付订单
     * @return 收银台 URL
     */
    private String buildCashierUrl(PaymentOrder order) {
        Map<String, String> params = new HashMap<>();
        params.put("transactionId", order.getTransactionId());
        params.put("payAmount", String.valueOf(order.getPayAmount()));
        params.put("subject", order.getSubject());
        params.put("body", order.getBody());
        params.put("uid", order.getUid());
        params.put("redirectUrl", order.getRedirectUrl());
        params.put("notifyUrl", order.getNotifyUrl());

        try {
            String queryString = WebUtils.buildQuery(params, "UTF-8");
            return cashierUrl + "?" + queryString;
        } catch (Exception e) {
            log.error("【支付】构建收银台URL失败", e);
            throw new RuntimeException("构建收银台URL失败", e);
        }
    }

    /**
     * 构建退款响应
     *
     * @param refundOrder 退款订单
     * @return 退款响应
     */
    private RefundResponse buildRefundResponse(RefundOrder refundOrder) {
        RefundResponse response = new RefundResponse();
        response.setRefundNo(refundOrder.getRefundNo());
        response.setOutRefundNo(refundOrder.getOutRefundNo());
        response.setRefundAmount(refundOrder.getRefundAmount());
        response.setRefundStatus(refundOrder.getRefundStatus());
        response.setReturnCode(PaymentConstants.SUCCESS);
        response.setReturnMsg(PaymentConstants.SUCCESS);
        return response;
    }

    /**
     * 生成机构侧支付流水号
     *
     * @return 支付流水号
     */
    private String generateOutTradeNo() {
        return PaymentConstants.PAY_OUT_TRADENO_PREFIX + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成机构侧退款流水号
     *
     * @return 退款流水号
     */
    private String generateOutRefundNo() {
        return PaymentConstants.REFUND_OUT_TRADENO_PREFIX + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
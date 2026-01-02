package com.tbsg.h5.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付订单实体类
 *
 * @author demo
 */
@Data
@TableName("payment_order")
public class PaymentOrder {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 淘宝闪购支付流水号
     */
    private String transactionId;

    /**
     * 三方支付交易号（机构侧生成）
     */
    private String outTradeNo;

    /**
     * 支付金额（单位：分）
     */
    private Integer payAmount;

    /**
     * 订单主题
     */
    private String subject;

    /**
     * 订单详情
     */
    private String body;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 支付状态：PENDING-待支付, SUCCESS-支付成功, FAIL-支付失败, PROCESSING-支付处理中, CLOSED-已关闭
     */
    private String payStatus;

    /**
     * 支付回调地址
     */
    private String notifyUrl;

    /**
     * 支付成功后跳转地址
     */
    private String redirectUrl;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 支付成功时间
     */
    private LocalDateTime successTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

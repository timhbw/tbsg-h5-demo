package com.tbsg.h5.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款订单实体类
 *
 * @author demo
 */
@Data
@TableName("refund_order")
public class RefundOrder {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 淘宝闪购退款流水号
     */
    private String refundNo;

    /**
     * 原支付流水号（关联 payment_order）
     */
    private String transactionId;

    /**
     * 三方退款单号（机构侧生成）
     */
    private String outRefundNo;

    /**
     * 原支付金额（单位：分）
     */
    private Integer payAmount;

    /**
     * 退款金额（单位：分）
     */
    private Integer refundAmount;

    /**
     * 退款状态：PENDING-退款中, SUCCESS-退款成功, FAIL-退款失败, PROCESSING-退款处理中
     */
    private String refundStatus;

    /**
     * 退款请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 退款成功时间
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

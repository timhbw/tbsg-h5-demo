package com.tbsg.h5.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账单记录实体类
 * 严格按照账单文档的 18 个字段设计
 *
 * @author demo
 */
@Data
@TableName("bill_record")
public class BillRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账单日期（yyyy-MM-dd）
     */
    private String billDate;

    /**
     * 支付code
     */
    private String payCode;

    /**
     * 交易类型：pay-支付, refund-退款
     */
    private String transType;

    /**
     * 请求时间（yyyy-MM-dd HH:mm:ss）
     */
    private String requestTime;

    /**
     * 成功时间（yyyy-MM-dd HH:mm:ss）
     */
    private String successTime;

    /**
     * 商户流水号（支付传淘宝闪购支付流水号，退款传淘宝闪购退款流水号）
     */
    private String transactionId;

    /**
     * 渠道流水号（支付传机构支付流水号，退款传机构退款流水号）
     */
    private String outTransactionId;

    /**
     * 交易状态（S=成功）
     */
    private String transStatus;

    /**
     * 交易金额（单位：分）
     */
    private Integer transAmount;

    /**
     * 用户实际交易金额（单位：分，默认传trans_amount）
     */
    private Integer userTransRealAmount;

    /**
     * 结算金额（单位：分，默认传0）
     */
    private Integer settleAmount;

    /**
     * 营销金额（单位：分，默认传0）
     */
    private Integer marketingAmount;

    /**
     * 营销补贴类型（随单结算/后置结算，默认传空）
     */
    private String marketingType;

    /**
     * 营销技术服务费（单位：分，默认传0）
     */
    private Integer marketingFee;

    /**
     * 原淘宝闪购支付流水号（退款时不可为空）
     */
    private String originTransactionId;

    /**
     * 税率（单位：分，默认传0）
     */
    private Integer rate;

    /**
     * 交易手续费（单位：分，默认传0）
     */
    private Integer fee;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

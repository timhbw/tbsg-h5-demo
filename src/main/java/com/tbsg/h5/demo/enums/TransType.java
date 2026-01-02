package com.tbsg.h5.demo.enums;

import lombok.Getter;

/**
 * 交易类型枚举（用于账单）
 *
 * @author demo
 */
@Getter
public enum TransType {
    
    /**
     * 支付
     */
    PAY("pay", "支付"),
    
    /**
     * 退款
     */
    REFUND("refund", "退款");

    private final String code;
    private final String description;

    TransType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TransType fromCode(String code) {
        for (TransType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的交易类型: " + code);
    }
}

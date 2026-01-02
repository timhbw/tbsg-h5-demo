-- 淘宝闪购渠道 H5 示例项目数据库初始化脚本
-- 数据库: tbsg_h5_demo

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS tbsg_h5_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tbsg_h5_demo;

-- =====================================================
-- 1. 支付订单表 (payment_order)
-- =====================================================
DROP TABLE IF EXISTS `payment_order`;
CREATE TABLE `payment_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `transaction_id` VARCHAR(64) NOT NULL COMMENT '淘宝闪购支付流水号',
    `out_trade_no` VARCHAR(64) DEFAULT NULL COMMENT '三方支付交易号（机构侧生成）',
    `pay_amount` INT(11) NOT NULL COMMENT '支付金额（单位：分）',
    `subject` VARCHAR(256) DEFAULT NULL COMMENT '订单主题',
    `body` VARCHAR(512) DEFAULT NULL COMMENT '订单详情',
    `uid` VARCHAR(64) DEFAULT NULL COMMENT '用户ID',
    `pay_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '支付状态：PENDING-待支付, SUCCESS-支付成功, FAIL-支付失败, PROCESSING-支付处理中, CLOSED-已关闭',
    `notify_url` VARCHAR(512) DEFAULT NULL COMMENT '支付回调地址',
    `redirect_url` VARCHAR(512) DEFAULT NULL COMMENT '支付成功后跳转地址',
    `request_time` DATETIME DEFAULT NULL COMMENT '请求时间',
    `success_time` DATETIME DEFAULT NULL COMMENT '支付成功时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_id` (`transaction_id`),
    KEY `idx_pay_status` (`pay_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单表';

-- =====================================================
-- 2. 退款订单表 (refund_order)
-- =====================================================
DROP TABLE IF EXISTS `refund_order`;
CREATE TABLE `refund_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `refund_no` VARCHAR(64) NOT NULL COMMENT '淘宝闪购退款流水号',
    `transaction_id` VARCHAR(64) NOT NULL COMMENT '原支付流水号（关联 payment_order）',
    `out_refund_no` VARCHAR(64) DEFAULT NULL COMMENT '三方退款单号（机构侧生成）',
    `pay_amount` INT(11) NOT NULL COMMENT '原支付金额（单位：分）',
    `refund_amount` INT(11) NOT NULL COMMENT '退款金额（单位：分）',
    `refund_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '退款状态：PENDING-退款中, SUCCESS-退款成功, FAIL-退款失败, PROCESSING-退款处理中',
    `request_time` DATETIME DEFAULT NULL COMMENT '退款请求时间',
    `success_time` DATETIME DEFAULT NULL COMMENT '退款成功时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_transaction_id` (`transaction_id`),
    KEY `idx_refund_status` (`refund_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款订单表';

-- =====================================================
-- 3. 账单记录表 (bill_record)
-- 说明：严格按照账单文档的 18 个字段设计
-- =====================================================
DROP TABLE IF EXISTS `bill_record`;
CREATE TABLE `bill_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `bill_date` VARCHAR(10) NOT NULL COMMENT '账单日期（yyyy-MM-dd）',
    `pay_code` VARCHAR(64) NOT NULL COMMENT '支付code',
    `trans_type` VARCHAR(10) NOT NULL COMMENT '交易类型：pay-支付, refund-退款',
    `request_time` VARCHAR(19) DEFAULT NULL COMMENT '请求时间（yyyy-MM-dd HH:mm:ss）',
    `success_time` VARCHAR(19) DEFAULT NULL COMMENT '成功时间（yyyy-MM-dd HH:mm:ss）',
    `transaction_id` VARCHAR(64) NOT NULL COMMENT '商户流水号（支付传淘宝闪购支付流水号，退款传淘宝闪购退款流水号）',
    `out_transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '渠道流水号（支付传机构支付流水号，退款传机构退款流水号）',
    `trans_status` VARCHAR(10) NOT NULL DEFAULT 'S' COMMENT '交易状态（S=成功）',
    `trans_amount` INT(11) NOT NULL COMMENT '交易金额（单位：分）',
    `user_trans_real_amount` INT(11) NOT NULL COMMENT '用户实际交易金额（单位：分，默认传trans_amount）',
    `settle_amount` INT(11) NOT NULL DEFAULT 0 COMMENT '结算金额（单位：分，默认传0）',
    `marketing_amount` INT(11) NOT NULL DEFAULT 0 COMMENT '营销金额（单位：分，默认传0）',
    `marketing_type` VARCHAR(64) DEFAULT NULL COMMENT '营销补贴类型（随单结算/后置结算，默认传空）',
    `marketing_fee` INT(11) NOT NULL DEFAULT 0 COMMENT '营销技术服务费（单位：分，默认传0）',
    `origin_transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '原淘宝闪购支付流水号（退款时不可为空）',
    `rate` INT(11) NOT NULL DEFAULT 0 COMMENT '税率（单位：分，默认传0）',
    `fee` INT(11) NOT NULL DEFAULT 0 COMMENT '交易手续费（单位：分，默认传0）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_bill_date` (`bill_date`),
    KEY `idx_transaction_id` (`transaction_id`),
    KEY `idx_trans_type` (`trans_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单记录表（用于生成CSV）';

-- =====================================================
-- 插入测试数据（可选）
-- =====================================================

-- 插入测试支付订单
INSERT INTO `payment_order` (`transaction_id`, `out_trade_no`, `pay_amount`, `subject`, `body`, `uid`, `pay_status`, `request_time`, `success_time`) 
VALUES 
('TEST_PAY_001', 'OUT_TRADE_001', 10000, '测试订单1', '这是一个测试订单', 'user_001', 'SUCCESS', NOW(), NOW()),
('TEST_PAY_002', 'OUT_TRADE_002', 20000, '测试订单2', '这是另一个测试订单', 'user_002', 'PENDING', NOW(), NULL);

-- 插入测试退款订单
INSERT INTO `refund_order` (`refund_no`, `transaction_id`, `out_refund_no`, `pay_amount`, `refund_amount`, `refund_status`, `request_time`, `success_time`) 
VALUES 
('TEST_REFUND_001', 'TEST_PAY_001', 'OUT_REFUND_001', 10000, 5000, 'SUCCESS', NOW(), NOW());

-- 插入测试账单记录
INSERT INTO `bill_record` (`bill_date`, `pay_code`, `trans_type`, `request_time`, `success_time`, `transaction_id`, `out_transaction_id`, `trans_status`, `trans_amount`, `user_trans_real_amount`) 
VALUES 
(DATE_FORMAT(NOW(), '%Y-%m-%d'), 'SDKDEMO_OPENPAY', 'pay', DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 'TEST_PAY_001', 'OUT_TRADE_001', 'S', 10000, 10000),
(DATE_FORMAT(NOW(), '%Y-%m-%d'), 'SDKDEMO_OPENPAY', 'refund', DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 'TEST_REFUND_001', 'OUT_REFUND_001', 'S', 5000, 5000);

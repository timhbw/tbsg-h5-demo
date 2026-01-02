package com.tbsg.h5.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tbsg.h5.demo.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付订单 Mapper 接口
 *
 * @author demo
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {

    /**
     * 根据交易ID查询订单
     *
     * @param transactionId 交易ID
     * @return 支付订单
     */
    default PaymentOrder selectByTransactionId(String transactionId) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentOrder>()
                .eq(PaymentOrder::getTransactionId, transactionId));
    }

    /**
     * 根据日期范围查询订单
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 订单列表
     */
    default List<PaymentOrder> selectByDateRange(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentOrder>()
                .between(PaymentOrder::getCreateTime, startTime, endTime)
                .orderByDesc(PaymentOrder::getCreateTime));
    }
}

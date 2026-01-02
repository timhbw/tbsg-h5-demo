package com.tbsg.h5.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tbsg.h5.demo.entity.RefundOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款订单 Mapper 接口
 *
 * @author demo
 */
@Mapper
public interface RefundOrderMapper extends BaseMapper<RefundOrder> {

    /**
     * 根据退款流水号查询退款订单
     *
     * @param refundNo 退款流水号
     * @return 退款订单
     */
    default RefundOrder selectByRefundNo(String refundNo) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RefundOrder>()
                .eq(RefundOrder::getRefundNo, refundNo));
    }

    /**
     * 根据原支付流水号查询退款订单列表
     *
     * @param transactionId 原支付流水号
     * @return 退款订单列表
     */
    default List<RefundOrder> selectByTransactionId(String transactionId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RefundOrder>()
                .eq(RefundOrder::getTransactionId, transactionId)
                .orderByDesc(RefundOrder::getCreateTime));
    }

    /**
     * 根据日期范围查询退款订单
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 退款订单列表
     */
    default List<RefundOrder> selectByDateRange(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RefundOrder>()
                .between(RefundOrder::getCreateTime, startTime, endTime)
                .orderByDesc(RefundOrder::getCreateTime));
    }
}

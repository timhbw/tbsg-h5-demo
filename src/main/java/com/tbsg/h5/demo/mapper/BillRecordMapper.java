package com.tbsg.h5.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tbsg.h5.demo.entity.BillRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 账单记录 Mapper 接口
 *
 * @author demo
 */
@Mapper
public interface BillRecordMapper extends BaseMapper<BillRecord> {

    /**
     * 根据账单日期查询账单记录
     *
     * @param billDate 账单日期（yyyy-MM-dd）
     * @return 账单记录列表
     */
    default List<BillRecord> selectByBillDate(String billDate) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BillRecord>()
                .eq(BillRecord::getBillDate, billDate)
                .orderByAsc(BillRecord::getTransType)  // pay 在前，refund 在后
                .orderByAsc(BillRecord::getCreateTime));
    }

    /**
     * 根据交易ID查询账单记录
     *
     * @param transactionId 交易ID
     * @return 账单记录
     */
    default BillRecord selectByTransactionId(String transactionId) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BillRecord>()
                .eq(BillRecord::getTransactionId, transactionId));
    }
}

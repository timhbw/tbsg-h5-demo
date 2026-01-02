package com.tbsg.h5.demo.service;

import com.tbsg.h5.demo.entity.BillRecord;
import com.tbsg.h5.demo.mapper.BillRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 账单服务
 *
 * 负责生成符合淘宝闪购规范的账单 CSV 文件
 * 账单包含17个字段，严格按照官方文档要求
 *
 * @author demo
 */
@Slf4j
@Service
public class BillService {

    @Autowired
    private BillRecordMapper billRecordMapper;

    @Autowired
    private OssService ossService;

    @Value("${tbsg.bill.storage-path}")
    private String storagePath;

    @Value("${tbsg.pay.code}")
    private String payCode;

    /**
     * 生成账单并上传到 OSS
     *
     * @param billDate 账单日期
     */
    public void generateAndUploadBill(String billDate) {
        log.info("【账单下载】开始生成并上传账单，billDate: {}", billDate);
        String fileName = null;
        java.io.File file = null;
        try {
            // 1. 生成本地 CSV 文件
            fileName = generateBillCsv(billDate);
            file = new java.io.File(storagePath, fileName);

            // 2. 上传到 OSS
            String objectName = "bills/" + fileName;
            ossService.uploadFile(objectName, file);

            log.info("【账单下载】账单上传 OSS 成功，objectName: {}", objectName);

        } catch (Exception e) {
            log.error("【账单下载】账单生成或上传失败，billDate: {}", billDate, e);
            throw new RuntimeException("账单处理失败", e);
        } finally {
            // 3. 删除本地临时文件
            if (file != null && file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("【账单下载】本地临时文件已删除: {}", file.getAbsolutePath());
                } else {
                    log.warn("【账单下载】本地临时文件删除失败: {}", file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 生成账单 CSV 文件并保存到本地
     *
     * 按照淘宝闪购账单文档规范，生成包含17个字段的 CSV 文件
     *
     * @param billDate 账单日期（格式：yyyy-MM-dd）
     * @return 生成的文件名
     * @throws IOException CSV 生成失败
     */
    public String generateBillCsv(String billDate) throws IOException {
        log.info("【账单下载】开始生成账单，billDate: {}", billDate);

        // 1. 准备文件
        java.io.File dir = new java.io.File(storagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 文件名格式：{payCode}_bill_{date}.csv
        String fileName = payCode + "_bill_" + billDate + ".csv";
        java.io.File file = new java.io.File(dir, fileName);

        // 2. 查询账单记录
        List<BillRecord> billRecords = billRecordMapper.selectByBillDate(billDate);

        if (billRecords == null || billRecords.isEmpty()) {
            log.warn("【账单下载】账单记录为空，生成空文件，billDate: {}", billDate);
            generateEmptyCsvFile(file);
            return fileName;
        }

        log.info("【账单下载】查询到 {} 条账单记录", billRecords.size());

        // 3. 过滤全额支付和全额退款的配对记录（同日整单退款不展示）
        List<BillRecord> filteredRecords = filterBillRecords(billRecords);
        log.info("【账单下载】过滤后剩余 {} 条账单记录", filteredRecords.size());

        // 4. 定义 CSV 格式（17个字段）
        String[] headers = {
            "bill_date",                // 1. 账单日期
            "pay_code",                 // 2. 支付code
            "trans_type",               // 3. 交易类型
            "request_time",             // 4. 请求时间
            "success_time",             // 5. 成功时间
            "transaction_id",           // 6. 商户流水号
            "out_transaction_id",       // 7. 渠道流水号
            "trans_status",             // 8. 交易状态
            "trans_amount",             // 9. 交易金额
            "user_trans_real_amount",   // 10. 用户实际交易金额
            "settle_amount",            // 11. 结算金额
            "marketing_amount",         // 12. 营销金额
            "marketing_type",           // 13. 营销补贴类型
            "marketing_fee",            // 14. 营销技术服务费
            "origin_transaction_id",    // 15. 原淘宝闪购支付流水号
            "rate",                     // 16. 税率
            "fee"                       // 17. 交易手续费
        };

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(headers)
                .build();

        // 5. 流式写入文件
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {

            for (BillRecord record : filteredRecords) {
                csvPrinter.printRecord(
                    record.getBillDate(),
                    record.getPayCode(),
                    record.getTransType(),
                    formatDateTime(record.getRequestTime()),
                    formatDateTime(record.getSuccessTime()),
                    record.getTransactionId(),
                    record.getOutTransactionId(),
                    record.getTransStatus(),
                    getAmountOrDefault(record.getTransAmount(), 0),
                    getAmountOrDefault(record.getUserTransRealAmount(), record.getTransAmount()),
                    getAmountOrDefault(record.getSettleAmount(), 0),
                    getAmountOrDefault(record.getMarketingAmount(), 0),
                    record.getMarketingType(),
                    getAmountOrDefault(record.getMarketingFee(), 0),
                    record.getOriginTransactionId(),
                    getAmountOrDefault(record.getRate(), 0),
                    getAmountOrDefault(record.getFee(), 0)
                );
            }

            csvPrinter.flush();
        }

        log.info("【账单下载】账单生成并保存成功，路径: {}", file.getAbsolutePath());
        return fileName;
    }

    /**
     * 生成空的 CSV 文件（只有表头）
     */
    private void generateEmptyCsvFile(java.io.File file) throws IOException {
        String[] headers = {
            "bill_date", "pay_code", "trans_type", "request_time", "success_time",
            "transaction_id", "out_transaction_id", "trans_status", "trans_amount",
            "user_trans_real_amount", "settle_amount", "marketing_amount", "marketing_type",
            "marketing_fee", "origin_transaction_id", "rate", "fee"
        };

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(headers)
                .build();

        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
            csvPrinter.flush();
        }
    }

    /**
     * 获取金额，如果为空则返回默认值
     *
     * @param amount 金额（分）
     * @param defaultValue 默认值
     * @return 金额
     */
    private Integer getAmountOrDefault(Integer amount, Integer defaultValue) {
        if (amount == null) {
            return defaultValue;
        }
        return amount;
    }

    /**
     * 格式化日期时间
     *
     * @param dateTime 日期时间字符串
     * @return 格式化后的日期时间字符串
     */
    private String formatDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "";
        }
        return dateTime;
    }

    /**
     * 验证账单日期格式
     *
     * @param billDate 账单日期（格式：yyyy-MM-dd）
     * @return 是否有效
     */
    public boolean isValidBillDate(String billDate) {
        if (billDate == null || billDate.length() != 10) {
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(billDate, formatter);

            // T-30 校验：只能查询最近 30 天内的账单
            LocalDate limitDate = LocalDate.now().minusDays(30);
            if (date.isBefore(limitDate)) {
                log.warn("【账单下载】账单日期超出 T-30 限制，billDate: {}", billDate);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("【账单下载】账单日期格式错误，billDate: {}", billDate, e);
            return false;
        }
    }

    /**
     * 过滤账单记录，移除同一天内全额支付和全额退款的配对记录
     *
     * @param billRecords 原始账单记录列表
     * @return 过滤后的账单记录列表
     */
    private List<BillRecord> filterBillRecords(List<BillRecord> billRecords) {
        if (billRecords == null || billRecords.isEmpty()) {
            return billRecords;
        }

        // 用于存储需要排除的记录ID
        Set<String> excludedTransactionIds = new HashSet<>();

        // 1. 构建支付记录的映射：transactionId -> BillRecord
        Map<String, BillRecord> payRecordMap = billRecords.stream()
                .filter(record -> "pay".equals(record.getTransType()))
                .collect(Collectors.toMap(
                        BillRecord::getTransactionId,
                        record -> record,
                        (existing, replacement) -> existing
                ));

        // 2. 遍历所有退款记录，查找匹配的支付记录
        for (BillRecord refundRecord : billRecords) {
            if (!"refund".equals(refundRecord.getTransType())) {
                continue;
            }

            String originTransactionId = refundRecord.getOriginTransactionId();
            if (originTransactionId == null) {
                continue;
            }

            // 查找对应的支付记录
            BillRecord payRecord = payRecordMap.get(originTransactionId);
            if (payRecord == null) {
                continue;
            }

            // 3. 检查是否为全额退款
            Integer refundAmount = refundRecord.getTransAmount();
            Integer payAmount = payRecord.getTransAmount();

            if (refundAmount != null && refundAmount.equals(payAmount)) {
                // 匹配成功：将支付记录和退款记录都标记为排除
                excludedTransactionIds.add(payRecord.getTransactionId());
                excludedTransactionIds.add(refundRecord.getTransactionId());
            }
        }

        // 4. 返回未被排除的记录
        return billRecords.stream()
                .filter(record -> !excludedTransactionIds.contains(record.getTransactionId()))
                .collect(Collectors.toList());
    }
}
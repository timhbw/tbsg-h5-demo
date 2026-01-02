package com.tbsg.h5.demo.task;

import com.tbsg.h5.demo.service.BillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 账单定时任务
 *
 * @author demo
 */
@Slf4j
@Component
public class BillTask {

    @Autowired
    private BillService billService;

    /**
     * 每天凌晨 4 点生成前一天的账单
     * 保存到本地 bills 目录下
     *
     * cron: 0 0 4 * * ? (每天 04:00:00 执行)
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void generateDailyBill() {
        // 生成前一天的账单
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String billDate = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log.info("【定时任务】开始生成账单，目标日期: {}", billDate);

        try {
            // 1. 生成账单并上传到 OSS
            billService.generateAndUploadBill(billDate);

            log.info("【定时任务】账单生成并上传成功。日期: {}", billDate);

        } catch (Exception e) {
            log.error("【定时任务】账单生成失败。日期: {}, 错误信息: {}", billDate, e.getMessage(), e);
        }
    }
}
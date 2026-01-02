package com.tbsg.h5.demo.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.Date;

/**
 * 阿里云 OSS 服务类
 *
 * @author demo
 */
@Slf4j
@Service
public class OssService {

    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件到 OSS
     *
     * @param objectName OSS 中的对象名称（包含路径）
     * @param file       本地文件
     */
    public void uploadFile(String objectName, File file) {
        try {
            log.info("开始上传文件到 OSS, bucketName: {}, objectName: {}, filePath: {}", bucketName, objectName, file.getAbsolutePath());
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file);
            ossClient.putObject(putObjectRequest);
            log.info("文件上传成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件上传失败: {}", objectName, e);
            throw new RuntimeException("OSS 文件上传失败", e);
        }
    }

    /**
     * 检查 OSS 中是否存在指定对象
     *
     * @param objectName OSS 中的对象名称
     * @return true 如果存在，否则 false
     */
    public boolean doesObjectExist(String objectName) {
        try {
            return ossClient.doesObjectExist(bucketName, objectName);
        } catch (Exception e) {
            log.error("检查 OSS 文件是否存在失败: {}", objectName, e);
            throw new RuntimeException("检查 OSS 文件是否存在失败", e);
        }
    }

    /**
     * 获取文件的预签名下载 URL
     *
     * @param objectName OSS 中的对象名称
     * @return 预签名 URL 字符串
     */
    public String getPresignedUrl(String objectName) {
        try {
            // 设置 URL 过期时间为 1 小时
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
        } catch (Exception e) {
            log.error("获取预签名 URL 失败: {}", objectName, e);
            throw new RuntimeException("获取 OSS 预签名 URL 失败", e);
        }
    }
}
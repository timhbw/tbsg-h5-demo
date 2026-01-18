package com.tbsg.h5.demo.openapi;

import com.alibaba.fastjson.JSON;
import eleme.openapi.sdk.api.entity.alliance.BusinessParamModel;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3CreateRequest;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3Model;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3Response;
import eleme.openapi.sdk.api.exception.ServiceException;
import eleme.openapi.sdk.api.service.AllianceService;
import eleme.openapi.sdk.config.Config;
import eleme.openapi.sdk.config.ElemeSdkLogger;
import eleme.openapi.sdk.oauth.response.Token;

import java.util.HashMap;
import java.util.Map;

public class CreateChannelLevel3Demo {
    public static void main(String[] args) {
        // 正式环境 Key、Secret 查看路径：管理中心-应用管理-查看应用-正式环境，https://open.shop.ele.me/manager/openapi/manage-center
        String appKey = "appKey";
        String appSecret = "appSecret";

        // Tips1：isPPE 一定要为 false 并且如下入参才能请求渠道专用接口
        boolean isPPE = false;
        Config config = new Config(appKey, isPPE, appSecret);
        config.setHttpPoolRequest(true);
        config.setLog(new ElemeSdkLogger() {
            @Override
            public void info(String message) {
                // 提取排查日志使用的 requestId
                String requestId = message.contains("\"id\":\"")
                        ? message.substring(message.indexOf("\"id\":\"") + 6, message.indexOf("\",", message.indexOf("\"id\":\"")))
                        : "unknown";
                System.out.println("RequestId: " + requestId);
            }

            @Override
            public void error(String message) {
                System.err.println("Error: " + message);
            }
        });


        // Tips2：该接口无需授权,token为空即可
        String accessToken = "";
        Token token = new Token();
        token.setAccessToken(accessToken);

        // 使用config和token对象，实例化一个服务对象
        AllianceService allianceService = new AllianceService(config, token);

        // 构建请求参数
        ChannelLevel3CreateRequest request = new ChannelLevel3CreateRequest();
        // 示例参数，请根据实际情况修改，接口文档：https://open.shop.ele.me/base/apilist/eleme-alliance/eleme-alliance-createChannelLevel3

        // 一级渠道id(默认都传2)
        request.setLevel1(2L);
        // 二级渠道id，联系平台获取
        request.setLevel2(386L);
        // 三级渠道名称，C端不展示
        request.setName("官方三级渠道测试-员工晚餐");
        // 后台展示的渠道名称(可以和name保持一致,C端不展示)
        request.setDisplayName("官方三级渠道测试-员工晚餐");
        // 三级渠道号，要求:字母小写、数字、下划线,二级渠道下保持唯一,C端不展示
        request.setChannel("three_channel_test_dinner");
        request.setDescription("三级渠道描述");
        request.setCreator("system_creator");
        // 生效中:1、审批中:2、审批失败:3、待生效:4、已下线 5
        request.setStatus(1);

        BusinessParamModel businessParam = new BusinessParamModel();
        Map<String,String> businessParams = new HashMap<>();
        businessParams.put("checkoutAnnouncementTip", "{\"config\":\"{\\\"checkoutAnnouncementTip\\\":\\\"公告标题:公告的具体文案内容\\\"}\",\"functionCode\":\"checkoutAnnouncementTip\"}");

        businessParam.setBusinessParams(businessParams);
        request.setBusinessParam(businessParam);

        try {
            ChannelLevel3Response response = allianceService.createChannelLevel3(request);
            // 打印完整响应
            System.out.println("完整响应: " + JSON.toJSONString(response));

            if (response != null && response.getResult() != null) {
                ChannelLevel3Model model = response.getResult();
                System.out.println("--------------------------------------------------");
                System.out.println("创建成功，渠道详情如下：");
                System.out.println("三级渠道ID: " + model.getId());
                System.out.println("三级渠道名称: " + model.getName());
                System.out.println("三级渠道号: " + model.getChannel());
                System.out.println("显示名称: " + model.getDisplayName());
                System.out.println("状态: " + model.getStatus());
                System.out.println("创建时间: " + model.getCreatedTime());
                System.out.println("--------------------------------------------------");
            } else {
                System.out.println("响应为空或未返回结果对象。");
            }
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
package com.tbsg.h5.demo.openapi;

import com.alibaba.fastjson.JSON;
import eleme.openapi.sdk.api.entity.alliance.BusinessParamModel;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3BatchUpdateRequest;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3UpdateRequest;
import eleme.openapi.sdk.api.exception.ServiceException;
import eleme.openapi.sdk.api.service.AllianceService;
import eleme.openapi.sdk.config.Config;
import eleme.openapi.sdk.config.ElemeSdkLogger;
import eleme.openapi.sdk.oauth.response.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateChannelLevel3Demo {
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
        ChannelLevel3BatchUpdateRequest channelLevel3BatchUpdateRequest = new ChannelLevel3BatchUpdateRequest();
        List<ChannelLevel3UpdateRequest> channelLevel3UpdateRequests = new ArrayList<ChannelLevel3UpdateRequest>();
        
        ChannelLevel3UpdateRequest channelLevel3UpdateRequest = new ChannelLevel3UpdateRequest();
        // 示例参数，请根据实际情况修改，接口文档：https://open.shop.ele.me/base/apilist/eleme-alliance/eleme-alliance-updateChannelLevel3
        channelLevel3UpdateRequest.setId(1L);
        channelLevel3UpdateRequest.setName("员工餐饮-午餐");
        channelLevel3UpdateRequest.setDisplayName("员工餐饮-午餐");
        channelLevel3UpdateRequest.setChannel("mobile.guanaitongnew.lunch");
        channelLevel3UpdateRequest.setDescription("系统添加");
        channelLevel3UpdateRequest.setStatus(1);
        
        // 设置业务参数
        BusinessParamModel businessParam = new BusinessParamModel();
        Map<String, String> businessParams = new HashMap<String, String>();
        businessParam.setBusinessParams(businessParams);
        
        List<String> ignoreBusinessCode = new ArrayList<String>();
        ignoreBusinessCode.add("hideOrderAgain");
        ignoreBusinessCode.add("hidePlatformSubsidies");
        businessParam.setIgnoreBusinessCode(ignoreBusinessCode);
        
        channelLevel3UpdateRequest.setBusinessParam(businessParam);
        channelLevel3UpdateRequests.add(channelLevel3UpdateRequest);
        channelLevel3BatchUpdateRequest.setChannelLevel3UpdateRequests(channelLevel3UpdateRequests);

        try {
            allianceService.updateChannelLevel3(channelLevel3BatchUpdateRequest);
            System.out.println("--------------------------------------------------");
            System.out.println("更新成功！");
            System.out.println("更新的渠道ID: " + channelLevel3UpdateRequest.getId());
            System.out.println("更新的渠道名称: " + channelLevel3UpdateRequest.getName());
            System.out.println("更新的渠道代码: " + channelLevel3UpdateRequest.getChannel());
            System.out.println("--------------------------------------------------");
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}

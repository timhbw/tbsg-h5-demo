package com.tbsg.h5.demo.openapi;

import com.alibaba.fastjson.JSON;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3BatchResponse;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3Model;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3QueryRequest;
import eleme.openapi.sdk.api.exception.ServiceException;
import eleme.openapi.sdk.api.service.AllianceService;
import eleme.openapi.sdk.config.Config;
import eleme.openapi.sdk.config.ElemeSdkLogger;
import eleme.openapi.sdk.oauth.response.Token;

import java.util.ArrayList;
import java.util.List;

public class QueryChannelLevel3ListDemo {
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
        ChannelLevel3QueryRequest channelLevel3QueryRequest = new ChannelLevel3QueryRequest();
        // 示例参数，请根据实际情况修改，接口文档：https://open.shop.ele.me/base/apilist/eleme-alliance/eleme-alliance-queryChannelLevel3List
        
        // 优先通过渠道ID列表查询
        List<Long> channelIdList = new ArrayList<Long>();
        channelIdList.add(949972L);
        channelLevel3QueryRequest.setChannelIdList(channelIdList);
        
        // 通过渠道代码列表查询
//        List<String> channelList = new ArrayList<String>();
//        channelList.add("mobile.sdkdemo.1");
//        channelList.add("mobile.sdkdemo.2");
//        channelLevel3QueryRequest.setChannelList(channelList);

        try {
            ChannelLevel3BatchResponse response = allianceService.queryChannelLevel3List(channelLevel3QueryRequest);
            // 打印完整响应
            System.out.println("完整响应: " + JSON.toJSONString(response));

            if (response != null && response.getResult() != null && !response.getResult().isEmpty()) {
                List<ChannelLevel3Model> channelList3Models = response.getResult();
                System.out.println("--------------------------------------------------");
                System.out.println("查询成功，共查询到 " + channelList3Models.size() + " 条渠道数据：");
                for (ChannelLevel3Model model : channelList3Models) {
                    System.out.println("---");
                    System.out.println("三级渠道ID: " + model.getId());
                    System.out.println("渠道名称: " + model.getName());
                    System.out.println("三级渠道号: " + model.getChannel());
                    System.out.println("显示名称: " + model.getDisplayName());
                    System.out.println("状态: " + model.getStatus());
                    System.out.println("创建时间: " + model.getCreatedTime());
                }
                System.out.println("--------------------------------------------------");
            } else {
                System.out.println("未查询到任何渠道数据。");
            }
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}

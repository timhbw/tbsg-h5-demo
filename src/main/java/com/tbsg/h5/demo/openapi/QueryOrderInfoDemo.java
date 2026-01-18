package com.tbsg.h5.demo.openapi;

import com.alibaba.fastjson.JSON;
import eleme.openapi.sdk.api.entity.alliance.ChannelOrderInfoModel;
import eleme.openapi.sdk.api.entity.alliance.ChannelOrderInfoResponse;
import eleme.openapi.sdk.api.entity.alliance.ChannelOrderQueryRequest;
import eleme.openapi.sdk.api.exception.ServiceException;
import eleme.openapi.sdk.api.service.AllianceService;
import eleme.openapi.sdk.config.Config;
import eleme.openapi.sdk.config.ElemeSdkLogger;
import eleme.openapi.sdk.oauth.response.Token;

import java.util.ArrayList;
import java.util.List;

public class QueryOrderInfoDemo {
    public static void main(String[] args) {
        // 不可使用沙箱环境，正式环境 Key、Secret 查看路径：管理中心-应用管理-查看应用-正式环境，https://open.shop.ele.me/manager/openapi/manage-center
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
        ChannelOrderQueryRequest request = new ChannelOrderQueryRequest();

        //   接口文档：https://open.shop.ele.me/base/apilist/eleme-alliance/eleme-alliance-queryOrderInfo
        List<Long> alscOrderNos = new ArrayList<>();
        alscOrderNos.add(8012666117942909222L);
//        alscOrderNos.add(8025746100338902000L);
        request.setAlscOrderNos(alscOrderNos);

        request.setChannelLevel2("mobile.sdkdemo");
        request.setChannelLevel3("mobile.sdkdemo.default");

        try {
            ChannelOrderInfoResponse response = allianceService.queryOrderInfo(request);
            // 打印完整响应
            System.out.println("完整响应: " + JSON.toJSONString(response));

            if (response != null && response.getResult() != null) {
                List<ChannelOrderInfoModel> orderList = response.getResult();
                System.out.println("查询到 " + orderList.size() + " 条订单信息：");
                for (ChannelOrderInfoModel order : orderList) {
                    System.out.println("--------------------------------------------------");
                    System.out.println("订单号: " + order.getAlscOrderNo());
                    System.out.println("店铺名称: " + order.getShopName());
                    System.out.println("订单状态: " + order.getStatus());
                    System.out.println("订单类型: " + order.getOrderType());
                    System.out.println("总金额(分): " + order.getTotalAmount());
                    System.out.println("实付金额(分): " + order.getRealAmount());
                    System.out.println("创建时间: " + order.getCreateAt());
                }
                System.out.println("--------------------------------------------------");
            } else {
                System.out.println("未查询到订单信息或响应为空。");
            }
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
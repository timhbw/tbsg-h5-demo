package com.tbsg.h5.demo.openapi;

import com.alibaba.fastjson.JSON;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3CreateRequest;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3Model;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3Response;
import eleme.openapi.sdk.api.exception.ServiceException;
import eleme.openapi.sdk.api.service.AllianceService;
import eleme.openapi.sdk.config.Config;
import eleme.openapi.sdk.oauth.response.Token;

public class CreateChannelLevel3Demo {
    public static void main(String[] args) {
        // 正式环境 Key、Secret 查看路径：管理中心-应用管理-查看应用-正式环境，https://open.shop.ele.me/manager/openapi/manage-center
        String appKey = "appKey";
        String appSecret = "appSecret";

        // Tips1：isPPE 一定要为 false 并且如下入参才能请求渠道专用接口
        boolean isPPE = false;
        Config config = new Config(appKey, isPPE, appSecret);
        config.setHttpPoolRequest(true);

        // Tips2：该接口无需授权,token为空即可
        String accessToken = "";
        Token token = new Token();
        token.setAccessToken(accessToken);

        // 使用config和token对象，实例化一个服务对象
        AllianceService allianceService = new AllianceService(config, token);

        // 构建请求参数
        ChannelLevel3CreateRequest request = new ChannelLevel3CreateRequest();
        // 示例参数，请根据实际情况修改，接口文档：https://open.shop.ele.me/base/apilist/eleme-alliance/eleme-alliance-createChannelLevel3
        request.setLevel1(1L);
        request.setLevel2(2L);
        request.setName("test_channel_name");
        request.setDisplayName("测试渠道名称");
        request.setChannel("test_channel_code");
        request.setDescription("测试渠道描述");
        request.setCreator("test_creator");
        request.setStatus(1); // 1: 有效

        try {
            ChannelLevel3Response response = allianceService.createChannelLevel3(request);
            // 打印完整响应
            System.out.println("完整响应: " + JSON.toJSONString(response));

            if (response != null && response.getResult() != null) {
                ChannelLevel3Model model = response.getResult();
                System.out.println("--------------------------------------------------");
                System.out.println("创建成功，渠道详情如下：");
                System.out.println("渠道ID: " + model.getId());
                System.out.println("渠道名称: " + model.getName());
                System.out.println("渠道代码: " + model.getChannel());
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
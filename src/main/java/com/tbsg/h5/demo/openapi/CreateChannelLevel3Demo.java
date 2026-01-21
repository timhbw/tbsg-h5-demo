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

import java.util.*;

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
        request.setChannel("three_channel_test_dinner3");
        request.setDescription("三级渠道描述");
        request.setCreator("system_creator");
        // 生效中:1、审批中:2、审批失败:3、待生效:4、已下线 5
        request.setStatus(1);

        // 设置业务参数 - 使用 BusinessParamBuilder 构建
        // 前端只需传入简单的业务字段，Builder 负责转换为 SDK 需要的复杂格式

        // 构建企业地址列表
        BusinessParamBuilder.EnterpriseAddress address = new BusinessParamBuilder.EnterpriseAddress(
                "1", "上海市普陀区", "近铁城市广场北区",
                "3", "31.232823191373864", "121.38141609736105", "0"
        );
        address.setAddressCheckNotice("请确认收货人");
        address.setPoiId("B0LAYUW7H2");

        BusinessParamBuilder.EnterpriseAddress address2 = new BusinessParamBuilder.EnterpriseAddress(
                "2", "澳门特别行政区", "石排郊野湾公园",
                "3", "22.122676889125273", "113.56507922628968", "1"
        );
        address2.setAddressCheckNotice("请确认收货人");
        address2.setPoiId("B073D00IID");

        // 构建用餐时间限制配置（周一至周日 17:30-22:00）
        Map<String, List<String[]>> weekTimeConfig = new HashMap<>();
        List<String[]> lunchTimeSlots = new ArrayList<>();
        lunchTimeSlots.add(new String[]{"17:30", "22:00"});
        weekTimeConfig.put("monday", lunchTimeSlots);
        weekTimeConfig.put("tuesday", lunchTimeSlots);
        weekTimeConfig.put("wednesday", lunchTimeSlots);
        weekTimeConfig.put("thursday", lunchTimeSlots);
        weekTimeConfig.put("friday", lunchTimeSlots);
        weekTimeConfig.put("saturday", lunchTimeSlots);
        weekTimeConfig.put("sunday", lunchTimeSlots);

        Map<String, String> businessParams = new BusinessParamBuilder()
                // 1. 结算页公告配置
                .withCheckoutAnnouncementTip("公告标题", "公告的具体文案内容")
                // 2. 配送地址类型：enterprise (企业地址), personal (个人地址)
                .withUseEnterpriseAddress("enterprise")
                // 3. 企业地址详情（当配送地址类型为 enterprise 时必填）
                .withEnterpriseAddressDetail(Arrays.asList(address, address2))
                // 4. 预定单过滤：true (过滤/不支持预定), false (支持预定)
                .withBookTimeTimeFilter(true)
                // 5. 屏蔽到店自提：true (隐藏/屏蔽), false (展示)
                .withHideTakeBySelf(true)
                // 6. 用餐时间限制
                .withOrderTimeLimit(weekTimeConfig)
                // 7. 搜推召回规则
                .withShopCallbackFlavor(Arrays.asList("209","3232","3240","212"))
                // 8. 店铺展示规则（隐藏商品分类）
                .withHideItemCategories(Arrays.asList("201839304", "201835105"))
                .build();

        BusinessParamModel businessParam = new BusinessParamModel();
        // 使用强制类型转换，因为 SDK 定义的类型是 Map<String, String>，但实际需要的是 Map<String, Object>
        // Java 泛型在运行时会被擦除，所以这种转换在运行时是安全的
        businessParam.setBusinessParams(businessParams);
        request.setBusinessParam(businessParam);

        try {
            // 打印完整请求参数
            System.out.println("完整请求: " + JSON.toJSONString(request));

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
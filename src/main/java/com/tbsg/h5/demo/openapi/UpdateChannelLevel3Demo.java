package com.tbsg.h5.demo.openapi;

import com.alibaba.fastjson.JSON;
import eleme.openapi.sdk.api.entity.alliance.BusinessParamModel;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3BatchResponse;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3BatchUpdateRequest;
import eleme.openapi.sdk.api.entity.alliance.ChannelLevel3UpdateRequest;
import eleme.openapi.sdk.api.exception.ServiceException;
import eleme.openapi.sdk.api.service.AllianceService;
import eleme.openapi.sdk.config.Config;
import eleme.openapi.sdk.config.ElemeSdkLogger;
import eleme.openapi.sdk.oauth.response.Token;

import java.util.*;

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
        List<ChannelLevel3UpdateRequest> channelLevel3UpdateRequests = new ArrayList<>();

        ChannelLevel3UpdateRequest channelLevel3UpdateRequest = new ChannelLevel3UpdateRequest();
        // 示例参数，请根据实际情况修改，接口文档：https://open.shop.ele.me/base/apilist/eleme-alliance/eleme-alliance-updateChannelLevel3
        channelLevel3UpdateRequest.setId(949972L);
        channelLevel3UpdateRequest.setName("官方三级渠道测试-员工晚餐-修改");
        channelLevel3UpdateRequest.setDisplayName("官方三级渠道测试-员工晚餐-修改");
        channelLevel3UpdateRequest.setChannel("mobile.sdkdemo.three_channel_test_dinner");
        channelLevel3UpdateRequest.setDescription("三级渠道描述-修改");
        channelLevel3UpdateRequest.setStatus(1);

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
                .withCheckoutAnnouncementTip("公告标题-修改", "公告的具体文案内容-修改")
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
                .withShopCallbackFlavor(Arrays.asList("209","3232","3240","212","3600","3592","3608","3344","3352","3616","3016","3248","3256","3264","222","223","224","225","226","227","228","3272","3624","3632","3312","3496","236","240","3640","3512","242","249","3472","250","263","3008","266","267","3648","269","354","362","370","378","386","394","3656","410","418","426","3664","442","3672","458","3304","474","482","490","498","506","514","522","530","538","546","554","562","570","3680","586","594","610","642","682","714","3688","3480","3696","3416","3424","3432","3440","3448","3456","3464","3488","746","3704","3088","3096","3104","3112","3120","3128","3136","3144","3152","3160","3168","3176","3184","3192","3200","3208","762","3032","3040","3048","3056","3064","3072","3080","3712","3720","786","794","802","810","818","826","834","842","850","858","866","3728","3736","3024","3216","3224","3280","3288","3296","3504","3744","906","3752","3760","962","3768","978","3328","3336","3776","3784","3792","1034","3800","3808","1066","1074","1082","1090","1098","1106","3816","3576","3824","3832","3840","3552","3848","3560","3568","3584","1170","1178","1186","1194","3520","1202","1210","1218","1226","3856","3528","3536","3544","1250","1258","3864","1274","1282","3368","3384","3392","1290","1298","3872","3880","1322","1330","1338","1346","1354","1362","3360","3376","3400","3408","3888","3320","1555","3896","3000","1635","214","218","221","232","234","241","268","402","434","450","578","730","738","754","770","778","874","882","890","914","930","970","986","1002","1018","1042","1058","1114","1122","1130","1138","1154","1234","1266","1306","1314","1474","1563"))
                // 8. 店铺展示规则（隐藏商品分类）
                .withHideItemCategories(Arrays.asList("201839304", "201835105"))
                .build();

        BusinessParamModel businessParam = new BusinessParamModel();
        businessParam.setBusinessParams(businessParams);
        channelLevel3UpdateRequest.setBusinessParam(businessParam);

        channelLevel3UpdateRequests.add(channelLevel3UpdateRequest);
        channelLevel3BatchUpdateRequest.setChannelLevel3UpdateRequests(channelLevel3UpdateRequests);

        try {
            // 打印完整请求参数
            System.out.println("完整请求: " + JSON.toJSONString(channelLevel3BatchUpdateRequest));

            ChannelLevel3BatchResponse response = allianceService.updateChannelLevel3(channelLevel3BatchUpdateRequest);
            System.out.println("完整响应: " + JSON.toJSONString(response));

            if (response != null && response.getResult() != null) {
                System.out.println("--------------------------------------------------");
                System.out.println("更新成功！");
                System.out.println("更新的三级渠道ID: " + channelLevel3UpdateRequest.getId());
                System.out.println("更新的渠道名称: " + channelLevel3UpdateRequest.getName());
                System.out.println("更新的三级渠道号: " + channelLevel3UpdateRequest.getChannel());
                System.out.println("--------------------------------------------------");
            } else {
                System.out.println("更新失败！");
            }
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}

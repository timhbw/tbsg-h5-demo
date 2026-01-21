package com.tbsg.h5.demo.openapi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务参数构建器（BusinessParamBuilder）
 *
 * 将简单的业务字段转换为饿了么开放平台 SDK 需要的复杂 JSON 格式。
 * 使用链式调用方式，可根据业务需求选择性配置参数。
 *
 * <p>支持的业务参数（按开放平台文档顺序）：</p>
 *
 * <h3>1. 提单相关配置</h3>
 * <ul>
 *   <li>1.1 配送地址类型 (useEnterpriseAddress) - 设置用户可选的配送地址类型</li>
 *   <li>1.2 企业地址详情 (enterpriseAddressDetail) - 配置企业固定收货地址列表</li>
 *   <li>1.3 提单公告 (checkoutAnnouncementTip) - 结算页顶部公告配置</li>
 *   <li>1.4 预定单过滤 (bookTimeTimeFilter) - 是否支持预定单</li>
 *   <li>1.5 屏蔽到店自提 (hide_take_by_self) - 是否隐藏到店自提选项</li>
 *   <li>1.6 用餐时间限制 (orderTimeLimit) - 按周配置可下单时间段</li>
 * </ul>
 *
 * <h3>2. 搜推召回规则</h3>
 * <ul>
 *   <li>首页店铺品类召回 (shopCallbackFlavor) - 配置首页店铺品类召回规则</li>
 * </ul>
 *
 * <h3>3. 店铺展示规则</h3>
 * <ul>
 *   <li>隐藏店铺商品类目 (hideItemCategories) - 隐藏指定的商品分类</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * Map<String, String> businessParams = new BusinessParamBuilder()
 *     .withUseEnterpriseAddress("enterprise")
 *     .withEnterpriseAddressDetail(addressList)
 *     .withCheckoutAnnouncementTip("公告标题", "公告内容")
 *     .withBookTimeTimeFilter(true)
 *     .withHideTakeBySelf(true)
 *     .withOrderTimeLimit(weekTimeConfig)
 *     .withShopCallbackFlavor(flavorIds)
 *     .withHideItemCategories(categoryIds)
 *     .build();
 * }</pre>
 *
 * @see <a href="https://open.shop.ele.me/base/documents/openh5">H5渠道标准版集成场景(仅定向合作)</a>
 */
public class BusinessParamBuilder {

    private final Map<String, String> businessParams = new HashMap<>();

    // ==================== 1. 提单相关配置 ====================

    /**
     * 1.1 配送地址类型 (useEnterpriseAddress)
     *
     * <p>设置用户可选的配送地址类型，控制用户在下单时能够选择的地址范围。</p>
     *
     * <p>参数说明：</p>
     * <ul>
     *   <li>enterprise - 企业地址：用户只能选择预设的企业地址（需配合 enterpriseAddressDetail 使用）</li>
     *   <li>personal - 个人地址：用户可以选择自己的收货地址</li>
     * </ul>
     *
     * <p>报文示例：</p>
     * <pre>
     * "useEnterpriseAddress": "{\"config\":\"{\\\"type\\\":\\\"enterprise\\\"}\",\"functionCode\":\"useEnterpriseAddress\"}"
     * </pre>
     *
     * @param type 地址类型枚举值：enterprise (企业地址) 或 personal (个人地址)
     * @return this
     */
    public BusinessParamBuilder withUseEnterpriseAddress(String type) {
        JSONObject innerConfig = new JSONObject();
        innerConfig.put("type", type);

        JSONObject outer = new JSONObject();
        outer.put("config", innerConfig.toJSONString());
        outer.put("functionCode", "useEnterpriseAddress");

        businessParams.put("useEnterpriseAddress", outer.toJSONString());
        return this;
    }

    /**
     * 1.2 企业地址详情 (enterpriseAddressDetail)
     *
     * <p>配置企业固定收货地址列表。当 useEnterpriseAddress 设置为 "enterprise" 时必填。</p>
     * <p>支持配置多个地址，用户下单时可从中选择。</p>
     *
     * <p>地址字段说明：</p>
     * <ul>
     *   <li>addressId - 地址唯一标识</li>
     *   <li>address - 地址（如：上海市浦东新区）</li>
     *   <li>addressDetail - 详细地址（如：近铁城市广场北区）</li>
     *   <li>addressTag - 地址标签</li>
     *   <li>latitude - 纬度</li>
     *   <li>longitude - 经度</li>
     *   <li>status - 状态（1: 启用）</li>
     *   <li>addressCheckNotice - 地址确认提示（可选）</li>
     *   <li>poiId - POI ID（可选）</li>
     * </ul>
     *
     * <p>报文示例：</p>
     * <pre>
     * "enterpriseAddressDetail": "{\"config\":\"{\\\"addressList\\\":[{\\\"addressId\\\":\\\"1001\\\",\\\"address\\\":\\\"上海市浦东新区\\\",\\\"addressDetail\\\":\\\"近铁城市广场北区\\\",\\\"addressTag\\\":\\\"3\\\",\\\"latitude\\\":\\\"31.23084\\\",\\\"longitude\\\":\\\"121.412851\\\",\\\"status\\\":\\\"1\\\",\\\"addressCheckNotice\\\":\\\"请确认收货人\\\",\\\"poiId\\\":\\\"B0LAYUW7H2\\\"}]}\",\"functionCode\":\"enterpriseAddressDetail\"}"
     * </pre>
     *
     * @param addressList 企业地址列表
     * @return this
     */
    public BusinessParamBuilder withEnterpriseAddressDetail(List<EnterpriseAddress> addressList) {
        JSONArray addressArray = new JSONArray();
        for (EnterpriseAddress addr : addressList) {
            JSONObject addrObj = new JSONObject();
            addrObj.put("addressId", addr.getAddressId());
            addrObj.put("address", addr.getAddress());
            addrObj.put("addressDetail", addr.getAddressDetail());
            addrObj.put("addressTag", addr.getAddressTag());
            addrObj.put("latitude", addr.getLatitude());
            addrObj.put("longitude", addr.getLongitude());
            addrObj.put("status", addr.getStatus());
            if (addr.getAddressCheckNotice() != null) {
                addrObj.put("addressCheckNotice", addr.getAddressCheckNotice());
            }
            if (addr.getPoiId() != null) {
                addrObj.put("poiId", addr.getPoiId());
            }
            addressArray.add(addrObj);
        }

        JSONObject innerConfig = new JSONObject();
        innerConfig.put("addressList", addressArray);

        JSONObject outer = new JSONObject();
        outer.put("config", innerConfig.toJSONString());
        outer.put("functionCode", "enterpriseAddressDetail");

        businessParams.put("enterpriseAddressDetail", outer.toJSONString());
        return this;
    }

    /**
     * 1.3 提单公告 (checkoutAnnouncementTip)
     *
     * <p>在用户提单页顶部展示的公告横幅，用于向用户展示重要提示。</p>
     * <p>公告将以"标题:内容"的格式展示。</p>
     *
     * <p>核心参数：</p>
     * <ul>
     *   <li>announcementNoticeTip - 公告的标题</li>
     *   <li>checkoutAnnouncementTip - 公告的具体文案内容</li>
     * </ul>
     *
     * <p>报文示例：</p>
     * <pre>
     * "checkoutAnnouncementTip": "{\"config\":\"{\\\"checkoutAnnouncementTip\\\":\\\"温馨提示:请适量点餐,避免浪费\\\"}\",\"functionCode\":\"checkoutAnnouncementTip\"}"
     * </pre>
     *
     * @param title   公告标题（announcementNoticeTip）
     * @param content 公告文案内容（checkoutAnnouncementTip）
     * @return this
     */
    public BusinessParamBuilder withCheckoutAnnouncementTip(String title, String content) {
        JSONObject innerConfig = new JSONObject();
        innerConfig.put("checkoutAnnouncementTip", title + ":" + content);

        JSONObject outer = new JSONObject();
        outer.put("config", innerConfig.toJSONString());
        outer.put("functionCode", "checkoutAnnouncementTip");

        businessParams.put("checkoutAnnouncementTip", outer.toJSONString());
        return this;
    }

    /**
     * 1.4 预定单过滤 (bookTimeTimeFilter)
     *
     * <p>是否拦截/不支持预定单（即只支持立即送达）。</p>
     *
     * <p>核心参数：</p>
     * <ul>
     *   <li>enabled: true - 过滤/不支持预定（只支持立即送达）</li>
     *   <li>enabled: false - 支持预定单功能</li>
     * </ul>
     *
     * <p>报文示例：</p>
     * <pre>
     * "bookTimeTimeFilter": "{\"functionCode\":\"bookTimeTimeFilter\",\"config\":\"{\\\"enabled\\\":\\\"true\\\"}\"}"
     * </pre>
     *
     * @param enabled true: 过滤/不支持预定（只支持立即送达）, false: 支持预定
     * @return this
     */
    public BusinessParamBuilder withBookTimeTimeFilter(boolean enabled) {
        JSONObject innerConfig = new JSONObject();
        innerConfig.put("enabled", String.valueOf(enabled));

        JSONObject outer = new JSONObject();
        outer.put("functionCode", "bookTimeTimeFilter");
        outer.put("config", innerConfig.toJSONString());

        businessParams.put("bookTimeTimeFilter", outer.toJSONString());
        return this;
    }

    /**
     * 1.5 屏蔽到店自提 (hide_take_by_self)
     *
     * <p>是否隐藏"到店自提"选项卡。</p>
     *
     * <p>核心参数：</p>
     * <ul>
     *   <li>enabled: true - 隐藏/屏蔽到店自提选项</li>
     *   <li>enabled: false - 展示到店自提选项</li>
     * </ul>
     *
     * <p>报文示例：</p>
     * <pre>
     * "hide_take_by_self": "{\"functionCode\":\"hide_take_by_self\",\"config\":\"{\\\"enabled\\\":\\\"true\\\"}\"}"
     * </pre>
     *
     * @param enabled true: 隐藏/屏蔽, false: 展示
     * @return this
     */
    public BusinessParamBuilder withHideTakeBySelf(boolean enabled) {
        JSONObject innerConfig = new JSONObject();
        innerConfig.put("enabled", String.valueOf(enabled));

        JSONObject outer = new JSONObject();
        outer.put("functionCode", "hide_take_by_self");
        outer.put("config", innerConfig.toJSONString());

        businessParams.put("hide_take_by_self", outer.toJSONString());
        return this;
    }

    /**
     * 1.6 用餐时间限制 (orderTimeLimit)
     *
     * <p>按周配置可下单的时间段，限制用户只能在指定时间段内下单。</p>
     * <p>支持每天配置多个时间段。</p>
     *
     * <p>配置说明：</p>
     * <ul>
     *   <li>key - 英文星期全称：monday, tuesday, wednesday, thursday, friday, saturday, sunday</li>
     *   <li>value - 时间段列表，每个时间段为 String[2]，格式为 {"开始时间", "结束时间"}，如 {"12:00", "13:30"}</li>
     * </ul>
     *
     * <p>报文示例（配置周一至周日 12:00-13:30）：</p>
     * <pre>
     * "orderTimeLimit": "{\"config\":\"{\\\"custom\\\":{\\\"monday\\\":[[\\\"12:00\\\",\\\"13:30\\\"]],\\\"tuesday\\\":[[\\\"12:00\\\",\\\"13:30\\\"]],\\\"wednesday\\\":[[\\\"12:00\\\",\\\"13:30\\\"]],\\\"thursday\\\":[[\\\"12:00\\\",\\\"13:30\\\"]],\\\"friday\\\":[[\\\"12:00\\\",\\\"13:30\\\"]],\\\"saturday\\\":[[\\\"12:00\\\",\\\"13:30\\\"]],\\\"sunday\\\":[[\\\"12:00\\\",\\\"13:30\\\"]]}}\",\"functionCode\":\"orderTimeLimit\"}"
     * </pre>
     *
     * @param weekTimeConfig 周时间配置，key为星期（monday, tuesday...），value为时间段列表
     * @return this
     */
    public BusinessParamBuilder withOrderTimeLimit(Map<String, List<String[]>> weekTimeConfig) {
        JSONObject customConfig = new JSONObject();
        for (Map.Entry<String, List<String[]>> entry : weekTimeConfig.entrySet()) {
            JSONArray dayTimeSlots = new JSONArray();
            for (String[] timeSlot : entry.getValue()) {
                JSONArray slot = new JSONArray();
                slot.add(timeSlot[0]); // 开始时间
                slot.add(timeSlot[1]); // 结束时间
                dayTimeSlots.add(slot);
            }
            customConfig.put(entry.getKey(), dayTimeSlots);
        }

        JSONObject innerConfig = new JSONObject();
        innerConfig.put("custom", customConfig);

        JSONObject outer = new JSONObject();
        outer.put("config", innerConfig.toJSONString());
        outer.put("functionCode", "orderTimeLimit");

        businessParams.put("orderTimeLimit", outer.toJSONString());
        return this;
    }

    // ==================== 2. 搜推召回规则 ====================

    /**
     * 2.1 首页店铺品类召回 (shopCallbackFlavor)
     *
     * <p><b>白名单机制</b>：仅召回列表中指定的行业二级类目 ID，餐饮、零售都可以配置。</p>
     * <p>类目 ID 列表文件请联系淘宝闪购渠道经理获取。</p>
     *
     * <p>报文示例：</p>
     * <pre>
     * "shopCallbackFlavor": "{\"config\":\"{\\\"shop_flavor\\\":[\\\"1066\\\",\\\"1186\\\",\\\"746\\\"]}\",\"functionCode\":\"shopCallbackFlavor\"}"
     * </pre>
     *
     * @param flavorIds 行业二级类目 ID 列表
     * @return this
     */
    public BusinessParamBuilder withShopCallbackFlavor(List<String> flavorIds) {
        JSONObject innerConfig = new JSONObject();
        innerConfig.put("shop_flavor", flavorIds);

        JSONObject outer = new JSONObject();
        outer.put("config", innerConfig.toJSONString());
        outer.put("functionCode", "shopCallbackFlavor");

        businessParams.put("shopCallbackFlavor", outer.toJSONString());
        return this;
    }

    // ==================== 3. 店铺展示规则 ====================

    /**
     * 3.1 隐藏店铺商品类目 (hideItemCategories)
     *
     * <p><b>黑名单机制</b>：在店铺页内隐藏指定的商品类目 ID，只要配置了对应的类目ID，店铺不展示对应品类。</p>
     * <p>类目 ID 列表文件请联系淘宝闪购渠道经理获取。</p>
     *
     * <p>报文示例：</p>
     * <pre>
     * "hideItemCategories": "{\"config\":\"{\\\"hideItem_categories\\\":[\\\"201839304\\\",\\\"201835105\\\"]}\",\"functionCode\":\"hideItemCategories\"}"
     * </pre>
     *
     * @param categoryIds 要隐藏的商品类目 ID 列表
     * @return this
     */
    public BusinessParamBuilder withHideItemCategories(List<String> categoryIds) {
        JSONObject innerConfig = new JSONObject();
        innerConfig.put("hideItem_categories", categoryIds);

        JSONObject outer = new JSONObject();
        outer.put("config", innerConfig.toJSONString());
        outer.put("functionCode", "hideItemCategories");

        businessParams.put("hideItemCategories", outer.toJSONString());
        return this;
    }

    // ==================== 构建方法 ====================

    /**
     * 构建最终的业务参数Map
     *
     * <p>将所有已配置的业务参数构建为 Map，用于设置到 BusinessParamModel 中。</p>
     *
     * @return 业务参数Map，key为参数名，value为JSON格式的参数值
     */
    public Map<String, String> build() {
        return new HashMap<>(businessParams);
    }

    // ==================== 内部实体类 ====================
    /**
     * 企业地址实体类
     *
     * <p>用于配置企业固定收货地址，配合 withEnterpriseAddressDetail 方法使用。</p>
     *
     * <p>字段说明（按官方文档）：</p>
     * <ul>
     *   <li>addressId - 地址唯一标识，由机构自定义（建议从1开始），不可重复（必填）</li>
     *   <li>address - 省市区信息，如"上海市浦东新区"（必填）</li>
     *   <li>addressDetail - 详细地址，不含省市区，如"近铁城市广场北区"（必填）</li>
     *   <li>latitude - 高德坐标系纬度（必填）</li>
     *   <li>longitude - 高德坐标系经度（必填）</li>
     *   <li>poiId - 高德地图 POI ID（必填）</li>
     *   <li>status - 状态标识：1-默认收货地址，0-非默认（必填）</li>
     *   <li>addressTag - 地址标签，固定传 3（可选）</li>
     *   <li>addressCheckNotice - 地址提示文案，C端用户可见（可选）</li>
     * </ul>
     */
    @Setter
    @Getter
    public static class EnterpriseAddress {
        /** 地址唯一标识，由机构自定义（建议从1开始），不可重复 */
        private String addressId;
        /** 省市区信息（如：上海市浦东新区） */
        private String address;
        /** 详细地址，不含省市区（如：近铁城市广场北区） */
        private String addressDetail;
        /** 地址标签，固定传 3 */
        private String addressTag;
        /** 高德坐标系纬度 */
        private String latitude;
        /** 高德坐标系经度 */
        private String longitude;
        /** 状态标识：1-默认收货地址，0-非默认 */
        private String status;
        /** 地址提示文案，C端用户可见（可选） */
        private String addressCheckNotice;
        /** 高德地图 POI ID */
        private String poiId;

        /**
         * 带必填参数的构造函数
         *
         * @param addressId     地址唯一标识，由机构自定义（建议从1开始），不可重复
         * @param address       省市区信息（如：上海市浦东新区）
         * @param addressDetail 详细地址，不含省市区（如：近铁城市广场北区）
         * @param addressTag    地址标签，固定传 3
         * @param latitude      高德坐标系纬度
         * @param longitude     高德坐标系经度
         * @param status        状态标识：1-默认收货地址，0-非默认
         */
        public EnterpriseAddress(String addressId, String address, String addressDetail,
                                  String addressTag, String latitude, String longitude, String status) {
            this.addressId = addressId;
            this.address = address;
            this.addressDetail = addressDetail;
            this.addressTag = addressTag;
            this.latitude = latitude;
            this.longitude = longitude;
            this.status = status;
        }

        // ==================== Getters and Setters ====================

    }
}

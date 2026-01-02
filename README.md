# 淘宝闪购 H5 开放平台接入示例 (Java 版) - 完整指南

> 这是一个完整的淘宝闪购 H5 支付渠道接入示例项目，基于 **eleme-openapi-h5-sdk** 开发。本文档旨在帮助 ISV 从零开始，快速理解业务流程、掌握配置细节并完成接入。

**部分逻辑不严谨，仅供参考，不可直接用于线上环境！**

---

##  目录

1.  [项目简介与特性](#项目简介与特性)
2.  [技术栈与项目结构](#技术栈与项目结构)
3.  [准备工作](#准备工作)
4.  [快速上手](#快速上手)
5.  [配置文件深度解析](#配置文件深度解析)
6.  [核心业务逻辑揭秘](#核心业务逻辑揭秘)
7.  [SDK 集成重点](#sdk-集成重点)
8.  [API 接口详解与示例](#api-接口详解与示例)
9.  [数据库设计](#数据库设计)
10. [测试指南](#测试指南)
11. [常见问题排查](#常见问题排查)

---

##  项目简介与特性

本项目展示了如何接入淘宝闪购 H5 标准版，涵盖了从用户登录、支付下单、支付回调、退款处理到账单下载的完整业务流程。

> **在线体验**：已部署演示环境，ISV 可直接访问测试：[https://tbsgh5.huangbowei.com/](https://tbsgh5.huangbowei.com/)

###  适用场景
- 第一次接入淘宝闪购支付的 ISV 开发者
- 需要了解完整支付流程的技术人员
- 需要参考代码实现的开发团队

###  核心特性
- **完整的支付流程**：支付下单 → 收银台 → 支付回调 → 订单查询
- **退款功能**：支持全额退款和部分退款
- **账单下载**：支持 **懒加载 + OSS 缓存** 策略，自动生成符合官方规范的 CSV 账单文件
- **SDK 自动签名**：继承 SDK 基类，自动处理复杂的签名与验签
- **数据持久化**：使用 MySQL 存储完整订单数据
- **详细注释**：每个类和方法都有完整的中文注释

---

##  技术栈与项目结构

### 技术栈
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8+ | 编程语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| MyBatis Plus | 3.5.3.1 | ORM 框架 |
| MySQL | 8.0+ | 数据库 |
| **eleme-openapi-h5-sdk** | **1.0.15-RELEASE** | **淘宝闪购官方 SDK** |
| Apache Commons CSV | 1.11.0 | 账单 CSV 生成 |
| Aliyun OSS SDK | 3.17.4 | 账单文件存储 |

###  项目结构
```
demo/
├── src/main/
│   ├── java/com/tbsg/h5/demo/
│   │   ├── controller/          # 控制器层 (API 接口)
│   │   │   ├── TbsgPayController.java        # 核心支付/退款/账单接口
│   │   │   ├── TbsgLoginController.java      # 登录接口
│   │   │   ├── TbsgCallbackController.java   # 回调处理
│   │   │   └── CashierPayController.java     # 收银台控制器
│   │   ├── service/             # 服务层
│   │   │   ├── PaymentService.java           # 支付核心逻辑
│   │   │   ├── BillService.java              # 账单生成与 OSS 上传逻辑
│   │   │   └── LoginService.java             # 登录逻辑
│   │   ├── entity/              # 数据库实体
│   │   │   ├── PaymentOrder.java             # 支付订单
│   │   │   ├── RefundOrder.java              # 退款订单
│   │   │   └── BillRecord.java               # 账单记录
│   │   ├── mapper/              # 数据访问层
│   │   └── enums/               # 枚举类
│   └── resources/
│       ├── application.yml      # 核心配置文件
│       ├── db/schema.sql        # 数据库建表脚本
│       └── static/              # 静态资源 (HTML/CSS/JS)
│           ├── index.html                 # 首页
│           └── cashier/
│               ├── pay.html               # 收银台支付页面
│               └── redirect.html          # 支付成功跳转页
└── pom.xml                      # Maven 依赖
```

---

## 准备工作

在开始之前，你需要填写完毕钉群内的 **入驻表格**，入驻完毕后可以在保障宝内获取以下关键信息（配置时必用）：

| 信息名称 | 变量名 (配置中) | 说明                            |
|---|---|-------------------------------|
| **支付渠道编码** | `code` | 你的商户身份标识，例如 `SDKDEMO_OPENPAY` |
| **平台公钥** | `platformPublicKey` | 淘宝闪购给你的公钥，用于验证他们发来的消息         |
| **商户私钥** | `merchantPrivateKey` | 你生成的私钥，用于给你的消息签名              |
| **站点编码** | `openSiteSourceCode` | 用于免登接口                        |
| **消费者密钥** | `consumerSecret` | 用于生成登录 JWT              |

---

##  快速上手

### 1. 数据库初始化
执行 `src/main/resources/db/schema.sql` 脚本，创建 `payment_order`、`refund_order` 和 `bill_record` 表。

### 2. 修改配置文件
- 复制 `src/main/resources/application.example.yml` 为 `src/main/resources/application.yml`，填入你的阿里云 OSS 配置。
- 复制 `src/main/resources/application-dev.example.yml` 为 `src/main/resources/application-dev.yml`，填入你的测试环境信息：本地数据库账号密码和保障宝内获取的测试淘宝闪购密钥参数。
- 复制 `src/main/resources/application-prod.example.yml` 为 `src/main/resources/application-prod.yml`，填入你的正式环境后信息：数据库账号密码和保障宝内获取的正式淘宝闪购密钥参数。

### 3. 启动项目
运行 `DemoApplication.java`，访问 http://localhost:8099/api/index.html  即可看到演示首页。

> **提示**：
> *   **本地测试**：项目配置了 `context-path: /api`，且默认端口为 **8099**，请访问 http://localhost:8099/api/index.html 。
> *   **服务器部署**：建议将 `src/main/resources/static` 下的静态资源直接部署到 Web 服务器（如 Nginx）的根目录下，无需 `/api` 前缀。

---

## 配置文件深度解析

`application.yml` 中的每一项配置都至关重要。为了防止配错，请仔细阅读下表：

### 1. 支付配置 (`tbsg.pay`)

| 配置项 | 含义 | 来源                  | 代码影响 (配错后果) |
|---|---|---------------------|---|
| `code` | **商户支付编码**，你的唯一身份标识 | **钉群内填写入驻表格后保障宝查看** | SDK 无法识别商户身份，所有接口调用失败。 |
| `platformPublicKey` | **平台公钥**，用于验签 | **钉群内填写入驻表格后保障宝查看** | 无法验证淘宝闪购发来的回调，导致回调处理失败。 |
| `merchantPrivateKey` | **商户私钥**，用于加签 | **本地自行生成**          | 发出的请求签名无效，被淘宝闪购拒绝。 |
| `cashierUrl` | **收银台页面地址** | 自定义 (本项目提供)         | 用户下单后无法跳转到支付页面。 |
| `intermediateUrl` | **中间页地址** | 自定义 (本项目提供)         | 支付成功后无法正确跳转回商户页面。 |

### 2. 登录配置 (`tbsg.login`)

| 配置项 | 含义 | 来源 | 代码影响 |
|---|---|---|---|
| `openSiteSourceCode` | **站点来源码** | **钉群内填写入驻表格后保障宝查看** | 生成的 H5 链接无效。 |
| `consumerSecret` | **消费者密钥** | **钉群内填写入驻表格后保障宝查看** | 无法生成有效的登录 Token，用户无法登录。 |
| `allowedMobiles` | **白名单手机号** | 自定义 | 测试环境下，非白名单用户无法登录。 |

### 3. 账单与 OSS 配置 (`tbsg.bill` & `aliyun.oss`)

| 配置项 | 含义 | 来源 | 代码影响 |
|---|---|---|---|
| `storage-path` | 本地临时文件存储路径 | 自定义 | 无法生成 CSV 文件，账单下载失败。 |
| `endpoint` | OSS 服务地址 | **阿里云控制台** | 无法连接 OSS。 |
| `accessKeyId` | OSS 访问 Key | **阿里云控制台** | 无法认证 OSS。 |
| `bucketName` | OSS 存储桶名称 | **阿里云控制台** | 无法上传/下载文件。 |

---

## 核心业务逻辑揭秘

### 1. 支付与回调流程
*   **支付流程**: 用户下单 -> 淘宝闪购请求机构 `payUrl/pay` -> 机构调用 SDK (`pay`) -> SDK 自动计算签名 -> 返回收银台 URL -> 前端跳转至淘宝闪购收银台。
*   **回调流程**: 用户支付成功 -> 商户 POST 通知淘宝闪购 `notifyUrl` -> SDK 自动验签 (`payCallback`) -> SDK 返回标准 JSON 响应。

### 2. 账单下载策略
账单下载不仅仅是“查库-生成文件”那么简单。为了提高性能并节省资源，本项目采用了 **“懒加载 + OSS 缓存”** 的策略。

**逻辑流程**:
1.  **请求到达**: 用户请求下载某天的账单。
2.  **检查 OSS**: 系统首先去阿里云 OSS 检查，`bills/{payCode}_bill_{date}.csv` 是否已经存在。
    *   **如果存在**: 直接跳到第 5 步。
    *   **如果不存在**: 进入第 3 步。
3.  **生成 CSV**:
    *   查询数据库 `bill_record` 表。
    *   **过滤逻辑**: 剔除同一天内“全额支付”和“全额退款”的配对记录（无效交易）。
    *   生成符合规范的 17 字段 CSV 文件，暂存本地。
4.  **上传 OSS**: 将本地 CSV 上传到 OSS，然后删除本地临时文件。
5.  **获取 URL**: 调用 OSS 接口生成一个 **预签名 URL (Presigned URL)**，有效期 1 小时。
6.  **返回**: 将这个 URL 返回给前端，用户直接从 OSS 下载。

> **为什么这么做？**
> *   **性能**: 只有第一个人下载时需要生成，后续下载直接走 OSS，速度极快。
> *   **安全**: 预签名 URL 是临时的，且不需要暴露 OSS 的读写权限。

---

## SDK 集成重点

我们提供了 `eleme-openapi-h5-sdk` 来简化开发。

### 1. 引入依赖
```xml
<dependency>
    <groupId>me.ele.openapi</groupId>
    <artifactId>eleme-openapi-h5-sdk</artifactId>
    <version>1.0.15-RELEASE</version>
</dependency>
```

### 2. 继承 BasePayService
这是 SDK 的核心魔法。你只需要继承 `BasePayService`，SDK 就会自动帮你处理所有脏活累活（签名、验签、参数解析）。

```java
// 你的 Controller 继承 BasePayService
public class TbsgPayController extends BasePayService {
    // 必须实现：提供平台公钥
    @Override
    public String getElemePublicKey() { return platformPublicKey; }

    // 必须实现：提供商户私钥
    @Override
    public String getPrivateKey() { return merchantPrivateKey; }
}
```

### 3. 自动签名与验签 (重点！)
> **注意**：很多 ISV 经常问：为什么接口参数里没有 sign 和 nonceStr？

**请注意：使用了 SDK 后，你完全不需要手动计算签名！**

*   **发送请求时**：SDK 会自动生成 `nonceStr`（随机字符串）和 `timestamp`，并使用你的私钥计算 `sign`，然后自动填充到请求头或参数中。
*   **接收响应时**：SDK 会自动使用平台公钥验证响应的 `sign`，如果验签失败会直接抛出异常。

**你只需要关注业务参数（如 `transactionId`, `payAmount`），剩下的交给 SDK。**

### 4. Context 模式与枚举
使用 Context 对象简化参数传递，并使用 SDK 官方枚举类：
```java
// 支付状态（SDK 官方）
import eleme.openapi.h5.sdk.pay.enums.PayStatus;
String status = PayStatus.SUCCESS.getCode();  // "SUCCESS"
```

---

##  API 接口详解与示例

> **注意**：以下所有接口的响应 JSON 中，SDK 都会自动添加 `sign` (签名) 和 `nonceStr` (随机串) 字段。为了简洁，部分示例可能未展示，但实际返回中**必须包含**。

以下是 5 大核心接口的详细说明，包含了代码级解析和 HTTP 请求示例。

### 1. 支付接口 (`pay`)
*   **功能**: 接收支付请求，创建订单，返回收银台跳转地址。
*   **代码流程**: SDK 验签 -> 幂等检查 -> 创建订单(NOTPAY) -> 构建收银台 URL -> 重定向。
*   **请求示例**:
    ```http
    POST /api/pay
    Content-Type: application/x-www-form-urlencoded

    {
    "redirectUrl": "https://h5.ele.me/2021001185671035/pages/ele-order-detail-tb/ele-order-detail-tb?orderId=&taobaoId=8012666021463392111&eosOrderId=8012666021463392111&from=mobile.xxxxxx&welfare_3pp=XXXXXX_OPENPAY&opensite_source=xxxxxx&jwt=eyJ0eXAiOiJKVXXXXJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlbGUubWUiLCJvcGVuX2lkIjoiXXXXXXwibW9iaWxlIjoiMTE0MDUxOTY0MTEiLCJzb3VyY2UiOiJobmNhcHAiLCJleHAiOjE3NDU1NTA3NzIsImlhdCI6MTc0NDY4Njc3MiwiZXh0SW5mbyI6IiJ9.zynEjXXX24Zte8t7SWyYjHjiZy6bCRQX0Mfrvb60",
    "backUrl": "https://h5.ele.me/2021001185671035/pages/ele-order-detail-tb/ele-order-detail-tb?orderId=&taobaoId=8012666021463392111&eosOrderId=8012666021463392111&from=mobile.xxxxxx&welfare_3pp=XXXXXX_OPENPAY&opensite_source=xxxxxx&jwt=eyJ0eXAiOiJXXXCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlbGUubWUiLCJvcGVuX2lkIjoiMjA4IiwibW9iaWxlIjoiMTE0MDUxOTY0MTEiLCJzb3VyY2UiOiJobmNhcHAiLCJlXXXE7NDU1NTUwNzc0MSwiImF0dHIiOjE3NDQ2ODY3NzMsImlhdCI6MTc0NDY4Njc3MywiZXh0SW5mbyI6IiJ9.zynEjuxUxXXX8t7SWyYjHjiZy6bCRQX0Mfrvb60",
    "subject": "淘宝闪购测试店铺", 
    "body": "淘宝闪购测试店铺", 
    "transactionId": "13110600725041515973707656111",
    "timeExpire": "20250415112856",
    "extendParams": "{\"orderType\":\"1\",\"alscChannel2\":\"mobile.hncapp\",\"orderId\":\"8012666021463392111\",\"alscChannel3\":\"mobile.xxxxxx.scheme_bf34178265cc4f77929036b6ac3212bc\",\"payCode\":\"XXXXXX_OPENPAY\"}",
    "uid": "208",
    "payAmount": 7400,
    "notifyUrl": "https://finnet-alsc.ele.me/callback/v1/pp3-PG.MID.ele.takeout-ele.openapi-create/PROD-0/1919927656111",
    "timestamp": "20250415111356"
    }
    ```
*   **响应**: HTTP 302 重定向到收银台页面。

### 2. 退款接口 (`refund`)
*   **功能**: 对已支付订单发起退款。
*   **代码流程**: SDK 解析 -> 检查原订单 -> 校验金额 -> 创建退款单(SUCCESS) -> 记录账单 -> 返回响应。
*   **请求示例**:
    ```http
    POST /api/refund
    Content-Type: application/json

    {
      "refundNo": "REFUND20231230001",
      "transactionId": "20231230001",
      "refundAmount": 500
    }
    ```
*   **响应示例**:
    ```json
    {
      "refundNo": "REFUND20231230001",
      "refundStatus": "SUCCESS",
      "returnCode": "SUCCESS",
      "sign": "SDK_AUTO_GENERATED_SIGN",
      "nonceStr": "SDK_AUTO_GENERATED_NONCE"
    }
    ```

### 3. 查询支付 (`queryPay`)
*   **功能**: 查询订单的支付状态。
*   **请求示例**:
    ```http
    POST /api/queryPay
    Content-Type: application/json

    { "transactionId": "20231230001" }
    ```
*   **响应示例**:
    ```json
    {
      "payStatus": "SUCCESS",
      "payAmount": 1000,
      "returnCode": "SUCCESS",
      "sign": "SDK_AUTO_GENERATED_SIGN",
      "nonceStr": "SDK_AUTO_GENERATED_NONCE"
    }
    ```

### 4. 查询退款 (`queryRefund`)
*   **功能**: 查询退款单状态。
*   **请求示例**:
    ```http
    POST /api/queryRefund
    Content-Type: application/json

    { "refundNo": "REFUND20231230001" }
    ```
*   **响应示例**:
    ```json
    {
      "refundStatus": "SUCCESS",
      "refundAmount": 500,
      "returnCode": "SUCCESS",
      "sign": "SDK_AUTO_GENERATED_SIGN",
      "nonceStr": "SDK_AUTO_GENERATED_NONCE"
    }
    ```

### 5. 下载账单 (`downloadBill`)
*   **功能**: 获取对账单下载链接。
*   **代码流程**: 校验日期 -> 检查 OSS -> (不存在则生成并上传) -> 返回预签名 URL。
*   **请求示例**:
    ```http
    POST /api/downloadBill
    Content-Type: application/json

    { "billDate": "2023-12-30" }
    ```
*   **响应示例**:
    ```json
    {
      "billUrl": "https://oss-cn-shanghai.aliyuncs.com/bills/...",
      "returnCode": "SUCCESS",
      "sign": "SDK_AUTO_GENERATED_SIGN",
      "nonceStr": "SDK_AUTO_GENERATED_NONCE"
    }
    ```

---

## 数据库设计

本项目包含 3 张核心表，分别用于存储支付订单、退款订单和账单记录。

### 1. 支付订单表 (`payment_order`)
用于存储商户侧的支付请求及状态。

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | BIGINT | 是 | 主键 ID |
| `transaction_id` | VARCHAR(64) | 是 | **商户订单号** (唯一索引)，对应请求中的 `transactionId` |
| `out_trade_no` | VARCHAR(64) | 否 | 三方支付交易号（机构侧生成） |
| `pay_amount` | INT | 是 | 支付金额 (**单位：分**) |
| `subject` | VARCHAR(256) | 否 | 订单标题 |
| `body` | VARCHAR(512) | 否 | 订单详情 |
| `uid` | VARCHAR(64) | 否 | 用户 ID |
| `pay_status` | VARCHAR(20) | 是 | 状态：`PENDING`(待支付), `SUCCESS`(成功), `FAIL`(失败), `CLOSED`(关闭) |
| `notify_url` | VARCHAR(512) | 否 | 异步回调地址 |
| `redirect_url` | VARCHAR(512) | 否 | 同步跳转地址 |
| `success_time` | DATETIME | 否 | 支付成功时间 |

### 2. 退款订单表 (`refund_order`)
用于存储退款请求及状态。

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | BIGINT | 是 | 主键 ID |
| `refund_no` | VARCHAR(64) | 是 | **商户退款单号** (唯一索引)，对应请求中的 `refundNo` |
| `transaction_id` | VARCHAR(64) | 是 | 原支付订单号 |
| `out_refund_no` | VARCHAR(64) | 否 | 三方退款流水号 |
| `pay_amount` | INT | 是 | 原订单金额 (分) |
| `refund_amount` | INT | 是 | 本次退款金额 (分) |
| `refund_status` | VARCHAR(20) | 是 | 状态：`PENDING`(处理中), `SUCCESS`(成功), `FAIL`(失败) |
| `success_time` | DATETIME | 否 | 退款成功时间 |

### 3. 账单记录表 (`bill_record`)
严格按照淘宝闪购账单规范设计，包含 19 个字段，用于生成对账 CSV 文件。

| 字段名 | 说明 | 备注 |
|---|---|---|
| `bill_date` | 账单日期 | yyyy-MM-dd |
| `pay_code` | 支付 code | 商户身份标识 |
| `trans_type` | 交易类型 | `pay` (支付) 或 `refund` (退款) |
| `request_time` | 请求时间 | yyyy-MM-dd HH:mm:ss |
| `success_time` | 成功时间 | yyyy-MM-dd HH:mm:ss |
| `transaction_id` | 商户流水号 | 支付传 transactionId，退款传 refundNo |
| `out_transaction_id` | 渠道流水号 | 机构侧流水号 |
| `trans_status` | 交易状态 | 固定为 `S` (成功) |
| `trans_amount` | 交易金额 | 单位：分 |
| `user_trans_real_amount` | 用户实付 | 通常等于交易金额 |
| `settle_amount` | 结算金额 | 默认 0 |
| `marketing_amount` | 营销金额 | 默认 0 |
| `marketing_type` | 营销类型 | 默认空 |
| `marketing_fee` | 技术服务费 | 默认 0 |
| `origin_transaction_id` | 原支付流水号 | 退款时必填 |
| `rate` | 税率 | 默认 0 |
| `fee` | 手续费 | 默认 0 |
| `create_time` | 创建时间 | 记录入库时间 |

---

## 测试指南

### 使用 Postman 测试
1.  **登录**: 调用 `/api/login` 获取 Token。
2.  **支付**: 调用 `/api/pay`，复制返回的 `cashierUrl` 在浏览器打开，完成支付。
3.  **查询**: 调用 `/api/queryPay` 确认状态变为 `SUCCESS`。
4.  **退款**: 调用 `/api/refund` 进行退款。
5.  **账单**: 调用 `/api/downloadBill` 获取 CSV 下载链接。

### 使用 Curl 命令行测试 (Pay 接口)
你可以直接在终端执行以下命令来测试支付接口（注意：URL 路径为实际 Controller 映射路径）。
> **注意**：这里去掉了 `--location` 参数，因为接口返回 302 重定向，我们只需要验证响应头中的 `Location` 是否正确即可。如果自动跳转，可能会因为用 POST 请求访问静态页面而报 405 错误。

```bash
curl -v --request POST 'http://127.0.0.1:8099/api/tbsg/channel/pay' \
--header 'dnt: 1' \
--header 'pragma: no-cache' \
--header 'priority: u=0, i' \
--header 'sec-fetch-user: ?1' \
--header 'upgrade-insecure-requests: 1' \
--header 'content-type: application/x-www-form-urlencoded' \
--data-urlencode 'redirectUrl=https://h5.ele.me/2021001185671035/pages/ele-order-detail-tb/ele-order-detail-tb?orderId=&taobaoId=8012666117498186214&eosOrderId=8012666117498186214&from=mobile.sdkdemo&welfare_3pp=SDKDEMO_OPENPAY&opensite_source=sdkdemo&jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlbGUubWUiLCJvcGVuX2lkIjoiMjA4ODIzMjI1NjkyNTY3NTExMjM4ODgiLCJtb2JpbGUiOiIxMTQwNTE5NjQxMSIsInNvdXJjZSI6InNka2RlbW8iLCJleHAiOjE3NjcyNDU3MTQsImlhdCI6MTc2Njk4NjUxNH0.0Bzead4nFwF1_eRyvzH0h75uG-nci2qeIkEEiqjs9Wg' \
--data-urlencode 'backUrl=https://h5.ele.me/2021001185671035/pages/ele-order-detail-tb/ele-order-detail-tb?orderId=&taobaoId=8012666117498186214&eosOrderId=8012666117498186214&from=mobile.sdkdemo&welfare_3pp=SDKDEMO_OPENPAY&opensite_source=sdkdemo&jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlbGUubWUiLCJvcGVuX2lkIjoiMjA4ODIzMjI1NjkyNTY3NTExMjM4ODgiLCJtb2JpbGUiOiIxMTQwNTE5NjQxMSIsInNvdXJjZSI6InNka2RlbW8iLCJleHAiOjE3NjcyNDU3MTQsImlhdCI6MTc2Njk4NjUxNH0.0Bzead4nFwF1_eRyvzH0h75uG-nci2qeIkEEiqjs9Wg' \
--data-urlencode 'subject=支付餐饮测试专用(仅供测试)外卖订单' \
--data-urlencode 'body=支付餐饮测试专用(仅供测试)外卖订单' \
--data-urlencode 'nonceStr=SqHcyiwnjilRK9rnraQXt5na7Og7W57T' \
--data-urlencode 'transactionId=13180600725123192081142656612' \
--data-urlencode 'timeExpire=20251231190046' \
--data-urlencode 'extendParams={"orderType":"1","alscChannel2":"mobile.sdkdemo","orderId":"8012666117498186214","alscChannel3":"mobile.sdkdemo.scheme_b574f0dc4cf848079f4cc0123547f120","payCode":"SDKDEMO_OPENPAY"}' \
--data-urlencode 'uid=20882322569256751123888' \
--data-urlencode 'payAmount=2' \
--data-urlencode 'notifyUrl=https://finnet-alsc.ele.me/callback/v1/pp3-PG.MID.ele.takeout-ele.openapi-create/PROD-0/1919927656612' \
--data-urlencode 'timestamp=20251231184545'
```

### 使用 Curl 命令行测试 (PayCallback 通知)

> **官方文档重点说明**：
> *   **回调时机**：只有 **支付成功** 才需要回调。
> *   **异常排查**：如果请求后返回 302 或者验签失败，请检查入参字段或确认订单是否确实支付成功。
> *   **NotifyUrl 来源**：`${payUrl}/pay` 接口入参中会动态生成 `notifyUrl`，每笔订单都需要将此 URL 落库保存，后续通过 HTTP POST 通知到该 `notifyUrl`。
> *   **重试机制**：支付成功后，机构需通知淘宝闪购。为保障推送到达率，**机构需要有重试机制**。如果回调失败，建议重试频率为：`1s, 5s, 5s, 10s, 30s, 1m, 5m, 10m`。

以下是商户后端调用 SDK 的 `payCallback` 方法时，实际向淘宝闪购开放平台发送的 HTTP 请求示例（供调试参考）：

```bash
curl --location --request POST 'http://finnet-alsc.ele.me/callback/v1/pp3-PG.MID.ele.takeout-ele.openapi-create/PROD-0/2215720950231' \
--header 'User-Agent: eleme' \
--header 'Content-Type: application/json; charset=utf-8' \
--header 'Accept: */*' \
--header 'Host: finnet-alsc.ele.me' \
--header 'Connection: keep-alive' \
--data-raw '{
    "nonceStr": "LzrMCTXCbQRWLtmdRcEgmPgQORkOlNRk",
    "outTradeNo": "UC4785273831760520087626",
    "payAmount": "4590",
    "payCode": "XYSHDX_OPENPAY",
    "payStatus": "SUCCESS",
    "sign": "eiztmahISYKNwa+AMuNJ28B8twNlO7kMzrbXduRs/DaAQdDUZWba45Sd8kZD8fPQt+asCsc+x41JHAysrzQjy9KL3A8Sb+n5EYZpypxCy9p+NswZKEk9EJIHVKzH3IafT/8DmHuuQqSeQHol8rswtpWS5okrxlg73S2/WR/0cIURygGtvDgo3KQweITR0p2tX35EsG9xQ79UuhMEMAc+Gr87rIs+wWgx8tD7F1O9mwm/sL9uIYz6TjPJbjdaiXLkV4mvT/4nXqfzd3oWL+2+K6wh1Cx6OYh4DM1U4QZUtdeaWCIskzivW3D4P0LN0EBaEsLJexDBwryWK5wSJWs0Dg==",
    "transactionId": "13170600725101564050888950231"
}'
```

---

## 常见问题排查

### Q1: 接口报错 "Signature verification failed"
**原因**: 公钥或私钥配置错误。
**解决**: 检查 `application.yml` 中的 `platformPublicKey` (验签用) 和 `merchantPrivateKey` (加签用) 是否正确。

### Q2: 账单下载失败
**原因**: OSS 配置错误或本地无写入权限。
**解决**: 检查 `aliyun.oss` 配置，并确保 `tbsg.bill.storage-path` 目录存在且可写。

### Q3: 登录提示 "该账户未授权"
**原因**: 手机号不在白名单中。
**解决**: 在 `application.yml` 的 `tbsg.login.allowedMobiles` 中添加测试手机号。

---

**祝你接入顺利！**
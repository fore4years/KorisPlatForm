# 发电机租赁平台 - 技术开发与维护指南

本文档详细说明了发电机租赁平台的系统架构、运行逻辑、数据库设计、代码规范及配置细节，旨在帮助开发人员快速接入和维护系统。

## 1. 系统概览

本系统是一个基于 Spring Boot 和 Vue 3 的发电机租赁管理平台，支持多角色（租户、商家、管理员）操作，实现了从发电机发布、搜索、租赁预订、在线支付、电子合同签署到设备交付归还的全流程闭环管理。

## 2. 技术栈

### 2.1 后端
*   **核心框架**: Spring Boot 3.x
*   **持久层**: MyBatis Plus 3.5.x
*   **数据库**: MySQL 8.0
*   **缓存/分布式锁**: Redis (Cluster模式)
*   **安全框架**: Spring Security (目前仅作基础配置，主要依赖业务层鉴权)
*   **支付集成**: Alipay SDK (沙箱环境)
*   **工具库**: Lombok, Hutool, Fastjson2

### 2.2 前端
*   **框架**: Vue 3 (Composition API)
*   **构建工具**: Vite
*   **UI 组件库**: Element Plus
*   **HTTP 客户端**: Axios
*   **路由管理**: Vue Router 4

## 3. 命名与代码规范

### 3.1 字符串与命名
*   **Java 类名**: UpperCamelCase (e.g., `OrderController`, `UserServiceImpl`)
*   **方法/变量**: lowerCamelCase (e.g., `createOrder`, `userId`)
*   **常量**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
*   **数据库表名**: snake_case, 复数形式 (e.g., `users`, `orders`, `generators`)
*   **数据库字段**: snake_case (e.g., `user_id`, `created_at`)
*   **API 路径**: kebab-case, 名词复数 (e.g., `/api/orders`, `/api/auth/login`)

### 3.2 统一响应格式
所有 API 均返回统一的 JSON 结构，由 `com.generator.rental.common.Result<T>` 封装：
```json
{
  "code": 200,      // 200: 成功, 其他: 失败
  "msg": "success", // 响应消息
  "data": { ... }   // 业务数据
}
```

## 4. 数据库设计 (MySQL)

### 4.1 用户表 (`users`)
存储系统所有角色的基本信息。
*   `id` (BIGINT, PK): 自增主键。
*   `username` (VARCHAR): 登录账号，唯一。
*   `role` (ENUM): 角色类型 (`TENANT`, `MERCHANT`, `ADMIN`)。
*   `status` (ENUM): 账号状态 (`ACTIVE`, `DISABLED`, `PENDING`)。
*   `pending_info` (TEXT): 存储商家入驻审核时的临时信息(JSON)。
*   **设计注意**: `user_id` 字段为业务逻辑ID，目前逻辑中常与 `id` 混用，建议后续统一使用 `id` 作为外键。

### 4.2 发电机表 (`generators`)
存储发电机设备详情。
*   `merchant_id` (BIGINT, FK): 关联商家。
*   `daily_rent`, `weekly_rent`, `monthly_rent` (DECIMAL): 阶梯租金。
*   `stock_status` (ENUM): 库存状态 (`AVAILABLE`, `RENTED`, `MAINTENANCE`)。
*   `latitude`, `longitude` (DECIMAL): 用于基于位置的搜索和距离计算。
*   `delivery_method` (VARCHAR): 配送方式 (`Seller Delivery`, `Self Pickup`)。

### 4.3 订单表 (`orders`)
核心业务表，记录租赁交易。
*   `tenant_id`, `merchant_id`, `generator_id`: 关联三方。
*   `status` (ENUM): 订单状态机。
    *   `WAIT_PAY`: 待支付（仅在线支付模式）。
    *   `WAIT_CONFIRM`: 待商家确认（线下支付或已支付）。
    *   `CONFIRMED`: 商家已接单，生成合同。
    *   `DELIVERED`: 商家已发货/待取货。
    *   `RENTING`: 租户确认收货，租赁进行中。
    *   `COMPLETED`: 归还并结算完成。
    *   `CANCELLED`: 订单取消。
*   `payment_method` (ENUM): `ONLINE` (支付宝), `OFFLINE` (线下转账)。
*   `payment_status` (ENUM): `PENDING`, `PAID`。
*   `delivery_type`: 实际交付方式。
*   `delivery_evidence_url`: 交付凭证图片。
*   `return_evidence_url`: 归还凭证图片。

### 4.4 合同表 (`contracts`)
*   `order_id` (FK): 关联订单。
*   `content`: 合同文本内容。
*   `tenant_signed`, `merchant_signed` (BOOLEAN): 双方签署状态。

## 5. Redis 使用说明

系统配置了 Redis Cluster 模式，用于缓存和潜在的分布式锁。

### 5.1 配置细节 (`RedisConfig.java`)
*   **序列化**:
    *   Key: `StringRedisSerializer` (便于阅读)
    *   Value: `Jackson2JsonRedisSerializer` (存取对象自动转 JSON)
*   **连接池**: Lettuce 连接池配置。

### 5.2 使用场景
*   **缓存**: 暂无大规模业务缓存，预留给高频查询（如发电机列表）。
*   **Session**: 虽然目前使用了 JWT/Token 机制，但 Redis 可用于存储 Token 黑名单实现注销功能。

## 6. 系统运行与业务逻辑

### 6.1 认证与鉴权
1.  **登录**: `/api/auth/login` 验证账号密码，返回 User 对象（含 Token）。
2.  **请求鉴权**:
    *   前端 `request.js` 拦截器在 Header 中注入 `Authorization` 和 `X-User-ID`。
    *   后端 Controller 通过 `@RequestHeader` 获取用户 ID。
    *   **注意**: `SecurityConfig` 配置为 `permitAll`，实际鉴权逻辑分散在业务层。建议后续引入 Spring Security Filter 统一拦截。

### 6.2 订单全生命周期逻辑
1.  **创建订单**: 校验库存 -> 计算租金 -> 生成订单 -> 状态置为 `WAIT_PAY` 或 `WAIT_CONFIRM`。
2.  **支付 (可选)**: 调用支付宝接口 -> 回调更新 `payment_status` -> 状态流转至 `WAIT_CONFIRM`。
3.  **商家确认**: 商家审核通过 -> 状态置为 `CONFIRMED` -> **自动生成合同**。
4.  **签署合同**: 双方调用签署接口 -> 更新 `contracts` 表。
5.  **交付**: 商家点击发货 -> 状态 `DELIVERED` -> 租户确认收货/上传凭证 -> 状态 `RENTING`。
6.  **归还**: 租户发起归还 -> 商家确认收货 -> 扣除杂费/计算退款 -> 状态 `COMPLETED` -> 释放库存。

### 6.3 文件上传
*   由 `FileController` 处理，文件直接存储在后端运行目录的 `uploads/` 文件夹下。
*   `WebConfig` 将 `/files/**` URL 路径映射到本地文件系统路径，实现静态资源访问。

## 7. 配置文件详解 (`application.yml`)

*   **server.port**: 8081
*   **spring.datasource**: MySQL 连接配置，注意 `allowPublicKeyRetrieval=true` 解决驱动兼容性问题。
*   **spring.data.redis.cluster**: Redis 集群节点配置 (192.168.159.128:7000-7005)。
*   **mybatis-plus**: 开启驼峰命名映射，Mapper XML 位于 `classpath*:/mapper/**/*.xml`。
*   **alipay**: 沙箱环境配置，需填写 `appId`, `privateKey` (应用私钥), `alipayPublicKey` (支付宝公钥)。

## 8. 前后端交互逻辑

### 8.1 跨域处理 (CORS)
后端 `SecurityConfig` 显式允许了 `http://localhost:5173` (前端开发服) 的跨域请求，支持 Credentials。

### 8.2 异常处理交互
后端抛出异常 -> `GlobalExceptionHandler` 捕获 -> 返回 JSON (Code != 200) -> 前端 Axios 响应拦截器捕获 -> `ElMessage` 弹窗提示 -> 抛出 Promise 异常中断业务流。

## 9. 开发与维护建议

1.  **新增字段**: 需同步更新 `database.sql`、Entity 类以及前端的相关接口类型定义。
2.  **接口测试**: 推荐使用 Postman 或 Apifox，注意在 Header 中添加 `X-User-ID` 模拟特定用户。
3.  **日志排查**: MyBatis Plus 已开启 `StdOutImpl` 日志，控制台可直接查看执行的 SQL 语句。
4.  **环境迁移**: 部署生产环境时，需修改 `WebConfig` 中的文件存储路径，并确保 Redis 地址可达。


商家
onlys/abc123456
管理员
admin01/admin001
普通
测试测试/ceshi001


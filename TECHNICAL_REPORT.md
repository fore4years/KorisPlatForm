# 发电机租赁平台项目技术报告

## 1. 本项目主要研究内容

本项目旨在构建一个高效、便捷、可靠的**发电机租赁服务平台**，连接发电机租赁商（商家）与有临时用电需求的个人或企业（租户）。项目基于前后端分离架构，前端采用 UniApp 跨端框架（兼容微信小程序和 H5），后端采用 Spring Boot 微服务架构。

**主要研究内容包括：**
*   **跨端应用开发**：利用 UniApp 实现一套代码多端运行，解决微信小程序与 H5 在底层 API、样式渲染及运行环境上的差异兼容问题。
*   **租赁业务流程数字化**：实现从设备发布、搜索匹配、在线下单、押金支付、物流交付到归还结算的闭环流程管理。
*   **基于地理位置的服务（LBS）**：利用经纬度计算实现“附近的设备”推荐及距离估算功能。
*   **多角色权限控制**：构建租户、商家、管理员三端权限体系，通过 Spring Security 实现细粒度的接口访问控制。
*   **信用与风控体系**：通过实名认证、营业执照审核、押金机制及评价系统，降低租赁交易风险。

---

## 2. 需求分析

### 2.1 角色功能分析

系统划分为三个核心角色，各角色功能模块如下：

#### (1) 租户 (Tenant)
*   **账号体系**：注册登录、实名认证（身份证校验）、个人中心管理。
*   **设备发现**：首页推荐、多维度筛选（功率、价格、距离）、关键字搜索、查看设备详情。
*   **租赁交易**：创建租赁订单、在线支付（租金+押金）、订单状态追踪（待确认、配送中、使用中）、确认收货、申请归还。
*   **售后服务**：评价打分、发起投诉、申请报修。

#### (2) 商家 (Merchant)
*   **入驻审核**：提交企业/个人资质材料（营业执照、身份证），等待管理员审核。
*   **设备管理**：发布新设备（上传图片、设置参数、定价）、编辑/下架设备、库存状态管理。
*   **订单处理**：接单/拒单、更新物流状态（上传发货凭证）、确认归还（检查设备状况、结算扣费）。
*   **经营报表**：查看收益统计、订单流水。

#### (3) 管理员 (Admin)
*   **全局概览**：平台数据看板（用户数、设备数、交易额）。
*   **审核管理**：审核商家入驻申请（通过/驳回）。
*   **用户管理**：用户状态控制（禁用/解封）。
*   **纠纷处理**：处理用户投诉与仲裁。

### 2.2 核心业务流程 (数据流)

1.  **租赁流程**：
    租户发起租赁请求 -> 生成待确认订单 -> 商家确认接单 -> 租户支付 -> 商家发货 -> 租户收货 -> 租赁期 -> 租户归还 -> 商家确认收货并结算押金 -> 订单完成。

2.  **入驻流程**：
    用户提交入驻申请 -> 写入申请表(Pending) -> 管理员后台查看 -> 审核操作 -> 更新用户角色为Merchant -> 发送通知。

---

## 3. 开发环境和技术选型

### 3.1 开发环境
*   **操作系统**：Windows 10/11 或 macOS
*   **JDK 版本**：Java 17 (LTS)
*   **数据库**：MySQL 8.0
*   **缓存**：Redis 6.x
*   **开发工具**：
    *   后端：IntelliJ IDEA
    *   前端：HBuilderX (UniApp 官方推荐) / VS Code
    *   调试：微信开发者工具

### 3.2 技术选型

#### 后端技术栈
*   **Spring Boot 3.1.7**：核心容器框架，提供自动配置与快速开发能力。
*   **MyBatis-Plus 3.5.7**：ORM 框架，简化 SQL 操作，提供强大的 CRUD 接口。
*   **Spring Security**：安全框架，负责用户认证（JWT）与授权。
*   **Redis**：用于缓存热点数据（如设备列表）、存储 Token 黑名单。
*   **Lombok**：简化实体类代码（Getter/Setter/Builder）。
*   **Alipay SDK**：集成支付宝沙箱环境进行模拟支付。

#### 前端技术栈 (UniApp)
*   **Vue 3**：采用 Composition API (组合式 API) 编写逻辑，提升代码复用性。
*   **Vite**：新一代前端构建工具，提供极速的冷启动和热更新。
*   **UniApp**：跨端开发框架，支持编译到微信小程序、H5、App 等平台。
*   **SCSS**：CSS 预处理器，用于编写结构化样式。

---

## 4. 数据库设计

### 4.1 E-R 图关系描述
*   **User (用户)** 1:N **Generator (设备)**：一个商家可发布多个设备。
*   **User (用户)** 1:N **Order (订单)**：一个用户可有多个订单（作为租户或商家）。
*   **Order (订单)** 1:1 **Review (评价)**：一个订单对应一条评价。
*   **Order (订单)** 1:N **Complaint (投诉)**：一个订单可关联投诉记录。

### 4.2 核心数据表设计

#### 1. 用户表 (`users`)
| 字段名 | 类型 | 注释 | 备注 |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | 主键ID | 自增 |
| `username` | VARCHAR | 用户名 | 唯一 |
| `password` | VARCHAR | 密码 | 加密存储 |
| `role` | VARCHAR | 角色 | TENANT/MERCHANT/ADMIN |
| `phone` | VARCHAR | 手机号 | |
| `status` | VARCHAR | 状态 | ACTIVE/DISABLED |
| `identity_card` | VARCHAR | 身份证号 | 个人实名认证 |
| `business_license` | VARCHAR | 营业执照 | 企业认证 |

#### 2. 发电机设备表 (`generators`)
| 字段名 | 类型 | 注释 | 备注 |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | 主键ID | 自增 |
| `merchant_id` | BIGINT | 商家ID | 外键 -> users.id |
| `name` | VARCHAR | 设备名称 | |
| `power` | VARCHAR | 功率 | 如 50KW |
| `daily_rent` | DECIMAL | 日租金 | |
| `deposit` | DECIMAL | 押金 | |
| `stock_status` | VARCHAR | 库存状态 | AVAILABLE/RENTED/MAINTENANCE |
| `image_url` | TEXT | 图片地址 | |
| `address` | VARCHAR | 存放地址 | |
| `delivery_method` | VARCHAR | 配送方式 | |

#### 3. 订单表 (`orders`)
| 字段名 | 类型 | 注释 | 备注 |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | 主键ID | 自增 |
| `tenant_id` | BIGINT | 租户ID | 外键 |
| `merchant_id` | BIGINT | 商家ID | 外键 |
| `generator_id` | BIGINT | 设备ID | 外键 |
| `total_amount` | DECIMAL | 总金额 | |
| `status` | VARCHAR | 订单状态 | WAIT_CONFIRM/RENTING... |
| `start_time` | DATETIME | 起租时间 | |
| `end_time` | DATETIME | 归还时间 | |
| `payment_status` | VARCHAR | 支付状态 | PENDING/PAID |
| `delivery_address` | VARCHAR | 收货地址 | |

#### 4. 商家入驻申请表 (`merchant_applications`)
| 字段名 | 类型 | 注释 | 备注 |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | 主键ID | |
| `user_id` | BIGINT | 申请用户ID | |
| `merchant_name`| VARCHAR | 店铺名称 | |
| `merchant_type` | VARCHAR | 类型 | PERSONAL/ENTERPRISE |
| `status` | VARCHAR | 审核状态 | PENDING/APPROVED/REJECTED |
| `audit_time` | DATETIME | 审核时间 | |

---

## 5. 核心功能模块实现

### 5.1 发电机搜索与展示模块 (租户端)

**功能描述**：
租户在首页可以通过下拉菜单筛选功率，或输入关键词搜索发电机。列表展示设备图片、名称、功率、距离（模拟）及价格。

**核心实现逻辑**：
前端使用 `uni-app` 的 `picker` 组件和 `input` 组件收集筛选条件，调用后端 `/api/generators/search` 接口。后端使用 MyBatis-Plus 的 `QueryWrapper` 进行动态 SQL 拼接查询。

**后端核心代码 (GeneratorController.java)**：
```java
/**
 * 搜索发电机接口
 * 接收前端传来的功率(power)、位置(location)、价格区间等参数
 */
@GetMapping("/search")
public Result<List<GeneratorDTO>> search(@ModelAttribute GeneratorSearchRequest request) {
    // 调用 Service 层处理复杂的筛选逻辑
    return Result.success(generatorService.searchGenerators(request));
}
```

**前端核心代码 (home.vue)**：
```javascript
// 响应式搜索表单数据
const searchForm = reactive({
  power: '',
  location: '',
  sortType: ''
})

// 触发搜索
const handleSearch = async () => {
  loading.value = true
  try {
    // 调用封装好的 API 方法
    const response = await searchGenerators(searchForm)
    generators.value = response || []
  } finally {
    loading.value = false
  }
}
```

### 5.2 租赁订单创建与处理模块

**功能描述**：
租户在详情页点击“立即租赁”，填写起止时间，系统自动计算价格。提交后生成订单，商家在后台进行接单确认。

**核心实现逻辑**：
1.  **算价**：前端监听日期变化，调用 `/api/orders/calculate-price` 获取预计租金。
2.  **下单**：调用 `/api/orders` (POST) 创建订单，状态置为 `WAIT_CONFIRM`。
3.  **确认**：商家调用 `/api/orders/{id}/confirm` 接口流转状态。

**后端核心代码 (OrderService.java)**：
```java
@Transactional
public OrderResponse createOrder(OrderRequest request, String tenantId) {
    // 1. 校验设备是否存在且库存为 AVAILABLE
    Generator generator = generatorMapper.selectById(request.getGeneratorId());
    if (generator.getStockStatus() != Generator.StockStatus.AVAILABLE) {
        throw new BusinessException("设备当前不可租");
    }
    
    // 2. 构建订单实体，计算金额
    Order order = new Order();
    order.setTenantId(Long.valueOf(tenantId));
    order.setMerchantId(generator.getMerchantId());
    order.setGeneratorId(generator.getId());
    order.setTotalAmount(calculateTotal(generator, request.getStartTime(), request.getEndTime()));
    order.setStatus(Order.Status.WAIT_CONFIRM);
    
    // 3. 落库
    orderMapper.insert(order);
    return convertToResponse(order);
}
```

### 5.3 商家入驻审核模块 (管理员端)

**功能描述**：
管理员在后台查看所有状态为 `PENDING` 的申请记录，查看其上传的证件照片，点击“通过”或“驳回”。

**核心实现逻辑**：
前端管理员首页通过 `Tabs` 切换到审核面板，调用 `/api/admin/merchant-applications` 获取列表。操作时调用 `/audit` 接口更新申请表状态，并同步修改用户表中的 `role` 字段。

**后端核心代码 (AdminController.java)**：
```java
@PostMapping("/merchant-applications/{id}/audit")
public Result<Void> auditApplication(@PathVariable Long id, @RequestBody AuditRequest request) {
    // 1. 获取申请记录
    MerchantApplication app = applicationMapper.selectById(id);
    
    // 2. 根据审核结果更新状态
    if (request.getStatus() == MerchantApplication.Status.APPROVED) {
        app.setStatus(MerchantApplication.Status.APPROVED);
        // 同时将对应用户的角色升级为 MERCHANT
        userService.updateUserRole(app.getUserId(), User.Role.MERCHANT);
    } else {
        app.setStatus(MerchantApplication.Status.REJECTED);
        app.setRejectionReason(request.getReason());
    }
    applicationMapper.updateById(app);
    return Result.success();
}
```

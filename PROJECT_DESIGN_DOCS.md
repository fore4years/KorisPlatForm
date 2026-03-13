# 发电机租赁平台项目设计文档

本文档详细描述了发电机租赁平台的系统设计，包括用例图、业务流程图、功能模块设计、详细功能设计以及数据库设计。

## 一、系统用例图

本系统包含三种角色：租户、商家和管理员。以下是各角色的核心用例。

```mermaid
graph TD
    subgraph 租户端
        T[租户] --> T1(注册与登录)
        T --> T2(个人实名认证)
        T --> T3(搜索与筛选发电机)
        T --> T4(查看设备详情)
        T --> T5(加入购物车)
        T --> T6(创建租赁订单)
        T --> T7(支付押金/租金)
        T --> T8(订单评价)
        T --> T9(发起报修/投诉)
    end

    subgraph 商家端
        M[商家] --> M1(注册与登录)
        M --> M2(企业/商户认证)
        M --> M3(发布发电机设备)
        M --> M4(设备上下架管理)
        M --> M5(处理租赁订单)
        M --> M6(签署电子合同)
        M --> M7(查看收益统计)
        M --> M8(处理维修请求)
    end

    subgraph 管理端
        A[管理员] --> A1(登录系统)
        A --> A2(用户管理)
        A --> A3(商户入驻审核)
        A --> A4(设备上架审核)
        A --> A5(订单纠纷处理)
        A --> A6(平台数据统计)
        A --> A7(系统参数配置)
    end
```

## 二、业务流程图

### 1. 核心租赁业务流程
描述了从租户下单到订单完成的完整闭环流程。

```mermaid
graph TD
    Start((开始)) --> Search[租户搜索设备]
    Search --> Detail[查看详情]
    Detail --> Order[提交订单]
    Order --> Pay{支付押金/租金}
    Pay -- 未支付 --> Cancel[取消订单]
    Pay -- 支付成功 --> MerchantCheck{商家接单确认}
    
    MerchantCheck -- 拒绝 --> Refund[退款]
    Refund --> End((结束))
    
    MerchantCheck -- 接受 --> Delivery[设备交付/物流]
    Delivery --> Renting[租赁计费中]
    Renting --> Return[租户归还设备]
    Return --> Inspect{商家验收}
    
    Inspect -- 有损坏 --> Deduction[扣除部分押金]
    Inspect -- 无损坏 --> Settlement[结算尾款/退押金]
    
    Deduction --> Complete[订单完成]
    Settlement --> Complete
    Complete --> Review[双方互评]
    Review --> End
```

### 2. 商家入驻与审核流程

```mermaid
graph TD
    Start((开始)) --> Register[用户注册]
    Register --> Apply[提交商户认证资料]
    Apply --> Pending[状态: 待审核]
    Pending --> AdminAudit{管理员审核}
    
    AdminAudit -- 驳回 --> Reject[审核失败]
    Reject --> Modify[用户修改资料]
    Modify --> Apply
    
    AdminAudit -- 通过 --> Approved[审核通过]
    Approved --> RoleChange[角色变更为商家]
    RoleChange --> Publish[允许发布设备]
    Publish --> End((结束))
```

### 3. 系统运行流程图

描述了从用户操作到数据持久化的全链路交互流程。

```mermaid
graph TD
    User((用户)) --> OS([操作系统])
    OS --> Vue[前端Vue框架]
    Vue --> Jump([组件跳转])
    Jump --> Request([向后端发送请求])
    Request --> Spring[后端SpringMVC架构]
    
    Spring --> Controller([Controller层接收])
    Controller --> Service([调用Service层处理对应请求])
    Service --> ServiceImpl([调用Service层实现类实现具体逻辑])
    ServiceImpl --> Mapper([调用Mapper层对持久层操作])
    
    %% 返回路径
    Mapper -- 返回数据 --> ServiceImpl
    ServiceImpl -- 返回数据 --> Service
    Service -- 返回数据 --> Controller
    Controller -- 返回数据 --> Spring
    Spring -- 返回数据 --> Vue
    Vue -- 渲染数据 --> OS
```


## 三、系统功能模块设计图

系统采用前后端分离架构，分为租户端、商家端和管理后台。

```mermaid
graph TD
    %% 根节点
    Root[发电机租赁平台]:::root

    %% 第一层分支
    Root --> Admin[后台管理系统]:::system
    Root --> Front[前台系统]:::system

    %% 后台管理系统分支
    Admin --> A_User[用户管理模块]:::module
    A_User --> A_UserList[用户列表]:::feature
    A_User --> A_RoleList[角色管理]:::feature
    A_User --> A_Audit[商户审核]:::feature

    Admin --> A_Order[订单管理模块]:::module
    A_Order --> A_OrderList[订单列表]:::feature
    A_Order --> A_Dispute[纠纷处理]:::feature
    A_Order --> A_Setting[订单设置]:::feature

    Admin --> A_Device[设备管理模块]:::module
    A_Device --> A_DeviceAudit[设备审核]:::feature
    A_Device --> A_Cat[设备分类]:::feature

    Admin --> A_Finance[财务管理模块]:::module
    A_Finance --> A_Flow[平台流水]:::feature
    A_Finance --> A_Withdraw[提现审核]:::feature

    %% 前台系统分支 (包含租户和商家)
    Front --> F_Public[公共服务]:::module
    F_Public --> F_Reg[注册/登录]:::feature
    F_Public --> F_Auth[实名认证]:::feature

    Front --> F_Tenant[租户功能]:::module
    F_Tenant --> F_Search[设备搜索]:::feature
    F_Tenant --> F_Cart[购物车]:::feature
    F_Tenant --> F_Rent[下单租赁]:::feature
    F_Tenant --> F_Comment[评价]:::feature

    Front --> F_Merchant[商家功能]:::module
    F_Merchant --> F_Publish[发布设备]:::feature
    F_Merchant --> F_MOrder[订单管理]:::feature
    F_Merchant --> F_Income[收益统计]:::feature
    
    %% 样式定义
    classDef root fill:#f96,stroke:#333,stroke-width:2px;
    classDef system fill:#6cf,stroke:#333,stroke-width:2px;
    classDef module fill:#9cf,stroke:#333,stroke-width:1px;
    classDef feature fill:#cdf,stroke:#333,stroke-width:1px;

```

## 四、系统功能详细设计

### 4.2.1 用户认证与权限管理功能流程
```mermaid
graph TD
    Start((开始)) --> Login[用户输入账号密码]
    Login --> Verify{系统身份校验}
    Verify -- 失败 --> Hint[报错提示]
    Hint --> Login
    Verify -- 成功 --> RoleCheck{判断用户角色}
    
    RoleCheck -- 租户 --> T_Dash[进入租户端首页]
    RoleCheck -- 商家 --> M_Dash[进入商家端工作台]
    RoleCheck -- 管理员 --> A_Dash[进入管理后台]
    
    T_Dash --> End((结束))
    M_Dash --> End
    A_Dash --> End
```

### 4.2.2 发电机搜索与展示功能流程
```mermaid
graph TD
    Start((开始)) --> Input[输入关键词/筛选条件]
    Input --> LBS[获取当前地理位置]
    LBS --> Search[查询数据库]
    Search --> Filter[多维度过滤: 功率/租金/距离]
    Filter --> Sort[排序: 距离优先/价格优先]
    Sort --> Render[渲染设备列表]
    Render --> Detail[点击查看设备详情]
    Detail --> End((结束))
```

### 4.2.3 租赁订单流程功能
```mermaid
graph TD
    Start((开始)) --> Select[选择设备与租赁时间]
    Select --> Create[生成待支付订单]
    Create --> Pay[在线支付押金/租金]
    Pay --> Merchant[商家接收订单通知]
    Merchant --> Confirm{商家是否接单}
    
    Confirm -- 拒绝 --> Refund[原路退款]
    Confirm -- 接受 --> Deliver[安排设备交付]
    
    Deliver --> Use[租户使用中]
    Use --> Return[设备归还与验收]
    Return --> Settle[订单结算完成]
    Settle --> End((结束))
```

### 4.2.4 在线支付功能流程
```mermaid
graph TD
    Start((开始)) --> Order[获取订单金额]
    Order --> Channel[选择支付方式: 微信/余额]
    Channel --> Gateway[调用支付网关接口]
    Gateway --> Callback{支付结果回调}
    
    Callback -- 失败 --> Retry[重新发起支付]
    Callback -- 成功 --> Update[更新订单状态: 已支付]
    Update --> Notify[发送支付成功通知]
    Notify --> End((结束))
```

### 4.2.5 信用评价功能流程
```mermaid
graph TD
    Start((开始)) --> Check[订单状态判定: 已完成]
    Check --> Input[填写评分与评论文字]
    Input --> Upload[上传现场照片凭证]
    Upload --> Submit[提交评价]
    Submit --> DB[保存至评价表]
    DB --> Calc[更新设备/商家信用分]
    Calc --> Display[前台展示评价内容]
    Display --> End((结束))
```

### 4.2.6 数据统计与分析功能流程
```mermaid
graph TD
    Start((开始)) --> Collect[收集订单/流水原始数据]
    Collect --> Filter[按时间维度筛选: 日/月/年]
    Filter --> Agg[聚合计算: 总营收/订单量]
    Agg --> Chart[生成图表数据: 折线图/饼图]
    Chart --> View[前端可视化展示]
    View --> Export[导出统计报表]
    Export --> End((结束))
```

## 五、核心功能详细设计图 (旧版)


## 五、系统的 E-R 图与逻辑结构设计

### 1. 数据库逻辑结构设计

根据数据库表结构设计，核心表的逻辑结构如下（下划线表示主键）：

1.  **用户表 (users)**
    *   结构：(<u>用户id</u>、用户名、密码、手机号、角色、真实姓名、身份证号、营业执照号、头像、公司名称、审核状态)
    *   说明：存储所有角色的基础信息，角色字段区分租户(TENANT)、商家(MERCHANT)、管理员(ADMIN)。

2.  **发电机设备表 (generators)**
    *   结构：(<u>设备id</u>、商家id、设备名称、功率、燃油消耗、尺寸、重量、日租金、押金、库存状态、图片URL、地址、经纬度)
    *   外键：商家id 关联 **用户表** 的 用户id。

3.  **租赁订单表 (orders)**
    *   结构：(<u>订单id</u>、租户id、商家id、设备id、开始时间、结束时间、总金额、押金金额、订单状态、支付状态、配送地址、联系人)
    *   外键：租户id 关联 **用户表**；商家id 关联 **用户表**；设备id 关联 **发电机设备表**。

4.  **评价表 (reviews)**
    *   结构：(<u>评价id</u>、订单id、租户id、设备id、评分、评论内容、创建时间)
    *   外键：订单id 关联 **租赁订单表**；租户id 关联 **用户表**；设备id 关联 **发电机设备表**。

5.  **购物车表 (cart_items)**
    *   结构：(<u>购物车项id</u>、用户id、设备id、租赁时长、配送方式)
    *   外键：用户id 关联 **用户表**；设备id 关联 **发电机设备表**。

6.  **收藏表 (favorites)**
    *   结构：(<u>收藏id</u>、用户id、设备id、创建时间)
    *   外键：用户id 关联 **用户表**；设备id 关联 **发电机设备表**。

7.  **投诉/纠纷表 (complaints)**
    *   结构：(<u>投诉id</u>、订单id、原告id、被告id、投诉内容、处理状态、处理结果)
    *   外键：订单id 关联 **租赁订单表**；原告id/被告id 关联 **用户表**。

8.  **维修请求表 (repair_requests)**
    *   结构：(<u>请求id</u>、订单id、故障描述、图片凭证、处理状态、商家回复)
    *   外键：订单id 关联 **租赁订单表**。

### 2. 系统 E-R 图

```mermaid
graph TD
    %% 实体定义
    User[用户]:::entity
    Generator[发电机设备]:::entity
    Order[租赁订单]:::entity
    Cart[购物车]:::entity
    Review[评价]:::entity
    Complaint[投诉]:::entity

    %% 属性定义 - 用户
    User_ID([id])
    User_Name([用户名])
    User_Role([角色])
    User_Phone([手机号])
    User --- User_ID
    User --- User_Name
    User --- User_Role
    User --- User_Phone

    %% 属性定义 - 发电机
    Gen_ID([id])
    Gen_Name([设备名称])
    Gen_Price([租金])
    Gen_Power([功率])
    Generator --- Gen_ID
    Generator --- Gen_Name
    Generator --- Gen_Price
    Generator --- Gen_Power

    %% 属性定义 - 订单
    Ord_ID([id])
    Ord_Amount([总金额])
    Ord_Status([状态])
    Ord_Time([时间])
    Order --- Ord_ID
    Order --- Ord_Amount
    Order --- Ord_Status
    Order --- Ord_Time

    %% 关系定义
    Rel_Publish{发布}:::relation
    Rel_Rent{租赁}:::relation
    Rel_Has{拥有}:::relation
    Rel_Complain{发起}:::relation
    Rel_Submit_Rev{提交}:::relation
    Rel_Rev_Order{关联}:::relation

    %% 属性定义 - 评价
    Rev_ID([id])
    Rev_Rating([评分])
    Rev_Content([内容])
    Review --- Rev_ID
    Review --- Rev_Rating
    Review --- Rev_Content

    %% 连接关系
    User ---|n| Rel_Publish
    Rel_Publish ---|1| Generator

    User ---|n| Rel_Rent
    Rel_Rent ---|n| Generator
    Rel_Rent -.->|生成| Order
    
    User ---|1| Rel_Has
    Rel_Has ---|1| Cart
    Cart ---|n| Generator

    User ---|n| Rel_Complain
    Rel_Complain ---|1| Complaint
    Complaint -.->|关联| Order

    User ---|n| Rel_Submit_Rev
    Rel_Submit_Rev ---|1| Review
    Review ---|1| Rel_Rev_Order
    Rel_Rev_Order ---|1| Order

    %% 样式
    classDef entity fill:#fff,stroke:#333,stroke-width:2px;
    classDef relation fill:#fff,stroke:#333,stroke-width:1px,shape:rhombus;
    %% 属性样式：模拟椭圆
    classDef default fill:#fff,stroke:#333,stroke-width:1px;

```

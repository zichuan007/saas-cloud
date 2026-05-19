# 04 — 接口设计

## 1. 全局约定

### 1.1 基础 URL

```
租户端:    https://api.example.com/api/{service}/...
平台端:    https://api.example.com/api/platform/...
```

### 1.2 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1700000000000,
  "traceId": "abc123def456"
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证（Token 无效/过期） |
| 403 | 无权限 / 租户已冻结 |
| 404 | 资源不存在 |
| 409 | 业务冲突（如配额超限） |
| 429 | 限流 |
| 500 | 服务器错误 |

### 1.3 分页请求与响应

```
请求参数:
  pageNum     int    页码，从 1 开始
  pageSize    int    每页条数，默认 10，最大 100

响应:
{
  "code": 200,
  "data": {
    "records": [ ... ],
    "total": 150,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 15
  }
}
```

### 1.4 认证 Header

```
Authorization: Bearer <access_token>
```

---

## 2. 认证接口（rbac-service）

### 2.1 租户注册

```
POST /api/rbac/auth/register

Request:
{
  "tenantName": "杭州某某科技有限公司",
  "contactPerson": "张三",
  "phone": "13800138000",
  "verifyCode": "123456",
  "password": "Abc@1234"
}

Response:
{
  "code": 200,
  "data": {
    "tenantId": 1001,
    "tenantCode": "T20260518001",
    "userId": 1,
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 7200
  }
}

说明:
  - 自动创建租户 + 管理员账号 + 默认角色 + 根部门
  - 默认套餐为免费版
  - 返回 Token 直接登录
```

### 2.2 登录

```
POST /api/rbac/auth/login

Request:
{
  "username": "zhangsan",
  "password": "Abc@1234",
  "tenantCode": "T20260518001"
}

Response:
{
  "code": 200,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 7200
  }
}

说明:
  - tenantCode 必填，用于定位租户
  - 登录前校验租户状态（冻结/注销 → 拒绝登录）
  - 连续 5 次密码错误锁定 30 分钟
```

### 2.3 刷新 Token

```
POST /api/rbac/auth/refresh-token

Request:
{
  "refreshToken": "eyJ..."
}

Response:
{
  "code": 200,
  "data": {
    "accessToken": "eyJ...(包含最新权限)",
    "refreshToken": "eyJ...",
    "expiresIn": 7200
  }
}
```

### 2.4 登出

```
POST /api/rbac/auth/logout

Response:
{
  "code": 200,
  "message": "success"
}

说明:
  - 将 access_token 和 refresh_token 加入 Redis 黑名单
  - 黑名单 TTL = Token 剩余有效期
```

### 2.5 获取当前用户信息

```
GET /api/rbac/auth/user-info

Response:
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "realName": "张三",
    "avatar": "https://...",
    "phone": "13800138000",
    "tenantId": 1001,
    "tenantName": "杭州某某科技有限公司",
    "deptId": 1,
    "deptName": "总公司",
    "roleLevel": 0,
    "roles": ["admin"],
    "permissions": ["sys:user:list", "sys:user:add", ...],
    "menus": [
      {
        "id": 1,
        "menuName": "系统管理",
        "path": "/system",
        "icon": "setting",
        "children": [ ... ]
      }
    ]
  }
}
```

### 2.6 获取验证码

```
POST /api/rbac/auth/captcha

Request:
{
  "phone": "13800138000",
  "type": "REGISTER"
}

Response:
{
  "code": 200,
  "message": "验证码已发送"
}
```

---

## 3. 用户管理接口（rbac-service）

```
GET    /api/rbac/user/list                分页查询用户列表
GET    /api/rbac/user/{id}                获取用户详情
POST   /api/rbac/user                     创建用户（直接添加）
PUT    /api/rbac/user/{id}                编辑用户
DELETE /api/rbac/user/{id}                删除用户（逻辑删除）
PUT    /api/rbac/user/{id}/status         启用/禁用
PUT    /api/rbac/user/{id}/reset-password 重置密码
PUT    /api/rbac/user/profile             修改个人信息
PUT    /api/rbac/user/password            修改个人密码

POST   /api/rbac/user/invite              邀请成员（发送邀请链接）
POST   /api/rbac/user/accept-invite       接受邀请
```

### 3.1 创建用户 — 配额校验示例

```
POST /api/rbac/user

Request:
{
  "username": "lisi",
  "realName": "李四",
  "phone": "13900139000",
  "password": "Init@1234",
  "deptId": 2,
  "roleIds": [1, 2]
}

Response (成功):
{
  "code": 200,
  "data": { "userId": 5 }
}

Response (配额超限):
{
  "code": 409,
  "message": "当前套餐最多支持 10 名用户，已使用 10 名，请升级套餐"
}

说明:
  - 创建前 Feign 调 platform-service 校验当前租户用户配额
  - 自动填充 tenant_id（从 TenantContext 获取）
```

### 3.2 邀请成员

```
POST /api/rbac/user/invite

Request:
{
  "phone": "13900139000",
  "roleIds": [2],
  "deptId": 3
}

Response:
{
  "code": 200,
  "data": {
    "inviteCode": "INV-abcdef123456",
    "expireTime": "2026-05-25 18:00:00"
  }
}

说明:
  - 生成邀请链接，通过短信/邮件发送
  - 邀请码 7 天有效
  - 受邀人打开链接后设置用户名和密码，完成注册
```

---

## 4. 角色管理接口（rbac-service）

```
GET    /api/rbac/role/list              角色列表（本租户）
GET    /api/rbac/role/{id}              角色详情（含已分配菜单ID列表）
POST   /api/rbac/role                   创建角色
PUT    /api/rbac/role/{id}              编辑角色
DELETE /api/rbac/role/{id}              删除角色
PUT    /api/rbac/role/{id}/status       启用/禁用
PUT    /api/rbac/role/{id}/menus        分配菜单权限
PUT    /api/rbac/role/{id}/data-scope   设置数据范围
```

### 4.1 分配菜单权限 — 套餐约束

```
PUT /api/rbac/role/{id}/menus

Request:
{
  "menuIds": [1, 2, 3, 10, 11, 20]
}

Response (成功):
{
  "code": 200,
  "message": "success"
}

Response (含套餐外菜单):
{
  "code": 403,
  "message": "菜单 [流程管理] 不在当前套餐范围内，请升级套餐"
}

说明:
  - 前端展示菜单树时，只展示本套餐可见的菜单
  - 后端二次校验：menuIds 必须是套餐 menu_ids 的子集
```

---

## 5. 部门管理接口（rbac-service）

```
GET    /api/rbac/dept/tree              部门树（本租户）
GET    /api/rbac/dept/{id}              部门详情
POST   /api/rbac/dept                   创建部门
PUT    /api/rbac/dept/{id}              编辑部门
DELETE /api/rbac/dept/{id}              删除部门（有子部门或用户时禁止）
```

### 5.1 创建部门 — ancestors 自动维护

```
POST /api/rbac/dept

Request:
{
  "deptName": "前端组",
  "parentId": 2,
  "leaderUserId": 5,
  "sortOrder": 1
}

处理逻辑:
  1. 查父部门的 ancestors = "0,1"
  2. 新部门的 ancestors = "0,1,2"
  3. 自动填充 tenant_id
```

---

## 6. 菜单管理接口（rbac-service —— 平台超管专用）

```
GET    /api/rbac/menu/tree              菜单树（全量）
GET    /api/rbac/menu/{id}              菜单详情
POST   /api/rbac/menu                   创建菜单
PUT    /api/rbac/menu/{id}              编辑菜单
DELETE /api/rbac/menu/{id}              删除菜单
```

**注意：** 菜单是平台级资源，只有平台超管才能管理。租户管理员只能在角色中分配自己套餐内的菜单，不能新增/修改菜单本身。

---

## 7. 流程管理接口（workflow-service）

### 7.1 流程设计

```
GET    /api/workflow/definition/list          流程定义列表（本租户）
GET    /api/workflow/definition/{id}          流程定义详情
POST   /api/workflow/definition               创建流程定义
PUT    /api/workflow/definition/{id}          编辑流程定义
DELETE /api/workflow/definition/{id}          删除流程定义
POST   /api/workflow/definition/{id}/deploy   部署流程
PUT    /api/workflow/definition/{id}/status   挂起/激活
GET    /api/workflow/definition/{id}/bpmn-xml 获取 BPMN XML

POST   /api/workflow/definition/node-config   保存节点审批人配置
GET    /api/workflow/definition/{id}/node-configs 获取节点配置列表

GET    /api/workflow/template/list            平台模板列表
POST   /api/workflow/template/{id}/import     导入模板到本租户
```

### 7.2 流程发起与审批

```
GET    /api/workflow/process/startable-list   可发起的流程列表
POST   /api/workflow/process/start            发起流程
GET    /api/workflow/process/my-initiated     我发起的
GET    /api/workflow/process/{id}             流程详情（含审批时间线）
GET    /api/workflow/process/{id}/diagram     流程图（高亮当前节点）
POST   /api/workflow/process/{id}/cancel      撤回

GET    /api/workflow/task/todo               我的待办
GET    /api/workflow/task/done               我的已办
GET    /api/workflow/task/copy               抄送我的
POST   /api/workflow/task/{id}/approve       通过
POST   /api/workflow/task/{id}/reject        驳回
POST   /api/workflow/task/{id}/transfer      转办
POST   /api/workflow/task/{id}/delegate      委派
POST   /api/workflow/task/{id}/add-sign      加签
POST   /api/workflow/task/{id}/urge          催办
PUT    /api/workflow/task/copy/{id}/read     标记抄送已读
```

### 7.3 流程监控（管理员）

```
GET    /api/workflow/monitor/instances        运行中的流程实例
POST   /api/workflow/monitor/{id}/terminate   强制终止
GET    /api/workflow/monitor/statistics        审批统计
```

### 7.4 发起流程示例

```
POST /api/workflow/process/start

Request:
{
  "processKey": "leave-apply",
  "title": "张三的请假申请 - 2026年5月20日",
  "formData": {
    "leaveType": "年假",
    "startDate": "2026-05-20",
    "endDate": "2026-05-21",
    "days": 2,
    "reason": "个人事务"
  }
}

Response:
{
  "code": 200,
  "data": {
    "processInstanceId": "12345",
    "currentTask": "部门主管审批",
    "currentAssignee": "李四"
  }
}

配额校验:
  - 调 platform-service 检查流程定义数量是否超限（仅创建新定义时）
  - 发起流程不受配额限制
```

---

## 8. 公众号接口（wechat-oa-service）

### 8.1 账号管理

```
GET    /api/wechat-oa/account/list           公众号列表（本租户）
GET    /api/wechat-oa/account/{id}           公众号详情
POST   /api/wechat-oa/account                绑定公众号
PUT    /api/wechat-oa/account/{id}           编辑公众号信息
DELETE /api/wechat-oa/account/{id}           解绑公众号
```

### 8.2 素材管理

```
GET    /api/wechat-oa/material/list          素材列表（按类型筛选）
POST   /api/wechat-oa/material/upload        上传素材
DELETE /api/wechat-oa/material/{id}          删除素材
POST   /api/wechat-oa/material/{id}/sync     同步到微信服务器
```

### 8.3 图文管理

```
GET    /api/wechat-oa/article/list           图文列表
GET    /api/wechat-oa/article/{id}           图文详情
POST   /api/wechat-oa/article                创建图文
PUT    /api/wechat-oa/article/{id}           编辑图文
DELETE /api/wechat-oa/article/{id}           删除图文
POST   /api/wechat-oa/article/{id}/preview   预览
POST   /api/wechat-oa/article/{id}/publish   发布
PUT    /api/wechat-oa/article/{id}/offline    下线
```

### 8.4 粉丝管理

```
GET    /api/wechat-oa/fan/list              粉丝列表
POST   /api/wechat-oa/fan/sync              全量同步粉丝
PUT    /api/wechat-oa/fan/{id}/blacklist     拉黑/取消拉黑
PUT    /api/wechat-oa/fan/{id}/tags          设置标签

GET    /api/wechat-oa/tag/list              标签列表
POST   /api/wechat-oa/tag                   创建标签
PUT    /api/wechat-oa/tag/{id}              编辑标签
DELETE /api/wechat-oa/tag/{id}              删除标签
POST   /api/wechat-oa/tag/sync              同步标签到微信
```

### 8.5 自动回复

```
GET    /api/wechat-oa/auto-reply/list       自动回复规则列表
POST   /api/wechat-oa/auto-reply            创建规则
PUT    /api/wechat-oa/auto-reply/{id}       编辑规则
DELETE /api/wechat-oa/auto-reply/{id}       删除规则
PUT    /api/wechat-oa/auto-reply/{id}/status 启用/禁用
```

### 8.6 公众号菜单

```
GET    /api/wechat-oa/menu/list             当前菜单配置
POST   /api/wechat-oa/menu/save             保存菜单配置
POST   /api/wechat-oa/menu/publish          发布到微信
```

### 8.7 数据看板

```
GET    /api/wechat-oa/dashboard/fan-trend    粉丝趋势（新增/取关/净增）
GET    /api/wechat-oa/dashboard/article-rank 图文排行

Query Parameters:
  accountId   long   公众号ID（必填）
  startDate   string 开始日期
  endDate     string 结束日期
```

### 8.8 微信回调（白名单，免认证）

```
GET  /api/wechat-oa/callback/{appId}     微信验证签名
POST /api/wechat-oa/callback/{appId}     接收微信推送事件

说明:
  - 通过 appId 定位到对应的公众号和租户
  - 处理事件：关注/取关/消息/菜单点击
  - 触发自动回复规则
```

---

## 9. 平台管理接口（platform-service）

### 9.1 平台认证

```
POST /api/platform/auth/login            平台管理员登录
POST /api/platform/auth/logout           登出
GET  /api/platform/auth/user-info        当前管理员信息
```

### 9.2 租户管理

```
GET    /api/platform/tenant/list          租户列表（分页，支持状态/套餐筛选）
GET    /api/platform/tenant/{id}          租户详情（含配额使用情况）
POST   /api/platform/tenant               创建租户（后台创建）
PUT    /api/platform/tenant/{id}          编辑租户信息
PUT    /api/platform/tenant/{id}/freeze   冻结租户
PUT    /api/platform/tenant/{id}/unfreeze 解冻租户
PUT    /api/platform/tenant/{id}/package  变更套餐
DELETE /api/platform/tenant/{id}          注销租户（软删除）
GET    /api/platform/tenant/{id}/quota    获取配额使用详情
```

### 9.3 套餐管理

```
GET    /api/platform/package/list         套餐列表
GET    /api/platform/package/{id}         套餐详情
POST   /api/platform/package              创建套餐
PUT    /api/platform/package/{id}         编辑套餐
PUT    /api/platform/package/{id}/status  启用/禁用
```

### 9.4 系统公告

```
GET    /api/platform/announcement/list       公告列表
POST   /api/platform/announcement            创建公告
PUT    /api/platform/announcement/{id}       编辑公告
POST   /api/platform/announcement/{id}/publish 发布公告
PUT    /api/platform/announcement/{id}/offline 下线公告
```

### 9.5 全局统计

```
GET /api/platform/statistics/overview

Response:
{
  "code": 200,
  "data": {
    "totalTenants": 150,
    "activeTenants": 120,
    "trialTenants": 25,
    "frozenTenants": 5,
    "totalUsers": 3500,
    "todayActiveUsers": 800,
    "totalProcessInstances": 12000,
    "monthlyProcessInstances": 1500
  }
}
```

### 9.6 全局配置

```
GET    /api/platform/config/list            配置列表
PUT    /api/platform/config/{key}           修改配置

预置配置项:
  default_package_id     默认套餐ID
  trial_days             试用天数（默认15）
  max_login_attempts     最大登录尝试次数
  password_min_length    密码最小长度
```

---

## 10. 通知接口（notify-service）

### 10.1 站内消息

```
GET    /api/notify/message/list             消息列表（支持类型/已读筛选）
GET    /api/notify/message/unread-count     未读消息数
PUT    /api/notify/message/{id}/read        标记已读
PUT    /api/notify/message/read-all         全部标记已读
DELETE /api/notify/message/{id}             删除消息
```

### 10.2 通知渠道配置（租户管理员）

```
GET    /api/notify/channel/list             渠道配置列表
PUT    /api/notify/channel/{channelType}    更新渠道配置（启用/禁用 + 配置信息）
POST   /api/notify/channel/{channelType}/test 发送测试通知
```

---

## 11. 内部 Feign 接口（服务间调用，不暴露给前端）

### 11.1 配额校验接口 (platform-service)

```java
@FeignClient("platform-service")
public interface PlatformFeignClient {

    @GetMapping("/internal/quota/check")
    R<Boolean> checkQuota(
        @RequestParam("tenantId") Long tenantId,
        @RequestParam("quotaType") String quotaType  // USER/PROCESS/WECHAT_ACCOUNT/STORAGE
    );

    @GetMapping("/internal/tenant/{id}")
    R<TenantDTO> getTenantInfo(@PathVariable("id") Long tenantId);

    @GetMapping("/internal/package/{id}/menu-ids")
    R<List<Long>> getPackageMenuIds(@PathVariable("id") Long packageId);
}
```

### 11.2 用户查询接口 (rbac-service)

```java
@FeignClient("rbac-service")
public interface RbacFeignClient {

    @GetMapping("/internal/user/{id}")
    R<UserDTO> getUserById(@PathVariable("id") Long userId);

    @GetMapping("/internal/user/batch")
    R<List<UserDTO>> getUsersByIds(@RequestParam("ids") List<Long> userIds);

    @GetMapping("/internal/dept/{id}/leader")
    R<UserDTO> getDeptLeader(@PathVariable("id") Long deptId);

    @GetMapping("/internal/role/{roleCode}/users")
    R<List<UserDTO>> getUsersByRoleCode(@RequestParam("roleCode") String roleCode);
}
```

---

## 12. 公众号操作的 accountId 上下文

公众号模块中，大部分接口需要指定 `accountId`。前端在页面顶部有「切换公众号」下拉，选中后前端将 `accountId` 作为请求参数或放在 Header 中。

```
方案: Query Parameter

GET /api/wechat-oa/material/list?accountId=1&materialType=0&pageNum=1&pageSize=10

说明:
  - 后端校验 accountId 是否属于当前租户
  - 如果 accountId 不属于当前 tenant_id → 403
```

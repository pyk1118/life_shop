<div align="center">

# 优品汇 LifeShop

**基于 Spring Boot 3 + Vue 3 的前后端分离多角色电商平台**

支持 用户 / 商家 / 平台管理员 三端角色，覆盖商品浏览、下单支付、售后退款、商家入驻审核的完整业务闭环

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-42b883.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

</div>

---

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [核心功能](#核心功能)
- [技术亮点](#技术亮点)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [接口概览](#接口概览)
- [已知不足与优化方向](#已知不足与优化方向)
- [项目收获](#项目收获)
- [开源协议](#开源协议)

---

## 项目简介

优品汇（LifeShop）是一个面向生活日用品类目的**多角色电商平台**，采用前后端分离架构独立开发完成。

项目并非停留在「商品增删改查」的演示层面，而是完整实现了电商核心业务链路中最具挑战的几个环节：

- **多角色权限隔离**：普通用户、入驻商家、平台管理员三类角色，基于 JWT + Spring Security 实现无状态鉴权与接口级权限控制
- **订单状态机流转**：7 种订单状态，每一次状态变更均做前置合法性校验，杜绝非法流转
- **商家入驻审核闭环**：用户提交入驻申请 → 平台管理员审核 → 异步邮件通知 → 商家账号开通并可登录后台
- **三级售后仲裁**：用户申请售后 → 商家处理 → 争议时平台介入仲裁

### 项目规模

| 指标 | 数量 |
|:---|:---|
| 后端 Java 文件 | 80 个（约 4,400 行） |
| 前端 Vue 组件 | 25 个（约 7,900 行） |
| REST 接口 | 89 个 |
| 数据库表 | 13 张 |
| 前端页面路由 | 14 个（含管理后台 11 个子路由） |

> 说明：本仓库为个人独立开发的学习实践项目，代码与文档均由本人完成。仓库中不包含任何真实用户数据与生产凭证。

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|:---|:---|:---|
| Spring Boot | 3.2.5 | 基础框架 |
| Java | 21 | 开发语言（LTS） |
| Spring Security | 6.x | 认证授权框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架 + 分页插件 |
| MySQL | 8.0 | 关系型数据库 |
| JJWT | 0.12.5 | JWT 令牌签发与校验 |
| Spring Mail | - | 异步邮件通知 |
| Hutool | 5.8.28 | 工具类库 |
| Aliyun OSS SDK | 3.17.4 | 对象存储（图片上传） |
| Lombok | - | 简化样板代码 |
| HikariCP | - | 数据库连接池 |

### 前端

| 技术 | 版本 | 用途 |
|:---|:---|:---|
| Vue | 3.4 | 渐进式框架（Composition API） |
| Vite | 5.3 | 构建工具 |
| Element Plus | 2.7 | UI 组件库 |
| Pinia | 2.1 | 状态管理 |
| Vue Router | 4.4 | 路由与导航守卫 |
| Axios | 1.7 | HTTP 客户端（含拦截器封装） |
| ECharts | 6.0 | 数据可视化看板 |
| Less | 4.2 | CSS 预处理器 |
| Day.js | 1.11 | 日期格式化 |

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        浏览器客户端                          │
│   用户端页面        商家后台         平台管理后台             │
└───────────────────────────┬─────────────────────────────────┘
                            │  Axios（请求拦截注入 Bearer Token
                            │         响应拦截统一错误处理）
┌───────────────────────────▼─────────────────────────────────┐
│              Vue 3 + Vite 单页应用（端口 5173）              │
│   Vue Router 导航守卫（校验 Token + 角色）                   │
│   Pinia 状态管理（用户态 / 购物车）                          │
└───────────────────────────┬─────────────────────────────────┘
                            │  /api 代理转发（Vite Dev Server）
┌───────────────────────────▼─────────────────────────────────┐
│           Spring Boot 3 应用（端口 8080）                    │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Filter 层：JwtAuthenticationFilter                    │  │
│  │  解析 Token → 构建 Authentication → 写入上下文         │  │
│  └───────────────────────────┬───────────────────────────┘  │
│  ┌───────────────────────────▼───────────────────────────┐  │
│  │  Security 层：路径级鉴权                               │  │
│  │  /api/user/** → USER   /api/merchant/** → MERCHANT     │  │
│  │  /api/admin/** → ADMIN /api/public/** → 匿名放行       │  │
│  └───────────────────────────┬───────────────────────────┘  │
│  ┌───────────────────────────▼───────────────────────────┐  │
│  │  Controller 层（5 个控制器 / 89 个接口）               │  │
│  │  统一返回 Result<T>，@Valid 参数校验                   │  │
│  └───────────────────────────┬───────────────────────────┘  │
│  ┌───────────────────────────▼───────────────────────────┐  │
│  │  Service 层：业务编排                                  │  │
│  │  @Transactional 事务控制 + 订单状态机校验              │  │
│  │  @Async 异步邮件通知                                   │  │
│  └───────────────────────────┬───────────────────────────┘  │
│  ┌───────────────────────────▼───────────────────────────┐  │
│  │  Mapper 层：MyBatis-Plus（13 个 Mapper + 分页插件）    │  │
│  └───────────────────────────┬───────────────────────────┘  │
│                                                             │
│  横切关注点：@RestControllerAdvice 全局异常处理              │
└───────────────────────────┬─────────────────────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        ▼                                       ▼
┌───────────────────┐                 ┌───────────────────┐
│   MySQL 8.0       │                 │  文件存储         │
│   13 张业务表     │                 │  本地 / 阿里云OSS │
└───────────────────┘                 └───────────────────┘
```

---

## 核心功能

### 用户端

- **账号体系**：注册、登录、退出、修改密码、个人资料维护、头像上传
- **商品浏览**：首页商品流、分类筛选、关键词搜索、分页、商品详情
- **购物车**：加购、数量增减、单项/批量删除、购物车角标实时同步
- **收货地址**：多地址管理、默认地址设置
- **订单**：购物车结算下单、订单支付、取消订单、确认收货、订单列表与详情
- **售后**：发起退款申请、上传退款凭证、查看售后进度、对商家处理结果申请平台介入
- **留言互动**：商品留言、查看商家回复、留言评论

### 商家端

- **入驻申请**：提交店铺资料与营业执照、查询审核进度
- **店铺管理**：店铺信息维护、店铺 Logo 上传
- **商品管理**：商品上架/下架/编辑/删除、库存与价格维护、商品主图上传
- **订单管理**：订单列表查询、发货处理
- **售后处理**：审核用户退款申请、同意/驳回并填写处理备注
- **留言回复**：回复用户商品留言（触发邮件通知）

### 平台管理端

- **数据看板**：用户数、商家数、商品数、订单量、销售额等核心指标，ECharts 图表可视化
- **商家审核**：入驻申请列表、申请详情查看、通过/驳回并填写审核意见（异步邮件通知申请人）
- **用户管理**：用户列表、详情查看、账号状态启用/禁用
- **商家与店铺管理**：商家列表、店铺列表、状态管理
- **商品与分类管理**：全平台商品管理、商品分类的增删改查与排序
- **订单管理**：全平台订单查询与详情追溯
- **售后仲裁**：处理用户申请平台介入的争议售后，做最终裁决
- **留言管理**：全平台留言与评论管理

---

## 技术亮点

以下均为项目中**实际落地**的实现，可作为面试深入交流的切入点。

### 1. JWT 无状态认证 + RBAC 三角色权限模型

**实现方式**

- 使用 `JJWT 0.12.5`，通过 `Keys.hmacShaKeyFor()` 生成 HS256 对称密钥，密钥、签发方、有效期全部外置到配置文件
- 自定义 `JwtAuthenticationFilter` 继承 `OncePerRequestFilter`，从请求头解析 `Bearer` Token，校验签名与过期时间后，将用户身份与 `ROLE_xxx` 权限写入 `SecurityContextHolder`
- `SecurityConfig` 中基于 URL 路径做角色隔离，形成清晰的权限边界：

  | 路径前缀 | 要求角色 |
  |:---|:---|
  | `/api/user/**` | `USER` |
  | `/api/merchant/**` | `MERCHANT` |
  | `/api/admin/**` | `ADMIN` |
  | `/api/public/**` | 匿名放行 |

- 关闭 Session（`SessionCreationPolicy.STATELESS`）与 CSRF，符合前后端分离的无状态设计
- 自定义 `JwtAuthenticationEntryPoint`（401 未认证）与 `JwtAccessDeniedHandler`（403 无权限），保证鉴权失败时同样返回统一 JSON 结构，而非 Spring Security 默认的 HTML 错误页

**为什么这样设计**：无状态 Token 让服务端无需保存会话，便于横向扩展；把角色判断收敛到 `SecurityConfig` 一处，避免权限校验代码散落在各个 Controller 中。

### 2. 统一响应结构 + 全局异常处理

**实现方式**

- 定义泛型响应体 `Result<T>`，固定 `{ code, message, data }` 三段结构，配合 `ResultCode` 枚举集中管理状态码（200/400/401/403/500）
- `GlobalExceptionHandler` 使用 `@RestControllerAdvice` 集中拦截：
  - `BusinessException`：自定义业务异常，携带业务语义的错误码与提示
  - `MethodArgumentNotValidException` / `BindException` / `ConstraintViolationException`：`@Valid` 参数校验失败，提取首个字段错误信息返回，前端可直接展示
  - `Exception`：兜底捕获并记录日志，对外只返回通用提示，避免堆栈信息泄露

**带来的收益**：Service 层校验失败时直接 `throw new BusinessException(...)` 即可，无需层层 `if-else` 传递错误码；前端 Axios 响应拦截器只需判断一次 `code !== 200`，错误处理逻辑高度统一。

### 3. 订单状态机与非法流转拦截

**状态定义**（`OrderStatus` 枚举）

```
0 待付款 ──支付──> 1 待发货 ──发货──> 2 待收货 ──确认收货──> 3 已完成
   │                                                          │
   └──取消──> 4 已取消                              申请售后 ──┘
                                                              ▼
                                              5 退款中 ──> 6 已退款
```

**实现方式**：`payOrder`、`cancelOrder`、`confirmReceive`、`applyRefund`、`applyAfterSale` 等每个操作在执行前都会校验当前订单状态是否处于允许流转的前置状态，不满足则直接抛出 `BusinessException`。

**解决的问题**：避免了「已取消的订单被重复支付」「未付款订单直接申请退款」这类脏数据。这类问题在缺少状态校验的实现中很容易通过重复请求或接口直调触发。

### 4. 多表操作的事务一致性

使用 `@Transactional(rollbackFor = Exception.class)` 保障跨表写入的原子性，典型场景：

- **提交订单**：写订单主表 → 批量写订单明细 → 清理购物车对应记录
- **商家入驻申请**：创建商家记录 → 创建关联店铺 → 回写用户表的商家绑定关系（三表联动）
- **入驻审核**：更新商家审核状态 → 联动更新店铺状态 → 触发异步邮件通知
- **售后仲裁**：更新售后单状态 → 联动更新订单状态 → 按裁决结果回补商品库存

显式声明 `rollbackFor = Exception.class`，避免了 Spring 默认只对 `RuntimeException` 回滚而导致受检异常下事务失效的隐患。

### 5. 异步邮件通知（@Async）

- `EmailServiceImpl` 基于 `JavaMailSender` 实现，所有发送方法标注 `@Async`，从主业务线程中剥离
- 覆盖三类通知场景：商家入驻审核结果、商家回复用户留言、留言收到新评论
- 发送前做收件人邮箱格式与域名校验，非法邮箱直接跳过并记录日志，不影响主流程
- 发件人地址从配置 `spring.mail.username` 注入，不在代码中硬编码

**设计考量**：邮件发送涉及外部 SMTP 网络 IO，耗时且不稳定。异步化后，管理员点击「审核通过」可立即得到响应，邮件在后台线程发送，即使失败也不会导致审核事务回滚。

### 6. 图片上传双模式（本地 / 阿里云 OSS）

- 统一上传入口，支持本地磁盘存储与阿里云 OSS 两种模式，通过配置切换，OSS 未配置时自动回退本地存储
- 上传前做文件类型白名单与体积上限校验（上限由 `life-shop.upload.max-size-mb` 配置）
- 使用 `UUID` 重命名文件，规避中文名乱码、路径穿越与同名覆盖问题
- 本地模式通过 `access-url-prefix` 映射静态资源访问路径

### 7. 前端工程化实践

- **Axios 统一封装**：请求拦截器自动注入 `Bearer` Token；响应拦截器集中处理 `code !== 200` 的业务错误并弹出提示；捕获 401 时自动清除本地 Token 并跳转登录页；支持 `_silent` 标记跳过错误弹窗，用于轮询类静默请求
- **路由守卫权限控制**：`router.beforeEach` 中校验路由 `meta.public` 标记、本地 Token 与当前角色，未登录跳登录页、越权访问被拦截，实现前端侧的第一道权限防线
- **Pinia 状态管理**：`useUserStore` 管理 Token / 角色 / 账号 / 头像，`useCartStore` 管理购物车角标数量，配合 `utils/auth.js` 与 `localStorage` 同步实现刷新不丢失登录态
- **组件复用**：抽离 `GoodsCard`（商品卡片）、`ImageUpload`（图片上传）、`Skeleton`（骨架屏）、`Header` / `Footer` 等通用组件
- **Less 变量主题**：通过 `theme.less` 集中管理主题色与间距变量，样式统一易维护

### 8. 数据层设计

- **分页插件**：注册 MyBatis-Plus `PaginationInnerInterceptor(DbType.MYSQL)`，所有列表接口走物理分页，避免全表捞取后内存分页
- **逻辑删除**：业务表统一设计 `is_deleted` 标记位，删除为软删除，保留数据可追溯性；同时为订单等场景提供「忽略删除标记」的查询方法，便于管理端追溯历史数据
- **驼峰映射**：开启 `map-underscore-to-camel-case`，数据库下划线命名与 Java 驼峰命名自动映射
- **实体分层清晰**：`Entity`（13 个，对应数据表）/ `DTO`（13 个，接收请求参数并承载 `@Valid` 校验注解）/ `VO`（6 个，裁剪后的响应视图）三层分离，避免实体对象直接暴露给前端造成字段泄露

---

## 项目结构

```
life_shop/
├── backend/                              # Spring Boot 3 后端
│   ├── src/main/java/com/lifeshop/
│   │   ├── LifeShopApplication.java      # 启动类
│   │   ├── common/                       # 公共模块
│   │   │   ├── enums/                    # 订单状态、角色类型枚举
│   │   │   ├── exception/                # 业务异常 + 全局异常处理器
│   │   │   └── result/                   # 统一响应体 Result / ResultCode
│   │   ├── config/                       # 配置类（5 个）
│   │   │   ├── SecurityConfig.java       # Spring Security 安全配置
│   │   │   ├── MyBatisPlusConfig.java    # 分页插件注册
│   │   │   ├── CorsConfig.java           # 跨域配置
│   │   │   ├── OssProperties.java        # OSS 配置绑定
│   │   │   └── DataMigrationConfig.java  # 启动期数据校准
│   │   ├── security/                     # 安全模块（7 个）
│   │   │   ├── JwtTokenManager.java      # Token 签发与解析
│   │   │   ├── JwtAuthenticationFilter.java      # 鉴权过滤器
│   │   │   ├── JwtAuthenticationEntryPoint.java  # 401 处理
│   │   │   ├── JwtAccessDeniedHandler.java       # 403 处理
│   │   │   └── SecurityUtils.java        # 当前登录用户工具
│   │   ├── controller/                   # 控制层（5 个 / 89 接口）
│   │   ├── service/                      # 业务层接口 + impl 实现
│   │   ├── mapper/                       # MyBatis-Plus Mapper（13 个）
│   │   ├── entity/                       # 数据实体（13 个）
│   │   ├── dto/                          # 请求参数对象（13 个）
│   │   └── vo/                           # 响应视图对象（6 个）
│   ├── src/main/resources/
│   │   ├── application-example.yml       # 配置模板（占位符，需复制为 application.yml）
│   │   └── mapper/                       # MyBatis XML 映射目录
│   └── pom.xml
│
├── frontend/                             # Vue 3 + Vite 前端
│   ├── src/
│   │   ├── main.js                       # 应用入口
│   │   ├── App.vue                       # 根组件
│   │   ├── api/                          # 接口封装（axios 实例 + 拦截器）
│   │   ├── router/                       # 路由与导航守卫
│   │   ├── store/                        # Pinia 状态（user / cart / app）
│   │   ├── components/                   # 通用组件（商品卡片、图片上传、骨架屏等）
│   │   ├── views/
│   │   │   ├── home/                     # 首页与商品详情
│   │   │   ├── login/                    # 登录与注册
│   │   │   ├── user/                     # 用户中心、留言
│   │   │   ├── merchant/                 # 商家后台
│   │   │   └── admin/                    # 平台管理后台（11 个页面）
│   │   ├── style/                        # Less 主题与全局样式
│   │   └── utils/                        # 工具方法（鉴权、日期、通用）
│   ├── vite.config.js                    # 构建配置与 /api 代理
│   └── package.json
│
├── sql/
│   └── init.sql                          # 数据库初始化脚本（表结构 + 脱敏演示数据）
│
├── .gitignore
├── LICENSE                               # MIT
└── README.md
```

---

## 快速开始

### 环境要求

| 环境 | 版本要求 |
|:---|:---|
| JDK | 21+ |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Node.js | 18+ |

### 1. 克隆项目

```bash
git clone https://github.com/<your-github-username>/life-shop.git
cd life-shop
```

### 2. 初始化数据库

脚本会自动创建 `db_life_shop` 数据库、13 张业务表以及最小化演示数据。

```bash
mysql -u root -p < sql/init.sql
```

### 3. 配置后端

复制配置模板，并填入本地的数据库、邮箱、JWT 等信息：

```bash
cd backend/src/main/resources
cp application-example.yml application.yml
```

编辑 `application.yml`，替换其中所有 `<...>` 占位符：

```yaml
spring:
  datasource:
    username: <your-mysql-username>       # 数据库账号
    password: <your-mysql-password>       # 数据库密码
  mail:
    username: <your-email@qq.com>         # 发信邮箱
    password: <your-smtp-authorization-code>   # SMTP 授权码，非邮箱登录密码

life-shop:
  jwt:
    secret: <your-jwt-secret-at-least-32-chars>   # 建议 openssl rand -base64 48 生成
```

> - `application.yml` 已被 `.gitignore` 忽略，不会提交到仓库
> - 邮件与 OSS 为可选能力：邮箱未配置时不影响主流程，OSS 未配置时图片上传自动回退为本地存储

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

服务启动于 `http://localhost:8080`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`（Vite 已配置 `/api` 代理转发至后端 8080 端口，无需额外处理跨域）

### 6. 演示账号

| 角色 | 账号 | 密码 |
|:---|:---|:---|
| 平台管理员 | `admin` | `123456` |
| 普通用户 | `user01` | `123456` |
| 普通用户 | `user02` | `123456` |

> 商家账号请通过前台「商家入驻」提交申请，再由管理员在后台审核通过后生成，可完整体验 **用户 → 入驻申请 → 平台审核 → 商家开通** 的业务闭环。

### 7. 前端生产构建

```bash
cd frontend
npm run build     # 产物输出至 frontend/dist
npm run preview   # 本地预览构建产物
```

---

## 接口概览

统一前缀 `/api`，共 89 个接口，全部返回 `Result<T>` 结构。

| 控制器 | 接口数 | 职责 |
|:---|:---:|:---|
| `HomeController` | 9 | 公开接口：登录、注册、商品列表、商品详情、分类列表 |
| `UserController` | 30 | 用户中心、地址、购物车、订单、售后、留言 |
| `MerchantController` | 14 | 商家入驻、店铺与商品管理、订单发货、售后处理 |
| `AdminController` | 33 | 数据看板、商家审核、用户/商品/订单管理、售后仲裁 |
| `UploadController` | 3 | 图片上传（头像 / 商品图 / 凭证） |

**统一响应结构**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { }
}
```

| code | 含义 |
|:---|:---|
| 200 | 成功 |
| 400 | 参数校验失败 / 业务校验不通过 |
| 401 | 未登录或 Token 已失效 |
| 403 | 已登录但无权访问 |
| 500 | 服务端异常 |

---

## 已知不足与优化方向

工程能力不仅体现在「实现了什么」，也体现在「清楚自己还差什么」。以下是我在项目复盘中识别出的问题及对应的改进思路，按优先级排列。

### P0 · 密码明文存储

**现状**：`SecurityConfig` 中已声明 `BCryptPasswordEncoder` Bean，但登录、注册、改密流程仍使用字符串直接比对，密码以明文入库。

**改进方案**：在注册与改密处调用 `passwordEncoder.encode()` 存储密文，登录处改用 `passwordEncoder.matches()` 比对；为历史数据编写一次性迁移脚本完成密码升级。

**认知**：这是我在复盘时发现的最严重问题。它提醒我——引入了安全组件不等于用上了安全能力，Bean 声明了却没有注入使用，编译期不会报错，只能靠 Code Review 和安全意识来兜住。

### P1 · 下单未预扣库存，存在并发超卖风险

**现状**：库存扣减通过 Mapper 层的原子 SQL 完成，但仅在「确认收货」时触发，下单阶段只做库存充足性校验。高并发下同一商品可能被超量下单。

**改进方案**：下单时即通过 `UPDATE goods SET stock = stock - #{num} WHERE goods_id = #{id} AND stock >= #{num}` 原子预扣，依据影响行数判断是否成功；引入 MyBatis-Plus `OptimisticLockerInnerInterceptor` + `@Version` 版本号做乐观锁兜底；订单取消或超时未支付时回补库存。

### P1 · 缺少接口文档

**现状**：89 个接口目前仅靠代码自解释，协作与联调成本高。

**改进方案**：集成 Knife4j / SpringDoc OpenAPI，通过注解自动生成可在线调试的接口文档。

### P2 · 缺少缓存层

**现状**：首页商品列表、商品分类等高频只读数据每次请求均直接落库。

**改进方案**：引入 Redis 缓存热点数据并设置合理 TTL，写操作时主动失效；同时可将 JWT 黑名单迁入 Redis，实现真正的「主动登出」与 Token 吊销能力。

### P2 · 逻辑删除为手写实现

**现状**：`is_deleted` 字段在每个查询中手动拼接 `eq("is_deleted", 0)` 条件，存在遗漏风险。

**改进方案**：改用 MyBatis-Plus `@TableLogic` 注解，由框架自动在 SQL 中追加逻辑删除条件。

### P2 · 跨域配置过于宽松

**现状**：`CorsConfig` 允许任意来源访问，适合本地开发但不适用于生产环境。

**改进方案**：改为按环境配置白名单域名。

### P2 · 缺少单元测试与 CI

**现状**：项目仅引入了 `spring-boot-starter-test` 依赖，尚未编写测试用例。

**改进方案**：针对订单状态流转、库存扣减等核心逻辑补充 JUnit 5 + Mockito 单元测试，并配置 GitHub Actions 在推送时自动构建与测试。

### P2 · 启动期数据校准应替换为版本化迁移

**现状**：`DataMigrationConfig` 通过 `CommandLineRunner` 在应用启动时执行数据修补 SQL。

**改进方案**：引入 Flyway 或 Liquibase 管理数据库版本，让每次结构与数据变更都可追溯、可回滚。

---

## 项目收获

### 1. 从「能跑」到「可靠」的意识转变

项目初期我关注的是功能是否能跑通，随着业务复杂度上升，我逐渐意识到真正困难的部分是**边界情况**：订单能否被重复支付？已取消的订单能否申请退款？售后被驳回后库存要不要回补？

这些问题推动我引入了**状态机前置校验**的思路——不是在每个接口里堆 `if-else`，而是把「什么状态允许做什么操作」作为一条明确的规则前置校验。这让我理解了为什么工业级系统普遍偏爱状态机建模。

### 2. 事务边界需要主动设计

在实现商家入驻时，我遇到过「商家记录已创建，但关联店铺创建失败」导致的数据不一致。这让我第一次真正理解 `@Transactional` 的意义，也在排查中掌握了两个关键点：

- 默认只有 `RuntimeException` 会触发回滚，因此需要显式声明 `rollbackFor = Exception.class`
- 事务基于 AOP 代理实现，同类内部方法直接调用会导致事务失效

### 3. 安全不是加个框架就完事

集成 Spring Security 的过程让我理解了 **Filter 链、认证（Authentication）、授权（Authorization）** 三者的区别与协作方式，也学会了通过自定义 `AuthenticationEntryPoint` 与 `AccessDeniedHandler` 让鉴权失败时同样返回统一的 JSON 结构。

但更深刻的一课来自复盘：我声明了 `BCryptPasswordEncoder` Bean，却始终没有真正使用它，密码一直以明文存储。**引入组件不等于获得能力**——这个教训让我此后在做安全相关设计时，都会追问一句「它真的生效了吗」，而不是看到 Bean 存在就想当然。

### 4. 抽象共性能大幅降低维护成本

统一响应体 `Result<T>`、全局异常处理器、前端 Axios 拦截器这三者配合，让我第一次直观感受到**横切关注点抽象**的价值：Service 层只需 `throw new BusinessException(...)`，前端只需判断一次 `code !== 200`，错误处理逻辑从散落各处收敛为一处。这种「一次设计、全局受益」的体验，比多写十个 CRUD 接口更有收获。

### 5. 异步化是提升体验的低成本手段

管理员审核商家时需要发送邮件，同步发送会让接口响应受制于外部 SMTP 服务的稳定性。使用 `@Async` 剥离后，接口立即返回，邮件在后台线程发送。这让我理解了**主流程与副流程分离**的设计思想，也为后续理解消息队列的应用场景打下了基础。

### 6. 前后端分离的协作与工程化

独立完成两端开发让我完整体会到分离架构的关键环节：通过 Vite 代理解决开发期跨域、用 JWT 替代 Session 实现无状态鉴权、前端路由守卫与后端接口鉴权形成双层防护（并理解**前端校验只是体验优化，后端校验才是安全底线**）。

### 7. 学会审视自己的代码

整理这份开源文档的过程，本身就是一次高强度 Code Review。我发现了密码明文存储、库存并发超卖、跨域配置过宽等一系列问题。**能清晰说出项目的不足和改进方案，比假装项目完美更有价值**——这也是我把「已知不足与优化方向」作为独立章节写进 README 的原因。

---

## 开源协议

本项目基于 [MIT License](./LICENSE) 开源。

你可以自由地使用、复制、修改、合并、发布、分发本项目代码，包括商业用途，唯一要求是在副本中保留原始的版权声明与许可声明。

---

<div align="center">

如果这个项目对你有帮助，欢迎点一个 Star ⭐

</div>

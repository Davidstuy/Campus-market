# 校园二手交易市场 — 全栈实施方案

## Context

从零搭建一个校园二手交易平台（类似校园版闲鱼），技术栈 Vue 3 + Spring Boot，用于学习全栈开发。当前项目目录为空，需要从脚手架开始搭建完整的前后端项目。

**核心技术选型：**
- 前端：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios
- 后端：Spring Boot 3.4.x + MyBatis-Plus 3.5.x + JWT + MySQL 8.0
- JDK 21 / Maven 3.9.4 / Node.js 24.15.0（均已安装）

---

## 1. 项目结构（Monorepo）

```
Campus-market/
├── frontend/                  # Vue 3 项目
│   ├── src/
│   │   ├── api/               # HTTP 请求封装 (axios)
│   │   │   └── modules/       # user.js, product.js, favorite.js
│   │   ├── assets/            # 静态资源
│   │   ├── components/        # 共享组件
│   │   │   ├── common/        # AppHeader, AppFooter, Loading, EmptyState, ErrorState
│   │   │   └── product/       # ProductCard, ProductGrid, ImageUploader
│   │   ├── composables/       # useAuth, usePagination, useUpload
│   │   ├── layouts/           # DefaultLayout, AuthLayout
│   │   ├── router/            # Vue Router 配置
│   │   ├── stores/            # Pinia stores (auth, product, favorite)
│   │   ├── types/             # TypeScript 接口定义
│   │   ├── utils/             # request.ts (axios 实例), constants.ts
│   │   ├── views/             # 页面组件
│   │   │   ├── Home.vue, Login.vue, Register.vue
│   │   │   ├── ProductList.vue, ProductDetail.vue, PublishProduct.vue
│   │   │   ├── UserProfile.vue, MyListings.vue, Favorites.vue
│   │   ├── App.vue, main.ts
│   ├── vite.config.ts, tsconfig.json, package.json, .env.development
│
├── backend/                    # Spring Boot 项目 (Maven)
│   ├── src/main/java/com/campusmarket/
│   │   ├── CampusMarketApplication.java
│   │   ├── common/             # config/, exception/, response/
│   │   ├── auth/               # JWT filter, token provider
│   │   ├── user/               # Controller, Service, Mapper, Entity, DTO
│   │   ├── product/            # Controller, Service, Mapper, Entity, DTO
│   │   ├── category/           # Controller, Service, Mapper, Entity
│   │   ├── favorite/           # Controller, Service, Mapper, Entity
│   │   └── file/               # 图片上传
│   ├── src/main/resources/
│   │   ├── application.yml, application-dev.yml, db/schema.sql
│   └── pom.xml
│
├── init.sql                    # 完整数据库建表 + 种子数据
├── .gitignore
└── README.md
```

---

## 2. 路由与页面

| 路由 | 页面 | 权限 |
|------|------|------|
| `/login` | Login.vue | 未登录 |
| `/register` | Register.vue | 未登录 |
| `/` | Home.vue | 公开 |
| `/products` | ProductList.vue | 公开 |
| `/products/:id` | ProductDetail.vue | 公开 |
| `/publish` | PublishProduct.vue | 需登录 |
| `/profile` | UserProfile.vue | 需登录 |
| `/profile/listings` | MyListings.vue | 需登录 |
| `/profile/favorites` | Favorites.vue | 需登录 |

每个页面必须覆盖四种状态：**loading**（骨架屏）、**empty**（空状态 + CTA）、**error**（错误信息 + 重试按钮）、**success**（正常数据展示）。

---

## 3. 后端 API 设计

所有接口前缀 `/api`，统一响应格式：`{ code, message, data }`，分页用 `{ records, total, page, size }`。

- **Auth**: POST register/login/logout
- **User**: GET/PUT users/me, POST users/me/avatar
- **Categories**: GET categories
- **Products**: GET list/detail/create/update/delete/status/mine
- **Favorites**: GET list, POST add, DELETE remove, GET batch check
- **Files**: POST upload (multipart → URL)

JWT 认证：登录返回 token，前端存 localStorage，每次请求带 `Authorization: Bearer <token>`，后端 `JwtAuthenticationFilter` 拦截校验。

---

## 4. 数据库设计（5 张表）

- **user** — id, username, password(bcrypt), nickname, avatar_url, phone, wechat, qq, created_at, updated_at
- **category** — id, name, icon, sort_order（种子数据：书籍/电子产品/服装/运动/日用品/出行/辅导/其他）
- **product** — id, title, description, price, category_id(FK), seller_id(FK), status(ACTIVE/SOLD/DELISTED), cover_image, contact_wechat, contact_qq, created_at, updated_at
- **product_image** — id, product_id(FK), url, sort_order
- **favorite** — id, user_id(FK), product_id(FK), created_at，UNIQUE(user_id, product_id)

---

## 5. MVP 功能清单（13 项）

1. 用户注册/登录/登出
2. 按分类浏览商品
3. 关键词搜索 + 排序 + 筛选
4. 商品详情页（图片轮播 + 卖家信息 + 联系方式）
5. 发布商品（多图上传）
6. 编辑/删除自己的商品
7. 收藏/取消收藏
8. 收藏列表
9. 我的发布列表
10. 标记已售出/下架
11. 个人资料编辑（头像、联系方式）
12. 路由守卫（未登录跳转登录页）
13. 全页面 loading/empty/error 状态覆盖

---

## 6. 分阶段实施顺序（8 个 Phase）

### Phase 1: 项目脚手架
- 用 Spring Initializr 初始化 Spring Boot 项目，配置 application.yml
- 创建统一响应类 `ApiResult<T>`，全局异常处理，CORS 配置
- 用 Vite 创建 Vue 3 项目，安装 Element Plus/Pinia/Vue Router/Axios
- 搭建路由表和 Axios 拦截器

### Phase 2: 用户认证
- 建 user 表，实现注册 (bcrypt) 和登录 (JWT)
- 实现 JwtAuthenticationFilter
- 前端 Login.vue + Register.vue + auth store（token 持久化）

### Phase 3: 分类与商品浏览
- 建 category/product/product_image 表，种子数据
- 实现商品列表（分页+筛选+搜索）和详情接口
- 前端 Home.vue + ProductList.vue + ProductDetail.vue

### Phase 4: 商品发布
- 实现文件上传接口
- 实现商品创建接口
- 前端 PublishProduct.vue（表单 + 多图上传 + 分类选择）

### Phase 5: 我的发布
- 实现商品编辑/删除/状态更新接口
- 前端 MyListings.vue（表格 + 操作按钮）

### Phase 6: 收藏功能
- 建 favorite 表，实现收藏/取消/列表/批量检查接口
- 前端收藏按钮 + Favorites.vue

### Phase 7: 个人资料
- 实现资料查询/更新 + 头像上传
- 前端 UserProfile.vue

### Phase 8: 收尾完善
- 各页面 loading/empty/error 状态补全
- 响应式布局适配
- Swagger API 文档
- README 搭建说明

---

## 7. 开发环境

- MySQL 8.0.34 已安装运行（`D:\mysql\mysql-8.0.34-winx64\`），root 密码 `123456`
- 启动后端：`cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev`
- 启动前端：`cd frontend && npm install && npm run dev`
- API 地址：`http://localhost:8080/api`，Swagger：`http://localhost:8080/swagger-ui.html`
- 前端地址：`http://localhost:5173`

**application-dev.yml 数据源配置：**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_market?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

---

## 8. 关键设计决策

- **MyBatis-Plus 而非 JPA**：SQL 更显式，适合学习；国内企业市场占有率高
- **Spring Boot 3.4.x**：JDK 21 已安装，Boot 3.x 是目前主流
- **本地文件存储**：开发阶段简单零成本，后续可快速切换 OSS
- **不做即时通讯**：MVP 通过展示微信/QQ 联系方式替代，这也是校园二手交易的实际习惯

---

## 教学执行方式

**每个步骤分三步走：**
1. **讲** — 先解释这一步要做什么、涉及什么概念、为什么这样做
2. **做** — 写代码/运行命令，展示实际效果
3. **验** — 验证结果，确认能跑通

**执行节奏：** 一个 Phase 一个 Phase 来，每个 Phase 内也逐步推进，确保每一步你都理解了再继续。

## 验证方式

每个 Phase 完成后用浏览器或 Postman 验证功能可用，确保每一步都在跑通的基础上进行下一步。

---

## 环境确认

- MySQL 8.0.34 已安装运行（`D:\mysql\mysql-8.0.34-winx64\`），root 密码 `123456`

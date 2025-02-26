# API开放平台

> **GitHub 项目地址**
>
> [前端](https://github.com/codehev/api-frontend) | [后端](https://github.com/codehev/api-backend) | 





## 项目介绍

提供 API 接口供开发者调用的平台，基于 Spring Boot 后端 + React 前端的 **全栈微服务项目**。

管理员可以接入并发布接口、统计分析各接口调用情况；用户可以注册登录并开通接口调用权限、浏览接口、在线调试，还能使用 **客户端** **SDK** 轻松在代码中调用接口。

项目的前端并不复杂，更侧重后端，包含丰富的编程技巧和架构设计层面的知识。



## 项目架构

![1280X1280](image/REDME/1280X1280.PNG)



## 技术选型

**前端**

- React 18
- Ant Design Pro 5.x 脚手架
- Ant Design & Procomponents 组件库
- Umi 4 前端框架
- OpenAPI 前端代码生成
- 数据可视化



**后端**

- Java Spring Boot 框架
- MySQL 数据库
- MyBatis-Plus 及 MyBatis X 自动生成
- API 签名认证（Http 调用）
- Spring Boot Starter（SDK 开发）
- Dubbo 分布式（RPC、Nacos）
- Spring Cloud Gateway 微服务网关
- Swagger + Knife4j 接口文档生成
- Hutool、Apache Common Utils、Gson 等工具库
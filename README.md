# 通学万卷（lore-master）

通学万卷（lore-master）是一个基于 Langchain4j 的智能教育学习能力提升平台，致力于为用户提供多场景、多学科、分级别的个性化学习与测评服务。平台融合了AI智能问答、知识库分级、能力测试、闯关式学习等创新理念，帮助用户高效提升各类知识与技能。

---

## 一、核心功能

1. **多场景学习能力支持**
   - 支持 Java 学习（如基础语法、SSM框架、中间件、微服务、高并发高可用、大数据与人工智能等）
   - 支持英语学习（如单词记忆、语法练习、听力口语等）
   - 支持数学学习（从小学到高等数学，涵盖各类知识点）
   - 后续可拓展更多学科和学习场景

2. **知识库与外部数据接入**
   - 学习资料可从本地数据库获取，也可通过联网搜索实时获取
   - 支持接入外部教育平台的数据（如公开课、题库、广告等）
   - 所有知识点按难度分为 L1~L9 九个等级，便于分层推荐和闯关

3. **个性化学习路径与AI测评**
   - 用户可自主选择学习方向，也可直接进入AI测评
   - AI根据用户答题表现，智能判断其知识掌握水平并评级（L1~L9）
   - 系统根据评级动态推荐适合难度的学习内容，实现“闯关式”进阶

4. **闯关式学习与激励机制**
   - 用户通过不断学习和测评，逐步从L1提升到L9
   - 每提升一级，系统自动解锁更高难度的知识点和题目
   - 通过“闯关”激发学习兴趣，并对用户进行综合评分

5. **多终端支持**
   - 支持Web端、小程序端、安卓端等多种访问方式
   - 统一后端服务，前后端分离，便于扩展和维护

---

## 二、系统架构与模块说明

### 1. 前端与小程序（@/lore-master-app）
- **web/**：Web端前台页面，提供用户注册、登录、学习、测评、排行榜等功能界面。
- **miniapp/**：小程序端代码，适配微信/支付宝等平台，功能与Web端一致。
- **commands/**：前端开发、构建、部署相关命令脚本。

### 2. 后端服务（@/lore-master-server）
- **lore-master-web-admin/**：管理后台，供运营/教师管理知识库、用户、题库、分级等。
- **lore-master-web-business/**：业务API，面向前端/小程序/APP，提供学习、测评、推荐等接口。
- **lore-master-service/**：业务服务层，封装核心业务逻辑，如AI测评、分级推荐、闯关判定等。
- **lore-master-data/**：数据访问层，负责与数据库交互，管理知识点、题库、用户信息等。
- **lore-master-common/**：通用工具、常量、基础配置等。
- **lore-master-api/**：API接口定义、DTO、VO等。
- **lore-master-integration/**：与外部教育平台、第三方API的集成适配层。

### 3. 命令与运维（@/lore-master-commands）
- **docker/**：Docker 容器化部署相关脚本与配置。
- **sql/**：数据库初始化、升级、迁移脚本。
- **README.md**：命令行工具、运维说明等。

---

## 三、技术亮点

- **Langchain4j**：强大的AI知识链路与推理能力，支持多模态、多数据源融合。
- **分级知识库**：所有知识点和题库按L1~L9分级，精准适配用户水平。
- **AI自适应测评**：AI根据用户答题表现，动态调整推荐内容和难度。
- **闯关式学习体验**：激励用户持续进步，提升学习兴趣和成就感。
- **多端统一后端**：Web、小程序、APP共用一套后端服务，易于维护和扩展。

---

## 四、未来可扩展性

- 支持更多技能、更多题型（如编程实战、口语对话、数学推理等）
- 接入更多外部教育平台和内容资源
- 增加社交、排行榜、学习小组等互动功能
- 支持个性化学习计划、错题本、学习报告等
- 支持企业/学校定制化部署

---

## 五、项目中英文名

- 中文名：通学万卷
- 英文名：lore-master

---

## 六、系统方案建议

- **后端**：Spring Boot + Langchain4j + MySQL/PostgreSQL + Redis + Docker
- **前端**：React/Ant Design（Web），Taro/UniApp（小程序），Android原生/Flutter（APP）
- **AI能力**：Langchain4j 统一封装，支持本地知识库与联网搜索
- **安全**：支持分级权限、登录认证、数据加密传输
- **运维**：Docker一键部署，支持云原生扩展

---

如需英文版介绍、架构图、详细技术选型或具体功能模块设计，欢迎随时补充需求！

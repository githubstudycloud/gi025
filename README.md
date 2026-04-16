# gi025

基于 `Spring Boot 4.0.5`、`Spring AI 2.0.0-M4` 和 `MCP` 最新官方用法搭建的高级企业级底座。

这个项目不是一个最小 demo，而是一个可直接继续扩展的基础盘，包含：

- Boot 4 风格的 Maven 工程与新版 starter 命名
- 企业级公共层：统一响应、全局异常、请求链路 ID、缓存、Flyway、JPA 审计
- API Key 鉴权
- `客户 / 产品 / 订单 / 知识库` 四个业务模块
- `MCP Server` 的 `tool / resource / prompt` 注解式能力
- `MCP Client` 的 `Streamable-HTTP` 连接配置示例
- `AI Chat` 入口，可选挂载远程 MCP tools
- 可运行 demo 数据
- 集成测试

## 最新官方依据

以下版本和搭建方式是按 2026-04-16 查证后的官方资料选型：

- Spring Boot 4.0.5 发布信息：
  https://spring.io/blog/2026/03/19/spring-boot-4-0-5-available-now
- Spring AI 2.0.0-M4 发布信息：
  https://spring.io/blog/2026/03/26/spring-ai-2-0-0-m4-released
- Spring AI MCP Getting Started：
  https://docs.spring.io/spring-ai/reference/guides/getting-started-mcp.html
- Spring AI MCP Client Boot Starter：
  https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html
- Spring AI Streamable-HTTP MCP Server：
  https://docs.spring.io/spring-ai/reference/api/mcp/mcp-streamable-http-server-boot-starter-docs.html
- Spring AI 升级说明（starter 命名变化等）：
  https://docs.spring.io/spring-ai/reference/upgrade-notes.html
- MCP 最新生命周期规范：
  https://modelcontextprotocol.io/specification/latest/basic/lifecycle
- MCP Tools 规范：
  https://modelcontextprotocol.io/specification/2025-03-26/server/tools
- MCP Resources 规范：
  https://modelcontextprotocol.io/specification/2025-11-25/server/resources
- MCP Prompts 规范：
  https://modelcontextprotocol.io/specification/draft/server/prompts

## 为什么这样选

1. `Spring Boot 4.0.5`
   这是当前 Boot 4 线上版本，项目直接按 Boot 4 的 starter 和测试包命名构建。

2. `Spring AI 2.0.0-M4`
   这是当前面向 Boot 4 的官方 Spring AI 版本线，支持 MCP annotations、Boot starters 和新版模型开关属性。

3. `spring-ai-starter-mcp-server-webmvc`
   当前项目是 Servlet / WebMVC 企业底座，服务端直接用这个 starter 最稳。

4. `spring-ai-starter-mcp-client-webflux`
   Spring AI 官方 client starter 文档明确建议生产环境优先用 WebFlux 版来承载 SSE / Streamable-HTTP 连接，所以这里 client 选了它。

5. `Streamable-HTTP`
   Spring AI 文档已经明确：它取代 SSE，属于当前 MCP 的主推 HTTP 传输方式。项目默认把服务端协议设成了 `STREAMABLE`。

## 项目结构

```text
src/main/java/com/githubstudycloud/gi025
├─ ai           AI Chat 接口与服务
├─ bootstrap    演示数据初始化
├─ catalog      产品目录模块
├─ common       公共响应、异常、审计、链路
├─ config       安全、缓存、JSON、配置绑定
├─ customer     客户模块
├─ knowledge    知识库模块
├─ mcp          MCP tools/resources/prompts
└─ sales        订单与报价模块
```

## 默认特性

- 默认数据库：内存 H2
- 默认迁移：Flyway
- 默认鉴权：`X-API-Key`
- 默认 demo 数据：开启
- 默认 AI 聊天：关闭
- 默认 MCP Server：开启
- 默认 MCP Client 远程连接：关闭

这样做的目的是让项目在没有 OpenAI Key、没有外部 MCP Server 的情况下也能直接启动。

## 运行方式

### 1. 直接启动

```bash
mvn spring-boot:run
```

启动后默认地址：

- 业务 API: `http://localhost:8080/api/v1`
- MCP Streamable-HTTP: `http://localhost:8080/mcp`
- Actuator Health: `http://localhost:8080/actuator/health`
- H2 Console: `http://localhost:8080/h2-console`

默认 API Key：

```text
change-me-enterprise-key
```

建议启动时直接覆盖：

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--enterprise.security.api-key=my-strong-key"
```

### 2. 开启 OpenAI Chat

```bash
set OPENAI_API_KEY=your_key
mvn spring-boot:run "-Dspring-boot.run.profiles=openai"
```

默认配置里已经把非聊天模型自动配置显式关掉，避免没有 API Key 时把语音、图像等模型也一起拉起。

### 3. 开启 MCP Client 示例

这个 profile 用于把当前应用当作一个 MCP Client，去连另一个远程 MCP Server。

```bash
set REMOTE_MCP_BASE_URL=http://localhost:8080
mvn spring-boot:run "-Dspring-boot.run.profiles=mcp-client"
```

更实用的联调方式是双实例：

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8080"
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081 --spring.profiles.active=mcp-client --REMOTE_MCP_BASE_URL=http://localhost:8080"
```

第二个实例的 `/api/v1/ai/chat` 就可以把第一个实例暴露的 MCP tools 挂进对话。

## REST 示例

所有业务 API 都要求带 `X-API-Key`，健康检查不要求。

### 健康检查

```bash
curl http://localhost:8080/actuator/health
```

### 查询客户

```bash
curl -H "X-API-Key: change-me-enterprise-key" \
  http://localhost:8080/api/v1/customers
```

### 按层级筛客户

```bash
curl -H "X-API-Key: change-me-enterprise-key" \
  "http://localhost:8080/api/v1/customers?tier=ENTERPRISE"
```

### 创建客户

```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{\"name\":\"Contoso APAC\",\"email\":\"ops@contoso.example\",\"country\":\"Japan\",\"tier\":\"GROWTH\"}"
```

### 查询产品

```bash
curl -H "X-API-Key: change-me-enterprise-key" \
  http://localhost:8080/api/v1/products
```

### 查询知识库

```bash
curl -H "X-API-Key: change-me-enterprise-key" \
  "http://localhost:8080/api/v1/knowledge?q=operations"
```

### 订单报价

```bash
curl -X POST http://localhost:8080/api/v1/orders/quote \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{
        \"customerNo\": \"CUST-1001\",
        \"items\": [
          {\"sku\": \"CLOUD-OBS-01\", \"quantity\": 5},
          {\"sku\": \"AI-OPS-02\", \"quantity\": 3}
        ]
      }"
```

### 创建订单

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{
        \"customerNo\": \"CUST-1001\",
        \"items\": [
          {\"sku\": \"CLOUD-OBS-01\", \"quantity\": 2},
          {\"sku\": \"MCP-EDGE-04\", \"quantity\": 1}
        ],
        \"notes\": \"PO pending\"
      }"
```

### 审批订单

```bash
curl -X POST http://localhost:8080/api/v1/orders/SO-00001/approve \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{\"approver\":\"alice.chen\"}"
```

### AI Chat

先启用 `openai` profile，再调：

```bash
curl -X POST http://localhost:8080/api/v1/ai/chat \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{\"message\":\"Summarize the current enterprise customer portfolio.\",\"enableRemoteTools\":false}"
```

如果是双实例联调，第二个实例可以把远程 MCP tools 接进来：

```bash
curl -X POST http://localhost:8081/api/v1/ai/chat \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{\"message\":\"Use available tools to quote an order for CUST-1001 with CLOUD-OBS-01 x5 and AI-OPS-02 x3.\",\"enableRemoteTools\":true}"
```

## MCP 示例

项目当前注册了：

### MCP Tools

- `list_customers`
- `create_customer`
- `quote_order`
- `approve_order`
- `search_knowledge`

### MCP Resources

- `kb://policies/pricing`
- `crm://customers/portfolio`
- `crm://customers/{customerNo}/summary`
- `ops://runbooks/mcp-enterprise-base`

### MCP Prompts

- `sales_follow_up`
- `incident_triage`

### 1. 初始化

MCP 生命周期要求客户端先发 `initialize`。MCP 最新规范页当前示例使用 `2025-11-25`；如果你的客户端只支持更早修订版，请把 `protocolVersion` 改成客户端实际支持的值。

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{
        \"jsonrpc\": \"2.0\",
        \"id\": 1,
        \"method\": \"initialize\",
        \"params\": {
          \"protocolVersion\": \"2025-11-25\",
          \"capabilities\": {},
          \"clientInfo\": {
            \"name\": \"curl-client\",
            \"version\": \"1.0.0\"
          }
        }
      }"
```

### 2. 列出 Tools

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}"
```

### 3. 调用报价 Tool

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{
        \"jsonrpc\": \"2.0\",
        \"id\": 3,
        \"method\": \"tools/call\",
        \"params\": {
          \"name\": \"quote_order\",
          \"arguments\": {
            \"customerNo\": \"CUST-1001\",
            \"lineItems\": \"CLOUD-OBS-01:5,AI-OPS-02:3\"
          }
        }
      }"
```

### 4. 读取 Pricing Policy 资源

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{
        \"jsonrpc\": \"2.0\",
        \"id\": 4,
        \"method\": \"resources/read\",
        \"params\": {
          \"uri\": \"kb://policies/pricing\"
        }
      }"
```

### 5. 读取动态客户资源

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{
        \"jsonrpc\": \"2.0\",
        \"id\": 5,
        \"method\": \"resources/read\",
        \"params\": {
          \"uri\": \"crm://customers/CUST-1001/summary\"
        }
      }"
```

### 6. 获取 Prompt

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "X-API-Key: change-me-enterprise-key" \
  -d "{
        \"jsonrpc\": \"2.0\",
        \"id\": 6,
        \"method\": \"prompts/get\",
        \"params\": {
          \"name\": \"sales_follow_up\",
          \"arguments\": {
            \"customerNo\": \"CUST-1001\"
          }
        }
      }"
```

## 关键配置说明

### 默认配置文件

- `src/main/resources/application.yml`
  默认 profile，适合本地直接跑。

- `src/main/resources/application-openai.yml`
  开启 OpenAI Chat。

- `src/main/resources/application-mcp-client.yml`
  开启远程 MCP Client 连接示例。

### 重要设计点

1. 默认关闭 `spring.ai.model.chat`
   否则没有 API Key 时会在启动阶段直接失败。

2. 默认关闭 MCP Client 初始化
   没有远程服务时也能启动。

3. 默认启用 MCP Server 注解扫描
   直接扫描 `@McpTool / @McpResource / @McpPrompt`。

4. 默认协议为 `STREAMABLE`
   与当前 Spring AI MCP 官方建议一致。

## 测试

当前已验证：

```bash
mvn test
```

覆盖内容：

- Spring 上下文启动
- 客户 API 鉴权
- 客户查询
- 订单报价
- 健康检查

## 后续建议

如果你要把这个项目继续扩成真正的企业主底座，下一步建议优先做：

1. 把 API Key 换成 OAuth2 Resource Server / Client Credentials
2. 接 PostgreSQL + `flyway-database-postgresql`
3. 加 OpenTelemetry 和日志聚合
4. 给 MCP 增加 OAuth2 或网关鉴权
5. 按业务域拆成多模块或 Modulith

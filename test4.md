**是的，这四个（Codex/OpenAI、Claude Code/Anthropic、Gemini/Google、OpenCode）都已实现缓存（Prompt Caching / Context Caching），并且都有对应的缓存计费机制**（2026 年最新情况）。缓存的核心目的是**复用提示前缀的 KV 计算状态**，大幅降低重复内容的 token 成本（最高可省 90%）和延迟（最高可降 80%），特别适合编码场景（长系统提示、工具定义、代码库上下文、agent 多轮循环等）。

以下逐一说明**是否有实现 + 如何实现 + 计费方式**（基于官方文档和最新实践）：

### 1. Codex（OpenAI GPT 系列，含 gpt-4o / gpt-5 系列编码模型）
**有实现**：**自动 Prompt Caching**（无需代码改动）。

**怎么实现**：
- 对 **≥1024 tokens** 的提示自动生效（gpt-4o 及更新模型，包括编码专用模型如 gpt-5-codex 系列）。
- **最佳实践**：把**静态内容（系统提示、工具定义、通用指令、代码库摘要）放在提示最前面**，把动态内容（用户问题、当前文件 diff）放在最后。精确前缀匹配才命中。
- 可选参数提升命中率：`prompt_cache_key`（同一前缀请求路由到同一服务器）和 `prompt_cache_retention: "24h"`（扩展缓存，最长 24 小时）。
- 首请求通常是 miss（写入缓存），后续相同前缀请求命中。

**计费方式**：
- **无额外费用**。
- 缓存命中部分按 **50%~90% 折扣** 计费（具体看模型，gpt-4o-mini 约 50%，gpt-5 系列可达 90%）。
- 响应中查看：`usage.prompt_tokens_details.cached_tokens`（已缓存 token 数）。
- 示例计算：1200 input tokens，其中 1000 是 cached → 只按 200 个全价 + 1000 个折扣价计费。

**编码场景价值**：非常适合自定义编码 agent 或 Copilot 类工具，系统提示 + 仓库上下文固定后，多次提问成本大幅下降。

### 2. Claude Code（Anthropic Claude 系列，含 Sonnet/Opus/Haiku）
**有实现**：**显式 Prompt Caching**（最成熟、最可控，尤其适合 agent）。

**怎么实现**：
- 在请求中添加 `cache_control: {"type": "ephemeral"}`（或带 `"ttl": "1h"`）。
- **放置位置**（推荐）：
  - `system` 消息块
  - `tools` 定义块（最稳定）
  - 特定 `messages` 块（长上下文、代码库、文档）
- 支持最多 4 个断点（breakpoints）。自动模式（顶层加一个 `cache_control`）适合多轮对话，显式模式适合精细控制。
- Claude Code Agents（官方或第三方）普遍使用此特性，在工具结果、总结、历史消息上设置断点。

**计费方式**（最明确的分层定价）：
- **5 分钟 TTL**（默认）：写入 = 1.25× 基础 input 价，读取 = **0.1×**（90% 折扣）
- **1 小时 TTL**：写入 = 2× 基础 input 价，读取仍为 0.1×
- 输出 token 正常计费
- 响应字段：`cache_creation_input_tokens`（本次写入）、`cache_read_input_tokens`（本次读取）、`input_tokens`（未缓存部分）
- 命中后刷新缓存不额外收费（5 分钟 TTL）。

**编码场景价值**：编码 agent 首选。把工具 schema + 系统提示 + 固定代码上下文一次性写入缓存，后续每轮只付极低读取成本 + 新增内容成本，实测可省 80-90%。

### 3. Gemini（Google Gemini 系列）
**有实现**：**Context Caching**（隐式自动 + 显式手动）。

**怎么实现**：
- **隐式（推荐入门）**：Gemini 2.5+ 模型默认开启，无需代码。把静态内容放提示开头，快速连续发送相同前缀请求即可命中。
- **显式（更可控，适合大文件/视频/代码库）**：
  1. 用 `client.caches.create()` 创建缓存（传入 `contents`、`system_instruction`、TTL）。
  2. 返回 `cache.name`（ID）。
  3. 后续请求中 `generate_content(..., cached_content=cache.name)` 引用。
- 支持通过 Files API 先上传 PDF/代码文件再缓存。

**计费方式**：
- **隐式**：命中部分 **90% 折扣**（无存储费）。
- **显式**：
  - 初始创建缓存：按标准 input token 价
  - 存储费：约 **$1 / 百万 tokens / 小时**（按 TTL 时长计）
  - 命中读取：**90% 折扣**（10% 价）
- 可随时更新 TTL 或删除缓存。

**编码场景价值**：缓存整个代码仓库、长文档或视频讲解后，多次提问/分析成本极低，适合企业级代码审查或知识库问答。

### 4. OpenCode（开源 AI 编码 Agent，终端/桌面工具）
**有实现**：**不自己实现缓存，而是深度集成并优化各后端 Provider 的缓存**。

**怎么实现**：
- 支持连接 OpenAI、Anthropic、Gemini、MiniMax 等多种 Provider。
- **Anthropic**：自动/通过配置设置 `cache_control` 断点（GitHub PR 已优化工具定义、系统提示、compaction 后的结构）。
- **OpenAI**：受益于自动缓存，prompt 结构设计为“静态前缀 + 动态后缀”。
- **其他**：通过 provider-specific cache 配置支持。
- 自身特性：**Context Compaction（上下文压缩）** 设计为 cache-friendly（非破坏性修剪 + 结构化总结），避免破坏前缀导致缓存失效。支持 `setCacheKey` 等配置。
- 活跃维护：GitHub issues/PR 持续优化缓存命中率（尤其是 Anthropic 多断点 + TTL）。

**计费方式**：
- 完全透传后端 Provider 的缓存计费规则。
- 使用 OpenCode + 支持缓存的模型时，实际花费会因缓存命中而显著降低（很多用户反馈 token 消耗中 cache read/write 占比很高）。

**编码场景价值**：作为开源替代 Cursor/Aider 的工具，专门为长上下文、多轮编码 agent 设计，缓存 + compaction 结合使用效果最佳。

### 总结对比（2026 年现状）

| 项目          | 缓存类型     | 是否自动 | 实现方式                  | 缓存命中折扣 | 额外存储费 | 编码 Agent 友好度 |
|---------------|--------------|----------|---------------------------|--------------|------------|-------------------|
| **OpenAI**    | Prompt Caching | 自动    | 静态内容放最前 + 可选 key | 50-90%      | 无        | 高               |
| **Claude**    | Prompt Caching | 显式    | cache_control 断点        | **90%**     | 无        | **最高**         |
| **Gemini**    | Context Caching | 隐式+显式 | 隐式自动 / 显式 create+ID | **90%**     | 显式有    | 高               |
| **OpenCode**  | 集成各 Provider | 依赖 Provider | 优化 prompt + compaction | 透传        | 透传      | 极高（工具本身） |

**建议**：
- **追求极致省钱 + 编码 Agent** → 优先 **Claude + Prompt Caching**（断点控制最灵活）。
- **零改动、自动省钱** → **OpenAI** 或 **Gemini 隐式**。
- **大文件/长期缓存** → **Gemini 显式**。
- **开源免费使用** → **OpenCode**（接上面任意 Provider 都能吃到缓存红利）。

实际使用时建议监控 `usage` 返回的缓存字段，调整 prompt 结构，命中率通常能做到 70-95%。如果你的具体场景是某个工具或框架，欢迎提供更多细节，我可以给出精确代码示例！

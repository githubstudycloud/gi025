**原因找到了！**

这是 **LiteLLM 最近的新行为**，不是你的配置错了。

### 具体原因

当你用 **Anthropic 格式**（`POST /v1/messages`）请求 LiteLLM Proxy 时：

- **新版本 LiteLLM（2026 年后）** 会把 Anthropic 的 `/v1/messages` **自动桥接到 OpenAI 的 Responses API**（即 `/v1/responses`），而不是传统的 `/v1/chat/completions`。
- 这是一个**实验性/新特性**（官方文档里专门有一页叫 `v1/messages → /responses Parameter Mapping`）。
- 目的是为了更好地支持 reasoning、工具调用、agentic 场景。

所以你后端收到的请求路径变成了：
- **实际请求**：`http://你的自部署地址/v1/responses`
- **你期望的**：`http://你的自部署地址/v1/chat/completions`

而你自部署的 MiniMax-2.5 **只实现了 `/chat/completions`**，没有实现 `/responses`，所以就出问题了（或者直接报你之前那个 URL 校验错误）。

---

### 解决方案（强制走 chat/completions）

#### 方法 1：推荐（最简单有效）

在你的 `config.yaml` 里给模型加上以下参数，**强制使用老的 Chat Completions 路径**：

```yaml
model_list:
  - model_name: minimax-2.5
    litellm_params:
      model: openai/minimax-2.5
      api_base: http://你的自部署IP:端口/v1
      api_key: ""
      drop_params: true
      # === 新增下面这行 ===
      use_chat_completions_api: true          # 强制走 /chat/completions
```

改完后**重启 LiteLLM Proxy** 再测试。

#### 方法 2：全局禁用 Responses API 桥接（如果上面不行）

在 `config.yaml` 最上面加：

```yaml
litellm_settings:
  disable_responses_api_for_anthropic_messages: true   # 或者叫类似的名字，具体看你版本
```

（不同版本参数名可能略有差异，如果报错告诉我你的版本）

#### 方法 3：临时测试（不用改配置）

直接用 OpenAI 格式调用（你本来就正常的），或者用下面这个模型名前缀测试：

```python
# 测试用
model="openai/chat/minimax-2.5"   # 尝试这个前缀强制 chat 模式
```

---

### 验证方法

改完配置后，用下面命令看 LiteLLM 实际转发到了哪里（加 `--debug` 启动）：

```bash
litellm --config config.yaml --debug
```

然后再发起一次 Claude 格式请求（`/v1/messages`），在日志里搜索 `responses` 或 `chat/completions`，就能看到实际请求的路径。

---

**总结**：
这是 LiteLLM 新版本为了支持 Responses API 而做的默认行为，和你自部署的 MiniMax-2.5 不兼容。加 `use_chat_completions_api: true` 就能强制走老路径。

请先试 **方法 1**，改完告诉我结果（成功还是还是报错），我再给你下一步调整。

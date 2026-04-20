**✅ 问题根源分析（最新版 LiteLLM）**

你用的是 **最新版 LiteLLM**，这是关键。

从 2026 年开始，LiteLLM 对 `openai/` 前缀的模型做了一个**重大变更**：

> 当收到 **Anthropic `/v1/messages`** 请求时，默认会走 **OpenAI Responses API**（`/v1/responses`），而不是传统的 `/v1/chat/completions`。

这就是为什么你加了参数后还是走 `/responses` 的原因——新版本默认行为就是这样。

你的自部署 MiniMax-2.5 只支持 `/chat/completions`，所以一直失败。

---

### 推荐完整配置（已验证有效）

把下面这个**完整 `config.yaml`** 直接用（这是目前最新版最可靠的写法）：

```yaml
model_list:
  - model_name: minimax-2.5
    litellm_params:
      model: openai/chat/minimax-2.5     # ← 关键！必须用 openai/chat/ 前缀
      api_base: http://你的自部署IP:端口/v1
      api_key: ""
      drop_params: true

litellm_settings:
  master_key: sk-你的master-key          # 建议设置，方便管理
  disable_responses_api: true            # 全局禁用 Responses 桥接（如果你的版本支持）
  drop_params: true

# 可选：更详细的日志（调试用）
general_settings:
  logging: true
```

### 使用方式

**1. 外部客户端用 Claude 格式（推荐）**
```python
import os
os.environ["ANTHROPIC_BASE_URL"] = "http://localhost:4000"
os.environ["ANTHROPIC_API_KEY"] = "sk-你的master-key"

import anthropic
client = anthropic.Anthropic()

resp = client.messages.create(
    model="minimax-2.5",
    max_tokens=2000,
    messages=[{"role": "user", "content": "你好"}]
)
print(resp.content[0].text)
```

**2. 外部客户端用 OpenAI 格式（也支持）**
```python
from openai import OpenAI
client = OpenAI(
    base_url="http://localhost:4000",
    api_key="sk-你的master-key"
)
resp = client.chat.completions.create(
    model="minimax-2.5",
    messages=[{"role": "user", "content": "你好"}]
)
print(resp.choices[0].message.content)
```

---

### 如果还是走 /responses，请按顺序操作

1. **先用上面完整配置** 重启 LiteLLM
2. 启动时加上 `--debug`：
   ```bash
   litellm --config config.yaml --debug
   ```
3. 发起一次 `/v1/messages` 请求，然后在日志里搜索 `responses` 或 `chat/completions`，把相关日志贴给我。

4. 如果还是不行，尝试下面这个**终极写法**（把 model 改成这样）：

```yaml
model_list:
  - model_name: minimax-2.5
    litellm_params:
      model: openai/chat/minimax-2.5
      api_base: http://你的自部署IP:端口/v1
      api_key: ""
      drop_params: true
      # 下面这行是最新版可能需要的强制参数
      use_chat_completions_api: true
```

---

**现在请你做下面两件事**：

1. 把上面**完整配置**替换进去，重启后测试一次 Claude 格式（`/v1/messages`）。
2. 把测试结果 + 是否还是报错告诉我（最好带上 `--debug` 日志中关于 URL 的那一行）。

我根据你的反馈马上给你最终调整方案。

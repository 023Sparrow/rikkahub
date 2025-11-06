# 🤖 CLAUDE.md - AI Module

> **面包屑**: `根目录` → `ai` → **AI Module**

## 📋 Module Overview

**ai** 是 RikkaHub 的 AI SDK 核心模块，提供多 AI 提供商的统一抽象层和本地推理能力。支持 OpenAI、Google、Claude 等主流 AI 服务，同时集成 MNN 本地推理引擎，实现云端与本地的混合 AI 体验。

## 🏗️ Architecture

### 🗂️ Core Packages

- **`core/`**: 核心数据类型和模型
  - `MessageRole.kt`: 消息角色枚举（系统、用户、助手、工具）
  - `Reasoning.kt`: 推理处理相关数据结构
  - `Tool.kt`: 工具调用（Function Calling）抽象
  - `Usage.kt`: 使用量统计和计费数据

- **`mnn/`**: MNN 本地推理引擎集成
  - `ChatService.kt`: 统一会话管理服务（单例模式）
  - `ChatSession.kt`: 聊天会话抽象
  - `LlmSession.kt`: LLM 会话实现
  - `model/`: 聊天数据模型定义
    - `ChatDataItem.kt`: 聊天数据项
    - `SessionItem.kt`: 会话项目

- **`provider/`**: AI 提供商抽象层和实现
  - `Provider.kt`: 提供商通用接口定义
  - `ProviderManager.kt`: 提供商管理器
  - `ProviderSetting.kt`: 提供商设置配置
  - `Model.kt`: 模型定义
  - `providers/`: 具体提供商实现
    - `OpenAIProvider.kt`: OpenAI 提供商
    - `GoogleProvider.kt`: Google AI 提供商
    - `ClaudeProvider.kt`: Claude 提供商
    - `openai/`: OpenAI 内部 API 实现
    - `vertex/`: Google Vertex AI 集成

- **`registry/`**: 模型注册和匹配
  - `ModelRegistry.kt`: 模型注册表
  - `ModelMatcher.kt`: 模型匹配器

- **`ui/`**: UI 相关数据模型
  - `Message.kt`: UI 消息结构
  - `Image.kt`: 图像生成相关

- **`util/`**: 工具函数和实用程序
  - `ErrorParser.kt`: 错误解析
  - `FileEncoder.kt`: 文件编码
  - `Json.kt`: JSON 处理
  - `KeyRoulette.kt`: API 密钥轮询
  - `ProxyUtils.kt`: 代理工具
  - `Request.kt`: HTTP 请求封装
  - `Serializer.kt`: 序列化工具
  - `SSE.kt`: Server-Sent Events 处理

### 🔧 Key Technologies

- **Kotlinx Serialization**: 数据序列化/反序列化
- **OkHttp**: HTTP 客户端，支持流式请求
- **Kotlin Coroutines**: 协程支持，特别是 Flow
- **MNN**: 阿里巴巴机器学习推理引擎
- **UUID**: 唯一标识符生成

## 🚀 Key Features

### AI Provider Integration
- **多提供商支持**: OpenAI GPT、Google Gemini、Anthropic Claude
- **统一接口**: 所有提供商实现相同的 `Provider<T>` 接口
- **流式响应**: 支持 Server-Sent Events (SSE) 流式数据处理
- **代理支持**: 内置 HTTP 代理配置
- **密钥轮询**: API 密钥池化管理

### 本地推理能力
- **MNN 集成**: 支持在 Android 设备上本地运行 AI 模型
- **会话管理**: 统一的多模型会话管理
- **内存优化**: 智能会话缓存和资源管理
- **多模态支持**: 支持文本、图像、音频等多种模态

### 模型管理
- **动态配置**: 支持运行时模型配置
- **匹配器**: 智能模型匹配和选择
- **注册表**: 集中化模型注册和管理
- **自定义模型**: 支持用户自定义模型配置

### 高级功能
- **工具调用**: Function Calling 工具调用机制
- **图像生成**: DALL-E、Midjourney 等图像生成服务
- **余额查询**: 支持多提供商余额获取
- **错误处理**: 统一的错误解析和重试机制
- **使用统计**: Token 使用量和成本统计

## 🔗 Dependencies

**内部模块依赖**:
- `app`: 作为 UI 层的依赖，提供用户界面集成

**外部依赖**:
- OkHttp: HTTP 网络库
- Kotlinx Serialization: JSON 序列化
- MNN: 机器学习推理引擎
- Coroutines: 协程库
- UUID: 唯一标识符

## 📁 Critical Files

- `provider/Provider.kt`: AI 提供商统一接口定义
- `provider/ProviderManager.kt`: 提供商管理核心
- `mnn/ChatService.kt`: MNN 本地推理核心
- `provider/ProviderSetting.kt`: 配置管理基类
- `util/SSE.kt`: 流式数据处理
- `util/KeyRoulette.kt`: API 密钥轮询

## 🎨 Usage Patterns

### Provider 管理
```kotlin
// 创建提供商管理器
val providerManager = ProviderManager(okHttpClient)

// 获取特定提供商
val openAIProvider = providerManager.getProvider("openai") as Provider<ProviderSetting.OpenAI>

// 根据设置获取提供商
val provider = providerManager.getProviderByType(providerSetting)
```

### 本地推理会话
```kotlin
// 创建 MNN 会话
val chatService = ChatService.provide()
val session = chatService.createSession(
    modelId = "qwen2.5-7b",
    modelName = "Qwen2.5-7B",
    sessionIdParam = null,
    historyList = chatHistory,
    configPath = modelPath
)
```

### 流式响应处理
```kotlin
// 流式文本生成
val flow = provider.streamText(
    providerSetting = openAISetting,
    messages = messages,
    params = TextGenerationParams(model = selectedModel)
)

flow.collect { chunk ->
    // 处理流式数据
    updateUI(chunk.content)
}
```

## 🔄 Integration Patterns

### 与 app 模块集成
```kotlin
// 在 app 模块中集成 AI 功能
@Module
@InstallIn(SingletonComponent::class)
object AIModule {
    @Provides
    fun provideProviderManager(): ProviderManager {
        return ProviderManager(okHttpClient)
    }

    @Provides
    fun provideChatService(): ChatService {
        return ChatService.provide()
    }
}
```

### Provider 配置示例
```kotlin
// OpenAI 配置
val openAISetting = ProviderSetting.OpenAI(
    id = Uuid.random(),
    enabled = true,
    name = "OpenAI",
    apiKey = "sk-...",
    baseUrl = "https://api.openai.com/v1",
    models = listOf(
        Model("gpt-4", "GPT-4", ModelType.CHAT),
        Model("gpt-4-turbo", "GPT-4 Turbo", ModelType.CHAT)
    )
)
```

## 🧪 Testing

- **Unit Tests**: 在 `src/test/java/me/rerere/ai/` 目录
- **Android Tests**: 在 `src/androidTest/java/me/rerere/ai/` 目录
- **Mock Provider**: 提供测试用的 Mock Provider 实现
- **本地测试**: MNN 推理的单元测试支持

## 🔐 Security & Configuration

### API 密钥管理
- 密钥存储在安全的 DataStore 中
- 支持多密钥轮询和负载均衡
- 密钥过期自动检测和重试

### 代理配置
```kotlin
val proxyConfig = ProviderProxy.Http(
    address = "192.168.1.100",
    port = 7890,
    username = "user",
    password = "pass"
)
```

### 网络安全
- 证书绑定支持
- TLS 1.3 强制启用
- 请求签名验证
- 敏感数据脱敏日志

## 📊 Performance & Monitoring

### 性能优化
- 连接池复用
- 请求合并和批处理
- 智能缓存策略
- 内存使用监控

### 监控指标
- Token 使用量统计
- 响应时间监控
- 错误率跟踪
- 成本计算

---

**📖 相关文档**:
- [根目录 CLAUDE.md](../CLAUDE.md)
- [app 模块 CLAUDE.md](../app/CLAUDE.md)
- [OpenAI API 文档](https://platform.openai.com/docs)
- [Google AI 文档](https://ai.google.dev)
- [Anthropic Claude 文档](https://docs.anthropic.com)

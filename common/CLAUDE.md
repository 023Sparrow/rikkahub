# 🔧 CLAUDE.md - Common Module

> **面包屑**: `根目录` → `common` → **Common Module**

## 📋 Module Overview

**common** 是 RikkaHub 的通用工具模块，提供跨模块复用的核心基础设施。包括高性能缓存系统、Android 平台工具、HTTP 网络封装和通用工具函数。采用现代 Kotlin 协程和响应式编程模式，为整个应用提供稳定可靠的基础能力。

## 🏗️ Architecture

### 🗂️ Core Packages

- **`android/`**: Android 平台专用工具
  - `ContextUtil.kt`: Context 扩展函数，提供缓存目录和临时目录管理
  - `Logging.kt`: 日志系统封装和配置

- **`cache/`**: 高性能缓存系统
  - `CacheStore.kt`: 缓存存储抽象接口
  - `CacheEntry.kt`: 缓存条目数据结构
  - `LruCache.kt`: LRU 缓存实现（内存 + 磁盘）
  - `FileIO.kt`: 文件 I/O 操作工具
  - `KeyCodec.kt`: 缓存键编码/解码
  - `SingleFileCacheStore.kt`: 单文件缓存存储实现
  - `PerKeyFileCacheStore.kt`: 按键分文件的缓存存储
  - `LruCache.kt`: 内存 + 磁盘的 LRU 缓存

- **`http/`**: HTTP 网络工具
  - `Request.kt`: OkHttp 协程扩展函数
  - `SSE.kt`: Server-Sent Events 流式处理
  - `Json.kt`: JSON 解析和序列化工具
  - `JsonExpression.kt`: JSON 路径表达式解析
  - `AcceptLang.kt`: Accept-Language 头处理

### 🔧 Key Technologies

- **Kotlin Coroutines**: 协程和 Flow 响应式编程
- **OkHttp**: 高性能 HTTP 客户端
- **LinkedHashMap**: LRU 缓存算法实现
- **Kotlinx Serialization**: JSON 序列化/反序列化
- **ReentrantLock**: 线程安全的缓存操作
- **Kotlin 扩展函数**: 增强 Android 和 OkHttp 功能

## 🚀 Key Features

### 高性能缓存系统
- **多级缓存**: 内存缓存 + 磁盘持久化
- **LRU 算法**: 自动淘汰最少使用的缓存条目
- **TTL 支持**: 缓存条目生存时间管理
- **线程安全**: 锁机制保证并发安全
- **预加载**: 支持启动时从磁盘预加载缓存
- **过期清理**: 自动清理过期缓存条目

### Android 平台集成
- **Context 扩展**: 便捷的缓存目录获取
- **临时文件管理**: 应用级别临时目录
- **文件系统封装**: 跨版本文件系统兼容性

### HTTP 网络工具
- **协程化 HTTP**: OkHttp 协程扩展，支持 `await()`
- **SSE 流式处理**: Server-Sent Events 响应式流封装
- **JSON 工具**: 统一的 JSON 解析和构建
- **请求优化**: 连接复用和性能优化

### 通用工具
- **键编码**: 安全的缓存键编码/解码
- **文件 I/O**: 高效的文件读写操作
- **国际化支持**: Accept-Language 头处理

## 🔗 Dependencies

**内部模块依赖**:
- `app`: UI 层依赖，共享缓存和工具函数
- `ai`: AI 模块依赖，使用缓存和网络工具

**外部依赖**:
- OkHttp: HTTP 客户端库
- Kotlin Coroutines: 协程库
- Kotlinx Serialization: JSON 序列化

## 📁 Critical Files

- `cache/LruCache.kt`: 核心缓存实现
- `cache/CacheStore.kt`: 缓存存储抽象
- `http/Request.kt`: HTTP 协程扩展
- `http/SSE.kt`: SSE 流式处理
- `android/ContextUtil.kt`: Android 工具扩展
- `cache/CacheEntry.kt`: 缓存数据结构

## 🎨 Usage Patterns

### 缓存系统使用
```kotlin
// 创建内存 + 磁盘缓存
val diskStore = PerKeyFileCacheStore<File>(cacheDir)
val lruCache = LruCache<String, Data>(
    capacity = 100,
    store = diskStore,
    expireAfterWriteMillis = 24 * 60 * 60 * 1000 // 24小时过期
)

// 存储缓存
lruCache.put("key", data, ttlMillis = 60 * 60 * 1000) // 1小时过期

// 获取缓存
val cachedData = lruCache.get("key")

// 检查缓存是否存在
if (lruCache.containsKey("key")) {
    // 缓存命中
}
```

### Android Context 扩展
```kotlin
// 获取应用临时目录
val tempDir = context.appTempFolder

// 获取命名空间缓存目录
val cacheDir = context.getCacheDirectory("chat_history")

// 创建临时文件
val tempFile = File(context.appTempFolder, "temp_${System.currentTimeMillis()}.tmp")
```

### HTTP 协程扩展
```kotlin
// 异步 HTTP 请求
val response = okHttpClient.newCall(request).await()

// 流式 SSE 处理
val sseFlow = okHttpClient.sseFlow(sseRequest)
sseFlow.collect { event ->
    when (event) {
        is SseEvent.Open -> println("SSE connection opened")
        is SseEvent.Event -> println("Received data: ${event.data}")
        is SseEvent.Closed -> println("SSE connection closed")
        is SseEvent.Failure -> println("SSE error: ${event.throwable}")
    }
}
```

## 🔄 Integration Patterns

### 与 app 模块集成
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides
    @Singleton
    fun provideCacheStore(@ApplicationContext context: Context): CacheStore<String, ByteArray> {
        val cacheDir = context.getCacheDirectory("app_cache")
        return PerKeyFileCacheStore(cacheDir)
    }

    @Provides
    @Singleton
    fun provideLruCache(store: CacheStore<String, ByteArray>): LruCache<String, ByteArray> {
        return LruCache(
            capacity = 1000,
            store = store,
            expireAfterWriteMillis = 7 * 24 * 60 * 60 * 1000 // 7天
        )
    }
}
```

### 缓存策略示例
```kotlin
// 模型响应缓存
val modelResponseCache = LruCache<String, String>(
    capacity = 200,
    store = diskStore,
    deleteOnEvict = true,
    preloadFromStore = true
)

// 聊天历史缓存
val chatHistoryCache = LruCache<String, ChatData>(
    capacity = 50,
    store = singleFileStore,
    expireAfterWriteMillis = null // 永不过期
)
```

### SSE 事件处理
```kotlin
// AI 流式响应处理
val sseFlow = httpClient.sseFlow(aiRequest)
val messageFlow = sseFlow
    .filterIsInstance<SseEvent.Event>()
    .map { parseSSEData(it.data) }
    .onEach { updateUI(it) }

messageFlow.collect()
```

## 🧪 Testing

- **Unit Tests**: 在 `src/test/java/me/rerere/common/` 目录
- **缓存测试**: LRU 算法的正确性和性能测试
- **网络测试**: HTTP 扩展和 SSE 流测试
- **并发测试**: 缓存多线程安全性测试
- **Android 测试**: Context 扩展函数测试

## 🔐 Security & Performance

### 安全特性
- **缓存键编码**: 防止缓存键注入攻击
- **文件权限**: 安全的缓存文件权限管理
- **内存管理**: 防止内存泄漏和溢出
- **并发控制**: 线程安全的缓存操作

### 性能优化
- **内存预分配**: 减少动态内存分配
- **磁盘 I/O 优化**: 批量读写和缓冲区
- **网络连接池**: OkHttp 连接复用
- **协程调度**: 优化的协程执行策略

### 监控指标
- **缓存命中率**: 监控缓存系统效率
- **内存使用量**: 跟踪内存占用情况
- **磁盘使用量**: 监控磁盘缓存空间
- **网络性能**: HTTP 请求响应时间

---

**📖 相关文档**:
- [根目录 CLAUDE.md](../CLAUDE.md)
- [app 模块 CLAUDE.md](../app/CLAUDE.md)
- [ai 模块 CLAUDE.md](../ai/CLAUDE.md)
- [OkHttp 文档](https://square.github.io/okhttp/)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)

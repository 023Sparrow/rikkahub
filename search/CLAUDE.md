# 🔍 CLAUDE.md - Search Module

> **面包屑**: `根目录` → `search` → **Search Module**

## 📋 Module Overview

**search** 是 RikkaHub 的多提供商搜索 SDK 模块，支持 12+ 种不同搜索引擎的统一抽象和实现。支持多种搜索服务提供商，包括 Exa、Tavily、Bing、Google、Brave 等，并提供统一的搜索和网页抓取 API。采用泛型接口设计，实现类型安全的搜索服务管理。

## 🏗️ Architecture

### 🗂️ Core Components

- **`SearchService<T>`**: 搜索服务泛型接口
  - **类型安全**: 泛型参数 `T` 支持不同服务选项类型
  - **统一 API**: 提供标准化的搜索和抓取方法
  - **结果抽象**: `SearchResult` 和 `ScrapedResult` 统一结果类型
  - **Compose 集成**: 内置 `Description()` Composable 组件

- **服务选项类型**:
  - `SearchCommonOptions`: 通用搜索选项
  - `SearchServiceOptions`: 各服务专用选项容器

- **数据模型**:
  - `SearchResult`: 搜索结果数据结构
  - `ScrapedResult`: 网页抓取结果数据结构
  - `SearchResultItem`: 单个搜索结果项

### 🔧 Key Technologies

- **Kotlinx Serialization**: JSON 序列化/反序列化
- **OkHttp**: HTTP 客户端，支持协程扩展
- **Kotlin Coroutines**: 协程和 Flow 响应式编程
- **Jetpack Compose**: UI 组件支持
- **AI Core Integration**: 与 AI 模块的 `InputSchema` 集成

## 🚀 Key Features

### 多提供商搜索支持
- **12+ 搜索提供商**: Exa、Tavily、Bing、Google、Brave、SearXNG、LinkUp、Metaso、Ollama、Perplexity、Firecrawl、Jina、BingLocal
- **统一接口**: 所有提供商实现 `SearchService<T>` 接口
- **类型安全**: 泛型设计确保编译时类型检查
- **动态配置**: 支持运行时服务选项配置

### 搜索和抓取功能
- **全文搜索**: 支持多关键词和主题搜索
- **网页抓取**: 提供网页内容提取功能
- **结果结构化**: 统一的结果数据格式
- **元数据支持**: 包含评分、原文内容等丰富信息

### AI 工具集成
- **参数 Schema**: 使用 `me.rerere.ai.core.InputSchema` 定义参数
- **工具调用**: 支持 Function Calling 工具调用模式
- **参数验证**: 运行时参数类型和值验证

## 🔗 Dependencies

**内部模块依赖**:
- `ai`: AI 核心模块，参数 Schema 定义
- `app`: UI 层集成，Compose 组件使用

**外部依赖**:
- OkHttp: HTTP 网络库
- Kotlinx Serialization: JSON 序列化
- Kotlin Coroutines: 协程库
- Android Compose: UI 框架

## 📁 Critical Files

- `SearchService.kt`: 核心接口和数据模型定义
- `ExaSearchService.kt`: Exa 搜索服务实现
- `TavilySearchService.kt`: Tavily 搜索和抓取服务实现
- `SearchServiceOptions.kt`: 服务选项类型定义

## 🎨 Usage Patterns

### 基本搜索服务使用
```kotlin
// 获取搜索服务实例
val searchService: SearchService<SearchServiceOptions.ExaOptions> = ExaSearchService

// 执行搜索
val result = searchService.search(
    params = buildJsonObject {
        put("query", "Android 开发最佳实践")
    },
    commonOptions = SearchCommonOptions(resultSize = 10),
    serviceOptions = SearchServiceOptions.ExaOptions(apiKey = "your-api-key")
)

result.fold(
    onSuccess = { searchResult ->
        searchResult.items.forEach { item ->
            println("标题: ${item.title}")
            println("URL: ${item.url}")
            println("内容: ${item.text}")
        }
    },
    onFailure = { error ->
        println("搜索失败: $error")
    }
)
```

### 网页抓取功能
```kotlin
// 网页内容抓取
val scrapedResult = tavilySearchService.scrape(
    params = buildJsonObject {
        put("url", "https://example.com/article")
    },
    commonOptions = SearchCommonOptions(),
    serviceOptions = SearchServiceOptions.TavilyOptions(apiKey = "your-key")
)

scrapedResult.fold(
    onSuccess = { result ->
        result.urls.forEach { urlResult ->
            println("URL: ${urlResult.url}")
            println("内容: ${urlResult.content.take(200)}...")
        }
    },
    onFailure = { error ->
        println("抓取失败: $error")
    }
)
```

### Compose UI 集成
```kotlin
@Composable
fun SearchScreen() {
    val searchService = ExaSearchService
    var searchResults by remember { mutableStateOf<List<SearchResultItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    Column {
        // 服务描述组件
        searchService.Description()

        // 搜索输入框
        var query by remember { mutableStateOf("") }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("搜索关键词") }
        )

        Button(
            onClick = {
                isLoading = true
                // 执行搜索
                lifecycleScope.launch {
                    val result = searchService.search(
                        params = buildJsonObject { put("query", query) },
                        commonOptions = SearchCommonOptions(resultSize = 10),
                        serviceOptions = SearchServiceOptions.ExaOptions(apiKey = apiKey)
                    )

                    result.fold(
                        onSuccess = { searchResults = it.items },
                        onFailure = { error -> /* 处理错误 */ }
                    )
                    isLoading = false
                }
            }
        ) {
            Text("搜索")
        }

        // 搜索结果列表
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(searchResults) { item ->
                    SearchResultItem(item = item)
                }
            }
        }
    }
}
```

## 🔄 Integration Patterns

### 与 app 模块集成
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SearchModule {
    @Provides
    @Singleton
    fun provideExaSearchService(): SearchService<SearchServiceOptions.ExaOptions> {
        return ExaSearchService
    }

    @Provides
    @Singleton
    fun provideTavilySearchService(): SearchService<SearchServiceOptions.TavilyOptions> {
        return TavilySearchService
    }
}

// 在 ViewModel 中使用
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val exaSearchService: SearchService<SearchServiceOptions.ExaOptions>,
    private val tavilySearchService: SearchService<SearchServiceOptions.TavilyOptions>
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResultItem>> = _searchResults.asStateFlow()

    fun performSearch(query: String) {
        viewModelScope.launch {
            val result = exaSearchService.search(
                params = buildJsonObject { put("query", query) },
                commonOptions = SearchCommonOptions(resultSize = 20),
                serviceOptions = SearchServiceOptions.ExaOptions(
                    apiKey = /* 从配置获取 */"",
                    type = "auto"
                )
            )

            result.fold(
                onSuccess = { _searchResults.value = it.items },
                onFailure = { /* 错误处理 */ }
            )
        }
    }
}
```

### 多服务提供商切换
```kotlin
enum class SearchProvider(val displayName: String) {
    EXA("Exa"),
    TAVILY("Tavily"),
    BING("Bing"),
    GOOGLE("Google")
}

@Composable
fun MultiProviderSearch() {
    var selectedProvider by remember { mutableStateOf(SearchProvider.EXA) }
    var query by remember { mutableStateOf("") }

    val searchService = when (selectedProvider) {
        SearchProvider.EXA -> ExaSearchService
        SearchProvider.TAVILY -> TavilySearchService
        SearchProvider.BING -> BingSearchService
        SearchProvider.GOOGLE -> GoogleSearchService
    }

    Column {
        // 提供商选择器
        LazyHorizontalGrid(
            rows = GridCells.Fixed(1),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(SearchProvider.values()) { provider ->
                FilterChip(
                    selected = selectedProvider == provider,
                    onClick = { selectedProvider = provider },
                    label = { Text(provider.displayName) }
                )
            }
        }

        // 搜索框和按钮
        Row {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                // 执行搜索
                performSearch(query, searchService)
            }) {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            }
        }
    }
}
```

### 与 AI 工具集成
```kotlin
// 在 AI Provider 中作为工具使用
class OpenAIProvider {
    private val searchTools = mapOf(
        "exa_search" to SearchTool(ExaSearchService, SearchServiceOptions.ExaOptions::class),
        "tavily_search" to SearchTool(TavilySearchService, SearchServiceOptions.TavilyOptions::class)
    )

    suspend fun handleFunctionCall(functionName: String, arguments: JsonObject): Result<String> {
        return when (functionName) {
            "exa_search" -> {
                val apiKey = getApiKey("exa")
                val serviceOptions = SearchServiceOptions.ExaOptions(apiKey = apiKey)

                ExaSearchService.search(
                    params = arguments,
                    commonOptions = SearchCommonOptions(resultSize = 10),
                    serviceOptions = serviceOptions
                ).map { result ->
                    json.encodeToString(result)
                }
            }
            // 其他搜索工具处理
            else -> Result.failure(Exception("Unknown function: $functionName"))
        }
    }
}
```

## 🧪 Testing

- **单元测试**: 在 `src/test/java/me/rerere/search/` 目录
- **模拟测试**: 提供测试用的 Mock SearchService 实现
- **搜索结果测试**: 验证搜索结果数据结构和完整性
- **网页抓取测试**: 测试网页内容提取功能
- **参数验证测试**: 测试输入参数的类型检查和验证

## 🔐 Security & Performance

### 安全特性
- **API 密钥安全**: 支持安全的环境变量配置
- **输入验证**: 运行时参数类型和值验证
- **网络隔离**: HTTP 请求超时和重试机制
- **内容过滤**: 支持搜索结果的关键词过滤

### 性能优化
- **连接复用**: OkHttp 连接池优化
- **请求缓存**: 智能缓存搜索结果
- **并发控制**: 限制同时进行的搜索请求数量
- **异步处理**: 非阻塞的搜索和抓取操作

### 监控指标
- **搜索性能**: 搜索响应时间统计
- **成功率**: 搜索成功率跟踪
- **提供商切换**: 自动故障转移和负载均衡
- **使用量统计**: API 调用次数和配额监控

## 🎯 扩展指南

### 添加新搜索提供商
```kotlin
// 1. 创建服务选项类
data class NewSearchOptions(
    val apiKey: String,
    val maxResults: Int = 10,
    val customParam: String? = null
)

// 2. 实现 SearchService 接口
object NewSearchService : SearchService<NewSearchOptions> {
    override val name: String = "NewSearch"

    override val parameters: InputSchema?
        get() = InputSchema.Obj(
            properties = buildJsonObject {
                put("query", buildJsonObject {
                    put("type", "string")
                    put("description", "search keyword")
                })
            },
            required = listOf("query")
        )

    override suspend fun search(
        params: JsonObject,
        commonOptions: SearchCommonOptions,
        serviceOptions: NewSearchOptions
    ): Result<SearchResult> {
        // 实现搜索逻辑
    }
}

// 3. 在服务选项注册表中注册
object SearchServiceOptions {
    // ... 其他选项类
    class NewSearchOptions(val apiKey: String)
}
```

### 自定义搜索结果处理
```kotlin
// 创建自定义搜索结果转换器
class SearchResultTransformer {
    fun transformToReadableContent(result: SearchResult): String {
        return buildString {
            appendLine("🔍 搜索结果 (${result.items.size} 项)")
            result.items.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.title}")
                appendLine("   📄 ${item.text.take(150)}...")
                appendLine("   🔗 ${item.url}")
                appendLine()
            }
        }
    }

    fun filterResults(
        results: List<SearchResultItem>,
        keywords: List<String>,
        minScore: Double = 0.5
    ): List<SearchResultItem> {
        return results.filter { item ->
            item.score >= minScore && keywords.all { keyword ->
                item.title.contains(keyword, ignoreCase = true) ||
                item.text.contains(keyword, ignoreCase = true)
            }
        }
    }
}
```

---

**📖 相关文档**:
- [根目录 CLAUDE.md](../CLAUDE.md)
- [app 模块 CLAUDE.md](../app/CLAUDE.md)
- [ai 模块 CLAUDE.md](../ai/CLAUDE.md)
- [Exa 搜索 API 文档](https://exa-api.dev/)
- [Tavily 搜索 API 文档](https://tavily.com/)
- [OkHttp 文档](https://square.github.io/okhttp/)
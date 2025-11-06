# 🎨 CLAUDE.md - Highlight Module

> **面包屑**: `根目录` → `highlight` → **Highlight Module**

## 📋 Module Overview

**highlight** 是 RikkaHub 的代码语法高亮模块，提供基于 JavaScript 的高性能语法高亮功能。集成了 QuickJS JavaScript 引擎和 Prism.js 语法高亮库，支持多种编程语言的实时语法高亮渲染。采用 Jetpack Compose 响应式 UI 框架，为代码展示提供美观的语法高亮体验。

## 🏗️ Architecture

### 🗂️ Core Components

- **`Highlighter.kt`**: 核心语法高亮引擎
  - **QuickJS 集成**: 基于 QuickJS 引擎运行 JavaScript 代码
  - **Prism.js 集成**: 使用 Prism.js 进行语法高亮处理
  - **协程化 API**: 支持异步高亮处理，不阻塞主线程
  - **单线程执行**: 避免多线程竞争，确保高亮安全性

- **`HighlightText.kt`**: Jetpack Compose UI 组件
  - **CompositionLocal**: `LocalHighlighter` 提供高亮器实例
  - **响应式组件**: `HighlightText` Composable 实时高亮显示
  - **主题系统**: `HighlightTextColorPalette` 完整颜色主题
  - **性能优化**: 4096 字符限制防止性能问题

### 📄 Resources

- **`res/raw/prism.js`**: Prism.js 语法高亮库
  - 轻量级 JavaScript 语法高亮库
  - 支持 200+ 种编程语言
  - 自定义高亮规则扩展

### 🔧 Key Technologies

- **QuickJS**: 轻量级 JavaScript 引擎
- **Prism.js**: 代码语法高亮库
- **Jetpack Compose**: 现代 UI 工具包
- **Kotlin Coroutines**: 协程异步处理
- **Kotlinx Serialization**: JSON 序列化
- **ExecutorService**: 单线程执行池

## 🚀 Key Features

### 多语言语法支持
- **广泛支持**: 支持 200+ 种编程语言
- **常见语言**: JavaScript, TypeScript, Python, Java, Kotlin, Swift, C/C++, C#, Go, Rust 等
- **配置化**: 根据语言参数动态高亮
- **自定义语法**: 支持扩展语法规则

### 高性能异步处理
- **非阻塞**: 异步高亮不阻塞 UI 线程
- **单线程安全**: 避免多线程竞争问题
- **内存优化**: 限制最大代码长度 (4096 字符)
- **资源管理**: 自动释放 QuickJS 资源

### Compose 深度集成
- **声明式 UI**: 完整的 Compose 组件支持
- **状态管理**: 响应式状态更新
- **主题定制**: 可配置的颜色主题系统
- **字体支持**: 等宽字体和自定义样式

### 灵活的主题系统
- **完整颜色映射**: 关键词、字符串、数字、注释等
- **默认主题**: 开箱即用的美观配色
- **自定义主题**: 支持完全自定义颜色方案
- **无障碍支持**: 高对比度主题支持

## 🔗 Dependencies

**内部模块依赖**:
- `app`: UI 层集成，提供 Compose 环境
- `common`: 无直接依赖

**外部依赖**:
- QuickJS: JavaScript 引擎 (`com.whl.quickjs:android`)
- Kotlin Coroutines: 协程库
- Kotlinx Serialization: JSON 序列化
- Android Compose: UI 框架

## 📁 Critical Files

- `Highlighter.kt`: 核心高亮引擎实现
- `HighlightText.kt`: Compose UI 组件
- `res/raw/prism.js`: Prism.js 高亮库
- `HighlightTextColorPalette.kt`: 主题配色定义

## 🎨 Usage Patterns

### 基本使用
```kotlin
// 1. 提供 Highlighter 实例
val highlighter = remember { Highlighter(context) }

// 2. 在 CompositionLocal 中提供
CompositionLocalProvider(LocalHighlighter provides highlighter) {
    // 3. 使用 HighlightText 组件
    HighlightText(
        code = """
            fun helloWorld() {
                println("Hello, World!")
            }
        """.trimIndent(),
        language = "kotlin"
    )
}
```

### 自定义主题
```kotlin
val customTheme = HighlightTextColorPalette(
    keyword = Color(0xFF569CD6),     // 蓝色关键词
    string = Color(0xFFCE9178),      // 橙色字符串
    number = Color(0xFFB5CEA8),      // 绿色数字
    comment = Color(0xFF6A9955),     // 绿色注释
    function = Color(0xFFDCDCAA),    // 黄色函数
    // ... 其他颜色配置
)

HighlightText(
    code = code,
    language = "java",
    colors = customTheme,
    fontSize = 14.sp,
    fontFamily = FontFamily.Monospace
)
```

### 异步高亮处理
```kotlin
// 获取高亮 tokens
val tokens = highlighter.highlight(code, language)

// 在 Compose 中处理
LaunchedEffect(code, language) {
    val highlightedTokens = highlighter.highlight(code, language)
    // 更新 UI 状态
    highlightedText = buildAnnotatedString {
        tokens.forEach { token ->
            buildHighlightText(token, colorPalette)
        }
    }
}
```

## 🔄 Integration Patterns

### 与 app 模块集成
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object HighlightModule {
    @Provides
    @Singleton
    fun provideHighlighter(@ApplicationContext context: Context): Highlighter {
        return Highlighter(context)
    }
}

// 在 UI 中使用
@Composable
fun CodeDisplayScreen() {
    val highlighter = LocalHighlighter.current

    HighlightText(
        code = code,
        language = language,
        modifier = Modifier.padding(16.dp)
    )
}
```

### 多语言支持示例
```kotlin
// JavaScript/TypeScript
HighlightText(code = jsCode, language = "javascript")

// Python
HighlightText(code = pyCode, language = "python")

// Kotlin
HighlightText(code = ktCode, language = "kotlin")

// HTML/CSS
HighlightText(code = htmlCode, language = "markup")
HighlightText(code = cssCode, language = "css")
```

### 性能优化配置
```kotlin
@Composable
fun OptimizedCodeDisplay(code: String) {
    // 对于超长代码，使用简化显示
    val displayCode = if (code.length > 8192) {
        code.substring(0, 8192) + "... (代码已截断)"
    } else {
        code
    }

    HighlightText(
        code = displayCode,
        language = "kotlin",
        fontSize = 12.sp,
        maxLines = 20
    )
}
```

## 🧪 Testing

- **单元测试**: 在 `src/test/java/me/rerere/highlight/` 目录
- **语法高亮测试**: 各种编程语言的语法高亮正确性
- **性能测试**: 大文件高亮性能和内存使用
- **UI 测试**: Compose 组件渲染测试
- **并发测试**: 多线程高亮安全性

## 🔐 Security & Performance

### 安全特性
- **代码隔离**: QuickJS 引擎隔离 JavaScript 执行
- **资源限制**: 最大代码长度限制防止资源耗尽
- **异常处理**: 完整的错误捕获和恢复机制
- **内存管理**: 自动释放 QuickJS 资源

### 性能优化
- **异步处理**: 非阻塞高亮处理
- **缓存机制**: 高亮结果缓存策略
- **内存优化**: 限制最大文本长度
- **线程安全**: 单线程执行避免竞争

### 监控指标
- **高亮性能**: 语法高亮处理时间
- **内存使用**: 高亮引擎内存占用
- **渲染性能**: Compose 渲染帧率
- **错误率**: 高亮失败和异常统计

## 🎯 扩展指南

### 添加新语言支持
```kotlin
// 1. 确保 Prism.js 支持该语言
// 2. 配置语言映射
val languageMap = mapOf(
    "kotlin" to "language-kotlin",
    "dart" to "language-dart",
    "lua" to "language-lua"
)

// 3. 在高亮时使用对应语言代码
highlighter.highlight(code, languageMap[language] ?: language)
```

### 自定义语法规则
```javascript
// 在 prism.js 中扩展自定义语法
Prism.languages['custom-lang'] = {
    'keyword': /\b(if|else|for|while)\b/,
    'string': /"[^"]*"/,
    'comment': /\/\/.*/
};
```

---

**📖 相关文档**:
- [根目录 CLAUDE.md](../CLAUDE.md)
- [app 模块 CLAUDE.md](../app/CLAUDE.md)
- [Prism.js 官方文档](https://prismjs.com/)
- [QuickJS Android 文档](https://github.com/line/quickjs-android)
- [Jetpack Compose 文档](https://developer.android.com/jetpack/compose)

# 世界书与记忆表格功能实现计划

## 当前项目状态总结

### ✅ 已完成的工作

1. **数据库层 (100%完成)**
   - ✅ [`WorldBookEntry`](app/src/main/java/me/rerere/rikkahub/data/db/entity/WorldBookEntry.kt) 实体类 (15个字段)
   - ✅ [`MemoryTable`](app/src/main/java/me/rerere/rikkahub/data/db/entity/MemoryTable.kt) 实体类
   - ✅ [`MemoryTableRow`](app/src/main/java/me/rerere/rikkahub/data/db/entity/MemoryTableRow.kt) 实体类
   - ✅ [`WorldBookDAO`](app/src/main/java/me/rerere/rikkahub/data/db/dao/WorldBookDAO.kt) 数据访问层
   - ✅ [`MemoryTableDAO`](app/src/main/java/me/rerere/rikkahub/data/db/dao/MemoryTableDAO.kt) 数据访问层
   - ✅ [`StringListConverter`](app/src/main/java/me/rerere/rikkahub/data/db/converter/StringListConverter.kt) 类型转换器
   - ✅ [`MemoryTableCellConverter`](app/src/main/java/me/rerere/rikkahub/data/db/converter/MemoryTableCellConverter.kt) 类型转换器
   - ✅ [`AppDatabase`](app/src/main/java/me/rerere/rikkahub/data/db/AppDatabase.kt) 版本12迁移

2. **Repository层 (100%完成)**
   - ✅ [`WorldBookRepository`](app/src/main/java/me/rerere/rikkahub/data/repository/WorldBookRepository.kt) 业务逻辑封装
   - ✅ [`MemoryTableRepository`](app/src/main/java/me/rerere/rikkahub/data/repository/MemoryTableRepository.kt) 业务逻辑封装

3. **UI层 (部分完成，但使用了Android View系统)**
   - ⚠️ `WorldBookFragment.kt` - 使用Fragment+DataBinding (与Jetpack Compose不兼容，已注释)
   - ⚠️ `MemoryTableFragment.kt` - 使用Fragment+DataBinding (与Jetpack Compose不兼容，已注释)
   - ⚠️ UI相关适配器和布局文件 (已注释)

4. **ViewModel层 (100%完成)**
   - ✅ [`WorldBookViewModel`](app/src/main/java/me/rerere/rikkahub/ui/viewmodel/WorldBookViewModel.kt)
   - ✅ [`MemoryTableViewModel`](app/src/main/java/me/rerere/rikkahub/ui/viewmodel/MemoryTableViewModel.kt)
   - ✅ [`MemoryTableEditorViewModel`](app/src/main/java/me/rerere/rikkahub/ui/viewmodel/MemoryTableEditorViewModel.kt)

5. **编译环境**
   - ✅ 项目主代码编译通过
   - ✅ Gradle配置正确
   - ✅ 依赖项完整

---

## ❌ 缺失的核心功能

### 1. **世界书匹配引擎** (优先级: ⭐⭐⭐⭐⭐)

**需要创建的文件:**
- `app/src/main/java/me/rerere/rikkahub/service/WorldBookMatcher.kt`

**功能需求:**
```kotlin
class WorldBookMatcher {
    /**
     * 扫描用户输入和对话历史，匹配世界书条目
     * @param input 用户当前输入
     * @param conversationHistory 最近的对话历史
     * @param entries 启用的世界书条目列表
     * @return 匹配的条目列表 (按优先级排序)
     */
    fun matchEntries(
        input: String,
        conversationHistory: List<UIMessage>,
        entries: List<WorldBookEntry>
    ): List<MatchedEntry>
    
    /**
     * 支持正则表达式匹配
     */
    private fun matchRegex(pattern: String, text: String): Boolean
    
    /**
     * 支持普通关键词匹配
     */
    private fun matchKeywords(keywords: List<String>, text: String): Boolean
    
    /**
     * 检查次要关键词
     */
    private fun checkSecondaryKeywords(entry: WorldBookEntry, text: String): Boolean
    
    /**
     * 处理递归扫描逻辑
     */
    fun recursiveMatch(
        baseMatches: List<MatchedEntry>,
        entries: List<WorldBookEntry>,
        maxDepth: Int = 3
    ): List<MatchedEntry>
}

data class MatchedEntry(
    val entry: WorldBookEntry,
    val matchDepth: Int = 0 // 递归深度
)
```

**需要的算法逻辑:**
1. ✅ 关键词精确匹配 (不区分大小写)
2. ✅ 关键词包含匹配 (word boundaries)
3. ✅ 正则表达式匹配 (when `useRegex = true`)
4. ✅ 次要关键词逻辑 (AND/OR 取决于`isSelective`)
5. ✅ 递归扫描 (匹配的条目内容可能触发其他条目)
6. ✅ 递归排除 (当`excludeRecursion = true`时不再递归)
7. ✅ 优先级排序 (`priority`字段)

---

### 2. **上下文注入器** (优先级: ⭐⭐⭐⭐⭐)

**需要创建的文件:**
- `app/src/main/java/me/rerere/rikkahub/service/WorldBookInjector.kt`

**功能需求:**
```kotlin
class WorldBookInjector {
    /**
     * 将匹配的世界书条目注入到对话上下文
     * @param messages 原始消息列表
     * @param matchedEntries 匹配的世界书条目
     * @param assistant 当前助手配置
     * @return 注入后的消息列表
     */
    fun injectWorldBook(
        messages: List<UIMessage>,
        matchedEntries: List<MatchedEntry>,
        assistant: Assistant
    ): List<UIMessage>
    
    /**
     * 根据injectionPosition确定注入位置
     * 0 = 开头 (在系统提示词之后)
     * 1 = 结尾 (在最后一条用户消息之前)
     * 2 = 自定义位置 (预留)
     */
    private fun determineInjectionPosition(
        messages: List<UIMessage>,
        position: Int
    ): Int
    
    /**
     * 格式化世界书内容为消息
     */
    private fun formatWorldBookContent(
        entries: List<MatchedEntry>
    ): UIMessage
    
    /**
     * 去重检查 (避免重复注入相同内容)
     */
    private fun deduplicateEntries(entries: List<MatchedEntry>): List<MatchedEntry>
}
```

**注入策略:**
1. ✅ 按`priority`排序 (数字越大优先级越高)
2. ✅ 去重 (同一条目在一次对话中只注入一次)
3. ✅ 位置控制 (`injectionPosition`: 0=开头, 1=结尾)
4. ✅ 格式化为system role消息或特殊标记
5. ✅ 长度控制 (考虑token限制)

---

### 3. **ChatService集成** (优先级: ⭐⭐⭐⭐⭐)

**需要修改的文件:**
- [`app/src/main/java/me/rerere/rikkahub/service/ChatService.kt`](app/src/main/java/me/rerere/rikkahub/service/ChatService.kt:345)

**修改位置: `handleMessageComplete()` 函数 (第345行)**

```kotlin
private suspend fun handleMessageComplete(
    conversationId: Uuid,
    messageRange: ClosedRange<Int>? = null
) {
    val settings = settingsStore.settingsFlow.first()
    val model = settings.getCurrentChatModel() ?: return
    val assistant = settings.getCurrentAssistant()

    runCatching {
        val conversation = getConversationFlow(conversationId).value
        
        // ⬇️⬇️⬇️ 新增: 世界书匹配和注入 ⬇️⬇️⬇️
        val worldBookEntries = if (assistant.enableWorldBook) { // 需要在Assistant模型中添加此字段
            worldBookRepository.getActiveWorldBookEntries(assistant.id.toString())
        } else {
            emptyList()
        }
        
        val matchedEntries = worldBookMatcher.matchEntries(
            input = conversation.currentMessages.lastOrNull()?.toText() ?: "",
            conversationHistory = conversation.currentMessages.takeLast(5),
            entries = worldBookEntries
        )
        
        val messagesWithWorldBook = worldBookInjector.injectWorldBook(
            messages = conversation.currentMessages,
            matchedEntries = matchedEntries,
            assistant = assistant
        )
        // ⬆️⬆️⬆️ 新增结束 ⬆️⬆️⬆️
        
        // reset suggestions
        updateConversation(conversationId, conversation.copy(chatSuggestions = emptyList()))
        
        // 原有的生成逻辑...
        generationHandler.generateText(
            settings = settings,
            model = model,
            messages = messagesWithWorldBook, // 使用注入后的消息
            // ...其他参数
        )
    }
}
```

**需要添加的依赖注入:**
```kotlin
class ChatService(
    // ...现有参数
    private val worldBookRepository: WorldBookRepository, // 新增
    private val worldBookMatcher: WorldBookMatcher,       // 新增
    private val worldBookInjector: WorldBookInjector,     // 新增
) {
    // ...
}
```

---

### 4. **记忆表格查询接口** (优先级: ⭐⭐⭐)

**需要创建的文件:**
- `app/src/main/java/me/rerere/rikkahub/service/MemoryTableQueryService.kt`

**功能需求:**
```kotlin
class MemoryTableQueryService(
    private val memoryTableRepository: MemoryTableRepository
) {
    /**
     * 根据关键词搜索记忆表格
     */
    suspend fun searchTables(
        assistantId: String,
        query: String
    ): List<MemoryTableSearchResult>
    
    /**
     * 将记忆表格数据格式化为Markdown表格
     */
    fun formatTableAsMarkdown(table: MemoryTable, rows: List<MemoryTableRow>): String
    
    /**
     * 导出表格为CSV
     */
    fun exportTableAsCSV(table: MemoryTable, rows: List<MemoryTableRow>): String
    
    /**
     * 从CSV导入表格
     */
    suspend fun importTableFromCSV(
        assistantId: String,
        csvContent: String
    ): Result<MemoryTable>
}
```

**使用场景:**
- AI可以通过工具调用查询记忆表格
- 用户可以导出/导入表格数据
- 支持结构化数据的检索和展示

---

### 5. **Jetpack Compose UI重写** (优先级: ⭐⭐⭐⭐)

**需要创建的文件:**
- `app/src/main/java/me/rerere/rikkahub/ui/pages/worldbook/WorldBookManagementPage.kt`
- `app/src/main/java/me/rerere/rikkahub/ui/pages/worldbook/WorldBookEditorPage.kt`
- `app/src/main/java/me/rerere/rikkahub/ui/pages/memorytable/MemoryTableManagementPage.kt`
- `app/src/main/java/me/rerere/rikkahub/ui/pages/memorytable/MemoryTableEditorPage.kt`

**设计参考现有页面:**
- [`AssistantPage.kt`](app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/AssistantPage.kt) - 列表管理页面
- [`AssistantDetailPage.kt`](app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/AssistantDetailPage.kt) - 详情编辑页面

**主要组件:**
```kotlin
@Composable
fun WorldBookManagementPage(
    navController: NavController,
    assistantId: String
) {
    // 世界书条目列表
    // 搜索和筛选功能
    // 新建/编辑/删除操作
}

@Composable
fun WorldBookEditorPage(
    navController: NavController,
    entryId: String?
) {
    // 标题输入
    // 关键词标签输入
    // 内容富文本编辑
    // 优先级设置
    // 高级选项(正则、递归等)
}

@Composable
fun MemoryTableManagementPage(
    navController: NavController,
    assistantId: String
) {
    // 表格列表
    // 新建/删除表格
}

@Composable
fun MemoryTableEditorPage(
    navController: NavController,
    tableId: String
) {
    // 表格名称和描述
    // 列标题管理
    // 行数据编辑(类似Excel)
    // 导入/导出功能
}
```

---

### 6. **聊天界面入口** (优先级: ⭐⭐⭐⭐)

**需要修改的文件:**
- [`app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatPage.kt`](app/src/main/java/me/rerere/rikkahub/ui/pages/chat/ChatPage.kt)

**在TopAppBar添加知识库按钮:**
```kotlin
TopAppBar(
    // ...现有代码
    actions = {
        // 新增知识库入口
        IconButton(onClick = {
            navController.navigate(
                Screen.WorldBookManagement(assistantId = settings.assistantId.toString())
            )
        }) {
            Icon(Lucide.Book, "Knowledge Base")
        }
        
        // ...其他现有按钮
    }
)
```

---

### 7. **Assistant模型扩展** (优先级: ⭐⭐⭐⭐)

**需要修改的文件:**
- `app/src/main/java/me/rerere/rikkahub/data/model/Assistant.kt`

**添加字段:**
```kotlin
data class Assistant(
    // ...现有字段
    val enableWorldBook: Boolean = false,      // 是否启用世界书
    val worldBookContextSize: Int = 2000,      // 世界书上下文token限制
    val enableMemoryTable: Boolean = false,    // 是否启用记忆表格
)
```

**对应的UI设置项:**
在[`AssistantDetailPage.kt`](app/src/main/java/me/rerere/rikkahub/ui/pages/assistant/detail/AssistantDetailPage.kt)添加新的设置选项卡或在现有的"Memory"选项卡中添加。

---

### 8. **导航路由配置** (优先级: ⭐⭐⭐)

**需要修改的文件:**
- `app/src/main/java/me/rerere/rikkahub/Screen.kt`

**添加路由:**
```kotlin
sealed class Screen {
    // ...现有路由
    
    data class WorldBookManagement(val assistantId: String) : Screen()
    data class WorldBookEditor(val entryId: String? = null) : Screen()
    data class MemoryTableManagement(val assistantId: String) : Screen()
    data class MemoryTableEditor(val tableId: String) : Screen()
}
```

---

### 9. **单元测试** (优先级: ⭐⭐)

**需要创建的测试文件:**
- `app/src/test/java/me/rerere/rikkahub/service/WorldBookMatcherTest.kt`
- `app/src/test/java/me/rerere/rikkahub/service/WorldBookInjectorTest.kt`
- `app/src/test/java/me/rerere/rikkahub/repository/WorldBookRepositoryTest.kt`
- `app/src/test/java/me/rerere/rikkahub/repository/MemoryTableRepositoryTest.kt`

**测试用例示例:**
```kotlin
class WorldBookMatcherTest {
    @Test
    fun `test exact keyword matching`() {
        // 测试精确关键词匹配
    }
    
    @Test
    fun `test regex matching`() {
        // 测试正则表达式匹配
    }
    
    @Test
    fun `test recursive matching with max depth`() {
        // 测试递归匹配深度限制
    }
    
    @Test
    fun `test priority sorting`() {
        // 测试优先级排序
    }
}
```

---

### 10. **文档和示例** (优先级: ⭐)

**需要创建的文档:**
- `docs/world-book-usage-guide.md` - 用户使用指南
- `docs/world-book-examples.md` - 示例场景和配置
- `docs/memory-table-guide.md` - 记忆表格使用指南

---

## 🔧 技术依赖检查

### 需要的库和工具
- ✅ Room Database (已集成)
- ✅ Kotlin Coroutines (已集成)
- ✅ Jetpack Compose (已集成)
- ✅ Hilt依赖注入 (需要确认WorldBookMatcher等新类的注入配置)
- ✅ kotlinx.serialization (已用于TypeConverter)

### 性能优化考虑
1. **关键词匹配缓存**: 使用LRU Cache缓存匹配结果
2. **数据库索引**: 为`keywords`字段添加FTS全文搜索支持
3. **懒加载**: 只加载当前assistant的世界书条目
4. **异步处理**: 匹配和注入操作在后台线程执行

---

## 📋 实现优先级排序

### Phase 1: 核心功能 (第1周)
1. ⭐⭐⭐⭐⭐ 世界书匹配引擎 (`WorldBookMatcher`)
2. ⭐⭐⭐⭐⭐ 上下文注入器 (`WorldBookInjector`)
3. ⭐⭐⭐⭐⭐ ChatService集成
4. ⭐⭐⭐⭐ Assistant模型扩展

### Phase 2: UI界面 (第2周)
5. ⭐⭐⭐⭐ Jetpack Compose UI重写
6. ⭐⭐⭐⭐ 聊天界面入口
7. ⭐⭐⭐ 导航路由配置

### Phase 3: 增强功能 (第3周)
8. ⭐⭐⭐ 记忆表格查询接口
9. ⭐⭐ 单元测试
10. ⭐ 文档和示例

---

## 🎯 关键决策点

### 需要用户确认的设计选择:

1. **世界书注入格式**
   - 选项A: 作为system role消息注入
   - 选项B: 作为特殊标记`[World Info]...[/World Info]`插入到对话中
   - 选项C: 追加到系统提示词
   - **推荐**: 选项A (更符合OpenAI API标准)

2. **匹配范围**
   - 选项A: 只匹配最后一条用户消息
   - 选项B: 匹配最近N条消息 (N=5)
   - 选项C: 匹配所有对话历史
   - **推荐**: 选项B (平衡性能和准确性)

3. **记忆表格的AI访问方式**
   - 选项A: 作为Tool供AI主动查询
   - 选项B: 自动注入到上下文
   - 选项C: 两者结合 (手动选择)
   - **推荐**: 选项A (给AI更大灵活性)

4. **UI风格**
   - 继续使用当前Material 3风格
   - 与现有Assistant管理页面保持一致
   - **确认**: 是否需要特殊的富文本编辑器?

5. **递归深度限制**
   - 默认值: 3层
   - 最大值: 5层
   - **确认**: 是否需要用户可配置?

---

## 📊 预估工作量

- **核心功能实现**: 40小时
  - WorldBookMatcher: 10小时
  - WorldBookInjector: 8小时
  - ChatService集成: 6小时
  - MemoryTableQueryService: 8小时
  - 调试和优化: 8小时

- **UI界面重写**: 30小时
  - WorldBookManagementPage: 8小时
  - WorldBookEditorPage: 10小时
  - MemoryTablePages: 10小时
  - 导航和集成: 2小时

- **测试和文档**: 10小时
  - 单元测试: 6小时
  - 文档编写: 4小时

**总计**: 约80小时 (2-3周全职工作)

---

## ✅ 下一步行动

请确认以下事项后我们再开始实现:

1. [ ] 确认世界书注入格式选择 (选项A/B/C)
2. [ ] 确认匹配范围策略 (选项A/B/C)
3. [ ] 确认记忆表格访问方式 (选项A/B/C)
4. [ ] 确认UI组件需求 (是否需要富文本编辑器)
5. [ ] 确认递归深度是否需要用户配置
6. [ ] 是否需要先创建简单原型验证匹配逻辑?
7. [ ] 是否需要我先实现WorldBookMatcher作为起点?

---

**生成时间**: 2025-10-27  
**文档版本**: v1.0  
**作者**: Roo AI Assistant
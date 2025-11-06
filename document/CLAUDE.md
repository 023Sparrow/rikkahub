# 📄 CLAUDE.md - Document Module

> **面包屑**: `根目录` → `document` → **Document Module**

## 📋 Module Overview

**document** 是 RikkaHub 的文档解析模块，专门处理 PDF 和 DOCX 文件的文本提取和格式化。支持将复杂文档格式转换为 Markdown 文本，保持原始文档的结构和格式，包括标题、段落、表格、列表、粗体和斜体等格式信息。

## 🏗️ Architecture

### 🗂️ Core Components

- **`PdfParser`**: PDF 文档解析器
  - **MuPDF 集成**: 使用 MuPDF 库进行高质量 PDF 文本提取
  - **分页处理**: 按页面分块提取文本，便于长文档处理
  - **结构化输出**: 生成带页面标记的结构化文本

- **`DocxParser`**: DOCX 文档解析器
  - **XML 解析**: 直接解析 DOCX 文件中的 XML 内容
  - **ZIP 处理**: 解析 DOCX 的 ZIP 包装格式，提取 `word/document.xml`
  - **格式保持**: 完整保留文档的格式化信息
  - **Markdown 转换**: 智能转换为 Markdown 格式

- **解析状态管理**:
  - `ListInfo`: 列表信息数据结构（层级、编号类型、序号）
  - **XML 解析器**: XmlPullParser 实现高效流式解析

### 🔧 Key Technologies

- **MuPDF**: 高性能 PDF 渲染和文本提取库
- **XmlPullParser**: 轻量级 XML 流式解析
- **ZipInputStream**: DOCX 文件 ZIP 格式处理
- **Kotlin Coroutines**: 异步文档解析支持（可选）

## 🚀 Key Features

### PDF 文档解析
- **文本提取**: 从 PDF 中准确提取文本内容
- **分页处理**: 自动识别和标记页面边界
- **结构保持**: 保持原始 PDF 的文本结构
- **MuPDF 优化**: 利用 MuPDF 的高性能文本提取能力

### DOCX 文档解析
- **完整格式支持**:
  - 标题（H1-H6）和段落
  - 粗体（**）、斜体（*）、粗斜体（***）
  - 有序和无序列表（多层级支持）
  - 表格（自动转换为 Markdown 表格格式）
- **智能转换**: 将 Word 文档格式智能转换为 Markdown
- **层级处理**: 支持多级列表和缩进
- **表格识别**: 自动识别表格结构并转换为标准 Markdown 表格

### Markdown 输出格式
- **标准兼容**: 生成标准 Markdown 格式，便于后续处理
- **格式标记**: 完整保留文档的视觉层次和格式信息
- **跨平台**: 输出内容可在各种 Markdown 渲染器中正确显示

## 🔗 Dependencies

**内部模块依赖**:
- 无直接内部依赖（独立模块）

**外部依赖**:
- **MuPDF (com.artifex.mupdf)**: PDF 处理核心库
- **Android XML Parser**: XmlPullParser（Android 内置）
- **Java.util.zip**: ZIP 文件处理（Android 内置）
- **Kotlin 标准库**: 基本数据类型和集合

## 📁 Critical Files

- `PdfParser.kt`: PDF 文本提取核心实现
- `DocxParser.kt`: DOCX 文档解析和格式转换核心实现
- `ListInfo.kt`: 列表处理辅助数据结构

## 🎨 Usage Patterns

### 基本 PDF 解析
```kotlin
// PDF 文档文本提取
val pdfFile = File("/path/to/document.pdf")
val extractedText = PdfParser.parserPdf(pafFile)

println("PDF 解析结果:")
println(extractedText)
/*
输出示例:
---Page 1:
这是一个 PDF 文档的内容
包含多个段落的文本信息

---Page 2:
第二页的内容...
*/
```

### 基本 DOCX 解析
```kotlin
// DOCX 文档解析和 Markdown 转换
val docxFile = File("/path/to/document.docx")
val markdownText = DocxParser.parse(docxFile)

println("DOCX 解析结果:")
println(markdownText)
/*
输出示例:
# 文档标题

这是正文段落，**包含粗体文本**和*斜体文本*。

## 二级标题

- 列表项 1
- 列表项 2
  - 子列表项 1
  - 子列表项 2

### 三级标题

| 列 1 | 列 2 | 列 3 |
| --- | --- | --- |
| A   | B   | C   |
| 1   | 2   | 3   |

有序列表:
1. 第一项
2. 第二项
3. 第三项
*/
```

### 完整文档处理示例
```kotlin
@Composable
fun DocumentProcessorScreen() {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var processedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        // 文件选择器
        Button(
            onClick = {
                // 启动文档选择器
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                // 在实际应用中使用 Activity Result API
            }
        ) {
            Text("选择文档")
        }

        if (selectedFile != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isProcessing = true
                    lifecycleScope.launch {
                        try {
                            processedText = when (selectedFile!!.extension.lowercase()) {
                                "pdf" -> PdfParser.parserPdf(selectedFile!!)
                                "docx" -> DocxParser.parse(selectedFile!!)
                                else -> "不支持的文件格式"
                            }
                        } catch (e: Exception) {
                            processedText = "解析错误: ${e.message}"
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                enabled = !isProcessing
            ) {
                Text(if (isProcessing) "解析中..." else "开始解析")
            }
        }

        // 解析结果显示
        if (processedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "解析结果:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 使用滚动容器显示长文本
            LazyColumn {
                item {
                    Text(
                        text = processedText,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // 复制到剪贴板
                    val clipboard = ContextCompat.getSystemService(
                        LocalContext.current,
                        ClipboardManager::class.java
                    )
                    clipboard?.setPrimaryClip(
                        ClipData.newPlainText("parsed_text", processedText)
                    )
                    // 显示复制成功提示
                }
            ) {
                Text("复制到剪贴板")
            }
        }
    }
}
```

### 与 AI 聊天集成
```kotlin
class DocumentProcessor {
    suspend fun processDocumentForAI(file: File): Result<ProcessedDocument> {
        return runCatching {
            val text = when (file.extension.lowercase()) {
                "pdf" -> PdfParser.parserPdf(file)
                "docx" -> DocxParser.parse(file)
                else -> throw IllegalArgumentException("不支持的文件格式")
            }

            ProcessedDocument(
                originalFileName = file.name,
                content = text,
                wordCount = text.split(Regex("\\s+")).size,
                pageCount = countPagesInText(text),
                format = when (file.extension.lowercase()) {
                    "pdf" -> DocumentFormat.PDF
                    "docx" -> DocumentFormat.DOCX
                    else -> DocumentFormat.UNKNOWN
                }
            )
        }
    }

    private fun countPagesInText(text: String): Int {
        // 简单的页面计数方法
        return text.count { it == '-' && it == '-' && it == '-' }
    }
}

data class ProcessedDocument(
    val originalFileName: String,
    val content: String,
    val wordCount: Int,
    val pageCount: Int,
    val format: DocumentFormat
)

enum class DocumentFormat {
    PDF,
    DOCX,
    UNKNOWN
}

// 在聊天界面中使用
@Composable
fun DocumentChatScreen() {
    val documentProcessor = remember { DocumentProcessor() }
    var uploadedDocuments by remember { mutableStateOf<List<ProcessedDocument>>(emptyList()) }

    // 文件上传处理
    fun handleDocumentUpload(file: File) {
        lifecycleScope.launch {
            documentProcessor.processDocumentForAI(file).fold(
                onSuccess = { processedDoc ->
                    uploadedDocuments = uploadedDocuments + processedDoc
                    // 自动发送到 AI 进行分析
                    sendToAI(processedDoc.content)
                },
                onFailure = { error ->
                    // 显示错误消息
                    showError("文档解析失败: ${error.message}")
                }
            )
        }
    }
}
```

### 批量文档处理
```kotlin
class BatchDocumentProcessor {
    suspend fun processMultipleDocuments(
        files: List<File>,
        progressCallback: (Int, Int) -> Unit = { _, _ -> }
    ): List<ProcessedDocument> {
        val results = mutableListOf<ProcessedDocument>()

        files.forEachIndexed { index, file ->
            try {
                val processed = when (file.extension.lowercase()) {
                    "pdf" -> PdfParser.parserPdf(file)
                    "docx" -> DocxParser.parse(file)
                    else -> {
                        "不支持的文件格式: ${file.extension}"
                    }
                }

                results.add(
                    ProcessedDocument(
                        originalFileName = file.name,
                        content = processed,
                        wordCount = processed.split(Regex("\\s+")).size,
                        pageCount = if (file.extension.lowercase() == "pdf") {
                            processed.count { it == '-' && it == '-' && it == '-' }
                        } else 1,
                        format = when (file.extension.lowercase()) {
                            "pdf" -> DocumentFormat.PDF
                            "docx" -> DocumentFormat.DOCX
                            else -> DocumentFormat.UNKNOWN
                        }
                    )
                )
            } catch (e: Exception) {
                // 记录错误但继续处理其他文件
                println("处理文件 ${file.name} 时出错: ${e.message}")
            }

            progressCallback(index + 1, files.size)
        }

        return results
    }
}

// 在 ViewModel 中使用
@HiltViewModel
class DocumentViewModel @Inject constructor() : ViewModel() {
    private val batchProcessor = BatchDocumentProcessor()

    private val _processingProgress = MutableStateFlow(ProcessingProgress())
    val processingProgress: StateFlow<ProcessingProgress> = _processingProgress.asStateFlow()

    private val _processedDocuments = MutableStateFlow<List<ProcessedDocument>>(emptyList())
    val processedDocuments: StateFlow<List<ProcessedDocument>> = _processedDocuments.asStateFlow()

    fun processBatch(documents: List<File>) {
        viewModelScope.launch {
            _processingProgress.value = ProcessingProgress(isProcessing = true, current = 0, total = documents.size)

            try {
                val results = batchProcessor.processMultipleDocuments(
                    files = documents,
                    progressCallback = { current, total ->
                        _processingProgress.value = ProcessingProgress(
                            isProcessing = true,
                            current = current,
                            total = total
                        )
                    }
                )

                _processedDocuments.value = results
                _processingProgress.value = ProcessingProgress(isProcessing = false, current = documents.size, total = documents.size)

            } catch (e: Exception) {
                // 处理整体错误
                _processingProgress.value = ProcessingProgress(
                    isProcessing = false,
                    error = "批量处理失败: ${e.message}"
                )
            }
        }
    }
}

data class ProcessingProgress(
    val isProcessing: Boolean = false,
    val current: Int = 0,
    val total: Int = 0,
    val error: String? = null
)
```

## 🔄 Integration Patterns

### 与 app 模块集成
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DocumentModule {
    @Provides
    @Singleton
    fun provideDocumentProcessor(): DocumentProcessor {
        return DocumentProcessor()
    }

    @Provides
    @Singleton
    fun provideBatchDocumentProcessor(): BatchDocumentProcessor {
        return BatchDocumentProcessor()
    }
}

// 在 Repository 中使用
@Singleton
class DocumentRepository @Inject constructor(
    private val documentProcessor: DocumentProcessor
) {
    suspend fun parseDocument(file: File): Result<ProcessedDocument> {
        return documentProcessor.processDocumentForAI(file)
    }

    suspend fun parseDocuments(files: List<File>): Result<List<ProcessedDocument>> {
        return runCatching {
            files.map { file ->
                parseDocument(file).getOrThrow()
            }
        }
    }
}
```

### 搜索和索引集成
```kotlin
class DocumentIndexer {
    private val searchableContent = mutableListOf<IndexedDocument>()

    suspend fun indexDocument(file: File): IndexedDocument {
        val processed = when (file.extension.lowercase()) {
            "pdf" -> PdfParser.parserPdf(file)
            "docx" -> DocxParser.parse(file)
            else -> throw IllegalArgumentException("不支持的文件格式")
        }

        val indexedDoc = IndexedDocument(
            fileName = file.name,
            filePath = file.absolutePath,
            content = processed,
            searchableText = processed.lowercase(),
            keywords = extractKeywords(processed),
            lastModified = file.lastModified()
        )

        searchableContent.add(indexedDoc)
        return indexedDoc
    }

    fun searchInDocuments(query: String): List<SearchResult> {
        return searchableContent
            .mapNotNull { doc ->
                val matches = findMatches(doc.searchableText, query.lowercase())
                if (matches.isNotEmpty()) {
                    SearchResult(
                        document = doc,
                        matches = matches,
                        relevanceScore = calculateRelevance(matches)
                    )
                } else null
            }
            .sortedByDescending { it.relevanceScore }
    }

    private fun extractKeywords(text: String): List<String> {
        return text.split(Regex("\\W+"))
            .filter { it.length > 3 }
            .map { it.lowercase() }
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 2 }
            .keys
            .take(20)
            .toList()
    }
}

data class IndexedDocument(
    val fileName: String,
    val filePath: String,
    val content: String,
    val searchableText: String,
    val keywords: List<String>,
    val lastModified: Long
)

data class SearchResult(
    val document: IndexedDocument,
    val matches: List<TextMatch>,
    val relevanceScore: Double
)

data class TextMatch(
    val startIndex: Int,
    val endIndex: Int,
    val context: String
)
```

## 🧪 Testing

- **单元测试**: 在 `src/test/java/me/rerere/document/` 目录
- **PDF 测试**: 创建测试 PDF 文件，验证文本提取准确性
- **DOCX 测试**: 使用各种 Word 文档格式测试解析器
- **格式保持测试**: 验证格式转换的准确性
- **错误处理测试**: 测试各种错误情况下的处理

## 🔐 Security & Performance

### 安全特性
- **文件验证**: 输入文件格式和内容验证
- **内存安全**: 流式处理避免大文件内存溢出
- **异常处理**: 完整的错误捕获和恢复机制
- **资源管理**: 自动释放文件和流资源

### 性能优化
- **流式处理**: XML 和 ZIP 文件使用流式解析
- **内存复用**: 避免不必要的字符串创建和复制
- **MuPDF 优化**: 利用 MuPDF 的高效 PDF 解析
- **并发处理**: 支持多文档并发解析

### 监控指标
- **解析时间**: 文档解析耗时统计
- **成功率**: 文档解析成功率跟踪
- **文件大小**: 支持的文件大小限制
- **内存使用**: 解析过程内存使用监控

## 🎯 扩展指南

### 添加新文档格式支持
```kotlin
// 添加 TXT 文件支持
object TxtParser {
    fun parse(file: File): String {
        return file.readText(Charset.defaultCharset())
    }
}

// 添加 Markdown 文件支持
object MarkdownParser {
    fun parse(file: File): String {
        return file.readText(Charset.defaultCharset())
    }
}

// 统一的文档解析管理器
object DocumentParserManager {
    fun parseDocument(file: File): Result<String> {
        return runCatching {
            when (file.extension.lowercase()) {
                "pdf" -> PdfParser.parserPdf(file)
                "docx" -> DocxParser.parse(file)
                "txt" -> TxtParser.parse(file)
                "md", "markdown" -> MarkdownParser.parse(file)
                else -> throw UnsupportedOperationException("不支持的文件格式: ${file.extension}")
            }
        }
    }

    fun getSupportedExtensions(): List<String> {
        return listOf("pdf", "docx", "txt", "md", "markdown")
    }
}
```

### 自定义格式转换器
```kotlin
class CustomFormatConverter {
    // HTML 格式转换
    fun convertToHtml(markdownText: String): String {
        return buildString {
            append("<html><body>")
            append(markdownToHtml(markdownText))
            append("</body></html>")
        }
    }

    // 纯文本格式转换
    fun convertToPlainText(markdownText: String): String {
        return markdownText
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")  // 移除粗体标记
            .replace(Regex("\\*([^*]+)\\*"), "$1")        // 移除斜体标记
            .replace(Regex("^#{1,6}\\s+", RegexOption.MULTILINE), "") // 移除标题标记
            .replace(Regex("^[-*+]\\s+", RegexOption.MULTILINE), "• ") // 转换无序列表
            .replace(Regex("^\\d+\\.\\s+", RegexOption.MULTILINE)) { matchResult ->
                val number = matchResult.value.substringBefore(".")
                "$number. "
            }
    }

    private fun markdownToHtml(markdown: String): String {
        return markdown
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "<strong>$1</strong>")
            .replace(Regex("\\*([^*]+)\\*"), "<em>$1</em>")
            .replace(Regex("^#{1,6}\\s+(.+)$", RegexOption.MULTILINE)) { matchResult ->
                val text = matchResult.groupValues[1]
                val level = matchResult.value.indexOf(' ').coerceAtMost(6)
                "<h$level>$text</h$level>"
            }
    }
}
```

---

**📖 相关文档**:
- [根目录 CLAUDE.md](../CLAUDE.md)
- [app 模块 CLAUDE.md](../app/CLAUDE.md)
- [MuPDF 文档](https://mupdf.com/)
- [DOCX 文件格式规范](https://officeopenxml.com/an-introduction-to-office-open-xml-formats.php)
- [XML Pull Parser 文档](https://www.xmlpull.org/)
- [Markdown 格式规范](https://www.markdownguide.org/)
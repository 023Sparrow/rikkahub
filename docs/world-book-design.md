# 世界书与记忆表格功能设计文档

## 功能概述

基于SillyTavern的世界书(World Info)和记忆表格(Memory Enhancement)功能，为RikkaHub Android应用设计相应的本地化实现。

## 1. SillyTavern世界书功能分析

### 核心特性
- **条目管理**: 创建、编辑、删除世界书条目
- **关键词匹配**: 支持多个关键词，支持正则表达式
- **条件触发**: AND/OR逻辑组合，优先级控制
- **上下文注入**: 自动将相关信息注入到AI对话上下文
- **递归扫描**: 条目内容可以触发其他条目
- **字符限制**: 控制注入内容的长度

### 数据结构需求
```typescript
interface WorldInfoEntry {
  id: string
  key: string[]          // 触发关键词列表
  keysecondary: string[] // 次要关键词
  content: string        // 条目内容
  comment: string        // 备注
  constant: boolean      // 是否总是激活
  selective: boolean     // 选择性匹配
  order: number         // 优先级/顺序
  position: number      // 注入位置
  disable: boolean      // 是否禁用
  addMemo: boolean      // 添加到记忆
  excludeRecursion: boolean // 排除递归扫描
  delayUntilRecursion: boolean // 延迟到递归
}
```

## 2. 记忆表格功能分析

### 核心特性
- **表格结构**: 类似电子表格的二维数据结构
- **动态更新**: 可以在对话过程中更新单元格内容
- **模板支持**: 预定义表格模板
- **上下文关联**: 与当前对话场景关联

### 数据结构需求
```typescript
interface MemoryTable {
  id: string
  name: string
  description: string
  assistantId: string
  rows: MemoryTableRow[]
  createdAt: number
  updatedAt: number
}

interface MemoryTableRow {
  id: string
  tableId: string
  cells: { [columnName: string]: string }
  order: number
}
```

## 3. Android实现设计

### 3.1 世界书数据模型

```kotlin
@Entity(tableName = "world_book_entry")
data class WorldBookEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "assistant_id")
    val assistantId: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "keywords")
    val keywords: String, // JSON数组存储关键词
    
    @ColumnInfo(name = "secondary_keywords")
    val secondaryKeywords: String = "[]",
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "comment")
    val comment: String = "",
    
    @ColumnInfo(name = "is_constant")
    val isConstant: Boolean = false,
    
    @ColumnInfo(name = "is_selective")
    val isSelective: Boolean = false,
    
    @ColumnInfo(name = "priority")
    val priority: Int = 0,
    
    @ColumnInfo(name = "injection_position")
    val injectionPosition: Int = 0, // 0=开头, 1=结尾, 2=自定义
    
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    
    @ColumnInfo(name = "exclude_recursion")
    val excludeRecursion: Boolean = false,
    
    @ColumnInfo(name = "use_regex")
    val useRegex: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 3.2 记忆表格数据模型

```kotlin
@Entity(tableName = "memory_table")
data class MemoryTable(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "assistant_id")
    val assistantId: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "column_headers")
    val columnHeaders: String, // JSON数组存储列标题
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "memory_table_row",
    foreignKeys = [ForeignKey(
        entity = MemoryTable::class,
        parentColumns = ["id"],
        childColumns = ["table_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MemoryTableRow(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "table_id")
    val tableId: String,
    
    @ColumnInfo(name = "row_data")
    val rowData: String, // JSON对象存储行数据
    
    @ColumnInfo(name = "row_order")
    val rowOrder: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
```

## 4. 核心功能逻辑

### 4.1 关键词匹配算法
- 支持普通文本匹配和正则表达式匹配
- 实现AND/OR逻辑组合
- 优先级排序和去重

### 4.2 上下文注入策略
- 根据匹配结果确定注入内容
- 控制注入位置和长度
- 避免重复注入

### 4.3 递归扫描机制
- 检测条目内容是否触发其他条目
- 防止无限递归
- 深度限制

## 5. UI设计要点

### 5.1 世界书管理界面
- 条目列表视图（支持搜索和筛选）
- 条目编辑器（富文本编辑）
- 关键词管理（标签式输入）
- 预览和测试功能

### 5.2 记忆表格界面
- 表格视图（类似电子表格）
- 行列操作（增删改）
- 导入导出功能
- 模板管理

## 6. 技术实现重点

### 6.1 数据库设计
- 合理的外键约束
- 索引优化（关键词搜索）
- 数据迁移策略

### 6.2 性能优化
- 关键词匹配缓存
- 懒加载和分页
- 后台处理

### 6.3 数据同步
- 与现有助手系统集成
- 备份和恢复支持

## 7. 开发阶段

1. **Phase 1**: 基础数据模型和DAO实现
2. **Phase 2**: 核心匹配和注入逻辑
3. **Phase 3**: UI界面开发
4. **Phase 4**: 集成测试和优化
5. **Phase 5**: 用户测试和文档完善
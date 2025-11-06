# 🧭 CLAUDE.md - App Module

> **面包屑**: `根目录` → `app` → **App Module**

## 📋 Module Overview

**app** 是 RikkaHub 的主应用模块，负责 UI 层、ViewModels、核心业务逻辑和数据层管理。采用 Jetpack Compose 和 Material Design 3 构建现代化的 Android 界面。

## 🏗️ Architecture

### 🗂️ Core Packages

- **`data/`**: 数据层实现
  - `ai/`: AI 相关功能（日志记录、请求拦截、生成处理）
  - `ai/mcp/`: MCP (Model Context Protocol) 协议支持
  - `ai/prompts/`: 提示词管理
  - `ai/tools/`: 本地工具系统
  - `ai/transformers/`: AI 数据转换器
  - `database/`: Room 数据库实体和 DAO
  - `datastore/`: DataStore 偏好设置存储
  - `repository/`: 数据仓库模式

- **`ui/pages/`**: 页面实现和 ViewModels
  - `chat/`: 聊天界面
  - `setting/`: 设置页面
  - `profile/`: 用户资料

- **`ui/components/`**: 可复用 UI 组件
  - 遵循 Material Design 3
  - 使用 Lucide 图标
  - 支持国际化

- **`di/`**: 依赖注入模块
  - Koin 容器配置
  - 单例和工厂模式

- **`utils/`**: 工具函数和扩展

### 🔧 Key Technologies

- **Jetpack Compose**: 现代 UI 工具包
- **Room**: 数据库 ORM，支持迁移
- **DataStore**: 偏好设置存储
- **Navigation Compose**: 应用导航
- **Koin**: 依赖注入
- **OkHttp**: HTTP 客户端（支持 SSE）
- **Kotlinx Serialization**: JSON 处理

## 🚀 Key Features

### AI Integration
- MCP 协议支持（`ai/mcp/`）
- 本地工具系统（`ai/tools/LocalTools.kt`）
- 图像处理和转换（`ai/transformers/`）
- 提示词模板管理（`ai/prompts/`）

### UI Components
- 聊天界面组件
- 设置页面组件
- 响应式布局
- 深色/浅色主题支持

### Data Management
- Room 数据库集成
- DataStore 配置持久化
- 缓存策略
- 数据迁移支持

## 📁 Critical Files

- `data/datastore/PreferencesStore.kt`: 配置管理
- `ui/pages/setting/SettingProviderPage.kt`: UI 模式参考
- `data/database/AppDatabase.kt`: 数据库配置
- `di/AppModule.kt`: 依赖注入配置

## 🔗 Dependencies

**内部模块依赖**:
- `common`: 通用工具和缓存
- `ai`: AI SDK 和 Provider 抽象

**外部依赖**:
- Android Framework (Compose, Room, DataStore)
- 网络库 (OkHttp)
- 依赖注入 (Koin)
- JSON 处理 (Kotlinx Serialization)

## 📱 Platform Requirements

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Compile SDK**: 36
- **JVM Target**: 11

## 🔄 Integration Patterns

### AI Provider Configuration
```kotlin
// 在 app 模块中配置 AI Provider
class ProviderManager @Inject constructor(
    private val preferencesStore: PreferencesStore
) {
    // Provider 切换逻辑
}
```

### Database Integration
```kotlin
@Database(entities = [...], version = X)
abstract class AppDatabase : RoomDatabase() {
    // DAO 定义
}
```

## 🎨 UI Development Guidelines

### 组件使用
- 使用 `ui/components/` 中的现有组件
- 遵循 `SettingProviderPage.kt` 的布局模式
- 使用 `FormItem` 实现一致的表单布局
- 采用 `LocalToaster.current` 显示 Toast 消息

### 状态管理
- 使用 ViewModels 进行状态管理
- Compose State 和 StateFlow 模式
- 避免在 Composable 中执行业务逻辑

### 国际化
- 字符串资源在 `app/src/main/res/values-*/strings.xml`
- 使用 `stringResource(R.string.key_name)`
- 页面特定字符串使用页面前缀（如 `setting_page_`）

## 🧪 Testing

- **Instrumented Tests**: `src/androidTest/`
- **Unit Tests**: `src/test/`
- **基准测试**: `baselineprofile/` 目录

## 🔐 Security & Configuration

- 需要 `google-services.json` 文件支持 Firebase 功能
- 签名密钥在 `local.properties` 中配置
- API 密钥通过环境变量或 DataStore 管理

---

**📖 相关文档**:
- [根目录 CLAUDE.md](../CLAUDE.md)
- [ai 模块 CLAUDE.md](../ai/CLAUDE.md)
- [common 模块 CLAUDE.md](../common/CLAUDE.md)
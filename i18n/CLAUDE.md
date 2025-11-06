# 🌍 CLAUDE.md - I18n Module

> **面包屑**: `根目录` → `i18n` → **I18n Module**

## 📋 Module Overview

RikkaHub I18n is an AI-powered translation manager for Android string resources with an interactive Terminal UI (TUI). This tool manages translations for the main RikkaHub Android application, which is a native LLM chat client supporting multiple AI providers.

## 🏗️ Architecture

### 🗂️ Core Components

- **Config System** (`src/config.ts`): YAML-based configuration loader for targets, modules, and AI providers
- **XML Parser** (`src/xml-parser.ts`): Android string resource parser and generator with merge capabilities
- **Translator** (`src/translator.ts`): AI-powered translation service with Google Gemini and OpenAI support
- **Module Loader** (`src/module-loader.ts`): Multi-module Android project scanner and statistics calculator
- **TUI Components** (`src/tui/`): Interactive terminal interface built with React and Ink

### 🔧 Key Technologies

- **TypeScript**: Primary language with strict configuration
- **React + Ink**: Terminal UI framework for interactive components
- **Vercel AI SDK**: Unified AI provider interface (@ai-sdk/google, @ai-sdk/openai)
- **xml2js**: XML parsing and building for Android string resources
- **YAML**: Configuration file format

## 🚀 Key Features

### AI 智能翻译
- **多提供商支持**: Google Gemini 和 OpenAI 双引擎支持
- **上下文感知**: 基于模块和键名的上下文翻译
- **格式化保护**: 保持 Android 字符串格式占位符
- **智能日志**: 完整的翻译过程日志记录

### 交互式终端界面
- **TUI 组件**: 基于 React + Ink 的终端用户界面
- **模块导航**: 直观的模块选择和进度显示
- **翻译表格**: 实时翻译状态和统计信息
- **过滤功能**: 支持按缺失翻译项过滤显示

### Android 资源管理
- **多模块扫描**: 自动检测 Android 项目中的字符串资源
- **统计计算**: 实时计算翻译完成度和缺失项
- **文件结构**: 遵循 Android 标准资源目录结构
- **增量更新**: 只翻译缺失的条目，保留现有翻译

## 🛠️ Development Commands

### Development
```bash
# Start the interactive TUI (recommended for development)
npm run dev
# or
bun run dev
```

### Build and Production
```bash
# Compile TypeScript to dist/
npm run build

# Run compiled version
npm start
```

## 🔗 Dependencies

**内部模块依赖**:
- Android 项目模块：app、ai、highlight、search 等

**外部依赖**:
- **@ai-sdk/google**: Google Gemini AI 提供商
- **@ai-sdk/openai**: OpenAI 提供商
- **@ai-sdk/openai-compatible**: 兼容 OpenAI 的提供商标准
- **ai**: Vercel AI SDK 核心库
- **react**: React UI 框架
- **ink**: 终端 UI 框架
- **xml2js**: XML 解析和构建
- **yaml**: YAML 配置文件处理
- **dotenv**: 环境变量管理

## 📁 Critical Files

- `src/config.ts`: 配置加载和管理
- `src/translator.ts`: AI 翻译服务核心
- `src/xml-parser.ts`: Android 字符串资源解析
- `src/module-loader.ts`: 模块扫描和统计
- `src/index.tsx`: 主应用入口
- `src/tui/`: TUI 界面组件目录
- `config.yml`: 翻译配置文件

## 🎨 Usage Patterns

### 基本翻译流程
```bash
# 1. 启动交互式界面
cd i18n && npm run dev

# 2. 选择要翻译的 Android 模块
# 3. 选择目标语言
# 4. 查看翻译统计和缺失项
# 5. 开始批量翻译
```

### 配置翻译任务
```typescript
// config.yml 配置示例
targets:
  - zh          # 简体中文
  - ja          # 日语
  - zh-rTW      # 繁体中文

workspaceRoot: ".."
modules:
  - app
  - ai
  - common
  - highlight

provider:
  type: "google"           # google 或 openai
  model: "gemini-2.5-flash"
```

### API 调用翻译
```typescript
import { createTranslator } from './src/translator';
import { loadConfig } from './src/config';

async function translateStrings() {
  const config = loadConfig('./config.yml');
  const translator = createTranslator(config);

  const results = await translator.translateBatch({
    module: 'app',
    targetLanguage: 'zh',
    sourceLanguage: 'en',
    onProgress: (progress) => {
      console.log(`翻译进度: ${progress.current}/${progress.total}`);
    }
  });

  console.log(`翻译完成: ${results.successCount} 项成功`);
}
```

## 🔄 Integration Patterns

### 与 Android 项目集成
```bash
# i18n 工具自动检测 Android 项目的模块结构
# 支持的目录结构:
# - {module}/src/main/res/values/strings.xml
# - {module}/src/main/res/values-{locale}/strings.xml

# 翻译后的文件会自动保存到相应语言目录
# 保持 Android 资源文件的完整 XML 结构
```

### AI 提供商配置
```typescript
// 支持多种 AI 提供商
const providers = {
  google: {
    apiKey: process.env.GOOGLE_GENERATIVE_AI_API_KEY,
    model: 'gemini-2.5-flash'
  },
  openai: {
    apiKey: process.env.OPENAI_API_KEY,
    model: 'gpt-4'
  }
};

// 切换提供商的简单方法
const translator = createTranslator({
  provider: {
    type: 'google',  // 或 'openai'
    model: 'gemini-2.5-flash'
  }
});
```

## 🔐 Security & Performance

### 安全特性
- **API 密钥安全**: 通过环境变量管理敏感信息
- **输入验证**: 配置文件和 XML 格式验证
- **错误处理**: 完整的异常捕获和恢复机制
- **日志记录**: 详细的翻译过程日志

### 性能优化
- **并发控制**: 可配置的并发请求数量限制
- **速率限制**: 100ms 延迟避免 API 限流
- **流式处理**: 实时进度更新和状态反馈
- **内存优化**: 有效的 XML 解析和字符串处理

### 监控指标
- **翻译成功率**: 成功翻译的条目统计
- **API 调用次数**: 各提供商的调用监控
- **处理时间**: 翻译任务的总耗时统计
- **错误率**: 翻译失败和重试情况

## 🎯 扩展指南

### 添加新 AI 提供商
```typescript
// 1. 在 translator.ts 中扩展 getModel 函数
function getModel(config: I18nConfig) {
  switch (config.provider.type) {
    case 'google':
      return google(config.provider.model);
    case 'openai':
      return createOpenAICompatible({ apiKey: process.env.OPENAI_API_KEY! });
    case 'anthropic':  // 新提供商
      return anthropic(config.provider.model);
    default:
      throw new Error(`不支持的提供商类型: ${config.provider.type}`);
  }
}

// 2. 更新配置 schema 以支持新提供商
interface I18nConfig {
  provider: {
    type: 'google' | 'openai' | 'anthropic';  // 新增类型
    model: string;
  };
}
```

### 自定义翻译规则
```typescript
// 1. 扩展 XML 解析器以支持新格式
interface CustomStringResource extends StringResource {
  context?: string;  // 添加上下文字段
  comment?: string;  // 添加注释字段
}

// 2. 自定义翻译提示词模板
const TRANSLATION_PROMPTS = {
  android: `翻译为 Android 应用的 UI 文本，保持简洁友好`,
  marketing: `翻译为营销文案，需要吸引力和说服力`,
  technical: `翻译为技术文档，保持专业性和准确性`
};
```

### 添加新语言支持
```typescript
// 1. 在 LANGUAGE_NAMES 中添加新语言
const LANGUAGE_NAMES: Record<string, string> = {
  'zh': 'Simplified Chinese (简体中文)',
  'ja': 'Japanese (日本語)',
  'es': 'Spanish (Español)',
  'fr': 'French (Français)',
  'de': 'German (Deutsch)',
  'it': 'Italian (Italiano)',
  'pt': 'Portuguese (Português)',
  'ru': 'Russian (Русский)',
  'ko': 'Korean (한국어)',  // 新增韩语支持
};

// 2. 配置文件中添加目标语言
targets:
  - ko  # 添加到目标语言列表
```

## 🧪 Testing

- **单元测试**: 在 `src/test/` 目录（可选）
- **集成测试**: 测试完整的翻译流程
- **API 测试**: 模拟 AI 提供商响应
- **配置文件测试**: 验证配置加载和解析
- **TUI 测试**: 测试终端界面的交互流程

## Configuration

### Environment Setup
Create `.env` file with AI provider API keys:
```env
# For Google Gemini (default)
GOOGLE_GENERATIVE_AI_API_KEY=your_gemini_api_key_here

# For OpenAI 
OPENAI_API_KEY=your_openai_api_key_here
```

### Project Configuration (`config.yml`)
```yaml
# Target languages (Android resource directory suffixes)
targets:
  - zh          # Simplified Chinese
  - ja          # Japanese
  - zh-rTW      # Traditional Chinese

# Workspace root relative to i18n directory
workspaceRoot: ".."

# Android modules to scan for string resources
modules:
  - app
  - ai
  - highlight
  - search
  - rag

# AI provider configuration
provider:
  type: google        # "google" or "openai"
  model: gemini-2.5-flash
```

## File Structure and Paths

### Android String Resource Paths
- Default strings: `{modulePath}/src/main/res/values/strings.xml`
- Localized strings: `{modulePath}/src/main/res/values-{locale}/strings.xml`

### Key Files
- `config.yml`: Main configuration
- `logs.txt`: Translation process logs (auto-generated)
- `package.json`: Dependencies and scripts
- `tsconfig.json`: TypeScript configuration with strict settings

## Translation Process

### Workflow
1. Scans configured Android modules for `strings.xml` files
2. Compares default strings with existing translations
3. Calculates completion statistics per module/language
4. Uses AI to translate missing entries with context awareness
5. Preserves Android formatting (`%1$d`, `%1$s`, `\\n`, `\\'`)
6. Saves translations to appropriate `values-{locale}/strings.xml` files

### TUI Navigation
- **Module Selection**: ↑↓ navigate, Enter select, shows completion progress
- **Language Selection**: ↑↓ navigate, Enter select, shows translation statistics  
- **Translation Table**: ↑↓ navigate, `e` edit, `t` translate all, `f` filter missing, `q` back
- **Edit Mode**: Type to edit, Enter save, Esc cancel

### AI Translation Features
- Context-aware translations with module and key information
- Automatic rate limiting with 100ms delays
- Error handling with fallback to original text
- Comprehensive logging to `logs.txt`
- Support for Android string formatting preservation

## Supported Languages

Current target languages with full language names for AI context:
- `zh`: Simplified Chinese (简体中文)
- `zh-rTW`: Traditional Chinese (繁體中文) 
- `ja`: Japanese (日本語)
- Additional languages can be added to `LANGUAGE_NAMES` mapping

## Development Guidelines

### Code Style
- Follows strict TypeScript configuration
- Uses ESNext modules with bundler resolution
- Implements proper error handling and logging
- React functional components with hooks

### Adding New AI Providers
Extend `getModel()` function in `translator.ts` and add configuration options to support additional Vercel AI SDK providers.

### Module Detection
The tool automatically scans for Android modules containing `src/main/res/values/strings.xml` files. Non-existent modules are skipped with warnings.

### Error Handling
- File permission issues: Check write access to Android module directories
- API rate limits: Built-in delays and retry logic
- Missing translations: Filter functionality to focus on incomplete items
- API key issues: Verify `.env` configuration and quota
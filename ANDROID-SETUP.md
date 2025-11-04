# Android 开发环境配置指南

## 概述

本指南将帮助您配置 RikkaHub 项目的 Android 开发环境，包括 Java、Android SDK 和构建工具的安装和配置。

## 系统要求

- **操作系统**: Windows 10/11, macOS 10.15+, 或 Linux (Ubuntu 20.04+)
- **Java**: OpenJDK 17 或更高版本
- **Android SDK**: API Level 36 (Android 16)
- **Gradle**: 8.10.2 或更高版本
- **内存**: 最少 8GB RAM (推荐 16GB)
- **磁盘空间**: 最少 10GB 可用空间

## 快速开始

### 自动配置 (Linux/macOS)

运行自动化脚本：

```bash
chmod +x setup-android-env.sh
./setup-android-env.sh
```

### Windows 用户

参考下方的手动安装步骤，或运行 `setup-android-env-windows.bat`。

## 手动安装步骤

### 1. 安装 Java 17

#### Windows
1. 访问 [Adoptium](https://adoptium.net/)
2. 下载 OpenJDK 17 (选择 "Windows x64 MSI")
3. 运行安装程序
4. 验证安装：
   ```cmd
   java -version
   ```

#### macOS
```bash
# 使用 Homebrew
brew install openjdk@17

# 或使用 SDKMAN
sdk install java 17.0.2-open
```

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

#### CentOS/RHEL
```bash
sudo yum install java-17-openjdk-devel
```

#### Arch Linux
```bash
sudo pacman -S jdk17-openjdk
```

### 2. 设置 JAVA_HOME

#### Windows
1. 右键"此电脑" → 属性 → 高级系统设置
2. 点击"环境变量"
3. 新建系统变量：
   - 变量名: `JAVA_HOME`
   - 变量值: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot` (根据实际路径)

#### Linux/macOS
添加到 `~/.bashrc` 或 `~/.zshrc`:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)  # macOS
# 或
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk    # Linux

export PATH=$JAVA_HOME/bin:$PATH
```

然后重新加载配置：
```bash
source ~/.bashrc  # 或 ~/.zshrc
```

### 3. 安装 Android SDK

#### 下载 SDK Command Line Tools

1. 访问 [Android Studio](https://developer.android.com/studio#command-tools)
2. 下载适合您系统的 Command Line Tools
3. 解压到合适的位置，如：
   - Windows: `C:\Users\YourName\AppData\Local\Android\Sdk`
   - macOS/Linux: `~/Android/Sdk`

#### 设置环境变量

##### Windows
```cmd
setx ANDROID_HOME "C:\Users\%USERNAME%\AppData\Local\Android\Sdk"
setx PATH "%ANDROID_HOME%\cmdline-tools\latest\bin;%ANDROID_HOME%\platform-tools;%PATH%"
```

##### Linux/macOS
添加到 `~/.bashrc`:
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
export PATH=$ANDROID_HOME/platform-tools:$PATH
```

重新加载配置：
```bash
source ~/.bashrc
```

### 4. 安装必要的 SDK 组件

```bash
# 接受所有许可证
yes | sdkmanager --licenses

# 安装必需组件
sdkmanager "platform-tools"
sdkmanager "platforms;android-36"
sdkmanager "build-tools;36.0.0"
sdkmanager "extras;android;m2repository"
```

### 5. 验证安装

检查 Java:
```bash
java -version
echo $JAVA_HOME
```

检查 Android SDK:
```bash
sdkmanager --version
echo $ANDROID_HOME
```

检查项目:
```bash
cd /path/to/rikkahub
ls -la app/build.gradle.kts
ls -la settings.gradle.kts
```

## 验证配置

### 运行构建测试

```bash
cd /path/to/rikkahub

# 检查 Gradle 配置
./gradlew tasks

# 运行完整构建
./gradlew build

# 运行单元测试
./gradlew test

# 生成调试 APK
./gradlew assembleDebug
```

### 预期输出示例

```
> ./gradlew build

BUILD SUCCESSFUL in 2m 30s
1234 actionable tasks: 1234 executed
```

## 常见问题

### 问题 1: Java 版本不匹配

**错误**:
```
UnsupportedClassVersionError: Unsupported major.minor version
```

**解决方案**: 确保使用 Java 17 或更高版本
```bash
java -version
# 应该显示 17.x.x 或更高
```

### 问题 2: ANDROID_HOME 未设置

**错误**:
```
ANDROID_HOME is not set
```

**解决方案**: 设置环境变量（见上方步骤 3）

### 问题 3: SDK 组件缺失

**错误**:
```
SDK location not found
```

**解决方案**: 安装必需的 SDK 组件
```bash
sdkmanager "platforms;android-36"
sdkmanager "build-tools;36.0.0"
```

### 问题 4: Gradle 构建失败

**错误**:
```
Failed to sync project
```

**解决方案**:
1. 清理构建缓存:
   ```bash
   ./gradlew clean
   rm -rf ~/.gradle/caches
   ```
2. 重新下载依赖:
   ```bash
   ./gradlew build --refresh-dependencies
   ```

### 问题 5: 权限错误 (Linux/macOS)

**错误**:
```
Permission denied
```

**解决方案**:
```bash
chmod +x ./gradlew
chmod +x gradlew
```

## IDE 配置 (可选)

### Android Studio

1. 安装 Android Studio
2. 打开项目：
   - File → Open → 选择 RikkaHub 项目
3. 等待 Gradle Sync 完成
4. 运行测试：
   - Run → Edit Configurations → Add New Configuration → JUnit

### VS Code

安装扩展:
- Kotlin
- Gradle for Java
- Android

## 性能优化

### 启用 Gradle Daemon

```bash
# 永久启用
echo "org.gradle.daemon=true" >> ~/.gradle/gradle.properties

# 或项目级别
echo "org.gradle.daemon=true" >> gradle.properties
```

### 配置内存

在 `gradle.properties` 中添加:
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
```

## 项目构建命令

### 常用命令

| 命令 | 说明 |
|------|------|
| `./gradlew build` | 完整构建项目 |
| `./gradlew clean` | 清理构建缓存 |
| `./gradlew assembleDebug` | 生成调试 APK |
| `./gradlew assembleRelease` | 生成发布 APK |
| `./gradlew test` | 运行单元测试 |
| `./gradlew connectedAndroidTest` | 运行集成测试 |
| `./gradlew :app:lint` | 运行 Lint 检查 |
| `./gradlew :app:dependencies` | 查看依赖树 |

### 测试特定模块

```bash
# 测试 app 模块
./gradlew :app:test

# 测试特定测试类
./gradlew :app:test --tests="*WorldBook*"

# 生成测试报告
./gradlew :app:testDebugUnitTest
```

## 下一步

完成环境配置后，您可以：

1. **验证世界书功能**:
   - 创建世界书条目
   - 测试关键词匹配
   - 验证内容注入

2. **验证记忆表格功能**:
   - 创建记忆表格
   - 测试行/列编辑
   - 验证搜索功能

3. **测试 AI 工具**:
   - 验证记忆表格查询工具
   - 测试 AI 响应

4. **创建 Pull Request**:
   - 在 GitHub 上创建 PR
   - 提交代码审查

## 资源链接

- [Android Developer 官网](https://developer.android.com/)
- [Gradle 用户指南](https://docs.gradle.org/)
- [Kotlin 官方文档](https://kotlinlang.org/)
- [Jetpack Compose 指南](https://developer.android.com/jetpack/compose)

## 支持

如果遇到问题：

1. 检查本指南的"常见问题"部分
2. 查看项目 [README.md](../README.md)
3. 在 GitHub 上提交 Issue

---

**最后更新**: 2025-11-03
**维护者**: RikkaHub 开发团队

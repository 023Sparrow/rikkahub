#!/bin/bash

# Android 开发环境配置脚本
# 用于设置 RikkaHub 项目的 Android 开发环境

set -e

echo "=========================================="
echo "  Android 开发环境配置脚本"
echo "=========================================="
echo

# 检测操作系统
OS="$(uname -s)"
case "${OS}" in
    Linux*)     MACHINE=Linux;;
    Darwin*)    MACHINE=Mac;;
    CYGWIN*)    MACHINE=Cygwin;;
    MINGW*)     MACHINE=MinGw;;
    *)          MACHINE="UNKNOWN:${OS}"
esac

echo "检测到操作系统: $MACHINE"
echo

# 1. 安装 Java
echo "步骤 1: 检查和安装 Java..."
if command -v java &> /dev/null; then
    echo "✅ Java 已安装:"
    java -version
else
    echo "❌ Java 未安装，正在安装..."

    if [[ "$MACHINE" == "Linux" ]]; then
        # Ubuntu/Debian
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y openjdk-17-jdk
        # CentOS/RHEL
        elif command -v yum &> /dev/null; then
            sudo yum install -y java-17-openjdk-devel
        # Arch Linux
        elif command -v pacman &> /dev/null; then
            sudo pacman -S --noconfirm jdk17-openjdk
        else
            echo "❌ 不支持的 Linux 发行版，请手动安装 Java 17"
            exit 1
        fi
    elif [[ "$MACHINE" == "Mac" ]]; then
        if command -v brew &> /dev/null; then
            brew install openjdk@17
        elif command -v port &> /dev/null; then
            sudo port install openjdk17
        else
            echo "❌ 请先安装 Homebrew 或 MacPorts"
            exit 1
        fi
    else
        echo "❌ 不支持的操作系统，请手动安装 Java 17"
        exit 1
    fi

    # 设置 JAVA_HOME
    if [[ "$MACHINE" == "Linux" ]]; then
        JAVA_PATH=$(dirname $(dirname $(readlink -f $(which java))))
        echo "export JAVA_HOME=$JAVA_PATH" >> ~/.bashrc
        echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
        source ~/.bashrc
    elif [[ "$MACHINE" == "Mac" ]]; then
        echo "请手动设置 JAVA_HOME:"
        echo "export JAVA_HOME=/usr/local/opt/openjdk@17"
    fi

    echo "✅ Java 安装完成"
fi

echo
echo "Java 版本验证:"
java -version
echo

# 2. 安装 Android SDK
echo "步骤 2: 检查和安装 Android SDK..."

if [ -z "$ANDROID_HOME" ]; then
    echo "❌ ANDROID_HOME 未设置"
    echo "正在下载 Android SDK..."

    # 创建 Android SDK 目录
    ANDROID_SDK_DIR="$HOME/Android/Sdk"
    mkdir -p "$ANDROID_SDK_DIR"

    cd "$ANDROID_SDK_DIR"

    if [[ "$MACHINE" == "Linux" ]]; then
        wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
        unzip cmdline-tools.zip
        mkdir -p cmdline-tools/latest
        mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
        rm cmdline-tools.zip
    elif [[ "$MACHINE" == "Mac" ]]; then
        curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip
        unzip cmdline-tools.zip
        mkdir -p cmdline-tools/latest
        mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
        rm cmdline-tools.zip
    else
        echo "❌ 请手动下载 Android SDK Command Line Tools"
        exit 1
    fi

    # 设置 ANDROID_HOME
    echo "export ANDROID_HOME=$ANDROID_SDK_DIR" >> ~/.bashrc
    echo "export PATH=\$ANDROID_HOME/cmdline-tools/latest/bin:\$PATH" >> ~/.bashrc
    echo "export PATH=\$ANDROID_HOME/platform-tools:\$PATH" >> ~/.bashrc
    source ~/.bashrc

    echo "✅ Android SDK 下载完成"
else
    echo "✅ ANDROID_HOME 已设置: $ANDROID_HOME"
fi

echo
echo "Android SDK 路径: $ANDROID_HOME"
echo

# 3. 安装必要的 SDK 组件
echo "步骤 3: 安装必要的 SDK 组件..."

# 接受所有许可证
yes | sdkmanager --licenses 2>/dev/null || true

# 安装 SDK 组件
echo "正在安装 SDK 组件..."
sdkmanager "platform-tools"
sdkmanager "platforms;android-36"
sdkmanager "build-tools;36.0.0"

echo "✅ SDK 组件安装完成"
echo

# 4. 检查 Gradle Wrapper
echo "步骤 4: 检查 Gradle..."
cd /d/Code/rikkahub

if [ -f "./gradlew" ]; then
    echo "✅ Gradle Wrapper 存在"
    chmod +x ./gradlew
    echo
    echo "Gradle 版本:"
    ./gradlew --version
else
    echo "❌ Gradle Wrapper 不存在"
    exit 1
fi

echo

# 5. 验证项目
echo "步骤 5: 验证项目配置..."

if [ -f "app/build.gradle.kts" ]; then
    echo "✅ app/build.gradle.kts 存在"
else
    echo "❌ app/build.gradle.kts 不存在"
    exit 1
fi

if [ -f "settings.gradle.kts" ]; then
    echo "✅ settings.gradle.kts 存在"
else
    echo "❌ settings.gradle.kts 不存在"
    exit 1
fi

echo

# 6. 运行测试构建
echo "步骤 6: 运行测试构建..."
echo "⚠️  这可能需要几分钟时间..."

if ./gradlew build --no-daemon; then
    echo
    echo "✅ 构建成功!"
else
    echo
    echo "❌ 构建失败，请检查错误信息"
    exit 1
fi

echo

# 7. 运行测试
echo "步骤 7: 运行单元测试..."
if ./gradlew test --no-daemon; then
    echo
    echo "✅ 所有测试通过!"
else
    echo
    echo "⚠️  部分测试失败，请检查"
fi

echo
echo "=========================================="
echo "  Android 开发环境配置完成!"
echo "=========================================="
echo
echo "环境变量已添加到 ~/.bashrc"
echo "如需立即生效，请运行: source ~/.bashrc"
echo
echo "下一步:"
echo "  1. 启动 Android Studio (可选)"
echo "  2. 打开项目: /d/Code/rikkahub"
echo "  3. 同步项目并运行测试"
echo
echo "快速命令:"
echo "  构建项目: ./gradlew build"
echo "  运行测试: ./gradlew test"
echo "  安装APK: ./gradlew installDebug"
echo

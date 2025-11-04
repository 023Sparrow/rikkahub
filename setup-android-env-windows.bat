@echo off
echo ==========================================
echo   Android 开发环境配置脚本 (Windows)
echo ==========================================
echo.

REM 检查 Java
echo 步骤 1: 检查 Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java 未安装
    echo 请从 https://adoptium.net/ 下载并安装 OpenJDK 17
    pause
    exit /b 1
) else (
    echo ✅ Java 已安装
    java -version
)

echo.

REM 设置环境变量示例
echo 步骤 2: 环境变量配置示例
echo.
echo 请设置以下环境变量:
echo.
echo ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
echo PATH=%%ANDROID_HOME%%\cmdline-tools\latest\bin;%%PATH%%
echo.
echo 从 https://developer.android.com/studio#command-tools 下载 Android SDK
echo.

REM 检查项目结构
echo 步骤 3: 检查项目结构...
if exist app\build.gradle.kts (
    echo ✅ app/build.gradle.kts 存在
) else (
    echo ❌ app/build.gradle.kts 不存在
    pause
    exit /b 1
)

echo.
echo ==========================================
echo   环境配置完成
echo ==========================================
echo.
echo 请手动安装:
echo   1. OpenJDK 17
echo   2. Android SDK Command Line Tools
echo   3. 设置 ANDROID_HOME 环境变量
echo.
echo 然后在项目根目录运行:
echo   gradlew.bat build
echo   gradlew.bat test
echo.
pause

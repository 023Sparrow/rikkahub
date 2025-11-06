# 🔊 CLAUDE.md - TTS Module

> **面包屑**: `根目录` → `tts` → **TTS Module**

## 📋 Module Overview

**tts** 是 RikkaHub 的多提供商文本转语音模块，支持多种 TTS 提供商的统一抽象和实现。支持 OpenAI、Gemini、MiniMax 和系统 TTS，提供流式音频处理、音频播放控制和文本分块功能。采用现代 Android 媒体库和协程架构，实现高性能的语音合成和播放体验。

## 🏗️ Architecture

### 🗂️ Core Components

- **`TTSProvider<T>`**: TTS 提供商泛型接口
  - **类型安全**: 泛型参数 `T` 支持不同提供商设置类型
  - **流式处理**: `Flow<AudioChunk>` 支持实时音频流处理
  - **上下文感知**: 支持 Android Context 进行音频管理

- **`TTSManager`**: TTS 提供商管理器
  - **多提供商路由**: 根据设置类型自动路由到对应提供商
  - **统一 API**: 提供简化的语音生成接口
  - **生命周期管理**: 自动管理提供商实例和资源

- **控制器层**:
  - `AudioPlayer`: 基于 ExoPlayer 的强大音频播放控制
  - `TtsController`: TTS 播放控制器
  - `TtsSynthesizer`: TTS 音频合成器
  - `TextChunker`: 智能文本分块器

- **数据模型**:
  - `TTSRequest/TTSResponse`: 请求响应数据结构
  - `AudioChunk`: 音频块数据结构
  - `PlaybackState`: 播放状态管理

### 🔧 Key Technologies

- **ExoPlayer**: Android 官方音频/视频播放器库
- **Kotlinx Serialization**: JSON 序列化/反序列化
- **OkHttp**: HTTP 客户端进行网络 TTS 请求
- **Kotlin Coroutines**: 协程和 Flow 响应式编程
- **Android Media3**: 现代媒体播放框架

## 🚀 Key Features

### 多提供商 TTS 支持
- **OpenAI TTS**: `gpt-4o-mini-tts` 模型，支持多种语音风格
- **Google Gemini TTS**: `gemini-2.5-flash-preview-tts` 高质量语音
- **MiniMax TTS**: `speech-2.5-hd-preview` 高清语音合成
- **系统 TTS**: 设备内置 TextToSpeech 引擎
- **统一接口**: 所有提供商实现相同的 `TTSProvider<T>` 接口

### 流式音频处理
- **实时合成**: 支持流式语音合成，边生成边播放
- **音频分块**: 智能音频块管理和缓存
- **格式支持**: 支持 PCM、MP3、WAV、AAC、OGG、OPUS 等多种音频格式
- **自动转换**: PCM 音频自动转换为 WAV 格式播放

### 高级播放控制
- **状态管理**: 完整的播放状态跟踪（Buffering、Playing、Paused、Ended、Error）
- **播放控制**: 暂停、恢复、停止、跳转、倍速播放
- **位置追踪**: 实时播放位置和进度更新
- **错误处理**: 完整的错误捕获和恢复机制

## 🔗 Dependencies

**内部模块依赖**:
- `app`: UI 层集成，Context 提供
- `ai`: 无直接依赖

**外部依赖**:
- ExoPlayer: 音频播放库
- OkHttp: HTTP 网络库
- Kotlinx Serialization: JSON 序列化
- Kotlin Coroutines: 协程库
- Android Media3: 媒体播放框架

## 📁 Critical Files

- `TTSProvider.kt`: TTS 提供商泛型接口定义
- `TTSManager.kt`: 提供商管理器和路由逻辑
- `TTSProviderSetting.kt`: 提供商设置数据类（密封类）
- `AudioPlayer.kt`: 基于 ExoPlayer 的音频播放器
- `TtsController.kt`: TTS 播放控制器

## 🎨 Usage Patterns

### 基本 TTS 使用
```kotlin
// 创建 TTS 管理器
val ttsManager = TTSManager(context)

// 配置 OpenAI TTS 提供商
val openAISetting = TTSProviderSetting.OpenAI(
    apiKey = "sk-your-api-key",
    baseUrl = "https://api.openai.com/v1",
    model = "gpt-4o-mini-tts",
    voice = "alloy"
)

// 执行语音合成
val audioChunks = ttsManager.generateSpeech(
    providerSetting = openAISetting,
    request = TTSRequest(text = "Hello, this is a text-to-speech demonstration!")
)

// 播放音频流
audioChunks.collect { chunk ->
    // 处理音频块
    println("Received audio chunk: ${chunk.data.size} bytes")
}
```

### 完整播放示例
```kotlin
@Composable
fun TTSPlayerScreen() {
    val context = LocalContext.current
    val audioPlayer = remember { AudioPlayer(context) }
    val playbackState by audioPlayer.playbackState.collectAsState()

    var ttsRequest by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // 文本输入
        OutlinedTextField(
            value = ttsRequest,
            onValueChange = { ttsRequest = it },
            label = { Text("输入要转换的文本") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 播放控制按钮
        Row {
            Button(
                onClick = {
                    if (!isPlaying) {
                        isPlaying = true
                        lifecycleScope.launch {
                            val audioChunks = ttsManager.generateSpeech(
                                providerSetting = TTSProviderSetting.OpenAI(
                                    apiKey = "your-api-key",
                                    voice = "alloy"
                                ),
                                request = TTSRequest(text = ttsRequest)
                            )

                            val allAudioData = mutableListOf<ByteArray>()
                            audioChunks.collect { chunk ->
                                allAudioData.add(chunk.data)
                            }

                            // 合并所有音频块
                            val totalSize = allAudioData.sumOf { it.size }
                            val combinedAudio = ByteArray(totalSize)
                            var offset = 0
                            allAudioData.forEach { audioData ->
                                System.arraycopy(audioData, 0, combinedAudio, offset, audioData.size)
                                offset += audioData.size
                            }

                            // 创建 TTSResponse 并播放
                            val response = TTSResponse(
                                audioData = combinedAudio,
                                format = AudioFormat.MP3,
                                duration = null
                            )

                            runCatching {
                                audioPlayer.play(response)
                            }.onFailure {
                                println("播放失败: ${it.message}")
                            }.onSuccess {
                                println("播放成功")
                            }

                            isPlaying = false
                        }
                    }
                },
                enabled = ttsRequest.isNotBlank() && !isPlaying
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始播放")
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (playbackState.status == PlaybackStatus.Playing) {
                        audioPlayer.pause()
                    } else if (playbackState.status == PlaybackStatus.Paused) {
                        audioPlayer.resume()
                    }
                }
            ) {
                if (playbackState.status == PlaybackStatus.Playing) {
                    Icon(Icons.Default.Pause, contentDescription = "暂停")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "播放")
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    audioPlayer.stop()
                    audioPlayer.clear()
                }
            ) {
                Icon(Icons.Default.Stop, contentDescription = "停止")
            }
        }

        // 播放进度显示
        if (playbackState.durationMs > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Slider(
                    value = playbackState.positionMs.toFloat(),
                    onValueChange = { value ->
                        audioPlayer.seekBy((value - playbackState.positionMs).toLong())
                    },
                    valueRange = 0f..playbackState.durationMs.toFloat()
                )
                Text(
                    text = "${playbackState.positionMs / 1000}s / ${playbackState.durationMs / 1000}s"
                )
            }
        }

        // 播放状态显示
        Text(
            text = "状态: ${playbackState.status.displayName}",
            style = MaterialTheme.typography.bodySmall,
            color = if (playbackState.status == PlaybackStatus.Error) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
```

### 高级 TTS 配置
```kotlin
// Gemini TTS 高质量配置
val geminiConfig = TTSProviderSetting.Gemini(
    apiKey = "your-gemini-api-key",
    model = "gemini-2.5-flash-preview-tts",
    voiceName = "Kore", // 高质量语音
    baseUrl = "https://generativelanguage.googleapis.com/v1beta"
)

// MiniMax TTS 高清配置
val miniMaxConfig = TTSProviderSetting.MiniMax(
    apiKey = "your-minimax-api-key",
    model = "speech-2.5-hd-preview",
    voiceId = "female-shaonv",
    emotion = "calm",
    speed = 1.0f
)

// 系统 TTS 配置
val systemConfig = TTSProviderSetting.SystemTTS(
    speechRate = 1.2f, // 语速加快
    pitch = 1.1f // 音调稍微提高
)

// 根据语言自动选择最佳提供商
fun selectBestTTSProvider(language: String, quality: Quality): TTSProviderSetting {
    return when {
        language == "zh" && quality == Quality.HIGH -> {
            TTSProviderSetting.MiniMax(voiceId = "female-shaonv")
        }
        language == "en" && quality == Quality.HIGH -> {
            TTSProviderSetting.Gemini(voiceName = "Kore")
        }
        else -> {
            TTSProviderSetting.SystemTTS(speechRate = 1.0f)
        }
    }
}
```

## 🔄 Integration Patterns

### 与 app 模块集成
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object TTSModule {
    @Provides
    @Singleton
    fun provideTTSManager(@ApplicationContext context: Context): TTSManager {
        return TTSManager(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(@ApplicationContext context: Context): AudioPlayer {
        return AudioPlayer(context)
    }
}

// 在 ViewModel 中使用
@HiltViewModel
class TTSViewModel @Inject constructor(
    private val ttsManager: TTSManager,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _playbackState = MutableStateFlow<PlaybackState?>(null)
    val playbackState: StateFlow<PlaybackState?> = _playbackState.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun synthesizeAndPlay(text: String, providerSetting: TTSProviderSetting) {
        viewModelScope.launch {
            _isGenerating.value = true

            try {
                val audioChunks = ttsManager.generateSpeech(
                    providerSetting = providerSetting,
                    request = TTSRequest(text = text)
                )

                val allAudioData = mutableListOf<ByteArray>()
                audioChunks.collect { chunk ->
                    allAudioData.add(chunk.data)
                }

                val combinedAudio = combineAudioChunks(allAudioData)
                val response = TTSResponse(
                    audioData = combinedAudio,
                    format = AudioFormat.MP3
                )

                audioPlayer.play(response)
                _playbackState.value = audioPlayer.playbackState.value

            } catch (e: Exception) {
                // 错误处理
                e.printStackTrace()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun combineAudioChunks(chunks: List<ByteArray>): ByteArray {
        val totalSize = chunks.sumOf { it.size }
        val result = ByteArray(totalSize)
        var offset = 0
        chunks.forEach { chunk ->
            System.arraycopy(chunk, 0, result, offset, chunk.size)
            offset += chunk.size
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
```

### 与 AI 聊天集成
```kotlin
class ChatTTSController {
    fun addTTSToChat(chatMessage: ChatMessage, ttsManager: TTSManager) {
        if (chatMessage.contentType == ContentType.TEXT) {
            val text = chatMessage.content
            val ttsSetting = getPreferredTTSSetting(chatMessage.language)

            val audioFlow = ttsManager.generateSpeech(
                providerSetting = ttsSetting,
                request = TTSRequest(text = text)
            )

            // 播放音频
            lifecycleScope.launch {
                val chunks = mutableListOf<AudioChunk>()
                audioFlow.collect { chunk ->
                    chunks.add(chunk)
                    // 实时播放或缓存
                    playAudioChunk(chunk)
                }
            }
        }
    }

    private fun getPreferredTTSSetting(language: String): TTSProviderSetting {
        return when (language) {
            "zh" -> TTSProviderSetting.MiniMax(voiceId = "female-shaonv")
            "en" -> TTSProviderSetting.OpenAI(voice = "alloy")
            else -> TTSProviderSetting.SystemTTS()
        }
    }
}
```

## 🧪 Testing

- **单元测试**: 在 `src/test/java/me/rerere/tts/` 目录
- **模拟测试**: 提供测试用的 Mock TTSProvider 实现
- **音频测试**: 验证音频格式转换和播放功能
- **播放状态测试**: 测试播放状态转换和错误处理
- **流式处理测试**: 测试 Flow<AudioChunk> 的流式处理

## 🔐 Security & Performance

### 安全特性
- **API 密钥安全**: 支持安全的环境变量配置
- **音频数据验证**: 音频块数据格式验证
- **网络超时**: HTTP 请求超时保护
- **内存管理**: 自动释放音频资源

### 性能优化
- **流式处理**: 边生成边播放，减少延迟
- **音频分块**: 智能分块减少内存占用
- **连接复用**: OkHttp 连接池优化
- **ExoPlayer 优化**: 利用原生媒体播放器的性能优化

### 监控指标
- **合成性能**: TTS 语音合成时间统计
- **播放质量**: 音频播放流畅度监控
- **提供商切换**: 自动故障转移和负载均衡
- **内存使用**: 音频缓冲区内存使用监控

## 🎯 扩展指南

### 添加新 TTS 提供商
```kotlin
// 1. 创建设置数据类
data class CustomTTS(
    override var id: Uuid = Uuid.random(),
    override var name: String = "Custom TTS",
    val apiKey: String = "",
    val voiceId: String = "default",
    val quality: String = "high"
) : TTSProviderSetting() {
    override fun copyProvider(id: Uuid, name: String): TTSProviderSetting {
        return this.copy(id = id, name = name)
    }
}

// 2. 实现 TTSProvider 接口
class CustomTTSProvider : TTSProvider<CustomTTS> {
    private val httpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun generateSpeech(
        context: Context,
        providerSetting: CustomTTS,
        request: TTSRequest
    ): Flow<AudioChunk> = flow {
        // 实现 TTS API 调用
        val apiRequest = buildCustomTTSRequest(request, providerSetting)
        val response = httpClient.newCall(apiRequest).execute()

        if (response.isSuccessful) {
            val audioData = response.body.bytes()
            emit(AudioChunk(data = audioData, format = AudioFormat.MP3))
        } else {
            throw Exception("TTS request failed: ${response.code}")
        }
    }

    private fun buildCustomTTSRequest(
        request: TTSRequest,
        setting: CustomTTS
    ): Request {
        // 构建具体的 HTTP 请求
    }
}

// 3. 在 TTSManager 中注册
class TTSManager {
    private val customProvider = CustomTTSProvider()

    fun generateSpeech(
        providerSetting: TTSProviderSetting,
        request: TTSRequest
    ): Flow<AudioChunk> {
        return when (providerSetting) {
            // ... 现有提供商
            is CustomTTS -> customProvider.generateSpeech(context, providerSetting, request)
        }
    }
}
```

### 自定义音频处理
```kotlin
class CustomAudioProcessor {
    fun enhanceAudio(audioData: ByteArray, enhancement: AudioEnhancement): ByteArray {
        return when (enhancement.type) {
            EnhancementType.VOLUME_BOOST -> boostVolume(audioData, enhancement.factor)
            EnhancementType.NOISE_REDUCTION -> reduceNoise(audioData)
            EnhancementType.PITCH_SHIFT -> shiftPitch(audioData, enhancement.semitones)
        }
    }

    fun mergeAudioChunks(chunks: List<AudioChunk>): AudioChunk {
        val totalSize = chunks.sumOf { it.data.size }
        val mergedData = ByteArray(totalSize)
        var offset = 0

        chunks.forEach { chunk ->
            System.arraycopy(chunk.data, 0, mergedData, offset, chunk.data.size)
            offset += chunk.data.size
        }

        return AudioChunk(
            data = mergedData,
            format = chunks.first().format
        )
    }
}

data class AudioEnhancement(
    val type: EnhancementType,
    val factor: Float = 1.0f,
    val semitones: Float = 0.0f
)

enum class EnhancementType {
    VOLUME_BOOST,
    NOISE_REDUCTION,
    PITCH_SHIFT
}
```

---

**📖 相关文档**:
- [根目录 CLAUDE.md](../CLAUDE.md)
- [app 模块 CLAUDE.md](../app/CLAUDE.md)
- [ExoPlayer 文档](https://developer.android.com/guide/topics/media-apps/exoplayer)
- [OpenAI TTS 文档](https://platform.openai.com/docs/guides/text-to-speech)
- [Google Gemini TTS 文档](https://ai.google.dev/gemini-api/docs/text-to-speech)
- [MiniMax TTS 文档](https://api.minimaxi.com/document/t2a_v2)
package me.rerere.rikkahub.data.model

import kotlinx.serialization.Serializable
import me.rerere.ai.provider.CustomBody
import me.rerere.ai.provider.CustomHeader
import me.rerere.ai.ui.UIMessage
import me.rerere.rikkahub.data.ai.tools.LocalToolOption
import kotlin.uuid.Uuid

@Serializable
data class Assistant(
    val id: Uuid = Uuid.random(),
    val chatModelId: Uuid? = null, // 如果为null, 使用全局默认模型
    val name: String = "",
    val avatar: Avatar = Avatar.Dummy,
    val useAssistantAvatar: Boolean = false, // 使用助手头像替代模型头像
    val tags: List<Uuid> = emptyList(),
    val systemPrompt: String = "",
    val temperature: Float? = null,
    val topP: Float? = null,
    val contextMessageSize: Int = 64,
    val streamOutput: Boolean = true,
    val enableMemory: Boolean = false,
    val enableRecentChatsReference: Boolean = false,
    val messageTemplate: String = "{{ message }}",
    val presetMessages: List<UIMessage> = emptyList(),
    val quickMessages: List<QuickMessage> = emptyList(),
    val regexes: List<AssistantRegex> = emptyList(),
    val thinkingBudget: Int? = 1024,
    val maxTokens: Int? = null,
    val customHeaders: List<CustomHeader> = emptyList(),
    val customBodies: List<CustomBody> = emptyList(),
    val mcpServers: Set<Uuid> = emptySet(),
    val localTools: List<LocalToolOption> = emptyList(),
    val background: String? = null,
    val learningMode: Boolean = false,
    
    // 世界书配置
    val enableWorldBook: Boolean = false,                      // 是否启用世界书
    val worldBookContextSize: Int = 2000,                      // 世界书上下文token限制
    val worldBookMaxHistoryMessages: Int = 5,                  // 匹配时检查的历史消息数量
    val worldBookMaxRecursionDepth: Int = 3,                   // 最大递归深度 (用户可配置)
    val worldBookFormatStyle: WorldBookFormatStyle = WorldBookFormatStyle.STRUCTURED, // 格式化风格
    
    // 记忆表格配置
    val enableMemoryTable: Boolean = false,                    // 是否启用记忆表格作为Tool

    // 记忆增强配置
    val enableMemoryEnhancement: Boolean = false,               // 是否启用记忆增强系统
    val memoryEnhancementMaxTokens: Int = 1500,                 // 记忆增强token限制
    val memoryEnhancementFormatStyle: MemoryEnhancementFormatStyle = MemoryEnhancementFormatStyle.STRUCTURED, // 格式化风格
    val memoryEnhancementInjectPosition: Int = 0,               // 注入位置: 0=开头, 1=结尾
    val memoryEnhancementEnableDeduplication: Boolean = true,   // 启用去重
    val memoryEnhancementMaxRows: Int = 50,                     // 最大显示行数
    val memoryEnhancementTruncateContent: Boolean = true,       // 截断长内容
)

/**
 * 世界书格式化风格
 */
@Serializable
enum class WorldBookFormatStyle {
    STRUCTURED,  // 结构化格式
    MINIMAL,     // 简洁格式
    MARKDOWN     // Markdown格式
}

/**
 * 记忆增强格式化风格
 */
@Serializable
enum class MemoryEnhancementFormatStyle {
    STRUCTURED,      // 结构化格式
    MINIMAL,         // 简洁格式
    MARKDOWN,        // Markdown格式
    CONVERSATIONAL   // 对话格式
}

@Serializable
data class QuickMessage(
    val title: String = "",
    val content: String = "",
)

@Serializable
data class AssistantMemory(
    val id: Int,
    val content: String = "",
)

@Serializable
enum class AssistantAffectScope {
    USER,
    ASSISTANT,
}

@Serializable
data class AssistantRegex(
    val id: Uuid,
    val name: String = "",
    val enabled: Boolean = true,
    val findRegex: String = "", // 正则表达式
    val replaceString: String = "", // 替换字符串
    val affectingScope: Set<AssistantAffectScope> = setOf(),
    val visualOnly: Boolean = false, // 是否仅在视觉上影响
)

fun String.replaceRegexes(
    assistant: Assistant?,
    scope: AssistantAffectScope,
    visual: Boolean = false
): String {
    if (assistant == null) return this
    if (assistant.regexes.isEmpty()) return this
    return assistant.regexes.fold(this) { acc, regex ->
        if (regex.enabled && regex.visualOnly == visual && regex.affectingScope.contains(scope)) {
            try {
                val result = acc.replace(
                    regex = Regex(regex.findRegex),
                    replacement = regex.replaceString,
                )
                // println("Regex: ${regex.findRegex} -> ${result}")
                result
            } catch (e: Exception) {
                e.printStackTrace()
                // 如果正则表达式格式错误，返回原字符串
                acc
            }
        } else {
            acc
        }
    }
}

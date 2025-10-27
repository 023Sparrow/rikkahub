package me.rerere.rikkahub

import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.service.WorldBookInjector
import me.rerere.rikkahub.service.WorldBookMatcher
import org.junit.Test
import org.junit.Assert.*

/**
 * WorldBookInjector单元测试
 * 测试上下文注入、格式化、位置插入等功能
 */
class WorldBookInjectorTest {

    private val injector = WorldBookInjector()
    
    @Test
    fun `test basic injection`() {
        // Arrange
        val messages = listOf(
            UIMessage(MessageRole.USER, listOf(UIMessagePart.Text("Hello")))
        )
        
        val matchedEntries = listOf(
            WorldBookMatcher.MatchedEntry(
                entry = WorldBookEntry(
                    id = "1",
                    assistantId = "test",
                    title = "Test Entry",
                    keywords = listOf("test"),
                    content = "This is test content.",
                    isEnabled = true,
                    priority = 0
                ),
                matchDepth = 0,
                matchedKeywords = listOf("test")
            )
        )
        
        val assistant = Assistant(id = "test", name = "Test", providerId = "test", modelId = "test")
        
        // Act
        val result = injector.injectWorldBook(messages, matchedEntries, assistant)
        
        // Assert
        assertTrue(result.size > messages.size)
        val injectedMessage = result.find { it.role == MessageRole.SYSTEM }
        assertNotNull(injectedMessage)
        val textContent = injectedMessage?.parts?.firstOrNull()
        assertTrue(textContent is UIMessagePart.Text)
        assertTrue((textContent as UIMessagePart.Text).text.contains("Test Entry"))
    }
    
    @Test
    fun `test empty entries returns original messages`() {
        val messages = listOf(
            UIMessage(MessageRole.USER, listOf(UIMessagePart.Text("Hello")))
        )
        val assistant = Assistant(id = "test", name = "Test", providerId = "test", modelId = "test")
        
        val result = injector.injectWorldBook(messages, emptyList(), assistant)
        
        assertEquals(messages, result)
    }
    
    @Test
    fun `test deduplication removes duplicate entries`() {
        val messages = listOf(
            UIMessage(MessageRole.USER, listOf(UIMessagePart.Text("Hello")))
        )
        
        val entry = WorldBookEntry(
            id = "1",
            assistantId = "test",
            title = "Test Entry",
            keywords = listOf("test"),
            content = "Content",
            isEnabled = true,
            priority = 0
        )
        
        // Same entry repeated
        val matchedEntries = listOf(
            WorldBookMatcher.MatchedEntry(entry, 0, listOf("test")),
            WorldBookMatcher.MatchedEntry(entry, 1, listOf("test")),
            WorldBookMatcher.MatchedEntry(entry, 2, listOf("test"))
        )
        
        val assistant = Assistant(id = "test", name = "Test", providerId = "test", modelId = "test")
        val config = WorldBookInjector.InjectionConfig(enableDeduplication = true)
        
        val result = injector.injectWorldBook(messages, matchedEntries, assistant, config)
        
        // Should only inject once despite multiple matches
        val systemMessages = result.filter { it.role == MessageRole.SYSTEM }
        assertEquals(1, systemMessages.size)
    }
    
    @Test
    fun `test message order is preserved`() {
        val messages = listOf(
            UIMessage(MessageRole.SYSTEM, listOf(UIMessagePart.Text("System prompt"))),
            UIMessage(MessageRole.USER, listOf(UIMessagePart.Text("User message 1"))),
            UIMessage(MessageRole.ASSISTANT, listOf(UIMessagePart.Text("Assistant reply"))),
            UIMessage(MessageRole.USER, listOf(UIMessagePart.Text("User message 2")))
        )
        
        val matchedEntries = listOf(
            WorldBookMatcher.MatchedEntry(
                entry = WorldBookEntry(
                    id = "1",
                    assistantId = "test",
                    title = "Test",
                    keywords = listOf("test"),
                    content = "Content",
                    isEnabled = true,
                    priority = 0
                ),
                matchDepth = 0,
                matchedKeywords = listOf("test")
            )
        )
        
        val assistant = Assistant(id = "test", name = "Test", providerId = "test", modelId = "test")
        
        val result = injector.injectWorldBook(messages, matchedEntries, assistant)
        
        // Original messages should maintain their relative order
        val userMessages = result.filter { it.role == MessageRole.USER }
        assertEquals(2, userMessages.size)
        assertTrue((userMessages[0].parts[0] as UIMessagePart.Text).text.contains("User message 1"))
        assertTrue((userMessages[1].parts[0] as UIMessagePart.Text).text.contains("User message 2"))
    }
    
    @Test
    fun `test different format styles`() {
        val messages = listOf(UIMessage(MessageRole.USER, listOf(UIMessagePart.Text("Test"))))
        val entry = WorldBookEntry(
            id = "1",
            assistantId = "test",
            title = "Test Entry",
            keywords = listOf("test"),
            content = "Test content.",
            isEnabled = true,
            priority = 10
        )
        val matchedEntries = listOf(WorldBookMatcher.MatchedEntry(entry, 0, listOf("test")))
        val assistant = Assistant(id = "test", name = "Test", providerId = "test", modelId = "test")
        
        // Test STRUCTURED format
        val structuredConfig = WorldBookInjector.InjectionConfig(formatStyle = WorldBookInjector.FormatStyle.STRUCTURED)
        val structuredResult = injector.injectWorldBook(messages, matchedEntries, assistant, structuredConfig)
        val structuredText = (structuredResult.first { it.role == MessageRole.SYSTEM }.parts[0] as UIMessagePart.Text).text
        assertTrue(structuredText.contains("=== World Information ==="))
        assertTrue(structuredText.contains("[Priority: 10]"))
        
        // Test MINIMAL format
        val minimalConfig = WorldBookInjector.InjectionConfig(formatStyle = WorldBookInjector.FormatStyle.MINIMAL)
        val minimalResult = injector.injectWorldBook(messages, matchedEntries, assistant, minimalConfig)
        val minimalText = (minimalResult.first { it.role == MessageRole.SYSTEM }.parts[0] as UIMessagePart.Text).text
        assertTrue(minimalText.contains("Relevant context:"))
        assertFalse(minimalText.contains("[Priority:"))
        
        // Test MARKDOWN format
        val markdownConfig = WorldBookInjector.InjectionConfig(formatStyle = WorldBookInjector.FormatStyle.MARKDOWN)
        val markdownResult = injector.injectWorldBook(messages, matchedEntries, assistant, markdownConfig)
        val markdownText = (markdownResult.first { it.role == MessageRole.SYSTEM }.parts[0] as UIMessagePart.Text).text
        assertTrue(markdownText.contains("## World Information"))
        assertTrue(markdownText.contains("### Test Entry"))
    }
}
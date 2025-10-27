package me.rerere.rikkahub

import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.db.entity.WorldBookEntry
import me.rerere.rikkahub.service.WorldBookMatcher
import org.junit.Test
import org.junit.Assert.*

/**
 * WorldBookMatcher单元测试
 * 测试关键词匹配、正则表达式、优先级排序等核心功能
 */
class WorldBookMatcherTest {

    private val matcher = WorldBookMatcher()

    @Test
    fun `test basic keyword matching`() {
        val entry1 = WorldBookEntry(
            id = "1",
            assistantId = "test-assistant",
            title = "Dragon Entry",
            keywords = listOf("dragon", "fire"),
            content = "Content about dragons",
            isEnabled = true,
            priority = 0
        )
        
        val entry2 = WorldBookEntry(
            id = "2",
            assistantId = "test-assistant",
            title = "Magic Entry",
            keywords = listOf("magic", "spell"),
            content = "Content about magic",
            isEnabled = true,
            priority = 0
        )

        // Act
        val result = matcher.matchEntries(
            input = "The dragon breathed fire across the battlefield.",
            conversationHistory = emptyList(),
            entries = listOf(entry1, entry2)
        )

        // Assert
        assertEquals(1, result.size)
        assertEquals("1", result[0].entry.id)
        assertTrue(result[0].matchedKeywords.containsAll(listOf("dragon", "fire")))
    }

    @Test
    fun `test priority sorting`() {
        val lowPriority = WorldBookEntry(
            id = "1",
            assistantId = "test-assistant",
            title = "Low Priority",
            keywords = listOf("test"),
            content = "Content 1",
            isEnabled = true,
            priority = 10
        )
        
        val highPriority = WorldBookEntry(
            id = "2",
            assistantId = "test-assistant",
            title = "High Priority",
            keywords = listOf("test"),
            content = "Content 2",
            isEnabled = true,
            priority = 100
        )
        
        val mediumPriority = WorldBookEntry(
            id = "3",
            assistantId = "test-assistant",
            title = "Medium Priority",
            keywords = listOf("test"),
            content = "Content 3",
            isEnabled = true,
            priority = 50
        )

        // Act
        val result = matcher.matchEntries(
            input = "This is a test message.",
            conversationHistory = emptyList(),
            entries = listOf(lowPriority, highPriority, mediumPriority)
        )

        // Assert
        assertEquals(3, result.size)
        assertEquals("2", result[0].entry.id) // High Priority
        assertEquals("3", result[1].entry.id) // Medium Priority
        assertEquals("1", result[2].entry.id) // Low Priority
    }

    @Test
    fun `test disabled entries are excluded`() {
        val enabledEntry = WorldBookEntry(
            id = "1",
            assistantId = "test-assistant",
            title = "Enabled Entry",
            keywords = listOf("test"),
            content = "Content 1",
            isEnabled = true,
            priority = 0
        )
        
        val disabledEntry = WorldBookEntry(
            id = "2",
            assistantId = "test-assistant",
            title = "Disabled Entry",
            keywords = listOf("test"),
            content = "Content 2",
            isEnabled = false,
            priority = 0
        )

        // Act
        val result = matcher.matchEntries(
            input = "This is a test.",
            conversationHistory = emptyList(),
            entries = listOf(enabledEntry, disabledEntry)
        )

        // Assert
        assertEquals(1, result.size)
        assertEquals("1", result[0].entry.id)
    }
}
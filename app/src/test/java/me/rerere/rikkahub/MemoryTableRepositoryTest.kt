package me.rerere.rikkahub

import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.data.db.entity.MemoryTableRow
import org.junit.Test
import org.junit.Assert.*

/**
 * MemoryTableRepository单元测试
 * 测试记忆表格的数据结构和操作逻辑
 * 
 * 注意：这是一个结构性测试，验证数据模型的正确性
 * 实际的Repository测试需要配置Room内存数据库
 */
class MemoryTableRepositoryTest {

    @Test
    fun `test basic table structure`() {
        val table = MemoryTable(
            id = "table-1",
            assistantId = "assistant-1",
            name = "Character Info",
            description = "Basic character information",
            columnHeaders = listOf("Name", "Age", "Occupation")
        )

        val row = MemoryTableRow(
            id = "row-1",
            tableId = "table-1",
            rowData = mapOf(
                "Name" to "Alice",
                "Age" to "25",
                "Occupation" to "Engineer"
            )
        )

        // 验证数据模型
        assertEquals("Character Info", table.name)
        assertEquals(3, table.columnHeaders.size)
        assertTrue(table.columnHeaders.contains("Name"))
        
        assertEquals("table-1", row.tableId)
        assertEquals("Alice", row.rowData["Name"])
        assertEquals(3, row.rowData.size)
    }

    @Test
    fun `test table column validation`() {
        val columns = listOf("Col1", "Col2", "Col3")
        
        // 验证列定义
        assertTrue(columns.isNotEmpty())
        assertEquals(3, columns.size)
        assertTrue(columns.all { it.isNotBlank() })
    }

    @Test
    fun `test row values match columns`() {
        val table = MemoryTable(
            id = "table-1",
            assistantId = "assistant-1",
            name = "Test Table",
            description = "Test",
            columnHeaders = listOf("A", "B", "C")
        )

        val validRow = MemoryTableRow(
            id = "row-1",
            tableId = "table-1",
            rowData = mapOf(
                "A" to "value1",
                "B" to "value2",
                "C" to "value3"
            )
        )

        // 验证行数据的列与表定义匹配
        table.columnHeaders.forEach { column ->
            assertTrue(validRow.rowData.containsKey(column))
        }
    }

    @Test
    fun `test empty table columns`() {
        val table = MemoryTable(
            id = "table-1",
            assistantId = "assistant-1",
            name = "Empty Table",
            description = "No columns",
            columnHeaders = emptyList()
        )

        // 空列表应该有效
        assertTrue(table.columnHeaders.isEmpty())
    }

    @Test
    fun `test row with partial values`() {
        val table = MemoryTable(
            id = "table-1",
            assistantId = "assistant-1",
            name = "Test Table",
            description = "Test",
            columnHeaders = listOf("A", "B", "C")
        )

        val partialRow = MemoryTableRow(
            id = "row-1",
            tableId = "table-1",
            rowData = mapOf(
                "A" to "value1",
                "B" to "" // 空值
                // 缺少 "C"
            )
        )

        // 验证部分填充的行
        assertTrue(partialRow.rowData.containsKey("A"))
        assertTrue(partialRow.rowData.containsKey("B"))
        assertFalse(partialRow.rowData.containsKey("C"))
    }

    @Test
    fun `test multiple rows in same table`() {
        val tableId = "table-1"
        
        val rows = listOf(
            MemoryTableRow(
                id = "row-1",
                tableId = tableId,
                rowData = mapOf("Name" to "Alice", "Age" to "25")
            ),
            MemoryTableRow(
                id = "row-2",
                tableId = tableId,
                rowData = mapOf("Name" to "Bob", "Age" to "30")
            ),
            MemoryTableRow(
                id = "row-3",
                tableId = tableId,
                rowData = mapOf("Name" to "Carol", "Age" to "28")
            )
        )

        // 验证多行数据
        assertEquals(3, rows.size)
        assertTrue(rows.all { it.tableId == tableId })
        assertEquals(listOf("row-1", "row-2", "row-3"), rows.map { it.id })
    }

    @Test
    fun `test table update operations`() {
        val original = MemoryTable(
            id = "table-1",
            assistantId = "assistant-1",
            name = "Original Name",
            description = "Original Description",
            columnHeaders = listOf("A", "B")
        )

        val updated = original.copy(
            name = "Updated Name",
            description = "Updated Description",
            columnHeaders = listOf("A", "B", "C")
        )

        // 验证更新操作
        assertEquals(original.id, updated.id)
        assertNotEquals(original.name, updated.name)
        assertNotEquals(original.description, updated.description)
        assertNotEquals(original.columnHeaders.size, updated.columnHeaders.size)
        assertEquals("Updated Name", updated.name)
        assertEquals(3, updated.columnHeaders.size)
    }

    @Test
    fun `test row update operations`() {
        val original = MemoryTableRow(
            id = "row-1",
            tableId = "table-1",
            rowData = mapOf("Name" to "Alice", "Age" to "25")
        )

        val updated = original.copy(
            rowData = mapOf("Name" to "Alice Smith", "Age" to "26", "City" to "NYC")
        )

        // 验证行更新
        assertEquals(original.id, updated.id)
        assertEquals(original.tableId, updated.tableId)
        assertNotEquals(original.rowData, updated.rowData)
        assertEquals("Alice Smith", updated.rowData["Name"])
        assertEquals(3, updated.rowData.size)
    }

    @Test
    fun `test column name constraints`() {
        // 验证列名约束
        val validColumns = listOf("Name", "Age", "City")
        val invalidColumns = listOf("", "   ", "\n")

        // 有效列名
        assertTrue(validColumns.all { it.isNotBlank() })
        assertTrue(validColumns.none { it.trim() != it })

        // 无效列名（应该在UI层或Repository层被拒绝）
        assertTrue(invalidColumns.any { it.isBlank() })
    }

    @Test
    fun `test search by column value`() {
        val rows = listOf(
            MemoryTableRow("row-1", "table-1", mapOf("Name" to "Alice", "City" to "NYC")),
            MemoryTableRow("row-2", "table-1", mapOf("Name" to "Bob", "City" to "LA")),
            MemoryTableRow("row-3", "table-1", mapOf("Name" to "Charlie", "City" to "NYC"))
        )

        // 模拟搜索功能
        val searchColumn = "City"
        val searchValue = "NYC"
        val matches = rows.filter { 
            it.rowData[searchColumn]?.contains(searchValue, ignoreCase = true) == true
        }

        assertEquals(2, matches.size)
        assertTrue(matches.all { it.rowData[searchColumn] == "NYC" })
    }

    @Test
    fun `test column reordering`() {
        val original = MemoryTable(
            id = "table-1",
            assistantId = "assistant-1",
            name = "Test",
            description = "Test",
            columnHeaders = listOf("A", "B", "C")
        )

        val reordered = original.copy(
            columnHeaders = listOf("C", "A", "B")
        )

        // 验证重新排序
        assertEquals(original.columnHeaders.size, reordered.columnHeaders.size)
        assertEquals(original.columnHeaders.toSet(), reordered.columnHeaders.toSet())
        assertNotEquals(original.columnHeaders, reordered.columnHeaders)
        assertEquals("C", reordered.columnHeaders[0])
    }

    @Test
    fun `test adding new column to existing table`() {
        val original = MemoryTable(
            id = "table-1",
            assistantId = "assistant-1",
            name = "Test",
            description = "Test",
            columnHeaders = listOf("A", "B")
        )

        val withNewColumn = original.copy(
            columnHeaders = original.columnHeaders + "C"
        )

        assertEquals(2, original.columnHeaders.size)
        assertEquals(3, withNewColumn.columnHeaders.size)
        assertTrue(withNewColumn.columnHeaders.contains("C"))
        assertTrue(withNewColumn.columnHeaders.containsAll(original.columnHeaders))
    }

    @Test
    fun `test removing column from table`() {
        val original = MemoryTable(
            id = "table-1",
            assistantId = "assistant-1",
            name = "Test",
            description = "Test",
            columnHeaders = listOf("A", "B", "C")
        )

        val withRemovedColumn = original.copy(
            columnHeaders = original.columnHeaders.filter { it != "B" }
        )

        assertEquals(3, original.columnHeaders.size)
        assertEquals(2, withRemovedColumn.columnHeaders.size)
        assertFalse(withRemovedColumn.columnHeaders.contains("B"))
        assertTrue(withRemovedColumn.columnHeaders.containsAll(listOf("A", "C")))
    }
}
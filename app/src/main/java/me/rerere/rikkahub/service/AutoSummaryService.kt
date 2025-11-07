package me.rerere.rikkahub.service

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import me.rerere.rikkahub.data.db.entity.MemoryTable
import me.rerere.rikkahub.data.db.entity.MemoryTableRow
import me.rerere.rikkahub.data.db.entity.MemoryColumn
import me.rerere.rikkahub.data.db.entity.ColumnType
import me.rerere.rikkahub.data.repository.MemoryTableRepository
import android.util.Log
import java.util.UUID

/**
 * 自动总结服务
 * 负责监控聊天消息并自动生成总结到记忆表格
 */
class AutoSummaryService(
    private val appScope: CoroutineScope,
    private val memoryTableRepository: MemoryTableRepository
) {
    companion object {
        private const val TAG = "AutoSummaryService"
    }

    // 每个助手的用户消息计数器
    private val userMessageCounters = mutableMapOf<String, Int>()

    // 每个助手的总结任务
    private val summaryJobs = mutableMapOf<String, Job>()

    // 自动总结间隔配置（默认每10条用户消息总结一次）
    private val summaryIntervals = mutableMapOf<String, Int>()

    // 事件流
    private val _events = MutableSharedFlow<AutoSummaryEvent>()
    val events = _events.asSharedFlow()

    /**
     * 设置自动总结间隔
     */
    fun setSummaryInterval(assistantId: String, interval: Int) {
        summaryIntervals[assistantId] = interval.coerceIn(1, 20)
        Log.d(TAG, "设置助手 $assistantId 的自动总结间隔为 ${summaryIntervals[assistantId]}")
    }

    /**
     * 获取自动总结间隔
     */
    fun getSummaryInterval(assistantId: String): Int {
        return summaryIntervals[assistantId] ?: 10
    }

    /**
     * 处理新消息（简化版本）
     */
    fun processMessage(assistantId: String, isUserMessage: Boolean) {
        // 只处理用户消息
        if (!isUserMessage) {
            return
        }

        // 增加用户消息计数
        val currentCount = userMessageCounters.getOrPut(assistantId) { 0 } + 1
        userMessageCounters[assistantId] = currentCount

        val interval = getSummaryInterval(assistantId)
        Log.d(TAG, "助手 $assistantId 的用户消息计数: $currentCount, 总结间隔: $interval")

        // 检查是否需要触发总结
        if (currentCount >= interval) {
            triggerAutoSummary(assistantId)
            userMessageCounters[assistantId] = 0
        }
    }

    /**
     * 触发自动总结
     */
    private fun triggerAutoSummary(assistantId: String) {
        // 取消之前的任务
        summaryJobs[assistantId]?.cancel()

        // 启动新的总结任务
        summaryJobs[assistantId] = appScope.launch {
            try {
                Log.d(TAG, "开始为助手 $assistantId 执行自动总结")
                _events.emit(AutoSummaryEvent.SummaryStarted(assistantId))

                // 生成占位总结
                val summary = "聊天总结 - ${System.currentTimeMillis()}"

                // 保存到记忆表格
                saveToMemoryTable(assistantId, summary)

                Log.d(TAG, "助手 $assistantId 的自动总结完成")
                _events.emit(AutoSummaryEvent.SummaryCompleted(assistantId, summary))

            } catch (e: Exception) {
                Log.e(TAG, "助手 $assistantId 的自动总结失败", e)
                _events.emit(AutoSummaryEvent.SummaryFailed(assistantId, e.message ?: "未知错误"))
            }
        }
    }

    /**
     * 保存到记忆表格
     */
    private suspend fun saveToMemoryTable(assistantId: String, summary: String) {
        try {
            // 获取或创建默认的记忆表格
            val table = getOrCreateDefaultTable(assistantId)

            // 创建新的行数据
            val rowData = mutableMapOf<String, String>()
            table.columns.forEach { column ->
                when (column.name) {
                    "时间" -> getCurrentTimeString()
                    "事件" -> summary
                    "类型" -> "聊天总结"
                    else -> column.defaultValue ?: ""
                }
            }

            val row = MemoryTableRow(
                tableId = table.id,
                rowData = rowData
            )

            memoryTableRepository.addRow(
                tableId = row.tableId,
                rowData = row.rowData
            )
            Log.d(TAG, "总结已保存到记忆表格: ${table.name}")

        } catch (e: Exception) {
            Log.e(TAG, "保存总结到记忆表格失败", e)
            throw e
        }
    }

    /**
     * 获取或创建默认记忆表格
     */
    private suspend fun getOrCreateDefaultTable(assistantId: String): MemoryTable {
        val existingTables = memoryTableRepository.getMemoryTablesByAssistant(assistantId).first()

        // 查找默认的聊天记录表格
        val defaultTable = existingTables.find { it.name == "聊天记录" }

        return defaultTable ?: run {
            // 创建新的默认表格
            val newTable = MemoryTable(
                assistantId = assistantId,
                name = "聊天记录",
                description = "自动生成的聊天记录总结表格",
                columns = listOf(
                    MemoryColumn(
                        id = UUID.randomUUID().toString(),
                        sheetId = "", // 临时值，将在创建表格后设置
                        name = "时间",
                        columnType = ColumnType.TEXT,
                        description = "记录时间"
                    ),
                    MemoryColumn(
                        id = UUID.randomUUID().toString(),
                        sheetId = "", // 临时值，将在创建表格后设置
                        name = "事件",
                        columnType = ColumnType.TEXT,
                        description = "事件描述",
                        isRequired = true
                    ),
                    MemoryColumn(
                        id = UUID.randomUUID().toString(),
                        sheetId = "", // 临时值，将在创建表格后设置
                        name = "类型",
                        columnType = ColumnType.TEXT,
                        description = "事件类型",
                        defaultValue = "聊天总结"
                    )
                )
            )

            val createdTable = memoryTableRepository.addTableWithColumns(
                assistantId = newTable.assistantId,
                name = newTable.name,
                description = newTable.description,
                columns = newTable.columns
            )
            Log.d(TAG, "创建默认记忆表格: ${createdTable.name}")
            createdTable
        }
    }

    /**
     * 获取当前时间字符串
     */
    private fun getCurrentTimeString(): String {
        val now = System.currentTimeMillis()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(now))
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        summaryJobs.values.forEach { it.cancel() }
        summaryJobs.clear()
        userMessageCounters.clear()
    }
}

/**
 * 自动总结事件
 */
sealed class AutoSummaryEvent {
    data class SummaryStarted(val assistantId: String) : AutoSummaryEvent()
    data class SummaryCompleted(val assistantId: String, val summary: String) : AutoSummaryEvent()
    data class SummaryFailed(val assistantId: String, val error: String) : AutoSummaryEvent()
}
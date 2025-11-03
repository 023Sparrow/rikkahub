package me.rerere.rikkahub.data.ai.tools

import android.content.Context
import com.whl.quickjs.wrapper.QuickJSContext
import com.whl.quickjs.wrapper.QuickJSObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import me.rerere.ai.core.InputSchema
import me.rerere.ai.core.Tool
import me.rerere.rikkahub.service.MemoryTableQueryService

@Serializable
sealed class LocalToolOption {
    @Serializable
    @SerialName("javascript_engine")
    data object JavascriptEngine : LocalToolOption()

    @Serializable
    @SerialName("memory_table_query")
    data object MemoryTableQuery : LocalToolOption()
}

class LocalTools(
    private val context: Context,
    private val memoryTableQueryService: MemoryTableQueryService? = null
) {
    val javascriptTool by lazy {
        Tool(
            name = "eval_javascript",
            description = "Execute JavaScript code with QuickJS. If use this tool to calculate math, better to add `toFixed` to the code.",
            parameters = {
                InputSchema.Obj(
                    properties = buildJsonObject {
                        put("code", buildJsonObject {
                            put("type", "string")
                            put("description", "The JavaScript code to execute")
                        })
                    },
                )
            },
            execute = {
                val context = QuickJSContext.create()
                val code = it.jsonObject["code"]?.jsonPrimitive?.contentOrNull
                val result = context.evaluate(code)
                buildJsonObject {
                    put(
                        "result", when (result) {
                            is QuickJSObject -> JsonPrimitive(result.stringify())
                            else -> JsonPrimitive(result.toString())
                        }
                    )
                }
            }
        )
    }

    val memoryTableQueryTool by lazy {
        Tool(
            name = "query_memory_table",
            description = "Query memory tables for structured data. Searches tables by keywords and returns formatted results.",
            parameters = {
                InputSchema.Obj(
                    properties = buildJsonObject {
                        put("assistantId", buildJsonObject {
                            put("type", "string")
                            put("description", "The assistant ID to query tables for")
                        })
                        put("query", buildJsonObject {
                            put("type", "string")
                            put("description", "Search keywords to match against table data")
                        })
                    },
                    required = listOf("assistantId", "query")
                )
            },
            execute = { parameters ->
                val assistantId = parameters.jsonObject["assistantId"]?.jsonPrimitive?.contentOrNull ?: ""
                val query = parameters.jsonObject["query"]?.jsonPrimitive?.contentOrNull ?: ""

                try {
                    memoryTableQueryService?.let { service ->
                        CoroutineScope(Dispatchers.Main).launch {
                            val results = withContext(Dispatchers.IO) {
                                service.searchTables(assistantId, query)
                            }

                            val formattedResults = results.map { result ->
                                buildJsonObject {
                                    put("tableName", result.table.name)
                                    put("tableDescription", result.table.description)
                                    put("columns", Json.parseToJsonElement(result.table.columnHeaders.toString()))
                                    put("rows", Json.parseToJsonElement(result.rows.toString()))
                                    put("matchedColumns", Json.parseToJsonElement(result.matchedColumns.toString()))
                                }
                            }

                            buildJsonObject {
                                put("success", true)
                                put("results", Json.parseToJsonElement(formattedResults.toString()))
                                put("totalTables", results.size)
                            }
                        }.join()
                    } ?: buildJsonObject {
                        put("success", false)
                        put("error", "MemoryTableQueryService not available")
                    }
                } catch (e: Exception) {
                    buildJsonObject {
                        put("success", false)
                        put("error", e.message ?: "Unknown error")
                    }
                }
            }
        )
    }

    fun getTools(options: List<LocalToolOption>): List<Tool> {
        val tools = mutableListOf<Tool>()
        if (options.contains(LocalToolOption.JavascriptEngine)) {
            tools.add(javascriptTool)
        }
        if (options.contains(LocalToolOption.MemoryTableQuery) && memoryTableQueryService != null) {
            tools.add(memoryTableQueryTool)
        }
        return tools
    }
}

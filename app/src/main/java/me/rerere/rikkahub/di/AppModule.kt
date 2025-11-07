package me.rerere.rikkahub.di

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.serialization.json.Json
import me.rerere.highlight.Highlighter
import me.rerere.rikkahub.AppScope
import me.rerere.rikkahub.data.ai.AILoggingManager
import me.rerere.rikkahub.data.ai.tools.LocalTools
import me.rerere.rikkahub.service.ChatService
import me.rerere.rikkahub.service.WorldBookMatcher
import me.rerere.rikkahub.service.WorldBookInjector
import me.rerere.rikkahub.service.MemoryTableInjector
import me.rerere.rikkahub.service.AutoSummaryService
import me.rerere.rikkahub.data.ai.memory.MemoryEnhancementService
import me.rerere.rikkahub.data.ai.memory.MemoryInjector
import me.rerere.rikkahub.ui.viewmodel.WorldBookViewModel
import me.rerere.rikkahub.ui.viewmodel.MemoryTableViewModel
import me.rerere.rikkahub.ui.pages.chat.ChatVM
import me.rerere.rikkahub.utils.EmojiData
import me.rerere.rikkahub.utils.EmojiUtils
import me.rerere.rikkahub.utils.JsonInstant
import me.rerere.rikkahub.utils.UpdateChecker
import me.rerere.tts.provider.TTSManager
import org.koin.dsl.module

val appModule = module {
    single<Json> { JsonInstant }

    single {
        Highlighter(get())
    }

    single {
        LocalTools(get())
    }

    single {
        UpdateChecker(get())
    }

    single {
        AppScope()
    }

    single<EmojiData> {
        EmojiUtils.loadEmoji(get())
    }

    single {
        TTSManager(get())
    }

    single {
        Firebase.crashlytics
    }

    single {
        Firebase.remoteConfig
    }

    single {
        Firebase.analytics
    }

    single {
        AILoggingManager()
    }

    single {
        WorldBookMatcher()
    }

    single {
        WorldBookInjector()
    }

    single {
        MemoryTableInjector()
    }

    // 临时禁用记忆增强服务以确保应用正常启动
    // single {
    //     MemoryEnhancementService(
    //         memorySheetDao = get(),
    //         memoryCellDao = get(),
    //         memoryColumnDao = get()
    //     )
    // }

    // // 使用lazy创建避免循环依赖
    // single {
    //     MemoryInjector(
    //         memoryEnhancementService = lazy { get<MemoryEnhancementService>() },
    //         memorySheetDao = get()
    //         // TODO: promptManager = get() - PromptManager暂未实现
    //     )
    // }

    single {
        ChatService(
            context = get(),
            appScope = get(),
            settingsStore = get(),
            conversationRepo = get(),
            memoryRepository = get(),
            generationHandler = get(),
            templateTransformer = get(),
            providerManager = get(),
            localTools = get(),
            mcpManager = get(),
            worldBookRepository = get(),
            worldBookMatcher = get(),
            worldBookInjector = get()
            // 临时移除记忆增强功能
            // memoryEnhancementService = get(),
            // memoryInjector = get()
        )
    }

    // TODO: 暂时禁用自动总结服务，调试崩溃问题
    // single {
    //     AutoSummaryService(
    //         appScope = get(),
    //         memoryTableRepository = get()
    //     )
    // }

    factory {
        WorldBookViewModel(
            repository = get(),
            matcher = get(),
            injector = get()
        )
    }

    factory {
        MemoryTableViewModel(
            repository = get(),
            injector = get()
        )
    }

    // ChatVM moved to ViewModelModule
    // factory { (id: String) ->
    //     ChatVM(
    //         id = id,
    //         context = get(),
    //         settingsStore = get(),
    //         conversationRepo = get(),
    //         chatService = get(),
    //         // autoSummaryService = get(),
    //         updateChecker = get(),
    //         analytics = get()
    //     )
    // }
}

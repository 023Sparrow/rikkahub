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
// Temporarily commented out for data layer testing
// import me.rerere.rikkahub.ui.pages.worldbook.WorldBookViewModel
// import me.rerere.rikkahub.ui.pages.memorytable.MemoryTableViewModel
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
        )
    }

    // Temporarily commented out for data layer testing
    // factory {
    //     WorldBookViewModel(
    //         repository = get()
    //     )
    // }

    // factory {
    //     MemoryTableViewModel(
    //         repository = get()
    //     )
    // }
}

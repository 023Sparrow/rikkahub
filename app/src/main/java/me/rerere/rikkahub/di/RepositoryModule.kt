package me.rerere.rikkahub.di

import me.rerere.rikkahub.data.repository.ConversationRepository
import me.rerere.rikkahub.data.repository.GenMediaRepository
import me.rerere.rikkahub.data.repository.MemoryRepository
import me.rerere.rikkahub.data.repository.WorldBookRepository
import me.rerere.rikkahub.data.repository.MemoryTableRepository
import org.koin.dsl.module

val repositoryModule = module {
    single {
        ConversationRepository(get(), get())
    }

    single {
        MemoryRepository(get())
    }

    single {
        GenMediaRepository(get())
    }

    single {
        WorldBookRepository(get())
    }

    single {
        MemoryTableRepository(get())
    }
}

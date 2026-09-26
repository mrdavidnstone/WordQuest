package com.spellinggames.app.di

import android.content.Context
import androidx.room.Room
import com.spellinggames.app.data.local.AppDatabase
import com.spellinggames.app.data.local.SpellingListDao
import com.spellinggames.app.data.local.WordDao
import com.spellinggames.app.data.local.WordProgressDao
import com.spellinggames.app.data.local.GameProgressDao
import com.spellinggames.app.data.local.AchievementDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "spelling_games.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides fun provideListDao(db: AppDatabase): SpellingListDao = db.spellingListDao()
    @Provides fun provideWordDao(db: AppDatabase): WordDao = db.wordDao()
    @Provides fun provideProgressDao(db: AppDatabase): WordProgressDao = db.wordProgressDao()
    @Provides fun provideGameProgressDao(db: AppDatabase): GameProgressDao = db.gameProgressDao()
    @Provides fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()
}

package com.spellinggames.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SpellingListEntity::class,
        WordEntity::class,
        WordProgressEntity::class,
        GameProgressEntity::class,
        AchievementEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spellingListDao(): SpellingListDao
    abstract fun wordDao(): WordDao
    abstract fun wordProgressDao(): WordProgressDao
    abstract fun gameProgressDao(): GameProgressDao
    abstract fun achievementDao(): AchievementDao
}

package com.example.todoapp.addtasks.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todoapp.addtasks.utils.Converters
import com.example.todoapp.taskcategory.data.CategoryDao
import com.example.todoapp.taskcategory.data.CategoryEntity

@Database(entities = [TaskEntity::class, CategoryEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class TodoDataBase:RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDataBase? = null

        fun getInstance(context: Context): TodoDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDataBase::class.java,
                    "TaskDatabase"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
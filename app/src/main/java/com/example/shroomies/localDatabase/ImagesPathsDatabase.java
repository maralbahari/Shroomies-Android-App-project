package com.example.shroomies.localDatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = ImagesPaths.class, version = 1, exportSchema = false)
public abstract class ImagesPathsDatabase extends RoomDatabase {
    private static final String DB_NAME = "images_paths_db";
    private static ImagesPathsDatabase instance;

    public static synchronized ImagesPathsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), ImagesPathsDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract ImagePathsDao imagePathsDao();
}

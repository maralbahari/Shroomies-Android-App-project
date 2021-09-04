package com.example.shroomies.localDatabase;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ImagePathsDao {
    @Insert
    void insertImagePath(ImagesPaths imagePath);
    @Query("SELECT * FROM images_paths WHERE messageId = :messageId")
    ImagesPaths getImagePath(String messageId);
    @Query("SELECT EXISTS(SELECT * FROM images_paths WHERE messageId = :messageId)")
    boolean isRowExists(String messageId);
}

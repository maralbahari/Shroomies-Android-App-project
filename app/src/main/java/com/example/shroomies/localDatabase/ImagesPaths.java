package com.example.shroomies.localDatabase;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "images_paths")
public class ImagesPaths {
    @PrimaryKey
    @NotNull
    private String messageId;
    private String localPath;

    public ImagesPaths(String messageId, String localPath) {
        this.messageId = messageId;
        this.localPath = localPath;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}

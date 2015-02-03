package com.wozlla.idea.file;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class SceneFileTypeFactory extends FileTypeFactory {

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
        for (int i = 0; i < SceneFileType.DEFAULT_ASSOCIATED_EXTENSIONS.length; i++) {
            fileTypeConsumer.consume(SceneFileType.INSTANCE, SceneFileType.DEFAULT_ASSOCIATED_EXTENSIONS[i]);
        }
    }
}

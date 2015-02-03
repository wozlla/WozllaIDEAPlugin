package com.wozlla.idea.file;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.wozlla.idea.Icons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SceneFileType implements FileType {

    public static final SceneFileType INSTANCE = new SceneFileType();

    @NonNls
    public static final String[] DEFAULT_ASSOCIATED_EXTENSIONS = { "jsonx" };

    @NotNull
    @Override
    public String getName() {
        return "WozllaScene";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "WozllaScene fiels";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_ASSOCIATED_EXTENSIONS[0];
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return Icons.SCENE_FILE_ICON;
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Nullable
    @Override
    public String getCharset(VirtualFile virtualFile, byte[] bytes) {
        return null;
    }
}

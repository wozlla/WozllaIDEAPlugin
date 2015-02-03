package com.wozlla.idea.editor;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.wozlla.idea.file.SceneFileType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class SceneEditorProvider implements FileEditorProvider {

    public static final String EDITOR_TYPE_ID = "WozllaSceneEditor";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return virtualFile.getFileType() instanceof SceneFileType;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return new SceneEditor(project, FileDocumentManager.getInstance().getDocument(virtualFile));
    }

    @Override
    public void disposeEditor(@NotNull FileEditor fileEditor) {
        fileEditor.dispose();
    }

    @NotNull
    @Override
    public FileEditorState readState(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile virtualFile) {
        return FileEditorState.INSTANCE;
    }

    @Override
    public void writeState(@NotNull FileEditorState fileEditorState, @NotNull Project project, @NotNull Element element) {

    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}

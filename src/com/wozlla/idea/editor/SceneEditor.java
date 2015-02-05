package com.wozlla.idea.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.ui.components.JBScrollPane;
import com.wozlla.idea.scene.GameObject;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public class SceneEditor extends UserDataHolderBase implements FileEditor, DumbAware {

    public static final String NAME = "SceneEditor";

    protected final Project project;
    protected final Document document;
    protected boolean isObsolete = true;


    protected SceneEditorKit sceneEditorKit;
    protected JEditorPane errorEditorPane;
    protected JSONObject rootJSONObject;

    public SceneEditor(@NotNull Project project, Document document) {
        this.project = project;
        this.document = document;

        // Listen to the document modifications.
        this.document.addDocumentListener(new DocumentAdapter() {
            @Override
            public void documentChanged(DocumentEvent e) {
                isObsolete = true;
            }
        });

        try {
            this.parseScene();
            this.initSceneEditorKit();
        } catch(Throwable e) {
            e.printStackTrace();
            this.rootJSONObject = null;
            this.errorEditorPane = new JEditorPane();
            this.errorEditorPane.setText("Fail to parse this file: \n" + e.toString());
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        if(this.errorEditorPane != null) {
            return this.errorEditorPane;
        }
        return sceneEditorKit;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        if(this.errorEditorPane != null) {
            return this.errorEditorPane;
        }
        return sceneEditorKit;
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @NotNull
    @Override
    public FileEditorState getState(@NotNull FileEditorStateLevel fileEditorStateLevel) {
        return FileEditorState.INSTANCE;
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {

    }

    @Override
    public boolean isModified() {
        return isObsolete;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder() {
        return null;
    }

    @Override
    public void dispose() {
        this.sceneEditorKit.onClose();
        Disposer.dispose(this);
    }

    protected void parseScene() throws JSONException {
        this.rootJSONObject = new JSONObject(new JSONTokener(this.document.getText()));
    }

    protected void initSceneEditorKit() throws JSONException {
        this.sceneEditorKit = new SceneEditorKit(this.project, this.document, this.rootJSONObject);
    }
}

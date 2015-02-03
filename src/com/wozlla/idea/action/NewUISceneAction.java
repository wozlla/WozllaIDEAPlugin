package com.wozlla.idea.action;

import com.intellij.ide.fileTemplates.FileTemplateManager;

public class NewUISceneAction extends NewTemplateAction {

    public NewUISceneAction() {
        super(FileTemplateManager.getInstance().getInternalTemplate("WozllaUIScene.jsonx"));
    }
}

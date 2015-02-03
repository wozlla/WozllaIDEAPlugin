package com.wozlla.idea.action;

import com.intellij.ide.fileTemplates.FileTemplateManager;

public class NewSceneAction extends NewTemplateAction {

    public NewSceneAction() {
        super(FileTemplateManager.getInstance().getInternalTemplate("WozllaScene.jsonx"));
    }
}

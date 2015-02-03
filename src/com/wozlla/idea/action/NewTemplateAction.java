package com.wozlla.idea.action;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.actions.CreateFromTemplateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.wozlla.idea.WozllaIDEAPlugin;

public abstract class NewTemplateAction extends CreateFromTemplateAction {

    protected NewTemplateAction(FileTemplate template) {
        super(template);
    }

    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(WozllaIDEAPlugin.isWozllaProject);
        e.getPresentation().setVisible(WozllaIDEAPlugin.isWozllaProject);
    }

}

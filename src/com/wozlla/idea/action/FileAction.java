package com.wozlla.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class FileAction extends AnAction {

    public void update(AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile[] files = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
        Presentation presentation = e.getPresentation();
        do {
            if(project == null) {
                presentation.setVisible(false);
                break;
            }
            if (files == null || files.length == 0) {
                presentation.setVisible(false);
                break;
            }
            if(!this.checkVisible(project, files)) {
                presentation.setVisible(false);
                break;
            }
            presentation.setVisible(true);
            presentation.setText(getText(project, files));
        } while(false);

    }

    protected abstract boolean checkVisible(Project project, VirtualFile[] files);

    protected abstract String getText(Project project, VirtualFile[] files);
}

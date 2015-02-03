package com.wozlla.idea;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;

public class NewFileTemplateFactory implements FileTemplateGroupDescriptorFactory {

    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("WozllaEditor", Icons.GAMEOBJECT_ICON);
        group.addTemplate(new FileTemplateDescriptor("WozllaScene.jsonx", Icons.GAMEOBJECT_ICON));
        group.addTemplate(new FileTemplateDescriptor("WozllaUIScene.jsonx", Icons.GAMEOBJECT_ICON));
        return group;
    }
}

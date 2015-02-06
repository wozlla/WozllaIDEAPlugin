package com.wozlla.idea.editor.inspector.fields;


import com.intellij.ide.dnd.DnDEvent;
import com.intellij.ide.dnd.DnDManager;
import com.intellij.ide.dnd.DnDTarget;
import com.intellij.ide.dnd.TransferableWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.wozlla.idea.Utils;
import com.wozlla.idea.WozllaIDEAPlugin;
import com.wozlla.idea.scene.PropertyObject;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class ImageSrcField extends StringField implements DnDTarget, Field.ProjectAware, Field.LabelAware {

    protected Project project;
    protected JLabel label;
    protected MouseListener labelMouseLisetner;

    public ImageSrcField(JTextComponent component, PropertyObject target, String propertyName) {
        super(component, target, propertyName);
        component.setEditable(false);
        this.initFieldValues();
        DnDManager.getInstance().registerTarget(this, component);
    }

    public ImageSrcField(PropertyObject target, String propertyName) {
        this(new JTextField(), target, propertyName);
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void setLabel(JLabel label) {
        this.label = label;
        this.label.addMouseListener(labelMouseLisetner = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    setValue("");
                    updateTargetPropertyValue();
                }
            }
        });
    }

    public void destroy() {
        super.destroy();
        if(this.labelMouseLisetner != null) {
            this.label.removeMouseListener(this.labelMouseLisetner);
        }
        DnDManager.getInstance().unregisterTarget(this, this.getComponent());
    }

    @Override
    public void cleanUpOnLeave() {

    }

    @Override
    public void updateDraggedImage(Image image, Point dropPoint, Point imageOffset) {

    }

    @Override
    public void drop(DnDEvent event) {
        File file = getSingleFile(event);
        if(!event.isDropPossible()) {
            return;
        }
        if(file != null) {
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
            if(virtualFile != null) {
                this.setValue(Utils.getProjectPath(project, virtualFile));
                this.updateTargetPropertyValue();
            }
        }
    }

    @Override
    public boolean update(DnDEvent event) {
        File file = getSingleFile(event);
        if(file != null && file.getName().matches(WozllaIDEAPlugin.IMAGE_PATTERN.pattern())) {
            event.setDropPossible(true);
        } else {
            event.setDropPossible(false);
        }
        return false;
    }

    private File getSingleFile(DnDEvent event) {
        java.util.List<File> result;
        Object attached = event.getAttachedObject();
        if (attached instanceof TransferableWrapper) {
            result = ((TransferableWrapper)attached).asFileList();
            if(result != null && result.size() == 1) {
                return result.get(0);
            }
        }
        return null;
    }
}

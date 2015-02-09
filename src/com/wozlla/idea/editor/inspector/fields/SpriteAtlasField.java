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
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

public class SpriteAtlasField extends StringField implements DnDTarget, Field.ProjectAware, Field.LabelAware {

    private static String history;

    protected Project project;
    protected VirtualFile boundFile;
    protected JSONObject spriteData;

    public SpriteAtlasField(JTextComponent component, PropertyObject target, String propertyName) {
        super(component, target, propertyName);
        component.setEditable(false);
        this.initFieldValues();
        DnDManager.getInstance().registerTarget(this, component);
    }

    public SpriteAtlasField(PropertyObject target, String propertyName) {
        this(new JTextField(), target, propertyName);
    }

    public void setProject(Project project) {
        this.project = project;
        String value = getValue();
        if(null == value || "".equals(value)) {
            if(history != null) {
                setValue(history);
                updateTargetPropertyValue();
            }
        }
        this.boundFile = project.getBaseDir().findFileByRelativePath(getValue());
    }

    @Override
    public void setLabel(JLabel label) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    setValue("");
                    updateTargetPropertyValue();
                }
            }
        });
    }

    public VirtualFile getBoundFile() {
        return boundFile;
    }

    public JSONObject getSpriteData() {
        if(spriteData == null) {
            try {
                spriteData = Utils.virtualFile2JSONObject(boundFile);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return spriteData;
    }

    public void destroy() {
        super.destroy();
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
        if(!event.isDropPossible()) {
            return;
        }
        File file = getSingleFile(event);
        if(file != null) {
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
            if (virtualFile != null) {
                this.boundFile = virtualFile;
                this.spriteData = null;
                this.setValue(Utils.getProjectPath(project, virtualFile));
                history = this.getValue();
                updateTargetPropertyValue();
            }
        }
    }

    @Override
    public boolean update(DnDEvent event) {
        File file = getSingleFile(event);
        if(file != null && file.getName().endsWith(WozllaIDEAPlugin.SPRITE_ATLAS_SUFFIX)) {
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

package com.wozlla.idea.editor.inspector.fields;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.wozlla.idea.Utils;
import com.wozlla.idea.WozllaIDEAPlugin;
import com.wozlla.idea.scene.PropertyObject;
import org.codehaus.jettison.json.JSONArray;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class RenderLayerField extends PropertyBindField<String, ComboBox> implements Field.GridBagLayoutAware, Field.ProjectAware {

    public RenderLayerField(PropertyObject target, String propertyName) {
        super(new ComboBox(), target, propertyName);
    }

    @Override
    public void setProject(Project project) {
        ComboBox combo = this.getComponent();
        combo.addItem(WozllaIDEAPlugin.DEFAULT_RENDER_LAYER);

        try {
            VirtualFile file = project.getBaseDir().findFileByRelativePath(WozllaIDEAPlugin.RENDER_LAYERS_PATH);
            JSONArray layers = Utils.virtualFile2JSONArray(file);
            for(int i=0,len=layers.length(); i<len; i++) {
                combo.addItem(layers.getString(i));
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        this.initFieldValues();
        this.getComponent().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    updateTargetPropertyValue();
                }
            }
        });
    }

    @Override
    public void setValue(String value) {
        getComponent().setSelectedItem(value);
    }

    @Override
    public String getValue() {
        return getComponent().getSelectedItem().toString();
    }

    @Override
    public LayoutParams getLayoutParams() {
        LayoutParams params = new LayoutParams();
        return params;
    }

}

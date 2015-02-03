package com.wozlla.idea.editor.inspector;

import com.intellij.openapi.project.Project;
import com.wozlla.idea.IComponentConfigManager;
import com.wozlla.idea.editor.inspector.fields.Field;
import com.wozlla.idea.scene.ComponentConfig;
import com.wozlla.idea.scene.Component;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import javax.swing.*;
import java.awt.*;

public class ComponentPane extends CommonPane {

    private Project project;
    private Component component;

    public ComponentPane(Project project, Component component) {
        super(component.getName(), new JPanel(), true);
        this.project = project;
        this.component = component;

        JPanel content = (JPanel)this.getContent();
        content.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;

        String componentName = this.component.getName();
        IComponentConfigManager mgr = project.getComponent(IComponentConfigManager.class);
        ComponentConfig config = mgr.getComponentConfig(componentName);

        for(String name : config.properties) {
            ComponentConfig.PropertyConfig propConfig = config.propertyConfigMap.get(name);
            System.out.println("Create Field[" + propConfig.name + "]");
            try {
                component.ensureProperty(propConfig.name, propConfig.defaultValue);
            } catch(JSONException e) {
                throw new RuntimeException(e);
            }
            Field field;
            if("combobox".equalsIgnoreCase(propConfig.getEditorType())) {
                JSONArray array = (JSONArray)propConfig.data;
                String[] data = new String[array.length()];
                try {
                    for (int i = 0; i < array.length(); i++) {
                        data[i] = array.get(i).toString();
                    }
                } catch(JSONException e) {
                    throw new RuntimeException(e);
                }
                field = FieldFactory.create(propConfig.getEditorType(), component, propConfig.name, data);
            } else {
                field = FieldFactory.create(propConfig.getEditorType(), component, propConfig.name);
            }
            if(field instanceof GridBagLayoutAware) {

                // add label
                gc.gridx = 0;
                gc.gridwidth = 1;
                gc.gridheight = 1;
                gc.weightx = 0.5;
                gc.fill = GridBagConstraints.HORIZONTAL;
                content.add(new JLabel(propConfig.name), gc);

                // add field
                GridBagLayoutAware.LayoutParams params = ((GridBagLayoutAware) field).getLayoutParams();
                gc.gridx = 1;
                gc.gridwidth = params.spanX;
                gc.gridheight = params.spanY;
                gc.weightx = params.weightx;
                gc.fill = params.fill;
                content.add(field.getComponent(), gc);

                gc.gridy += gc.gridheight;
            }
        }
    }

    public Component getInspectingComponent() {
        return component;
    }
}

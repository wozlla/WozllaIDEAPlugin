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
import java.util.HashMap;
import java.util.Map;

public class ComponentPane extends CommonPane {

    private Project project;
    private Component component;
    private Map<String, Field> fieldMap;

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

        fieldMap = new HashMap<String, Field>();

        for(String name : config.properties) {
            ComponentConfig.PropertyConfig propConfig = config.propertyConfigMap.get(name);
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
            this.fieldMap.put(propConfig.name, field);
            if(field instanceof ProjectAware) {
                ((ProjectAware)field).setProject(project);
            }
            if(field instanceof ComponentPaneAware) {
                ((ComponentPaneAware)field).setComponentPane(this);
            }
            if(field instanceof PropertyConfigAware) {
                ((PropertyConfigAware)field).setPropertyConfig(propConfig);
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

    public Project getProject() {
        return this.project;
    }

    public Field getField(String name) {
        return this.fieldMap.get(name);
    }

    public Component getInspectingComponent() {
        return component;
    }

    public void destroyFields() {
        this.fieldMap.clear();
        JComponent content = this.getContent();
        for(java.awt.Component comp : content.getComponents()) {
            if(comp instanceof Field) {
                ((Field)comp).destroy();
            }
        }
    }
}

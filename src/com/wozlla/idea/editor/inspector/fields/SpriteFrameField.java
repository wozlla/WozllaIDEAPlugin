package com.wozlla.idea.editor.inspector.fields;

import com.wozlla.idea.dialog.SpriteFrameSelector;
import com.wozlla.idea.editor.inspector.ComponentPane;
import com.wozlla.idea.scene.ComponentConfig;
import com.wozlla.idea.scene.PropertyObject;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SpriteFrameField extends StringField implements Field.PropertyConfigAware, Field.ComponentPaneAware {

    protected ComponentConfig.PropertyConfig config;
    protected ComponentPane componentPane;

    protected boolean showingDialog = false;

    public SpriteFrameField(final JTextComponent component, PropertyObject target, String propertyName) {
        super(component, target, propertyName);
        component.setEditable(false);
        this.initFieldValues();
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SpriteFrameField.this.mouseClicked(e);
            }
        });
    }

    public SpriteFrameField(PropertyObject target, String propertyName) {
        this(new JTextField(), target, propertyName);
    }

    @Override
    public void setPropertyConfig(ComponentConfig.PropertyConfig config) {
        this.config = config;
    }

    @Override
    public void setComponentPane(ComponentPane pane) {
        this.componentPane = pane;
    }

    public void mouseClicked(MouseEvent e) {
        if(showingDialog) {
            return;
        }
        if(componentPane != null) {
            JSONObject data = (JSONObject)config.data;
            try {
                Field field = componentPane.getField(data.getString("fromSpriteAtlas"));
                if(field != null && field instanceof SpriteAtlasField) {
                    SpriteAtlasField atlasField = (SpriteAtlasField) field;
                    if(atlasField.getBoundFile() != null) {
                        SpriteFrameSelector selector = new SpriteFrameSelector(
                                componentPane.getProject(),
                                atlasField.getBoundFile(),
                                atlasField.getSpriteData(),
                                getValue()
                        );
                        showingDialog = true;
                        if(selector.showAndGet()) {
                            setValue(selector.getSelectedFrame());
                            updateTargetPropertyValue();
                        }
                        showingDialog = false;
                    }
                }
            } catch(JSONException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}

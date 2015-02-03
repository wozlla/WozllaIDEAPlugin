package com.wozlla.idea.editor.inspector.fields;

import com.intellij.util.ui.CheckBox;
import com.wozlla.idea.editor.inspector.GridBagLayoutAware;
import com.wozlla.idea.scene.PropertyObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class BooleanField extends PropertyBindField<Boolean, JCheckBox> implements GridBagLayoutAware {

    public BooleanField(JCheckBox component, PropertyObject target, String propertyName) {
        super(component, target, propertyName);
        component.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateTargetPropertyValue();
            }
        });
    }

    public BooleanField(PropertyObject target, String propertyName) {
        this(new JCheckBox(), target, propertyName);
    }

    @Override
    public void setValue(Boolean value) {
        getComponent().setSelected(value);
    }

    @Override
    public Boolean getValue() {
        return getComponent().isSelected();
    }

    @Override
    public LayoutParams getLayoutParams() {
        LayoutParams params = new LayoutParams();
        return params;
    }
}

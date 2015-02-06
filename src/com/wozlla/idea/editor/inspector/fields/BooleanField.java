package com.wozlla.idea.editor.inspector.fields;

import com.wozlla.idea.scene.PropertyObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BooleanField extends PropertyBindField<Boolean, JCheckBox> implements Field.GridBagLayoutAware {

    public BooleanField(JCheckBox component, PropertyObject target, String propertyName) {
        super(component, target, propertyName);
        component.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateTargetPropertyValue();
            }
        });
        this.initFieldValues();
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

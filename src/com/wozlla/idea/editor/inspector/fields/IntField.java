package com.wozlla.idea.editor.inspector.fields;

import com.wozlla.idea.editor.inspector.GridBagLayoutAware;
import com.wozlla.idea.scene.PropertyObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class IntField extends PropertyBindField<Integer, JSpinner> implements GridBagLayoutAware {

    public IntField(JSpinner component, PropertyObject target, String propertyName) {
        super(component, target, propertyName);
        component.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateTargetPropertyValue();
            }
        });
    }

    public IntField(PropertyObject target, String propertyName) {
        this(new JSpinner(), target, propertyName);
    }

    @Override
    public void setValue(Integer value) {

        getComponent().setValue(value);
    }

    @Override
    public Integer getValue() {
        return (Integer)getComponent().getValue();
    }

    @Override
    public LayoutParams getLayoutParams() {
        return new LayoutParams();
    }
}

package com.wozlla.idea.editor.inspector.fields;

import com.wozlla.idea.scene.PropertyObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NumberField extends PropertyBindField<Double, JSpinner> implements Field.GridBagLayoutAware {

    public NumberField(JSpinner component, PropertyObject target, String propertyName) {
        super(component, target, propertyName);
        this.initFieldValues();
        component.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println(getValue());
                updateTargetPropertyValue();
            }
        });
    }

    public NumberField(PropertyObject target, String propertyName) {
        this(new JSpinner(new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0)), target, propertyName);
    }

    @Override
    public void setValue(Double value) {
        getComponent().setValue(value);
    }

    @Override
    public Double getValue() {
        return (Double)getComponent().getValue();
    }

    @Override
    public Double getTargetPropertyValue() {
        return Double.valueOf(getTarget().getProperty(getName()).toString());
    }

    @Override
    public GridBagLayoutAware.LayoutParams getLayoutParams() {
        return new GridBagLayoutAware.LayoutParams();
    }
}

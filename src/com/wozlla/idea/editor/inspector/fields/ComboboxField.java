package com.wozlla.idea.editor.inspector.fields;

import com.intellij.openapi.ui.ComboBox;
import com.wozlla.idea.scene.PropertyObject;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ComboboxField<T> extends PropertyBindField<T, ComboBox> implements Field.GridBagLayoutAware {

    public ComboboxField(ComboBox component, PropertyObject target, String propertyName) {
        this(component, target, propertyName, null);
    }

    public ComboboxField(PropertyObject target, String propertyName, T[] items) {
        this(new ComboBox(), target, propertyName, items);
    }

    public ComboboxField(ComboBox component, PropertyObject target, String propertyName, T[] items) {
        super(component, target, propertyName);
        if(items != null) {
            for (Object item : items) {
                component.addItem(item);
            }
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
    public void setValue(T value) {
        getComponent().setSelectedItem(value);
    }

    @Override
    public T getValue() {
        return (T)getComponent().getSelectedItem();
    }

    @Override
    public LayoutParams getLayoutParams() {
        LayoutParams params = new LayoutParams();
        return params;
    }

}

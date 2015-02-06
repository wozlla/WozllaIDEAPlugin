package com.wozlla.idea.editor.inspector.fields;

import com.wozlla.idea.scene.PropertyObject;
import org.codehaus.jettison.json.JSONException;

import javax.swing.*;

public abstract class PropertyBindField<T extends Object, C extends JComponent> implements Field<T, C>, PropertyObject.ChangeListener {

    private PropertyObject target;
    private C component;
    private String propertyName;

    public PropertyBindField(C component, PropertyObject target, String propertyName) {
        this.target = target;
        this.target.addChangeListener(this);
        this.component = component;
        this.propertyName = propertyName;

    }

    public PropertyObject getTarget() {
        return target;
    }

    @Override
    public C getComponent() {
        return this.component;
    }

    @Override
    public String getName() {
        return propertyName;
    }

    public void initFieldValues() {
        this.setValue(getTargetPropertyValue());
        this.target.addChangeListener(this);
    }

    @Override
    public void onChange(String key, Object newValue, Object oldValue) {
        if(key.equals(propertyName)) {
            Object fieldValue = this.getValue();
            if(newValue != fieldValue && !fieldValue.equals(newValue)) {
                this.setValue((T)newValue);
            }
        }
    }

    public T getTargetPropertyValue() {
        return (T) target.getProperty(propertyName);
    }

    public void setTargetPropertyValue(T value) {
        this.target.setProperty(getName(), value);
    }

    public void updateTargetPropertyValue() {
        this.setTargetPropertyValue(getValue());
    }

    public void destroy() {
        this.target.removeChangeListener(this);
    }

}

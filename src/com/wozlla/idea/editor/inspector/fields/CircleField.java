package com.wozlla.idea.editor.inspector.fields;

import com.wozlla.idea.editor.inspector.GridBagLayoutAware;
import com.wozlla.idea.scene.PropertyObject;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class CircleField extends PropertyBindField<JSONArray, JPanel> implements GridBagLayoutAware, ChangeListener {

    JSpinner centerX = new JSpinner();
    JSpinner centerY = new JSpinner();
    JSpinner radius = new JSpinner();

    public CircleField(PropertyObject target, String propertyName) {
        super(new JPanel(new GridLayout(1, 4)), target, propertyName);
        JPanel panel = this.getComponent();
        panel.add(centerX);
        panel.add(centerY);
        panel.add(radius);
        centerX.addChangeListener(this);
        centerY.addChangeListener(this);
        radius.addChangeListener(this);
        this.setValue(getTargetPropertyValue());
    }

    @Override
    public void setValue(JSONArray value) {
        if(centerX == null) {
            return;
        }
        try {
            centerX.setValue(value.get(0));
            centerY.setValue(value.get(1));
            radius.setValue(value.get(2));
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JSONArray getValue() {
        JSONArray value = new JSONArray();
        value.put(centerX.getValue());
        value.put(centerY.getValue());
        value.put(radius.getValue());
        return value;
    }

    @Override
    public LayoutParams getLayoutParams() {
        LayoutParams params = new LayoutParams();
        return params;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        updateTargetPropertyValue();
    }
}

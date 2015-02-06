package com.wozlla.idea.editor.inspector.fields;

import com.wozlla.idea.scene.PropertyObject;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class PointField extends PropertyBindField<JSONArray, JPanel> implements Field.GridBagLayoutAware, ChangeListener {

    protected JSpinner xSpinner = new JSpinner();
    protected JSpinner ySpinner = new JSpinner();

    public PointField(PropertyObject target, String propertyName) {
        super(new JPanel(new GridLayout(1, 2)), target, propertyName);
        JPanel panel = this.getComponent();
        panel.add(xSpinner);
        panel.add(ySpinner);
        this.initFieldValues();
        xSpinner.addChangeListener(this);
        ySpinner.addChangeListener(this);
    }

    @Override
    public void setValue(JSONArray value) {
        if(xSpinner == null) {
            return;
        }
        try {
            xSpinner.setValue(value.get(0));
            ySpinner.setValue(value.get(1));
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JSONArray getValue() {
        JSONArray value = new JSONArray();
        value.put(xSpinner.getValue());
        value.put(ySpinner.getValue());
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

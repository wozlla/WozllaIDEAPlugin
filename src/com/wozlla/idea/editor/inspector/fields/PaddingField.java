package com.wozlla.idea.editor.inspector.fields;

import com.wozlla.idea.editor.inspector.GridBagLayoutAware;
import com.wozlla.idea.scene.PropertyObject;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class PaddingField extends PropertyBindField<JSONArray, JPanel> implements GridBagLayoutAware, ChangeListener {

    JSpinner topSpinner = new JSpinner();
    JSpinner leftSpinner = new JSpinner();
    JSpinner bottomSpinner = new JSpinner();
    JSpinner rightSpinner = new JSpinner();

    public PaddingField(PropertyObject target, String propertyName) {
        super(new JPanel(new GridLayout(1, 4)), target, propertyName);
        JPanel panel = this.getComponent();
        panel.add(topSpinner);
        panel.add(leftSpinner);
        panel.add(bottomSpinner);
        panel.add(rightSpinner);
        topSpinner.addChangeListener(this);
        leftSpinner.addChangeListener(this);
        bottomSpinner.addChangeListener(this);
        rightSpinner.addChangeListener(this);
        this.setValue(getTargetPropertyValue());
    }

    @Override
    public void setValue(JSONArray value) {
        if(topSpinner == null) {
            return;
        }
        try {
            topSpinner.setValue(value.get(0));
            leftSpinner.setValue(value.get(1));
            bottomSpinner.setValue(value.get(2));
            rightSpinner.setValue(value.get(3));
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JSONArray getValue() {
        JSONArray value = new JSONArray();
        value.put(topSpinner.getValue());
        value.put(leftSpinner.getValue());
        value.put(bottomSpinner.getValue());
        value.put(rightSpinner.getValue());
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

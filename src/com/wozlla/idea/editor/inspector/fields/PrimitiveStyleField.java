package com.wozlla.idea.editor.inspector.fields;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.wozlla.idea.scene.PropertyObject;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

public class PrimitiveStyleField extends PropertyBindField<JSONObject, JPanel> implements Field.GridBagLayoutAware, Field.WithoutLabel {

    protected JSpinner alphaSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.01));
    protected JCheckBox strokeCheckBox = new JBCheckBox();
    protected JSpinner strokeWidthSpinner = new JSpinner();
    protected JTextField strokeColorField = new JTextField();
    protected JCheckBox fillCheckBox = new JBCheckBox();
    protected JTextField fillColorField = new JTextField();

    public PrimitiveStyleField(PropertyObject target, String propertyName) {
        super(new JPanel(), target, propertyName);

        JPanel content = this.getComponent();
        content.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        int gridy = 0;
        int fullGridWidth = 4;

        addLabel(gc, "alpha", gridy++);
        gc.gridx = 1;
        gc.gridwidth = fullGridWidth-1;
        content.add(alphaSpinner, gc);

        addLabel(gc, "stroke", gridy++);
        gc.gridx = 1;
        gc.gridwidth = 1;
        content.add(strokeCheckBox, gc);
        gc.gridx = 2;
        gc.gridwidth = 1;
        content.add(strokeColorField, gc);
        gc.gridx = 3;
        gc.gridwidth = 1;
        content.add(strokeWidthSpinner, gc);

        addLabel(gc, "fill", gridy++);
        gc.gridx = 1;
        gc.gridwidth = 1;
        content.add(fillCheckBox, gc);
        gc.gridx = 2;
        gc.gridwidth = 1;
        content.add(fillColorField, gc);

        this.initFieldValues();

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateTargetPropertyValue();
                }
            }
        };

        FocusAdapter focusAdapter = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                updateTargetPropertyValue();
            }
        };

        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateTargetPropertyValue();
            }
        };

        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    updateTargetPropertyValue();
                }
            }
        };

        alphaSpinner.addChangeListener(changeListener);
        strokeCheckBox.addChangeListener(changeListener);
        strokeColorField.addKeyListener(keyAdapter);
        strokeColorField.addFocusListener(focusAdapter);
        strokeWidthSpinner.addChangeListener(changeListener);
        fillCheckBox.addChangeListener(changeListener);
        fillColorField.addKeyListener(keyAdapter);
        fillColorField.addFocusListener(focusAdapter);
    }

    @Override
    public void setValue(JSONObject value) {
        if(alphaSpinner != null) {
            try {
                alphaSpinner.setValue(value.getDouble("alpha"));
                strokeCheckBox.setSelected(value.getBoolean("stroke"));
                strokeColorField.setText(value.getString("strokeColor"));
                strokeWidthSpinner.setValue(value.getInt("strokeWidth"));
                fillCheckBox.setSelected(value.getBoolean("fill"));
                fillColorField.setText(value.getString("fillColor"));
            } catch(JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public JSONObject getValue() {
        JSONObject value = new JSONObject();
        try {
            value.put("alpha", alphaSpinner.getValue());
            value.put("stroke", strokeCheckBox.isSelected());
            value.put("strokeColor", strokeColorField.getText());
            value.put("strokeWidth", strokeWidthSpinner.getValue());
            value.put("fill", fillCheckBox.isSelected());
            value.put("fillColor", fillColorField.getText());
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    @Override
    public LayoutParams getLayoutParams() {
        LayoutParams params = new LayoutParams();
        params.gridX = 0;
        params.spanX = 3;
        params.spanY = 5;
        return params;
    }

    protected JLabel addLabel(GridBagConstraints gc, String labelName, int gridy) {
        JPanel content = getComponent();
        JLabel label;
        gc.gridx = 0;
        gc.gridy = gridy;
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.weightx = 0.5;
        gc.fill = GridBagConstraints.HORIZONTAL;
        content.add(label = new JLabel(labelName), gc);
        return label;
    }
}

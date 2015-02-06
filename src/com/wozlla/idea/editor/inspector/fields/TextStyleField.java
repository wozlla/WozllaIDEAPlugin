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

public class TextStyleField extends PropertyBindField<JSONObject, JPanel> implements Field.GridBagLayoutAware, Field.WithoutLabel {

    protected JTextField fontField = new JTextField();
    protected JTextField colorField = new JTextField();
    protected JCheckBox shadowCheckBox = new JBCheckBox();
    protected JSpinner shadowOffsetXSpinner = new JSpinner();
    protected JSpinner shadowOffsetYSpinner = new JSpinner();
    protected JCheckBox strokeCheckBox = new JBCheckBox();
    protected JTextField strokeColorField = new JTextField();
    protected JSpinner strokeWidthSpinner = new JSpinner();
    protected ComboBox alignComboBox = new ComboBox(new String[] {
        "start", "center", "end"
    });
    protected ComboBox baseLineComboBox = new ComboBox(new String[] {
        "top", "middle", "bottom"
    });

    public TextStyleField(PropertyObject target, String propertyName) {
        super(new JPanel(), target, propertyName);

        JPanel content = this.getComponent();
        content.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        int gridy = 0;
        int fullGridWidth = 4;

        addLabel(gc, "font", gridy++);
        gc.gridx = 1;
        gc.gridwidth = fullGridWidth-1;
        content.add(fontField, gc);

        addLabel(gc, "color", gridy++);
        gc.gridx = 1;
        gc.gridwidth = fullGridWidth-1;
        content.add(colorField, gc);

        addLabel(gc, "shadow", gridy++);
        gc.gridx = 1;
        gc.gridwidth = 1;
        content.add(shadowCheckBox, gc);
        gc.gridx = 2;
        gc.gridwidth = 1;
        content.add(shadowOffsetXSpinner, gc);
        gc.gridx = 3;
        gc.gridwidth = 1;
        content.add(shadowOffsetYSpinner, gc);

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

        addLabel(gc, "align", gridy++);
        gc.gridx = 1;
        gc.gridwidth = 1;
        content.add(alignComboBox, gc);
        gc.gridx = 2;
        gc.gridwidth = 1;
        content.add(baseLineComboBox, gc);

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

        fontField.addKeyListener(keyAdapter);
        fontField.addFocusListener(focusAdapter);

        colorField.addKeyListener(keyAdapter);
        colorField.addFocusListener(focusAdapter);

        shadowCheckBox.addItemListener(itemListener);
        shadowOffsetYSpinner.addChangeListener(changeListener);
        shadowOffsetYSpinner.addChangeListener(changeListener);

        strokeCheckBox.addItemListener(itemListener);
        strokeColorField.addKeyListener(keyAdapter);
        strokeColorField.addFocusListener(focusAdapter);

        strokeWidthSpinner.addChangeListener(changeListener);

        alignComboBox.addItemListener(itemListener);
        baseLineComboBox.addItemListener(itemListener);
    }

    @Override
    public void setValue(JSONObject value) {
        if(fontField != null) {
            try {
                fontField.setText(value.getString("font"));
                colorField.setText(value.getString("color"));
                shadowCheckBox.setSelected(value.getBoolean("shadow"));
                shadowOffsetXSpinner.setValue(value.getInt("shadowOffsetX"));
                shadowOffsetYSpinner.setValue(value.getInt("shadowOffsetY"));
                strokeCheckBox.setSelected(value.getBoolean("stroke"));
                strokeColorField.setText(value.getString("strokeColor"));
                strokeWidthSpinner.setValue(value.getInt("strokeWidth"));
                alignComboBox.setSelectedItem(value.getString("align"));
                baseLineComboBox.setSelectedItem(value.getString("baseline"));
            } catch(JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public JSONObject getValue() {
        JSONObject value = new JSONObject();
        try {
            value.put("font", fontField.getText());
            value.put("color", colorField.getText());
            value.put("shadow", shadowCheckBox.isSelected());
            value.put("shadowOffsetX", shadowOffsetXSpinner.getValue());
            value.put("shadowOffsetY", shadowOffsetYSpinner.getValue());
            value.put("stroke", strokeCheckBox.isSelected());
            value.put("strokeColor", strokeColorField.getText());
            value.put("strokeWidth", strokeWidthSpinner.getValue());
            value.put("align", alignComboBox.getSelectedItem().toString());
            value.put("baseline", baseLineComboBox.getSelectedItem().toString());
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

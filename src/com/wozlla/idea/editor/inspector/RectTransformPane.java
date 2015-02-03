package com.wozlla.idea.editor.inspector;

import com.wozlla.idea.editor.inspector.fields.BooleanField;
import com.wozlla.idea.editor.inspector.fields.ComboboxField;
import com.wozlla.idea.editor.inspector.fields.Field;
import com.wozlla.idea.editor.inspector.fields.NumberField;
import com.wozlla.idea.scene.GameObject;
import com.wozlla.idea.scene.Transform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import static java.awt.GridBagConstraints.HORIZONTAL;

public class RectTransformPane extends CommonPane {

    private GameObject gameObject;
    private Transform transform;
    private Map<String, Object> fieldMap = new HashMap<String, Object>();

    public RectTransformPane(GameObject gameObject) {
        super("RectTransform", new JPanel(), false);
        this.gameObject = gameObject;
        this.transform = gameObject.getTransform();

        ComboboxField<String> anchorModelField;

        fieldMap.put("anchorMode", anchorModelField = new ComboboxField<String>(transform, "anchorMode", new String[] {
                "Left_Top",
                "Left_Middle",
                "Left_Bottom",
                "Left_VStrength",
                "Center_Top",
                "Center_Middle",
                "Center_Bottom",
                "Center_VStrength",
                "Right_Top",
                "Right_Middle",
                "Right_Bottom",
                "Right_VStrength",
                "HStrength_Top",
                "HStrength_Middle",
                "HStrength_Bottom",
                "HStrength_VStrength"
        }));
        fieldMap.put("x", new NumberField(transform, "px"));
        fieldMap.put("y", new NumberField(transform, "py"));
        fieldMap.put("top", new NumberField(transform, "top"));
        fieldMap.put("left", new NumberField(transform, "left"));
        fieldMap.put("right", new NumberField(transform, "right"));
        fieldMap.put("bottom", new NumberField(transform, "bottom"));
        fieldMap.put("width", new NumberField(transform, "width"));
        fieldMap.put("height", new NumberField(transform, "height"));

        JPanel content = (JPanel)this.getContent();
        content.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        this.addWithConstraints(content, gc, 0, 0, 1, 1, HORIZONTAL, new JLabel("anchor mode"));
        this.addWithConstraints(content, gc, 1, 0, 2, 1, HORIZONTAL, getFieldComponent("anchorMode"));

        this.addWithConstraints(content, gc, 0, 1, 1, 1, HORIZONTAL, new JLabel("position"));
        this.addWithConstraints(content, gc, 1, 1, 1, 1, HORIZONTAL, getFieldComponent("x"));
        this.addWithConstraints(content, gc, 2, 1, 1, 1, HORIZONTAL, getFieldComponent("y"));

        this.addWithConstraints(content, gc, 0, 2, 1, 1, HORIZONTAL, new JLabel("left"));
        this.addWithConstraints(content, gc, 1, 2, 2, 1, HORIZONTAL, getFieldComponent("left"));

        this.addWithConstraints(content, gc, 0, 3, 1, 1, HORIZONTAL, new JLabel("top"));
        this.addWithConstraints(content, gc, 1, 3, 2, 1, HORIZONTAL, getFieldComponent("top"));

        this.addWithConstraints(content, gc, 0, 4, 1, 1, HORIZONTAL, new JLabel("right"));
        this.addWithConstraints(content, gc, 1, 4, 2, 1, HORIZONTAL, getFieldComponent("right"));

        this.addWithConstraints(content, gc, 0, 5, 1, 1, HORIZONTAL, new JLabel("bottom"));
        this.addWithConstraints(content, gc, 1, 5, 2, 1, HORIZONTAL, getFieldComponent("bottom"));


        this.addWithConstraints(content, gc, 0, 6, 1, 1, HORIZONTAL, new JLabel("size"));
        this.addWithConstraints(content, gc, 1, 6, 1, 1, HORIZONTAL, getFieldComponent("width"));
        this.addWithConstraints(content, gc, 2, 6, 1, 1, HORIZONTAL, getFieldComponent("height"));

        anchorModelField.getComponent().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateOtherFieldState(e.getItem().toString());
            }
        });

        updateOtherFieldState(anchorModelField.getValue());
    }

    public void updateOtherFieldState(String anchorMode) {
        String[] modes = anchorMode.split("_");
        String hmode = modes[0];
        String vmode = modes[1];
        if(hmode.equals("Left") || hmode.equals("Center") || hmode.equals("Right")) {
            setComponenEnabled("x", true);
            setComponenEnabled("left", false);
            setComponenEnabled("right", false);
            setComponenEnabled("width", true);
        } else {
            setComponenEnabled("x", false);
            setComponenEnabled("left", true);
            setComponenEnabled("right", true);
            setComponenEnabled("width", false);
        }
        if(vmode.equals("Top") || vmode.equals("Middle") || vmode.equals("Bottom")) {
            setComponenEnabled("y", true);
            setComponenEnabled("top", false);
            setComponenEnabled("bottom", false);
            setComponenEnabled("height", true);
        } else {
            setComponenEnabled("y", false);
            setComponenEnabled("top", true);
            setComponenEnabled("bottom", true);
            setComponenEnabled("height", false);
        }
    }

    protected void setComponenEnabled(String name, boolean enabled) {
        getFieldComponent(name).setEnabled(enabled);
    }

    public void addWithConstraints(JComponent container, GridBagConstraints gc, int x, int y,
                                   int spanX, int spanY, int fill, JComponent comp) {
        gc.gridx = x;
        gc.gridy = y;
        gc.gridwidth = spanX;
        gc.gridheight = spanY;
        gc.fill = fill;
        gc.weightx = 0.5;
        container.add(comp, gc);
    }

    protected JComponent getFieldComponent(String name) {
        Object field = fieldMap.get(name);
        if(field instanceof Field) {
            return ((Field) field).getComponent();
        }
        return (JComponent)field;
    }
}

package com.wozlla.idea.editor.inspector;

import com.wozlla.idea.editor.inspector.fields.BooleanField;
import com.wozlla.idea.editor.inspector.fields.Field;
import com.wozlla.idea.editor.inspector.fields.IntField;
import com.wozlla.idea.editor.inspector.fields.NumberField;
import com.wozlla.idea.scene.GameObject;
import com.wozlla.idea.scene.Transform;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static java.awt.GridBagConstraints.HORIZONTAL;

public class TransformPane extends CommonPane {

    private GameObject gameObject;
    private Transform transform;
    private Map<String, Object> fieldMap = new HashMap<String, Object>();

    public TransformPane(GameObject gameObject) {
        super("Transform", new JPanel(), false);
        this.gameObject = gameObject;
        this.transform = gameObject.getTransform();

        fieldMap.put("x", new NumberField(transform, "x"));
        fieldMap.put("y", new NumberField(transform, "y"));
        fieldMap.put("scaleX", new NumberField(transform, "scaleX"));
        fieldMap.put("scaleY", new NumberField(transform, "scaleY"));
        fieldMap.put("rotation", new NumberField(transform, "rotation"));
        fieldMap.put("skewX", new NumberField(transform, "skewX"));
        fieldMap.put("skewY", new NumberField(transform, "skewY"));
        fieldMap.put("relative", new BooleanField(transform, "relative"));

        JPanel content = (JPanel)this.getContent();
        content.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        this.addWithConstraints(content, gc, 0, 0, 1, 1, HORIZONTAL, new JLabel("position"));
        this.addWithConstraints(content, gc, 1, 0, 1, 1, HORIZONTAL, getFieldComponent("x"));
        this.addWithConstraints(content, gc, 2, 0, 1, 1, HORIZONTAL, getFieldComponent("y"));

        this.addWithConstraints(content, gc, 0, 1, 1, 1, HORIZONTAL, new JLabel("scale"));
        this.addWithConstraints(content, gc, 1, 1, 1, 1, HORIZONTAL, getFieldComponent("scaleX"));
        this.addWithConstraints(content, gc, 2, 1, 1, 1, HORIZONTAL, getFieldComponent("scaleY"));

        this.addWithConstraints(content, gc, 0, 2, 1, 1, HORIZONTAL, new JLabel("rotation"));
        this.addWithConstraints(content, gc, 1, 2, 2, 1, HORIZONTAL, getFieldComponent("rotation"));


        this.addWithConstraints(content, gc, 0, 3, 1, 1, HORIZONTAL, new JLabel("skew"));
        this.addWithConstraints(content, gc, 1, 3, 1, 1, HORIZONTAL, getFieldComponent("skewX"));
        this.addWithConstraints(content, gc, 2, 3, 1, 1, HORIZONTAL, getFieldComponent("skewY"));

        this.addWithConstraints(content, gc, 0, 4, 1, 1, HORIZONTAL, new JLabel("relative"));
        this.addWithConstraints(content, gc, 1, 4, 2, 1, HORIZONTAL, getFieldComponent("relative"));
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

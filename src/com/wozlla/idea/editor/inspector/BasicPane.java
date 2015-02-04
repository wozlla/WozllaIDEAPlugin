package com.wozlla.idea.editor.inspector;

import com.wozlla.idea.editor.inspector.fields.BooleanField;
import com.wozlla.idea.editor.inspector.fields.Field;
import com.wozlla.idea.editor.inspector.fields.IntField;
import com.wozlla.idea.editor.inspector.fields.StringField;
import com.wozlla.idea.scene.GameObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static java.awt.GridBagConstraints.*;

public class BasicPane extends CommonPane {

    private GameObject gameObject;
    private Map<String, Object> fieldMap = new HashMap<String, Object>();

    public BasicPane(GameObject gameObject) {
        super("Basic", new JPanel(), false);
        this.gameObject = gameObject;

        fieldMap.put("name", new StringField(gameObject, "name"));
        fieldMap.put("id", new StringField(gameObject, "id"));
        fieldMap.put("z", new IntField(gameObject, "z"));
        fieldMap.put("active", new BooleanField(gameObject, "active"));
        fieldMap.put("visible", new BooleanField(gameObject, "visible"));
        fieldMap.put("touchable", new BooleanField(gameObject, "touchable"));

        JPanel content = (JPanel)this.getContent();
        content.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        this.addWithConstraints(content, gc, 0, 0, 1, 1, HORIZONTAL, new JLabel("name"), 0.3);
        this.addWithConstraints(content, gc, 1, 0, 2, 1, HORIZONTAL, getFieldComponent("name"), 0.7);

        this.addWithConstraints(content, gc, 0, 1, 1, 1, HORIZONTAL, new JLabel("id"), 0.3);
        this.addWithConstraints(content, gc, 1, 1, 2, 1, HORIZONTAL, getFieldComponent("id"));

        this.addWithConstraints(content, gc, 0, 2, 1, 1, HORIZONTAL, new JLabel("z-order"), 0.3);
        this.addWithConstraints(content, gc, 1, 2, 2, 1, HORIZONTAL, getFieldComponent("z"));

        this.addWithConstraints(content, gc, 0, 3, 1, 1, HORIZONTAL, new JLabel("active"), 0.3);
        this.addWithConstraints(content, gc, 1, 3, 2, 1, HORIZONTAL, getFieldComponent("active"));

        this.addWithConstraints(content, gc, 0, 4, 1, 1, HORIZONTAL, new JLabel("visible"), 0.3);
        this.addWithConstraints(content, gc, 1, 4, 2, 1, HORIZONTAL, getFieldComponent("visible"));

        this.addWithConstraints(content, gc, 0, 5, 1, 1, HORIZONTAL, new JLabel("touchable"), 0.3);
        this.addWithConstraints(content, gc, 1, 5, 2, 1, HORIZONTAL, getFieldComponent("touchable"));

    }

    public void destroyFields() {
        JComponent content = this.getContent();
        for(java.awt.Component comp : content.getComponents()) {
            if(comp instanceof Field) {
                ((Field)comp).destroy();
            }
        }
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

    public void addWithConstraints(JComponent container, GridBagConstraints gc, int x, int y,
                                   int spanX, int spanY, int fill, JComponent comp, double weightx) {
        gc.weightx = weightx;
        this.addWithConstraints(container, gc, x, y, spanX, spanY, fill, comp);
    }

    protected JComponent getFieldComponent(String name) {
        Object field = fieldMap.get(name);
        if(field instanceof Field) {
            return ((Field) field).getComponent();
        }
        return (JComponent)field;
    }


}

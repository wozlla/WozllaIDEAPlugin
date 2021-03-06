package com.wozlla.idea.editor.inspector.fields;

import com.wozlla.idea.scene.PropertyObject;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class StringField extends PropertyBindField<String, JTextComponent> implements Field.GridBagLayoutAware {

    public StringField(final JTextComponent component, PropertyObject target, String propertyName) {
        super(component, target, propertyName);
        this.initFieldValues();
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    updateTargetPropertyValue();
                }
            }
        });
        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                updateTargetPropertyValue();
            }
        });
    }

    public StringField(PropertyObject target, String propertyName) {
        this(new JTextField(), target, propertyName);
    }

    @Override
    public void setValue(String value) {
        (this.getComponent()).setText(value);
    }

    @Override
    public String getValue() {
        return (this.getComponent()).getText();
    }

    @Override
    public LayoutParams getLayoutParams() {
        return new LayoutParams();
    }
}

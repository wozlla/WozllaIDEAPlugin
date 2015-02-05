package com.wozlla.idea.editor.inspector.fields;

import com.wozlla.idea.scene.PropertyObject;

import javax.swing.*;

public class SpriteOffsetField extends PointField {

    public SpriteOffsetField(PropertyObject target, String propertyName) {
        super(target, propertyName);
        this.xSpinner.setModel(new SpinnerNumberModel(0, 0.0, 1.0, 0.01));
        this.setValue(getTargetPropertyValue());
    }

}

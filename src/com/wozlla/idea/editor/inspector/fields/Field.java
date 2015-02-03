package com.wozlla.idea.editor.inspector.fields;

import javax.swing.*;

public interface Field<T extends Object, C extends JComponent> {

    String getName();

    C getComponent();

    void setValue(T value);
    T getValue();

    void destroy();



}

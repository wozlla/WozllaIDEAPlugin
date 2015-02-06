package com.wozlla.idea.editor.inspector.fields;

import com.intellij.openapi.project.Project;
import com.wozlla.idea.editor.inspector.ComponentPane;
import com.wozlla.idea.scene.ComponentConfig;

import javax.swing.*;
import java.awt.*;

public interface Field<T extends Object, C extends JComponent> {

    String getName();

    C getComponent();

    void setValue(T value);
    T getValue();

    void destroy();



    public static interface WithoutLabel {

    }

    public static interface CustomeLayout {
        void doLayoutField(GridBagConstraints gc, JPanel content);
    }

    interface GridBagLayoutAware {

        LayoutParams getLayoutParams();

        public static class LayoutParams {
            public int gridX = 1;
            public int spanX = 2;
            public int spanY = 1;
            public int fill = GridBagConstraints.HORIZONTAL;
            public double weightx = 0.75;

        }

    }

    interface LabelAware {
        void setLabel(JLabel label);
    }

    interface ProjectAware {
        void setProject(Project project);
    }

    interface PropertyConfigAware {

        void setPropertyConfig(ComponentConfig.PropertyConfig config);

    }

    interface ComponentPaneAware {

        void setComponentPane(ComponentPane pane);

    }
}

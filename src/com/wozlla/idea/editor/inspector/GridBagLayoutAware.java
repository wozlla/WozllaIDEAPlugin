package com.wozlla.idea.editor.inspector;


import java.awt.*;

public interface GridBagLayoutAware {

    LayoutParams getLayoutParams();

    public static class LayoutParams {

        public int spanX = 2;
        public int spanY = 1;
        public int fill = GridBagConstraints.HORIZONTAL;
        public double weightx = 0.5;

    }

}

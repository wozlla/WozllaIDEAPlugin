package com.wozlla.idea.utils;

import com.intellij.ide.dnd.DnDEvent;
import com.intellij.ide.dnd.DnDTarget;

import java.awt.*;

public class DropAdapter implements DnDTarget {
    @Override
    public void cleanUpOnLeave() {

    }

    @Override
    public void updateDraggedImage(Image image, Point dropPoint, Point imageOffset) {

    }

    @Override
    public void drop(DnDEvent event) {

    }

    @Override
    public boolean update(DnDEvent event) {
        return false;
    }
}

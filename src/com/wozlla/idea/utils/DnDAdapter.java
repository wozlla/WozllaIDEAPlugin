package com.wozlla.idea.utils;

import com.intellij.ide.dnd.*;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class DnDAdapter implements DnDSource, DnDTarget {

    @Override
    public boolean canStartDragging(DnDAction action, Point dragOrigin) {
        return false;
    }

    @Override
    public DnDDragStartBean startDragging(DnDAction action, Point dragOrigin) {
        return null;
    }

    @Nullable
    @Override
    public Pair<Image, Point> createDraggedImage(DnDAction action, Point dragOrigin) {
        return null;
    }

    @Override
    public void dragDropEnd() {

    }

    @Override
    public void dropActionChanged(int gestureModifiers) {

    }

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

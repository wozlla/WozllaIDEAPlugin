package com.wozlla.idea.scene;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Transform extends PropertyObject {

    public static final int ORIGIN = 1;
    public static final int RECT = 2;

    public final GameObject gameObject;
    public final int type;

    public Transform(GameObject gameObject, JSONObject source, int type, SceneChangeListener sceneChangeListener) throws JSONException {
        super(source, sceneChangeListener);
        this.gameObject = gameObject;
        this.type = type;

        if(type == RECT) {
            this.ensureProperty("anchorMode", "Left_Top");
            this.ensureProperty("px", 0);
            this.ensureProperty("py", 0);
            this.ensureProperty("left", 0);
            this.ensureProperty("top", 0);
            this.ensureProperty("right", 0);
            this.ensureProperty("bottom", 0);
            this.ensureProperty("width", 0);
            this.ensureProperty("height", 0);
        } else {
            this.ensureProperty("x", 0);
            this.ensureProperty("y", 0);
            this.ensureProperty("scaleX", 1);
            this.ensureProperty("scaleY", 1);
            this.ensureProperty("rotation", 0);
            this.ensureProperty("skewX", 0);
            this.ensureProperty("skewY", 0);
            this.ensureProperty("relative", false);
        }
    }

    public void deltaX(double deltaX) {
        String property = type == RECT ? "px" : "x";
        try {
            double x = this.source.getDouble(property);
            this.setProperty(property, x + deltaX);
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void deltaY(double deltaY) {
        String property = type == RECT ? "py" : "y";
        try {
            double y = this.source.getDouble(property);
            this.setProperty(property, y + deltaY);
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

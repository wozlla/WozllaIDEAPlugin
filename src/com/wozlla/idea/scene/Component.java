package com.wozlla.idea.scene;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.UUID;

public class Component extends PropertyObject {

    public static Component create(GameObject rootGameObject, String name) {
        JSONObject source = new JSONObject();
        try {
            source.put("uuid", UUID.randomUUID().toString());
            source.put("name", name);
            source.put("properties", new JSONObject());
            return new Component(source, rootGameObject.getSceneChangeListener());
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }

    protected GameObject gameObject;
    protected JSONObject componentSource;

    public Component(JSONObject source, SceneChangeListener listener) throws JSONException {
        super(source.getJSONObject("properties"), listener);
        this.componentSource = source;
        checkProperty(source, "name");
    }

    public String getUUID() {
        try {
            return this.componentSource.getString("uuid");
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        try {
            return this.componentSource.getString("name");
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

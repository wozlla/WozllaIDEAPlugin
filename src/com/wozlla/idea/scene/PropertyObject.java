package com.wozlla.idea.scene;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PropertyObject {

    public static void checkProperty(JSONObject obj, String name) throws JSONException {
        if(!obj.has(name)) {
            throw new JSONException("property '" + name + "' not exists");
        }
    }

    public static void ensureProperty(JSONObject obj, String name, Object defaultValue) throws JSONException {
        if(!obj.has(name)) {
            obj.put(name, defaultValue);
        }
    }

    protected final JSONObject source;

    private final List<ChangeListener> listenerList = new ArrayList<ChangeListener>();

    private SceneChangeListener sceneChangeListener;

    public PropertyObject(JSONObject source, SceneChangeListener sceneChangeListener) {
        this.source = source;
        this.sceneChangeListener = sceneChangeListener;
    }

    public boolean hasProperty(String key) {
        return this.source.has(key);
    }

    public Object getProperty(String key) {
        try {
            return this.get(key);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setProperty(String key, Object value) {
        this.setProperty(key, value, false);
    }

    public void setProperty(String key, Object value, boolean silent) {
        Object oldValue = this.hasProperty(key) ? this.getProperty(key) : null;
        if(oldValue == null && value == null) {
            return;
        }
        else if(oldValue == value || (oldValue != null && oldValue.equals(value))) {
            return;
        }
        try {
            this.set(key, value);
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
        if(!silent) {
            this.dispatchChange(key, value, oldValue);
            this.sceneChangeListener.onScenePropertyChange(this, key, value, oldValue);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        listenerList.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(listener);
    }

    public SceneChangeListener getSceneChangeListener() {
        return sceneChangeListener;
    }

    public void dispatchChange(String key, Object newValue, Object oldValue) {
        for(ChangeListener l : listenerList) {
            l.onChange(key, newValue, oldValue);
        }
    }

    public void checkProperty(String name) throws JSONException {
        if(!this.source.has(name)) {
            throw new JSONException("property '" + name + "' not exists");
        }
    }

    public void ensureProperty(String name, Object defaultValue) throws JSONException {

        if(!this.source.has(name)) {
            this.set(name, defaultValue);
        }
    }

    public String toJSONString () {
        return this.source.toString();
    }

    protected final void set(String key, Object value) throws JSONException {
        this.source.put(key, value);
    }

    protected final Object get(String key) throws JSONException {
        return this.source.opt(key);
    }

    public static interface ChangeListener {

        void onChange(String key, Object newValue, Object oldValue);

    }

}

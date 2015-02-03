package com.wozlla.idea.scene;


import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameObject extends PropertyObject {

    public static interface HierarchyChangeListener {
        void onAddChild(GameObject child);
        void onRemoveChild(GameObject child);
        void onAddComponent(Component component);
        void onRemoveComponent(Component component);
    }

    public static GameObject create(GameObject root, int transformType) {
        try {
            JSONObject source = new JSONObject();
            source.put("uuid", UUID.randomUUID().toString());
            source.put("name", "GameObject");
            source.put("rect", transformType == Transform.RECT);
            source.put("transform", new JSONObject());
            return new GameObject(source, root.getSceneChangeListener());
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GameObject deepClone(GameObject target) {
        try {
            JSONObject source = new JSONObject(target.toJSONString());
            source.put("uuid", UUID.randomUUID().toString());
            return new GameObject(source, target.getSceneChangeListener());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final String uuid;
    private Transform transform;
    private GameObject parent;

    private List<GameObject> children = new ArrayList<GameObject>();
    private List<Component> components = new ArrayList<Component>();

    private List<HierarchyChangeListener> listenerList = new ArrayList<HierarchyChangeListener>();

    public GameObject(JSONObject source, SceneChangeListener listener) throws JSONException {
        super(source, listener);

        this.uuid = source.getString("uuid");
        this.checkProperty("name");
        this.ensureProperty("rect", false);
        this.ensureProperty("id", "");
        this.ensureProperty("z", 0);
        this.ensureProperty("active", true);
        this.ensureProperty("visible", true);
        this.ensureProperty("touchable", false);
        this.ensureProperty("children", new JSONArray());
        this.ensureProperty("components", new JSONArray());

        this.transform = new Transform(this, this.source.getJSONObject("transform"),
                this.source.getBoolean("rect") ? Transform.RECT : Transform.ORIGIN, listener);

        JSONArray children = source.getJSONArray("children");
        for (int i = 0, len = children.length(); i < len; i++) {
            GameObject child = new GameObject(children.getJSONObject(i), listener);
            child.parent = this;
            this.children.add(child);
        }
        JSONArray components = source.getJSONArray("components");
        for (int i = 0, len = components.length(); i < len; i++) {
            Component component = new Component(components.getJSONObject(i), listener);
            component.gameObject = this;
            this.components.add(component);
        }
    }

    public String getUUID() {
        return this.uuid;
    }

    public Transform getTransform() {
        return transform;
    }

    public GameObject getParent() {
        return parent;
    }

    public List<Component> getComponents() {
        return components;
    }

    public List<GameObject> getChildren() {
        return children;
    }

    public void addGameObject(GameObject child) {
        try {
            this.source.getJSONArray("children").put(child.source);
            child.parent = this;
            for(HierarchyChangeListener l : listenerList) {
                l.onAddChild(child);
            }
            getSceneChangeListener().onAddGameObject(this, child);
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeGameObject(GameObject child) {
        if(this.children.remove(child)) {
            try {
                child.parent = null;
                this.source.getJSONArray("children").remove(child.source);
                for(HierarchyChangeListener l : listenerList) {
                    l.onRemoveChild(child);
                }
                getSceneChangeListener().onRemoveGameObject(this, child);
            } catch(JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addComponent(Component comp) {
        try {
            this.source.getJSONArray("components").put(comp.componentSource);
            comp.gameObject = this;
            this.components.add(comp);
            for(HierarchyChangeListener l : listenerList) {
                l.onAddComponent(comp);
            }
            getSceneChangeListener().onAddComponent(this, comp);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeComponent(Component comp) {
        if(this.components.remove(comp)) {
            try {
                this.source.getJSONArray("components").remove(comp.componentSource);
                comp.gameObject = null;
                for(HierarchyChangeListener l : listenerList) {
                    l.onRemoveComponent(comp);
                }
                getSceneChangeListener().onRemoveComponent(this, comp);
            } catch(JSONException e) {}
        }
    }

    public String toString() {
        try {
            return this.get("name").toString();
        } catch(Exception e) {
            return e.toString();
        }
    }

}

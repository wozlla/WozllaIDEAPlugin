package com.wozlla.idea.scene;

public interface SceneChangeListener {

    void onScenePropertyChange(PropertyObject source, String name, Object newValue, Object oldValue);
    void onAddGameObject(GameObject parent, GameObject child);
    void onRemoveGameObject(GameObject parent, GameObject child);
    void onAddComponent(GameObject gameObj, Component component);
    void onRemoveComponent(GameObject gameObj, Component component);

}

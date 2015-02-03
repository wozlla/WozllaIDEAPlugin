package com.wozlla.idea.internal;

import com.intellij.openapi.components.ProjectComponent;
import com.wozlla.idea.IComponentConfigManager;
import com.wozlla.idea.scene.ComponentConfig;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ComponentConfigManager implements IComponentConfigManager, ProjectComponent {

    private List<IConfigUpdatedListener> listeners = new ArrayList<IConfigUpdatedListener>();

    private Map<String, ComponentConfig> configMap = new HashMap<String, ComponentConfig>();

    public ComponentConfig getComponentConfig(String name) {
        return configMap.get(name);
    }

    public Collection<ComponentConfig> getComponentConfigAsList() {
        return configMap.values();
    }

    @Override
    public void addConfigUpdatedListener(IConfigUpdatedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConfigUpdatedListener(IConfigUpdatedListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void updateConfig(String configPlainText) {

    }

    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return ComponentConfigManager.class.getName();
    }
}

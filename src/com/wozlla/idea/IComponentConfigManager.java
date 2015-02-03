package com.wozlla.idea;

import com.wozlla.idea.scene.ComponentConfig;

import java.util.Collection;

public interface IComponentConfigManager {

    void addConfigUpdatedListener(IConfigUpdatedListener listener);

    void removeConfigUpdatedListener(IConfigUpdatedListener listener);

    void updateConfig(String configPlainText);

    ComponentConfig getComponentConfig(String name);

    Collection<ComponentConfig> getComponentConfigAsList();

    public static interface IConfigUpdatedListener {

        void onUpdated();

    }

}

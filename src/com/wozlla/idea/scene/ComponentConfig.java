package com.wozlla.idea.scene;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentConfig {

    public final String name;
    public final Map<String, PropertyConfig> propertyConfigMap;
    public final List<String> properties;

    public ComponentConfig(JSONObject configJSON) {
        try {
            this.name = configJSON.getString("name");
            this.propertyConfigMap = new HashMap<String, PropertyConfig>();
            this.properties = new ArrayList<String>();
            List<JSONObject> propConfigArray = new ArrayList<JSONObject>();
            this.visitProperties(propConfigArray, configJSON.getJSONArray("properties"));
            for(JSONObject cfg : propConfigArray) {
                String name = cfg.getString("name");
                String type = cfg.getString("type");
                Object defaultValue = cfg.has("defaultValue") ? cfg.get("defaultValue") : null;
                String editor = cfg.has("editor") ? cfg.getString("editor") : null;
                Object data = cfg.has("data") ? cfg.get("data") : null;
                this.propertyConfigMap.put(name, new PropertyConfig(name, type, defaultValue, editor, data));
                this.properties.add(name);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    protected void visitProperties(List<JSONObject> propConfigArray, JSONArray properties) throws JSONException {
        for(int i=0,len=properties.length(); i<len; i++) {
            JSONObject cfg = properties.getJSONObject(i);
            if (cfg.has("group")) {
                visitProperties(propConfigArray, cfg.getJSONArray("properties"));
            } else {
                propConfigArray.add(cfg);
            }
        }
    }

    public class PropertyConfig {
        public final String name;
        public final String type;
        public final Object defaultValue;
        public final String editor;
        public final Object data;

        public PropertyConfig(String name, String type, Object defaultValue, String editor, Object data) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.editor = editor;
            this.data = data;
        }

        public String getEditorType() {
            return editor == null ? type : editor;
        }
    }
}

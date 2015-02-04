package com.wozlla.idea.editor.inspector;

import com.wozlla.idea.editor.inspector.fields.*;
import com.wozlla.idea.scene.PropertyObject;

import java.util.HashMap;
import java.util.Map;

public class FieldFactory {

    private static Map<String, Class<? extends Field>> fieldMap = new HashMap<String, Class<? extends Field>>();

    static {
        fieldMap.put("boolean", BooleanField.class);
        fieldMap.put("int", IntField.class);
        fieldMap.put("string", StringField.class);
        fieldMap.put("number", NumberField.class);
        fieldMap.put("combobox", ComboboxField.class);
        fieldMap.put("padding", PaddingField.class);
        fieldMap.put("margin", MarginField.class);
        fieldMap.put("rect", RectField.class);
        fieldMap.put("spriteAtlas".toLowerCase(), SpriteAtlasField.class);
        fieldMap.put("spriteFrame".toLowerCase(), SpriteFrameField.class);
    }

    public static Field create(String type, PropertyObject target, String propertyName) {
        Class<? extends Field> fieldClass = fieldMap.get(type);
        if(fieldClass == null) {
            fieldClass = fieldMap.get(type.toLowerCase());
        }
        try {
            return fieldClass.getConstructor(PropertyObject.class, String.class).newInstance(target, propertyName);
        } catch(Exception e) {
            throw new RuntimeException("Fail to create field: " + type, e);
        }
    }

    public static Field create(String type, PropertyObject target, String propertyName, String[] data) {
        if("combobox".equalsIgnoreCase(type)) {
            return new ComboboxField<String>(target, propertyName, data);
        }
        return null;
    }

}

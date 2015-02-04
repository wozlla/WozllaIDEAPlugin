package com.wozlla.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;

public class Utils {

    public static JSONObject virtualFile2JSONObject(VirtualFile file) throws IOException, JSONException {
        return new JSONObject(new String(file.contentsToByteArray()));
    }

    public static JSONArray virtualFile2JSONArray(VirtualFile file) throws IOException, JSONException {
        return new JSONArray(new String(file.contentsToByteArray()));
    }

    public static String getProjectPath(Project project, VirtualFile file) {
        return file.getPath().replace(project.getBaseDir().getPath() + "/", "");
    }
}

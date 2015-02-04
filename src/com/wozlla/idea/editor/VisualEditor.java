package com.wozlla.idea.editor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.events.*;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.wozlla.idea.IComponentConfigManager;
import com.wozlla.idea.WozllaIDEAPlugin;
import com.wozlla.idea.scene.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.sanselan.util.IOUtils;
import org.codehaus.jettison.json.JSONException;

import javax.swing.*;
import java.awt.*;
import java.awt.Component;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class VisualEditor extends JPanel {

    private final Project project;
    private final GameObject rootGameObject;
    private final Browser browser;
    private final BrowserView browserView;
    private final JToolBar toolBar;
    private JavascriptBridge bridge;
    private LoadCallback loadCallback;

    public VisualEditor(Project project, GameObject rootGameObject) {
        super(new BorderLayout(0, 0));
        this.project = project;
        this.rootGameObject = rootGameObject;
        this.browser = new Browser();
        this.browserView = new BrowserView(browser);
        this.browserView.setBackground(this.getBackground());

        this.toolBar = new JToolBar(JToolBar.HORIZONTAL);
        this.toolBar.setPreferredSize(new Dimension(0, 34));

        this.add(this.toolBar, BorderLayout.NORTH);
        this.add(this.browserView, BorderLayout.CENTER);

        this.browser.getPreferences().setAllowDisplayingInsecureContent(true);
        this.browser.getPreferences().setAllowRunningInsecureContent(true);
        this.initBridge();
        this.listenConsole();

        LoggerProvider.getBrowserLogger().setLevel(Level.WARNING);
    }

    public void load(final LoadCallback callback) {
        InputStream htmlStrem = VisualEditor.class.getResourceAsStream("/com/wozlla/idea/editor/Editor.html");
        InputStream runtimeJSStream = VisualEditor.class.getResourceAsStream("/com/wozlla/idea/editor/runtime.js");
        InputStream editorJSStrem = VisualEditor.class.getResourceAsStream("/com/wozlla/idea/editor/Editor.js");
        String html;
        try {
            html = new String(IOUtils.getInputStreamBytes(htmlStrem));
            String js = new String(IOUtils.getInputStreamBytes(runtimeJSStream));
            html = html.replace("<script src=\"runtime.js\"></script>", "<script type='text/javascript'>\n" + js + "\n</script>");
            js = new String(IOUtils.getInputStreamBytes(editorJSStrem));
            html = html.replace("<script src=\"Editor.js\"></script>", "<script type='text/javascript'>\n" + js + "\n</script>");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        String encoding = "UTF-8";
        String baseURL = "file://" + project.getBaseDir().getPath() + "/";
        LoadHTMLParams params = new LoadHTMLParams(html, encoding, baseURL);
        this.browser.loadHTML(params);
        this.loadCallback = callback;
    }

    public void onGameObjectSelectionChange(GameObject[] objArray) {
        this.bridge.onGameObjectSelectionChange(objArray);
    }

    public void onScenePropertyChange(PropertyObject source, String name, Object newValue, Object oldValue) {
        this.bridge.onScenePropertyChange(source, name, newValue, oldValue);
    }

    public void onAddGameObject(GameObject parent, GameObject child) {
        this.bridge.onAddGameObject(parent, child);
    }

    public void onRemoveGameObject(GameObject parent, GameObject child) {
        this.bridge.onRemoveGameObject(parent, child);
    }

    public void onAddComponent(GameObject gameObj, com.wozlla.idea.scene.Component component) {
        this.bridge.onAddComponent(gameObj, component);
    }

    public void onRemoveComponent(GameObject gameObj, com.wozlla.idea.scene.Component component) {
        this.bridge.onRemoveComponent(gameObj, component);
    }

    public void onInsertBeforeGameObject(GameObject beInserted, GameObject relatived) {
        this.bridge.onInsertBeforeGameObject(beInserted, relatived);
    }

    public void onInsertAfterGameObject(GameObject beInserted, GameObject relatived) {
        this.bridge.onInsertAfterGameObject(beInserted, relatived);
    }

    protected void initBridge() {
        this.bridge = new JavascriptBridge();
        this.browser.registerFunction("bridgeInvoke", this.bridge);
    }

    protected void listenConsole() {
        this.browser.addConsoleListener(new ConsoleListener() {
            @Override
            public void onMessage(ConsoleEvent consoleEvent) {
                String path = consoleEvent.getSource();
                path = path.replace("file://" + project.getBaseDir().getPath(), "");
                System.out.println("console.log " + path + ':' + consoleEvent.getLineNumber() + " " + consoleEvent.getMessage());
            }
        });
    }

    protected static interface LoadCallback {
        public void onload();
    }

    private class JavascriptBridge implements BrowserFunction {

        @Override
        public JSValue invoke(JSValue... jsValues) {
            String funcName = jsValues[0].getString();
            System.out.println("bridgeInvoke [" + funcName + "]");
            if("editorReady".equals(funcName)) {
                onEditorReady();
            }
            else if("getOpenedJSONXData".equals(funcName)) {
                return JSValue.create(rootGameObject.toJSONString());
            }
            else if("isExistInProject".equals(funcName)) {
                String path = jsValues[1].getString();
                if("__internal_coords_image.png".equals(path)) {
                    return JSValue.create(true);
                }
                return JSValue.create(null != project.getBaseDir().findFileByRelativePath(path));
            }
            else if("readProjectFile".equals(funcName)) {
                String path = jsValues[1].getString();
                VirtualFile file = project.getBaseDir().findFileByRelativePath(path);
                if(file == null) {
                    throw new RuntimeException(new FileNotFoundException(path));
                }
                try {
                    return JSValue.create(new String(file.contentsToByteArray()));
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if("readProjectFileAsBase64".equals(funcName)) {
                String path = jsValues[1].getString();
                if("__internal_coords_image.png".equals(path)) {
                    return JSValue.create(WozllaIDEAPlugin.COORDS_IMAGE_BASE64);
                }
                VirtualFile file = project.getBaseDir().findFileByRelativePath(path);
                if(file == null) {
                    throw new RuntimeException(new FileNotFoundException(path));
                }
                try {
                    return JSValue.create(Base64.encodeBase64String(file.contentsToByteArray()));
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if("throw".equals(funcName)) {
                throw new RuntimeException(jsValues[1].toString());
            }
            else if("updateComponentConfig".equals(funcName)) {
                project.getComponent(IComponentConfigManager.class).updateConfig(jsValues[1].getString());
            }
            return null;
        }

        public void onEditorReady() {
            loadCallback.onload();
            loadCallback = null;
        }

        public void onGameObjectSelectionChange(GameObject[] objArray) {
            if(objArray != null && objArray.length == 1) {
                execute("bridge.onGameObjectSelectionChange('" + objArray[0].getUUID() + "');");
            } else {
                execute("bridge.onGameObjectSelectionChange();");
            }
        }

        public void onScenePropertyChange(PropertyObject source, String name, Object newValue, Object oldValue) {
            if(oldValue == null) {
                oldValue = "";
            }
            if (source instanceof GameObject) {
                String uuid = ((GameObject) source).getUUID();
                execute("bridge.onGameObjectPropertyChange(" +
                        "'" + uuid + "'," +
                        "'" + name + "'," +
                        "'" + newValue.toString() + "'," +
                        "'" + oldValue.toString() + "'" +
                        ")");
            } else if (source instanceof Transform) {
                String uuid = ((Transform)source).gameObject.getUUID();
                execute("bridge.onTransformPropertyChange(" +
                        "'" + uuid + "'," +
                        "'" + name + "'," +
                        "'" + newValue.toString() + "'," +
                        "'" + oldValue.toString() + "'" +
                        ")");
            } else if (source instanceof com.wozlla.idea.scene.Component) {
                String uuid = ((com.wozlla.idea.scene.Component) source).getUUID();
                execute("bridge.onComponentPropertyChange(" +
                        "'" + uuid + "'," +
                        "'" + name + "'," +
                        "'" + newValue.toString() + "'," +
                        "'" + oldValue.toString() + "'" +
                        ")");
            }
        }

        public void onAddGameObject(GameObject parent, GameObject child) {
            this.executeOnHierachyChange();
        }

        public void onRemoveGameObject(GameObject parent, GameObject child) {
            this.executeOnHierachyChange();
        }

        public void onAddComponent(GameObject gameObj, com.wozlla.idea.scene.Component component) {
            this.executeOnHierachyChange();
        }

        public void onRemoveComponent(GameObject gameObj, com.wozlla.idea.scene.Component component) {
            this.executeOnHierachyChange();
        }

        public void onInsertBeforeGameObject(GameObject beInserted, GameObject relatived) {
            this.executeOnHierachyChange();
        }

        public void onInsertAfterGameObject(GameObject beInserted, GameObject relatived) {
            this.executeOnHierachyChange();
        }

        private void executeOnHierachyChange() {
            execute("bridge.onHierarchyChange();");
        }

        private void execute(String code) {
            browser.executeJavaScript(code);
        }

    }
}

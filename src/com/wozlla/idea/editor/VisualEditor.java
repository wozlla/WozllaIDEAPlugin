package com.wozlla.idea.editor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.events.*;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.wozlla.idea.scene.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.sanselan.util.IOUtils;

import javax.swing.*;
import java.awt.*;
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

    }

    public void onScenePropertyChange(PropertyObject source, String name, Object newValue, Object oldValue) {

    }

    public void onAddGameObject(GameObject parent, GameObject child) {
    }

    public void onRemoveGameObject(GameObject parent, GameObject child) {
    }

    public void onAddComponent(GameObject gameObj, com.wozlla.idea.scene.Component component) {
    }

    public void onRemoveComponent(GameObject gameObj, com.wozlla.idea.scene.Component component) {
    }

    public void onEditorReady() {
        this.loadCallback.onload();
    }

    protected void initBridge() {
        this.browser.registerFunction("bridgeInvoke", new BrowserFunction() {
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
                        return JSValue.create("iVBORw0KGgoAAAANSUhEUgAAAKoAAACqCAMAAAAKqCSwAAAAgVBMVEUAAACtaGgAAAAAA" +
                                "AAAAAClY2MAAAADAAAEAAC4bm4AAAACAAADAAACAAADAAB/MQADAAADAAAKCwkDAAABenYCVlQGAQBaIgACQkBSHwCnY2MA//b/ZgC" +
                                "aXFyRi4nHbVgA1s7oXAAAtrAA7ubOUQCMVFQAmZSaPQDHjIy5ZVC2SAAdWcZyAAAAG3RSTlMA5CQxC9cExUDUGXxIaYz4s1baoPPa+" +
                                "sfz+MQ1to2JAAACyklEQVR42u3cyXLbMAyAYZSkKZeLVi9xyTSSrNiJ3/8BC9mOl5l2egwwg/9iH7+BRFInws/nFkApq4I2cI021cV" +
                                "VGZzlQFV+XNVRGQZUF9dpbMugzI3649ZiSakQizHtV7VHrIWL9PWr94JU7eYlpdSvirJxF+rrx8fva4lk+8M68KO+vVF+AfaPLwBSS" +
                                "S4r3/Yz9LKs7lQWmxUpqnVaK6e0dnY+Ag5F1AglSXWxq33j6y6688HaKAtEqars00v3kvpSgdFBGwtUqc6vU7/p09o7AGMM3FrMvX9" +
                                "+vi/OfT/V6m5MfRo7bWHun9Rf8O252O7Tvo0O/tKyyLlYApFMKMY01sGQp1rlVymltVeWOtU0BW4B+75uDHGq1eUhHYpVOpTa0qaaJ" +
                                "e5Vta9xt1oa2lSr54Nq2XS1pz5VMPPxb5zWivq7+hD5zUqo/02owCGhYkIFDgkVEypwSKiYUIFDQsWEChwSKiZU4JBQMaECh4SKCRU" +
                                "4JFRMqMAhoWJCBQ4JFRMqcEiomFCBQ0LFhAocEiomVOCQUDGhAoeEigkVOCRUTKjAIaFiQgUOCRUTKnBIqJhQgUNCxYQKHBIqJlTgk" +
                                "FAxoQKHhIoJFTgkVEyowCGhYkIFDgkVEypwSKiYUIFDQsWEChwSKiZU4NCV+nxJI80u1OvVl42+Xn1Jsi+qCds8dbGb8nZJdKz3qXa" +
                                "nXBVVPpbUpwquKYZhMwxFQ3Sod6pVvspDrrwiOtQ7FUyopzzVgepQH6igyipXpQKqPVCdR6p3QDWm1IjUSJNqnXJhpgb8Y8Fo33lNc" +
                                "1Xp6KNvc27xJyq0KrIfAKE67dpNzpt2d9rS3aTmdHnM0y7n3UT4OL1dJD/lcyfCO/85q2Kb54aW6MK/Z3VZZawqyR78t0woBhxqQfz" +
                                "xn1O+wqF6ugf/81h5DBVAlccj4a+px0yz3ZL97n/OuhAc+eX/B3tnjfTmc1XlAAAAAElFTkSuQmCC");
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
                return null;
            }
        });
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
}

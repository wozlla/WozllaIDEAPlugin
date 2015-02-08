package com.wozlla.idea.editor;

import com.intellij.icons.AllIcons;
import com.intellij.ide.browsers.BrowserSelector;
import com.intellij.ide.browsers.BrowserStarter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ListCellRendererWrapper;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.events.*;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.wozlla.idea.IComponentConfigManager;
import com.wozlla.idea.Icons;
import com.wozlla.idea.WozllaIDEAPlugin;
import com.wozlla.idea.scene.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.sanselan.util.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.hyperlink.HyperlinkAction;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
        BrowserPreferences.setChromiumSwitches("--remote-debugging-port=9222", "--disable-web-security", "--allow-file-access-from-files");
        this.project = project;
        this.rootGameObject = rootGameObject;
        this.browser = new Browser();

        this.browserView = new BrowserView(browser);
        this.browserView.setBackground(this.getBackground());
        this.browserView.setAutoscrolls(true);

        this.toolBar = new JToolBar();
        this.toolBar.setPreferredSize(new Dimension(0, 28));
        this.toolBar.setFloatable(false);

        final JSlider scaleSlider = new JSlider(JSlider.HORIZONTAL, 50, 120, 50);
        final JLabel scaleValueLabel = new JLabel("50%");
        scaleValueLabel.setIcon(Icons.ZOOM_ICON);
        scaleSlider.setMaximumSize(new Dimension(80, 24));
        scaleSlider.setPreferredSize(new Dimension(80, 24));
        scaleSlider.setSize(new Dimension(80, 24));
        scaleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                scaleValueLabel.setText(scaleSlider.getValue() + "%");
                bridge.onZoomChange(scaleSlider.getValue());
            }
        });

        ComboBox screenCombobox = new ComboBox(new ScreenSize[] {
                new ScreenSize(960, 640),
                new ScreenSize(640, 960),
                new ScreenSize(1136, 640),
                new ScreenSize(640, 1136)
        });
        screenCombobox.setRenderer(new ListCellRendererWrapper() {
            @Override
            public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                setIcon(Icons.MOBILE_ICON);
            }
        });
        screenCombobox.setSize(new Dimension(100, 24));
        screenCombobox.setMaximumSize(new Dimension(120, 24));
        screenCombobox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    ScreenSize size = (ScreenSize) e.getItem();
                    bridge.onResize(size.width, size.height);
                }
            }
        });

        this.toolBar.add(Box.createHorizontalGlue());
        this.toolBar.addSeparator();
        this.toolBar.add(scaleValueLabel);
        this.toolBar.add(scaleSlider);
        this.toolBar.addSeparator();
        this.toolBar.add(screenCombobox);
        this.toolBar.addSeparator();

        String remoteDebuggingURL = browser.getRemoteDebuggingURL();
        try {
            JXHyperlink link = new JXHyperlink(HyperlinkAction.createHyperlinkAction(new URI(remoteDebuggingURL)));
            link.setText("");
            link.setIcon(Icons.DEBUG_ICON);
            this.toolBar.add(link);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        this.add(this.toolBar, BorderLayout.NORTH);
        this.add(this.browserView, BorderLayout.CENTER);

        this.browser.getPreferences().setAllowDisplayingInsecureContent(true);
        this.browser.getPreferences().setAllowRunningInsecureContent(true);
        this.initBridge();
        this.listenConsole();

        LoggerProvider.getBrowserLogger().setLevel(Level.SEVERE);
        LoggerProvider.getIPCLogger().setLevel(Level.SEVERE);
        LoggerProvider.getChromiumProcessLogger().setLevel(Level.SEVERE);
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

    public void unload() {
        this.browser.dispose();
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

        private GameObject selectedGameObject;

        @Override
        public JSValue invoke(final JSValue... jsValues) {
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
            } else if("moveX".equals(funcName)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(selectedGameObject != null) {
                            selectedGameObject.getTransform().deltaX(jsValues[1].getNumber());
                        }
                    }
                });
            } else if("moveY".equals(funcName)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (selectedGameObject != null) {
                            selectedGameObject.getTransform().deltaY(jsValues[1].getNumber());
                        }
                    }
                });
            } else if("moveXY".equals(funcName)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (selectedGameObject != null) {
                            selectedGameObject.getTransform().deltaX(jsValues[1].getNumber());
                            selectedGameObject.getTransform().deltaY(jsValues[2].getNumber());
                        }
                    }
                });
            }
            return null;
        }

        public void onEditorReady() {
            loadCallback.onload();
            loadCallback = null;
        }

        public void onResize(int width, int height) {
            execute("bridge.onResize(" + width + "," + height + ");");
        }

        public void onZoomChange(double zoom) {
            execute("bridge.onZoomChange(" + zoom + ");");
        }

        public void onGameObjectSelectionChange(GameObject[] objArray) {
            if(objArray != null && objArray.length == 1) {
                execute("bridge.onGameObjectSelectionChange('" + objArray[0].getUUID() + "');");
                selectedGameObject = objArray[0];
            } else {
                selectedGameObject = null;
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

    public static class ScreenSize {

        public int width;
        public int height;

        public ScreenSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public String toString() {
            return width + "x" + height;
        }
    }
}

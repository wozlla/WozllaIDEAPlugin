package com.wozlla.idea.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.wozlla.idea.scene.*;
import com.wozlla.idea.scene.Component;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.util.*;
import java.util.Timer;

public class SceneEditorKit extends JPanel implements SceneChangeListener {

    private Hierarchy hierarchy;
    private Inspector inspector;
    private VisualEditor visualEditor;
    private JComponent content;

    private final Project project;
    private final Document document;
    private final JSONObject rootJSONObject;
    private final GameObject rootGameObject;
    private int updateDocumentActionId = 0;
    private Timer updateDocumentTimer;

    public SceneEditorKit(Project project, Document document, JSONObject rootJSONObject) throws JSONException {
        super(new BorderLayout(0, 0));
        this.project = project;
        this.document = document;
        this.rootJSONObject = rootJSONObject;
        this.rootGameObject = new GameObject(rootJSONObject.getJSONObject("root"), this);

        hierarchy = new Hierarchy(rootGameObject);
        inspector = new Inspector(this.project, rootGameObject);
        JBScrollPane hierarchyContainer = new JBScrollPane(hierarchy);
        JBScrollPane inspectorContainer = new JBScrollPane(inspector);
        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        leftSplitPane.setDividerSize(2);
        leftSplitPane.setBorder(null);
        leftSplitPane.add(hierarchyContainer, JSplitPane.LEFT);
        leftSplitPane.add(inspectorContainer, JSplitPane.RIGHT);

        hierarchyContainer.setPreferredSize(new Dimension(200, 200));
        inspectorContainer.setPreferredSize(new Dimension(200, 200));
        hierarchyContainer.setMinimumSize(new Dimension(200, 200));
        inspectorContainer.setMinimumSize(new Dimension(200, 200));
        inspectorContainer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        visualEditor = new VisualEditor(this.project, rootGameObject);
        visualEditor.setPreferredSize(new Dimension(600, 200));

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        rightSplitPane.setDividerSize(2);
        rightSplitPane.setBorder(null);
        rightSplitPane.add(leftSplitPane, JSplitPane.RIGHT);
        rightSplitPane.add(visualEditor, JSplitPane.LEFT);

        this.content = rightSplitPane;

        this.registerListeners();
        this.initEnviroment();
    }

    public void onClose() {
        this.visualEditor.unload();
    }

    public void onScenePropertyChange(final PropertyObject source, final String name,
                                      final Object newValue, final Object oldValue) {
        visualEditor.onScenePropertyChange(source, name, newValue, oldValue);
        delayUpdateDocument("ScenePropertyChange");
    }

    @Override
    public void onAddGameObject(final GameObject parent, final GameObject child) {
        hierarchy.onAddGameObject(parent, child);
        visualEditor.onAddGameObject(parent, child);
        delayUpdateDocument("AddGameObject");
    }

    @Override
    public void onRemoveGameObject(final GameObject parent, final GameObject child) {
        hierarchy.onRemoveGameObject(parent, child);
        visualEditor.onRemoveGameObject(parent, child);
        delayUpdateDocument("RemoveGameObject");
    }

    @Override
    public void onAddComponent(final GameObject gameObj, final Component component) {
        inspector.onAddComponent(gameObj, component);
        visualEditor.onAddComponent(gameObj, component);
        delayUpdateDocument("AddComponent");
    }

    @Override
    public void onRemoveComponent(final GameObject gameObj, final Component component) {
        inspector.onRemoveComponent(gameObj, component);
        visualEditor.onRemoveComponent(gameObj, component);
        delayUpdateDocument("RemoveComponent");
    }

    @Override
    public void onInsertBeforeGameObject(GameObject beInserted, GameObject relatived) {
        hierarchy.onInsertBeforeGameObject(beInserted, relatived);
        visualEditor.onInsertBeforeGameObject(beInserted, relatived);
        delayUpdateDocument("InsertBeforeGameObject");
    }

    @Override
    public void onInsertAfterGameObject(GameObject beInserted, GameObject relatived) {
        hierarchy.onInsertAfterGameObject(beInserted, relatived);
        visualEditor.onInsertAfterGameObject(beInserted, relatived);
        delayUpdateDocument("InsertAfterGameObject");
    }

    protected void registerListeners() {
        this.hierarchy.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if(hierarchy.getSelectedNode() == null) {
                    inspector.inspect(null);
                    visualEditor.onGameObjectSelectionChange(null);
                } else {
                    GameObject[] objArray = new GameObject[]{hierarchy.getSelectedNode().gameObject};
                    inspector.inspect(objArray);
                    visualEditor.onGameObjectSelectionChange(objArray);
                }
            }
        });
    }

    protected void initEnviroment() {
        final JLabel loadingLabel = new JLabel("Loading Scene ...", JLabel.CENTER);
        this.add(loadingLabel, BorderLayout.CENTER);

        // TODO listen compile

        visualEditor.load(new VisualEditor.LoadCallback() {
            @Override
            public void onload() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        remove(loadingLabel);
                        add(content, BorderLayout.CENTER);
                        revalidate();
                        repaint();
                    }
                });
            }
        });

    }

    protected void delayUpdateDocument(final String action) {
        final int actionId = ++updateDocumentActionId;
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                if (actionId == updateDocumentActionId) {
                    try {
                        System.out.println("save");
                        document.setText(rootJSONObject.toString(2));
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }


}

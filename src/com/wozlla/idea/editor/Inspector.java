package com.wozlla.idea.editor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import com.wozlla.idea.IComponentConfigManager;
import com.wozlla.idea.dialog.ComponentDialog;
import com.wozlla.idea.editor.inspector.*;
import com.wozlla.idea.scene.ComponentConfig;
import com.wozlla.idea.scene.GameObject;
import com.wozlla.idea.scene.Transform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Inspector extends JBPanel {

    private final Project project;
    private final GameObject rootGameObject;
    private Object inspectingObject;
    private JButton addComponentBtn;

    private CommonPane.CloseListener commonPaneCloseListener;

    public Inspector(final Project project, final GameObject rootGameObject) {
        super();
        this.project = project;
        this.rootGameObject = rootGameObject;
        this.addComponentBtn = new JButton("Add Component");
        this.addComponentBtn.setMargin(new Insets(20, 20, 20, 20));
        this.addComponentBtn.setMaximumSize(new Dimension(200, 80));
        this.addComponentBtn.setFocusable(false);
        this.addComponentBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ComponentDialog dialog = new ComponentDialog(project);
                dialog.setSize(600, 700);
                if(dialog.showAndGet()) {
                    ComponentConfig config = dialog.getSelectedComponentConfig();
                    if(config == null || inspectingObject == null) {
                        return;
                    }
                    GameObject inspectingTarget = null;
                    if(inspectingObject instanceof GameObject[]) {
                        GameObject[] arr = ((GameObject[])inspectingObject);
                        if(arr.length == 1) {
                            inspectingTarget = arr[0];
                        }
                    } else if(inspectingObject instanceof GameObject) {
                        inspectingTarget = (GameObject) inspectingObject;
                    }
                    if(inspectingTarget == null) {
                        return;
                    }
                    com.wozlla.idea.scene.Component component = com.wozlla.idea.scene.Component.create(
                            Inspector.this.rootGameObject, config.name);
                    inspectingTarget.addComponent(component);
                }
            }
        });

        this.commonPaneCloseListener = new CommonPane.CloseListener() {
            @Override
            public void onClose(CommonPane pane) {
                if(Messages.OK == Messages.showOkCancelDialog(project, "Delete this component?",
                        "Confirm", Messages.getQuestionIcon())) {
                    if(pane instanceof ComponentPane) {
                        GameObject inspectingTarget = null;
                        if(inspectingObject instanceof GameObject[]) {
                            GameObject[] arr = ((GameObject[])inspectingObject);
                            if(arr.length == 1) {
                                inspectingTarget = arr[0];
                            }
                        } else if(inspectingObject instanceof GameObject) {
                            inspectingTarget = (GameObject) inspectingObject;
                        }
                        if(inspectingTarget == null) {
                            return;
                        }
                        inspectingTarget.removeComponent(((ComponentPane)pane).getInspectingComponent());
                    }
                }
            }
        };

        this.project.getComponent(IComponentConfigManager.class)
                .addConfigUpdatedListener(new IComponentConfigManager.IConfigUpdatedListener() {
            public void onUpdated() {
                inspect(inspectingObject, true);
            }
        });
    }

    public void inspect(Object obj) {
        this.inspect(obj, false);
    }

    public void inspect(Object obj, boolean force)  {
        do {
            if(obj instanceof GameObject) {
                if(!force && obj == this.inspectingObject) {
                    return;
                }
                this.inspect((GameObject)obj);
                break;
            }
            if(obj instanceof GameObject[]) {
                GameObject[] objArray = (GameObject[]) obj;
                if(objArray.length == 1) {
                    this.inspect(objArray[0]);
                    if(!force && obj == this.inspectingObject) {
                        return;
                    }
                    break;
                }
            }
            this.clearInspect();
        } while(false);
        this.inspectingObject = obj;
    }

    public void inspect(GameObject gameObject) {
        this.clearInspect();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        this.setLayout(layout);

        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.weighty = 0;
        this.add(new BasicPane(gameObject), gc);

        gc.gridy ++;

        if(gameObject.getTransform().type == Transform.RECT) {
            this.add(new RectTransformPane(gameObject), gc);
        } else {
            this.add(new TransformPane(gameObject), gc);
        }

        java.util.List<com.wozlla.idea.scene.Component> components = gameObject.getComponents();

        for(com.wozlla.idea.scene.Component component: components) {
            gc.gridy ++;
            ComponentPane pane = new ComponentPane(project, component);
            pane.addCloseListener(commonPaneCloseListener);
            this.add(pane, gc);
        }

        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy++;
        gc.weightx = 0.5;
        this.add(this.addComponentBtn, gc);

        gc.fill = GridBagConstraints.BOTH;
        gc.gridy ++;
        gc.weightx = 1;
        gc.weighty = 1;
        this.add(new JLabel(), gc);

        this.revalidate();
        this.repaint();
    }

    public void clearInspect() {
        for(Component comp : getComponents()) {
            if(comp instanceof ComponentPane) {
                ComponentPane compPane = ((ComponentPane) comp);
                compPane.removeCloseListener(commonPaneCloseListener);
                compPane.destroyFields();
            }
        }
        this.removeAll();
    }

    public void onAddComponent(GameObject gameObject, com.wozlla.idea.scene.Component component) {
        GameObject target = null;
        if(this.inspectingObject instanceof GameObject[]) {
            GameObject[] arr = (GameObject[]) this.inspectingObject;
            if(arr.length == 1) {
                target = arr[0];
            }
        }
        else if(this.inspectingObject instanceof GameObject) {
            target = (GameObject) this.inspectingObject;
        }
        if(target == gameObject) {
            this.inspect(gameObject, true);
        }
    }

    public void onRemoveComponent(GameObject gameObject, com.wozlla.idea.scene.Component component) {
        for(Component jcomp : this.getComponents()) {
            if(jcomp instanceof ComponentPane) {
                if(component == ((ComponentPane) jcomp).getInspectingComponent()) {
                    this.remove(jcomp);
                }
            }
        }
    }
}

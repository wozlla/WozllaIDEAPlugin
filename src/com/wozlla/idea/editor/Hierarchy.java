package com.wozlla.idea.editor;

import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.JBColor;
import com.intellij.ui.treeStructure.Tree;
import com.thaiopensource.xml.dtd.om.Def;
import com.wozlla.idea.Icons;
import com.wozlla.idea.scene.GameObject;
import com.wozlla.idea.scene.PropertyObject;
import com.wozlla.idea.scene.Transform;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

public class Hierarchy extends Tree implements ActionListener {

    public static final String ACTION_ADD_GAMEOBJECT = "Add GameObject";
    public static final String ACTION_ADD_GAMEOBJECT_R = "Add GameObject(RectTransform)";
    public static final String ACTION_ADD_REFERENCE = "Add Reference";
    public static final String ACTION_DELETE = "Delete";
    public static final String ACTION_DUPLICATE = "Dumplicate";

    private GameObject rootGameObject;

    public Hierarchy(GameObject rootGameObject) {
        super();
        this.setModel(new GameObjectTreeModel(new GameObjectNode(rootGameObject)));
        this.rootGameObject = rootGameObject;
        this.setLargeModel(true);
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Color getBackgroundNonSelectionColor() {
                return (null);
            }
            @Override
            public Color getBackgroundSelectionColor() {
                return (null);
            }
        };
        renderer.setLeafIcon(Icons.GAMEOBJECT_ICON);
        renderer.setOpenIcon(Icons.GAMEOBJECT_ICON);
        renderer.setClosedIcon(Icons.GAMEOBJECT_ICON);
        renderer.setDisabledIcon(Icons.GAMEOBJECT_ICON);
        renderer.setBorderSelectionColor(renderer.getBackgroundSelectionColor());
        DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
        selectionModel.setSelectionMode(DefaultTreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

        this.setSelectionModel(selectionModel);
        this.setCellRenderer(renderer);

        JBPopupMenu popupMenu = new JBPopupMenu();
        JMenuItem item;
        popupMenu.add(item = new JMenuItem(ACTION_ADD_GAMEOBJECT));
        item.addActionListener(this);
        popupMenu.add(item = new JMenuItem(ACTION_ADD_GAMEOBJECT_R));
        item.addActionListener(this);
        popupMenu.add(item = new JMenuItem(ACTION_ADD_REFERENCE));
        item.addActionListener(this);
        popupMenu.add(new JPopupMenu.Separator());
        popupMenu.add(item = new JMenuItem(ACTION_DELETE));
        item.addActionListener(this);
        popupMenu.add(new JPopupMenu.Separator());
        popupMenu.add(item = new JMenuItem(ACTION_DUPLICATE));
        item.addActionListener(this);

        this.setComponentPopupMenu(popupMenu);
    }

    public GameObject[] findGameObjectsByPaths(TreePath[] paths) {
        GameObject[] objArray = new GameObject[paths.length];
        for(int i=0; i<objArray.length; i++) {
            Object comp = paths[i].getPathComponent(0);
            objArray[i] = ((GameObjectNode) comp).gameObject;
        }
        return objArray;
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        GameObjectNode node = getSelectedNode();
        GameObject obj;
        if(null == node) {
            obj = this.rootGameObject;
        } else {
            obj = node.gameObject;
        }
        String cmd = e.getActionCommand();
        if(ACTION_ADD_GAMEOBJECT.equals(cmd)) {
            obj.addGameObject(GameObject.create(rootGameObject, Transform.ORIGIN));
        }
        else if(ACTION_ADD_GAMEOBJECT_R.equals(cmd)) {
            obj.addGameObject(GameObject.create(rootGameObject, Transform.RECT));
        }
        else if(ACTION_ADD_REFERENCE.equals(cmd)) {

        }
        else if(ACTION_DELETE.equals(cmd)) {
            if(obj == rootGameObject) {
                return;
            }
            obj.getParent().removeGameObject(obj);
        }
        else if(ACTION_DUPLICATE.equals(cmd)) {
            if(obj == rootGameObject) {
                return;
            }
            GameObject parent = obj.getParent();
            GameObject child = GameObject.deepClone(obj);
            parent.addGameObject(child);
        }
    }

    public GameObjectNode getSelectedNode() {
        return (GameObjectNode)this.getLastSelectedPathComponent();
    }

    public GameObjectNode searchNode(GameObject obj) {
        GameObjectNode node;
        DefaultTreeModel treeModel = (DefaultTreeModel)getModel();
        GameObjectNode rootNode = (GameObjectNode)treeModel.getRoot();
        Enumeration e = rootNode.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            node = (GameObjectNode) e.nextElement();
            if (obj == node.getUserObject()) {
                return node;
            }
        }
        return null;
    }

    public void onAddGameObject(GameObject parent, GameObject child) {
        DefaultTreeModel treeModel = (DefaultTreeModel)this.getModel();
        GameObjectNode node = getSelectedNode();
        GameObjectNode childNode = new GameObjectNode(child);
        if(node == null || node.gameObject != parent) {
            node = searchNode(parent);
        }
        treeModel.insertNodeInto(childNode, node, node.getChildCount());
        expandPath(new TreePath(treeModel.getPathToRoot(node)));
    }

    public void onRemoveGameObject(GameObject parent, GameObject child) {
        DefaultTreeModel treeModel = (DefaultTreeModel) getModel();
        GameObjectNode childNode = getSelectedNode();
        if(childNode == null) {
            childNode = searchNode(child);
        }
        treeModel.removeNodeFromParent(childNode);
        childNode.onRemoveFromParent();
    }

    public class GameObjectNode extends DefaultMutableTreeNode implements PropertyObject.ChangeListener {

        GameObject gameObject;

        public GameObjectNode(GameObject gameObject) {
            super(gameObject);
            this.gameObject = gameObject;
            this.gameObject.addChangeListener(this);
            for(GameObject child : this.gameObject.getChildren()) {
                this.add(new GameObjectNode(child));
            }
        }

        public GameObjectNode getChildNodeByGameObject(GameObject obj) {
            for(int i=0,len=getChildCount(); i<len; i++) {
                GameObjectNode node = (GameObjectNode)getChildAt(i);
                if(node.gameObject == obj) {
                    return node;
                }
            }
            return null;
        }

        @Override
        public void onChange(String key, Object newValue, Object oldValue) {
            if(key.equals("name")) {
                ((GameObjectTreeModel)getModel()).nodeChanged(this);
            }
        }

        public void onRemoveFromParent() {
            this.gameObject.removeChangeListener(this);
        }

    }

    static class GameObjectTreeModel extends DefaultTreeModel {

        public GameObjectTreeModel(TreeNode root) {
            super(root);
        }

    }


}

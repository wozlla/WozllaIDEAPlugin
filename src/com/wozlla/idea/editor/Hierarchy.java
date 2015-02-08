package com.wozlla.idea.editor;

import com.intellij.ide.dnd.*;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativeRectangle;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import com.thaiopensource.xml.dtd.om.Def;
import com.wozlla.idea.Icons;
import com.wozlla.idea.WozllaIDEAPlugin;
import com.wozlla.idea.scene.GameObject;
import com.wozlla.idea.scene.PropertyObject;
import com.wozlla.idea.scene.Transform;
import com.wozlla.idea.utils.DnDAdapter;
import org.apache.batik.apps.svgbrowser.DOMDocumentTree;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class Hierarchy extends Tree implements ActionListener {

    public static final String ACTION_ADD_GAMEOBJECT = "Add GameObject";
    public static final String ACTION_ADD_GAMEOBJECT_R = "Add GameObject(RectTransform)";
    public static final String ACTION_DELETE = "Delete";
    public static final String ACTION_DUPLICATE = "Dumplicate";

    public static final int DROP_ACTION_APPEND = 1;
    public static final int DROP_ACTION_INSERT_BEFORE = 2;
    public static final int DROP_ACTION_INSERT_AFTER = 3;

    private GameObject rootGameObject;
    private DnDHandler dnDHandler;

    public Hierarchy(GameObject rootGameObject) {
        super();
        this.setModel(new DefaultTreeModel(new GameObjectNode(rootGameObject)));
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
        selectionModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

        this.setSelectionModel(selectionModel);
        this.setCellRenderer(renderer);

        JBPopupMenu popupMenu = new JBPopupMenu();
        JMenuItem item;
        popupMenu.add(item = new JMenuItem(ACTION_ADD_GAMEOBJECT));
        item.addActionListener(this);
        popupMenu.add(item = new JMenuItem(ACTION_ADD_GAMEOBJECT_R));
        item.addActionListener(this);
        popupMenu.add(new JPopupMenu.Separator());
        popupMenu.add(item = new JMenuItem(ACTION_DELETE));
        item.addActionListener(this);
        popupMenu.add(new JPopupMenu.Separator());
        popupMenu.add(item = new JMenuItem(ACTION_DUPLICATE));
        item.addActionListener(this);

        this.setComponentPopupMenu(popupMenu);

        this.dnDHandler = new DnDHandler();

        DnDManager.getInstance().registerSource(this.dnDHandler, this);
        DnDManager.getInstance().registerTarget(this.dnDHandler, this);
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

    public void onInsertBeforeGameObject(GameObject beInserted, GameObject relatived) {
        DefaultTreeModel treeModel = (DefaultTreeModel)this.getModel();
        GameObjectNode relativedNode = searchNode(relatived);
        GameObjectNode parent = (GameObjectNode)relativedNode.getParent();
        int index = parent.getIndex(relativedNode);
        GameObjectNode childNode = new GameObjectNode(beInserted);
        treeModel.insertNodeInto(childNode, parent, index);
    }

    public void onInsertAfterGameObject(GameObject beInserted, GameObject relatived) {
        DefaultTreeModel treeModel = (DefaultTreeModel)this.getModel();
        GameObjectNode relativedNode = searchNode(relatived);
        GameObjectNode parent = (GameObjectNode)relativedNode.getParent();
        int index = parent.getIndex(relativedNode);
        GameObjectNode childNode = new GameObjectNode(beInserted);
        treeModel.insertNodeInto(childNode, parent, index+1);
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
                ((DefaultTreeModel)getModel()).nodeChanged(this);
            }
        }

        public void onRemoveFromParent() {
            this.gameObject.removeChangeListener(this);
        }

    }

    class DnDHandler extends DnDAdapter {

        @Override
        public boolean canStartDragging(DnDAction action, Point dragOrigin) {
            GameObjectNode node = getSelectedNode();
            return node != null && !node.isRoot();
        }

        @Override
        public DnDDragStartBean startDragging(DnDAction action, Point dragOrigin) {
            return new DnDDragStartBean(getSelectedNode(), dragOrigin);
        }

        @Nullable
        @Override
        public Pair<Image, Point> createDraggedImage(DnDAction action, Point dragOrigin) {
            GameObjectNode node = getSelectedNode();
            JLabel label = new JLabel(node.gameObject.toString());
            label.setIcon(Icons.GAMEOBJECT_ICON);
            label.setOpaque(true);
            label.setForeground(Hierarchy.this.getForeground());
            label.setBackground(Hierarchy.this.getBackground());
            label.setFont(Hierarchy.this.getFont());
            label.setSize(label.getPreferredSize());
            BufferedImage image = UIUtil.createImage(label.getWidth(), label.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D)image.getGraphics();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            label.paint(g2);
            g2.dispose();
            return new Pair<Image, Point>(image, new Point(-image.getWidth(null)/3*2, -image.getHeight(null)));
        }

        @Override
        public boolean update(DnDEvent event) {
            if(!(event.getAttachedObject() instanceof GameObjectNode)) {
                File file = getSingleFile(event);
                if(file != null && file.getName().endsWith(WozllaIDEAPlugin.SCENE_FILE_SUFFIX)) {
                    event.setDropPossible(true);
                } else {
                    event.setDropPossible(false);
                }
                return false;
            }
            final GameObjectNode draggedNode = (GameObjectNode)event.getAttachedObject();
            final Point point = event.getPoint();
            final TreePath path = Hierarchy.this.getClosestPathForLocation(point.x, point.y);
            final GameObjectNode targetNode = (GameObjectNode)path.getLastPathComponent();
            final Rectangle pathBounds = Hierarchy.this.getPathBounds(path);
            if(pathBounds == null || targetNode == null || draggedNode == targetNode) {
                event.setDropPossible(false);
                return false;
            }
            final double distance = point.y - pathBounds.getCenterY();
            int type;
            if(Math.abs(distance) <= 7) {
                type = DnDEvent.DropTargetHighlightingType.RECTANGLE;
            } else {
                if(targetNode.isRoot()) {
                    event.setDropPossible(false);
                    return false;
                }
                type = DnDEvent.DropTargetHighlightingType.H_ARROWS;
                if(distance > 0) {
                    pathBounds.y += pathBounds.getHeight()/2;
                } else {
                    pathBounds.y -= pathBounds.getHeight()/2;
                }
            }
            event.setHighlighting(new RelativeRectangle(Hierarchy.this, pathBounds), type);
            event.setDropPossible(true);
            return false;
        }

        @Override
        public void drop(DnDEvent event) {
            if(!event.isDropPossible()) {
                return;
            }

            if(!(event.getAttachedObject() instanceof GameObjectNode)) {
                File file = getSingleFile(event);
                if(file != null && file.getName().endsWith(WozllaIDEAPlugin.SCENE_FILE_SUFFIX)) {
                    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
                    if(virtualFile != null) {

                    }
                }
                return;
            }

            final GameObjectNode draggedNode = (GameObjectNode)event.getAttachedObject();
            final Point point = event.getPoint();
            final TreePath path = Hierarchy.this.getClosestPathForLocation(point.x, point.y);
            final GameObjectNode targetNode = (GameObjectNode)path.getLastPathComponent();
            final Rectangle pathBounds = Hierarchy.this.getPathBounds(path);
            if(pathBounds == null || targetNode == null || draggedNode == targetNode) {
                return;
            }

            final double distance = point.y - pathBounds.getCenterY();
            int action;
            if(Math.abs(distance) <= 7) {
                action = DROP_ACTION_APPEND;
            } else {
                if(distance > 0) {
                    action = DROP_ACTION_INSERT_AFTER;
                } else {
                    action = DROP_ACTION_INSERT_BEFORE;
                }
            }

            switch(action) {
                case DROP_ACTION_APPEND:
                    draggedNode.gameObject.getParent().removeGameObject(draggedNode.gameObject);
                    targetNode.gameObject.addGameObject(draggedNode.gameObject);
                    break;
                case DROP_ACTION_INSERT_AFTER:
                    draggedNode.gameObject.getParent().removeGameObject(draggedNode.gameObject);
                    targetNode.gameObject.getParent().insertAfter(draggedNode.gameObject, targetNode.gameObject);
                    break;
                case DROP_ACTION_INSERT_BEFORE:
                    draggedNode.gameObject.getParent().removeGameObject(draggedNode.gameObject);
                    targetNode.gameObject.getParent().insertBefore(draggedNode.gameObject, targetNode.gameObject);
                    break;
            }
        }

        private File getSingleFile(DnDEvent event) {
            java.util.List<File> result;
            Object attached = event.getAttachedObject();
            if (attached instanceof TransferableWrapper) {
                result = ((TransferableWrapper)attached).asFileList();
                if(result != null && result.size() == 1) {
                    return result.get(0);
                }
            }
            return null;
        }
    }

}

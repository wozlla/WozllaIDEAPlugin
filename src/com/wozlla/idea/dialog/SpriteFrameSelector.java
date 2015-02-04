package com.wozlla.idea.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import javafx.scene.control.ComboBox;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;

public class SpriteFrameSelector extends DialogWrapper {

    private Project project;
    private VirtualFile spriteAtlasFile;
    private JSONObject spriteData;
    private FrameItem selectedFrame;
    private SpriteAtlasPanel spriteAtlasPanel;
    private String initFrame;

    public SpriteFrameSelector(Project project, VirtualFile spriteAtlasFile, JSONObject spriteData, String initFrame) {
        super(project, false);
        this.project = project;
        this.spriteAtlasFile = spriteAtlasFile;
        this.spriteData = spriteData;
        this.initFrame = initFrame;
        this.init();
    }

    public String getSelectedFrame() {
        return selectedFrame.name;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setPreferredSize(new Dimension(700, 500));
        container.setSize(new Dimension(700, 500));

        try {
            String imagePath = spriteData.getJSONObject("meta").getString("image");
            VirtualFile imageFile = spriteAtlasFile.getParent().findChild(imagePath);
            if (imageFile != null) {
                Image image = ImageIO.read(imageFile.getInputStream());
                spriteAtlasPanel = new SpriteAtlasPanel(image);
                JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel frameComboLabel = new JLabel("Search: ");

                FrameItem initFrameItem = new FrameItem("none", null);
                java.util.List<FrameItem> frames = new ArrayList<FrameItem>();
                frames.add(initFrameItem);
                Object framesData = spriteData.get("frames");
                if(framesData instanceof JSONArray) {
                    for(int i=0, len=((JSONArray)framesData).length(); i<len; i++) {
                        FrameItem frameItem = new FrameItem(i+"", ((JSONArray) framesData).getJSONObject(i));
                        frames.add(frameItem);
                        if((i+"").equals(initFrame)) {
                            initFrameItem = frameItem;
                        }
                    }
                } else {
                    JSONObject framesJSONObj = (JSONObject) framesData;
                    Iterator<String> iter = framesJSONObj.keys();
                    while(iter.hasNext()) {
                        String name = iter.next();
                        FrameItem frameItem = new FrameItem(name, framesJSONObj.getJSONObject(name));
                        frames.add(frameItem);
                        if(name.equals(initFrame)) {
                            initFrameItem = frameItem;
                        }
                    }
                }

                JComboBox<FrameItem> comboBox = new com.intellij.openapi.ui.ComboBox();
                for(FrameItem item : frames) {
                    comboBox.addItem(item);
                }
                comboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        selectFrame((FrameItem)e.getItem());
                    }
                });
                comboBox.setEditable(false);
                toolbar.add(frameComboLabel);
                toolbar.add(comboBox);
                container.add(toolbar, BorderLayout.NORTH);
                JBScrollPane scrollPane = new JBScrollPane(spriteAtlasPanel);
                scrollPane.setAutoscrolls(true);
                container.add(scrollPane, BorderLayout.CENTER);
                comboBox.setSelectedItem(initFrameItem);
                return container;
            }

            JLabel label = new JLabel("Image not found");
            container.add(label);
            return container;
        } catch(Exception e) {
            JLabel label = new JLabel(e.getMessage());
            container.add(label);
            return container;
        }
    }

    protected void selectFrame(FrameItem frameItem) {
        if(null == frameItem.data) {
            spriteAtlasPanel.rect = null;
            spriteAtlasPanel.repaint();
            return;
        }
        Rectangle rect = new Rectangle();
        try {
            JSONObject frame = frameItem.data.getJSONObject("frame");
            rect.setBounds(
                (int) frame.getDouble("x"),
                (int) frame.getDouble("y"),
                (int) frame.getDouble("w"),
                (int) frame.getDouble("h")
            );
            spriteAtlasPanel.rect = rect;
            spriteAtlasPanel.repaint();
            this.selectedFrame = frameItem;
        } catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private class FrameItem {

        private String name;
        private JSONObject data;

        FrameItem(String name, JSONObject data) {
            this.name = name;
            this.data = data;
        }

        public String toString() {
            return name;
        }

    }

    private class SpriteAtlasPanel extends JPanel {

        public Image image;

        public Rectangle rect;

        SpriteAtlasPanel(Image image) {
            super(new BorderLayout());
            this.image = image;
            this.setSize(new Dimension(this.image.getWidth(null), this.image.getHeight(null)));
            this.setPreferredSize(new Dimension(this.image.getWidth(null), this.image.getHeight(null)));
            this.setMaximumSize(new Dimension(this.image.getWidth(null), this.image.getHeight(null)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters
            g.setColor(new Color(75, 75, 255, 130));
            if(rect != null) {
                g.fillRect(rect.x, rect.y, rect.width, rect.height);
            }
        }

    }


}
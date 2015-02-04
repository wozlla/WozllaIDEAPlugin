package com.wozlla.idea.editor.inspector;

import com.intellij.icons.AllIcons;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public abstract class CommonPane extends JPanel {

    private JComponent content;
    private boolean closable;

    private java.util.List<CloseListener> listenerList = new ArrayList<CloseListener>();

    public CommonPane(String title, final JComponent content, boolean closable) {
        super(new BorderLayout(0, 0));
        this.content = content;
        this.closable = closable;
        content.setBorder(new EmptyBorder(4, 4, 4, 4));
        final JPanel header = new JPanel(new BorderLayout());
        final JLabel label = new JLabel(title);
        label.setIcon(AllIcons.Nodes.TreeDownArrow);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(100, 100, 100)), new EmptyBorder(0, 4, 0, 0)));
        header.add(label, BorderLayout.CENTER);
        final JLabel closeLabel = new JLabel("x");
        closeLabel.setVisible(closable);
        closeLabel.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(100, 100, 100)), new EmptyBorder(0, 4, 0, 4)));
        header.add(closeLabel, BorderLayout.EAST);
        this.add(header, BorderLayout.NORTH);
        this.add(content, BorderLayout.CENTER);

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();
                    content.setVisible(!content.isVisible());
                    label.setIcon(content.isVisible() ? AllIcons.Nodes.TreeDownArrow : AllIcons.Nodes.TreeRightArrow);
                }
            }
        });

        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for(CloseListener l : listenerList) {
                    l.onClose(CommonPane.this);
                }
            }
        });


    }

    public JComponent getContent() {
        return this.content;
    }

    public void addCloseListener(CloseListener listener) {
        listenerList.add(listener);
    }

    public void removeCloseListener(CloseListener listener) {
        listenerList.remove(listener);
    }

    public abstract void destroyFields();

    public static interface CloseListener {
        void onClose(CommonPane pane);
    }
}

package com.wozlla.idea.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.wozlla.idea.IComponentConfigManager;
import com.wozlla.idea.Icons;
import com.wozlla.idea.scene.ComponentConfig;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

public class ComponentDialog extends DialogWrapper {

    private Project project;
    private ComponentConfig componentConfig;

    public ComponentDialog(Project project) {
        super(project, false);
        this.project = project;
        this.init();
    }

    public ComponentConfig getSelectedComponentConfig() {
        return componentConfig;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        IComponentConfigManager mgr = project.getComponent(IComponentConfigManager.class);
        Collection<ComponentConfig> configList = mgr.getComponentConfigAsList();
        final ComponentConfig[] configArray = new ComponentConfig[configList.size()];
        configList.toArray(configArray);

        JPanel container = new JPanel(new BorderLayout());
        final JTextField searchField;

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Search: ");
        searchPanel.add(searchLabel);
        searchPanel.add(searchField = new JTextField());
        searchField.setPreferredSize(new Dimension(440, 26));
        container.add(searchPanel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(new LineBorder(new JBColor(0x555555, 0x555555)));
        final JBList compList = new JBList(configArray);
        compList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    doOKAction();
                }
            }
        });
        compList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        compList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                componentConfig = (ComponentConfig)compList.getSelectedValue();
            }
        });
        compList.setSelectedIndex(0);
        compList.setAutoscrolls(true);

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(6, 6, 6, 6));
                label.setIcon(Icons.COMPONENT_ICON);
                return label;
            }
        };
        compList.setCellRenderer(renderer);
        listPanel.add(compList, BorderLayout.CENTER);
        container.add(listPanel, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent) {
                String filter = searchField.getText();
                DefaultListModel model = (DefaultListModel)compList.getModel();
                for(ComponentConfig config : configArray) {
                    if(!config.name.contains(filter)) {
                        if(model.contains(config)) {
                            model.removeElement(config);
                        } else {
                            model.addElement(config);
                        }
                    }
                }
                compList.setSelectedIndex(0);
            }
        });

        container.setPreferredSize(new Dimension(500, 500));
        container.setSize(new Dimension(500, 500));

        return container;
    }
}

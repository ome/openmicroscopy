/*
 * adminTool.GroupList 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package src.adminTool.ui;

// Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import src.adminTool.model.Model;

// Third-party libraries

// Application-internal dependencies

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since OME3.0
 */
public class GroupList extends JPanel {
    private JList groups;

    private DefaultListModel listModel;

    private Model model;

    private GroupListController controller;

    private boolean filterByUser;

    private String userName;

    public void filterOff() {
        filterByUser = false;
    }

    public void filterGroupsByUser(String name) {
        filterByUser = true;
        userName = name;
        refresh();
    }

    public void refresh() {
        List data;
        if (filterByUser)
            data = model.getGroupsUserNotMemberOf(userName);
        else
            data = model.getGroupsList();
        listModel.clear();
        for (int i = 0; i < data.size(); i++)
            listModel.add(i, data.get(i));
        groups.setModel(listModel);
    }

    public void selectFirstGroup() {
        if (listModel.size() != 0)
            groups.setSelectedIndex(0);
    }

    public int findCurrentGroup(String name) {
        for (int i = 0; i < listModel.size(); i++) {
            String listelement = (String) listModel.get(i);
            if (listelement.equals(name))
                return i;
        }
        return -1;
    }

    public void selectCurrentGroup() {
        int currentGroup = findCurrentGroup(model.getCurrentGroup().getName());
        if (currentGroup != -1)
            groups.setSelectedIndex(currentGroup);
    }

    public void clear() {
        listModel.clear();
    }

    public String getSelectedGroup() {
        if (listModel.size() == 0)
            return null;
        if (groups.getLeadSelectionIndex() < 0)
            return null;
        if (groups.getLeadSelectionIndex() < listModel.size())
            return (String) listModel.get(groups.getLeadSelectionIndex());
        else
            return null;
    }

    public GroupList(Model model) {
        listModel = new DefaultListModel();
        this.model = model;
        filterByUser = false;
        createGroupList();
        buildUI();
    }

    void createGroupList() {
        groups = new JList(listModel);
        groups.setCellRenderer(new ListRenderer());

        List data = model.getGroupsList();

        listModel.clear();
        for (int i = 0; i < data.size(); i++)
            listModel.add(i, data.get(i));

        groups.setModel(listModel);

        groups.setPreferredSize(new Dimension(200, 7 * 22));
        groups.setMinimumSize(new Dimension(200, 7 * 22));
        groups.setMaximumSize(new Dimension(200, 7 * 22));
        groups.setPreferredSize(new Dimension(200, 7 * 22));
    }

    void buildUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(groups, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        JPanel containerPanel = new JPanel();
        containerPanel
                .setLayout(new BoxLayout(containerPanel, BoxLayout.X_AXIS));
        containerPanel.add(panel);
        this.setLayout(new BorderLayout());
        this.add(containerPanel);
    }

    /**
     * @param controller
     */
    public void setController(GroupListController listcontroller) {
        this.controller = listcontroller;

        groups.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    JList list = (JList) e.getSource();
                    if (list.getLeadSelectionIndex() >= 0
                            && list.getLeadSelectionIndex() < listModel.size())
                        controller.groupSelected((String) listModel.get(list
                                .getLeadSelectionIndex()));
                }
            }
        });
    }
}

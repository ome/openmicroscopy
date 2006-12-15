/*
 * adminTool.GroupMembershipList 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.ui;

// Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
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
public class GroupMembershipList extends JPanel {
    private JList users;

    private DefaultListModel listModel;

    private Model model;

    private UserListController controller;

    private String currentGroup;

    public void clear() {
        listModel.clear();
    }

    public ArrayList getUsers() {
        ArrayList currentUsers = new ArrayList();

        for (int i = 0; i < listModel.size(); i++)
            currentUsers.add(listModel.get(i));
        return currentUsers;
    }

    public void refresh() {
        if (currentGroup != null) {
            List data = model.getGroupMembership(currentGroup);
            listModel.clear();
            for (int i = 0; i < data.size(); i++)
                listModel.add(i, data.get(i));
            users.setModel(listModel);
        }
    }

    public String getSelectedUser() {
        if (listModel.size() == 0)
            return null;
        if (users.getLeadSelectionIndex() < 0)
            return null;

        return (String) listModel.get(users.getLeadSelectionIndex());
    }

    public void setGroup(String grp) {
        currentGroup = grp;
        refresh();
    }

    public GroupMembershipList(Model model) {
        listModel = new DefaultListModel();
        this.model = model;
        currentGroup = null;
        createMembersList();
        buildUI();
    }

    void setController(UserListController control) {
        this.controller = control;
        users.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    JList list = (JList) e.getSource();
                    if (list.getLeadSelectionIndex() >= 0)
                        controller.userSelected((String) listModel.get(list
                                .getLeadSelectionIndex()));
                }
            }
        });
    }

    void createMembersList() {
        users = new JList(listModel);
        users.setCellRenderer(new UserListRenderer(model));

        refresh();

        users.setPreferredSize(new Dimension(200, 7 * 22));
        users.setMinimumSize(new Dimension(200, 7 * 22));
        users.setMaximumSize(new Dimension(200, 7 * 22));
        users.setPreferredSize(new Dimension(200, 7 * 22));
    }

    void buildUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        panel.add(users, BorderLayout.CENTER);
        this.add(panel);
    }

}

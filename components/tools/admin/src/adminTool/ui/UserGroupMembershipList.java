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
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

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
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$Date: $)
 *          </small>
 * @since OME3.0
 */
public class UserGroupMembershipList extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -4914633082830959422L;

    private JList groups;

    private DefaultListModel listModel;

    private Model model;

    public void clear() {
        listModel.clear();
    }

    public String getSelectedUserGroup() {
        if (listModel.size() == 0) {
            return null;
        }
        if (groups.getLeadSelectionIndex() < 0) {
            return null;
        }
        if (groups.getLeadSelectionIndex() < listModel.size()) {
            return (String) listModel.get(groups.getLeadSelectionIndex());
        } else {
            return null;
        }
    }

    public UserGroupMembershipList(Model model) {
        listModel = new DefaultListModel();
        this.model = model;
        createGroupList();
        buildUI();
    }

    void createGroupList() {
        groups = new JList(listModel);
        groups.setCellRenderer(new UserGroupMembershipListRenderer(model));
        groups.setPreferredSize(new Dimension(200, 7 * 22));
        groups.setMinimumSize(new Dimension(200, 7 * 22));
        groups.setMaximumSize(new Dimension(200, 7 * 22));
        groups.setPreferredSize(new Dimension(200, 7 * 22));
    }

    public void setUser(String userName) {
        listModel.clear();
        if (model.findUserByName(userName)) {
            List data = model.getUserGroupMembership(model.getCurrentUserID());
            listModel.clear();
            for (int i = 0; i < data.size(); i++) {
                listModel.add(i, data.get(i));
            }
        }
    }

    void buildUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(groups, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.setLayout(new BorderLayout());
        this.add(panel);
    }

}

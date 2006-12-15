/*
 * adminTool.groupPanel.UserAction 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.groupPanel;

// Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public class UserAction implements ActionListener {
    public final static int ADD = 1;

    public final static int REMOVE = 2;

    private GroupsTabController controller;

    int actionType;

    UserAction(int actionType, GroupsTabController controller) {
        this.controller = controller;
        this.actionType = actionType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        switch (actionType) {
            case ADD:
                controller.addUser();
                break;
            case REMOVE:
                controller.removeUser();
                break;
        }
    }

}

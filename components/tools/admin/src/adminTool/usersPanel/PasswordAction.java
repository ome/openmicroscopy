/*
 * adminTool.usersPanel.PasswordAction 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.usersPanel;

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
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$Date: $)
 *          </small>
 * @since OME3.0
 */
public class PasswordAction 
	implements ActionListener 
{
	public static int CHANGE_SELECTED_USER = 0;
	public static int CHANGE_CURRENT_USER = 1;
	
    private UsersTabController controller;
    private int 				actionType;

    PasswordAction(int actionType, UsersTabController controller) {
    	this.actionType = actionType;
        this.controller = controller;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
    	if(actionType == CHANGE_SELECTED_USER)
    		controller.changePassword();
    	if(actionType == CHANGE_CURRENT_USER)
    		controller.changeLoggedInUserPassword();
    	

    }

}

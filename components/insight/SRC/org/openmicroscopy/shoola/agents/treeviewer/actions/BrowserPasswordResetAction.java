/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserPasswordResetAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.util.PasswordDialog;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;

/** 
 * Resets the password of the selected experimenters. 
 * This is only available to administrator.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class BrowserPasswordResetAction 
	extends BrowserAction
{

    /** The description of the action. */
    private static final String DESCRIPTION = "Resets the password " +
    		"of the selected users.";
    
    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see BrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
    	 switch (model.getState()) {
	    	 case Browser.LOADING_DATA:
	         case Browser.LOADING_LEAVES:
	         case Browser.COUNTING_ITEMS:  
	             setEnabled(false);
	             break;
	         default: 
	        	 if (model.getBrowserType() != Browser.ADMIN_EXPLORER)
	             		setEnabled(false);
	             	else
	             		onDisplayChange(model.getLastSelectedDisplay());
         }
    }

    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
    	if (!TreeViewerAgent.isAdministrator() || selectedDisplay == null) {
   		 	setEnabled(false);
            return;
    	}
    	if (model.getBrowserType() != Browser.ADMIN_EXPLORER) {
    		setEnabled(false);
    		return;
    	}
    	setEnabled(selectedDisplay.getUserObject() instanceof ExperimenterData);
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public BrowserPasswordResetAction(Browser model)
	{
		super(model);
		setEnabled(false);
		IconManager icons = IconManager.getInstance();
		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PASSWORD));
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION));
	}
	
    /**
     * Displays a modal dialog.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	JFrame f = TreeViewerAgent.getRegistry().getTaskBar().getFrame();
    	PasswordDialog d = new PasswordDialog(f);
    	d.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				if (PasswordDialog.RESET_PASSWORD_PROPERTY.equals(
						evt.getPropertyName())) {
					String value = (String) evt.getNewValue();
					model.resetPassword(value);
				}
				
			}
		});
    	UIUtilities.centerAndShow(d);
    }
	
}

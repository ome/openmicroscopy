/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.DisplayModeAction
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
/**
 * Action to toggle between the view per users and view per group.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class DisplayModeAction
	extends TreeViewerAction
{

	/** The name of the action. */
    private static final String NAME_GROUP = "Group Display";
    
    /** The name of the action. */
    private static final String NAME_EXPERIMENTER = "User Display";
    
    /** The description of the action. */
    private static final String DESCRIPTION_EXP = "Display the data per user";
    
	/** The description of the action. */
    private static final String DESCRIPTION_GROUP =
    		"Display the data per group";
    
    /** One of the constants defined by this class.*/
    private int index;
    
    /**
     * Checks and sets the index.
     * 
     * @param index The value to set.
     */
    private void checkIndex(int index)
    {
    	switch (index) {
			case LookupNames.GROUP_DISPLAY:
			case LookupNames.EXPERIMENTER_DISPLAY:
				this.index = index;
				break;
			default:
				this.index = LookupNames.EXPERIMENTER_DISPLAY;
		}
    }
    
    /** Sets the details of the action.*/
    private void setDetails()
    {
    	switch (index) {
	    	case LookupNames.GROUP_DISPLAY:
	    		putValue(Action.NAME, NAME_GROUP);
	    		putValue(Action.SHORT_DESCRIPTION, 
	    				UIUtilities.formatToolTipText(DESCRIPTION_GROUP));
	    		break;
	    	case LookupNames.EXPERIMENTER_DISPLAY:
	    		putValue(Action.NAME, NAME_EXPERIMENTER);
	    		putValue(Action.SHORT_DESCRIPTION, 
	    				UIUtilities.formatToolTipText(DESCRIPTION_EXP));
    	}
    }
    
    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
    	if (browser == null) return;
    	setEnabled(model.getState() == TreeViewer.READY);
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public DisplayModeAction(TreeViewer model, int index)
    {
    	super(model);
    	checkIndex(index);
    	setEnabled(true);
    	setDetails();
    }
    
    /**
     * Returns the index associated to the action.
     * 
     * @return See above.
     */
    public int getIndex() { return index; }
    
    /**
     * Modifies the display mode.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.setDisplayMode(index); }

}

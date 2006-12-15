/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.RootLevelAction
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.cmd.RootLevelCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Action to select the root of the retrieved hierarchy e.g. if the level 
 * selected is {@link TreeViewer#USER_ROOT}, the user's hierarchies will
 * be loaded.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class RootLevelAction
	extends TreeViewerAction
{
    
    /** 
     * Name of the action if the {@link #rootLevel} is 
     * {@link TreeViewer#USER_ROOT}. 
     */
    private static final String NAME_USER = "My work";
    
    /** 
     * Description of the action if the {@link #rootLevel} is 
     * {@link TreeViewer#USER_ROOT}. 
     */
    private static final String DESCRIPTION_USER = "Reads only my work.";
    
    /** 
     * Name of the action if the {@link #rootLevel} is 
     * {@link TreeViewer#GROUP_ROOT}. 
     */
    private static final String NAME_GROUP = "Group ";
    
    /** 
     * Description of the action if the {@link #rootLevel} is 
     * {@link TreeViewer#GROUP_ROOT}. 
     */
    private static final String DESCRIPTION_GROUP = "Reads any hierarchy " +
    											"within the specified group.";
    
    /** Identifies the root's level. */
    private int     rootLevel;
    
    /**
     * The Id of the root if the {@link #rootLevel}
     * is {@link TreeViewer#GROUP_ROOT}.
     */
    private long    rootID;
    
    /**
     * Sets the name and the description of the action.
     * 
     * @param name The name to set if any.
     */
    private void setValues(String name)
    {
        switch (rootLevel) {
	        case TreeViewer.USER_ROOT:
	            putValue(Action.NAME, NAME_USER);
        		putValue(Action.SHORT_DESCRIPTION, 
        		        UIUtilities.formatToolTipText(DESCRIPTION_USER));
	            break;
	        case TreeViewer.GROUP_ROOT:
	            putValue(Action.NAME, NAME_GROUP+name);
    			putValue(Action.SHORT_DESCRIPTION, 
    		        UIUtilities.formatToolTipText(DESCRIPTION_GROUP));
	            break;
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model     Reference to the Model. Mustn't be <code>null</code>.
     */
    public RootLevelAction(TreeViewer model)
    {
        super(model);
        this.rootLevel = model.getRootLevel();
        rootID = model.getRootGroupID();
        setValues(null);
        setEnabled(true);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param rootLevel {@link TreeViewer#GROUP_ROOT}.
     * @param rootID The Id of the root node.
     * @param rootName The name of the group.
     */
    public RootLevelAction(TreeViewer model, int rootLevel, long rootID,
            				String rootName)
    {
        super(model);
        if (rootLevel != TreeViewer.GROUP_ROOT)
            throw new IllegalArgumentException("Root level not supported.");
        this.rootLevel = rootLevel;
        this.rootID = rootID;
        setValues(rootName);
        setEnabled(true);
    }

    /**
     * Creates a {@link RootLevelCmd} command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent ae)
    {
        RootLevelCmd cmd = new RootLevelCmd(model, rootLevel, rootID);
        cmd.execute();
    }
    
}

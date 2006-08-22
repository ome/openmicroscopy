/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.RootLevelAction
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
        this.rootLevel = TreeViewer.USER_ROOT;
        rootID = -1;
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

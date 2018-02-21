/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ProjectionProjectAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Projects the whole image.
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
public class ProjectionProjectAction 
	extends ViewerAction
{

	/** The name of the action. */
    private static final String NAME = "Project...";
    
    /** The description of the action. */
    private static final String DESCRIPTION = 
    	"Project the selected interval for the whole image.";
    
    protected void onTabSelection()
    {
    	if (model.getState() == ImViewer.READY && !model.isBigImage()) {
    		if (model.getSelectedIndex() != ImViewer.PROJECTION_INDEX)
    			setEnabled(false);
    		else setEnabled(model.canEdit() &&
    		        model.getActiveChannelsInProjection().size() > 0);
    	} else setEnabled(false);
    }

    /**
     * Reacts to state changes.
     * @see ViewerAction#onStateChange(ChangeEvent)
     */
    protected void onStateChange(ChangeEvent e) { onTabSelection(); }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public ProjectionProjectAction(ImViewer model)
    {
    	super(model, NAME);
    	putValue(Action.NAME, NAME);
    	putValue(Action.SHORT_DESCRIPTION, 
    			UIUtilities.formatToolTipText(DESCRIPTION));
    }
    
    /** 
     * Previews the projected image.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.loadContainers(); }
    
}

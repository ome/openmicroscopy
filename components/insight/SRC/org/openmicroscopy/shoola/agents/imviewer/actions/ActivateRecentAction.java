/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ActivateRecentAction 
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
import javax.swing.AbstractAction;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewerRecentObject;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Action to bring the viewer for an image recently viewed.
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
public class ActivateRecentAction 
	extends AbstractAction
{

	/** Description of the action. */
    private static final String DESCRIPTION = "View the selected image.";
    
    /** The object hosting information about a recently viewed image. */
    private ImViewerRecentObject recent;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param recent 	The object hosting information about a recently 
	 * 					viewed image.
	 */
	public ActivateRecentAction(ImViewerRecentObject recent)
	{
		if (recent == null)
			throw new IllegalArgumentException("No recent viewed image.");
		this.recent = recent;
		putValue(Action.NAME, recent.getImageName());
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        putValue(Action.SMALL_ICON, recent.getImageIcon());
	}
	
	/** 
     * Activates the model. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    { 
    	EventBus bus = ImViewerAgent.getRegistry().getEventBus();
    	ViewImage event = new ViewImage(recent.getSecurityContext(),
    			new ViewImageObject(recent.getImageID()), null);
    	bus.post(event);
    }
    
}

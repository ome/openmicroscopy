/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ChannelsSelectionAction
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
package org.openmicroscopy.shoola.agents.imviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Action to select or de-select all the channels.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ChannelsSelectionAction 
	extends ViewerAction
{

	/** Flag indicating to select or de-select all channels. */
	private boolean selection;
	
	/** 
	 * The name of the action if {@link #selection} is <code>true</code>.
	 */
	private static final String NAME_ON = "Turn channels on";
	
	/** 
	 * The name of the action if {@link #selection} is <code>true</code>.
	 */
	private static final String NAME_OFF = "Turn channels off";

	/** 
	 * The Description of the action if {@link #selection} is <code>true</code>.
	 */
	private static final String DESCRIPTION_ON = "Turn all channels on.";
	
	/** 
	 * The Description of the action if {@link #selection} is <code>false</code>.
	 */
	private static final String DESCRIPTION_OFF = "Turn all channels off.";
	
    /**
     * Creates a new instance.
     * 
     * @param model		Reference to the model. Mustn't be <code>null</code>.
     * @param selection Pass <code>true</code> to select all the channels,
     * 					<code>false</code> otherwise.
     */
	public ChannelsSelectionAction(ImViewer model, boolean selection)
	{
		super(model);
		this.selection = selection;
		IconManager icons = IconManager.getInstance();
    	putValue(Action.SMALL_ICON, icons.getIcon(IconManager.TRANSPARENT));
		if (selection) {
			putValue(Action.NAME, NAME_ON);
			putValue(Action.SHORT_DESCRIPTION, 
	                UIUtilities.formatToolTipText(DESCRIPTION_ON));
		} else {
			putValue(Action.NAME, NAME_OFF);
			putValue(Action.SHORT_DESCRIPTION, 
	                UIUtilities.formatToolTipText(DESCRIPTION_OFF));
		}
	}
	
	/** 
	 * Turns all channels on or off.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
    public void actionPerformed(ActionEvent e)
    {
    	model.selectAllChannels(selection);
    }
    
}

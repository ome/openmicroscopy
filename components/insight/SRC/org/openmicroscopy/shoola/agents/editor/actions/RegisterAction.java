/*
 * org.openmicroscopy.shoola.agents.editor.actions.RegisterAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.agents.editor.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.events.editor.ShowEditorEvent;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Registers the agent.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class RegisterAction 
	extends AbstractAction
{
	
	/** The name of the action. */
	public static final String NAME = "Editor...";
	
	/** The description of the action. */
	private static final String DESCRIPTION = "Open the Editor.";
	
	/**
	 * Creates a new instance.
	 */
	public RegisterAction()
	{
		IconManager icons = IconManager.getInstance();
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION));
		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.EDITOR)); 
	}
	
	/**
	 * Posts an event on the bus to open the editor.
	 * @see AbstractAction#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		EventBus bus = EditorAgent.getRegistry().getEventBus();
		bus.post(new ShowEditorEvent());
	}

}

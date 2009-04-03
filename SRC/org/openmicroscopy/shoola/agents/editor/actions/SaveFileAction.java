 /*
 * org.openmicroscopy.shoola.agents.editor.actions.SaveFileLocallyAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.actions;

//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.view.Editor;

/** 
 * This Action allows users to choose a local 
 * location to save an OMERO.editor file. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SaveFileAction 
	extends EditorAction
	implements PropertyChangeListener
{

	/** The description of the action. */
	private static final String 	NAME = "Save File";

	/** The description of the action. */
	private static final String 	DESCRIPTION = "Save the current file.";

	/** 
	 * Implement this method to disable the Save Action if no file is open
	 * or there is no data to save.  
	 * @see EditorAction#onStateChange()
	 */
	protected void onStateChange()
	{
		int state = model.getState();
		setEnabled(state == Editor.READY);
		
		if (!model.hasDataToSave()) setEnabled(false);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public SaveFileAction(Editor model)
	{
		super(model);
		setEnabled(false);
		setName(NAME);
		setDescription(DESCRIPTION);
		setIcon(IconManager.SAVE_ICON);
		model.registerBrowserListener(this);
	}

	/**
	 * Saves the currently edited file.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		// if not saved (e.g. file is new) ask where to save...
		if (!model.saveCurrentFile()) {
			ActionCmd save = new SaveNewCmd(model);
			save.execute();
		}
	}

	/**
	 * Reacts to property fired by the <code>Browser</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (Browser.BROWSER_EDIT_PROPERTY.equals(name)) 
			onStateChange();
	}
	
}

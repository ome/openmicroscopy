/*
 * org.openmicroscopy.shoola.agents.rnd.editor.ChannelEditorBar
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

package org.openmicroscopy.shoola.agents.rnd.editor;

//Java imports
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ChannelEditorBar
	extends JToolBar	
{
	
	/** Dimension of the separator between the toolBars. */
	private static final Dimension	SEPARATOR_END = new Dimension(100, 0);
	private static final Dimension	SEPARATOR = new Dimension(15, 0);
	
	private ChannelEditorManager 	manager;
	private JButton					saveButton, cancelButton;
	
	ChannelEditorBar(ChannelEditorManager manager)
	{
		this.manager = manager;
		initButtons();
		buildGUI();
	}

	/** Returns the save button. */
	JButton getSaveButton() { return saveButton; }
	
	/** Returns the cancel button. */
	JButton getCancelButton() { return cancelButton; }
	
	/** Initializes the buttons. */
	void initButtons()
	{
		saveButton = new JButton("OK");
		saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		//make panel transparent
		saveButton.setOpaque(false);
		//suppress button press decoration
		saveButton.setContentAreaFilled(false); 
		saveButton.setToolTipText(
			UIUtilities.formatToolTipText("Save data in the DB."));
		saveButton.setEnabled(false);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		//suppress button press decoration
		cancelButton.setContentAreaFilled(false); 
		cancelButton.setToolTipText(
			UIUtilities.formatToolTipText("Close without saving."));	
	}
	
	/** Build and lay out the GUI. */
	void buildGUI()
	{
		setFloatable(false);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		addSeparator(SEPARATOR_END);
		add(saveButton);
		addSeparator(SEPARATOR);
		add(cancelButton);
	}
	
}


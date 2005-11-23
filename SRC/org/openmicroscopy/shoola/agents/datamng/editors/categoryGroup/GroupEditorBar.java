/*
 * org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup.GroupEditorBar
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

package org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup;


//Java imports
import java.awt.Cursor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
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
class GroupEditorBar
	extends JToolBar
{

	JButton					saveButton, addButton, viewButton;;
	
	GroupEditorBar()
	{
		initButtons();
		buildGUI();
		setFloatable(false);
	}
	
	/** Initializes the buttons. */
	private void initButtons()
	{
		saveButton = new JButton("Save");
		saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
		saveButton.setToolTipText(
			UIUtilities.formatToolTipText("Save data in the DB."));
		saveButton.setEnabled(false);
		addButton = new JButton("Add categories");
		addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addButton.setToolTipText(
			UIUtilities.formatToolTipText("Add categories to this group."));
        viewButton = new JButton("Browse");
        viewButton.setToolTipText(UIUtilities.formatToolTipText("Browse" +
                " the categoryGroup."));
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
        add(UIUtilities.buildComponentPanelRight(buildButtonPanel()));
	}
	
	/** Build panel with buttons. */
	private JPanel buildButtonPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(viewButton);
        p.add(Box.createRigidArea(DataManagerUIF.HBOX));
		p.add(saveButton);
		//p.add(Box.createRigidArea(DataManagerUIF.HBOX));
		//p.add(addButton);
		p.add(Box.createRigidArea(DataManagerUIF.HBOX));
		p.setOpaque(false); //make panel transparent
		return p;
	}
	
}

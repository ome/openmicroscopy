/*
 * org.openmicroscopy.shoola.agents.viewer.util.SelectionDialog
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

package org.openmicroscopy.shoola.agents.viewer.util;

//Java imports
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;

/** 
 * Dialog widget to give the user the choice to save or not the image with the
 * specified name. Note that this dialog only pops up if a file with the same 
 * name and extension already exists in the current directory.
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
class SelectionDialog
	extends JDialog
{
	
	/** Width and Height of the widget. */
	private static final int		W_WIDTH = 200, W_HEIGHT  = 200;
	
	private static final Dimension	HBOX = new Dimension(10, 0);
		
	private ImageSaver 				parentDialog;
	
	private JButton					yesButton, noButton;	
	
	protected String				format, fileName, message;
	
	SelectionDialog (ImageSaver parentDialog, String format, String fileName, 
					String message) 
	{
		super((JFrame) parentDialog.getController().getReferenceFrame(), 
				"Are you sure?", true);
		this.parentDialog = parentDialog;
		this.format = format;
		this.fileName = fileName;
		this.message = message;
		buildGUI();
		new SelectionDialogManager(this);
		setSize(W_WIDTH, W_HEIGHT);
	}

	ImageSaver getParentDialog() { return parentDialog; }
	
	JButton getYesButton() { return yesButton; }
	
	JButton getNoButton() { return noButton; }
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(buildMainPanel(), BorderLayout.CENTER);
	}
	
	/** Main panel. */
	private JPanel buildMainPanel()
	{
		JPanel p = new JPanel(), iconPanel = new JPanel();
		IconManager im = IconManager.getInstance(
							parentDialog.getController().getRegistry());
		iconPanel.add(new JLabel(im.getIcon(IconManager.QUESTION)));
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(iconPanel, c); 
		p.add(iconPanel);
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		MultilineLabel area = buildTextPanel();
		gridbag.setConstraints(area, c);
		p.add(area); 
		c.insets = new Insets(10, 0, 0, 0);  //top padding
		c.gridy = 1;
		JPanel buttons = buttonsPanel();
		gridbag.setConstraints(buttons, c); 
		p.add(buttons);
		return p;
	}
	
	/** Build the text message. */
	private MultilineLabel buildTextPanel()
	{ 
		String  s = "A file with the same name and extension already " +
					"exists in this directory. ";
				s += "Do you still want to save the image with this name?";
		MultilineLabel label = new MultilineLabel(s);
		return label;
	}
	
	/** Build a panel with the buttons. */
	private JPanel buttonsPanel()
	{
		JPanel controls = new JPanel();
		yesButton = new JButton("Yes");
		yesButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		noButton = new JButton("No");
		noButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(yesButton);
		controls.add(Box.createRigidArea(HBOX));
		controls.add(noButton);
		controls.setOpaque(false); //make panel transparent
		return controls;
	}
	
}

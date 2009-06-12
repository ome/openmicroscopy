 /*
 * org.openmicroscopy.shoola.agents.editor.view.NoFileOpenUI 
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
package org.openmicroscopy.shoola.agents.editor.view;

//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * This is a panel to display in the Editor when first started, if no file is
 * open. Gives users something to get them started, with options for opening
 * a file. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class NoFileOpenUI 
	extends JPanel 
{
	
	/** The default font. */
	private static final Font DEFAULT_FONT = new Font("Sans Serif", 
			Font.PLAIN, 14);
	
	/**
	 * Handy method for setting font, alignment, icon of buttons
	 * 
	 * @param action
	 * @param container
	 * @param icon
	 */
	private void addButton(Action action, JComponent container, Icon icon) 
	{
		JButton b = new CustomButton(action);
		b.setIcon(icon);
		b.setAlignmentX(Component.LEFT_ALIGNMENT);
		b.setFont(DEFAULT_FONT);
		container.add(b);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the control.
	 */
	NoFileOpenUI(EditorControl controller)
	{
		// configure the main panel 
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                UIUtilities.LIGHT_GREY));
		
		IconManager im = IconManager.getInstance();
		
		// only show welcome message if running as stand-alone. 
		boolean server = EditorAgent.isServerAvailable();
		JLabel welcomeLabel = null;
		if (!server) {
			// Labels for text 
			welcomeLabel = new CustomLabel();
			welcomeLabel.setFont(DEFAULT_FONT);
			welcomeLabel.setText("Welcome to OMERO.editor");
			welcomeLabel.setIconTextGap(10);
			welcomeLabel.setIcon(im.getIcon(IconManager.OMERO_EDITOR_48));
			welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		
		JLabel infoLabel = new CustomLabel();
		infoLabel.setForeground(new Color(100, 100, 100));
		infoLabel.setFont(new Font("Sans Serif", Font.PLAIN, 12));
		infoLabel.setText("Please choose an option to get you started:");
		infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		if (server) {
			infoLabel.setIcon(im.getIcon(IconManager.OMERO_EDITOR));
		}
		
		
		// A container for the buttons
		//JPanel buttonContainer = new JPanel(); 
		JToolBar buttonContainer = new JToolBar();
		buttonContainer.setFloatable(false);
		buttonContainer.setOrientation(JToolBar.VERTICAL);
		Border lineBorder = BorderFactory.createMatteBorder(1, 1, 1, 1,
                UIUtilities.LIGHT_GREY);
		Border cb = BorderFactory.createCompoundBorder(lineBorder, 
						BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonContainer.setBorder(cb);
		buttonContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonContainer.setLayout
					(new BoxLayout(buttonContainer, BoxLayout.Y_AXIS));
		buttonContainer.setBackground(null);
	
		// add buttons to container, with new icons
		Action openNew = controller.getAction(EditorControl.NEW_BLANK_FILE);
		Icon openIcon = im.getIcon(IconManager.NEW_FILE_ICON_32);
		Action openWww = controller.getAction(EditorControl.OPEN_WWW_FILE);
		Icon wwwIcon = im.getIcon(IconManager.WWW_FOLDER_ICON_32);
		Action openOld = controller.getAction(EditorControl.OPEN_LOCAL_FILE);
		Icon folderIcon = im.getIcon(IconManager.OPEN_FOLDER_ICON_32);
		
		addButton(openNew, buttonContainer, openIcon);
		addButton(openWww, buttonContainer, wwwIcon);
		addButton(openOld, buttonContainer, folderIcon);
		
		
		// need a couple of spacers to align centre panel
		JPanel topSpacer = new JPanel();
		topSpacer.setBackground(null);
		JPanel bottomSpacer = new JPanel();
		bottomSpacer.setBackground(null);

		add(topSpacer);
		
		// add the content of interest. 
		if (welcomeLabel != null) add(welcomeLabel);
		add(infoLabel);
		add(buttonContainer);
		
		add(bottomSpacer);
	}

}

/*
 * org.openmicroscopy.shoola.agents.metadata.editor.TextualAnnotationComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

//Third-party libraries






//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.TextualAnnotationData;

/** 
 * Component displaying a textual annotation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class TextualAnnotationComponent 
	extends JPanel
	implements ActionListener
{

	/** Action id to edit the comment.*/
	private static final int EDIT = 0;
	
	/** Action id to delete the comment.*/
	private static final int DELETE = 1;
	
	/** Area displaying the textual annotation. */
	private JTextArea 		area;
	
	/** The annotation to handle. */
	private TextualAnnotationData	data;
	
	/** Reference to the model.	*/
	private EditorModel	model;

	/** Button to edit the annotation. */
	private JMenuItem	editButton;
	
	/** Button to delete the file annotation. */
	private JMenuItem	deleteButton;
	
	/** The Button used to display the managing option. */
	private JButton		menuButton;
	
	/** The pop-up menu. */
	private JPopupMenu	popMenu;
	
	/** The bar displaying the controls.*/
	private JPanel		controlsBar;
	
	/** Edits the comment.*/
	private void editComment()
	{
		
	}
	
	/** Initializes the UI components. */
	private void initialize()
	{
		area = new JTextArea();
        area.setEnabled(false);
        area.setOpaque(true);
		area.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
        area.setText(data.getText());
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

		IconManager icons = IconManager.getInstance();
		if (model.canEdit(data)) {
			
			editButton = new JMenuItem(icons.getIcon(IconManager.EDIT_12));
			editButton.setText("Edit");
			editButton.setActionCommand(""+EDIT);
			editButton.addActionListener(this);
		}
		if (model.canDelete(data)) {
			menuButton = new JButton(icons.getIcon(IconManager.UP_DOWN_9_12));
			UIUtilities.unifiedButtonLookAndFeel(menuButton);
			menuButton.setBackground(UIUtilities.BACKGROUND_COLOR);
			menuButton.addMouseListener(new MouseAdapter() {
				
				public void mousePressed(MouseEvent e)
				{
					showMenu(menuButton, e.getPoint());
				}
			});
			deleteButton = new JMenuItem(icons.getIcon(IconManager.DELETE_12));
			deleteButton.setText("Delete");
			deleteButton.addActionListener(this);
			deleteButton.setActionCommand(""+DELETE);
		}
	}
	
	/** 
	 * Brings up the menu. 
	 * 
	 * @param invoker The component where the clicks occurred.
	 * @param p The location of the mouse pressed.
	 */
	private void showMenu(JComponent invoker, Point p)
	{
		if (popMenu == null) {
			popMenu = new JPopupMenu();
			//if (editButton != null) popMenu.add(editButton);
			if (deleteButton != null) popMenu.add(deleteButton);
		}
		popMenu.show(invoker, p.x, p.y);
	}
	
	/**
	 * Builds the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		controlsBar = new JPanel();
		controlsBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		String owner = model.formatOwner(data);
		String date = model.formatDate(data);
		JLabel l = new JLabel(owner+" "+date);
		l.setFont(l.getFont().deriveFont(Font.BOLD));
		if (menuButton != null) controlsBar.add(menuButton);
		controlsBar.add(l);
		return controlsBar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		
		add(buildToolBar(), c);
		c.gridy++;
		
		add(area, c);
	}
	
	/**
	 * Creates a new component.
	 * 
	 * @param uiDelegate	Reference to the UI component hosting this 
	 * 						component.
	 * @param model			Reference to the model. 
	 * 						Mustn't be <code>null</code>.
	 * @param data			The annotation to handle.
	 */
	TextualAnnotationComponent(EditorModel model, TextualAnnotationData data)
	{
		this.data = data;
		this.model = model;
		initialize();
		buildGUI();
	}
	
	/**
	 * Returns the annotation hosted by this component.
	 * 
	 * @return See above.
	 */
	TextualAnnotationData getData() { return data; }
	
	/**
	 * Sets the background of the area, the background color
	 * will change when the user clicks on the node.
	 * 
	 * @param color The color to set.
	 */
	void setAreaColor(Color color)
	{
		//area.setOriginalBackground(color);
		setBackground(color);
		Component[] components = getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setBackground(color);
		}
		controlsBar.setBackground(color);
		if (menuButton != null)
			menuButton.setBackground(color);
		//Color.white
		area.setBackground(color);//UIUtilities.BACKGROUND_COLOR);
	}

	/** 
	 * Deletes or edits the annotation.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case DELETE:
				firePropertyChange(AnnotationUI.REMOVE_ANNOTATION_PROPERTY,
						null, this);
				break;
			case EDIT:
				editComment();
		}
	}
	
}

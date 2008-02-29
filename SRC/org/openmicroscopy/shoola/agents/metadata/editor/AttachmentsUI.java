/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AttachmentsUI 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;

import pojos.URLAnnotationData;

/** 
 * 
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
class AttachmentsUI
	extends AnnotationUI 
	implements ActionListener
{

	/** The title associated to this component. */
	private static final String TITLE = "Related documents ";
	
	/** Action id indicating to add new file. */
	private static final String	ADD_ACTION = "add";
	
	/** Button to add a new file. */
	private JButton		addButton;
	
	/** The UI component hosting the areas. */
	private JPanel		addedContent;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		addedContent = new JPanel();
		addButton = new JButton("Attach");
		addButton.setToolTipText("Attach a document.");
		addButton.addActionListener(this);
		addButton.setActionCommand(ADD_ACTION);
	}
	
	/** Launches a file chooser to select the file to attach. */
	private void browseFile()
	{
		
	}
	
	/**
	 * Lays out the components used to add new <code>URL</code>s.
	 * 
	 * @return See above.
	 */
	private JPanel layoutAddContent()
	{
		 JPanel content = new JPanel();
		 double[][] tl = {{TableLayout.PREFERRED, 5, TableLayout.FILL}, //columns
				 {TableLayout.PREFERRED, 60} }; //rows
		 TableLayout layout = new TableLayout(tl);
		 content.setLayout(layout);
		 content.add(addButton, "0, 0, f, c");
		 JScrollPane pane = new JScrollPane(addedContent);
		 pane.setOpaque(false);
		 pane.setBorder(null);
		 content.add(pane, "2, 0, 2, 1");
		 return content;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	AttachmentsUI(EditorModel model)
	{
		super(model);
		title = TITLE;
		initComponents();
	}
	
	/**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		int n = model.getAttachmentsCount();
		title = TITLE+LEFT+n+RIGHT;
		Border border = new TitledLineBorder(title, getBackground());
		setBorder(border);
		getCollapseComponent().setBorder(border);
		if (n == 0) {
			add(layoutAddContent());
			return;
		} 
		
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }
	
	/**
	 * Adds the selected annotation to the collection of elements to remove.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();
		if (ADD_ACTION.equals(s)) {
			browseFile();
		} else {
			
		}
	}
}

/*
 * org.openmicroscopy.shoola.agents.metadata.editor.URLComponent 
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
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.URLAnnotationData;

//Third-party libraries

//Application-internal dependencies

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
 * @since 3.0-Beta4
 */
class URLComponent 
	extends JPanel
{

	/** The annotation hosted by this component. */
	private URLAnnotationData 	data;
	
	/** Reference to the model. */
	private EditorModel			model;
	
	/** Button to delete the attachment. */
	private JButton				deleteButton;
	
	/** Component displaying the file name. */
	private JLabel				label;
	
	/** Browses the url */
	private void browse()
	{
		if (data == null) return;
		MetadataViewerAgent.getRegistry().getTaskBar().openURL(data.getURL());
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		if (model.isCurrentUserOwner(data)) {
			IconManager icons = IconManager.getInstance();
			deleteButton = new JButton(icons.getIcon(IconManager.MINUS));
			UIUtilities.unifiedButtonLookAndFeel(deleteButton);
			deleteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
			deleteButton.setToolTipText("Remove the URL.");
			deleteButton.addActionListener(new ActionListener() {
			
				/**
				 * Fires a property change to delete the links.
				 * @see ActionListener#actionPerformed(ActionEvent)
				 */
				public void actionPerformed(ActionEvent e) {
					firePropertyChange(AnnotationUI.DELETE_ANNOTATION_PROPERTY,
							null, data);
			
				}
			
			});
		}
		label = new JLabel();
		label.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		if (data == null) {
			label.setText(AnnotationUI.DEFAULT_TEXT);
		} else {
			label = new JLabel(UIUtilities.formatURL(data.getURL()));
			label.setToolTipText("Added: "+model.formatDate(data));
		}
		label.addMouseListener(new MouseAdapter() {
		
			/** 
			 * Browses the url.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e)
			{
				browse();
			}
		
			/** 
			 * Modified the cursor.
			 * @see MouseAdapter#mouseEntered(MouseEvent)
			 */
			public void mouseEntered(MouseEvent e) {
				JLabel l = (JLabel) e.getSource();
				l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			
		});
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(label);
		if (deleteButton != null)
			add(deleteButton);
	}
	
	/**
	 * Creates a new instance,
	 * 
	 * @param data	The annotation. 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	URLComponent(URLAnnotationData data, EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No Model.");
		this.model = model;
		this.data = data;
		initComponents();
		buildGUI();
	}
	
}

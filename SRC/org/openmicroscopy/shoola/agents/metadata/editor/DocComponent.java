/*
 * org.openmicroscopy.shoola.agents.metadata.editor.DocComponent 
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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;


//Third-party libraries
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.DataObjectListCellRenderer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.AnnotationData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.TagAnnotationData;

/** 
 * Component displaying the annotation.
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
class DocComponent 
	extends JPanel
{

	/** The annotation hosted by this component. */
	private Object		data;
	
	/** Reference to the model. */
	private EditorModel	model;
	
	/** Button to delete the attachment. */
	private JButton		deleteButton;
	
	/** Component displaying the file name. */
	private JLabel		label;
	
	/** Flag indicating if the element has been added. */
	private boolean		added;
	
	/**
	 * Formats the passed annotation.
	 * 
	 * @param annotation The value to format.
	 * @return See above.
	 */
	private String formatTootTip(AnnotationData annotation)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body>");
		ExperimenterData exp = null;
		if (annotation.getId() > 0)
			exp = model.getOwner(annotation);
		if (exp != null) {
			buf.append("<b>");
			buf.append("Owner: ");
			buf.append("</b>");
			buf.append(EditorUtil.formatExperimenter(exp));
			buf.append("<br>");
		}
		
		if (data instanceof FileAnnotationData) {
			if (annotation.getId() > 0) {
				buf.append("<b>");
				buf.append("Date Added: ");
				buf.append("</b>");
				buf.append(UIUtilities.formatWDMYDate(
						annotation.getLastModified()));
				buf.append("<br>");
				buf.append("<b>");
			}
			
			buf.append("Size: ");
			buf.append("</b>");
			buf.append(UIUtilities.formatFileSize(
					((FileAnnotationData) annotation).getFileSize()));
			buf.append("<br>");
		}
		buf.append("</body></html>");
		return buf.toString();
	}
	
	/** 
	 * Posts an event on the eventBus, with the attachment file's ID, name etc.
	 */
	private void postFileClicked()
	{
		if (data == null) return;
		if (data instanceof FileAnnotationData) {
			FileAnnotationData f = (FileAnnotationData) data;
			Registry reg = MetadataViewerAgent.getRegistry();		
			reg.getEventBus().post(new EditFileEvent(f.getFileName(), 
					f.getFileID(), f.getFileSize()));
		}
	}
	
	/** Fires a property to delete the attachment. */
	private void delete()
	{
		firePropertyChange(AnnotationUI.REMOVE_ANNOTATION_PROPERTY,
				null, this);
	}
	
	/** Initializes the {@link #deleteButton}. */
	private void initButton()
	{
		IconManager icons = IconManager.getInstance();
		deleteButton = new JButton(icons.getIcon(IconManager.MINUS));
		UIUtilities.unifiedButtonLookAndFeel(deleteButton);
		deleteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		deleteButton.setToolTipText("Remove the attachment.");
		deleteButton.addActionListener(new ActionListener() {
		
			/**
			 * Fires a property change to delete the attachment.
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) { delete(); }
		
		});
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		if (model.isCurrentUserOwner(data)) initButton();
		label = new JLabel();
		label.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		if (data == null) {
			label.setText(AnnotationUI.DEFAULT_TEXT);
		} else {
			if (data instanceof FileAnnotationData) {
				FileAnnotationData f = (FileAnnotationData) data;
				label.setToolTipText(formatTootTip(f));
				label.setText(EditorUtil.getPartialName(
						f.getFileName()));
				initButton();
				if (f.getId() < 0)
					label.setForeground(
						DataObjectListCellRenderer.NEW_FOREGROUND_COLOR);
			} else if (data instanceof File) {
				initButton();
				File f = (File) data;
				label.setText(f.getName());
				label.setForeground(Color.BLUE);
			} else if (data instanceof TagAnnotationData) {
				TagAnnotationData tag = (TagAnnotationData) data;
				label.setText(tag.getTagValue());
				initButton();
				if (tag.getId() < 0)
					label.setForeground(
						DataObjectListCellRenderer.NEW_FOREGROUND_COLOR);
			}
		}
			
		label.addMouseListener(new MouseAdapter() {
		
			/** 
			 * Posts an event to edit the file.
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent e)
			{
				if (e.getClickCount() == 2) postFileClicked();
			}
		
		});
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		add(label);
		if (deleteButton != null) {
			JToolBar bar = new JToolBar();
			bar.setBackground(UIUtilities.BACKGROUND_COLOR);
			bar.setFloatable(false);
			bar.setRollover(true);
			bar.setBorder(null);
			bar.setOpaque(true);
			bar.add(deleteButton);
			add(bar);
		}
	}
	
	/**
	 * Creates a new instance,
	 * 
	 * @param data	The document annotation. 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param added Pass <code>true</code> to indicate that the document is 
	 * 				added, <code>false</code> otherwise.
	 */
	DocComponent(Object data, EditorModel model, boolean added)
	{
		if (model == null)
			throw new IllegalArgumentException("No Model.");
		this.model = model;
		this.data = data;
		this.added = added;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Returns the object hosted by this component.
	 * 
	 * @return See above.
	 */
	Object getData() { return data; }
	
	/**
	 * Returns <code>true</code> if the component has been added, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isAdded() { return added; }
	
	
}

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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

//Third-party libraries


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.DataObjectListCellRenderer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;

/** 
 * Component displaying the annotation, either <code>FileAnnotationData</code>
 * or <code>TagAnnotationData</code>.
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
	implements ActionListener, PropertyChangeListener
{

	/** Action id to delete the annotation. */
	private static final int DELETE = 0;
	
	/** Action id to edit the annotation. */
	private static final int EDIT = 1;
	
	/** The annotation hosted by this component. */
	private Object		data;
	
	/** Reference to the model. */
	private EditorModel	model;
	
	/** Button to delete the annotation. */
	private JButton		deleteButton;
	
	/** Button to edit the annotation. */
	private JButton		editButton;
	
	/** Component displaying the file name. */
	private JLabel		label;
	
	/** The location of the mouse click. */
	private Point		popupPoint;
	
	/** The original description of the tag. */
	private String		originalDescription;
	
	/** The original description of the tag. */
	private String		originalName;
	
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
		} else if (data instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) data;
			exp = MetadataViewerAgent.getUserDetails();
			String description = tag.getTagDescription();
			List l;
			Iterator j;
			if (tag.getId() > 0) {
				List descriptions = tag.getTagDescriptions();
				if (descriptions != null && descriptions.size() > 0) {
					Iterator i = descriptions.iterator();
					TextualAnnotationData desc;
					ExperimenterData owner;
					while (i.hasNext()) {
						desc = (TextualAnnotationData) i.next();
						if (desc != null) {
							owner = desc.getOwner();
							buf.append("<b>Described by: ");
							buf.append(EditorUtil.formatExperimenter(owner));
							buf.append("</b><br>");
							if (owner.getId() == exp.getId() && 
									description != null)
								l = UIUtilities.wrapStyleWord(description);
							else l = UIUtilities.wrapStyleWord(desc.getText());
							if (l != null) {
								j = l.iterator();
								while (j.hasNext()) {
									buf.append((String) j.next());
									buf.append("<br>");
								}
							}
						}
					}
				}
			} else { //new tag
				buf.append("<b>Described by: ");
				buf.append(EditorUtil.formatExperimenter(exp));
				buf.append("</b><br>");
				l = UIUtilities.wrapStyleWord(description);
				if (l != null) {
					j = l.iterator();
					while (j.hasNext()) {
						buf.append((String) j.next());
						buf.append("<br>");
					}
				}
				
			}
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
			reg.getEventBus().post(new EditFileEvent(f));
		}
	}
	
	/** Initializes the {@link #deleteButton}. */
	private void initButton()
	{
		IconManager icons = IconManager.getInstance();
		deleteButton = new JButton(icons.getIcon(IconManager.MINUS_9));
		UIUtilities.unifiedButtonLookAndFeel(deleteButton);
		deleteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		if (data instanceof FileAnnotationData)
			deleteButton.setToolTipText("Remove the attachment.");
		else if (data instanceof TagAnnotationData) {
			deleteButton.setToolTipText("Remove the Tag.");
			editButton = new JButton(icons.getIcon(IconManager.EDIT_12));
			editButton.setOpaque(false);
			UIUtilities.unifiedButtonLookAndFeel(editButton);
			editButton.setBackground(UIUtilities.BACKGROUND_COLOR);
			editButton.setToolTipText("Add or Edit the description.");
			
			editButton.setActionCommand(""+EDIT);
			editButton.addActionListener(this);
			editButton.addMouseListener(new MouseAdapter() {
				
				/** 
				 * Sets the location of the mouse click.
				 * @see MouseAdapter#mousePressed(MouseEvent)
				 */
				public void mousePressed(MouseEvent e)
				{
					popupPoint = e.getPoint();
				}
			
			});
		}
			
		deleteButton.addActionListener(this);
		deleteButton.setActionCommand(""+DELETE);
		
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
				label.setToolTipText(formatTootTip(tag));
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
		JToolBar bar = new JToolBar();
		bar.setBackground(UIUtilities.BACKGROUND_COLOR);
		bar.setFloatable(false);
		bar.setRollover(true);
		bar.setBorder(null);
		bar.setOpaque(true);
		if (editButton != null) bar.add(editButton);
		if (deleteButton != null) bar.add(deleteButton);
		if (bar.getComponentCount() > 0) add(bar);
	}
	
	/** Adds or edits the description of the tag. */
	private void editDescription()
	{
		TagAnnotationData tag = (TagAnnotationData) data;
		String text = model.getTagDescription(tag);
		originalDescription = text;
		originalName = tag.getTagValue();
		SwingUtilities.convertPointToScreen(popupPoint, this);
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		EditorDialog d = new EditorDialog(f, tag, false, 
				EditorDialog.EDIT_TYPE);
		d.addPropertyChangeListener(this);
		d.setOriginalDescription(originalDescription);
		d.setSize(300, 250);
		UIUtilities.showOnScreen(d, popupPoint);
		/*
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		String title = "Add or Edit description";
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.EDIT_48);
		SwingUtilities.convertPointToScreen(popupPoint, this);
		InputDialog d = new InputDialog(f, title, text, icon);
		int option = d.showMsgBox(popupPoint);
		if (option == InputDialog.SAVE) {
			String txt = d.getText();
			if (txt != null && txt.length() > 0) {
				tag.setTagDescription(txt);
				firePropertyChange(AnnotationUI.EDIT_TAG_PROPERTY, null, this);
			}
		} else if (option == InputDialog.CANCEL) 
			originalText = null;
			*/
	}
	
	/**
	 * Creates a new instance,
	 * 
	 * @param data	The document annotation. 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	DocComponent(Object data, EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No Model.");
		originalDescription = null;
		this.model = model;
		this.data = data;
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
	 * Returns <code>true</code> if the description of the tag has been 
	 * modified, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasBeenModified()
	{
		if (originalName == null) return false;
		if (data instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) data;
			if (!originalName.equals(tag.getTagValue())) return true;
			String txt = tag.getTagDescription();
			if (txt != null) 
				return !(originalDescription.equals(txt));	
			return false;
		}
		return false;
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
				editDescription();
		}
	}

	/**
	 * Listens to property fired by the Editor dialog.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (EditorDialog.CREATE_NO_PARENT_PROPERTY.equals(name)) {
			//reset text and tooltip
			TagAnnotationData tag = (TagAnnotationData) data;
			label.setToolTipText(formatTootTip(tag));
			label.setText(tag.getTagValue());
			firePropertyChange(AnnotationUI.EDIT_TAG_PROPERTY, null, this);
		} 
	}
	
	
}

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

//Third-party libraries


//Application-internal dependencies
import omero.model.OriginalFile;

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.events.editor.EditFileEvent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.DataObjectListCellRenderer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.BMPFilter;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

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

	/** Flag indicates to load the image from the server. */
	static final int		LOAD_FROM_SERVER = 0;
	
	/** Flag indicates to load the image from the local machine. */
	static final int		LOAD_FROM_LOCAL = 1;
	
	/** Action id to delete the annotation. */
	private static final int DELETE = 0;
	
	/** Action id to edit the annotation. */
	private static final int EDIT = 1;
	
	/** Action id to download the file. */
	private static final int DOWNLOAD = 2;
	
	/** Action id to edit the annotation. */
	private static final int OPEN = 3;
	
	/** Collection of filters supported. */
	private static final List<CustomizedFileFilter> FILTERS;
		
	static {
		FILTERS = new ArrayList<CustomizedFileFilter>();
		FILTERS.add(new TIFFFilter());
		FILTERS.add(new JPEGFilter());
		FILTERS.add(new PNGFilter());
		FILTERS.add(new BMPFilter());
	}
	
	/** The annotation hosted by this component. */
	private Object		data;
	
	/** Reference to the model. */
	private EditorModel	model;
	
	/** Button to delete the annotation. */
	private JButton		deleteButton;
	
	/** Button to edit the annotation. */
	private JButton		editButton;
	
	/** Button to download the file linked to the annotation. */
	private JButton		downloadButton;
	
	/** Button to open the file linked to the annotation. */
	private JButton		openButton;
	
	/** Component displaying the file name. */
	private JLabel		label;
	
	/** The location of the mouse click. */
	private Point		popupPoint;
	
	/** The original description of the tag. */
	private String		originalDescription;
	
	/** The original description of the tag. */
	private String		originalName;
	
	/** 
	 * Index indicating that the attachment is an image that
	 * can be displayed as a thumbnail e.g. TIFF, JPEG, PNG, etc.
	 */
	private int 		imageToLoad;
	
	/** 
	 * The thumbnail corresponding to the attachment, <code>null</code>
	 * if the attachment is not a supported image.
	 */
	private Icon 		thumbnail;
	
	/** Opens the file. */
	private void openFile()
	{
		if (!(data instanceof FileAnnotationData)) return;
		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		bus.post(new EditFileEvent((FileAnnotationData) data));
	}
	
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
			String ns = ((FileAnnotationData) data).getNameSpace();
			if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(ns)) {
				buf.append("<b>");
				buf.append("Editor File: ");
				buf.append("</b>");
				buf.append("Experiment");
				buf.append("<br>");
				buf.append("<b>");
			} else if (FileAnnotationData.EDITOR_PROTOCOL_NS.equals(ns)) {
				buf.append("<b>");
				buf.append("Editor File: ");
				buf.append("</b>");
				buf.append("Protocol");
				buf.append("<br>");
				buf.append("<b>");
			}
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
			//size not kb
			long size = ((FileAnnotationData) annotation).getFileSize();
			buf.append(UIUtilities.formatFileSize(size/1000));
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
		deleteButton = new JButton(icons.getIcon(IconManager.MINUS_12));
		UIUtilities.unifiedButtonLookAndFeel(deleteButton);
		deleteButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		deleteButton.addActionListener(this);
		deleteButton.setActionCommand(""+DELETE);
		if (data instanceof FileAnnotationData) {
			FileAnnotationData fa = (FileAnnotationData) data;
			deleteButton.setToolTipText("Remove the attachment.");
			
			if (fa.getId() > 0) {
				downloadButton = new JButton(icons.getIcon(
						IconManager.DOWNLOAD_12));
				downloadButton.setOpaque(false);
				UIUtilities.unifiedButtonLookAndFeel(downloadButton);
				downloadButton.setBackground(UIUtilities.BACKGROUND_COLOR);
				downloadButton.setToolTipText("Download the selected file.");
				downloadButton.setActionCommand(""+DOWNLOAD);
				downloadButton.addActionListener(this);
				
				String ns = fa.getNameSpace();
				if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(ns) ||
						FileAnnotationData.EDITOR_PROTOCOL_NS.equals(ns) ||
						FileAnnotationData.COMPANION_FILE_NS.equals(ns)) {
					openButton = new JButton(icons.getIcon(
							IconManager.EDITOR_12));
					openButton.setOpaque(false);
					UIUtilities.unifiedButtonLookAndFeel(openButton);
					openButton.setBackground(UIUtilities.BACKGROUND_COLOR);
					openButton.setToolTipText("Open the file in the editor.");
					openButton.setActionCommand(""+OPEN);
					openButton.addActionListener(this);
				} 
				if (FileAnnotationData.COMPANION_FILE_NS.equals(ns) ||
					FileAnnotationData.MEASUREMENT_NS.equals(ns))
					deleteButton = null;
			}
		} else if (data instanceof TagAnnotationData) {
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
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		imageToLoad = -1;
		if (model.isCurrentUserOwner(data)) initButton();
		label = new JLabel();
		label.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		if (data == null) {
			label.setText(AnnotationUI.DEFAULT_TEXT);
		} else {
			if (data instanceof FileAnnotationData) {
				FileAnnotationData f = (FileAnnotationData) data;
				String fileName = f.getFileName();
				label.setToolTipText(formatTootTip(f));
				if (FileAnnotationData.MEASUREMENT_NS.equals(fileName))
					label.setText(f.getDescription());
				else label.setText(EditorUtil.getPartialName(fileName));
					
				Iterator<CustomizedFileFilter> i = FILTERS.iterator();
				CustomizedFileFilter filter;
				long id = f.getId();
				while (i.hasNext()) {
					filter = i.next();
					if (filter.accept(fileName)) {
						if (id > 0) imageToLoad = LOAD_FROM_SERVER;
						else  imageToLoad = LOAD_FROM_LOCAL;
						break;
					}
				}
				initButton();
				if (id < 0)
					label.setForeground(
						DataObjectListCellRenderer.NEW_FOREGROUND_COLOR);
				switch (imageToLoad) {
					case LOAD_FROM_LOCAL:
						if (thumbnail == null) setThumbnail(f.getFilePath());
						break;
					case LOAD_FROM_SERVER:
						if (thumbnail == null) {
							//Load the file, copy locally and delete it.
							//model.loadFile((FileAnnotationData) data, this);
						}
				}
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
		if (downloadButton != null) bar.add(downloadButton);
		if (openButton != null) bar.add(openButton);
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
	}
	
	/** 
	 * Brings up a dialog so that the user can select where to 
	 * download the file.
	 */
	private void download()
	{
		String name = null;
		if (data instanceof FileAnnotationData) {
			name = ((FileAnnotationData) data).getFileName();
		}
		JFrame f = EditorAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
				"Download", "Select where to download the file.", null, true);
		if (name != null && name.trim().length() > 0) 
			chooser.setSelectedFileFull(name);
		IconManager icons = IconManager.getInstance();
		chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
		chooser.setApproveButtonText("Download");
		chooser.addPropertyChangeListener(this);
		chooser.centerDialog();
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
	 * Returns <code>true</code> if the image has been loaded, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isImageLoaded() { return thumbnail != null; }
	
	/**
	 * Sets the image representing the file.
	 * 
	 * @param path The path to the file.
	 */
	void setThumbnail(String path)
	{
		if (path == null) return;
		this.thumbnail = Factory.createIcon(path, 
				Factory.THUMB_DEFAULT_WIDTH/2, 
				Factory.THUMB_DEFAULT_HEIGHT/2);
		if (thumbnail != null) {
			label.setText("");
			label.setIcon(thumbnail);
			label.repaint();
			revalidate();
			repaint();
		}
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
				break;
			case DOWNLOAD:
				download();
				break;
			case OPEN:
				openFile();
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
		} else if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File folder = (File) evt.getNewValue();
			if (folder == null)
				folder = UIUtilities.getDefaultFolder();
			UserNotifier un = EditorAgent.getRegistry().getUserNotifier();
			if (data == null) return;
			FileAnnotationData fa = (FileAnnotationData) data;
			OriginalFile f = (OriginalFile) fa.getContent();
			IconManager icons = IconManager.getInstance();
			
			DownloadActivityParam activity = new DownloadActivityParam(f,
					folder, icons.getIcon(IconManager.DOWNLOAD_22));
			un.notifyActivity(activity);
			//un.notifyDownload((FileAnnotationData) data, folder);
		}
	}
	
}

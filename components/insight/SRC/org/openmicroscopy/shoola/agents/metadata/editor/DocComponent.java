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
import java.awt.Dimension;
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
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
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
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;
import pojos.AnnotationData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.TagAnnotationData;

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
	
	/** Action id to unlink the annotation. */
	private static final int UNLINK = 0;
	
	/** Action id to edit the annotation. */
	private static final int EDIT = 1;
	
	/** Action id to download the file. */
	private static final int DOWNLOAD = 2;
	
	/** Action id to open the annotation. */
	private static final int OPEN = 3;
	
	/** Action id to open the annotation. */
	private static final int DELETE = 4;
	
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
	
	/** Button to unlink the annotation. */
	private JMenuItem	unlinkButton;
	
	/** Button to edit the annotation. */
	private JMenuItem		editButton;
	
	/** Button to download the file linked to the annotation. */
	private JMenuItem		downloadButton;
	
	/** Button to open the file linked to the annotation. */
	private JMenuItem		openButton;
	
	/** Button to delete the file annotation. */
	private JMenuItem		deleteButton;
	
	/** Button to display information. */
	private JMenuItem		infoButton;
	
	/** Component displaying the file name. */
	private JLabel		label;
	
	/** The location of the mouse click. */
	private Point		popupPoint;
	
	/** The original description of the tag. */
	private String		originalDescription;
	
	/** The original description of the tag. */
	private String		originalName;
	
	/** The Button used to display the managing option. */
	private JButton		menuButton;
	
	/** 
	 * The component used to display the summary of the protocol or
	 * experiment.
	 */
	private PreviewPanel	preview;
	
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
	
	/** The pop-up menu. */
	private JPopupMenu	popMenu;
	
	/** Flag indicating if the node can be deleted. */
	private boolean		deletable;
	
	/**
	 * Enables or disables the various buttons depending on the passed value.
	 * Returns <code>true</code> if some controls are visible, 
	 * <code>false</code> otherwise.
	 * 
	 * @param enabled 	Pass <code>true</code> to enable the controls,
	 * 					<code>false</code> otherwise.
	 * @return See above.
	 */
	private boolean setControlsEnabled(boolean enabled)
	{
		boolean b = enabled;
		boolean link = enabled;
		int count = 0;
		if (enabled && data != null) {
			b = model.isUserOwner(data);
			link = model.isLinkOwner(data);
		}
		if (unlinkButton != null) {
			unlinkButton.setEnabled(link);
			unlinkButton.setVisible(link);
			if (link) count++;
		} 
		
		if (editButton != null) {
			editButton.setEnabled(b);
			editButton.setVisible(b);
			if (b) count++;
		}
		if (downloadButton != null) {
			downloadButton.setEnabled(link);
			downloadButton.setVisible(link);
			if (link) count++;
		}
		if (openButton != null) {
			//openButton.setEnabled(enabled);
			//openButton.setVisible(enabled);
			openButton.setEnabled(link);
			openButton.setVisible(link);
			if (link) count++;
		}
		if (infoButton != null) {
			infoButton.setEnabled(true);
			infoButton.setVisible(true);
			if (link) count++;
		}
		if (deleteButton != null) {
			deleteButton.setEnabled(b);
			deleteButton.setVisible(b);
			if (b) count++;
		}
		return count > 0;
	}
	
	/** Opens the file. */
	private void openFile()
	{
		if (!(data instanceof FileAnnotationData)) return;
		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
		bus.post(new EditFileEvent((FileAnnotationData) data));
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
			if (editButton != null) popMenu.add(editButton);
			if (unlinkButton != null) popMenu.add(unlinkButton);
			if (downloadButton != null) popMenu.add(downloadButton);
			if (openButton != null) popMenu.add(openButton);
			if (deleteButton != null) popMenu.add(deleteButton);
			if (infoButton != null) popMenu.add(infoButton);
		}
		popMenu.show(invoker, p.x, p.y);
	}
	
	/**
	 * Displays information about the attachment.
	 * 
	 * @param invoker The component where the clicks occurred.
	 * @param p The location of the mouse pressed.
	 */
	private void displayInformation(JComponent invoker, Point p)
	{
		String text = label.getToolTipText();
		if (text == null || text.trim().length() == 0) return;
		JComponent comp;
		if (preview != null) {
			comp = preview;
		} else {
			JLabel l = new JLabel();
			l.setText(text);
			comp = l;
		}
		
		TinyDialog d = new TinyDialog(null, comp, TinyDialog.CLOSE_ONLY);
		d.setModal(true);
		d.getContentPane().setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
		SwingUtilities.convertPointToScreen(p, invoker);
		d.pack();
		d.setLocation(p);
		d.setVisible(true);
	}
	
	/**
	 * Adds the experimenters who use the annotation if any.
	 * 
	 * @param buf The buffer
	 * @param annotation The annotation to handle.
	 */
	private void checkAnnotators(StringBuffer buf, AnnotationData annotation)
	{
		List<ExperimenterData> annotators = model.getAnnotators(annotation);
		if (annotators.size() == 0) return;
		Iterator<ExperimenterData> i = annotators.iterator();
		ExperimenterData annotator;
		buf.append("<b>Added by:</b><br>");
		while (i.hasNext()) {
			annotator =  i.next();
			buf.append(EditorUtil.formatExperimenter(annotator)+"<br>");
		}
		if (annotators.size() > 1) {
			String text = label.getText();
			text += " ["+annotators.size()+"]";
			label.setText(text);
		}
	}
	
	/**
	 * Formats the passed annotation.
	 * 
	 * @param annotation The value to format.
	 * @param name The full name.
	 * @return See above.
	 */
	private String formatTootTip(AnnotationData annotation, String name)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body>");
		ExperimenterData exp = null;
		if (name != null) {
			buf.append("<b>");
			buf.append("Name: ");
			buf.append("</b>");
			buf.append(name);
			buf.append("<br>");
		}
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
			if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(ns) ||
				FileAnnotationData.EDITOR_PROTOCOL_NS.equals(ns)) {
				FileAnnotationData fa = (FileAnnotationData) data;
				preview = new PreviewPanel(fa.getDescription(), fa.getId());
				buf.append("<b>");
				if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(ns))
					buf.append("Experiment Description: ");
				else buf.append("Protocol Description: ");
				buf.append("</b>");
				List<String> values = preview.getFormattedDesciption();
				Iterator<String> i = values.iterator();
				while (i.hasNext()) {
					buf.append(i.next());
					buf.append("<br>");
				}
				buf.append("<b>");
			} 
			if (annotation.getId() > 0) {
				buf.append("<b>");
				buf.append("File ID: ");
				buf.append("</b>");
				FileAnnotationData fa = (FileAnnotationData) data;
				buf.append(fa.getFileID());
				buf.append("<br>");
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
			buf.append(UIUtilities.formatFileSize(size));
			buf.append("<br>");
			checkAnnotators(buf, annotation);
		} else if (data instanceof TagAnnotationData) {
			checkAnnotators(buf, annotation);
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
	
	/**
	 * Confirms with the user that he/she wants to delete the attachment.
	 * 
	 * @param location The location of the mouse pressed.
	 */
	private void deleteDocument(Point location)
	{
		JFrame f = 
			MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		MessageBox box = new MessageBox(f, "Delete attachment",
				"Are you sure you want to delete the attachment?");
		Dimension d = box.getSize();
		if (location != null) {
			Point p = new Point(location.x-d.width, location.y);
			if (box.showMsgBox(p) == MessageBox.YES_OPTION)
				firePropertyChange(AnnotationUI.DELETE_ANNOTATION_PROPERTY,
					null, this);
		} else {
			if (box.centerMsgBox() == MessageBox.YES_OPTION)
				firePropertyChange(AnnotationUI.DELETE_ANNOTATION_PROPERTY,
					null, this);
		}
	}
	
	/** Initializes the various buttons. */
	private void initButtons()
	{
		IconManager icons = IconManager.getInstance();
		menuButton = new JButton(icons.getIcon(IconManager.UP_DOWN_9_12));
		UIUtilities.unifiedButtonLookAndFeel(menuButton);
		menuButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		menuButton.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e)
			{
				Point p = e.getPoint();
				showMenu(menuButton, p);
			}
		});
		infoButton = new JMenuItem(icons.getIcon(IconManager.INFO));
		infoButton.setText("Info...");
		infoButton.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e)
			{
				Point p = e.getPoint();
				displayInformation(label, p);
			}
		});
		unlinkButton = new JMenuItem(icons.getIcon(IconManager.MINUS_12));
		unlinkButton.setText("Unlink");
		//UIUtilities.unifiedButtonLookAndFeel(unlinkButton);
		//unlinkButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		unlinkButton.addActionListener(this);
		unlinkButton.setActionCommand(""+UNLINK);
		if (data instanceof FileAnnotationData) {
			FileAnnotationData fa = (FileAnnotationData) data;
			unlinkButton.setToolTipText("Remove the attachment.");
			
			if (fa.getId() > 0) {
				if (deletable) {
					deleteButton = new JMenuItem(icons.getIcon(
							IconManager.DELETE_12));
					deleteButton.setText("Delete");
					//deleteButton.addActionListener(this);
					//deleteButton.setActionCommand(""+DELETE);
					deleteButton.addMouseListener(new MouseAdapter() {
						
						public void mousePressed(MouseEvent e) {
							Point p = e.getPoint();
							SwingUtilities.convertPointToScreen(p, 
									menuButton);
							deleteDocument(p);
						}
					});
				}
				downloadButton = new JMenuItem(icons.getIcon(
						IconManager.DOWNLOAD_12));
				downloadButton.setText("Download...");
				downloadButton.setToolTipText("Download the selected file.");
				downloadButton.setActionCommand(""+DOWNLOAD);
				downloadButton.addActionListener(this);
				
				String ns = fa.getNameSpace();
				//if (FileAnnotationData.EDITOR_EXPERIMENT_NS.equals(ns) ||
					//	FileAnnotationData.EDITOR_PROTOCOL_NS.equals(ns) ||
					//	FileAnnotationData.COMPANION_FILE_NS.equals(ns)) {
					openButton = new JMenuItem(icons.getIcon(
							IconManager.VIEW_DOC_12));
					openButton.setText("View");
					openButton.setToolTipText("View the file.");
					openButton.setActionCommand(""+OPEN);
					openButton.addActionListener(this);
				//} 
				if (FileAnnotationData.COMPANION_FILE_NS.equals(ns) ||
					FileAnnotationData.MEASUREMENT_NS.equals(ns))
					unlinkButton = null;
			}
		} else if (data instanceof TagAnnotationData) {
			unlinkButton.setToolTipText("Remove the Tag.");
			editButton = new JMenuItem(icons.getIcon(IconManager.EDIT_12));
			editButton.setText("Edit");
			//editButton.setOpaque(false);
			//UIUtilities.unifiedButtonLookAndFeel(editButton);
			//editButton.setBackground(UIUtilities.BACKGROUND_COLOR);
			//editButton.setToolTipText("Add or Edit the description.");
			
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
		if (model.isUserOwner(data)) 
			initButtons();
		label = new JLabel();
		label.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		if (data == null) {
			label.setText(AnnotationUI.DEFAULT_TEXT);
		} else {
			if (data instanceof FileAnnotationData) {
				FileAnnotationData f = (FileAnnotationData) data;
				String fileName = f.getFileName();
				String s = fileName;
				if (FileAnnotationData.MEASUREMENT_NS.equals(fileName)) {
					label.setText(f.getDescription());
					s = label.getText();
				} else label.setText(UIUtilities.formatPartialName(
						EditorUtil.getPartialName(fileName)));
				label.setToolTipText(formatTootTip(f, s));
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
				initButtons();
				if (id < 0)
					label.setForeground(
						DataObjectListCellRenderer.NEW_FOREGROUND_COLOR);
				switch (imageToLoad) {
					case LOAD_FROM_LOCAL:
						if (thumbnail == null) setThumbnail(f.getFilePath());
						break;
						/*
					case LOAD_FROM_SERVER:
						if (thumbnail == null) {
							model.loadFile((FileAnnotationData) data, this);
						}
						*/
				}
			} else if (data instanceof File) {
				initButtons();
				File f = (File) data;
				label.setText(EditorUtil.getPartialName(f.getName()));
				label.setForeground(Color.BLUE);
			} else if (data instanceof TagAnnotationData) {
				TagAnnotationData tag = (TagAnnotationData) data;
				label.setText(tag.getTagValue());
				label.setToolTipText(formatTootTip(tag, null));
				initButtons();
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
				if (e.getClickCount() == 1) {
					if (e.isPopupTrigger()) showMenu(label, e.getPoint());
				} else if (e.getClickCount() == 2) postFileClicked();
			}
			
			/** 
			 * Shows menu
			 * @see MouseAdapter#mouseReleased(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger()) showMenu(label, e.getPoint());
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
		boolean b = setControlsEnabled(data != null);
		int count = 0;
		if (editButton != null) count++;
		if (unlinkButton != null) count++;
		if (downloadButton != null) count++;
		if (infoButton != null) count++;
		if (openButton != null) count++;
		if (deleteButton != null) count++;
		if (count > 0) {
			bar.add(menuButton);
			if (!b) bar.add(Box.createHorizontalStrut(8));
			add(bar);
		}
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
	 * @param deletable Pass <code>false</code> to indicate that the document
	 *					cannot be deleted regardless of the permissions,
	 *					<code>true</code> otherwise.
	 */
	DocComponent(Object data, EditorModel model, boolean deletable)
	{
		if (model == null)
			throw new IllegalArgumentException("No Model.");
		originalDescription = null;
		this.model = model;
		this.data = data;
		this.deletable = deletable;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Creates a new instance,
	 * 
	 * @param data	The document annotation. 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	DocComponent(Object data, EditorModel model)
	{
		this(data, model, true);
	}
	
	/**
	 * Returns <code>true</code> if a thumbnail has to be loaded,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasThumbnailToLoad()
	{
		return false;//(imageToLoad == LOAD_FROM_SERVER && thumbnail == null);
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
	 * Returns <code>true</code> if the object can be unlinked,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean canUnlink()
	{
		if (unlinkButton == null) return false;
		return unlinkButton.isVisible();
	}
	
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
			case UNLINK:
				firePropertyChange(AnnotationUI.REMOVE_ANNOTATION_PROPERTY,
						null, this);
				break;
			case DELETE:
				JFrame f = 
					MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
				MessageBox box = new MessageBox(f, "Delete attachment",
						"Are you sure you want to delete the attachment?");
				if (box.centerMsgBox() == MessageBox.YES_OPTION)
					firePropertyChange(AnnotationUI.DELETE_ANNOTATION_PROPERTY,
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
				break;
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
			label.setText(tag.getTagValue());
			label.setToolTipText(formatTootTip(tag, null));
			firePropertyChange(AnnotationUI.EDIT_TAG_PROPERTY, null, this);
		} else if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			if (data == null) return;
			FileAnnotationData fa = (FileAnnotationData) data;
			OriginalFile f = (OriginalFile) fa.getContent();
			File folder;
			Object o = evt.getNewValue();
			if (o instanceof String) {
				String path = (String) o;
				if (!path.endsWith(File.separator)) {
					path += File.separator;
				}
				path += fa.getFileName();
				folder = new File(path);
			} else {
				File[] files = (File[]) o;
				folder = files[0];
			}
			if (folder == null)
				folder = UIUtilities.getDefaultFolder();
			UserNotifier un = EditorAgent.getRegistry().getUserNotifier();
			
			IconManager icons = IconManager.getInstance();
			
			DownloadActivityParam activity = new DownloadActivityParam(f,
					folder, icons.getIcon(IconManager.DOWNLOAD_22));
			//Check Name space
			activity.setLegend(fa.getDescription());
			un.notifyActivity(activity);
			//un.notifyDownload((FileAnnotationData) data, folder);
		}
	}
	
}

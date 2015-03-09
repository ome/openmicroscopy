/*
 * org.openmicroscopy.shoola.agents.metadata.editor.DocComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import omero.model.OriginalFile;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.DataObjectListCellRenderer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ToolTipGenerator;
import org.openmicroscopy.shoola.agents.util.editorpreview.PreviewPanel;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.BMPFilter;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import pojos.AnnotationData;
import pojos.BooleanAnnotationData;
import pojos.DataObject;
import pojos.DoubleAnnotationData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.LongAnnotationData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TimeAnnotationData;
import pojos.XMLAnnotationData;

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
	private static final int REMOVE = 0;
	
	/** Action id to edit the annotation. */
	private static final int EDIT = 1;
	
	/** Action id to download the file. */
	private static final int DOWNLOAD = 2;
	
	/** Action id to open the annotation. */
	private static final int OPEN = 3;

	/** Collection of filters supported. */
	private static final ImmutableCollection<CustomizedFileFilter> FILTERS =
	        ImmutableList.of(new TIFFFilter(), new JPEGFilter(), new PNGFilter(), new BMPFilter());

	/** The maximum length of the text to display.*/
	private static final int TEXT_LENGTH = 10;

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

	/** Flag indicating if it is a XML modulo annotation.*/
	private boolean isModulo;

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
		int count = 0;
		if (infoButton != null) {
			infoButton.setEnabled(true);
			infoButton.setVisible(true);
			count++;
		}
		if (!enabled) {
			if (unlinkButton != null) {
				unlinkButton.setEnabled(false);
				unlinkButton.setVisible(false);
			}
			if (editButton != null) {
				editButton.setEnabled(false);
				editButton.setVisible(false);
			}
			if (downloadButton != null) {
				downloadButton.setEnabled(false);
				downloadButton.setVisible(false);
			}
			if (openButton != null) {
				openButton.setEnabled(false);
				openButton.setVisible(false);
			}
			return count > 0;
		}
		boolean b = false;
		if (unlinkButton != null) {
			b = model.canDeleteLink(data);
			if (b && isModulo) { //check if it is a modulo annotation. Do not remove.
			   b = false;
			}
			unlinkButton.setEnabled(b);
			unlinkButton.setVisible(b);
			if (b) count++;
		} 
		
		if (editButton != null) {
			b = model.canEdit(data);
			editButton.setEnabled(b);
			editButton.setVisible(b);
			if (b) count++;
		}
		if (downloadButton != null) {
			b = true;
			downloadButton.setEnabled(b);
			downloadButton.setVisible(b);
			if (b) count++;
		}
		if (openButton != null) {
			b = true;
			openButton.setEnabled(b);
			openButton.setVisible(b);
			if (b) count++;
		}
		return count > 0;
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
		d.setModal(false);
		d.getContentPane().setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
		SwingUtilities.convertPointToScreen(p, invoker);
		d.pack();
		d.setLocation(p);
		d.setVisible(true);
	}
	
	/**
	 * Adds the experimenters who use the annotation if any.
	 * 
	 * @param tt The {@link ToolTipGenerator} to add the information on to
	 * @param annotation The annotation to handle.
	 */
	private void checkAnnotators(ToolTipGenerator tt, AnnotationData annotation)
	{
		List<ExperimenterData> annotators = model.getAnnotators(annotation);
		if (annotators.size() == 0) return;
		Iterator<ExperimenterData> i = annotators.iterator();
		ExperimenterData annotator;
		tt.addLine("Linked by:", true);
		while (i.hasNext()) {
			annotator =  i.next();
			tt.addLine(EditorUtil.formatExperimenter(annotator));
		}
		if (annotators.size() > 1) {
			String text = label.getText();
			text += " ["+annotators.size()+"]";
			label.setText(text);
		}
	}
	
	/**
	 * Returns the list of users who annotated that image only if the
	 * annotation cannot be unlinked.
	 * 
	 * @param object The object the annotation is linked to.
	 * @param annotation The annotation to handle.
	 */
	private String formatAnnotators(DataObject object,
			AnnotationData annotation)
	{
		StringBuffer buffer = new StringBuffer();
		List<ExperimenterData> annotators = model.getAnnotators(object, 
				annotation);
		if (annotators.size() == 0) return null;
		long userID = model.getCurrentUser().getId();
		Iterator<ExperimenterData> i = annotators.iterator();
		ExperimenterData annotator;
		int n = annotators.size()-1;
		int index = 0;
		buffer.append(" (");
		while (i.hasNext()) {
			annotator =  i.next();
			if (annotator.getId() != userID) {
				buffer.append(EditorUtil.formatExperimenter(annotator));
				if (index != n)
					buffer.append(", ");
				index++;
			}
		}
		if (index == 0) return null;
		buffer.append(")");
		return buffer.toString();
	}
	
	/**
	 * Formats the passed annotation.
	 * 
	 * @param annotation The value to format.
	 * @param name The full name.
	 * @return See above.
	 */
	private String formatToolTip(AnnotationData annotation, String name)
	{
		ToolTipGenerator tt = new ToolTipGenerator();
		if (model.isMultiSelection()) {
			Map<DataObject, Boolean> m = null;
			Entry<DataObject, Boolean> e;
			Iterator<Entry<DataObject, Boolean>> j;
			String text = "";
			if (annotation instanceof TagAnnotationData) {
				m = model.getTaggedObjects(annotation);
				text += "Can remove Tag from ";
			} else if (annotation instanceof FileAnnotationData) {
				m = model.getObjectsWith(annotation);
				text += "Can remove Attachment from ";
			} else if (annotation instanceof XMLAnnotationData) {
				m = model.getObjectsWith(annotation);
				text += "Can remove XML files from ";
			} else if (annotation instanceof TermAnnotationData) {
				m = model.getObjectsWith(annotation);
				text += "Can remove Term from ";
			}
			if (m == null) return "";
			j = m.entrySet().iterator();
			Collection<Boolean> l = m.values();
			int n = 0;
			Iterator<Boolean> k = l.iterator();
			while (k.hasNext()) {
				if (k.next().booleanValue())
					n++;
			}
			tt.addLineNoBr(text+""+n+" ");
			int index = 0;
			String s;
			while (j.hasNext()) {
				e = j.next();
				if (index == 0) {
				    tt.addLine(model.getObjectTypeAsString(e.getKey())+"s", true);
					index++;
				}
				tt.addLine("ID "+e.getKey().getId(), UIUtilities.formatPartialName(
                        model.getObjectName(e.getKey())), true);
				//Indicates who annotates the object if not the user
				//currently logged in.
				s = formatAnnotators(e.getKey(), annotation);
				if (s != null) 
				    tt.addLine(s);
			}
			return tt.toString();
		}
		
		if (name != null) {
		    tt.addLine("Name", name, true);
		}
		
		ExperimenterData exp = null;
		
		if(annotation.getId()>0) {
			exp = model.getOwner(annotation);
			tt.addLine("ID", ""+annotation.getId(), true);
		}
		
		String ns = annotation.getNameSpace();
		if(!CommonsLangUtils.isEmpty(ns) && !isInternalNS(ns)) {
		    tt.addLine("Namespace", ns, true);
		}
		
		String desc = annotation.getDescription();
		if(!CommonsLangUtils.isEmpty(desc)) {
		    tt.addLine("Description", desc, true);
		}
		
		if(exp!=null) {
		    tt.addLine("Owner", EditorUtil.formatExperimenter(exp), true);
		}
		
		Timestamp created = annotation.getCreated();
		if(created !=null) {
		    tt.addLine("Date", UIUtilities.formatShortDateTime(created), true);
		}
		
		if (data instanceof FileAnnotationData) {
		    FileAnnotationData fa = (FileAnnotationData) data;
		    long size = ((FileAnnotationData) annotation).getFileSize();
		    tt.addLine("File ID", ""+fa.getFileID(), true);
		    tt.addLine("Size", UIUtilities.formatFileSize(size)+"kb", true);
			checkAnnotators(tt, annotation);
		} else if (data instanceof TagAnnotationData || data instanceof
				XMLAnnotationData || data instanceof TermAnnotationData ||
				data instanceof LongAnnotationData ||
				data instanceof DoubleAnnotationData ||
				data instanceof BooleanAnnotationData) {
			checkAnnotators(tt, annotation);
		}
		return tt.toString();
	}

	/**
	 * Checks if the given namespace is an internal one.
	 * 
	 * @param ns
	 *            The namespace to check
	 * @return See above
	 */
	private boolean isInternalNS(String ns) {
		return ns.startsWith("openmicroscopy.org") || ns.startsWith("omero.");
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
				showMenu(menuButton, e.getPoint());
			}
		});
		infoButton = new JMenuItem(icons.getIcon(IconManager.INFO));
		infoButton.setText("Info...");
		infoButton.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e)
			{
				popMenu.setVisible(false);
				displayInformation(label, e.getPoint());
			}
		});
		unlinkButton = new JMenuItem(icons.getIcon(IconManager.MINUS_12));
		unlinkButton.setText("Remove");
		unlinkButton.addActionListener(this);
		unlinkButton.setActionCommand(""+REMOVE);
		if (data instanceof FileAnnotationData) {
			FileAnnotationData fa = (FileAnnotationData) data;
			unlinkButton.setToolTipText("Remove the attachment.");
			
			if (fa.getId() > 0) {
				unlinkButton.setEnabled(deletable);
				downloadButton = new JMenuItem(icons.getIcon(
						IconManager.DOWNLOAD_12));
				downloadButton.setText("Download...");
				downloadButton.setToolTipText("Download the selected file.");
				downloadButton.setActionCommand(""+DOWNLOAD);
				downloadButton.addActionListener(this);
				
				String ns = fa.getNameSpace();
				openButton = new JMenuItem(icons.getIcon(
						IconManager.VIEW_DOC_12));
				openButton.setText("View");
				openButton.setToolTipText("View the file.");
				openButton.setActionCommand(""+OPEN);
				openButton.addActionListener(this);
				if (FileAnnotationData.COMPANION_FILE_NS.equals(ns) ||
					FileAnnotationData.MEASUREMENT_NS.equals(ns))
					unlinkButton = null;
			}
		} else if (data instanceof TagAnnotationData ||
				data instanceof XMLAnnotationData ||
				data instanceof TermAnnotationData ||
				data instanceof LongAnnotationData ||
				data instanceof DoubleAnnotationData ||
				data instanceof BooleanAnnotationData) {
			unlinkButton.setToolTipText("Remove the annotation.");
			editButton = new JMenuItem(icons.getIcon(IconManager.EDIT_12));
			if (isModulo) editButton.setText("View");
			else editButton.setText("Edit");
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
	    isModulo = model.isModulo(data);
		imageToLoad = -1;
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
				if (FileAnnotationData.MEASUREMENT_NS.equals(f.getNameSpace())) {
					String desc = f.getDescription();
					if (desc != null && desc.trim().length() > 0)
						label.setText(desc);
					else {
						label.setText(UIUtilities.formatPartialName(
								EditorUtil.getPartialName(fileName)));
					}
					s = label.getText();
				} else {
					label.setText(UIUtilities.formatPartialName(
							EditorUtil.getPartialName(fileName)));
				}
						
				label.setToolTipText(formatToolTip(f, s));
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
				File f = (File) data;
				label.setText(EditorUtil.getPartialName(f.getName()));
				label.setForeground(Color.BLUE);
			} else if (data instanceof TagAnnotationData) {
				TagAnnotationData tag = (TagAnnotationData) data;
				label.setText(tag.getTagValue());
				label.setToolTipText(formatToolTip(tag, null));
				if (tag.getId() < 0)
					label.setForeground(
						DataObjectListCellRenderer.NEW_FOREGROUND_COLOR);
			} else if (data instanceof XMLAnnotationData) {
				XMLAnnotationData tag = (XMLAnnotationData) data;
				label.setText(EditorUtil.truncate(tag.getText(), TEXT_LENGTH,
				        false));
				label.setToolTipText(formatToolTip(tag, null));
				if (tag.getId() < 0)
					label.setForeground(
						DataObjectListCellRenderer.NEW_FOREGROUND_COLOR);
			} else if (data instanceof TermAnnotationData) {
				TermAnnotationData tag = (TermAnnotationData) data;
				label.setText(tag.getTerm());
				label.setToolTipText(formatToolTip(tag, null));
				if (tag.getId() < 0)
					label.setForeground(
						DataObjectListCellRenderer.NEW_FOREGROUND_COLOR);
			} else if (data instanceof LongAnnotationData) {
				LongAnnotationData tag = (LongAnnotationData) data;
				label.setText(tag.getContentAsString());
				label.setToolTipText(formatToolTip(tag, null));
				if (tag.getId() < 0)
					label.setForeground(
						DataObjectListCellRenderer.NEW_FOREGROUND_COLOR);
			} else if (data instanceof DoubleAnnotationData) {
				DoubleAnnotationData tag = (DoubleAnnotationData) data;
				label.setText(tag.getContentAsString());
				label.setToolTipText(formatToolTip(tag, null));
				if (tag.getId() < 0)
					label.setForeground(
						DataObjectListCellRenderer.NEW_FOREGROUND_COLOR);
			}
			else if (data instanceof BooleanAnnotationData) {
				BooleanAnnotationData tag = (BooleanAnnotationData) data;
				label.setText(tag.getContentAsString());
				label.setToolTipText(formatToolTip(tag, null));
				if (tag.getId() < 0)
					label.setForeground(
						DataObjectListCellRenderer.NEW_FOREGROUND_COLOR);
			}
			else if (data instanceof TimeAnnotationData) {
				TimeAnnotationData tag = (TimeAnnotationData) data;
				label.setText(tag.getContentAsString());
				label.setToolTipText(formatToolTip(tag, null));
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
				if (e.getClickCount() == 1 && e.isPopupTrigger()) {
					showMenu(label, e.getPoint());
				}
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
		if (count > 0 && data != null) {
			menuButton.setEnabled(true);
			if (model.isAcrossGroups()) menuButton.setEnabled(false);
			bar.add(menuButton);
			if (!b) bar.add(Box.createHorizontalStrut(8));
			add(bar);
		}
	}
	
	/** Adds or edits the description of the tag. */
	private void editDescription()
	{
		if (!(data instanceof AnnotationData)) return;
		String text = model.getAnnotationDescription((AnnotationData) data);
		originalDescription = text;
		SwingUtilities.convertPointToScreen(popupPoint, this);
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		int type = EditorDialog.EDIT_TYPE;
		if (isModulo) type = EditorDialog.VIEW_TYPE;
		EditorDialog d = new EditorDialog(f, (AnnotationData) data, false, type);
		if (isModulo) d.allowEdit(false);
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
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
				"Download", "Select where to download the file.", null, true, true);
		if (CommonsLangUtils.isNotBlank(name)) 
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
			case REMOVE:
				firePropertyChange(AnnotationUI.REMOVE_ANNOTATION_PROPERTY,
						null, this);
				break;
			case EDIT:
				editDescription();
				break;
			case DOWNLOAD:
				download();
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
			String text = "";
			String description = "";
			AnnotationData annotation = null;
			if (data instanceof TagAnnotationData ||
				data instanceof TermAnnotationData ||
				data instanceof XMLAnnotationData) {
				annotation = (AnnotationData) data;
				text = annotation.getContentAsString();
				text = EditorUtil.truncate(text, TEXT_LENGTH,
				        false);
			}
			if(data instanceof DoubleAnnotationData) {
				annotation = (AnnotationData) data;
				text = ""+((DoubleAnnotationData) data).getDataValue();
			}
			if(data instanceof LongAnnotationData) {
				annotation = (AnnotationData) data;
				text = ""+((LongAnnotationData) data).getDataValue();
			}
			if(data instanceof BooleanAnnotationData) {
				annotation = (AnnotationData) data;
				text = ""+((BooleanAnnotationData) data).getValue();
			}
			description = model.getAnnotationDescription(annotation);
			if (annotation == null) return;
			label.setText(text);
			label.setToolTipText(formatToolTip(annotation, null));
			originalName = text;
			originalDescription = description;
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
			UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
			
			IconManager icons = IconManager.getInstance();
			
			DownloadActivityParam activity = new DownloadActivityParam(f,
					folder, icons.getIcon(IconManager.DOWNLOAD_22));
			//Check Name space
			activity.setLegend(fa.getDescription());
			un.notifyActivity(model.getSecurityContext(), activity);
		}
	}

}

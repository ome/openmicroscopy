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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

//Third-party libraries
//import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;
import org.openmicroscopy.shoola.util.filter.file.HTMLFilter;
import org.openmicroscopy.shoola.util.filter.file.PDFFilter;
import org.openmicroscopy.shoola.util.filter.file.PowerPointFilter;
import org.openmicroscopy.shoola.util.filter.file.TEXTFilter;
import org.openmicroscopy.shoola.util.filter.file.WordFilter;
import org.openmicroscopy.shoola.util.filter.file.XMLFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import ome.model.annotations.FileAnnotation;
import pojos.AnnotationData;
import pojos.FileAnnotationData;

/** 
 * The UI component displaying the documents linked to the data object.
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
	implements ActionListener, PropertyChangeListener
{

	/** Identified the icons view. */
	private static final int		ICON_VIEW = 0;
	
	/** Identified the columns view. */
	private static final int		COLUMNS_VIEW = 1;
	
	/** The title associated to this component. */
	private static final String 	TITLE = "Related documents ";
	
	/** Action id indicating to add new file. */
	private static final String		ADD_NEW_ACTION = "addNew";
	
	/** Action id indicating to add new file. */
	private static final String		ADD_UPLOADED_ACTION = "addUploaded";
	
	/** Action id indicating to add new file. */
	private static final String		ICON_VIEW_ACTION = "iconView";
	
	/** Action id indicating to add new file. */
	private static final String		COLUMNS_VIEW_ACTION = "columnsView";

	/** Action id indicating to delete the annotation. */
	private static final String		DELETE_ACTION = "delete";

	/** Action id indicating to delete the annotation. */
	private static final String		DOWNLOAD_ACTION = "download";
	
	/** Action id indicating to order the annotation by name. */
	private static final String		BY_NAME_ACTION = "byName";
	
	/** Action id indicating to order the annotation by date. */
	private static final String		BY_DATE_ACTION = "byDate";
	
	/** Action id indicating to order the annotation by size. */
	private static final String		BY_SIZE_ACTION = "bySize";
	
	/** Action id indicating to order the annotation by kind. */
	private static final String		BY_KIND_ACTION = "bykind";

	/** Index indicating to order the files by name. */
	private static final int		BY_NAME = 0;
	
	/** Index indicating to order the files by date. */
	private static final int		BY_DATE = 1;
	
	/** Index indicating to order the files by size. */
	private static final int		BY_SIZE = 2;
	
	/** Index indicating to order the files by kind. */
	private static final int		BY_KIND = 3;
	
	/** The dimension of the scroll pane. */
	private static final Dimension 	SCROLL_SIZE = new Dimension (100, 150);

	/** Button to add a new file. */
	private JButton								addButton;
	
	/** Collection of annotation files to remove. */
	private List<AnnotationData> 				removedFiles;
	
	/** Collection of supported file formats. */
	private List<FileFilter>					filters;
	
	/** Collection of files to attach to the object. */
	private List<File>							addedFiles;
	
	/** Collection of files to attach to the object. */
	private List<FileAnnotationData>			addedFileAnnotations;
	
	/** Map used to handle the files to add. */
	private Map<Integer, Object>				rows;
	
	/** Collection of {@link AttachmentComponent}s. */
	private List<AttachmentComponent> 			toDownload;
	
	/** The label corresponding to the selected file. */
	private AttachmentComponent					selectedFile;
	
	/** The selected annotation. Used to synchronize the various view. */
	private FileAnnotationData					selectedAnnotation;
	
	/** The toolbar displaying the layout options. */
	private JToolBar							toolBar;
	
	/** The layout index used to display the attachments. */
	private int									viewIndex;
	
	/** The selection menu. */
	private JPopupMenu							selectionMenu;
	
	/** The order by menu. */
	private JPopupMenu							orderByMenu;
	
	/** The management menu. */
	private JPopupMenu							managementMenu;
	
	/** The panel hosting the added files. */
	private JPanel								toAddPane;
	
	/** The panel hosting the attachments. */
	private JScrollPane 						pane;
	
	/** Button used to display the {@link #orderByMenu}. */
	private JButton 							arrangeByButton;
	
	/** The layout index used to display the attachments. */
	private int									orderByIndex;
	
	/** Keeps tracks of the comparator. */
	private Map<Integer, Comparator>			comparators;
	
	/** The border displaying the title. */
	private TitledLineBorder 					border;
	
	/**
	 * Creates the order by menu.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createOrderByMenu()
	{
		if (orderByMenu != null) return orderByMenu;
		String s = "Arrange by ";
		String tip = "Order the documents by ";
		orderByMenu = new JPopupMenu();
		JMenuItem item = new JMenuItem(s+AttachmentsTable.NAME);
		item.addActionListener(this);
		item.setActionCommand(BY_NAME_ACTION);
		item.setToolTipText(tip+"name.");
		orderByMenu.add(item);
		item = new JMenuItem(s+AttachmentsTable.DATE);
		item.addActionListener(this);
		item.setActionCommand(BY_DATE_ACTION);
		item.setToolTipText(tip+"date.");
		orderByMenu.add(item);
		item = new JMenuItem(s+AttachmentsTable.SIZE);
		item.addActionListener(this);
		item.setActionCommand(BY_SIZE_ACTION);
		item.setToolTipText(tip+"size.");
		orderByMenu.add(item);
		item = new JMenuItem(s+AttachmentsTable.KIND);
		item.addActionListener(this);
		item.setActionCommand(BY_KIND_ACTION);
		item.setToolTipText(tip+"kind.");
		orderByMenu.add(item);
		return orderByMenu;
	}
	
	/**
	 * Creates the selection menu.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createSelectionMenu()
	{
		if (selectionMenu != null) return selectionMenu;
		IconManager icons = IconManager.getInstance();
		selectionMenu = new JPopupMenu();
		JMenuItem item = new JMenuItem("New document");
		item.setIcon(icons.getIcon(IconManager.UPLOAD));
		item.setToolTipText("Attach new document.");
		item.addActionListener(this);
		item.setActionCommand(""+ADD_NEW_ACTION);
		selectionMenu.add(item);
		item = new JMenuItem("Uploaded document");
		item.setIcon(icons.getIcon(IconManager.DOWNLOAD));
		item.setToolTipText("Attach a document already uploaded.");
		item.addActionListener(this);
		item.setActionCommand(""+ADD_UPLOADED_ACTION);
		selectionMenu.add(item);
		return selectionMenu;
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		comparators = new HashMap<Integer, Comparator>();
		viewIndex = ICON_VIEW;
		orderByIndex = BY_NAME;
		IconManager icons = IconManager.getInstance();
		JToggleButton iconsView = new JToggleButton(
								icons.getIcon(IconManager.LIST_VIEW));
		iconsView.setToolTipText("List View");
		iconsView.setActionCommand(""+ICON_VIEW_ACTION);
		iconsView.addActionListener(this);
		JToggleButton columnsView = new JToggleButton(
								icons.getIcon(IconManager.COLUMNS_VIEW));
		columnsView.setToolTipText("Columns View");
		columnsView.setActionCommand(""+COLUMNS_VIEW_ACTION);
		columnsView.addActionListener(this);
		iconsView.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(iconsView);
		group.add(columnsView);
		
		toDownload = new ArrayList<AttachmentComponent>();
		rows = new HashMap<Integer, Object>();
		addedFiles = new ArrayList<File>();
		filters = new ArrayList<FileFilter>();
		filters.add(new PDFFilter());
		filters.add(new TEXTFilter());
		filters.add(new XMLFilter());
		filters.add(new HTMLFilter());
		filters.add(new PowerPointFilter());
		filters.add(new ExcelFilter());
		filters.add(new WordFilter());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		addButton = new JButton("Attach...");
		addButton.setToolTipText("Attach a document.");
		addButton.addMouseListener(new MouseAdapter() {
		
			public void mouseReleased(MouseEvent e) {
				Point p = e.getPoint();
				createSelectionMenu().show(addButton, p.x, p.y);
			}
		
		});
		removedFiles = new ArrayList<AnnotationData>();
		addedFileAnnotations = new ArrayList<FileAnnotationData>();
		toolBar = new JToolBar();
		toolBar.setBorder(null);
		toolBar.setFloatable(false);
		toolBar.add(UIUtilities.setTextFont("View: "));
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(iconsView);
		toolBar.add(columnsView);
		arrangeByButton = new JButton(icons.getIcon(IconManager.SORT));
		UIUtilities.unifiedButtonLookAndFeel(arrangeByButton);
		arrangeByButton.setToolTipText("Arrange files by name, date, " +
										"size or kind.");
		arrangeByButton.addMouseListener(new MouseAdapter() {
		
			public void mouseReleased(MouseEvent e) {
				if (!arrangeByButton.isEnabled()) return;
				Point p = e.getPoint();
				createOrderByMenu().show(arrangeByButton, p.x, p.y);
			}
		
		});
		arrangeByButton.setEnabled(viewIndex == ICON_VIEW);
		toolBar.add(arrangeByButton);
		
		
		toAddPane = new JPanel();
		toAddPane.setLayout(new GridBagLayout());
		pane = new JScrollPane();
		pane.setPreferredSize(SCROLL_SIZE);
	}

	/**
	 * Sorts the passed collection by name, date, size or kind depending
	 * on the value of {@link #orderByIndex}.
	 * 
	 * @param annotations The collection to sort.
	 */
	private void sortAnnotations(List<FileAnnotationData> annotations)
	{
		if (annotations == null || annotations.size() == 0) return;
		Comparator c;
		switch (orderByIndex) {
			default:
			case BY_NAME:
				c = comparators.get(BY_NAME);
				if (c == null) {
					c = new Comparator() {
			            public int compare(Object o1, Object o2)
			            {
			                String s1 = 
			                	((FileAnnotationData) o1).getFileName();
			                String s2 = 
			                	((FileAnnotationData) o2).getFileName();
			                s1 = s1.toLowerCase();
			                s2 = s2.toLowerCase();
			                int v = 0;
			                int result = s1.compareTo(s2);
			                if (result < 0) v = -1;
			                else if (result > 0) v = 1;
			                return -v;
			            }
			        };
			        comparators.put(BY_NAME, c);
				}
				break;
	
			case BY_DATE:
				c = comparators.get(BY_DATE);
				if (c == null) {
					c = new Comparator() {
			            public int compare(Object o1, Object o2)
			            {
			                Timestamp t1 = 
			                		((FileAnnotationData) o1).getLastModified(),
			                          t2 = 
			                        ((FileAnnotationData) o2).getLastModified();
			                long n1 = t1.getTime();
			                long n2 = t2.getTime();
			                int v = 0;
			                if (n1 < n2) v = -1;
			                else if (n1 > n2) v = 1;
			                return -v;
			            }
			        };
			        comparators.put(BY_DATE, c);
				}
				
				break;
			case BY_SIZE:
				c = comparators.get(BY_SIZE);
				if (c == null) {
					c = new Comparator() {
			            public int compare(Object o1, Object o2)
			            {
			                long n1 = ((FileAnnotationData) o1).getFileSize();
			                long n2 = ((FileAnnotationData) o2).getFileSize();
			                int v = 0;
			                if (n1 < n2) v = -1;
			                else if (n1 > n2) v = 1;
			                return -v;
			            }
			        };
			        comparators.put(BY_SIZE, c);
				}
				break;
			case BY_KIND:
				c = comparators.get(BY_KIND);
				if (c == null) {
					c = new Comparator() {
			            public int compare(Object o1, Object o2)
			            {
			                String s1 = 
			                		((FileAnnotationData) o1).getFileKind(),
			                          s2 = 
			                        ((FileAnnotationData) o2).getFileKind();
			                s1 = s1.toLowerCase();
			                s2 = s2.toLowerCase();
			                int v = 0;
			                int result = s1.compareTo(s2);
			                if (result < 0) v = -1;
			                else if (result > 0) v = 1;
			                return -v;
			            }
			        };
			        comparators.put(BY_KIND, c);
				}
		}
        
		
        Collections.sort(annotations, c);
	}
	
	/** Launches a file chooser to select the file to attach. */
	private void browseFile()
	{
		JFrame owner = 
			MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(owner, FileChooser.SAVE, 
								"Browse File", "Attach a file to the " +
										"selected element", filters);
		chooser.addPropertyChangeListener(
				FileChooser.APPROVE_SELECTION_PROPERTY, this);
		UIUtilities.centerAndShow(chooser);
	}
	
	/**
	 * Brings up the widget asking the user what to do with the file.
	 * 
	 * @param source The source of the click.
	 */
	void viewFile(AttachmentComponent source)
	{
		FileAnnotationData data = source.getFile();
		if (data == null) return;
		Registry reg = MetadataViewerAgent.getRegistry();
		
		FileChooser chooser = new FileChooser(reg.getTaskBar().getFrame(), 
					FileChooser.FOLDER_CHOOSER, "Directory selection",
					"Select the folder where to save the files.");
		if (chooser.showDialog() == JFileChooser.APPROVE_OPTION) {
			File dir = chooser.getSelectedFile();
			UserNotifier un = reg.getUserNotifier();
			un.notifyDownload(((FileAnnotation) data.asAnnotation()).getFile(),
								dir);
		}
	}
	
	/**
	 * Formats the passed annotation.
	 * 
	 * @param f The value to format.
	 * @return See above.
	 */
	static String formatTootTip(FileAnnotationData f)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body>");
		buf.append("<b>");
		buf.append(AttachmentsTable.DATE+": ");
		buf.append("</b>");
		buf.append(UIUtilities.formatWDMYDate(f.getLastModified()));
		buf.append("<br>");
		buf.append("<b>");
		buf.append(AttachmentsTable.SIZE+": ");
		buf.append("</b>");
		buf.append(UIUtilities.formatFileSize(f.getFileSize()));
		buf.append("<br>");
		buf.append("</body></html>");
		return buf.toString();
	}
	
	/**
	 * Lays out the existing file using icons.
	 * 
	 * @param toLayout The collection of file to lay out.
	 * @return See above.
	 */
	private JPanel buildIconViews(List<FileAnnotationData> toLayout)
	{
		sortAnnotations(toLayout);
		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		c.gridx = 0;
		int index = 0;
		FileAnnotationData f;
		Iterator i = toLayout.iterator();
		toDownload.clear();
		AttachmentComponent comp;
		while (i.hasNext()) {
			++c.gridx;
			f = (FileAnnotationData) i.next();
			comp = new AttachmentComponent(this, f);
			toDownload.add(comp);
			content.add(comp, c);
			++c.gridx;
			content.add(Box.createHorizontalStrut(10), c);
			if (index%3 == 0 && index != 0) {
				c.gridy++;
				c.gridx = 0;
			}
			index++;
		}
		
		
		content.setOpaque(false);
		content.setBackground(UIUtilities.BACKGROUND);
		JPanel p = UIUtilities.buildComponentPanel(content);
		p.setBackground(UIUtilities.BACKGROUND);
		p.addMouseListener(new MouseAdapter() {
			
			/**
			 * Resets the selection.
			 */
			public void mousePressed(MouseEvent e) {
				setSelectedFile(null);
			}
		
		});
		return p;
	}
	
	/** Lays out the nodes. */
	private void layoutView()
	{
		arrangeByButton.setEnabled(viewIndex == ICON_VIEW);
		Collection attachments = model.getAttachments();
		if (attachments == null) return;
		List<FileAnnotationData> toLayout = new ArrayList<FileAnnotationData>();
		JViewport port = pane.getViewport();
		port.removeAll();
		Iterator i = attachments.iterator();
		FileAnnotationData f;
		while (i.hasNext()) {
			f = (FileAnnotationData) i.next();
			if (!removedFiles.contains(f))
				toLayout.add(f);
		}
		JPanel p = buildIconViews(toLayout);
		switch (viewIndex) {
			case ICON_VIEW:
			default:
				port.add(p);
				break;
			case COLUMNS_VIEW:
				port.add(new AttachmentsTable(this, toLayout));
		}
		port.revalidate();
		port.repaint();
	}
	
	/**
	 * Lays out the components used to add new <code>file</code>s.
	 * 
	 * @return See above.
	 */
	private JPanel layoutAttachments()
	{
		layoutView();
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(UIUtilities.buildComponentPanelRight(toolBar), 
				Component.RIGHT_ALIGNMENT);
		p.add(pane, Component.CENTER_ALIGNMENT);
		return p;
	}
	
	/**
	 * Builds a UI component displaying the file to add.
	 * 
	 * @param f		The object to add, either a file or a file annotataion.
	 * @param index	The index associated to the file.
	 * @return See above.
	 */
	private JPanel buildAddedFileRow(Object f, int index)
	{
		JPanel row = new JPanel();
		IconManager icons = IconManager.getInstance();
		JTextArea area = new JTextArea();
		JLabel label = new JLabel();
		if (f instanceof File) {
			area.setText(((File) f).getAbsolutePath());
			label.setIcon(icons.getIcon(IconManager.UPLOAD));
		} else if (f instanceof FileAnnotationData) {
			label.setIcon(icons.getIcon(IconManager.DOWNLOAD));
			area.setText(((FileAnnotationData) f).getFilePath());
		}
		UIUtilities.setTextAreaDefault(area);
		row.add(label);
		row.add(area);
		JButton remove = new JButton(icons.getIcon(IconManager.REMOVE));
		UIUtilities.unifiedButtonLookAndFeel(remove);
		remove.setToolTipText("Remove the attachment.");
		remove.addActionListener(this);
		remove.setActionCommand(""+index);
		row.add(remove);
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	    p.add(row);
		return p;
	}
	
	/**
	 * Lays out the files to add.
	 * 
	 * @return See above.
	 */
	private JPanel layoutAddedFiles()
	{
		rows.clear();
		toAddPane.removeAll();
		Iterator i = addedFiles.iterator();
		int index = 0;
		File f;
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		while (i.hasNext()) {
			++c.gridy;
			f = (File) i.next();
			rows.put(index, f);
			toAddPane.add(buildAddedFileRow(f, index), c);
			index++;
		}
		i = addedFileAnnotations.iterator();
		FileAnnotationData data;
		while (i.hasNext()) {
			++c.gridy;
			data = (FileAnnotationData) i.next();
			rows.put(index, data);
			toAddPane.add(buildAddedFileRow(data, index), c);
			index++;
			
		}
		return toAddPane;
	}
	
	/**
	 * Lays out the components used to add new <code>URL</code>s.
	 * 
	 * @return See above.
	 */
	private JPanel layoutContent()
	{
		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		content.add(addButton, c);
		c.weightx = 0.5;
		JScrollPane pane = new JScrollPane(layoutAddedFiles());
		pane.setOpaque(false);
		pane.setBorder(null);
		c.gridx++;
		content.add(pane, c);
		content.revalidate();
		content.repaint();
		return content;
	}
	
	/**
	 * Returns <code>true</code> if the file has already been added, 
	 * <code>false</code> otherwise.
	 * 
	 * @param data The value to check.
	 * @return See above.
	 */
	private boolean isFileAnnotationAdded(FileAnnotationData data)
	{
		if (data == null) return true;
		Iterator<FileAnnotationData> i = addedFileAnnotations.iterator();
		FileAnnotationData f;
		while (i.hasNext()) {
			f = i.next();
			if (f.getId() == data.getId())
				return true;
		}
		return false;
	}
	

	/** Adds the selected file to the collection of items to remove. */
	private void removeSelectedAttachment() 
	{
		if (selectedFile == null) return;
		FileAnnotationData data = selectedFile.getFile();
		if (data == null) return;
		toDownload.remove(selectedFile);
		selectedFile = null;
		selectedAnnotation = null;
		removedFiles.add(data);
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
		buildUI();
		revalidate();
		repaint();
	}
	
	/** Brings up the widget to download the archived files. */
	private void downloadSelectedAttachment()
	{
		if (selectedFile == null) return;
		viewFile(selectedFile);
		selectedFile = null;
	}
	
	/** 
	 * Orders the file according to the passed index.
	 * 
	 * @param index One of the ordering constants defined by this class.
	 */
	private void setOrderIndex(int index)
	{
		if (index == orderByIndex) return;
		orderByIndex = index;
		buildUI();
		revalidate();
		repaint();
	}
	
	/** Sets the title of the components. */
	private void setNodesTitle()
	{
		int n = model.getAttachmentsCount()-removedFiles.size();
		title = TITLE+LEFT+n+RIGHT;
		border.setTitle(title);
		((TitledBorder) getBorder()).setTitle(title);
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
		border = new TitledLineBorder(title, getBackground());
		UIUtilities.setBoldTitledBorder(title, this);
		getCollapseComponent().setBorder(border);
		add(layoutContent());
	}

	/**
	 * Sets the selected annotation and brings up the {@link #managementMenu}.
	 * 
	 * @param data		The selected annotation.
	 * @param invoker	The component invoking the menu.
	 * @param x			The x-coordinate of the mouse pressed.
	 * @param y			The y-coordinate of the mouse pressed.
	 */
	void manageAnnotation(FileAnnotationData data, JComponent invoker, 
						int x, int y)
	{
		if (data == null) return;
		Iterator<AttachmentComponent> i = toDownload.iterator();
		FileAnnotationData f;
		AttachmentComponent l;
		while (i.hasNext()) {
			l = i.next();
			f = l.getFile();
			if (f.getId() == data.getId()) {
				selectedFile = l;
				break;
			}
		}
		createManagementMenu().show(invoker, x, y);
	}
	
	/** Shows the collection of existing tags. */
	void showSelectionWizard()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		Collection l = model.getExistingAttachments();
		if (l == null) return;
		List<Object> r = new ArrayList<Object>();
		Collection attachments = model.getAttachments();
		Iterator i;
		Set<Long> ids = new HashSet<Long>();
		AnnotationData data;
		if (attachments != null) {
			i = attachments.iterator();
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (!removedFiles.contains(data)) 
					ids.add(data.getId());
			}
		}
		
		if (l.size() > 0) {
			
			i = l.iterator();
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (!ids.contains(data.getId()))
					r.add(data);
			}
		}
		
		Registry reg = MetadataViewerAgent.getRegistry();
		if (r.size() == 0) {
			UserNotifier un = reg.getUserNotifier();
			un.notifyInfo("Existing Files", "No files found.");
			return;
		}
		SelectionWizard wizard = new SelectionWizard(
										reg.getTaskBar().getFrame(), r);
		IconManager icons = IconManager.getInstance();
		wizard.setTitle("Upload Files Selection" , "Select files already " +
				"updloaded to the server", 
				icons.getIcon(IconManager.ATTACHMENT_48));
		wizard.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(wizard);
	}
	
	/**
	 * Creates the management menu.
	 * 
	 * @return See above.
	 */
	JPopupMenu createManagementMenu()
	{
		if (managementMenu != null) return managementMenu;
		IconManager icons = IconManager.getInstance();
		managementMenu = new JPopupMenu();
		JMenuItem item = new JMenuItem("Remove");
		item.addActionListener(this);
		item.setActionCommand(DELETE_ACTION);
		item.setIcon(icons.getIcon(IconManager.REMOVE));
		item.setToolTipText("Remove the attachment.");
		managementMenu.add(item);
		item = new JMenuItem("Download");
		item.addActionListener(this);
		item.setActionCommand(DOWNLOAD_ACTION);
		item.setIcon(icons.getIcon(IconManager.DOWNLOAD));
		item.setToolTipText("Download the file.");
		managementMenu.add(item);
		return managementMenu;
	}
	
	/** Sets the selected component. */
	void setSelected()
	{
		Iterator<AttachmentComponent> i = toDownload.iterator();
		AttachmentComponent comp;
		long id = -1;
		if (selectedAnnotation != null) id = selectedAnnotation.getId();
		while (i.hasNext()) {
			comp = i.next();
			comp.setSelectedBackground((comp.getFile().getId() == id));
		}
	}
	
	/**
	 * Sets the selected annotation.
	 * 
	 * @param selectedFile The value to set.
	 */
	void setSelectedFile(AttachmentComponent selectedFile)
	{
		Iterator<AttachmentComponent> i = toDownload.iterator();
		while (i.hasNext()) 
			i.next().setSelectedBackground(false);
		this.selectedFile = selectedFile;
		if (selectedFile != null) {
			selectedAnnotation = selectedFile.getFile();
			selectedFile.setSelectedBackground(true);
		} else selectedAnnotation = null;	
	}
	
	/**
	 * Sets the selected annotation.
	 * 
	 * @param data The value to set.
	 */
	void setSelectedFileAnnotation(FileAnnotationData data)
	{
		selectedAnnotation = data;
	}
	
	/**
	 * Returns the selected annotation.
	 * 
	 * @return See above.
	 */
	FileAnnotationData getSelectedAnnotation() { return selectedAnnotation; }
	
	/**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		clearDisplay();
		int n = model.getAttachmentsCount()-removedFiles.size();
		if (n > 0) add(layoutAttachments());
		add(layoutContent());
		setSelected();
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }
	
	/**
	 * Returns the collection of attachments to remove.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove()
	{
		return removedFiles;
	}

	/**
	 * Returns the collection of attachments to add.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{
		List<AnnotationData> toAdd = new ArrayList<AnnotationData>();
		Iterator i = addedFiles.iterator();
		File f;
		while (i.hasNext()) {
			f = (File) i.next();
			toAdd.add(new FileAnnotationData(f));
		}
		if (addedFileAnnotations.size() > 0)
			toAdd.addAll(addedFileAnnotations);
		return toAdd;
	}

	/**
	 * Returns <code>true</code> if annotation to save,
	 * <code>false</code> otherwise.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		List l = addedFiles;
		if (l != null && l.size() > 0) return true; 
		if (addedFileAnnotations.size() > 0) return true;
		l = getAnnotationToRemove();
		if (l != null && l.size() > 0) return true; 
		return false;
	}
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		addedFiles.clear();
		removedFiles.clear();
		addedFileAnnotations.clear();
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{
		removeAll();
		setNodesTitle();
	}
	
	/**
	 * Adds the selected annotation to the collection of elements to remove.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();
		if (ADD_NEW_ACTION.equals(s)) {
			browseFile();
		} else if (ADD_UPLOADED_ACTION.equals(s)) {
			if (model.getExistingAttachments() == null) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				model.loadExistingAttachments();
			} else showSelectionWizard();
		} else if (ICON_VIEW_ACTION.equals(s)) {
			viewIndex = ICON_VIEW;
			buildUI();
			revalidate();
			repaint();
		} else if (COLUMNS_VIEW_ACTION.equals(s)) {
			viewIndex = COLUMNS_VIEW;
			buildUI();
			revalidate();
			repaint();
		} else if (DELETE_ACTION.equals(s)) {
			removeSelectedAttachment(); 
		} else if (DOWNLOAD_ACTION.equals(s)) {
			downloadSelectedAttachment();  
		} else if (BY_NAME_ACTION.equals(s)) {
			setOrderIndex(BY_NAME);
		} else if (BY_DATE_ACTION.equals(s)) {
			setOrderIndex(BY_DATE);
		} else if (BY_SIZE_ACTION.equals(s)) {
			setOrderIndex(BY_SIZE);
		} else if (BY_KIND_ACTION.equals(s)) {
			setOrderIndex(BY_KIND);
		} else {
			int index = Integer.parseInt(e.getActionCommand());
			Object f = rows.get(index);
			if (f != null) {
				if (f instanceof File)
					addedFiles.remove(f);
				else if (f instanceof FileAnnotationData)
					addedFileAnnotations.remove(f);
				layoutAddedFiles();
				revalidate();
				repaint();
			}
		}
	}
	
	/**
	 * Adds the new file to the display.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File f = (File) evt.getNewValue();
			Iterator i = addedFiles.iterator();
			boolean exist = false;
			File file;
			while (i.hasNext()) {
				file = (File) i.next();
				if (file.getAbsolutePath().equals(f.getAbsolutePath())) {
					exist = true;
					break;
				}
			}
			if (exist) return;
			addedFiles.add(f);
			firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
								Boolean.TRUE);
			layoutAddedFiles();
			//buildUI();
			revalidate();
			repaint();
		} else if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Collection l = (Collection) evt.getNewValue();
			if (l == null || l.size() == 0) return;
			Iterator i = l.iterator();
			FileAnnotationData data;
	    	while (i.hasNext()) {
	    		data = (FileAnnotationData) i.next();
	    		if (!isFileAnnotationAdded(data))
	    			addedFileAnnotations.add(data);
	    	}
	    	layoutAddedFiles();
	    	revalidate();
			repaint();
	    	firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
					Boolean.TRUE);
		}
	}
	
}

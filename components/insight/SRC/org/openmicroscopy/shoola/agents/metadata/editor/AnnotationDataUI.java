/*
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.SeparatorOneLineBorder;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.BooleanAnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DoubleAnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.LongAnnotationData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TermAnnotationData;
import omero.gateway.model.TimeAnnotationData;
import omero.gateway.model.XMLAnnotationData;

/** 
 * Components displaying the various annotations linked to the related 
 * object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta3
 */
class AnnotationDataUI
	extends AnnotationUI
	implements ActionListener, PropertyChangeListener
{

	/** Indicates to display all the annotations linked to the object. */
	public static final int				SHOW_ALL = 0;
	
	/** 
	 * Indicates to display the annotations linked by current user to 
	 * the object. 
	 */
	public static final int				ADDED_BY_ME = 1;
	
	/** Indicates to display the annotations linked by others to the object. */
	public static final int				ADDED_BY_OTHERS = 2;
	
	/** The maximum number of elements displayed at a time. */
	private static final int				MAX = 3;
	
	/** The names associated to the above constants. */
	private static final String[]			NAMES;
	
	static {
		NAMES = new String[3];
		NAMES[SHOW_ALL] = "Show all";
		NAMES[ADDED_BY_ME] = "Show added by me";
		NAMES[ADDED_BY_OTHERS] = "Show added by others";
	}
	
	/** Component used to rate the object. */
	private RatingComponent 				rating;
	
	/** Components hosting the tags. */
	private JPanel							tagsPane;
	
	/** Components hosting the attachments. */
	private JPanel							docPane;

	/** The last selected value. */
	private int								selectedValue;
	
	/** The initial value of the rating. */
	private int 							initialValue;

	/** The UI component hosting the various annotations. */
	private JPanel							content;
	
	/** Button to add tags. */
	private JButton							addTagsButton;
	
	/** Button to add documents. */
	private JButton							addDocsButton;
	
	/** Button to remove all documents. */
	private JButton							removeDocsButton;
	
	/** Button to remove all tags. */
	private JButton							removeTagsButton;
	
	/** Button to make the FileAnnotations selectable */
	private JButton selectButton;
	
	/** Button to remove the rate of the object. */
	private JButton							unrateButton;
	
	/** Reference to the control. */
	private EditorControl					controller;
	
	/** Reference to the view. */
	private EditorUI view;
	
	/** Flag indicating that tags have been added or removed. */
	private boolean							tagFlag;
	
	/** Flag indicating that documents have been added or removed. */
	private boolean							docFlag;
	
	/** The collection of tags. */
	private List<String> 					tagNames;
	
	/** The collection of existing tags. */
	private Map<String, TagAnnotationData>	existingTags;
	
	/** 
	 * The selection menu to attach either local documents or already
	 * upload files. 
	 */
	private JPopupMenu						docSelectionMenu;
	
	/** Collection of tags objects. */
	private List<DocComponent>				tagsDocList;
	
	/** Collection of files objects. */
	private List<DocComponent>				filesDocList;
	
	/** Indicates if the image has been published or not. */
	private JCheckBox						publishedBox;
	
	/** Flag indicating to build the UI once. */
	private boolean 						init;
	
	/** Host the rating average by others. */
	private JLabel							otherRating;
	
	/** The button to filter the annotations i.e. show all, mine, others. */
	private JButton							filterButton;
	
	/** The selected index. */
	private int								filter;
	
	/** The collection of annotations to replace. */
	private List<FileAnnotationData> 		toReplace;
	
	/** The document of reference. */
	private JComponent docRef;
	
	/** Components hosting the other annotations. */
	private JPanel otherPane;
	
	/** Collection of other annotations objects. */
	private List<DocComponent> otherList;
	
	/** Flag indicating that other annotations have been added or removed. */
	private boolean otherFlag;
	
	/** Button to remove all other annotations. */
	private JButton removeOtherAnnotationsButton;
	
	/** The component displaying the MapAnnotations */
	private MapAnnotationsComponent mapsPane;
	
	/** Flag to indicate if the FileAnnotations should be selectable */
	private boolean selectable;
	
	/**
	 * Creates and displays the menu 
	 * @param src The invoker.
	 * @param p   The location where to show the menu.
	 */
	private void displayMenu(Component src, Point p)
	{
		JPopupMenu menu = new JPopupMenu();
		ButtonGroup group = new ButtonGroup();
		JCheckBoxMenuItem item = createMenuItem(SHOW_ALL);
		group.add(item);
		menu.add(item);
		item = createMenuItem(ADDED_BY_ME);
		group.add(item);
		menu.add(item);
		item = createMenuItem(ADDED_BY_OTHERS);
		group.add(item);
		menu.add(item);
		menu.show(src, p.x, p.y);
	}
	
	/**
	 * Creates a menu item.
	 * 
	 * @param index The index associated to the item.
	 * @return See above.
	 */
	private JCheckBoxMenuItem createMenuItem(int index)
	{
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(NAMES[index]);
		Font f = item.getFont();
		item.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
		item.setSelected(filter == index);
		item.addActionListener(this);
		item.setActionCommand(""+index);
		return item;
	}
	
	/** Displays the annotations depending on the selected filter. */
	private void filterAnnotations()
	{
		filterButton.setText(NAMES[filter]);
		Iterator<DocComponent> i = tagsDocList.iterator();
		List<Object> nodes = new ArrayList<Object>();
		Object data;
		while (i.hasNext()) {
			data = i.next().getData();
			if (data != null) nodes.add(data);
		}
		layoutTags(nodes);
		i = filesDocList.iterator();
		nodes = new ArrayList<Object>();
		while (i.hasNext()) {
			data = i.next().getData();
			if (data != null) nodes.add(data);
		}
		layoutAttachments(nodes);
		
		i = otherList.iterator();
		nodes = new ArrayList<Object>();
		while (i.hasNext()) {
			data = i.next().getData();
			if (data != null) nodes.add(data);
		}
		layoutOthers(nodes);
		buildGUI();
	}
	
	/**
	 * Creates the selection menu.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createDocSelectionMenu()
	{
		if (docSelectionMenu != null) return docSelectionMenu;
		docSelectionMenu = new JPopupMenu();
		JMenuItem item = new JMenuItem("Local document...");
		item.setToolTipText("Import a local document to the server " +
				"and attach it.");
		item.addActionListener(controller);
		item.setActionCommand(""+EditorControl.ADD_LOCAL_DOCS);
		docSelectionMenu.add(item);
		item = new JMenuItem("Uploaded document...");
		item.setToolTipText("Attach a document already uploaded " +
				"to the server.");
		item.addActionListener(controller);
		item.setActionCommand(""+EditorControl.ADD_UPLOADED_DOCS);
		docSelectionMenu.add(item);
		return docSelectionMenu;
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		setLayout(new BorderLayout());
		setBackground(UIUtilities.BACKGROUND);
		setBorder(new SeparatorOneLineBorder());
		
		toReplace = new ArrayList<FileAnnotationData>();
		IconManager icons = IconManager.getInstance();
		filter = SHOW_ALL;
		filterButton = new JButton(NAMES[SHOW_ALL]);
		filterButton.setToolTipText("Filter tags and attachments.");
		UIUtilities.unifiedButtonLookAndFeel(filterButton);
		Font font = filterButton.getFont();
		filterButton.setFont(font.deriveFont(font.getStyle(), 
				font.getSize()-2));
		
		filterButton.setIcon(icons.getIcon(IconManager.UP_DOWN_9_12));
		
		filterButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		filterButton.addMouseListener(new MouseAdapter() {
			
			/** 
			 * Brings up the menu. 
			 * @see MouseListener#mouseReleased(MouseEvent)
			 */
			public void mouseReleased(MouseEvent me)
			{
				Object source = me.getSource();
		        if (source instanceof Component)
		        	displayMenu((Component) source, me.getPoint());
			}
			
		});
		
		
		otherRating = new JLabel();
		otherRating.setBackground(UIUtilities.BACKGROUND_COLOR);
		font = otherRating.getFont();
		otherRating.setFont(font.deriveFont(Font.ITALIC, font.getSize()-2));
		content = new JPanel();
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		tagFlag = false;
		docFlag = false;
		otherFlag = false;
		tagNames = new ArrayList<String>();
		tagsDocList = new ArrayList<DocComponent>();
		filesDocList = new ArrayList<DocComponent>();
		otherList = new ArrayList<DocComponent>();
		existingTags = new HashMap<String, TagAnnotationData>();
		
		addTagsButton = new JButton(icons.getIcon(IconManager.PLUS_12));
		UIUtilities.unifiedButtonLookAndFeel(addTagsButton);
		addTagsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		addTagsButton.setToolTipText("Add Tags.");
		addTagsButton.addActionListener(controller);
		addTagsButton.setActionCommand(""+EditorControl.ADD_TAGS);
		addDocsButton = new JButton(icons.getIcon(IconManager.PLUS_12));
		addDocsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		addDocsButton.setToolTipText("Attach a document.");
		addDocsButton.addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(MouseEvent e) {
				if (addDocsButton.isEnabled()) {
					Point p = e.getPoint();
					createDocSelectionMenu().show(addDocsButton, p.x, p.y);
				}
			}
		
		});
		UIUtilities.unifiedButtonLookAndFeel(addDocsButton);
		
		removeTagsButton = new JButton(icons.getIcon(IconManager.MINUS_12));
		UIUtilities.unifiedButtonLookAndFeel(removeTagsButton);
		removeTagsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		removeTagsButton.setToolTipText("Remove Tags.");
		removeTagsButton.addMouseListener(controller);
		removeTagsButton.setActionCommand(""+EditorControl.REMOVE_TAGS);
		
		removeDocsButton = new JButton(icons.getIcon(IconManager.MINUS_12));
		UIUtilities.unifiedButtonLookAndFeel(removeDocsButton);
		removeDocsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		removeDocsButton.setToolTipText("Remove Attachments.");
		removeDocsButton.addMouseListener(controller);
		removeDocsButton.setActionCommand(""+EditorControl.REMOVE_DOCS);
		
		removeOtherAnnotationsButton = new JButton(
				icons.getIcon(IconManager.MINUS_12));
		UIUtilities.unifiedButtonLookAndFeel(removeOtherAnnotationsButton);
		removeOtherAnnotationsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		removeOtherAnnotationsButton.setToolTipText("Remove Annotations.");
		removeOtherAnnotationsButton.addMouseListener(controller);
		removeOtherAnnotationsButton.setActionCommand(
				""+EditorControl.REMOVE_OTHER_ANNOTATIONS);
		
        selectButton = new JButton(icons.getIcon(IconManager.ANALYSIS));
        selectButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        selectButton.setToolTipText("Select Files for Scripts");
        selectButton.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                if (selectButton.isEnabled()) {
                    setFileAnnotationSelectable(!isFileAnnotationSelectable());
                }
            }

        });
        UIUtilities.unifiedButtonLookAndFeel(selectButton);
        
		selectedValue = 0;
		initialValue = selectedValue;
		rating = new RatingComponent(selectedValue, 
									RatingComponent.MEDIUM_SIZE);
		rating.setOpaque(false);
		rating.setBackground(UIUtilities.BACKGROUND_COLOR);
		rating.addPropertyChangeListener(this);
		unrateButton = new JButton(icons.getIcon(IconManager.MINUS_12));
		UIUtilities.unifiedButtonLookAndFeel(unrateButton);
		unrateButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		unrateButton.setToolTipText("Unrate.");
		unrateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{ 
				rating.setValue(0);
				view.saveData(true);
			}
		});
		tagsPane = new JPanel();
		tagsPane.setLayout(new BoxLayout(tagsPane, BoxLayout.Y_AXIS));
		tagsPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		DocComponent doc = new DocComponent(null, model);
		tagsDocList.add(doc);
		tagsPane.add(doc);
		docPane = new JPanel();
		docPane.setLayout(new BoxLayout(docPane, BoxLayout.Y_AXIS));
		docPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		docRef = docPane;
		doc = new DocComponent(null, model);
		filesDocList.add(doc);
		docPane.add(doc);
		publishedBox = new JCheckBox();
		publishedBox.setBackground(UIUtilities.BACKGROUND_COLOR);
		publishedBox.addItemListener(new ItemListener() {
		
			public void itemStateChanged(ItemEvent e) {
				firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
			}
		});
		
		mapsPane = new MapAnnotationsComponent(model, view);
		
		otherPane = new JPanel();
		otherPane.setLayout(new GridBagLayout());
		otherPane.setBackground(UIUtilities.BACKGROUND_COLOR);
	}
	
	/**
	 * Returns if the FileAnnotations are selectable
	 * @return See above.
	 */
	private boolean isFileAnnotationSelectable() {
	    return this.selectable;
	}
	
	/**
	 * Make the FileAnnotations selectable
	 * @param b Pass <code>true</code> to make them selectable, 
	 * <code>false</code> otherwise
	 */
	private void setFileAnnotationSelectable(boolean b) {
	    this.selectable = b;
	    filterAnnotations();
	}
	
	/**
	 * Creates a tool bar and adds the passed buttons to it.
	 * 
	 * @param buttons The buttons to add.
	 * @return See above.
	 */
	private JToolBar createBar(JButton... buttons)
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(null);
		bar.setBackground(UIUtilities.BACKGROUND_COLOR);
		for (JButton button : buttons)
		    bar.add(button);
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();

		JLabel l = new JLabel();
		Font f = l.getFont();
		int size = f.getSize() - 1;
		content.removeAll();
		content.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 1, 2, 1);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;

		if (!model.isAnnotationLoaded()) {
			l.setText("Annotation could not be loaded");
			content.add(l, c);
			return;
		}

		if (model.isMultiSelection()) {
			Object refObject = model.getRefObject();
			StringBuffer buffer = new StringBuffer();
			buffer.append("Annotate the selected ");
			buffer.append(model.getObjectTypeAsString(refObject));
			buffer.append("s");
			l.setText(buffer.toString());
			content.add(l, c);
			c.gridy++;
		}

		// filters
		content.add(createBar(filterButton), c);
		c.gridy++;

		// rating
		c.gridwidth = 1;
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(UIUtilities.setTextFont("Rating:", Font.BOLD, size));
		p.add(createBar(unrateButton));
		content.add(p, c);
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(rating);
		p.add(Box.createHorizontalStrut(2));
		p.add(otherRating);
		content.add(p, c);
		c.gridy++;

		// tags
		c.gridx = 0;
		p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(UIUtilities.setTextFont("Tags:", Font.BOLD, size));
		p.add(createBar(addTagsButton, removeTagsButton));
		content.add(p, c);
		c.gridy++;
		content.add(tagsPane, c);
		c.gridy++;

		// attachment
		c.gridx = 0;
		c.gridwidth = 2;
		p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(UIUtilities.setTextFont("Attachments:", Font.BOLD, size));
		p.add(createBar(addDocsButton, removeDocsButton, selectButton));
		content.add(p, c);
		c.gridy++;
		content.add(docRef, c);
		c.gridy++;

		if(!model.isMultiSelection()) {
			mapsPane.reload(filter);
			content.add(mapsPane, c);
			c.gridy++;
		}

		// other
		if (!CollectionUtils.isEmpty(model.getAllOtherAnnotations())) {
			p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			p.setBackground(UIUtilities.BACKGROUND_COLOR);
			p.add(UIUtilities.setTextFont("Others:", Font.BOLD, size));
			p.add(createBar( removeOtherAnnotationsButton));
			content.add(p, c);
			c.gridy++;
			content.add(otherPane, c);
		}

		add(content, BorderLayout.CENTER);
	}
	
	/**
	 * Creates a row.
	 * 
	 * @return See above.
	 */
	private JPanel initRow()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		return p;
	}
  
	/**
	 * Returns the selected FileAnnotations or an empty Collection
	 * if there are no FileAnnotations
	 * 
	 * @return See above
	 */
    public Collection<FileAnnotationData> getSelectedFileAnnotations() {
        Collection<FileAnnotationData> annos = new ArrayList<FileAnnotationData>();
        for (Component comp : docPane.getComponents()) {
            if (comp instanceof DocComponent) {
                DocComponent doc = (DocComponent) comp;
                if (doc.isSelected()
                        && (doc.getData() instanceof FileAnnotationData)) {
                    annos.add((FileAnnotationData) doc.getData());
                }
            }
        }
        return annos;
    }
	
	/** 
	 * Lays out the attachments. 
	 * 
	 * @param list The collection of components to add.
	 */
	private void layoutAttachments(Collection list)
	{
		docPane.removeAll();
		filesDocList.clear();
		DocComponent doc;
		int h = 0;
		int v;
		if (list != null && list.size() > 0) {
			Iterator i = list.iterator();
			Map<FileAnnotationData, Object> 
				loadThumbnails = 
					new LinkedHashMap<FileAnnotationData, Object>();
			DataObject data;
			switch (filter) {
				case SHOW_ALL:
					while (i.hasNext()) {
						data = (DataObject) i.next();
						if (!toReplace.contains(data)) {
							doc = new DocComponent(data, model, true, selectable);
							doc.addPropertyChangeListener(controller);
							if (doc.hasThumbnailToLoad()) {
								loadThumbnails.put((FileAnnotationData) data, 
										doc);
							}
							filesDocList.add(doc);
							docPane.add(doc);
							v = doc.getPreferredSize().height;
							if (h < v) h = v;
						}
					}
					break;
				case ADDED_BY_OTHERS:
					while (i.hasNext()) {
						data = (DataObject) i.next();
						if (!toReplace.contains(data)) {
							doc = new DocComponent(data, model, true, selectable);
							doc.addPropertyChangeListener(controller);
							filesDocList.add(doc);
							if (model.isAnnotatedByOther(data)) {
								if (doc.hasThumbnailToLoad()) {
									loadThumbnails.put(
											(FileAnnotationData) data, doc);
								}
								docPane.add(doc);
								v = doc.getPreferredSize().height;
								if (h < v) h = v;
							}
						}
					}
					break;
				case ADDED_BY_ME:
					while (i.hasNext()) {
						data = (DataObject) i.next();
						if (!toReplace.contains(data)) {
							doc = new DocComponent(data, model, true, selectable);
							doc.addPropertyChangeListener(controller);
							filesDocList.add(doc);
							if (model.isLinkOwner(data)) {
								if (doc.hasThumbnailToLoad()) {
									loadThumbnails.put(
											(FileAnnotationData) data, doc);
								}
								docPane.add(doc);
								v = doc.getPreferredSize().height;
								if (h < v) h = v;
							}
						}
					}
			}
			//load the thumbnails 
			/*
			if (loadThumbnails.size() > 0  
					&& MetadataViewerAgent.isFastConnection()) {
				model.loadFiles(loadThumbnails);
			}
			*/
		}
		if (filesDocList.size() == 0 || docPane.getComponentCount() == 0) {
			doc = new DocComponent(null, model, true, false);
			filesDocList.add(doc);
			docPane.add(doc);
		}
		docRef = docPane;
		
		docPane.revalidate();
		docPane.repaint();
	}
	
	
	/**
	 * Lays out the tags.
	 * 
	 * @param list The collection of tags to layout.
	 */
	private void layoutTags(Collection list)
	{
		tagsPane.removeAll();
		tagsDocList.clear();
		DocComponent doc;
		if (list != null && list.size() > 0) {
			Iterator i = list.iterator();
			int width = 0;
			JPanel p = initRow();
			DataObject data;
			switch (filter) {
				case SHOW_ALL:
					while (i.hasNext()) {
						doc = new DocComponent(i.next(), model);
						doc.addPropertyChangeListener(controller);
						tagsDocList.add(doc);
					    if (width+doc.getPreferredSize().width >= COLUMN_WIDTH)
					    {
					    	tagsPane.add(p);
					    	p = initRow();
							width = 0;
					    } else {
					    	width += doc.getPreferredSize().width;
					    	width += 2;
					    }
						p.add(doc);
					}
					break;
				case ADDED_BY_ME:
					while (i.hasNext()) {
						data = (DataObject) i.next();
						doc = new DocComponent(data, model);
						doc.addPropertyChangeListener(controller);
						tagsDocList.add(doc);
						if (model.isLinkOwner(data)) {
							if (width+doc.getPreferredSize().width 
									>= COLUMN_WIDTH) {
								tagsPane.add(p);
								p = initRow();
								width = 0;
							} else {
								width += doc.getPreferredSize().width;
								width += 2;
							}
							p.add(doc);
						}
					}
					break;
				case ADDED_BY_OTHERS:
					while (i.hasNext()) {
						data = (DataObject) i.next();
						doc = new DocComponent(data, model);
						doc.addPropertyChangeListener(controller);
						tagsDocList.add(doc);
						if (model.isAnnotatedByOther(data)) {
							if (width+doc.getPreferredSize().width 
									>= COLUMN_WIDTH) {
								tagsPane.add(p);
								p = initRow();
								width = 0;
							} else {
								width += doc.getPreferredSize().width;
								width += 2;
							}
							p.add(doc);
						}
					}
			}
			if (p.getComponentCount() == 0) {
				switch (filter) {
					case ADDED_BY_OTHERS:
					case ADDED_BY_ME:
						doc = new DocComponent(null, model);
						tagsDocList.add(doc);
						tagsPane.add(doc);
				}
			} else tagsPane.add(p);
		}
		if (tagsDocList.size() == 0) {
			doc = new DocComponent(null, model);
			tagsDocList.add(doc);
			tagsPane.add(doc);
		}
		tagsPane.revalidate();
		tagsPane.repaint();
	}
	
	/**
	 * Lays out the other annotations.
	 * 
	 * @param list The collection of annotation to layout.
	 */
	private void layoutOthers(Collection list)
	{
		otherPane.removeAll();
		otherList.clear();
		DocComponent doc;
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(1, 2, 1, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;		
		
		if(!CollectionUtils.isEmpty(list)) {
			
			Iterator i = list.iterator();
			while (i.hasNext()) {
				
				c.gridx = 0;
				c.weightx = 0;
				c.fill = GridBagConstraints.NONE;
				
				DataObject item = (DataObject) i.next();
				if(filter==SHOW_ALL || (filter==ADDED_BY_ME && model.isLinkOwner(item)) || (filter==ADDED_BY_OTHERS && model.isAnnotatedByOther(item))) {
					doc = new DocComponent(item, model);
					doc.addPropertyChangeListener(controller);
					
					otherList.add(doc);
					
					otherPane.add(new JLabel(getType((AnnotationData)item)+":"), c);
					
					c.gridx = 1;
					c.weightx = 1;
					c.fill = GridBagConstraints.HORIZONTAL;
					otherPane.add(doc, c);
					
					c.gridy++;
				}
					
			}
		}
		
		otherPane.revalidate();
		otherPane.repaint();
	}
	
	/**
	 * Gets a readable name for the type of Annotation
	 * 
	 * @param d
	 *            The Annotation
	 * @return See above.
	 */
	private String getType(AnnotationData d) {
		if (d instanceof XMLAnnotationData)
			return "XML";
		if (d instanceof BooleanAnnotationData)
			return "Boolean";
		if (d instanceof DoubleAnnotationData)
			return "Double";
		if (d instanceof LongAnnotationData)
			return "Long";
		if (d instanceof TermAnnotationData)
			return "Term";
		if (d instanceof TimeAnnotationData)
			return "Time";
		return "";
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view Reference to the view. Mustn't be <code>null</code>.
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param controller Reference to the control. Mustn't be <code>null</code>.
	 */
	AnnotationDataUI(EditorUI view, EditorModel model, 
			EditorControl controller)
	{
		super(model);
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.controller = controller;
		this.view = view;
		initComponents();
		init = false;
	}
	
	/**
	 * Overridden to lay out the rating.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		selectedValue = 0;
		StringBuffer buffer = new StringBuffer();
		publishedBox.setSelected(model.hasBeenPublished());
		//Add attachments
		Collection l;
		int count = 0;
		//Viewed by
		if (!model.isMultiSelection()) {
			l = model.getTags();
			if (l != null) count += l.size();
			layoutTags(l);
			l = model.getAttachments();
			if (l != null) count += l.size();
			layoutAttachments(l);
			selectedValue = model.getUserRating();
			int n = model.getRatingCount(EditorModel.ALL);
			if (n > 0) {
				buffer.append("(avg:"+model.getRatingAverage(EditorModel.ALL)+
						" | "+n+" vote");
				if (n > 1) buffer.append("s");
				buffer.append(")");
			}
			otherRating.setVisible(n > 0);
			l = model.getOtherAnnotations();
			if (l != null) count += l.size();
			layoutOthers(l);
			
		} else {
			layoutTags(model.getAllTags());
			layoutAttachments(model.getAllAttachments());
			layoutOthers(model.getAllOtherAnnotations());
			selectedValue = model.getRatingAverage(EditorModel.ME);
			int n = model.getRatingCount(EditorModel.ME);
			if (n > 0) {
				buffer.append("out of "+n);
				 buffer.append(" rating");
				if (n > 1) buffer.append("s");
			}
			otherRating.setVisible(true);
		}
		
		otherRating.setText(buffer.toString()); 
		
		initialValue = selectedValue;
		rating.setValue(selectedValue);

		publishedBox.setSelected(model.hasBeenPublished());
		filterButton.setEnabled(count > 0);
		//Allow to handle annotation.
		boolean enabled = model.canAnnotate();
		if (enabled && model.isMultiSelection()) {
			enabled = !model.isAcrossGroups();
		}
		rating.setEnabled(enabled);
		addTagsButton.setEnabled(enabled);
		addDocsButton.setEnabled(enabled);
		
		enabled = model.canDeleteAnnotationLink();
		removeTagsButton.setEnabled(enabled);
		removeDocsButton.setEnabled(enabled);
		removeOtherAnnotationsButton.setEnabled(enabled);
		enabled = model.canDelete(); //to be reviewed
		unrateButton.setEnabled(enabled);
		buildGUI();
	}

	/** Updates the UI when the related nodes have been set.*/
	void onRelatedNodesSet()
	{
		if (!addTagsButton.isEnabled()) return;
		boolean b = model.canAddAnnotationLink();
		addTagsButton.setEnabled(b);
		addDocsButton.setEnabled(b);
		b = model.canDeleteAnnotationLink();
		removeTagsButton.setEnabled(b);
		removeDocsButton.setEnabled(b);
		removeOtherAnnotationsButton.setEnabled(b);
	}

	/**
	 * Attaches the passed file. Returns <code>true</code> if the file
	 * does not already exist, <code>false</code> otherwise.
	 * 
	 * @param files The files to attach.
	 * @return See above
	 */
	boolean attachFiles(File[] files)
	{
		List<FileAnnotationData> list = getCurrentAttachmentsSelection();
		DocComponent doc;
		List<File> toAdd = new ArrayList<File>();
		Object data = null;
		if (filesDocList.size() > 0) {
			Iterator<DocComponent> i = filesDocList.iterator();
			FileAnnotationData fa;
			while (i.hasNext()) {
				doc = i.next();
				data = doc.getData();
				if (data instanceof FileAnnotationData) {
					fa = (FileAnnotationData) data;
					/*
					for (int j = 0; j < files.length; j++) {
						if (fa.getId() <= 0) {
							if (!fa.getFilePath().equals(
									files[j].getAbsolutePath()))
								toAdd.add(files[j]);
							list.add(fa);
						} else {
							if (fa.getFileName().equals(files[j].getName())) {
								toReplace.add(fa);
							} else toAdd.add(files[j]);
						}
					}
					*/
					for (int j = 0; j < files.length; j++) {
						if (fa.getId() >= 0 &&
								fa.getFileName().equals(files[j].getName())) {
							toReplace.add(fa);
						}
					}
				}
			}
		}
		//if (data == null) {
			for (int i = 0; i < files.length; i++) {
				toAdd.add(files[i]);
			}
		//}
		if (toAdd.size() > 0) {
			data = null;
			try {
				docFlag = true;
				Iterator<File> j = toAdd.iterator();
				while (j.hasNext()) {
					list.add(new FileAnnotationData(j.next()));
				}
				
			} catch (Exception e) {} 
			firePropertyChange(EditorControl.SAVE_PROPERTY, 
					Boolean.valueOf(false), Boolean.valueOf(true));
		}
		layoutAttachments(list);
		return toAdd.size() > 0;
	}
	
	/**
	 * Removes the passed file from the display.
	 * 
	 * @param file The file to remove.
	 */
	void removeAttachedFile(Object file)
	{ 
		if (file == null) return;
		FileAnnotationData fData = (FileAnnotationData) file;
		List<FileAnnotationData> attachments = getCurrentAttachmentsSelection();
		Iterator<FileAnnotationData> i = attachments.iterator();
		FileAnnotationData data;
		List<FileAnnotationData> toKeep = new ArrayList<FileAnnotationData>();
		while (i.hasNext()) {
			data = i.next();
			if (data.getId() != fData.getId())
				toKeep.add(data);
		}
		if (filesDocList.size() > 0) {
			Iterator<DocComponent> j = filesDocList.iterator();
			DocComponent doc;
			Object fa;
			while (j.hasNext()) {
				doc = j.next();
				fa = doc.getData();
				if (fa instanceof FileAnnotationData) {
					data = (FileAnnotationData) fa;
					if (data.getId() <= 0 && !data.equals(file)) {
						toKeep.add(data);
					}
				}
			}
		}
		handleObjectsSelection(FileAnnotationData.class, toKeep, true);
	}
	
	/**
	 * Removes a annotation from the view.
	 * 
	 * @param annotation The annotation to remove.
	 */
	void removeAnnotation(AnnotationData annotation)
	{
		if (annotation == null) return;
		List<AnnotationData> toKeep = new ArrayList<AnnotationData>();
		AnnotationData data;
		if (annotation instanceof TagAnnotationData) {
			List<TagAnnotationData> tags = getCurrentTagsSelection();
			Iterator<TagAnnotationData> i = tags.iterator();
			while (i.hasNext()) {
				data = i.next();
				if (data.getId() != annotation.getId())
					toKeep.add(data);
			}
			handleObjectsSelection(TagAnnotationData.class, toKeep, true);
		} else if (annotation instanceof TermAnnotationData ||
				annotation instanceof XMLAnnotationData ||
				annotation instanceof BooleanAnnotationData ||
				annotation instanceof LongAnnotationData ||
				annotation instanceof DoubleAnnotationData) {
			List<AnnotationData> tags = getCurrentOtherSelection();
			Iterator<AnnotationData> i = tags.iterator();
			while (i.hasNext()) {
				data = i.next();
				if (data.getId() != annotation.getId())
					toKeep.add(data);
			}
			handleObjectsSelection(AnnotationData.class, toKeep, true);
		} 
		
	}
	
	/**
	 * Handles the selection of objects via the selection wizard.
	 * 
	 * @param type	  The type of objects to handle.
	 * @param objects The objects to handle.
	 * @param fire 	  Pass <code>true</code> to notify, <code>false</code>
	 * 				  otherwise.
	 */
	void handleObjectsSelection(Class<?> type, Collection objects, boolean fire)
	{
		if (objects == null) return;
		if (TagAnnotationData.class.equals(type)) {
			layoutTags(objects);
			List<Long> ids = new ArrayList<Long>();
			Iterator i = objects.iterator();
			TagAnnotationData tag;
			tagFlag = false;
			Collection tags = model.getAllTags();
			if (tags == null || tags.size() != objects.size()) {
				tagFlag = true;
			} else {
				while (i.hasNext()) {
					tag = (TagAnnotationData) i.next();
					if (tag != null && !model.isAnnotationUsedByUser(tag)) {
						ids.add(tag.getId());
					}
				}
				i = tags.iterator();
				while (i.hasNext()) {
					tag = (TagAnnotationData) i.next();
					if (tag != null && !ids.contains(tag.getId())) {
						tagFlag = true;
						break;
					}
				}
			}
		} else if (FileAnnotationData.class.equals(type)) {
			layoutAttachments(objects);
			List<Long> ids = new ArrayList<Long>();
			Iterator i = objects.iterator();
			FileAnnotationData data;
			docFlag = false;
			Collection attachments = model.getAllAttachments();
			if (attachments == null || attachments.size() != objects.size()) {
				docFlag = true;
			} else {
				while (i.hasNext()) {
					data = (FileAnnotationData) i.next();
					if  (data != null) ids.add(data.getId());
				}
				i = attachments.iterator();
				while (i.hasNext()) {
					data = (FileAnnotationData) i.next();
					if (data != null && !ids.contains(data.getId())) {
						docFlag = true;
						break;
					}
				}
			}
		} else if (AnnotationData.class.equals(type)) {
			layoutOthers(objects);
			List<Long> ids = new ArrayList<Long>();
			Iterator i = objects.iterator();
			AnnotationData data;
			otherFlag = false;
			Collection<AnnotationData> 
			annotations = model.getAllOtherAnnotations();
			if (annotations == null || annotations.size() != objects.size()) {
				otherFlag = true;
			} else {
				while (i.hasNext()) {
					data = (AnnotationData) i.next();
					if  (data != null) ids.add(data.getId());
				}
				i = annotations.iterator();
				while (i.hasNext()) {
					data = (AnnotationData) i.next();
					if (data != null && !ids.contains(data.getId())) {
						otherFlag = true;
						break;
					}
				}
			}
		}
		buildGUI();
		if (fire)
			firePropertyChange(EditorControl.SAVE_PROPERTY, 
					Boolean.valueOf(false), Boolean.valueOf(true));
	}
	
	/**
	 * Returns the tags currently selected. A tag will be added to the list
	 * only if it is linked to all the selected objects
	 * 
	 * @return See above.
	 */
	List<TagAnnotationData> getCurrentTagsSelection()
	{
		List<TagAnnotationData> selection = new ArrayList<TagAnnotationData>();
		if (tagsDocList.size() == 0)  return selection;
		DocComponent doc;
		Object object;
		TagAnnotationData tag;
		Iterator<DocComponent> i = tagsDocList.iterator();
		while (i.hasNext()) {
			doc = i.next();
			object = doc.getData();
			if (object instanceof TagAnnotationData) {
				tag = (TagAnnotationData) object;
				if (tag.getId() > 0)
					selection.add(tag);
			}
		}
		return selection;
	}
	
	/**
	 * Returns the list of attachments currently selected by the user.
	 * 
	 * @return See above.
	 */
	List<FileAnnotationData> getCurrentAttachmentsSelection() 
	{
		List<FileAnnotationData> list = new ArrayList<FileAnnotationData>();
		if (filesDocList.size() == 0)  return list;
		DocComponent doc;
		Object object;
		FileAnnotationData data;
		Iterator<DocComponent> i = filesDocList.iterator();
		while (i.hasNext()) {
			doc = i.next();
			object = doc.getData();
			if (object instanceof FileAnnotationData) {
				data = (FileAnnotationData) object;
				if (data.getId() > 0)
					list.add(data);
			}
		}
		return list;
	}
	
	/**
	 * Returns the other annotations currently selected. 
	 * 
	 * @return See above.
	 */
	List<AnnotationData> getCurrentOtherSelection()
	{
		List<AnnotationData> selection = new ArrayList<AnnotationData>();
		if (otherList.size() == 0)  return selection;
		DocComponent doc;
		Object object;
		AnnotationData tag;
		Iterator<DocComponent> i = otherList.iterator();
		while (i.hasNext()) {
			doc = i.next();
			object = doc.getData();
			if (object instanceof AnnotationData) {
				tag = (AnnotationData) object;
				if (tag.getId() > 0)
					selection.add(tag);
			}
		}
		return selection;
	}
	
	/**
	 * Returns the collection of attachments.
	 * 
	 * @return See above.
	 */
	List<FileAnnotationData> removeAttachedFiles()
	{
		List<FileAnnotationData> list = new ArrayList<FileAnnotationData>();
		if (filesDocList.size() == 0) {
			docFlag = false;
			return list;
		}
		List<FileAnnotationData> toKeep = new ArrayList<FileAnnotationData>();
		FileAnnotationData data;
		DocComponent doc;
		Object object;
		Iterator<DocComponent> i = filesDocList.iterator();
		while (i.hasNext()) {
			doc = i.next();
			object = doc.getData();
			if (doc.canUnlink()) {
				if (object instanceof FileAnnotationData) {
					data = (FileAnnotationData) object;
					if (data.getId() > 0)
						list.add(data);
				}
			} else {
				toKeep.add((FileAnnotationData) object);
			}
		}
		handleObjectsSelection(FileAnnotationData.class, toKeep, false);
		if (list.size() == 0) docFlag = false;
		return list;
	}
	
	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	List<TagAnnotationData> removeTags()
	{
		List<TagAnnotationData> list = new ArrayList<TagAnnotationData>();
		if (tagsDocList.size() == 0)  {
			tagFlag = false;
			return list;
		}
		List<TagAnnotationData> toKeep = new ArrayList<TagAnnotationData>();
		TagAnnotationData data;
		DocComponent doc;
		Object object;
		Iterator<DocComponent> i = tagsDocList.iterator();
		while (i.hasNext()) {
			doc = i.next();
			object = doc.getData();
			if (doc.canUnlink()) {
				if (object instanceof TagAnnotationData) {
					data = (TagAnnotationData) object;
					if (data.getId() > 0)
						list.add(data);
				} 
			} else {
				toKeep.add((TagAnnotationData) object);
			}
		}
		handleObjectsSelection(TagAnnotationData.class, toKeep, false);
		if (list.size() == 0) tagFlag = false;
		return list;
	}
	
	/**
	 * Returns the collection of other annotations.
	 * 
	 * @return See above.
	 */
	List<AnnotationData> removeOtherAnnotation()
	{
		List<AnnotationData> list = new ArrayList<AnnotationData>();
		if (otherList.size() == 0)  {
			otherFlag = false;
			return list;
		}
		List<AnnotationData> toKeep = new ArrayList<AnnotationData>();
		AnnotationData data;
		DocComponent doc;
		Object object;
		Iterator<DocComponent> i = otherList.iterator();
		while (i.hasNext()) {
			doc = i.next();
			object = doc.getData();
			if (doc.canUnlink()) {
				if (object instanceof AnnotationData) {
					data = (AnnotationData) object;
					if (data.getId() > 0)
						list.add(data);
				} 
			} else {
				toKeep.add((AnnotationData) object);
			}
		}
		handleObjectsSelection(AnnotationData.class, toKeep, false);
		if (list.size() == 0) otherFlag = false;
		return list;
	}
	
	/**
	 * Returns <code>true</code> some tags can be unlinked,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasTagsToUnlink()
	{
		if (tagsDocList.size() == 0) return false;
		DocComponent doc;
		Object object;
		Iterator<DocComponent> i = tagsDocList.iterator();
		while (i.hasNext()) {
			doc = i.next();
			object = doc.getData();
			if (doc.canUnlink()) {
				if (object instanceof TagAnnotationData) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> some tags can be unlinked,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasOtherAnnotationsToUnlink()
	{
		if (otherList.size() == 0) return false;
		DocComponent doc;
		Object object;
		Iterator<DocComponent> i = otherList.iterator();
		while (i.hasNext()) {
			doc = i.next();
			object = doc.getData();
			if (doc.canUnlink()) {
				if (object instanceof AnnotationData) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> some tags can be unlinked,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasAttachmentsToUnlink()
	{
		if (filesDocList.size() == 0) return false;
		DocComponent doc;
		Object object;
		Iterator<DocComponent> i = filesDocList.iterator();
		while (i.hasNext()) {
			doc = i.next();
			object = doc.getData();
			if (doc.canUnlink()) {
				if (object instanceof FileAnnotationData) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return ""; }
	
	/**
	 * Returns the collection of annotation to remove.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<Object> getAnnotationToRemove()
	{ 
		List<Object> l = new ArrayList<Object>();
		if (selectedValue != initialValue && selectedValue == 0) {
			RatingAnnotationData rating = model.getUserRatingAnnotation();
			if (rating != null) l.add(rating);
		}
		List<Long> idsToKeep;
		DocComponent doc;
		long id;
		
		Object object;
		Iterator<DocComponent> i;
		Collection original;
		Iterator j;
		AnnotationData annotation;
		idsToKeep = new ArrayList<Long>();
		if (tagFlag) {
			i = tagsDocList.iterator();
			while (i.hasNext()) {
				doc = i.next();
				object = doc.getData();
				if (object instanceof TagAnnotationData) {
					annotation = (AnnotationData) object;
					id = annotation.getId();
					if (id > 0) 
						idsToKeep.add(id);
				}
			}
			
			original = model.getAllTags();
			j = original.iterator();
			while (j.hasNext()) {
				annotation = (AnnotationData) j.next();
				id = annotation.getId();
				if (!idsToKeep.contains(id))// && model.isAnnotationToDelete(tag))
					l.add(annotation);
			}
		}
		if (docFlag) {
			i = filesDocList.iterator();
			while (i.hasNext()) {
				doc = i.next();
				object = doc.getData();
				if (object instanceof FileAnnotationData) {
					annotation = (AnnotationData) object;
					id = annotation.getId();
					if (id > 0) 
						idsToKeep.add(id);
				}
			}
			original = model.getAllAttachments();
			j = original.iterator();
			while (j.hasNext()) {
				annotation = (AnnotationData) j.next();
				id = annotation.getId();
				if (!idsToKeep.contains(id))//  && model.isAnnotationToDelete(fa))
					l.add(annotation);
			}
		}
		if (otherFlag) {
			i = otherList.iterator();
			while (i.hasNext()) {
				doc = i.next();
				object = doc.getData();
				if (object instanceof AnnotationData) {
					annotation = (AnnotationData) object;
					id = annotation.getId();
					if (id > 0) 
						idsToKeep.add(id);
				}
			}
			
			original = model.getAllOtherAnnotations();
			j = original.iterator();
			while (j.hasNext()) {
				annotation = (AnnotationData) j.next();
				id = annotation.getId();
				if (!idsToKeep.contains(id))// && model.isAnnotationToDelete(tag))
					l.add(annotation);
			}
		}
		if (model.hasBeenPublished()) {
			if (!publishedBox.isSelected()) {
				BooleanAnnotationData b = model.getPublishedAnnotation();
				if (b.getValue().booleanValue()) l.add(b);
			}
		}
		
		l.addAll(mapsPane.getEmptyMapAnnotations());
		
		return l; 
	}

	/**
	 * Returns the collection of annotations to add.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{
		List<AnnotationData> l = new ArrayList<AnnotationData>();
		DocComponent doc;
		Iterator<DocComponent> i;
		Object object;
		AnnotationData annotation;
		Iterator j;
		List<Long> ids;
		Collection original;
		long id;
		if (tagFlag) {
			original = model.getAllTags();
			j = original.iterator();
			ids = new ArrayList<Long>();
			while (j.hasNext()) {
				annotation = (AnnotationData) j.next();
				ids.add(annotation.getId());
			}
			
			i = tagsDocList.iterator();
			Map<Long, Integer> map = new HashMap<Long, Integer>();
			Map<Long, AnnotationData> 
				annotations = new HashMap<Long, AnnotationData>();
			Integer count;
			while (i.hasNext()) {
				doc = i.next();
				object = doc.getData();
				if (object instanceof TagAnnotationData) {
					annotation = (AnnotationData) object;
					id = annotation.getId();
					if (!ids.contains(id)) {
						l.add(annotation);
					} else {
						count = map.get(id);
						if (count != null) {
							count++;
							map.put(id, count);
						} else {
							count = 1;
							annotations.put(id, annotation);
							map.put(id, count);
						}
					}
				}
			}
			
			//check the count
			Entry<Long, Integer> entry;
			Iterator<Entry<Long, Integer>> k = map.entrySet().iterator();
			int n = tagsDocList.size();
			Map<DataObject, Boolean> m;
			while (k.hasNext()) {
				entry = k.next();
				count = entry.getValue();
				if (count != null && count == n) {
					//Check if the annotation needs to be added
					annotation = annotations.get(entry.getKey());
					m = model.getTaggedObjects(annotation);
					if (m.size() < count) {
						l.add(annotation);
					}
				}
			}
		}
		i = tagsDocList.iterator();
		while (i.hasNext()) {
			doc = i.next();
			object = doc.getData();
			if (doc.hasBeenModified()) {
				annotation = (AnnotationData) object;
				if (!l.contains(annotation)) l.add(annotation);
			}
		}
		if (docFlag) {
			original = model.getAllAttachments();
			j = original.iterator();
			ids = new ArrayList<Long>();
			while (j.hasNext()) {
				ids.add(((AnnotationData) j.next()).getId());
			}
			i = filesDocList.iterator();
			Map<Long, Integer> map = new HashMap<Long, Integer>();
			Map<Long, AnnotationData> 
				annotations = new HashMap<Long, AnnotationData>();
			Integer count;
			while (i.hasNext()) {
				doc = i.next();
				object = doc.getData();
				if (object instanceof FileAnnotationData) {
					annotation = (AnnotationData) object;
					id = annotation.getId();
					if (!ids.contains(id)) {
						l.add(annotation);
					} else {
						count = map.get(id);
						if (count != null) {
							count++;
							map.put(id, count);
						} else {
							count = 1;
							annotations.put(id, annotation);
							map.put(id, count);
						}
					}
				}
			}
			
			//check the count
			Entry<Long, Integer> entry;
			Iterator<Entry<Long, Integer>> k = map.entrySet().iterator();
			int n = filesDocList.size();
			Map<DataObject, Boolean> m;
			while (k.hasNext()) {
				entry = k.next();
				count = entry.getValue();
				if (count != null && count == n) {
					//Check if the annotation needs to be added
					annotation = annotations.get(entry.getKey());
					m = model.getObjectsWith(annotation);
					if (m.size() < count) {
						l.add(annotation);
					}
				}
			}
		}
		if (otherFlag) {
			original = model.getAllOtherAnnotations();
			j = original.iterator();
			ids = new ArrayList<Long>();
			while (j.hasNext()) {
				ids.add(((AnnotationData) j.next()).getId());
			}
			i = otherList.iterator();
			Map<Long, Integer> map = new HashMap<Long, Integer>();
			Map<Long, AnnotationData> 
				annotations = new HashMap<Long, AnnotationData>();
			Integer count;
			while (i.hasNext()) {
				doc = i.next();
				object = doc.getData();
				if (object instanceof AnnotationData) {
					annotation = (AnnotationData) object;
					id = annotation.getId();
					if (!ids.contains(id)) {
						l.add(annotation);
					} else {
						count = map.get(id);
						if (count != null) {
							count++;
							map.put(id, count);
						} else {
							count = 1;
							annotations.put(id, annotation);
							map.put(id, count);
						}
					}
				}
			}
			
			//check the count
			Entry<Long, Integer> entry;
			Iterator<Entry<Long, Integer>> k = map.entrySet().iterator();
			int n = otherList.size();
			Map<DataObject, Boolean> m;
			while (k.hasNext()) {
				entry = k.next();
				count = entry.getValue();
				if (count != null && count == n) {
					//Check if the annotation needs to be added
					annotation = annotations.get(entry.getKey());
					m = model.getObjectsWith(annotation);
					if (m.size() < count) {
						l.add(annotation);
					}
				}
			}
		}
		if (selectedValue != initialValue)
			l.add(new RatingAnnotationData(selectedValue));
		
		if (!model.hasBeenPublished()) {
			if (publishedBox.isSelected()) {
				BooleanAnnotationData data = new BooleanAnnotationData(true);
				data.setNameSpace(BooleanAnnotationData.INSIGHT_PUBLISHED_NS);
				l.add(data);
			}
		}
		
		l.addAll(mapsPane.getMapAnnotations(true, true));
		
		return l;
	}
	
	/**
	 * Returns <code>true</code> if annotation to save.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		if (tagFlag || docFlag || otherFlag) return true;
		Iterator<DocComponent> i = tagsDocList.iterator();
		while (i.hasNext()) {
			if (i.next().hasBeenModified()) return true;
		}
		if (model.hasBeenPublished()) {
			if (!publishedBox.isSelected()) return true;
		} else {
			if (publishedBox.isSelected()) return true;
		}
		
		if(!mapsPane.getMapAnnotations(true, false).isEmpty()) {
			// just save, don't ask
			view.saveData(true);
			return false;
		}
		
		return (selectedValue != initialValue);
	}

	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData(Object)
	 */
	protected void clearData(Object oldObject)
	{
		if (!init) {
			buildGUI();
			init = true;
		}
		tagNames.clear();
		existingTags.clear();
		selectedValue = 0;
		initialValue = 0;
		otherRating.setText("");
		rating.removePropertyChangeListener(RatingComponent.RATE_PROPERTY, 
				this);
		rating.setValue(selectedValue);
		rating.addPropertyChangeListener(RatingComponent.RATE_PROPERTY, this);
		
		publishedBox.setSelected(false);
		tagsPane.removeAll();
		tagsDocList.clear();
		DocComponent doc = new DocComponent(null, model);
		tagsDocList.add(doc);
		tagsPane.add(doc);
		docPane.removeAll();
		filesDocList.clear();
		doc = new DocComponent(null, model);
		filesDocList.add(doc);
		docPane.add(doc);
		tagFlag = false;
		docFlag = false;
		otherFlag = false;
		otherPane.removeAll();
		otherList.clear();
		content.revalidate();
		content.repaint();
		mapsPane.clear();
		setFileAnnotationSelectable(false);
		revalidate();
		repaint();
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() {}
	
	/**
	 * Sets the title of the component.
	 * @see AnnotationUI#setComponentTitle()
	 */
	protected void setComponentTitle() {}
	
	/**
	 * Sets the currently selected rating value.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (RatingComponent.RATE_PROPERTY.equals(name)) {
			int newValue = (Integer) evt.getNewValue();
			if (newValue != selectedValue) {
				selectedValue = newValue;
				view.saveData(true);
			}
		} else if (RatingComponent.RATE_END_PROPERTY.equals(name)) {
			view.saveData(true);
		}
	}

	/**
	 * Sets the filter.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case SHOW_ALL:
			case ADDED_BY_ME:
			case ADDED_BY_OTHERS:
				if (index != filter) {
					filter = index;
					filterAnnotations();
				}
		}
	}

}

/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AnnotationDataUI 
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.SeparatorOneLineBorder;
import pojos.AnnotationData;
import pojos.BooleanAnnotationData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.RatingAnnotationData;
import pojos.TagAnnotationData;

/** 
 * Components displaying the various annotations linked to the related 
 * object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class AnnotationDataUI
	extends AnnotationUI
	implements PropertyChangeListener
{

	/** Component used to rate the object. */
	private RatingComponent 				rating;
	
	/** Components hosting the tags. */
	private JPanel							tagsPane;
	
	/** Components hosting the attachments. */
	private JPanel							docPane;
	
	/** Components displaying the name of the users who viewed the image. */
	private JPanel							viewedByPane;
	
	/** The last selected value. */
	private int								selectedValue;
	
	/** The initial value of the rating. */
	private int 							initialValue;
	
	/** The index of the view row. */
	private int								viewedByRow;
	
	/** The index of the tag row. */
	private int								tagRow;
	
	/** The index of the published row. */
	private int								publishedRow;
	
	/** The UI component hosting the various annotations. */
	private JPanel							content;
	
	/** Collection of components. */
	private Map<Long, ViewedByComponent>	viewedBy;
	
	/** Button to add tags. */
	private JButton							addTagsButton;
	
	/** Button to add documents. */
	private JButton							addDocsButton;
	
	/** Button to unrate the object. */
	private JButton							unrateButton;
	
	/** Reference to the control. */
	private EditorControl					controller;
	
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
		item = new JMenuItem("New Experiment...");
		item.setEnabled(controller.isSingleMode());
		item.setToolTipText("Create a new experiment.");
		item.addActionListener(controller);
		item.setActionCommand(""+EditorControl.CREATE_NEW_EXPERIMENT);
		docSelectionMenu.add(item);
		return docSelectionMenu;
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		content = new JPanel();
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		tagFlag = false;
		docFlag = false;
		tagNames = new ArrayList<String>();
		tagsDocList = new ArrayList<DocComponent>();
		filesDocList = new ArrayList<DocComponent>();
		existingTags = new HashMap<String, TagAnnotationData>();
		IconManager icons = IconManager.getInstance();
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
				Point p = e.getPoint();
				createDocSelectionMenu().show(addDocsButton, p.x, p.y);
			}
		
		});
		//addDocsButton.addActionListener(controller);
		//addDocsButton.setActionCommand(""+EditorControl.ADD_DOCS);
		UIUtilities.unifiedButtonLookAndFeel(addDocsButton);
		viewedBy = new HashMap<Long, ViewedByComponent>();
		selectedValue = 0;
		initialValue = selectedValue;
		rating = new RatingComponent(selectedValue, 
									RatingComponent.MEDIUM_SIZE);
		rating.setOpaque(false);
		rating.setBackground(UIUtilities.BACKGROUND_COLOR);
		rating.addPropertyChangeListener(RatingComponent.RATE_PROPERTY, this);
		unrateButton = new JButton(icons.getIcon(IconManager.MINUS_12));
		UIUtilities.unifiedButtonLookAndFeel(unrateButton);
		unrateButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		unrateButton.setToolTipText("Unrate.");
		unrateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { rating.setValue(0); }
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
		doc = new DocComponent(null, model);
		filesDocList.add(doc);
		docPane.add(doc);
		viewedByPane = new JPanel();
		viewedByPane.setLayout(new BoxLayout(viewedByPane, BoxLayout.Y_AXIS));
		viewedByPane.setOpaque(false);
		viewedByPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		publishedBox = new JCheckBox();
		publishedBox.setBackground(UIUtilities.BACKGROUND_COLOR);
		publishedBox.addItemListener(new ItemListener() {
		
			public void itemStateChanged(ItemEvent e) {
				firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
			}
		});
	}
	
	/**
	 * Creates a tool bar and adds the passed button to it.
	 * 
	 * @param button The button to add.
	 * @return See above.
	 */
	private JToolBar createBar(JButton button)
	{
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setBorder(null);
		bar.setBackground(UIUtilities.BACKGROUND_COLOR);
		bar.add(button);
		return bar;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JLabel l = new JLabel();
		Font f = l.getFont();
		int size = f.getSize()-1;
		
    	double[] columns = {TableLayout.PREFERRED, 5,
    			TableLayout.PREFERRED, 5, TableLayout.PREFERRED};//DEFAULT_WIDTH};
    	TableLayout layout = new TableLayout();
    	content.setLayout(layout);
    	layout.setColumn(columns);
		int i = 0;
		JPanel p = UIUtilities.buildComponentPanel(publishedBox, 0, 0);
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		layout.insertRow(i, TableLayout.PREFERRED);
		content.add(UIUtilities.setTextFont("published", Font.BOLD, size), 
				"0, "+i);
		content.add(p, "2, "+i);
		publishedRow = i;
		i++;
		
		layout.insertRow(i, TableLayout.PREFERRED);
		p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(UIUtilities.setTextFont("rate", Font.BOLD, size));
		p.add(createBar(unrateButton));
		
		content.add(p, "0, "+i);
		p = UIUtilities.buildComponentPanel(rating, 0, 0);
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		content.add(p, "2, "+i);
		i++;
		layout.insertRow(i, TableLayout.PREFERRED);
		p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(UIUtilities.setTextFont("tag", Font.BOLD, size));
		p.add(createBar(addTagsButton));
		
		content.add(p, "0, "+i+", l, t");
		content.add(tagsPane, "2, "+i);
		tagRow = i;
		i++;
		layout.insertRow(i, TableLayout.PREFERRED);
		p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(UIUtilities.setTextFont("attachment", Font.BOLD, size));
		p.add(createBar(addDocsButton));
		content.add(p, "0, "+i+", l, t");
		content.add(docPane, "2, "+i);
		i++;
		layout.insertRow(i, 0);
		content.add(UIUtilities.setTextFont("viewed by", Font.BOLD, size),
				"0, "+i+", l, t");
		p = UIUtilities.buildComponentPanel(viewedByPane, 0, 0);
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		content.add(viewedByPane, "2, "+i);
		viewedByRow = i;

		
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setBackground(UIUtilities.BACKGROUND);
		setBorder(new SeparatorOneLineBorder());
		add(content);
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
	 * Lays out the passed row. 
	 * 
	 * @param row The row to lay out.
	 * @return See above.
	 */
	private JPanel layoutRow(JPanel row)
	{
		JPanel p = new JPanel(); 
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		Component[] comps = row.getComponents();
		row.removeAll();
		JLabel l;
		for (int i = 0; i < comps.length; i++) {
			row.add(comps[i]);
			if (i < comps.length-1) {
				l = new JLabel();
				l.setBackground(UIUtilities.BACKGROUND_COLOR);
				l.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
				l.setText(", ");
				row.add(l);
			}
		}
		p.add(row);
		return p;
	}
	
	/** 
	 * Lays out the users who viewed the image. 
	 * Returns <code>true</code> if the image has been seen by other users,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean layoutViewedBy()
	{
		//value order by user name.
		Collection views = model.getViewedBy();
		viewedByPane.removeAll();
		viewedBy.clear();
		if (views == null || views.size() == 0) return false;
		Iterator i = views.iterator();
		ViewedByDef def;
		ViewedByComponent comp;
		JPanel p = initRow();
		int width = 0;
		while (i.hasNext()) {
			def = (ViewedByDef) i.next();
			comp = new ViewedByComponent(def, model);
			viewedBy.put(def.getExperimenter().getId(), comp);
			if (width+comp.getPreferredSize().width >= COLUMN_WIDTH) {
				viewedByPane.add(layoutRow(p));
				p = initRow();
				width = 0;
			} else {
				width += comp.getPreferredSize().width;
			}
			p.add(comp);
		}
		if (p.getComponentCount() > 0)
			viewedByPane.add(layoutRow(p));
			
		return true;
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
		if (list != null && list.size() > 0) {
			Iterator i = list.iterator();
			
			while (i.hasNext()) {
				doc = new DocComponent(i.next(), model);
				doc.addPropertyChangeListener(controller);
				filesDocList.add(doc);
				docPane.add(doc);
			}
		}
		if (filesDocList.size() == 0) {
			doc = new DocComponent(null, model);
			filesDocList.add(doc);
			docPane.add(doc);
		}
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
			while (i.hasNext()) {
				doc = new DocComponent(i.next(), model);
				doc.addPropertyChangeListener(controller);
				tagsDocList.add(doc);
			    if (width+doc.getPreferredSize().width >= COLUMN_WIDTH) {
			    	tagsPane.add(p);
			    	p = initRow();
					width = 0;
			    } else {
			    	width += doc.getPreferredSize().width;
			    }
				p.add(doc);
			}
			if (p.getComponentCount() > 0)
				tagsPane.add(p);
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
	 * Creates a new instance.
	 * 
	 * @param model 	 Reference to the model. Mustn't be <code>null</code>.
	 * @param controller Reference to the control. Mustn't be <code>null</code>.
	 */
	AnnotationDataUI(EditorModel model, EditorControl controller)
	{
		super(model);
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		this.controller = controller;
		initComponents();
		init = false;
	}
	
	/** Sets the thumbnails. */
	void setThumbnails()
	{
		Map<Long, BufferedImage> thumbnails = model.getThumbnails();
		if (thumbnails == null) return;
		Set set = thumbnails.entrySet();
		Iterator i = set.iterator();
		Entry entry;
		ViewedByComponent comp;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			comp = viewedBy.get(entry.getKey());
			if (comp != null)
				comp.setThumbnail((BufferedImage) entry.getValue());
		}
	}
	
	/**
	 * Overridden to lay out the rating.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		//rating
		if (!init) {
			buildGUI();
			init = true;
		}
		selectedValue = 0;
		if (!model.isMultiSelection()) 
		    selectedValue = model.getUserRating();
		initialValue = selectedValue;
		rating.setValue(selectedValue);
		publishedBox.setSelected(model.hasBeenPublished());
		//Add attachments
		layoutAttachments(model.getAttachments());
		
		//Viewed by
		Object refObject = model.getRefObject();
		TableLayout layout = (TableLayout) content.getLayout();
		double h = 0;
		double hTag = TableLayout.PREFERRED;//0;
		double hPublished = 0;
		if (!model.isMultiSelection()) {
			if (refObject instanceof ImageData) {
				if (layoutViewedBy()) h = TableLayout.PREFERRED;
			} 
			layoutTags(model.getTags());
			//hTag = TableLayout.PREFERRED;
		}
		//if (refObject instanceof ImageData) hPublished = TableLayout.PREFERRED;
		layout.setRow(publishedRow, hPublished);
		layout.setRow(viewedByRow, h);
		layout.setRow(tagRow, hTag);
		content.revalidate();
		content.repaint();
		revalidate();
		repaint();
		
		//
	}
	
	/**
	 * Attaches the passed file.
	 * 
	 * @param file The file to attach.
	 */
	void attachFile(File file)
	{
		List<FileAnnotationData> list = getCurrentAttachmentsSelection();
		DocComponent doc;
		boolean exist = false;
		Object data;
		if (filesDocList.size() > 0) {
			Iterator<DocComponent> i = filesDocList.iterator();
			FileAnnotationData fa;
			while (i.hasNext()) {
				doc = i.next();
				data = doc.getData();
				if (data instanceof FileAnnotationData) {
					fa = (FileAnnotationData) data;
					if (fa.getId() <= 0) {
						if (fa.getFilePath().equals(file.getAbsolutePath()))
							exist = true;
						list.add(fa);
					}
				}
			}
		}
		if (!exist) {
			data = null;
			try {
				docFlag = true;
				list.add(new FileAnnotationData(file));
			} catch (Exception e) {} 
			firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
					Boolean.TRUE);
		}
		layoutAttachments(list);
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
		handleObjectsSelection(FileAnnotationData.class, toKeep);
	}
	
	/**
	 * Removes a tag from the view.
	 * 
	 * @param tag The tag to remove.
	 */
	void removeTag(TagAnnotationData tag)
	{
		if (tag == null) return;
		List<TagAnnotationData> tags = getCurrentTagsSelection();
		Iterator<TagAnnotationData> i = tags.iterator();
		TagAnnotationData data;
		List<TagAnnotationData> toKeep = new ArrayList<TagAnnotationData>();
		while (i.hasNext()) {
			data = i.next();
			if (data.getId() != tag.getId())
				toKeep.add(data);
		}
		handleObjectsSelection(TagAnnotationData.class, toKeep);
	}
	
	/**
	 * Handles the selection of objects via the selection wizard.
	 * 
	 * @param type	  The type of objects to handle.
	 * @param objects The objects to handle.
	 */
	void handleObjectsSelection(Class type, Collection objects)
	{
		if (objects == null) return;
		if (TagAnnotationData.class.equals(type)) {
			layoutTags(objects);
			List<Long> ids = new ArrayList<Long>();
			Iterator i = objects.iterator();
			TagAnnotationData tag;
			tagFlag = false;
			Collection tags = model.getTags();
			if (tags == null || tags.size() != objects.size()) {
				tagFlag = true;
			} else {
				while (i.hasNext()) {
					tag = (TagAnnotationData) i.next();
					ids.add(tag.getId());
				}
				i = tags.iterator();
				while (i.hasNext()) {
					tag = (TagAnnotationData) i.next();
					if (!ids.contains(tag.getId())) {
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
			Collection attachments = model.getAttachments();
			if (attachments == null || attachments.size() != objects.size()) {
				docFlag = true;
			} else {
				while (i.hasNext()) {
					data = (FileAnnotationData) i.next();
					ids.add(data.getId());
				}
				i = attachments.iterator();
				while (i.hasNext()) {
					data = (FileAnnotationData) i.next();
					if (!ids.contains(data.getId())) {
						docFlag = true;
						break;
					}
				}
			}
		}
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
				Boolean.TRUE);
	}
	
	/**
	 * Returns the tags currently selected.
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
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return ""; }
	
	/**
	 * Returns the collection of rating to remove.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove()
	{ 
		List<AnnotationData> l = new ArrayList<AnnotationData>();
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
		if (tagFlag && !model.isMultiSelection()) {
			idsToKeep = new ArrayList<Long>();
			
			TagAnnotationData tag;
			i = tagsDocList.iterator();
			while (i.hasNext()) {
				doc = i.next();
				object = doc.getData();
				if (object instanceof TagAnnotationData) {
					tag = (TagAnnotationData) object;
					id = tag.getId();
					if (id > 0) 
						idsToKeep.add(id);
				}
			}
			
			original = model.getTags();
			j = original.iterator();
			while (j.hasNext()) {
				tag = (TagAnnotationData) j.next();
				id = tag.getId();
				if (!idsToKeep.contains(id))
					l.add(tag);
			}
		}
		if (docFlag) {
			idsToKeep = new ArrayList<Long>();
			i = filesDocList.iterator();
			FileAnnotationData fa;
			while (i.hasNext()) {
				doc = i.next();
				object = doc.getData();
				if (object instanceof FileAnnotationData) {
					fa = (FileAnnotationData) object;
					id = fa.getId();
					if (id > 0) 
						idsToKeep.add(id);
				}
			}
			
			original = model.getAttachments();
			j = original.iterator();
			while (j.hasNext()) {
				fa = (FileAnnotationData) j.next();
				id = fa.getId();
				if (!idsToKeep.contains(id))
					l.add(fa);
			}
		}
		if (model.hasBeenPublished()) {
			if (!publishedBox.isSelected()) {
				BooleanAnnotationData b = model.getPublishedAnnotation();
				if (b.getValue().booleanValue()) l.add(b);
			}
		}
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
			original = model.getTags();
			j = original.iterator();
			ids = new ArrayList<Long>();
			while (j.hasNext()) {
				ids.add(((AnnotationData) j.next()).getId());
			}
			
			i = tagsDocList.iterator();
			while (i.hasNext()) {
				doc = i.next();
				object = doc.getData();
				if (object instanceof TagAnnotationData) {
					annotation = (AnnotationData) object;
					id = annotation.getId();
					if (!ids.contains(id)) l.add(annotation);
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
			original = model.getAttachments();
			j = original.iterator();
			ids = new ArrayList<Long>();
			while (j.hasNext()) {
				ids.add(((AnnotationData) j.next()).getId());
			}
			i = filesDocList.iterator();
			while (i.hasNext()) {
				doc = i.next();
				object = doc.getData();
				if (object instanceof FileAnnotationData) {
					annotation = (AnnotationData) object;
					id = annotation.getId();
					if (!ids.contains(id)) l.add(annotation);
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
		return l;
	}
	
	/**
	 * Returns <code>true</code> if annotation to save.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		if (tagFlag || docFlag) return true;
		Iterator<DocComponent> i = tagsDocList.iterator();
		while (i.hasNext()) {
			if (i.next().hasBeenModified()) return true;
		}
		if (model.hasBeenPublished()) {
			if (!publishedBox.isSelected()) return true;
		} else {
			if (publishedBox.isSelected()) return true;
		}
		return (selectedValue != initialValue);
	}

	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		if (!init) {
			buildGUI();
			init = true;
		}
		tagNames.clear();
		existingTags.clear();
		selectedValue = 0;
		if (!model.isMultiSelection())
		    selectedValue = model.getUserRating();
		initialValue = 0;
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
		doc = new DocComponent(null, model);
		filesDocList.add(doc);
		docPane.add(doc);
		tagFlag = false;
		docFlag = false;
		double h = TableLayout.PREFERRED;
		//if (!model.isMultiSelection()) h = TableLayout.PREFERRED;
		TableLayout layout = (TableLayout) content.getLayout();
		layout.setRow(tagRow, h);
		content.revalidate();
		content.repaint();
		revalidate();
		repaint();
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() { clearData(); }
	
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
				firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
									Boolean.TRUE);
			}
		}
	}

}

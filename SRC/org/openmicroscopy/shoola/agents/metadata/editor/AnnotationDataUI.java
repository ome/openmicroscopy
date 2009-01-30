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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagCellRenderer;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagItem;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;
import org.openmicroscopy.shoola.util.ui.RatingComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.SeparatorOneLineBorder;
import pojos.AnnotationData;
import pojos.DataObject;
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
	implements DocumentListener, FocusListener, PropertyChangeListener
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
	
	/** The index of the view by Row. */
	private int								viewedByRow;
	
	/** The UI component hosting the various annotations. */
	private JPanel							content;
	
	/** Collection of components. */
	private Map<Long, ViewedByComponent>	viewedBy;
	
	/** Button to add tags. */
	private JButton							addTagsButton;
	
	/** Button to add documents. */
	private JButton							addDocsButton;
	
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
	
	/** Flag indicating that the tags are loaded for autocomplete. */
	private boolean							autoComplete;
	
	/** The dialog displaying the possible tags. */
	private HistoryDialog					autoCompleteDialog;

	/** 
	 * The selection menu to attach either local documents or already
	 * upload files. 
	 */
	private JPopupMenu						docSelectionMenu;
	
	/** Collection of tags documents objects. */
	private List<DocComponent>				tagsDocList;
	
	/** Collection of tags documents objects. */
	private List<DocComponent>				filesDocList;
	
	/**
	 * Creates the selection menu.
	 * 
	 * @return See above.
	 */
	private JPopupMenu createDocSelectionMenu()
	{
		if (docSelectionMenu != null) return docSelectionMenu;
		docSelectionMenu = new JPopupMenu();
		JMenuItem item = new JMenuItem("Local document");
		item.setToolTipText("Upload and attach a local document.");
		item.addActionListener(controller);
		item.setActionCommand(""+EditorControl.ADD_LOCAL_DOCS);
		docSelectionMenu.add(item);
		item = new JMenuItem("Uploaded document");
		item.setToolTipText("Attach a document already uploaded.");
		item.addActionListener(controller);
		item.setActionCommand(""+EditorControl.ADD_UPLOADED_DOCS);
		docSelectionMenu.add(item);
		return docSelectionMenu;
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		tagFlag = false;
		docFlag = false;
		tagNames = new ArrayList<String>();
		tagsDocList = new ArrayList<DocComponent>();
		filesDocList = new ArrayList<DocComponent>();
		existingTags = new HashMap<String, TagAnnotationData>();
		IconManager icons = IconManager.getInstance();
		addTagsButton = new JButton(icons.getIcon(IconManager.PLUS));
		UIUtilities.unifiedButtonLookAndFeel(addTagsButton);
		addTagsButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		addTagsButton.setToolTipText("Add Tags.");
		addTagsButton.addActionListener(controller);
		addTagsButton.setActionCommand(""+EditorControl.ADD_TAGS);
		addDocsButton = new JButton(icons.getIcon(IconManager.PLUS));
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
		/*
		tagsPane = new JTextPane();
		tagsPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		tagsPane.setForeground(UIUtilities.DEFAULT_FONT_COLOR);
		tagsPane.setText(DEFAULT_TEXT);
		tagsPane.addFocusListener(this);
		tagsPane.getDocument().addDocumentListener(this);
		*/
		tagsPane = new JPanel();
		tagsPane.setLayout(new BoxLayout(tagsPane, BoxLayout.Y_AXIS));
		tagsPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		DocComponent doc = new DocComponent(null, model);
		tagsDocList.add(doc);
		tagsPane.add(doc);
		/*
		urlPane = new JPanel();
		urlPane.setLayout(new BoxLayout(urlPane, BoxLayout.Y_AXIS));
		urlPane.add(new URLComponent(null, model));
		*/
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
		
		/*
		Action a = createEnterAction();
		Object key = a.getValue(Action.NAME);
		tagsPane.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), key);
		tagsPane.getActionMap().put(key, a);
		a = createVKUpAction();
		key = a.getValue(Action.NAME);
		tagsPane.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), key);
		tagsPane.getActionMap().put(key, a);
		a = createVKDownAction();
		key = a.getValue(Action.NAME);
		tagsPane.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), key);
		tagsPane.getActionMap().put(key, a);
		*/
	}
	
	/**
	 * Creates the action associated to the <code>Enter</code> key.
	 * 
	 * @return See above.
	 */
	private Action createEnterAction()
	{
		return new AbstractAction("enterPressed") {
		
			public void actionPerformed(ActionEvent e) {
				handleKeyEnterPressed();
			}
		};
	}
	
	/**
	 * Creates the action associated to the <code>VK_UP</code> key.
	 * 
	 * @return See above.
	 */
	private Action createVKUpAction()
	{
		return new AbstractAction("enterVKUp") {
		
			public void actionPerformed(ActionEvent e) {
				if (autoCompleteDialog != null && 
						autoCompleteDialog.isVisible())
					autoCompleteDialog.setSelectedIndex(false);
			}
		};
	}
	
	/**
	 * Creates the action associated to the <code>VK_UP</code> key.
	 * 
	 * @return See above.
	 */
	private Action createVKDownAction()
	{
		return new AbstractAction("enterVKUp") {
		
			public void actionPerformed(ActionEvent e) {
				if (autoCompleteDialog != null && 
						autoCompleteDialog.isVisible())
					autoCompleteDialog.setSelectedIndex(true);
			}
		};
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
		content = new JPanel();
    	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    	double[] columns = {TableLayout.PREFERRED, 5, COLUMN_WIDTH};
    	TableLayout layout = new TableLayout();
    	content.setLayout(layout);
    	layout.setColumn(columns);
		int i = 0;
		JPanel p = UIUtilities.buildComponentPanel(rating, 0, 0);
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		layout.insertRow(i, TableLayout.PREFERRED);
		content.add(UIUtilities.setTextFont("rate", Font.BOLD, size), "0, "+i);
		content.add(p, "2, "+i);
		i++;
		layout.insertRow(i, TableLayout.PREFERRED);
		p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		p.add(UIUtilities.setTextFont("tag", Font.BOLD, size));
		p.add(createBar(addTagsButton));
		
		content.add(p, "0, "+i+", l, t");
		content.add(tagsPane, "2, "+i);
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
	 * Adds the selected tag when the <code>Enter</code> key is pressed.
	 */
	private void handleKeyEnterPressed()
	{
		/*
		if (autoCompleteDialog == null || !autoCompleteDialog.isVisible())
			return;
		String name = tagsPane.getText();
		if (name == null) return;
		TagItem o = (TagItem) autoCompleteDialog.getSelectedTextValue();
		if (o == null) return;
		DataObject ho = o.getDataObject();
		tagsPane.getDocument().removeDocumentListener(this);
		if (ho instanceof TagAnnotationData) 
			handleAutoCompleteTagEnter((TagAnnotationData) ho);
			*/
		
	}
	
	/** Creates the dialog displaying the available tags. */
	private void startAutoComplete()
	{
		if (autoCompleteDialog != null) return;
		Rectangle r = tagsPane.getBounds();
		Object[] data = null;
		Collection l = model.getExistingTags();
		if (l != null && l.size() > 0) {
			data = new Object[l.size()];
			Iterator j = l.iterator();
			DataObject object;
			Collection usedTags = model.getTags();
			
			TagItem item;
			int i = 0;
			while (j.hasNext()) {
				object = (DataObject) j.next();
				item = new TagItem(object);
				if (usedTags != null && usedTags.contains(object)) 
					item.setAvailable(false);
				data[i] = item;
				i++;
			}
			long id = MetadataViewerAgent.getUserDetails().getId();
			autoCompleteDialog = new HistoryDialog(data, r.width);
			autoCompleteDialog.setListCellRenderer(new TagCellRenderer(id));
			autoCompleteDialog.addPropertyChangeListener(
					HistoryDialog.SELECTION_PROPERTY, this);
		}
	}
	
	/**
	 * Removes the latest text entry and adds the tag.
	 * 
	 * @param tag Teh tag to add.
	 */
	private void handleAutoCompleteTagEnter(TagAnnotationData tag)
	{
		/*
		String ns = tag.getNameSpace();
		if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
			Set<TagAnnotationData> tags = tag.getTags();
			if (tags != null) {
				Iterator<TagAnnotationData> k = tags.iterator();
				while (k.hasNext())
					handleAutoCompleteTagEnter(k.next());
			}
			return;
		}
    	String[] names = tagsPane.getText().split(SearchUtil.COMMA_SEPARATOR);
    	String value = "";
    	int n = names.length-1;
    	for (int i = 0; i < n; i++) {
    		value += names[i];
    		if (i != n-1) value += SearchUtil.COMMA_SEPARATOR;
		}
    	tagsPane.getDocument().removeDocumentListener(this);
    	tagsPane.setText(value);
    	tagsPane.getDocument().addDocumentListener(this);
    	handleTagEnter(tag);
    	*/
	}
	
	/**
	 * Handles a tag addition.
	 * 
	 * @param tag The value to handle.
	 */
	private void handleTagEnter(TagAnnotationData tag)
	{
		/*
		String ns = tag.getNameSpace();
		if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
			Set<TagAnnotationData> tags = tag.getTags();
			if (tags != null) {
				Iterator<TagAnnotationData> k = tags.iterator();
				while (k.hasNext())
					handleTagEnter(k.next());
			}
			return;
		}
		String value = tagsPane.getText();
		String[] names = value.split(SearchUtil.COMMA_SEPARATOR);
		List<String> values = new ArrayList<String>();
		String tagValue = tag.getContentAsString();
		boolean exist = false;
		for (int i = 0; i < names.length; i++) {
			if (!names[i].equals(DEFAULT_TEXT)) {
				values.add(names[i]);
				if (tagValue.equals(names[i])) 
					exist = true;
			}
		}
		if (!exist) {
			existingTags.put(tag.getTagValue(), tag);
			tagFlag = true;
			values.add(tagValue);
			firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
					Boolean.TRUE);
		}
		layoutTags(values);
		*/
	}
	
	/** Handles the text removal. */
	private void handleTextRemoval()
	{
		/*
		String value = tagsPane.getText();
		String[] names = value.split(SearchUtil.COMMA_SEPARATOR);
		int existing = 0;
		int newTag = 0;
		String v;
		for (int i = 0; i < names.length; i++) {
			v = names[i];
			v = v.trim();
			if (v.length() > 0) {
				if (!v.equals(DEFAULT_TEXT)) {
					if (tagNames.contains(names[i])) existing++;
					else newTag++;
				}
			}
		}
		if (existing != tagNames.size()) tagFlag = true;
		else tagFlag = newTag > 0;
		*/
	}
	
	/** Handles tag insert, autocomplete if fast connection. */
	private void handleTextInsert()
	{
		//Check if fast connection.
		/*
		Collection tags = model.getExistingTags();
		if (tags == null) {
			autoComplete = true;
			controller.loadExistingTags();
		}
		startAutoComplete();
		String name = tagsPane.getText();
		setSelectedTextValue(name.split(SearchUtil.COMMA_SEPARATOR));
		tagFlag = true;
		*/
	}
	
	/**
	 * Sets the selected value in the history list.
	 * 
	 * @param names The names already set.
	 */
    private void setSelectedTextValue(String[] names)
    {
        if (autoCompleteDialog == null) return;
        int l = names.length;
        if (l > 0) {
        	if (autoCompleteDialog.setSelectedTextValue(names[l-1].trim())) {
        		setAutoCompleteVisible(true);
        		tagsPane.requestFocus();
        	} else setAutoCompleteVisible(false);
        }	
    }
    
    /**
     * Shows or hides the autocomplete dialog. 
     * 
     * @param visible 	Pass <code>true</code> to show the window,
     * 					<code>false</code> to hide it.
     */
    private void setAutoCompleteVisible(boolean visible)
    {
    	if (visible) {
    		Rectangle r = tagsPane.getBounds();
    		autoCompleteDialog.show(tagsPane, 0, r.height);
    	} else {
    		autoCompleteDialog.setVisible(false);
    	}
    }
    
	/**
	 * Lays out the tags.
	 * 
	 * @param values The values to lay out.
	 */
    /*
	private void layoutTags(List<String> values)
	{
		Iterator i = values.iterator();
		StringBuffer buffer = new StringBuffer();
		String tag;
		String value = "";
		String s;
		while (i.hasNext()) {
			tag = (String) i.next();
			if (tag != null && tag.trim().length() > 0) {
				s = value+tag;
				if (s.length() > COLUMN_WIDTH) {
					buffer.append(value);
					buffer.append("\n");
					value = "";
				} else {
					if (value.length() == 0) value += tag;
					else value = value+", "+tag;
				}
			}
		}
		if (value.length() > 0) {
			buffer.append(value);
		}
		tagsPane.getDocument().removeDocumentListener(this);
		tagsPane.setText(buffer.toString());
		tagsPane.getDocument().addDocumentListener(this);
	}
	*/
    
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
		
		/*
		Collection l = model.getAttachments();
		Iterator i;
		DocComponent doc;
		docPane.removeAll();
		filesDocList.clear();
		if (l != null && l.size() > 0) {
			i = l.iterator();
			while (i.hasNext()) {
				doc = new DocComponent(i.next(), model, false);
				doc.addPropertyChangeListener(controller);
				filesDocList.add(doc);
				docPane.add(doc);
			}
		}
		if (list != null && list.size() > 0) {
			i = list.iterator();
			while (i.hasNext()) {
				docPane.add((DocComponent) i.next());
			}
		}
		if (filesDocList.size() == 0) {
			doc = new DocComponent(null, model, false);
			filesDocList.add(doc);
			docPane.add(doc);
		}
			
		docPane.revalidate();
		*/
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
				
				//tagsPane.add(doc);
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
		buildGUI();
	}
	
	/** Sets the thumbnails. */
	void setThumbnails()
	{
		Map<Long, BufferedImage> thumbnails = model.getThumbnails();
		if (thumbnails == null) return;
		Iterator<Long> i = thumbnails.keySet().iterator();
		ViewedByComponent comp;
		Long id;
		while (i.hasNext()) {
			id = i.next();
			comp = viewedBy.get(id);
			if (comp != null)
				comp.setThumbnail(thumbnails.get(id));
		}
	}
	
	/**
	 * Overridden to lay out the rating.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		/*
		Collection tags = model.getTags();
		
		if (tags != null) {
			Iterator i = tags.iterator();
			while (i.hasNext()) {
				tagNames.add(((TagAnnotationData) i.next()).getTagValue());
			}
		}
		*/
		//rating
		selectedValue = 0;
		if (!model.isMultiSelection()) 
		    selectedValue = model.getUserRating();
		initialValue = selectedValue;
		rating.setValue(selectedValue);

		//tags
		layoutTags(model.getTags());
		/*
		Iterator i;
		Collection l = model.getTags();
		if (l != null && l.size() > 0) {
			i = l.iterator();
			List<String> values = new ArrayList<String>();
			while (i.hasNext()) 
				values.add(((AnnotationData) i.next()).getContentAsString());
			//layoutTags(values);
		}
		*/
		//Add attachments
		layoutAttachments(model.getAttachments());
		
		//Viewed by
		Object refObject = model.getRefObject();
		TableLayout layout = (TableLayout) content.getLayout();
		double h = 0;
		if (!model.isMultiSelection()) {
			if (refObject instanceof ImageData) {
				if (layoutViewedBy()) h = TableLayout.PREFERRED;
			} 
		}
		layout.setRow(viewedByRow, h);
		content.revalidate();
		content.repaint();
		revalidate();
		repaint();
	}
	
	/**
	 * Returns the value of the autocomplete flag.
	 * 
	 * @return See above.
	 */
	boolean isAutoComplete() { return autoComplete; }
	
	/** 
	 * Displays the existing in the <code>SelectionWizard</code>
	 * or in the UI component used for code completion.
	 */
	void setExistingTags()
	{
		/*
		if (autoComplete) {
			startAutoComplete();
			String name = tagsPane.getText();
			setSelectedTextValue(name.split(SearchUtil.COMMA_SEPARATOR));
		}
		autoComplete = false;
		*/
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
		File f;
		boolean exist = false;
		Object data;
		if (filesDocList.size() > 0) {
			Iterator<DocComponent> i = filesDocList.iterator();
			while (i.hasNext()) {
				doc = i.next();
				data = doc.getData();
				if (data instanceof File) {
					f = (File) doc.getData();
					if (f.equals(file)) exist = true;
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
			/*
		if (file instanceof File) {
			try {
				FileAnnotationData data = new FileAnnotationData((File) file);
				removeFile(data);
			} catch (Exception e) {}
		} else if (file instanceof FileAnnotationData)
			removeFile((FileAnnotationData) file);
		*/
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
		if (tagFlag) {
			List<Long> idsToKeep = new ArrayList<Long>();
			DocComponent doc;
			TagAnnotationData tag;
			long id;
			
			Object object;
			Iterator<DocComponent> i = tagsDocList.iterator();
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
			
			Collection original = model.getTags();
			Iterator j = original.iterator();
			while (j.hasNext()) {
				tag = (TagAnnotationData) j.next();
				id = tag.getId();
				if (!idsToKeep.contains(id))
					l.add(tag);
			}
			/*
			 * 
			String value = tagsPane.getText();
			String[] names = value.split(SearchUtil.COMMA_SEPARATOR);
			String v;
			List<String> values = new ArrayList<String>();
			Map<String, TagAnnotationData> 
				tags = new HashMap<String, TagAnnotationData>();
			Collection col = model.getTags();
			if (col != null) {
				Iterator j = col.iterator();
				TagAnnotationData tag;
				while (j.hasNext()) {
					tag = (TagAnnotationData) j.next();
					tags.put(tag.getTagValue(), tag);
				}
			}
			
			for (int i = 0; i < names.length; i++) {
				v = names[i];
				v = v.trim();
				if (v.length() > 0) {
					if (!v.equals(DEFAULT_TEXT)) {
						if (tagNames.contains(names[i])) {
							values.add(v);
						}
					}
				}
			}
			if (values.size() != tags.size()) {
				Iterator<String> k = values.iterator();
				while (k.hasNext()) {
					tags.remove(k.next());
				}
				k = tags.keySet().iterator();
				while (k.hasNext()) {
					l.add(tags.get(k.next()));
				}
			}
			*/
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
		return (selectedValue != initialValue);
	}

	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
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
		/*
		tagsPane.getDocument().removeDocumentListener(this);
		tagsPane.setText(DEFAULT_TEXT);
		tagsPane.getDocument().addDocumentListener(this);
		*/
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
		} else if (HistoryDialog.SELECTION_PROPERTY.equals(name)) {
			Object item = evt.getNewValue();
			if (!(item instanceof TagItem)) return;
			DataObject ho = ((TagItem) item).getDataObject();
			if (ho instanceof TagAnnotationData) 
				handleAutoCompleteTagEnter((TagAnnotationData) ho);
		}
	}
	
	/**
	 * Synchronizes controls when new text values are entered in the
	 * {@link #tagsPane}.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		handleTextInsert();
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
				Boolean.TRUE);
	}

	/**
	 * Synchronizes controls when new text values are entered in the
	 * {@link #tagsPane}.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		handleTextRemoval();
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
				Boolean.TRUE);
	}
	
	/**
	 * Resets the default text of the text fields if <code>null</code> or
	 * length <code>0</code>.
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e)
	{
		Object src = e.getSource();
		String text;
		if (src == tagsPane) {
			/*
			text = tagsPane.getText();
			if (text == null || text.length() == 0) {
				tagsPane.getDocument().removeDocumentListener(this);
				tagsPane.setText(DEFAULT_TEXT);
				tagsPane.getDocument().addDocumentListener(this);
			}
			*/
		}
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation 
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}

	/**
	 * Required by the {@link FocusListener} I/F but no-op implementation 
	 * in our case.
	 * @see FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent e) {}

}

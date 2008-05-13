/*
 * org.openmicroscopy.shoola.agents.metadata.editor.TagsUI 
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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.util.AnnotationEditor;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagCellRenderer;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagItem;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;

/** 
 * UI component displaying the tags.
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
class TagsUI 
	extends AnnotationUI 
	implements ActionListener, DocumentListener, PropertyChangeListener
{

	/** The title associated to this component. */
	private static final String TITLE = "Tags ";
	
	/** Action id indicating to add new url area. */
	private static final String	ADD_ACTION = "add";
	
	/** Action id indicating to add new url area. */
	private static final String	NEW_ACTION = "new";
	
	/** Text indicating how to use the tag entries. */
	private static final String DESCRIPTION = "Separate tags with " +
												""+SearchUtil.COMMA_SEPARATOR;
	
	/** Color for the tags selected. */
	private static final String	DESCRIPTION_EXISTING_TAGS = "Double click on " +
			"the tag to edit it.";
	
	/** Button to add existing tags. */
	private JButton					addButton;
	
	/** Button to create new tags. */
	private JButton					newButton;
	
    /** Area where to enter the name of the tags. */
    private JTextField				nameArea;
     
    /** Area where to enter the description of the tags. */
    private JTextArea				descriptionArea; 
    
    /** Collection of tags to add. */
    private List<AnnotationData>	addedTags;
    
    /** Collection of tags to remove. */
    private List<AnnotationData>	removedTags;
    
    /** Collection of selected tags. */
    private List<TagComponent>		selectedTags;
    
    /** Menu used to manage the tags. */
    private TagPopupMenu			menu;
    
    /** The dialog displaying the existing tags. */
    private HistoryDialog			historyDialog;

    /** 
     * Flag indicating that the tags retrieved have to be diplayed in 
     * the <code>SelectionWizard</code>/
     */
    private boolean					wizard;

    /** The UI component hosting the existing tags. */
    private JPanel					existingTags;
    
    /** The location of the mouse click. */
    private Point					popupPoint;
	
    /** The edited tag component. */
    private TagComponent			editedTag;
    
	/** The border displaying the title. */
	private TitledLineBorder		border;
	
	
    /** Loads the tags and adds code completion. */
    private void handleTagInsert()
    {
    	Collection l = model.getExistingTags();
		if (l == null) {
			wizard = false;
			model.loadExistingTags();
		}
		codeCompletion();
		String name = nameArea.getText();
        setSelectedTextValue(name.split(SearchUtil.COMMA_SEPARATOR));
    }
    
    /** Shows the dialog. */
    private void showHistoryDialog()
    {
    	Rectangle r = nameArea.getBounds();
		historyDialog.show(nameArea, 0, r.height);
    }
    
    /** Removes the latest entry from the text area. */
    private void removeLatestTextEntry()
    {
    	String name = nameArea.getText();
    	String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
    	String value = "";
    	int n = names.length-1;
    	for (int i = 0; i < n; i++) {
    		value += names[i];
    		if (i != n-1) value += SearchUtil.COMMA_SEPARATOR;
		}
        nameArea.setText(value);
    }
	
    /**
     * Returns <code>true</code> is the tag has already been added
     * or is already linked to the object, <code>false</code> otherwise.
     * 
     * @param data The tag to handle.
     * @return See above.
     */
    private boolean isTagEntered(TagAnnotationData data)
    {
    	Collection tags = model.getTags();
    	Iterator i;
    	TagAnnotationData tag;
    	long tagID = data.getId();
    	if (tags != null) {
    		i = tags.iterator();
    		while (i.hasNext()) {
    			tag = (TagAnnotationData) i.next();
    			if (tagID == tag.getId())
    				return true;
			}
    	}
    	i = addedTags.iterator();
    	while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (tagID == tag.getId())
				return true;
		}
    	return false;
    }
    
	/**
	 * Invokes when a tag is selected by pressing the <code>enter</code> key.
	 * 
	 * @param data The selected value.
	 */
	private void handleTagEnter(TagAnnotationData data)
	{
		if (data == null) return;
		removeLatestTextEntry();
		if (isTagEntered(data)) return;
		addedTags.add(data);
	}

    /**
     * Sets the selected value in the history list.
     * 
     * @param names The names already set.
     */
    private void setSelectedTextValue(String[] names)
    {
        if (historyDialog == null) return;
        int l = names.length;
        if (l > 0) {
        	if (historyDialog.setSelectedTextValue(names[l-1].trim())) {
        		showHistoryDialog();
        		nameArea.requestFocus();
        	} else historyDialog.setVisible(false);
        }	
    }
    
	/** 
	 * Displays the selected item in the name area when the user
	 * presses the <code>Enter</code> key.
	 */
	private void handleEnter()
	{
		if (historyDialog == null || !historyDialog.isVisible())
			return;
		String name = nameArea.getText();
		if (name == null) return;
		TagItem o = (TagItem) historyDialog.getSelectedTextValue();
		if (o == null) return;
		DataObject ho = o.getDataObject();
		if (ho instanceof TagAnnotationData) 
			handleTag((TagAnnotationData) ho);
	}
	
	/**
	 * Handles the addition of tag.
	 * 
	 * @param tag The tag to handle.
	 */
	private void handleTag(TagAnnotationData tag)
	{
		handleTagEnter(tag);
		createExistingTagsPane();
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}
	/**
	 * Displays the description of the category if it exists.
	 * 
	 * @param p The location of the mouse pressed.
	 */
	private void handleMousePressed(Point p)
	{
		String name = nameArea.getText();
		if (name == null || name.length() == 0) return;
		String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
		Rectangle2D r = new Rectangle();
		Graphics context = nameArea.getGraphics();
		int l = 0;
		String v;
		int vl;
		Iterator j;
		TagAnnotationData item;
		/*
		Collection tags = model.getTags();
		for (int i = 0; i < names.length; i++) {
			v = names[i];
			if (metrics != null)
				r = metrics.getStringBounds(v, context);
			vl = (int) r.getWidth();
			if (p.x >= l && p.x <= (l+vl)) {
				v = v.trim();
				if (tags != null) {
					j = tags.iterator();
					while (j.hasNext()) {
						item = (TagAnnotationData) j.next();
						if (item.getTagValue().equals(v)) {
							//descriptionArea.setText(item.getT);
							return;
						}
					}
				}
				
				//descriptionArea.setText("");
				return;
			}
			l += vl;
		}
		*/
	}
	
	/** Initializes the {@link HistoryDialog} used for code completion. */
    private void codeCompletion()
    {
    	if (historyDialog != null) return;
    	Rectangle r = nameArea.getBounds();
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
    		historyDialog = new HistoryDialog(data, r.width);
			historyDialog.setListCellRenderer(new TagCellRenderer(id));
			historyDialog.addPropertyChangeListener(
					HistoryDialog.SELECTION_PROPERTY, this);
		}
    }
    
	/** Initializes the UI components. */
	private void initComponents()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		addButton = new JButton("Available...");
		addButton.setActionCommand(ADD_ACTION);
		addButton.addActionListener(this);
		newButton = new JButton("New...");
		newButton.setActionCommand(NEW_ACTION);
		newButton.addActionListener(this);
		nameArea = new JTextField();
		nameArea.getDocument().addDocumentListener(this);
		
		nameArea.addKeyListener(new KeyAdapter() {

            /** Finds the phrase. */
            public void keyPressed(KeyEvent e)
            {
            	Object source = e.getSource();
            	if (source != nameArea) return;
            	switch (e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						handleEnter();
						break;
					case KeyEvent.VK_UP:
						if (historyDialog != null && historyDialog.isVisible())
							historyDialog.setSelectedIndex(false);
						break;
					case KeyEvent.VK_DOWN:
						if (historyDialog != null && historyDialog.isVisible())
							historyDialog.setSelectedIndex(true);
						break;
				}
                
            }
        });
		nameArea.addMouseListener(new MouseAdapter() {
    		
			/**
			 * Displays the description of the tag 
			 * @see MouseAdapter#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e) {
				handleMousePressed(e.getPoint());
			}
		
		});

		descriptionArea = new JTextArea();
		descriptionArea.setBorder(
					BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		addedTags = new ArrayList<AnnotationData>();
		removedTags = new ArrayList<AnnotationData>();
		selectedTags = new ArrayList<TagComponent>();
	}
	
	/** 
	 * Lays out the newly created tags.
	 * 
	 * @return See above.
	 */
	private JPanel layoutNewTags()
	{
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		p.add(new JLabel("New Tags"), c);
		c.gridx++;
		p.add(Box.createHorizontalStrut(5), c);
		c.gridx++;
		c.weightx = 0.5;
		p.add(nameArea, c);
		c.gridy++;
		p.add(UIUtilities.setTextFont(DESCRIPTION, Font.ITALIC, 10), c);
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0;
		p.add (new JLabel("Description"), c);
		c.gridx++;
		p.add(Box.createHorizontalStrut(5), c);
		c.gridx++;
		c.weightx = 0.5;
		c.ipady = 60; 
		p.add (descriptionArea, c);
		return p;
	}
	
	/**
	 * Builds and lays out the UI component hosting the {@link #existingTags}.
	 * 
	 * @return See above.
	 */
	private JPanel createExistingTagsPane()
	{
		if (existingTags == null) existingTags = new JPanel();
		else existingTags.removeAll();
		
		Collection l = model.getTags();
		Iterator i;
		AnnotationData data;
		List<TagComponent> tags = new ArrayList<TagComponent>();
		if (l != null) {
			i = l.iterator();
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (!isRemoved(data)) 
					tags.add(new TagComponent(this, data));
			}
		}
		
		
		//Lay out tags
		existingTags.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		existingTags.add(UIUtilities.setTextFont("Tagged with: "), c);
		c.gridx++;
		c.weightx = 0.5;

		//Layout the tags
		existingTags.add(layoutTags(tags), c);
		
		c.gridy++;
		c.gridx = 0;
		existingTags.add(Box.createVerticalStrut(5));
		c.gridy++;
		c.weightx = 0;
		existingTags.add(UIUtilities.setTextFont("Tags to Add: "), c);
		c.gridx++;
		c.weightx = 0.5;
		
		if (addedTags.size() > 0) {
			i = addedTags.iterator();
			tags.clear();
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				tags.add(new TagComponent(this, data));
			}
			existingTags.add(layoutTags(tags), c);
		}
		c.gridy++;
		c.gridx = 0;
		existingTags.add(Box.createVerticalStrut(5));
		c.gridx++;
		c.gridy++;
		existingTags.add(UIUtilities.setTextFont(DESCRIPTION_EXISTING_TAGS, 
												Font.ITALIC, 10), c);
		existingTags.revalidate();
		existingTags.repaint();
		return existingTags;
	}
	
	/**
	 * Lays out the list of <code>TagComponent</code>s
	 * 
	 * @param tags The collection to layout.
	 * @return See above.
	 */
	private JPanel layoutTags(List<TagComponent> tags)
	{
		JPanel pane = new JPanel();
		if (tags.size() == 0) return pane;
		Iterator<TagComponent> i = tags.iterator();
		TagComponent tag;
		
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridx = 0;
		c.gridy = 0;
		int index = 0;
		pane.add(Box.createHorizontalStrut(2), c);
		int n = tags.size();
		JPanel row = new JPanel();
		row.setBackground(UIUtilities.BACKGROUND);
		row.setLayout(new GridBagLayout());
		GridBagConstraints cRow = new GridBagConstraints();
		cRow.anchor = GridBagConstraints.FIRST_LINE_START;
		cRow.gridx = 0;
		while (i.hasNext()) {
			tag = i.next();
			if (n > 1) 
				tag.setSeparator(SearchUtil.COMMA_SEPARATOR);
			
			++cRow.gridx;
			row.add(tag, cRow);
			++cRow.gridx;
			row.add(Box.createHorizontalStrut(5), cRow);
			n--;
			if (index%2 == 0 && index != 0) {
				pane.add(row, c);
				c.gridy++;
				row = new JPanel();
				row.setLayout(new GridBagLayout());
				row.setBackground(UIUtilities.BACKGROUND);
				cRow.gridx = 0;
			}
			index++;
		}
		
		if (row.getComponentCount() > 0)
			pane.add(row, c);
		JPanel content = new JPanel();
		content.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		content.add(pane);
		pane.setBackground(UIUtilities.BACKGROUND);
		content.setBackground(UIUtilities.BACKGROUND);
		content.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return content;
	}
	
	/** Shows the collection of existing tags. */
	private void showSelectionWizard()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		Collection l = model.getExistingTags();
		List<Object> r = new ArrayList<Object>();
		Collection tags = model.getTags();
		Iterator i;
		Set<Long> ids = new HashSet<Long>();
		AnnotationData data;
		if (tags != null) {
			i = tags.iterator();
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (!isRemoved(data)) 
					ids.add(data.getId());
			}
		}
		
		if (addedTags.size() > 0 && tags != null) {
			i = tags.iterator();
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (!isRemoved(data)) 
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
			un.notifyInfo("Existing Tags", "No tags found.");
			return;
		}
		SelectionWizard wizard = new SelectionWizard(
										reg.getTaskBar().getFrame(), r);
		IconManager icons = IconManager.getInstance();
		wizard.setTitle("Tags Selection" , "Select existing tags", 
				icons.getIcon(IconManager.TAGS_48));
		wizard.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(wizard);
	}
	
	/**
	 * Returns <code>true</code> if the tag has been removed,
	 * <code>false</code> otherwise.
	 * 
	 * @param tag The tag to handle.
	 * @return See above.
	 */
	private boolean isRemoved(AnnotationData tag)
	{
		Iterator i = removedTags.iterator();
		TagAnnotationData data;
		long id = tag.getId();
		while (i.hasNext()) {
			data = (TagAnnotationData) i.next();
			if (data.getId() == id) return true;
		}
		return false;
	}
	
	/** Clears the selected tags. */
	private void clearSelectedTags()
	{
		Iterator<TagComponent> i = selectedTags.iterator();
		while (i.hasNext()) 
			i.next().resetFont();
		selectedTags.clear();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	TagsUI(EditorModel model)
	{
		super(model);
		title = TITLE;
		initComponents();
		border = new TitledLineBorder(title, getBackground());
		UIUtilities.setBoldTitledBorder(title, this);
		getCollapseComponent().setBorder(border);
		add(layoutNewTags());
		add(UIUtilities.buildComponentPanel(addButton));
	}
	
	/**
	 * Sets the selected tag.
	 * 
	 * @param isShiftDown	Pass <code>true</code> if the <code>Shift</code>
	 * 						is down, to allow multi selections. 
	 * 						Pass <code>false</code> otherwise.
	 * @param data			The selected component.
	 */
	void setSelectedTag(boolean isShiftDown, TagComponent data)
	{
		//if (!isShiftDown) 
		clearSelectedTags();
		selectedTags.add(data);
	}
	
	/**
	 * Returns the number of selected tags.
	 * 
	 * @return See above.
	 */
	int getSelectedTagsCount() { return selectedTags.size(); }
	
	/**
	 * Edits the passed annotation only if the tag.
	 * 
	 * @param location		The location of the mouse click.
	 * @param tagComponent	The component hosting the tag to edit. 	
	 */
	void editAnnotation(Point location, TagComponent tagComponent)
	{
		popupPoint = location;
		if (tagComponent == null) return;
		editedTag = tagComponent;
		AnnotationData data = editedTag.getAnnotation();
		if (data == null) return;
		TagAnnotationData tag = (TagAnnotationData) data;
		List l = tag.getTagDescriptions();
		String text = "";
		if (l != null && l.size() > 0) {
			long userID = MetadataViewerAgent.getUserDetails().getId();
			Iterator i = l.iterator();
			TextualAnnotationData ann;
			while (i.hasNext()) {
				ann = (TextualAnnotationData) i.next();
				if (ann.getOwner().getId() == userID) {
					text = ann.getText();
				
					break;
				}
			}
		}
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		AnnotationEditor editor = new AnnotationEditor(f, tag.getTagValue(),
														text);
		editor.addPropertyChangeListener(AnnotationEditor.EDIT_PROPERTY, this);
		SwingUtilities.convertPointToScreen(popupPoint, this);
		editor.setLocation(popupPoint);
		editor.setVisible(true);
	}
	
	/**
	 * Shows the tag menu.
	 * 
	 * @param location	The location of the mouse click.
	 * @param invoker	The last selected component.
	 */
	void showMenu(Point location, Component invoker)
	{
		popupPoint = location;
		menu = new TagPopupMenu(this);
		menu.show(invoker, location.x, location.y);
	}
	
	/** Removes the selected tags. */
	void removeSelectedTags()
	{
		Iterator<TagComponent> i = selectedTags.iterator();
		TagComponent comp;
		AnnotationData tag;
		while (i.hasNext()) {
			comp = i.next();
			tag = comp.getAnnotation();
			if (addedTags.contains(tag))addedTags.remove(tag);
			else removedTags.add(tag);
		}
		clearSelectedTags();
		createExistingTagsPane();
	}
	
	/** Edits the selected tags. */
	void editSelectedTags()
	{
		//should only have one tag.
		int n = selectedTags.size();
		if (n != 1) return;
		editAnnotation(popupPoint, selectedTags.get(0));
	}
	
	/** Browses the selected tags. */
	void browseSelectedTags()
	{
		clearSelectedTags();
	}
	
	/** 
	 * Displays the existing in the <code>SelectionWizard</code>
	 * or in the UI component used for code completion.
	 */
	void setExistingTags()
	{
		if (wizard) showSelectionWizard();
		else {
			codeCompletion();
			String name = nameArea.getText();
	        setSelectedTextValue(name.split(SearchUtil.COMMA_SEPARATOR));
		}
		wizard = false;
	}
	
	/**
	 * Overridden to lay out the tags.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		int n = model.getTagsCount();
		title = TITLE+LEFT+n+RIGHT;
		border.setTitle(title);
		
		//setLayout(new BorderLayout());
		add(createExistingTagsPane());
		add(Box.createVerticalStrut(5));
		add(new JSeparator());
		add(Box.createVerticalStrut(5));
		add(layoutNewTags());
		add(UIUtilities.buildComponentPanel(addButton));
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return title; }
	
	/**
	 * Returns the collection of tags to remove.
	 * @see AnnotationUI#getAnnotationToRemove()
	 */
	protected List<AnnotationData> getAnnotationToRemove()
	{ 
		return removedTags; 
	}

	/**
	 * Returns the collection of tags to add.
	 * @see AnnotationUI#getAnnotationToSave()
	 */
	protected List<AnnotationData> getAnnotationToSave()
	{ 
		//First the tags to create.
		String name = nameArea.getText();
		String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
		String value;
		TagAnnotationData data;
		String description = descriptionArea.getText();
		if (description != null)
			description = description.trim();
		List<AnnotationData> newTags = new ArrayList<AnnotationData>();
		for (int i = 0; i < names.length; i++) {
			value = names[i];
			if (value != null) {
				value = value.trim();
				if (value != null && value.length() > 0) {
					data = new TagAnnotationData(value);
					if (description.length() > 0)
						data.setTagDescription(description);
					newTags.add(data);
				}
			}
		}
		if (newTags.size() > 0)
			addedTags.addAll(newTags);
		return addedTags;
	}
	
	/**
	 * Returns <code>true</code> if annotation to save.
	 * @see AnnotationUI#hasDataToSave()
	 */
	protected boolean hasDataToSave()
	{
		if (removedTags.size() > 0) return true;
		if (addedTags.size() > 0) return true;
		String name = nameArea.getText();
		String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
		String value;
		TagAnnotationData data;
		String description = descriptionArea.getText();
		if (description != null)
			description = description.trim();
		List<AnnotationData> newTags = new ArrayList<AnnotationData>();
		for (int i = 0; i < names.length; i++) {
			value = names[i];
			if (value != null) {
				value = value.trim();
				if (value != null && value.length() > 0) {
					data = new TagAnnotationData(value);
					if (description.length() > 0)
						data.setTagDescription(description);
					newTags.add(data);
				}
			}
		}
		
		if (newTags.size() > 0) return true;
		return false;
	}
	
	/**
	 * Clears the data to save.
	 * @see AnnotationUI#clearData()
	 */
	protected void clearData()
	{
		removedTags.clear();
		nameArea.setText("");
		addedTags.clear();
	}
	
	/**
	 * Clears the UI.
	 * @see AnnotationUI#clearDisplay()
	 */
	protected void clearDisplay() 
	{ 
		removeAll();
		addedTags.clear();
		removedTags.clear();
		selectedTags.clear();
		editedTag = null;
		historyDialog = null;
		nameArea.setText("");
		descriptionArea.setText("");
		int n = model.getTagsCount();
		title = TITLE+LEFT+n+RIGHT;
		border.setTitle(title);
	}
	
	/**
	 * Brings up the selection wizard to select existing tags.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String s = e.getActionCommand();
		if (ADD_ACTION.equals(s)) {
			Collection l = model.getExistingTags();
			if (l == null) {
				wizard = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				model.loadExistingTags();
				return;
			}
			showSelectionWizard();
		} 
	}

	/**
	 * Reacts to property fired by the history dialog.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (HistoryDialog.SELECTION_PROPERTY.equals(name)) {
			Object item = evt.getNewValue();
			if (!(item instanceof TagItem)) return;
			DataObject ho = ((TagItem) item).getDataObject();
			if (ho instanceof TagAnnotationData) 
				handleTag((TagAnnotationData) ho);
		} else if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Collection l = (Collection) evt.getNewValue();
			if (l == null || l.size() == 0) return;
			Iterator i = l.iterator();
	    	while (i.hasNext()) 
	    		handleTagEnter((TagAnnotationData) i.next());
	    	createExistingTagsPane();
	    	firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
					Boolean.TRUE);
		} else if (AnnotationEditor.EDIT_PROPERTY.equals(name)) {
			int n = selectedTags.size();
			if (n != 1) return;
			TagComponent comp = selectedTags.get(0);
			TagAnnotationData tag = (TagAnnotationData) comp.getAnnotation();
			tag.setTagDescription((String) evt.getNewValue());
			addedTags.add(tag);
			firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
					Boolean.TRUE);
		} else if (AnnotationEditor.CLOSE_PROPERTY.equals(name)) {
			if (editedTag != null) {
				editedTag.resetFont();
				existingTags.repaint();
			}
		}
	}
	
	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e)
	{
		handleTagInsert();
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
	}

	/**
	 * Fires property indicating that some text has been entered.
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
	public void removeUpdate(DocumentEvent e)
	{
		firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
							Boolean.TRUE);
	}
	
	/**
	 * Required by the {@link DocumentListener} I/F but no-op implementation
	 * in our case.
	 * @see DocumentListener#changedUpdate(DocumentEvent)
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}

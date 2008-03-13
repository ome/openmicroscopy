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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
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
import java.util.Iterator;
import java.util.List;
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
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries
import layout.TableLayout;

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
	implements ActionListener, PropertyChangeListener
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
	private static final Color	EXISTING_TAGS = Color.RED;
	
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

    /** Helper reference to the font metrics. */
    private FontMetrics				metrics;
    
    /** The UI component hosting the existing tags. */
    private JPanel					existingTags;
    
    /** The location of the mouse click. */
    private Point					popupPoint;
    
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
		if (ho instanceof TagAnnotationData) {
			handleTagEnter((TagAnnotationData) ho);
			layoutExistingTags();
		}
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
		Rectangle2D r;
		Graphics context = nameArea.getGraphics();
		int l = 0;
		String v;
		int vl;
		Iterator j;
		TagAnnotationData item;
		for (int i = 0; i < names.length; i++) {
			v = names[i];
			r = metrics.getStringBounds(v, context);
			vl = (int) r.getWidth();
			if (p.x >= l && p.x <= (l+vl)) {
				v = v.trim();
				j = model.getTags().iterator();
				while (j.hasNext()) {
					item = (TagAnnotationData) j.next();
					if (item.getTagValue().equals(v)) {
						//descriptionArea.setText(item.getDescription());
						return;
					}
				}
				descriptionArea.setText("");
				return;
			}
			l += vl;
		}
	}
	
	/** Initializes the {@link HistoryDialog} used for code completion. */
    private void codeCompletion()
    {
    	Rectangle r = nameArea.getBounds();
    	if (historyDialog == null) {
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
					if (usedTags.contains(object)) 
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
    }
    
    
	/** Initializes the UI components. */
	private void initComponents()
	{
		addButton = new JButton("Available...");
		addButton.setActionCommand(ADD_ACTION);
		addButton.addActionListener(this);
		newButton = new JButton("New...");
		newButton.setActionCommand(NEW_ACTION);
		newButton.addActionListener(this);
		nameArea = new JTextField();
		metrics = nameArea.getFontMetrics(nameArea.getFont());
		nameArea.getDocument().addDocumentListener(new DocumentListener() {
		
			/** 
             * Loads existing tags.
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
			public void insertUpdate(DocumentEvent de)
			{
                handleTagInsert();
            }
			
			/** 
			 * Required by the {@link DocumentListener} I/F but no-op
			 * implementation in our case.
			 * @see DocumentListener#removeUpdate(DocumentEvent)
			 */
			public void removeUpdate(DocumentEvent e) {}
		
			/** 
			 * Required by the {@link DocumentListener} I/F but no-op
			 * implementation in our case.
			 * @see DocumentListener#removeUpdate(DocumentEvent)
			 */
			public void changedUpdate(DocumentEvent e) {}
		
		});
		
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
			 * Displays the description of the category 
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
		double[][] tl = {{TableLayout.PREFERRED, 5, TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5,
				TableLayout.PREFERRED, 60} }; //rows
		p.setLayout(new TableLayout(tl));
		p.add (new JLabel("New Tags"), "0, 0");
		p.add(nameArea, "2, 0");
		JLabel l = UIUtilities.setTextFont(DESCRIPTION, Font.ITALIC, 10);
		p.add(l, "2, 2");
		p.add (new JLabel("Description"), "0, 4");
		p.add(descriptionArea, "2, 4, 2, 5");
		return p;
	}
	
	/**
	 * Builds and lays out the UI component hosting the {@link #existingTags}.
	 * 
	 * @return See above.
	 */
	private JPanel createExistingTagsPane()
	{
		JPanel p = new JPanel();
		double[][] size = {{TableLayout.PREFERRED, 5, TableLayout.FILL},
							{TableLayout.PREFERRED, TableLayout.FILL}};
		p.setLayout(new TableLayout(size));
		JLabel label = UIUtilities.setTextFont("Tags: ");
		p.add(label, "0, 0, f, c");
		layoutExistingTags();
		p.add(existingTags, "2, 0, 2, 1");
		return p;
	}
	
	/** Lays out the tags associated to the object. */
	private void layoutExistingTags()
	{
		Collection l = model.getTags();
		Iterator i;
		TagComponent comp;
		AnnotationData data;
		List<TagComponent> tags = new ArrayList<TagComponent>();
		if (existingTags != null) existingTags.removeAll();
		else existingTags = new JPanel();
		int col = 3;
		double[] columns = {TableLayout.PREFERRED, 5, TableLayout.PREFERRED,
							5, TableLayout.PREFERRED};
		TableLayout layout = new TableLayout();
		layout.setColumn(columns);
		existingTags.setLayout(layout);
		if (l != null) {
			i = l.iterator();
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (!isRemoved(data)) 
					tags.add(new TagComponent(this, data));
			}
		}
		i = addedTags.iterator();
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			//if (!removedTags.contains(data)) {
				comp = new TagComponent(this, data);
				comp.setForeground(EXISTING_TAGS);
				tags.add(comp);
			//}
		}
		int index = 0;
		int row = 0;
		int n = tags.size();
		for (int j = 0; j < n; j++) {
			if (j%col == 0) {
				layout.insertRow(index, TableLayout.PREFERRED);
				index++;
			}
		}
		i = tags.iterator();
		index = 0;
		while (i.hasNext()) {
			comp = (TagComponent) i.next();
			existingTags.add(comp, index+", "+row+", f, c");
			index = index+2;
			if (index%(2*col) == 0) {
				index = 0;
				row++;
			}
		}
		
		existingTags.revalidate();
		existingTags.repaint();
	}
	
	/** Shows the collection of existing tags. */
	private void showSelectionWizard()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		Collection l = model.getExistingTags();
		Registry reg = MetadataViewerAgent.getRegistry();
		if (l.size() == 0) {
			UserNotifier un = reg.getUserNotifier();
			un.notifyInfo("Existing Tags", "No tags found.");
			return;
		}
		SelectionWizard wizard = new SelectionWizard(
										reg.getTaskBar().getFrame(), l);
		IconManager icons = IconManager.getInstance();
		wizard.setTitle("Tags Selection" , "Select existing tags", 
				icons.getIcon(IconManager.TAGS_48));
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
		if (!isShiftDown) clearSelectedTags();
		data.setComponentFont(Font.BOLD);
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
	 * @param location	The location of the mouse click.
	 * @param data 		The tag to edit.
	 */
	void editAnnotation(Point location, AnnotationData data)
	{
		popupPoint = location;
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
		AnnotationEditor editor = new AnnotationEditor(f, text);
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
		//if (menu == null)
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
			if (addedTags.contains(tag))
				addedTags.remove(tag);
			else 
				removedTags.add(tag);
		}
		clearSelectedTags();
		layoutExistingTags();
	}
	
	/** Edits the selected tags. */
	void editSelectedTags()
	{
		//should only have one tag.
		int n = selectedTags.size();
		if (n != 1) return;
		TagComponent comp = selectedTags.get(0);
		editAnnotation(popupPoint, comp.getAnnotation());
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
		Border border = new TitledLineBorder(title, getBackground());
		setBorder(border);
		getCollapseComponent().setBorder(border);
		/*
		double[][] size = {{TableLayout.FILL}, {TableLayout.PREFERRED, 
							5, TableLayout.PREFERRED, TableLayout.PREFERRED}};
		setLayout(new TableLayout(size));
		add(createExistingTagsPane(), "0, 0, f, c");
		add(new JSeparator(), "0, 1, f, c");
		add(layoutNewTags(), "0, 2, f, c");
		add(UIUtilities.buildComponentPanel(addButton), "0, 3, f, c");
		*/
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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
		if (getAnnotationToSave().size() > 0) return true;
		return false;
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
		existingTags = null;
		historyDialog = null;
		nameArea.setText("");
		descriptionArea.setText("");
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
			if (ho instanceof TagAnnotationData) {
				handleTagEnter((TagAnnotationData) ho);
				layoutExistingTags();
				firePropertyChange(EditorControl.SAVE_PROPERTY, Boolean.FALSE, 
						Boolean.TRUE);
			}
		} else if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Collection l = (Collection) evt.getNewValue();
			if (l == null || l.size() == 0) return;
			Iterator i = l.iterator();
	    	while (i.hasNext()) 
	    		handleTagEnter((TagAnnotationData) i.next());
	    	layoutExistingTags();
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
		}
	}
	
}

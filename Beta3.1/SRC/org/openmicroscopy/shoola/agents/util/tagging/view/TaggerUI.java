/*
 * org.openmicroscopy.shoola.agents.util.tagging.view.TaggerUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.tagging.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.tagging.util.TagCellRenderer;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagItem;
import org.openmicroscopy.shoola.agents.util.tagging.util.TagSaverDef;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;

/** 
 * The UI delegate. Presents the components composing the data.
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
class TaggerUI
	extends JPanel
{

	/** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
	/** The maximum length of the name. */
    private static final int		MAX_NAME = 255;
    
    /** Area where to enter the name of the <code>DataObject</code>. */
    private JTextField			nameArea;
     
    /** Area where to enter the description of the <code>DataObject</code>. */
    private JTextArea			descriptionArea;
    
    /** Button to finish the operation. */
    private JButton				finishButton;
    
    /** Button to cancel the object creation. */
    private JButton				cancelButton;
    
    /** Button to display the history dialog. */
    private JButton				popupButton;
    
    /** Helper reference to the font metrics. */
    private FontMetrics			metrics;
    
    /** A {@link DocumentListener} for the {@link #nameArea}. */
    private DocumentListener	nameAreaListener;
    
    /** The dialog displaying the available categories. */
    private HistoryDialog		historyDialog;
    
    /** 
     * Flag indicating that the user is currently entering text in the
     * {@link #nameArea}.
     */
    private boolean				typing;
    
    /** 
     * The panel hosting the {@link #nameArea} and the {@link #popupButton}.
     */
    private JPanel				namePanel;
    
    /** Reference to the model. */
    private TaggerModel			model;
    
    /** Reference to the control. */
    private TaggerControl		controller;
    
    /** Reference to the view. */
    private TaggerView			view;
    
    /**  Initializes the component composing the display. */
    private void initComponents()
    {
    	nameArea = new JTextField();
    	nameArea.setBorder(null);
        UIUtilities.setTextAreaDefault(nameArea);
        metrics = nameArea.getFontMetrics(nameArea.getFont());
        descriptionArea = new MultilineLabel();
        UIUtilities.setTextAreaDefault(descriptionArea);
        cancelButton = new JButton(controller.getAction(TaggerControl.CLOSE));
        finishButton = new JButton(controller.getAction(TaggerControl.FINISH));
        //view.getRootPane().setDefaultButton(finishButton);
        popupButton = new JButton(controller.getAction(TaggerControl.POP_UP));
        UIUtilities.setTextAreaDefault(popupButton);
        popupButton.setBorder(null);
        //popupButton.setEnabled(false);
        
        nameAreaListener = new DocumentListener() {
            
            /** 
             * Updates the editor's controls when some text is inserted. 
             * @see DocumentListener#insertUpdate(DocumentEvent)
             */
            public void insertUpdate(DocumentEvent de)
            {
            	String textInsert = "";
            	try {
            		textInsert = de.getDocument().getText(de.getOffset(), 
            												de.getLength());
				} catch (Exception e) {}
            	
                handleNameAreaInsert(textInsert);
            }
            
            /** 
             * Displays an error message when the data object has no name. 
             * @see DocumentListener#removeUpdate(DocumentEvent)
             */
            public void removeUpdate(DocumentEvent de)
            {
                handleNameAreaRemove(de.getDocument().getLength());
            }

            /** 
             * Required by I/F but no-op implementation in our case. 
             * @see DocumentListener#changedUpdate(DocumentEvent)
             */
            public void changedUpdate(DocumentEvent de) {}
            
        };
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
        nameArea.getDocument().addDocumentListener(nameAreaListener);
    }
    
    /**
	 * 
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
		Iterator j, k;
		CategoryData item;
		Set set;
		for (int i = 0; i < names.length; i++) {
			v = names[i];
			r = metrics.getStringBounds(v, context);
			vl = (int) r.getWidth();
			if (p.x >= l && p.x <= (l+vl)) {
				v = v.trim();
				j = model.getTags().iterator();
				while (j.hasNext()) {
					item = (CategoryData) j.next();
					if (item.getName().equals(v)) {
						descriptionArea.setText(item.getDescription());
						return;
					}
				}
				j = model.getTagSets().iterator();
				while (j.hasNext()) {
					set = ((CategoryGroupData) j.next()).getCategories();
					if (set != null) {
						k = set.iterator();
						while (k.hasNext()) {
							item = (CategoryData) k.next();
							if (item.getName().equals(v)) {
								descriptionArea.setText(item.getDescription());
								return;
							}
						}
					}
					
				}
			
				descriptionArea.setText("");
				return;
			}
			l += vl;
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
		DataObject ho = o.getDataObject();
		if (ho instanceof CategoryData) {
			handleTagEnter((CategoryData) ho);
		} else if (ho instanceof CategoryGroupData) {
			handleTagSetEnter((CategoryGroupData) ho);
		}
	}
	
	/**
	 * Invokes when a tag set is selected by pressing the 
	 * <code>enter</code> key.
	 * 
	 * @param data The selected value.
	 */
	private void handleTagSetEnter(CategoryGroupData data)
	{
		String name = nameArea.getText();
		String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
		Set categories = data.getCategories();
		Iterator j = categories.iterator();
		CategoryData category;
		String s;
		String n;
		String text = "";
		boolean exist = false;
		int l = names.length-1;
		for (int i = 0; i < l; i++) {
			n = names[i].trim();
			text += n;
			text += SearchUtil.COMMA_SEPARATOR+SearchUtil.SPACE_SEPARATOR;
		}
		while (j.hasNext()) {
			category = (CategoryData) j.next();
			s = category.getName();
			for (int i = 0; i < l; i++) {
				n = names[i].trim();
				if (s.equals(n)) {
					exist = true;
				}
			}
			if (!exist) {
				text += s+SearchUtil.COMMA_SEPARATOR+SearchUtil.SPACE_SEPARATOR;
				descriptionArea.setText(category.getDescription());
			}
		}
		if (text.length() > 2)
			text = text.substring(0, text.length()-2);
		setNameAreaValue(text);
	}
	
	/**
	 * Invokes when a tag is selected by pressing the <code>enter</code> key.
	 * 
	 * @param data The selected value.
	 */
	private void handleTagEnter(CategoryData data)
	{
		String name = nameArea.getText();
		String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
		String s = data.getName();
		String n;
		String text = "";
		boolean exist = false;
		
		int l = names.length-1;
		for (int i = 0; i < l; i++) {
			n = names[i].trim();
			text += n;
			if (s.equals(n)) {
				if (i != (l-1))
					text += SearchUtil.COMMA_SEPARATOR
							+SearchUtil.SPACE_SEPARATOR;
				exist = true;
			} else text += SearchUtil.COMMA_SEPARATOR
							+SearchUtil.SPACE_SEPARATOR;
		}
		if (!exist) {
			descriptionArea.setText(data.getDescription());
			descriptionArea.setEnabled(false);
			text += s;
		} else text = text.substring(0, text.length()-2);
		setNameAreaValue(text);
	}

	/** Initializes the {@link historyDialog}. */
	private void initDialog()
	{
		Rectangle r = namePanel.getBounds();
		if (historyDialog == null) {
			Object[] data, data2 = null;
			int i = 0;
			Iterator j;
			List tags = model.getAllTags();//model.getAvailableTags();
			if (tags == null || tags.size() == 0) {
				data = new Object[0];
			} else {
				data = new Object[tags.size()]; 
				j = tags.iterator();
				DataObject object;
				List usedTags = model.getTags();
				TagItem item;
				while (j.hasNext()) {
					object = (DataObject) j.next();
					item = new TagItem(object);
					if (usedTags.contains(object)) item.setAvailable(false);
					data[i] = item;
					i++;
				}
			}
			List tagSets = model.getTagSets();
			if (tagSets != null && tagSets.size() > 0) {
				i = 0;
				j = tagSets.iterator();
				CategoryGroupData group;
				List<DataObject> toKeep = new ArrayList<DataObject>();
				Set categories;
				while (j.hasNext()) {
					group = (CategoryGroupData) j.next();
					categories = group.getCategories();
					if (categories != null && categories.size() > 0)
						toKeep.add(group);
				}
				if (toKeep.size() > 0) {
					data2 = new Object[toKeep.size()]; 
					j = toKeep.iterator();
					while (j.hasNext()) {
						data2[i] = new TagItem((DataObject) j.next());
						i++;
					}
				}
				
			}
			if (data2 != null) {
				historyDialog = new HistoryDialog(data, data2, r.width);
				historyDialog.setListCellRenderer(new TagCellRenderer(), 
												new TagCellRenderer());
			} else {
				historyDialog = new HistoryDialog(data, r.width);
				historyDialog.setListCellRenderer(new TagCellRenderer());
			}
			historyDialog.addPropertyChangeListener(
					HistoryDialog.SELECTION_PROPERTY, controller);
		}   
	}
	
	/** Displays the dialog. */
	private void showDialog()
	{
		if (historyDialog == null) return;
		Rectangle r = namePanel.getBounds();
		historyDialog.show(namePanel, 0, r.height);
	}
	
	/**
     * Displays an error message when the length of the inserted name is
     * <code>0</code>.
     * 
     * @param length The length of the inserted text.
     */
    private void handleNameAreaRemove(int length)
    { 
    	descriptionArea.setText("");
		descriptionArea.setEnabled(true);
    	typing = true;
    	finishButton.setEnabled(length != 0);
    	String name = nameArea.getText();
    	String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
    	if (length == 0 && historyDialog != null)
    		historyDialog.setVisible(false);
    	else 
    		setSelectedTextValue(names);
    }
    
    /**
     * Sets the selected value in the history list.
     * 
     * @param names The names already set.
     */
    private void setSelectedTextValue(String[] names)
    {
    	initDialog();
        if (historyDialog == null) return;
        int l = names.length;
        if (l > 0) {
        	if (historyDialog.setSelectedTextValue(names[l-1].trim())) {
        		showDialog();
        		nameArea.requestFocus();
        	} else historyDialog.setVisible(false);
        }	
    }
    
    /**
	 * Controls if the passed name is not too long.
	 * 
	 * @param name The value to check.
	 */
	private void checkName(String name)
	{
		if (name.length() > MAX_NAME) {
			/*
        	UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
        	un.notifyInfo("Editor", "The name is too long. Cannot be more " +
        			"than "+MAX_NAME+" characters long.");
        			*/
        	setNameAreaValue("");
        	finishButton.setEnabled(false);
        	return;
        }
	}
	
	/**
	 * Sets the text of the {@link #nameArea}. First removes the
	 * documnet listener otherwise an event is fired.
	 * 
	 * @param v The value to set.
	 */
	private void setNameAreaValue(String v)
	{
		nameArea.getDocument().removeDocumentListener(nameAreaListener);
    	nameArea.setText(v);
    	nameArea.getDocument().addDocumentListener(nameAreaListener);
	}
    /**
     * Enables the {@link #finishButton} and removes the warning message
     * when the name of the <code>DataObject</code> is valid.
     * Sets the {@link #typing} flag to <code>true</code>.
     * 
     * @param insert	The text inserted.
     */
    private void handleNameAreaInsert(String insert)
    {
    	typing = true;
        finishButton.setEnabled(true);
        String name = nameArea.getText();
        String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
        setSelectedTextValue(names);
    }
    
    /**
     * Builds the tool bar hosting the {@link #cancelButton} and
     * {@link #finishButton}.
     * 
     * @return See above.
     */
    private JPanel buildToolBar()
    {
        JPanel bar = new JPanel();
        bar.setBorder(null);
        bar.add(finishButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
        JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        return p;
    }
    
    /**
     * Builds the panel hosting the {@link #nameArea} and the
     * {@link #descriptionArea}. 
     * 
     * @return See above.
     */
    private JPanel buildContentPanel()
    {
        JPanel content = new JPanel();
        content.setOpaque(true);
        content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        int height = 100;
        double[][] tl = {{TableLayout.PREFERRED, TableLayout.FILL}, //columns
        				{0, TableLayout.PREFERRED, 5, 0, height} }; //rows
        namePanel = new JPanel();
        double[][] pl = {{TableLayout.FILL, TableLayout.PREFERRED}, //columns
			{TableLayout.PREFERRED} }; //rows
        
        namePanel.setLayout(new TableLayout(pl));
        namePanel.setBorder(
        		BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        namePanel.add(nameArea, "0, 0, f, c");
        namePanel.add(popupButton, "1, 0, f, c");
        TableLayout layout = new TableLayout(tl);
        content.setLayout(layout);
        content.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel l;
        content.add(UIUtilities.setTextFont("Name"), "0, 1, l, c");
        content.add(namePanel, "1, 1, f, c");
        //content.add(popupButton, "2, 1, l, c");
        content.add(new JLabel(), "0, 2, 1, 2");
        
        l = UIUtilities.setTextFont("Description");
        int h = l.getFontMetrics(l.getFont()).getHeight()+5;
        layout.setRow(3, h);
        content.add(l, "0, 3, l, c");
        JScrollPane pane = new JScrollPane(descriptionArea);
        content.add(pane, "1, 3, 1, 4");
        
        return content;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setLayout(new BorderLayout(0, 0));
    	add(buildContentPanel(), BorderLayout.CENTER);
    	add(buildToolBar(), BorderLayout.SOUTH);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model 	 Reference to the model. Mustn't be <code>null</code>.
     * @param controller Reference to the control. Mustn't be <code>null</code>.
     * @param view 		 Reference to the view. Mustn't be <code>null</code>.
     */
    TaggerUI(TaggerModel model, TaggerControl controller, TaggerView view)
    {
    	if (model == null)
    		throw new IllegalArgumentException("No model.");
    	if (controller == null)
    		throw new IllegalArgumentException("No controller.");
    	if (view == null)
    		throw new IllegalArgumentException("No view.");
    	this.controller = controller;
    	this.model = model;
    	this.view = view;
    	initComponents();
    	buildGUI();
    }
    
    /** Sets the focus on the {@link #nameArea} field. */
    void requestFocusOnField()
    { 
    	if (nameArea != null) nameArea.requestFocus();
    }
    
    /** Displays the available tags if any. */
    void showTags()
    {
    	initDialog();
		historyDialog.resetListData();
		showDialog();
		requestFocusOnField();
    }
    
	/**
	 * Handles the selection of a tag set via the {@link HistoryDialog}.
	 * 
	 * @param item The item to handle.
	 */
	void handleTagSetSelection(CategoryGroupData item)
	{
		if (item == null) return;
		Set categories = item.getCategories();
		if (categories == null) return;
		Iterator i = categories.iterator();
		while (i.hasNext())
			handleTagSelection((CategoryData) i.next());
	}
	
	/**
	 * Handles the selection of a tag via the {@link HistoryDialog}.
	 * 
	 * @param item The item to handle.
	 */
	void handleTagSelection(CategoryData item) 
	{
		if (item == null) return;
		List linked = model.getTags();
		if (linked != null && linked.contains(item)) return;
		String name = nameArea.getText();
		int length = name.length();
		String itemName = item.getName();
		String text = "";
		if (length >= 1) {
			boolean exist = false;
			String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
			String n;
			int l = names.length;
			if (typing) l = l-1;
			if (names != null && l > 0) {
				for (int i = 0; i < l; i++) {
					n = names[i].trim();
					text += n;
					if (itemName.equals(n)) {
						if (i != (l-1))
							text += SearchUtil.COMMA_SEPARATOR
									+SearchUtil.SPACE_SEPARATOR;
						exist = true;
					} else text += SearchUtil.COMMA_SEPARATOR
									+SearchUtil.SPACE_SEPARATOR;
				}
			}
			if (!exist) {
				text += itemName;
			} else text = text.substring(0, text.length()-2);
		} else text += itemName;
		
		
		descriptionArea.setText(item.getDescription());
		setNameAreaValue(text);
		finishButton.setEnabled(true);
		typing = false;
	}

	/** 
	 * Creates the object hosting the tags to save or updates.
	 * 
	 * @return See above.
	 */
	TagSaverDef saveTags()
	{
		Set<CategoryData> tagsToUpdate = new HashSet<CategoryData>();
		Set<CategoryData> tagsToCreate = new HashSet<CategoryData>();
		TagSaverDef def = new TagSaverDef(tagsToCreate, tagsToUpdate);
		CategoryData data = new CategoryData();
		String name = nameArea.getText();
		CategoryData item;
		if (name == null) return null;
		String[] names = name.split(SearchUtil.COMMA_SEPARATOR);
		int l = names.length;
		if (names != null && l > 0) {
			String v; 
			String d;
			for (int i = 0; i < l; i++) {
				v = names[i];
				if (v != null) {
					v = v.trim();
					if (v.length() > 0) {
						checkName(v);
						item = model.checkAvailableTag(v);
						data = new CategoryData();
						if (item == null) {
							if (model.checkUsedTag(v) == null) {
								data.setName(v);
								tagsToCreate.add(data);
							}
						} else {
							if (model.checkUsedTag(v) == null) {
								data.setId(item.getId());
								data.setName(v);
								//data.setDescription(item.getDescription())
								if (l == 1) {
									d = descriptionArea.getText();
									if (d != null) 
										data.setDescription(d.trim());
								}
								tagsToUpdate.add(data);
							}
						}
					}
					
				}
			}
		} 
		return def;
	}
    
}

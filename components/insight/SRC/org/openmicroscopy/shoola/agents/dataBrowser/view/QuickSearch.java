/*
 * org.openmicroscopy.shoola.util.ui.QuickSearch 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;




//Third-party libraries
import info.clearthought.layout.TableLayout;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang.StringUtils;
//Application-internal dependencies
import org.jdesktop.swingx.JXBusyLabel;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.SearchContextMenu;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;

/** 
 * Basic panel with text field for searching.
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
public class QuickSearch
	extends JPanel
	implements ActionListener, DocumentListener, PropertyChangeListener
{

	/** Indicates to search for tags. */
	public static final int 	TAGS = 0;
	
	/** Indicates to search for textual annotations. */
	public static final int 	COMMENTS = 1;
	
	/** Indicates to search for name. */
	public static final int 	FULL_TEXT = 2;
	
	/** Indicates to search for objects rated one or higher. */
	public static final int 	RATED_ONE_OR_BETTER = 3;
	
	/** Indicates to search for objects rated two or higher. */
	public static final int 	RATED_TWO_OR_BETTER = 4;
	
	/** Indicates to search for objects rated three or higher. */
	public static final int 	RATED_THREE_OR_BETTER = 5;
	
	/** Indicates to search for objects rated four or higher. */
	public static final int 	RATED_FOUR_OR_BETTER = 6;
	
	/** Indicates to search for objects rated five. */
	public static final int 	RATED_FIVE = 7;
	
	/** Indicates to search for objects unrated. */
	public static final int 	UNRATED = 8;
	
	/** Indicates to reset all nodes. */
	public static final int 	SHOW_ALL = 9;
	
	/** Indicates to search for the untagged nodes. */
	public static final int 	UNTAGGED = 10;
	
	/** Indicates to search for the uncommented nodes.  */
	public static final int 	UNCOMMENTED = 11;
	
	/** Indicates to search for the tagged nodes.  */
	public static final int 	TAGGED = 12;
	
	/** Indicates to search for the commented nodes.  */
	public static final int 	COMMENTED = 13;
	
	/** Indicates to search for nodes with ROIs.  */
        public static final int         HAS_ROIS = 14;
        
        /** Indicates to search for nodes without ROIs.  */
        public static final int         NO_ROIS = 15;
        
        /** Indicates a customized search where there is no matching quick search option. */
        public static final int         NONE = 16;
	
	/** Bound property indicating to search for given terms. */
	public static final String	QUICK_SEARCH_PROPERTY = "quickSearch";
	
	/** Bound property indicating to search for given terms. */
	public static final String	VK_UP_SEARCH_PROPERTY = "vkUpSearch";
	
	/** Bound property indicating to search for given terms. */
	public static final String	VK_DOWN_SEARCH_PROPERTY = "vkDownSearch";
	
	/** Text displayed when the show all buttons is selected. */
	private static final String	SHOW_ALL_TEXT = "Show All ";
	
	/** Text displayed when the show all buttons is selected. */
	private static final String	SHOW_ALL_DESCRIPTION = "filter "; 
	
	/** Removes the text from the text field. */
	private static final int 	CLEAR = 0;
	
	/** The selected node. */
	private SearchObject		selectedNode;
	
	/** Area where to enter the tags to search. */
	private JTextField			searchArea;
	
	/** Button to look nice. Does nothing for now. */
	private JButton				searchButton;
	
	/** Button to clear the text. */
	private JButton				clearButton;
	
	/** Button to clear the text. */
	private JButton				menuButton;
	
	/** Status label. */
	private JXBusyLabel			status;
	
	/** The Layout manager used to lay out the search area. */
	private TableLayout			layoutManager;
	
	/** UI Component hosting the various items for the search. */
	private JPanel				searchPanel;
	
	/** The popup menu. */
	private SearchContextMenu	menu;
	
	/** The possible options. */
	private List<SearchObject> 	nodes;

	/** The possible options. */
	private List<SearchObject> 	ratedNodes;
	
	/** Label if any. */
	private String				label;

	/** The node to show all the objects. */
	private SearchObject		showAll;
	
	/** Tool bar hosting the clear functionalities. */
	private JToolBar 			cleanBar;
	
	/** 
	 * Flag indicating that only one context can be selected at a time
	 * if set to <code>true</code>, more than one context if set 
	 * to <code>false</code>.
	 */
	private boolean				singleSelection;
	
	/** The default text displayed when all elements are shown. */
	private String				defaultText;
	
	/** Shows the menu. */
	private void initMenu()
	{
		Rectangle r = searchPanel.getBounds();
        if (menu == null) {
        	if (selectedNode != null)
        		menu = new SearchContextMenu(nodes, ratedNodes, r.width, 
        									selectedNode, singleSelection);
        	else 
        		menu = new SearchContextMenu(nodes, ratedNodes, r.width, 
        									singleSelection);
        	menu.addPropertyChangeListener(
        			SearchContextMenu.SEARCH_CONTEXT_PROPERTY, this);
        }
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		defaultText = " images";
		showAll = new SearchObject(SHOW_ALL, null, SHOW_ALL_TEXT+defaultText);
		IconManager icons = IconManager.getInstance();
		Icon icon = icons.getIcon(IconManager.CLEAR_DISABLED);
		Dimension d = new Dimension(icon.getIconWidth(), icon.getIconHeight());
		status = new JXBusyLabel(d);
		UIUtilities.setTextAreaDefault(status);
		status.setBorder(null);
		clearButton = new JButton(icon);
		//clearButton.setEnabled(false);
		clearButton.setToolTipText("Clear Filtering and Show All");
		UIUtilities.setTextAreaDefault(clearButton);
		clearButton.setBorder(null);
		clearButton.addActionListener(this);
		clearButton.setActionCommand(""+CLEAR);
		
		searchArea = new JTextField(15);
		if (selectedNode != null && selectedNode.getIndex() != SHOW_ALL) {
		    searchArea.setText("");
		    setSearchEnabled(true);
		} else {
		    searchArea.setText(SHOW_ALL_DESCRIPTION+defaultText);
		}
        UIUtilities.setTextAreaDefault(searchArea);
        searchArea.setBorder(null);
        searchArea.getDocument().addDocumentListener(this);
        searchArea.addKeyListener(new KeyAdapter() {

            /** Finds the phrase. */
            public void keyPressed(KeyEvent e)
            {
            	Object source = e.getSource();
            	if (source != searchArea) return;
            	switch (e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						handleKeyEnter();
						break;
					case KeyEvent.VK_UP:
						firePropertyChange(VK_UP_SEARCH_PROPERTY, 
								Boolean.valueOf(false), Boolean.valueOf(true));
						break;
					case KeyEvent.VK_DOWN:
						firePropertyChange(VK_DOWN_SEARCH_PROPERTY, 
								Boolean.valueOf(false), Boolean.valueOf(true));
				}
            }
        });
	}
	
	/** 
	 * Initializes the components composing the display.
	 *  
	 * @param icon The search icon.
	 */
	private void initComponents(Icon icon)
	{
		IconManager icons = IconManager.getInstance();
		if (icon == null) icon = icons.getIcon(IconManager.SEARCH);
		searchButton = new JButton(icon);
		UIUtilities.setTextAreaDefault(searchButton);
		searchButton.setBorder(null);
		initComponents();
	}
	
	/**
	 * Initializes the menu and related components. 
	 * 
	 * @param nodes 		The nodes to display.
	 * @param ratedNodes 	The rated nodes if any.
	 */
	private void initSearchComponents(List<SearchObject> nodes, 
									List<SearchObject> ratedNodes)
	{
		if (ratedNodes == null) ratedNodes = new ArrayList<SearchObject>();
		this.nodes = nodes;
		this.ratedNodes = ratedNodes;
		IconManager icons = IconManager.getInstance();
		//check when to create it
		menuButton = new JButton(icons.getIcon(IconManager.FILTER_MENU));
		menuButton.setToolTipText("Display the filtering options.");
		UIUtilities.setTextAreaDefault(menuButton);
		menuButton.setBorder(null);
		menuButton.addMouseListener(new MouseAdapter() {
		
			/** 
			 * Displays a menu with the available context.
			 * @see MouseAdapter#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{ 
				initMenu(); 
				Rectangle r = searchPanel.getBounds();
		        menu.show(searchPanel, 0, r.height);
			}
		
		});
		Iterator<SearchObject> j = nodes.iterator();
		while (j.hasNext()) {
		    SearchObject o = j.next();
            if (o.getIndex() == FULL_TEXT) {
                selectedNode = o;
                break;
            }
        }
		if (selectedNode == null) selectedNode = nodes.get(0);
		searchButton = new JButton(selectedNode.getIcon());
		UIUtilities.setTextAreaDefault(searchButton);
		searchButton.setBorder(null);
		initComponents();
		searchArea.setToolTipText(selectedNode.getDescription()+".");
	}
	
	/** 
	 * Builds and lays out the UI. 
	 *
	 * @param label The text to display in front of the text field.
	 */
	private void buildGUI(String label)
	{
		double w = 0;
		if (menuButton != null) w = TableLayout.PREFERRED;
		double[][] pl = {{TableLayout.PREFERRED, w, TableLayout.FILL, 
			TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED} }; //rows
		layoutManager = new TableLayout(pl);
		
		searchPanel = new JPanel();
		UIUtilities.setTextAreaDefault(searchPanel);
		searchPanel.setLayout(layoutManager);
		searchPanel.setBorder(
					BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		//searchPanel.add(searchButton, "0, 0, f, c");
		if (menuButton != null) {
			JToolBar bar = new JToolBar();
	        bar.setFloatable(false);
	        bar.setRollover(true);
	        bar.setBorder(null);
	        bar.add(menuButton);
			searchPanel.add(bar, "1, 0, FULL, CENTER");
		}
			
		searchPanel.add(searchArea, "2, 0, FULL, CENTER");
		if (clearButton != null) {
			cleanBar = new JToolBar();
			cleanBar.setFloatable(false);
			cleanBar.setRollover(true);
			cleanBar.setBorder(null);
			cleanBar.add(clearButton);
			cleanBar.setVisible(false);
			searchPanel.add(cleanBar, "3, 0, FULL, CENTER");
		}
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setOpaque(true);
		if (label != null && label.trim().length() > 0) 
			add(UIUtilities.setTextFont(label));
		add(searchPanel);
	}
	
	/** 
	 * Displays the context of the search.
	 * 
	 * @param oldNode The node previously selected.
	 */
	private void setSearchContext(SearchObject oldNode)
	{
		String text = "";
		setSearchEnabled(false);
                cleanBar.setVisible(false);
                
		if(selectedNode!=null) {
        		switch (selectedNode.getIndex()) {
        			case RATED_ONE_OR_BETTER:
        			case RATED_TWO_OR_BETTER:
        			case RATED_THREE_OR_BETTER:
        			case RATED_FOUR_OR_BETTER:
        			case RATED_FIVE:
        			case UNRATED:
        			case UNTAGGED:
        			case UNCOMMENTED:
        			case TAGGED:
        			case COMMENTED:
        			case HAS_ROIS:
        			case NO_ROIS:
        				text = selectedNode.getDescription();
        				break;
        			case SHOW_ALL:
        				text = SHOW_ALL_DESCRIPTION+defaultText;
        				break;
        			case TAGS:
        			case COMMENTS:
        			case FULL_TEXT:
        				if (oldNode != null) {
        					int oldIndex = oldNode.getIndex();
        					if (oldIndex != TAGS && oldIndex != COMMENTS &&
        							oldIndex != FULL_TEXT) text = "";
        				}
        				setFocusOnArea();
        				setSearchEnabled(true);
        		}
		}
		
		searchArea.getDocument().removeDocumentListener(this);
		searchArea.setText(text);
		searchArea.getDocument().addDocumentListener(this);
	}
	
	/** Creates a new instance. */
	public QuickSearch()
	{
		initComponents(null);
		buildGUI(null);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param icon The search icon.
	 */
	public QuickSearch(Icon icon)
	{
		this(null, icon);
	}

	/** 
	 * Creates a new instance.
	 * 
	 * @param label The text displayed in front of the panel.
	 */
	public QuickSearch(String label)
	{
		this.label = label;
		initComponents(null);
		buildGUI(label);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param label The text displayed in front of the panel.
	 * @param icon  The search icon.
	 */
	public QuickSearch(String label, Icon icon)
	{
		this.label = label;
		initComponents(icon);
		buildGUI(label);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param nodes			The nodes describing the context of the search.
	 * @param ratedNodes 	The rated nodes if any.
	 */
	public QuickSearch(List<SearchObject> nodes, List<SearchObject> ratedNodes)
	{
		this(null, nodes, ratedNodes);
	}
	
	/** 
	 * Creates a new instance.
	 * 
	 * @param label			The text displayed in front of the panel.
	 * @param nodes			The nodes describing the context of the search.
	 * @param ratedNodes 	The rated nodes if any.
	 */
	public QuickSearch(String label, List<SearchObject> nodes, 
						List<SearchObject> ratedNodes)
	{
		this.label = label;
		if (CollectionUtils.isEmpty(nodes))
			initComponents(null);
		else initSearchComponents(nodes, ratedNodes);
		buildGUI(label);
	}
	
	
	/** Selects the node. */
	protected void onNodeSelection()
	{
		List<String> l = SearchUtil.splitTerms(searchArea.getText(), 
				SearchUtil.COMMA_SEPARATOR);
		if (selectedNode == null) return;
		selectedNode.setResult(l);
		firePropertyChange(QUICK_SEARCH_PROPERTY, null, selectedNode);
	}
	
	/** 
	 * Sets to <code>true</code> if only one context can be selected at a time
	 * to <code>false</code> otherwise.
	 * 
	 * @param singleSelection The value to set.
	 */
	public void setSingleSelection(boolean singleSelection)
	{
		this.singleSelection = singleSelection;
	}
	
	/** 
	 * Adds the default searching context. 
	 * 
	 * @param text The default text.
	 */
	public void setDefaultSearchContext(String text)
	{
		if (text != null) defaultText = text;
		List<SearchObject> nodes = new ArrayList<SearchObject>();
		SearchObject node = new SearchObject(SHOW_ALL, null, 
				SHOW_ALL_TEXT+defaultText);
    	nodes.add(node);
    	node = new SearchObject(FULL_TEXT, null, 
    								SearchComponent.NAME_TEXT);
    	nodes.add(node);
    	node = new SearchObject(TAGS, null, SearchComponent.NAME_TAGS);
    	nodes.add(node);
    	node = new SearchObject(COMMENTS, null, SearchComponent.NAME_COMMENTS);
    	nodes.add(node);
    	node = new SearchObject(TAGGED, null, SearchComponent.TAGGED_TEXT);
    	nodes.add(node);
    	node = new SearchObject(UNTAGGED, null, SearchComponent.UNTAGGED_TEXT);
    	nodes.add(node);
    	node = new SearchObject(COMMENTED, null, 
    			SearchComponent.COMMENTED_TEXT);
    	nodes.add(node);
    	node = new SearchObject(UNCOMMENTED, null, 
    			SearchComponent.UNCOMMENTED_TEXT);
    	nodes.add(node);
    	node = new SearchObject(HAS_ROIS, null, 
                SearchComponent.HAS_ROIS_TEXT);
        nodes.add(node);
        node = new SearchObject(NO_ROIS, null, 
                        SearchComponent.NO_ROIS_TEXT);
        nodes.add(node);
    	
    	List<SearchObject> ratedNodes = new ArrayList<SearchObject>();
    	node = new SearchObject(RATED_ONE_OR_BETTER, null, "* or better");
    	ratedNodes.add(node);
    	node = new SearchObject(RATED_TWO_OR_BETTER, null, "** or better");
    	ratedNodes.add(node);
    	node = new SearchObject(RATED_THREE_OR_BETTER, null, "*** or better");
    	ratedNodes.add(node);
    	node = new SearchObject(RATED_FOUR_OR_BETTER, null, "**** or better");
    	ratedNodes.add(node);
    	node = new SearchObject(RATED_FIVE, null, "*****");
    	ratedNodes.add(node);
    	
    	node = new SearchObject(UNRATED, null, "Unrated");
    	ratedNodes.add(node);
    	
    	initSearchComponents(nodes, ratedNodes);
    	removeAll();
    	buildGUI(label);
	}
	
	/** 
	 * Sets the default text.
	 * 
	 * @param defaultText The value to set.
	 */
	public void setDefaultText(String defaultText)
	{ 
		if (defaultText == null) return;
		this.defaultText = defaultText;
		searchArea.setText(SHOW_ALL_DESCRIPTION+defaultText);
		showAll.setDescription(SHOW_ALL_TEXT+defaultText);
	}
	
	/**
	 * Returns the selected node.
	 * 
	 * @return See above.
	 */
	public SearchObject getSelectedNode() { return selectedNode; }

	/**
	 * Returns the search area.
	 * 
	 * @return See above.
	 */
	public JComponent getSelectionArea() { return searchPanel; }
	
	/** Sets the focus on the {@link #searchArea}. */
	public void setFocusOnArea()
	{ 
		searchArea.requestFocusInWindow();
		String v = getSearchValue();
		int l = v.length();
		searchArea.setCaretPosition(l);
		searchArea.moveCaretPosition(l);
	}

	/**
	 * Returns the text of the {@link #searchArea}.
	 * 
	 * @return See above.
	 */
	public String getSearchValue() { return searchArea.getText(); }
	
	/**
	 * Sets the value of the {@link #searchArea}.
	 * 
	 * @param text The value to set.
	 * @param removeLast Pass <code>true</code> to remove the last item,
	 * 					 <code>false</code> otherwise.
	 */
	public void setSearchValue(String text, boolean removeLast)
	{
		if (text == null) return;
    	List<String> l = SearchUtil.splitTerms(getSearchValue(), 
				SearchUtil.COMMA_SEPARATOR);
    	if (removeLast && l.size() > 0) l.remove(l.size()-1);
    	String result = SearchUtil.formatString(text, l);
    	searchArea.getDocument().removeDocumentListener(this);
    	searchArea.setText(result);
    	searchArea.getDocument().addDocumentListener(this);
	}
	
	/**
	 * Sets the enabled flag of the {@link #searchArea}.
	 * 
	 * @param enabled The value to set.
	 */
	public void setSearchEnabled(boolean enabled)
	{
		//searchArea.setEnabled(enabled);
		searchArea.setEditable(enabled);
	}
	
	/**
	 * Sets the value of the {@link #searchArea}.
	 * 
	 * @param text The value to set.
	 * @param removeLast Pass <code>true</code> to remove the last item,
	 * 					 <code>false</code> otherwise.
	 */
	public void setSearchValue(List<String> text, boolean removeLast)
	{
		if (text == null || text.size() == 0) return;
    	List<String> l = SearchUtil.splitTerms(getSearchValue(), 
				SearchUtil.COMMA_SEPARATOR);
    	Iterator<String> i = text.iterator();
    	String value;
    	List<String> values = new ArrayList<String>();
    	StringBuffer term = new StringBuffer();
    	int index = 0;
    	int n = text.size()-1;
    	
    	while (i.hasNext()) {
    		value = i.next();
			if (value != null) {
				value = value.trim();
				if (!l.contains(value)) 
					values.add(value);
				else {
					term.append(value);
					if (index < n) {
						term.append(SearchUtil.COMMA_SEPARATOR);
						term.append(SearchUtil.SPACE_SEPARATOR);
					}	
					index++;
				}
			}
		}
    	
    	i = values.iterator();
    	index = 0;
    	while (i.hasNext()) {
    		term.append(i.next());
			if (index < n) {
				term.append(SearchUtil.COMMA_SEPARATOR);
				term.append(SearchUtil.SPACE_SEPARATOR);
			}
			index++;
		}
    	searchArea.getDocument().removeDocumentListener(this);
    	searchArea.setText(term.toString());
    	searchArea.getDocument().addDocumentListener(this);
    	//setSearchValue(term, removeLast);
	}
	
	/**
	 * Determines the context corresponding to the passed index.
	 * 
	 * @param index The value to handle.
	 */
	public void setSearchContext(int index)
	{
		Iterator<SearchObject> i = nodes.iterator();
		SearchObject node;
		SearchObject selectedNode = null;
		while (i.hasNext()) {
			node = i.next();
			if (node.getIndex() == index) {
				selectedNode = node;
				break;
			}
		}
		if (selectedNode == null) {
			i = ratedNodes.iterator();
			while (i.hasNext()) {
				node = i.next();
				if (node.getIndex() == index) {
					selectedNode = node;
					break;
				}
			}
		}
		
		setFilteringStatus(true);
		initMenu();
		menu.setSelectedNode(selectedNode);
		this.selectedNode = selectedNode;
		setSearchContext(null);
	}
	
	/**
	 * Replaces the clear button and shows the tool bar if the passed 
	 * value is <code>true</code>. Otherwise add the clear button and 
	 * shows or hides the bar depending on the selected node.
	 * 
	 * @param busy See above.
	 */
	public void setFilteringStatus(boolean busy)
	{
		status.setBusy(busy);
		cleanBar.removeAll();
		if (busy) {
			cleanBar.add(status);
			cleanBar.setVisible(true);
			setFocusOnArea();
		} else {
			cleanBar.add(clearButton);
			boolean visible = false;
			if (selectedNode != null) {
				switch (selectedNode.getIndex()) {
					case TAGS:
					case COMMENTS:
					case FULL_TEXT:
						String text = searchArea.getText();
						if (text != null && text.trim().length() == 0)
							visible = true;
				}
			}
			cleanBar.setVisible(visible);
			setFocusOnArea();
		}
	}
	
	/** Removes the text from the display. */
	public void clear()
	{
		searchArea.getDocument().removeDocumentListener(this);
		searchArea.setText("");
		searchArea.getDocument().addDocumentListener(this);
		searchPanel.validate();
		searchPanel.repaint();
		cleanBar.setVisible(false);
	}
	
	/** 
	 * Class extending this class should override that method for 
	 * code completion.
	 */
	protected void handleTextInsert() {}
	
	/** 
	 * Class extending this class should override that method for 
	 * code completion.
	 */
	protected void handleKeyEnter() {}

	/** 
     * Shows or hides the {@link #clearButton} when some text is entered.
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
	public void insertUpdate(DocumentEvent e)
	{
		try {
			String s = e.getDocument().getText(e.getOffset(), e.getLength());
			if (SearchUtil.COMMA_SEPARATOR.equals(s) ||
				SearchUtil.SPACE_SEPARATOR.equals(s))
				return;
		} catch (Exception ex) {
			//ignore
		}
		
		handleTextInsert();
		cleanBar.setVisible(true);
		searchPanel.validate();
		searchPanel.repaint();
	}

	/** 
     * Shows or hides the {@link #clearButton} when some text is entered.
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
	public void removeUpdate(DocumentEvent e)
	{
	    String text = searchArea.getText();
		if (StringUtils.isBlank(text))
			cleanBar.setVisible(false);
		else handleTextInsert();
	}
	
	/** Subclasses should override this method to handle the search. */
	public void search() {}
	
	/**
	 * Clears the text from the display.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CLEAR:
				clear();
				break;
		}
	}
	
	/**
	 * Reacts to the property fired by the <code>SearchContextMenu</code>
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (SearchContextMenu.SEARCH_CONTEXT_PROPERTY.equals(name)) {
			SearchObject node = (SearchObject) evt.getNewValue();
			SearchObject oldNode = selectedNode;
			if (node != null) selectedNode = node;
			searchButton.setIcon(selectedNode.getIcon());
			UIUtilities.setTextAreaDefault(searchButton);
			searchButton.setBorder(null);
			searchArea.setToolTipText(selectedNode.getDescription());
			setSearchContext(oldNode);
			onNodeSelection();
		} else if (QUICK_SEARCH_PROPERTY.equals(name)) search();
	}
	
	/** 
     * Required by I/F but no-operation implementation in our case. 
     * @see DocumentListener#changedUpdate(DocumentEvent)
     */
	public void changedUpdate(DocumentEvent e) {}

}

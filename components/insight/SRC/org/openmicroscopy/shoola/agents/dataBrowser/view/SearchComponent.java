/*
 * org.openmicroscopy.shoola.util.ui.search.SearchComponent 
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.SearchEvent;
import org.openmicroscopy.shoola.env.data.util.SearchParameters;
import org.openmicroscopy.shoola.util.ui.SeparatorPane;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.GroupContext;
import org.openmicroscopy.shoola.util.ui.search.SearchContext;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import pojos.GroupData;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;

/** 
 * Component with advanced search options.
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
public class SearchComponent
	extends JPanel
	implements ActionListener
{

	/** Identifies the text to filter by untagged data. */
	public static final String		UNTAGGED_TEXT = "Untagged";
	
	/** Identifies the text to filter by tagged data. */
	public static final String		TAGGED_TEXT = "Tagged";
	
	/** Identifies the text to filter by commented data. */
	public static final String		COMMENTED_TEXT = "Commented";
	
	/** Identifies the text to filter by uncommented data. */
	public static final String		UNCOMMENTED_TEXT = "Uncommented";
	
	/** Identifies the text to filter by unrated data. */
	public static final String		UNRATED = "Unrated";
	
	/** Identifies the text for searching for name. */
	public static final String		NAME_TEXT = "Name";
	
	/** Identifies the text for searching for name. */
	public static final String		NAME_DESCRIPTION = "Description";
	
	/** Identifies the text for searching for tags. */
	public static final String		NAME_TAGS = "Tags";
	
	/** Identifies the text for searching for comments. */
	public static final String		NAME_COMMENTS = "Comments";
	
	/** Identifies the text for searching for ROIs. */
	public static final String              NAME_ROIS = "ROIs";
	
	/** Identifies the text for searching for URLs. */
	public static final String		NAME_URL = "URL";
	
	/** Identifies the text for searching for attachments. */
	public static final String		NAME_ATTACHMENT = "Attachments"; 
	
	/** Identifies the text for searching for annotations. */
        public static final String              NAME_ANNOTATION = "Annotations"; 
        
	/** Identifies the text for searching for rate. */
	public static final String		NAME_RATE = "Rate"; 
	
	/** Identifies the text for searching for time. */
	public static final String		NAME_TIME = "Time"; 
	
	/** Identifies the text for searching for time. */
	public static final String		NAME_CUSTOMIZED = "Custom"; 
	
	/** Identifies the text to filter by ROIs. */
        public static final String              HAS_ROIS_TEXT = "Has ROIs";
        
        /** Identifies the text to filter by ROIs. */
        public static final String              NO_ROIS_TEXT = "No ROIs";
	
	/** Bound property indicating to search. */
	public static final String 		SEARCH_PROPERTY = "search";
	
	/** Bound property indicating to cancel the search. */
	public static final String 		CANCEL_SEARCH_PROPERTY = "cancelSearch";
	
	/** Bound property indicating that nodes are expanded. */
	public static final String 		NODES_EXPANDED_PROPERTY = "nodesExpanded";

	/** Action command ID indicating to cancel. */
	public static final int 		CANCEL = 0;
	
	/** Action command ID indicating to search. */
	public static final int 		SEARCH = 1;
	
	/** Action command ID indicating to search. */
	public static final int 		COLLAPSE = 2;
	
	/** Action command ID indicating to search. */
	public static final int 		HELP = 3;
	
	/** Action command ID indicating to set the date. */
	static final int 				DATE = 4;
	
	/** 
         * Action command ID indicating to reset the date fields
         */
        static final int                                RESET_DATE = 7;
        
	/** 
	 * The size of the invisible components used to separate buttons
	 * horizontally.
	 */
    static final Dimension  		H_SPACER_SIZE = new Dimension(5, 10);
    
	/** The UI with all the search fields. */
	protected SearchPanel 			uiDelegate;
	
	/** Button to close the dialog. */
	private JButton					searchButton;
	
	/** Component indicating the progress of the search. */
	private JXBusyLabel				busyLabel;
	
	/** Displays the search message. */
	private JLabel					progressLabel;
	
	/** The available nodes. */
	private List<SearchObject>		nodes;
	
	/** The possible types. */
	private List<SearchObject>		types;
	
	/** The default search context. */
	private SearchContext 			searchContext;
	
	/** The UI component hosting the result if any. */
	private JComponent				resultPane;
	
	/** The list of groups.*/
	protected Collection<GroupData> groups;
	
	/** The groups to handle.*/
	protected List<GroupContext> groupsContext;
	
	/** 
	 * Initializes the components composing the display. 
	 */
	private void initComponents()
	{
		uiDelegate = new SearchPanel(this);
		searchButton = new JButton("Search");
		searchButton.setToolTipText("Search");
		searchButton.setActionCommand(""+SEARCH);
		searchButton.addActionListener(this);

		searchButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		busyLabel = new JXBusyLabel();
		busyLabel.setEnabled(false);
		progressLabel = new JLabel("");
		progressLabel.setEnabled(false);
		progressLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
	}
	
	/**
	 * Builds and lays out the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.setBackground(UIUtilities.BACKGROUND_COLOR);
        bar.setBorder(null); 
        bar.add(searchButton);
        JPanel p = UIUtilities.buildComponentPanel(bar);
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        return p;
	}
	
	/** 
	 * Lays out the components indicating the status.
	 * 
	 * @return See above.
	 */
	private JPanel buildStatusBar()
	{
		JPanel bar = new JPanel();
		bar.setBackground(UIUtilities.BACKGROUND_COLOR);
		bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
		bar.add(progressLabel);
		JPanel p = UIUtilities.buildComponentPanelCenter(busyLabel);
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		bar.add(p);
		return bar;
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param showControl	Pass <code>true</code> to display the buttons,
	 * 						<code>false</code> otherwise.
	 */
	private void buildGUI(boolean showControl)
	{
		setBackground(UIUtilities.BACKGROUND_COLOR);
		double[][] size = {{TableLayout.PREFERRED}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED,
			TableLayout.PREFERRED, TableLayout.PREFERRED,
			TableLayout.PREFERRED}};
		setLayout(new TableLayout(size));
		int i = 0;
		String key = "0, ";
		add(uiDelegate, key+i);
		i++;
		if (showControl) {
			add(buildToolBar(), key+i);
		}
		i++;
		add(buildStatusBar(), key+i);
		resultPane = new JPanel();
		resultPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		resultPane.setLayout(new BoxLayout(resultPane, BoxLayout.Y_AXIS));
		JPanel sep = new SeparatorPane();
		sep.setBackground(UIUtilities.BACKGROUND_COLOR);
		i++;
		add(sep, key+i);
		i++;
		add(resultPane, key+i);
	}
	
	/** Closes and disposes of the window. */
	private void cancel()
	{
		firePropertyChange(CANCEL_SEARCH_PROPERTY,
				Boolean.valueOf(false), Boolean.valueOf(true));
		setSearchEnabled(-1);
	}
	
	/** Sets the default contexts. */
	private void setDefaultContext()
	{
		nodes = new ArrayList<SearchObject>();
		
	SearchObject node = new SearchObject(SearchContext.NAME, 
				null, NAME_TEXT);
    	nodes.add(node);
    	
    	node = new SearchObject(SearchContext.DESCRIPTION, 
				null, NAME_DESCRIPTION);
    	nodes.add(node);
    	
    	node = new SearchObject(SearchContext.ANNOTATION, null, 
                NAME_ANNOTATION);
    	nodes.add(node);
    	
    	types = new ArrayList<SearchObject>();
    	
    	node = new SearchObject(SearchContext.IMAGES, null, "Images");
        types.add(node);
        node = new SearchObject(SearchContext.DATASETS, null, "Datasets");
        types.add(node);
        node = new SearchObject(SearchContext.PROJECTS, null, "Projects");
        types.add(node);
        node = new SearchObject(SearchContext.PLATES, null, "Plates");
        types.add(node);
        node = new SearchObject(SearchContext.SCREENS, null, "Screens");
        types.add(node);
        
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param context		The context of the search.
	 */
	public SearchComponent(SearchContext context)
	{
		searchContext = context;
	}
	
	/** Creates a new instance. */
	public SearchComponent()
	{
		this(null);
	}
	
	/**
	 * Initializes the component. Displays the controls buttons.
	 * 
	 * @param controls The collection of controls to add.
	 * @param groups The collection of groups.
	 */
	public void initialize(
			Collection<GroupData> groups)
	{
		initialize(true, groups);
	}
	
	/**
	 * Initializes the component.
	 * 
	 * @param showControl Pass <code>true</code> to display the buttons,
	 *                    <code>false</code> otherwise.
	 * @param controls The collection of controls to add.
	 * @param groups The collection of groups.
	 */
	public void initialize(boolean showControl, Collection<GroupData> groups)
	{
		setDefaultContext();
		if (groups == null)
			throw new IllegalArgumentException("No groups specified.");
		this.groups = groups;
		initComponents();
		buildGUI(showControl);
	}
	
	/**
	 * Returns the terms that may be in the document.
	 * 
	 * @return See above.
	 */
	protected List<String> getTerms()
	{ 
		String[] terms = uiDelegate.getQueryTerms();
		List<String> l = new ArrayList<String>();
		if (terms != null) {
			for (int i = 0; i < terms.length; i++) {
				l.add(terms[i]);
			}
		}
		return l; 
	}
	
	/** 
	 * Sets the values to add. 
	 * 
	 * @param values The values to add.
	 */
	protected void setTerms(List<String> terms)
	{
		uiDelegate.setTerms(terms);
	}
	
	/**
	 * Returns the initial search context.
	 * 
	 * @return See above.
	 */
	SearchContext getSearchContext() { return searchContext; }
	
	/**
	 * Returns the collection of possible context.
	 * 
	 * @return See above.
	 */
	List<SearchObject> getNodes() { return nodes; }
	
	/**
	 * Returns the collection of possible types.
	 * 
	 * @return See above.
	 */
	List<SearchObject> getTypes() { return types; }
	
	/** Fires a property when the nodes are expanded. */
	void notifyNodeExpanded()
	{
		firePropertyChange(NODES_EXPANDED_PROPERTY,Boolean.FALSE, Boolean.TRUE);
	}
	
        /** Fires a property change to search. */
        void search() {
            
            List<Integer> scope = uiDelegate.getScope();
            SearchContext ctx;
    
                String query = uiDelegate.getQuery();
                
                if(query.trim().equals("*")) {
                    String msg = "Wildcard searches (*) must contain more than a single wildcard.";
                    DataBrowserAgent.getRegistry().getUserNotifier().notifyWarning("Cannot perform search", msg);
                    return;
                }
                
                ctx = new SearchContext(query, scope);
    
                Timestamp start = uiDelegate.getFromDate();
                Timestamp end = uiDelegate.getToDate();
                if (start != null && end != null) {
                    if(start.after(end)) 
                        ctx.setTime(end, start);
                    else
                        ctx.setTime(start, end);
                }
                else if(start!=null) {
                    ctx.setTime(start, null);
                }
                else if(end!=null) {
                    ctx.setTime(null, end);
                }
                ctx.setTimeType(uiDelegate.getDateType().equals(SearchPanel.ITEM_ACQUISITIONDATE) ? SearchParameters.DATE_ACQUISITION : SearchParameters.DATE_IMPORT);
                
                ctx.setSelectedOwner(uiDelegate.getUserId());
                ctx.setSelectedGroup(uiDelegate.getGroupId());
            
                ctx.setType(uiDelegate.getType());
            
                firePropertyChange(SEARCH_PROPERTY, null, ctx);
        }
        
	/**
	 * Returns the list of possible groups.
	 * 
	 * @return See above.
	 */
	List<GroupContext> getGroups()
	{ 
		if (groupsContext != null && groupsContext.size() > 0)
			return groupsContext;
		Iterator<GroupData> i = groups.iterator();
		GroupData g;
		GroupContext gc;
		groupsContext = new ArrayList<GroupContext>();
		while (i.hasNext()) {
			g = i.next();
			gc = new GroupContext(g);
			groupsContext.add(gc);
		}
		return groupsContext; 
	}
	
    /**
     * Sets the buttons enabled when performing search.
     * 
     * @param resultSize
     *            Number of results found, pass <code>-1</code> if search still
     *            in progress
     */
    public void setSearchEnabled(int resultSize) {
        if (resultSize == -1)
            setSearchEnabled("Searching...", false);
        else
            setSearchEnabled(resultSize + " results found", false);
    }
	
	/**
	 * Sets the buttons enabled when performing  search.
	 * 
	 * @param text 	The text to display.
	 * @param b 	Pass <code>true</code> to enable the {@link #searchButton}, 
	 * 				<code>false</code>otherwise, and modifies the cursor.
	 */
	public void setSearchEnabled(String text, boolean b)
	{
		searchButton.setEnabled(!b);
		busyLabel.setEnabled(b);
		busyLabel.setBusy(b);
		progressLabel.setText(text);
	}
	
	/**
	 * Adds the component displaying the result.
	 * 
	 * @param result The value to set.
	 */
	public void displayResult(JComponent result)
	{
		remove(resultPane);
		if (result != null) {
			resultPane = result;
			resultPane.setBackground(UIUtilities.BACKGROUND_COLOR);
			add(resultPane, "0, 4");
		}
		repaint();
	}
	
	/**
	 * Adds the specified component to the result display.
	 * 
	 * @param result The component to add.
	 * @param clear Pass <code>true</code> to remove the previous components,
	 *              <code>false</code> otherwise.
	 */
	public void addResult(JComponent result, boolean clear)
	{
		if (clear) resultPane.removeAll();
		if (result == null) return;
		resultPane.add(result);
		resultPane.add(new JSeparator());
		result.setBackground(UIUtilities.BACKGROUND_COLOR);
		revalidate();
		repaint();
	}
	
	/**
	 * Cancels or searches.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				cancel();
				break;
			case SEARCH:
				search();
				break;
			case HELP:
				help();
				break;
			case RESET_DATE:
			        uiDelegate.resetDate();
			        break;
		}
	}

	/** Subclasses should override this method. */
	protected void help() {}
	
	/**
	 * Handles a SearchEvent (sent by the search field
	 * in the toolbar); just takes the search query from
	 * the event and initiates a search with it
	 */
	public void handleSearchEvent(SearchEvent evt) {
	    uiDelegate.reset();
            uiDelegate.setTerms(Collections.singletonList(evt.getQuery()));
            search();
	}
	
}

/*
 * org.openmicroscopy.shoola.util.ui.search.SearchComponent 
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
package org.openmicroscopy.shoola.util.ui.search;


//Java imports
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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

	/** Bound property indicating to search. */
	public static final String 		SEARCH_PROPERTY = "search";
	
	/** Bound property indicating to cancel the search. */
	public static final String 		CANCEL_SEARCH_PROPERTY = "cancelSearch";
	
	/** Bound property indicating to select the owner. */
	public static final String 		OWNER_PROPERTY = "owner";
    
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
	
	/** Action command ID indicating to select the user who owns the object. */
	static final int 				OWNER = 5;

	/** 
	 * Action command ID indicating to select the user who annotated the 
	 * object. 
	 */
	static final int 				ANNOTATOR = 6;
	
	/** 
	 * The size of the invisible components used to separate buttons
	 * horizontally.
	 */
    static final Dimension  		H_SPACER_SIZE = new Dimension(5, 10);
    
	/** The UI with all the search fields. */
	private SearchPanel 			uiDelegate;
	
	/** Button to close the dialog. */
	private JButton					cancelButton;
	
	/** Button to close the dialog. */
	private JButton					searchButton;
	
	/** Progress bar visible while searching. */
	private JProgressBar			progressBar;
	
	/** Displays the search message. */
	private JLabel					progressLabel;
	
	/** The available nodes. */
	private List<SearchObject>		nodes;
	
	/** The possible types. */
	private List<SearchObject>		types;
	
	/** Either {@link #ANNOTATOR} or {@link #OWNER}. */
	private int						userIndex;
	
	/** The default search context. */
	private SearchContext 			searchContext;
	
	/** Sets the window properties. */
	private void setProperties()
	{
		/*
		setModal(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
        {
        	public void windowOpened(WindowEvent e) {
        		uiDelegate.setFocusOnSearch();
        	} 
        	
        	public void windowClosing(WindowEvent e) {
        		cancel();
        	}
        });
        */
		
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		uiDelegate = new SearchPanel(this);
		cancelButton = new JButton();
		cancelButton.setToolTipText("Cancel the search");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(this);
		searchButton = new JButton("Search");
		searchButton.setToolTipText("Search");
		searchButton.setActionCommand(""+SEARCH);
		searchButton.addActionListener(this);
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		progressLabel = new JLabel("");
		progressLabel.setEnabled(false);
		
	}
	
	/**
	 * Builds and lays out the toolbar.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
        bar.setBorder(null);
        //bar.add(cancelButton);
       // bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(searchButton);
        return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param showControl	Pass <code>true</code> to display the buttons,
	 * 						<code>false</code> otherwise.
	 */
	private void buildGUI(boolean showControl)
	{
		JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(UIUtilities.buildComponentPanel(uiDelegate));
        if (showControl) {
        	 controls.add(buildToolBar());
             controls.add(Box.createVerticalStrut(10));
        }
		add(controls, BorderLayout.CENTER);
	}
	
	/** Closes and disposes of the window. */
	private void cancel()
	{
		//setVisible(false);
		firePropertyChange(CANCEL_SEARCH_PROPERTY, Boolean.FALSE, Boolean.TRUE);
		//dispose();
	}
	
	/** Brings up the Help dialog. */
	private void showHelp()
	{
		//SearchHelp helpDialog = new SearchHelp((JFrame) getOwner());
		//UIUtilities.centerAndShow(helpDialog);
	}
	
	/** Fires a property change to search. */
	private void search()
	{
		//Terms cannot be null
		String[] some = uiDelegate.getSome();
		String[] must = uiDelegate.getMust();
		String[] none = uiDelegate.getNone();
	
		List<Integer> scope = uiDelegate.getScope();
		SearchContext ctx = new SearchContext(some, must, none, scope);
		int index = uiDelegate.getSelectedDate();
		Timestamp start, end;
		
		switch (index) {
			case SearchContext.RANGE:
				start = uiDelegate.getFromDate();
				end = uiDelegate.getToDate();
				if (start != null && end != null && start.after(end)) 
					ctx.setTime(end, start);
				else ctx.setTime(start, end);
				break;
			default:
				ctx.setTime(index);
		}
		ctx.setOwnerSearchContext(uiDelegate.getOwnerSearchContext());
		ctx.setAnnotatorSearchContext(uiDelegate.getAnnotatorSearchContext());
		ctx.setOwners(uiDelegate.getOwners());
		ctx.setAnnotators(uiDelegate.getAnnotators());
		ctx.setCaseSensitive(uiDelegate.isCaseSensitive());
		ctx.setType(uiDelegate.getType());
		ctx.setAttachmentType(uiDelegate.getAttachment());
		ctx.setTimeType(uiDelegate.getTimeIndex());
		ctx.setExcludedOwners(uiDelegate.getExcludedOwners());
		ctx.setExcludedAnnotators(uiDelegate.getExcludedAnnotators());
		firePropertyChange(SEARCH_PROPERTY, null, ctx);
	}
	
	/** Sets the default contexts. */
	private void setDefaultContext()
	{
		nodes = new ArrayList<SearchObject>();
    	SearchObject node = new SearchObject(SearchContext.NAME_DESCRIPTION, 
				null, "Name/Description");
    	nodes.add(node);
    	node = new SearchObject(SearchContext.TEXT_ANNOTATION, null, 
					"Comments");
    	nodes.add(node);
    	node = new SearchObject(SearchContext.TAGS, null, "Tags");
    	nodes.add(node);
    	node = new SearchObject(SearchContext.URL_ANNOTATION, null, "URLs");
    	nodes.add(node);
    	node = new SearchObject(SearchContext.FILE_ANNOTATION, null, 
					"Attachments");
    	nodes.add(node);
    	
    	types = new ArrayList<SearchObject>();
    	node = new SearchObject(SearchContext.IMAGES, null, "Image");
    	types.add(node);
    	node = new SearchObject(SearchContext.DATASETS, null, "Dataset");
    	types.add(node);
    	node = new SearchObject(SearchContext.PROJECTS, null, "Project");
    	types.add(node);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param context		The context of the search.
	 * @param showControl	Pass <code>true</code> to display the buttons,
	 * 						<code>false</code> otherwise.
	 */
	public SearchComponent(SearchContext context, boolean showControl)
	{
		searchContext = context;
		setDefaultContext();
		setProperties();
		initComponents();
		
		buildGUI(showControl);
	}
	
	/** Creates a new instance. */
	public SearchComponent()
	{
		this(null, true);
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
	
	/**
	 * Sets the buttons enabled when performing  search.
	 * 
	 * @param b Pass <code>true</code> to enable the {@link #searchButton}, 
	 * 			<code>false</code>otherwise, and modifies the cursor.
	 */
	public void setSearchEnabled(boolean b)
	{
		if (b) setSearchEnabled("Searching", b);
		else setSearchEnabled("", b);
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
		progressBar.setVisible(b);
		if (b) setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else setCursor(Cursor.getDefaultCursor());
		progressLabel.setText(text);
	}
	
	/**
	 * Sets the name of the selected user.
	 * 
	 * @param name The string to set.
	 */
	public void setUserString(String name)
	{
		if (name == null) return;
		name = name.trim();
		if (name.length() == 0) return;
		switch (userIndex) {
			case OWNER:
				uiDelegate.setOwnerString(name);
				break;
			case ANNOTATOR:
				uiDelegate.setAnnotatorString(name);
		}
	}
	
	/**
	 * Cancels or searches.
	 * @see {@link ActionListener#actionPerformed(ActionEvent)}
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
			case DATE:
				uiDelegate.setDateIndex();
				break;
			case OWNER:
				userIndex = OWNER;
				firePropertyChange(OWNER_PROPERTY, Boolean.FALSE, Boolean.TRUE);
				break;
			case ANNOTATOR:
				userIndex = ANNOTATOR;
				firePropertyChange(OWNER_PROPERTY, Boolean.FALSE, Boolean.TRUE);
				break;
			case HELP:
				showHelp();
		}
	}

}

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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries
import layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.SeparatorPane;
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
	
	/** Identifies the text for searching for URLs. */
	public static final String		NAME_URL = "URL";
	
	/** Identifies the text for searching for attachments. */
	public static final String		NAME_ATTACHMENT = "Attachments"; 
	
	/** Identifies the text for searching for attachments. */
	public static final String		NAME_RATE = "Rate"; 
	
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
	
	/** Action command ID indicating to display the advanced search. */
	static final int 				ADVANCED_SEARCH = 7;
	
	/** Action command ID indicating to display the advanced search. */
	static final int 				BASIC_SEARCH = 8;
	
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
	
	/** Component indicating the progress of the search. */
	private JXBusyLabel				busyLabel;
	
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
	
	/** The UI component hosting the result if any. */
	private JComponent				resultPane;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		uiDelegate = new SearchPanel(this);
		cancelButton = new JButton();
		cancelButton.setBackground(UIUtilities.BACKGROUND_COLOR);
		cancelButton.setToolTipText("Cancel the search");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(this);
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
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(cancelButton);
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
		double[][] size = {{TableLayout.FILL}, //columns
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
					TableLayout.PREFERRED, TableLayout.PREFERRED, 
					TableLayout.PREFERRED}}; //rows
		setLayout(new TableLayout(size));
		setBackground(UIUtilities.BACKGROUND_COLOR);
		add(uiDelegate, "0, 0");
		if (showControl) 
			add(buildToolBar(), "0, 1");
		add(buildStatusBar(), "0, 2");
		resultPane = new JPanel();
		resultPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		JPanel sep = new SeparatorPane();
		sep.setBackground(UIUtilities.BACKGROUND_COLOR);
		add(sep, "0, 3");
		add(resultPane, "0, 3");
	}
	
	/** Closes and disposes of the window. */
	private void cancel()
	{
		firePropertyChange(CANCEL_SEARCH_PROPERTY, Boolean.FALSE, Boolean.TRUE);
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
    	node = new SearchObject(SearchContext.TEXT_ANNOTATION, null, 
					NAME_COMMENTS);
    	nodes.add(node);
    	node = new SearchObject(SearchContext.TAGS, null, NAME_TAGS);
    	nodes.add(node);
    	node = new SearchObject(SearchContext.URL_ANNOTATION, null, NAME_URL);
    	nodes.add(node);
    	node = new SearchObject(SearchContext.FILE_ANNOTATION, null, 
					NAME_ATTACHMENT);
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
	
	/** Fires a property change to search. */
	void search()
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
		busyLabel.setEnabled(b);
		busyLabel.setBusy(b);
		progressLabel.setText(text);
	}
	
	/**
	 * Sets the name of the selected user.
	 * 
	 * @param userID The id of the owner.
	 * @param name   The string to set.
	 */
	public void setUserString(long userID, String name)
	{
		if (name == null) return;
		name = name.trim();
		if (name.length() == 0) return;
		switch (userIndex) {
			case OWNER:
				uiDelegate.setOwnerString(userID, name);
				break;
			case ANNOTATOR:
				uiDelegate.setAnnotatorString(name);
		}
		validate();
		repaint();
	}
	
	/**
	 * Adds the component displaying the result.
	 * 
	 * @param result The value to set.
	 */
	public void displayResult(JComponent result)
	{
		remove(resultPane);
		resultPane = result;
		resultPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		add(resultPane, "0, 4");
		repaint();
	}
	
	/** Requests focus on the search field. */
	public void requestFocusOnField()
	{
		uiDelegate.advancedSearch(false);
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
				firePropertyChange(OWNER_PROPERTY, Boolean.valueOf(false), 
						Boolean.valueOf(true));
				break;
			case ANNOTATOR:
				userIndex = ANNOTATOR;
				firePropertyChange(OWNER_PROPERTY, Boolean.valueOf(false), 
						Boolean.valueOf(true));
				break;
			case HELP:
				help();
				break;
			case ADVANCED_SEARCH:
				uiDelegate.advancedSearch(true);
				break;
			case BASIC_SEARCH: 
				uiDelegate.advancedSearch(false);
		}
	}

	/** Subclasses should override this method. */
	protected void help() {}
	
}

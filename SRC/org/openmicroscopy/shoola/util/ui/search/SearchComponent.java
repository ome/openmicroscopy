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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Dialog with advanced search options.
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
	extends JDialog
	implements ActionListener
{
	
	/** Bound property indicating to search. */
	public static final String 		SEARCH_PROPERTY = "search";
	
	/** Bound property indicating to cancel the search. */
	public static final String 		CANCEL_SEARCH_PROPERTY = "cancelSearch";
	
	/** Bound property indicating to select the owner. */
	public static final String 		OWNER_PROPERTY = "owner";
    
    /** The window's title. */
	private static final String		TITLE = "Search";
	
	/** The textual decription of the window. */
	private static final String 	TEXT = "To search for several terms, " +
			"separate each term with a comma.";
	
	/** Action command ID indicating to cancel. */
	private static final int 		CANCEL = 0;
	
	/** Action command ID indicating to search. */
	private static final int 		SEARCH = 1;

	/** Action command ID indicating to set the date. */
	static final int 				DATE = 3;
	
	/** Action command ID indicating to search. */
	static final int 				OWNER = 4;
	
	/** 
	 * The size of the invisible components used to separate buttons
	 * horizontally.
	 */
    static final Dimension  		H_SPACER_SIZE = new Dimension(5, 10);
    
	/** The UI with all the search fields. */
	private SearchPanel 		uiDelegate;
	
	/** Button to close the dialog. */
	private JButton				cancelButton;
	
	/** Button to close the dialog. */
	private JButton				searchButton;
	
	/** Progress bar visible while searching. */
	private JProgressBar		progressBar;
	
	/** Displays the search message. */
	private JLabel				progressLabel;
	
	/** The available nodes. */
	private List<SearchObject>	nodes;
	
	/** Sets the window properties. */
	private void setProperties()
	{
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
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		uiDelegate = new SearchPanel(this);
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Cancel the search");
		cancelButton.setActionCommand(""+CANCEL);
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
		getRootPane().setDefaultButton(searchButton);
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
        bar.add(cancelButton);
        bar.add(Box.createRigidArea(H_SPACER_SIZE));
        bar.add(searchButton);
        return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/**
	 * Builds and lays out the progress bar and the message.
	 * 
	 * @return See above.
	 */
	private JPanel buildStatusPanel()
	{
		JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));  
        progressPanel.add(progressLabel);
        progressPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        progressPanel.add(progressBar);
        progressPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        return UIUtilities.buildComponentPanel(progressPanel);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		IconManager icons = IconManager.getInstance();
		TitlePanel titlePanel = new TitlePanel(TITLE, TEXT, 
				icons.getIcon(IconManager.SEARCH_48));
		c.add(titlePanel, BorderLayout.NORTH);
		JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(UIUtilities.buildComponentPanel(uiDelegate));
        controls.add(buildToolBar());
        controls.add(Box.createVerticalStrut(10));
		c.add(controls, BorderLayout.CENTER);
		c.add(buildStatusPanel(), BorderLayout.SOUTH);
	}
	
	/** Closes and disposes of the window. */
	private void cancel()
	{
		setVisible(false);
		firePropertyChange(CANCEL_SEARCH_PROPERTY, Boolean.FALSE, Boolean.TRUE);
		//dispose();
	}
	
	/** Fires a property change to search. */
	private void search()
	{
		//Terms cannot be null
		List<String> terms = uiDelegate.getTerms();
		//Determine the context.
		List<Integer> l = uiDelegate.getScope();
		
		//Determine the time
		SearchContext ctx = new SearchContext(terms, l);
		int index = uiDelegate.getSelectedDate();
		switch (index) {
			case SearchContext.RANGE:
				ctx.setTime(uiDelegate.getFromDate(), uiDelegate.getToDate());
				break;
			default:
				ctx.setTime(index);
		}
		ctx.setUserSearchContext(uiDelegate.getUserSearchContext());
		ctx.setUsers(uiDelegate.getUsers());
		ctx.setSeparator(uiDelegate.getSeparator());
		ctx.setCaseSensitive(uiDelegate.isCaseSensitive());
		firePropertyChange(SEARCH_PROPERTY, null, ctx);
	}
	
	/** Sets the default contexts. */
	private void setDefaultContext()
	{
		IconManager icons = IconManager.getInstance();
		nodes = new ArrayList<SearchObject>();
    	SearchObject node = new SearchObject(SearchContext.TAGS, 
				icons.getImageIcon(IconManager.SEARCH_TAG), "Tags");
    	nodes.add(node);
    	node = new SearchObject(SearchContext.TAG_SETS, 
				icons.getImageIcon(IconManager.SEARCH_TAG_SET), "Tag Sets");
    	nodes.add(node);
    	node = new SearchObject(SearchContext.ANNOTATIONS, 
				icons.getImageIcon(IconManager.SEARCH_ANNOTATION), 
					"Annotations");
    	nodes.add(node);
    	node = new SearchObject(SearchContext.IMAGES, 
				icons.getImageIcon(IconManager.SEARCH_IMAGE), 
					"Images");
    	
    	nodes.add(node);
    	/*
    	node = new SearchObject(SearchContext.DATASETS, icons.getImageIcon(
    							IconManager.SEARCH_DATASET), "Datasets");
    	nodes.add(node);
    	node = new SearchObject(SearchContext.PROJECTS, 
    							icons.getImageIcon(IconManager.SEARCH_PROJECT), 
								"Projects");
    	nodes.add(node);
    	*/
    	
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of this dialog.
	 */
	public SearchComponent(JFrame owner)
	{
		super(owner);
		setDefaultContext();
		setProperties();
		initComponents();
		buildGUI();
		//setSize(WIN_SIZE);
		pack();
	}

	/**
	 * Returns the collection of possible context.
	 * 
	 * @return See above.
	 */
	List<SearchObject> getNodes() { return nodes; }
	
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
		uiDelegate.setUserString(name);
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
				firePropertyChange(OWNER_PROPERTY, Boolean.FALSE, Boolean.TRUE);
		}
	}
	
}

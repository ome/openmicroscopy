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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
	
	 /** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
    /** The window's title. */
	private static final String		TITLE = "Search";
	
	/** The textual decription of the window. */
	private static final String 	TEXT = "Add text";
	
	/** Action command ID indicating to cancel. */
	private static final int 		CANCEL = 0;
	
	/** Action command ID indicating to search. */
	private static final int 		SEARCH = 1;
	
	/** The UI with all the search fields. */
	private SearchPanel uiDelegate;
	
	/** Button to close the dialog. */
	private JButton		cancelButton;
	
	/** Button to close the dialog. */
	private JButton		searchButton;
	
	/** Sets the window properties. */
	private void setProperties()
	{
		setModal(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		uiDelegate = new SearchPanel();
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Cancels the search");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		searchButton = new JButton("Search");
		searchButton.setToolTipText("Searches");
		searchButton.setActionCommand(""+SEARCH);
		searchButton.addActionListener(this);
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
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		IconManager icons = IconManager.getInstance();
		TitlePanel titlePanel = new TitlePanel(TITLE, TEXT, 
				icons.getIcon(IconManager.SEARCH_48));
		c.add(titlePanel, BorderLayout.NORTH);
		c.add(uiDelegate, BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/** Closes and disposes of the window. */
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/** Fires a property change to search. */
	private void search()
	{
		
	}
	
	public SearchComponent(JFrame owner)
	{
		super(owner);
		setProperties();
		initComponents();
		buildGUI();
		pack();
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
		}
		
	}

}

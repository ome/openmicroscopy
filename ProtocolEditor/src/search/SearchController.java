package search;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.xml.sax.SAXParseException;

import tree.DataFieldConstants;
import ui.AbstractComponent;
import ui.IModel;
import ui.XMLView;
import util.ImageFactory;
import util.XMLMethods;
import util.XMLMethods.ElementAttributesHashMapHandler;
import xmlMVC.ConfigConstants;

/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 *
 * This code is adapted from the Apache demo classes:
 * (see below)
 */

/**
 * This class controls the search process.
 * It has a searchTerm source (a JTextComponent to provide a search term String) 
 * and it listens to a number of ActionEvent sources which can start the search process.
 * This includes a searchTerm search and a "more-like-this" search, using a <code>File</code>
 * from the model (uses the currently-opened file).
 * Once a search is complete, a changeEvent is fired, and listeners 
 * can get the searchPanel (displaying results).
 */

public class SearchController 
	extends AbstractComponent
	implements ActionListener {
	
	/**
	 * Action command for determining which search to perform;
	 * Keyword search performs a "Google" type search using a String from <code>searchTermSource</code>
	 */
	public static final String KEYWORD_SEARCH = "keywordSearch";
	
	/**
	 * Action command for determining which search to perform;
	 * More-Like-This-Search uses a file to build a search query, to return similar files
	 */
	public static final String MORE_LIKE_THIS_SEARCH = "moreLikeThisSearch";
	
	/**
	 * Action command for determining which search to perform;
	 * Template-Search uses a file to build a search query string. 
	 * Fields that are filled in are used as search fields (like a search form). 
	 */
	public static final String TEMPLATE_SEARCH = "templateSearch";
	
	/**
	 * Model, passed to SearchPanel, so results files can be opened. 
	 * Also used to provide a reference to the currently opened file, for use in 
	 * a more-like-this search. 
	 */
	IModel model;
	
	/**
	 * This is a UI component the user types the searchWord into. When the search is 
	 * started, use getText() to get the searchWord from this component
	 */
	JTextComponent searchTermSource;	
	
	/**
	 * The outer UI panel, holds a "Close" button and the searchResultsPanel 
	 */
	JPanel searchControllerPanel;
	
	/**
	 * This is an instance of <code>SearchPanel</code> that is generated from each search.
	 * It is placed within the <code>searchControllerPanel</code>.
	 */
	JPanel searchResultsPanel;
	
	/**
	 * a flag set to false when close button is clicked. When this is false,
	 * getSearchResultsPanel() will return null, so that observers will know to hide the search pane
	 */
	boolean displayControllerPanel = true;
	
	/**
	 * Creates an instance of this class. 
	 * @param model		model is used to open files (search hits) and provide current-file
	 */
	public SearchController(IModel model) {
		this.model = model;
		buildUI();
	}
	
	/**
	 * Used to provide the search with a searchTerm source. 
	 * This is required before you can run a searchTerm search, but is not needed for 
	 * a More-Like-This search.
	 * @param searchTermSource
	 */
	public void setSearchTermSource(JTextComponent searchTermSource) {
		this.searchTermSource = searchTermSource;
	}
	
	
	/**
	 * Allows button components to register as source of searchAction
	 */
	public void addSearchActionSource(AbstractButton button) {
		button.addActionListener(this);
	}
	
	/**
	 * Allows textField components to register as source of searchAction
	 */
	public void addSearchActionSource(JTextField textField) {
		textField.addActionListener(this);
	}

	
	/**
	 * Once this class is registered as an ActionListener to a button or textField,
	 * they can be used to start the search. 
	 * The default search is a searchTerm search, but if the ActionCommand is moreLikeThis
	 * then a more-like-this search will be performed.
	 */
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals(MORE_LIKE_THIS_SEARCH)) {
			findMoreLikeThis();
		} else 
		if (event.getActionCommand().equals(TEMPLATE_SEARCH)) {
			searchWithCurrentFileAsForm();
		} else
			searchFiles();
	}
	
	/**
	 * Starts the search using a searchTerm from <code>searchTermSource</code>
	 * Creates an instance of <code>SearchPanel</code>
	 */
	public void searchFiles() {
		
		if (searchTermSource == null) 
			return;
		
		JPanel newResultPanel = new SearchPanel(searchTermSource.getText(), model);
		setSearchResultsPanel(newResultPanel);
	}
	
	/**
	 * Starts the search using a given search term.
	 * Creates an instance of <code>SearchPanel</code>
	 */
	public void searchFiles(String searchQuery) {
		if (searchQuery == null) 
			return;
		JPanel newResultPanel = new SearchPanel(searchQuery, model);
		setSearchResultsPanel(newResultPanel);
	}
	
	public void searchWithCurrentFileAsForm() {
		File file = new File(ConfigConstants.OMERO_EDITOR_FILE + File.separator + "searchFile");
			
		model.exportTreeToXmlFile(file);
		
		XMLMethods xmlMethods = new XMLMethods();
		try {
			String searchQuery = "";
			
			ArrayList<HashMap<String, String>> elements = xmlMethods.getAllXmlFileAttributes(file, xmlMethods.new ElementAttributesHashMapHandler());
			
			for (HashMap<String, String> element : elements) {
				String attributeName = element.get(DataFieldConstants.ELEMENT_NAME);
				String value = element.get(DataFieldConstants.VALUE);
				
				if ((value != null) && (value.length() > 0) && 
						(attributeName != null) && (attributeName.length() > 0)) {
					attributeName = attributeName.replace(" ", "");
					
					searchQuery = searchQuery  + attributeName + ":" + "\"" + value + "\" ";
				}
			}
			
			System.out.println("SearchController searchWithCurrentFileAsForm() searchQuery = " + searchQuery);
			
			searchFiles(searchQuery);
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		file.delete();
	}

	/**
	 * Uses the currently opened file from <code>IModel</code> to start a more-like-this search.
	 */
	public void findMoreLikeThis() {
		File file = new File(ConfigConstants.OMERO_EDITOR_FILE + File.separator + "searchFile");
		
		model.exportTreeToXmlFile(file);
		
		if (file == null) return;
				
		searchFiles(file);
				
		file.delete();
		
	}

	/**
	 * Starts the search using a file from <code>IModel</code>
	 * Creates an instance of <code>SearchPanel</code>
	 */
	public void searchFiles(File file) {
		if (file == null) 
			return;
		JPanel newResultPanel = new SearchPanel(file, model);
		setSearchResultsPanel(newResultPanel);
	}
	
	
	
	/**
	 * Takes a JPanel from a search and places it within <code>searchControllerPanel</code>,
	 * removing any previous instances of <code>searchResultsPanel</code>
	 * This method is called after each search to display the results and notify any changeListeners.
	 * @param resultsPanel	The panel returned from a search
	 */
	public void setSearchResultsPanel(JPanel resultsPanel) {
		
		if (searchResultsPanel != null)
			searchControllerPanel.remove(searchResultsPanel);
		
		searchResultsPanel = resultsPanel;
		
		searchControllerPanel.add(searchResultsPanel, BorderLayout.CENTER);
		// update the flag which tells getSearchResultsPanel() to return Panel (instead of null)
		displayControllerPanel = true;
		fireStateChange();
	}
	
	/**
	 * Builds the UI. 
	 * A simple layout of a close-button placed above a space for the results panel.
	 * The search results panel is added after a search, and replaced with subsequent searches.
	 */
	public void buildUI() {
		
		searchControllerPanel = new JPanel(new BorderLayout());
		
		Icon noIcon = ImageFactory.getInstance().getIcon(ImageFactory.N0);
		JButton closeButton = new JButton("Close this window", noIcon);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				displayControllerPanel = false;
				fireStateChange();
			}
		});
		
		searchControllerPanel.add(closeButton, BorderLayout.NORTH);
		
	}
	
	/** called by observers that want to display the search panel after a search.
	 * Normally returns the <code>searchControllerPanel</code>, containing the closeButton
	 * and the searchResultsPanel.
	 * Unless the <code>displayControllerPanel</code> boolean is set to false (by the closeButton)
	 * in which case <code>null</code> is returned, as this panel should be closed. 
	 * 
	 * @return
	 */
	public JPanel getSearchResultsPanel() {
		if (displayControllerPanel)
			return searchControllerPanel;
		else return null;
	}

}

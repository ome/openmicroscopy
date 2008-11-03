/*
 * org.openmicroscopy.shoola.agents.editor.view.EditorModel 
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
package org.openmicroscopy.shoola.agents.editor.view;


//Java imports
import java.io.File;

import javax.swing.tree.TreeModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorLoader;
import org.openmicroscopy.shoola.agents.editor.FileLoader;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelFactory;

/** 
 * The Model component in the <code>Editor</code> MVC triad.
 * It delegates the treeModel to the Browser.
 * 
 * This class tracks the <code>Editor</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class  provide  a suitable data loader. 
 * The {@link EditorComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class EditorModel
{

	/** Holds one of the state flags defined by {@link Editor}. */
	private int				state;
	
	/** The name of the file to edit. */
	private String  		fileName;
	
	/** The id of the file to edit. Will not be set if editing local file */
	private long 			fileID;
	
	/** The size of the file to edit. */
	private long 			fileSize;
	
	/** The file retrieved either from the DB or local machine. */
	private File			fileToEdit;
	
	/**	The browser component */
	private Browser 		browser;
	
	/** 
	 * Will either be a data loader or <code>null</code> depending on the 
	 * current state. 
	 */
	private EditorLoader	currentLoader;
	
	/** Reference to the component that embeds this model. */
	private Editor			component;
	
	/** 
	 * Creates a new instance and sets the state to {@link Editor#NEW}.
	 * 
	 * @param fileName  The name of the file to edit.
	 * @param fileID	The id of the file to edit.
	 * @param fileSize 	The size of the file to edit.
	 */
	EditorModel(String fileName, long fileID, long fileSize)
	{
		state = Editor.NEW;
		this.fileID = fileID;
		this.fileName = fileName;
		this.fileSize = fileSize;
	}
	
	/**
	 * Creates a new instance and sets the state to {@link Editor#NEW}.
	 * {@link #fileSize} and {@link #fileID} are not set.
	 * File is not opened. To do this, call {@link Editor#setFileToEdit(File)}
	 * 
	 * @param file		The file to open. Sets the {@link #fileName} to the 
	 * 					name of this file but does not open file. 
	 */
	EditorModel(File file) 
	{
		if (file == null) throw new NullPointerException("No file.");
		
		state = Editor.NEW;
		this.fileName = file.getName();
	}
	
	/**
	 * Creates a new instance and sets the state to {@link Editor#NEW}.
	 * {@link #fileSize} and {@link #fileID} are not set.
	 *  
	 */
	EditorModel() 
	{
		state = Editor.NEW;
		this.fileName = EditorFactory.BLANK_MODEL;
	}
	
	/**
	 * Called by the <code>Editor</code> after creation to allow this
	 * object to store a back reference to the embedding component, and to 
	 * create a browser. 
	 * 
	 * @param component The embedding component.
	 */
	void initialize(Editor component)
	{ 
		this.component = component; 
		
		browser = BrowserFactory.createBrowser();
	}
	
	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link Editor} interface.  
	 */
	int getState() { return state; }    

	/**
	 * Sets the object in the {@link Editor#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		cancel();
		state = Editor.DISCARDED;
	}

	/**
	 * Sets the object in the {@link Editor#READY} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void cancel()
	{
		if (currentLoader != null) currentLoader.cancel();
		currentLoader = null;
		state = Editor.READY;
	}
	
	/**
	 * Returns the id of the file to edit.
	 * 
	 * @return See above.
	 */
	long getFileID() { return fileID; }
	
	/**
	 * Returns the name of the edited file.
	 * 
	 * @return See above.
	 */
	String getFileName() { return fileName; }
	
	/**
	 * Starts the asynchronous loading of the file to edit. 
	 * and sets the state to {@link Editor#LOADING}.
	 */
	void fireFileLoading()
	{
		currentLoader = new FileLoader(component, fileName, fileID, fileSize);
		currentLoader.load();
		state = Editor.LOADING;
	}

	/**
	 * Returns the file to edit.
	 * 
	 * @return See above.
	 */
	File getFileToEdit() { return fileToEdit; }
	
	/**
	 * Sets the file to edit.
	 * 
	 * @param file The file to edit.
	 */
	void setFileToEdit(File file)
	{
		fileToEdit = file;
		fileName = file.getName();
		TreeModel treeModel = TreeModelFactory.getTree(file);
		browser.setTreeModel(treeModel);
		state = Editor.READY;
	}
	
	/**
	 * Creates a new blank file and opens it in the browser.
	 */
	void setBlankFile() 
	{
		fileName = "New Blank File";
		TreeModel treeModel = TreeModelFactory.getTree();
		browser.setTreeModel(treeModel);
		state = Editor.READY;
	}
	
	/**
	 * Returns the browser component.
	 * 
	 * @return		see above.
	 */
	Browser getBrowser() { return browser; }
	
}

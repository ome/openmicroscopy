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
import omero.model.FileAnnotation;

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.EditorLoader;
import org.openmicroscopy.shoola.agents.editor.FileLoader;
import org.openmicroscopy.shoola.agents.editor.FileSaver;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelFactory;
import org.openmicroscopy.shoola.agents.editor.model.CPEexport;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import pojos.FileAnnotationData;

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
	private int					state;
	
	/** The name of the file to edit. */
	private String  			fileName;
	
	/** The annotation object hosting information about the file. */
	private FileAnnotationData 	fileAnnotation;
	
	/** The id of the file to edit. Will not be set if editing local file */
	private long 				fileID;
	
	/**  A string that defines the type of file we're editing. eg protocol */
	private String				nameSpace;
	
	/** The size of the file to edit. */
	//private long 				fileSize;
	
	/** The file retrieved either from the DB or local machine. */
	private File				fileToEdit;

	/**	The browser component */
	private Browser 			browser;
	
	/** 
	 * Will either be a data loader or <code>null</code> depending on the 
	 * current state. 
	 */
	private EditorLoader		currentLoader;
	
	/** Reference to the component that embeds this model. */
	private Editor				component;
	
	/**
	 * Saves the {@link TreeModel} from the {@link Browser} as an XML file.
	 * Returns <code>true</code> if the file can be parsed, <code>false</code>
	 * otherwise.
	 * 
	 * @param file The file to save.
	 * @return See above
	 */
	private boolean saveFile(File file)
	{
		CPEexport xmlExport = new CPEexport();
		boolean saved = xmlExport.export(browser.getTreeModel(), file);
		
		if (saved) browser.setEdited(false);
		return saved;
	}
	
	/** 
	 * Creates a new instance and sets the state to {@link Editor#NEW}.
	 * 
	 * @param fileAnnotationData  The annotation hosting the file to edit.
	 */
	EditorModel(FileAnnotationData fileAnnotationData)
	{
		state = Editor.NEW;
		if (fileAnnotationData == null)
			throw new IllegalArgumentException("No file annotation specified.");
		setFileAnnotationData(fileAnnotationData);
	}
	
	/** 
	 * Creates a new instance and sets the state to {@link Editor#NEW}.
	 * 
	 * @param fileID	The id of the file to edit.
	 */
	EditorModel(long fileID)
	{
		state = Editor.NEW;
		this.fileID = fileID;
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
	 * Returns the type of file we are editing.
	 *  E.g. openmicroscopy.org/omero/editor/protocol or experiment. 
	 *  
	 * @return See above. 
	 */
	String getNameSpace() { return nameSpace; }
	
	/**
	 * Starts the asynchronous loading of the file to edit. 
	 * and sets the state to {@link Editor#LOADING}.
	 */
	void fireFileLoading()
	{
		long size = 0;
		if (fileAnnotation != null) size = fileAnnotation.getFileSize();
		currentLoader = new FileLoader(component, fileName, fileID, size);
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
	 * If the file cannot be read by {@link TreeModelFactory#getTree()} then
	 * the state of this model is re-set to {@link Editor#NEW}.
	 * 
	 * @param file The file to edit.
	 * @return See above.
	 */
	boolean setFileToEdit(File file)
	{
		if (file == null) {
			fileToEdit = null;
			state = Editor.NEW;
			fileName = EditorFactory.BLANK_MODEL;
			return false;
		}
		TreeModel treeModel = null;
		
		// try opening file as recognised OMERO.editor file (pro.xml or cpe.xml)
		try {
			treeModel = TreeModelFactory.getTree(file);
			fileToEdit = file;
		} catch (ParsingException e) {
			
			// may get a parsing exception simply because the file was not 
			// recognised as Editor File..
			
			Registry reg = EditorAgent.getRegistry();
			UserNotifier un = reg.getUserNotifier();
			
			// ... try opening as ANY xml file
			try {
				treeModel = TreeModelFactory.getTreeXml(file);
				// if this worked, we have an XML file converted to cpe.xml
				// .. tell user..
				un.notifyInfo("File not recognised", 
						"File was converted from an unrecognised format into\n"+
						"OMERO.editor's cpe.xml format.\nOverwriting the " +
						"original file will erase the original XML format.");
				// must avoid overwriting the original file...
				// 'Save' won't work. 
				fileToEdit = null;
				setFileAnnotationData(null);
				
			} catch (ParsingException ex) {
				
				LogMessage message = new LogMessage();
				message.print(ex);
				reg.getLogger().error(this, message);
				
				// ...and notify the user. Use the exception message. 
				String errMsg = ex.getMessage();
			    un.notifyInfo("File Failed to Open", errMsg);
			}
		}
		
		if (treeModel == null) {
			fileToEdit = null;
			state = Editor.NEW;
			fileName = EditorFactory.BLANK_MODEL;
			return false;
		}
		
		fileName = file.getName();
		browser.setTreeModel(treeModel);
		state = Editor.READY;
		return true;
	}
	
	/**
	 * Creates a new blank file and opens it in the browser.
	 */
	void setBlankFile() 
	{
		fileToEdit = null;
		fileID = 0;
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

	/**
	 * Saves the locally the current file.
	 * If file came from the server, saves the file there. 
	 * If the file is not saved anywhere, returns <code>false</code>,
	 * otherwise returns <code>true</code>.
	 * Delegates to {@link #saveFile(File)} to do the saving.
	 * 
	 * @return See above.
	 */
	boolean saveLocalFile() 
	{
		// fileToEdit will not exist if not already saved 
		// (or working with file on the server)
		if (fileToEdit != null && fileToEdit.exists()) {
			return saveFile(fileToEdit);
		} 
		return false;
	}
	
	/**
	 * Starts an asynchronous call to save the passed file back to the server.
	 * 
	 * @param file The file to save.
	 */
	void fireFileSaving(File file)
	{
		boolean fileIsExp = browser.isModelExperiment();
		int fileType = (fileIsExp ? FileSaver.EXPERIMENT : FileSaver.PROTOCOL);
		
		saveFile(file);
		FileAnnotationData data = null;
		if (fileAnnotation != null) data = fileAnnotation;
		currentLoader = new FileSaver(component, file, data, fileType);
		currentLoader.load();
		state = Editor.SAVING;
	}
	
	/**
	 * Saves a file locally. If the save was successful, updates the current
	 * file, fileName, etc so that future "Save" operations will write to it.
	 * Delegates to {@link #saveFile(File)} to do the saving.  
	 * 
	 * @param file		The local file destination to save to. 
	 * @return			True if the save was successful. 
	 */
	boolean saveFileAs(File file)
	{
		if (saveFile(file)) {
			fileToEdit = file;
			fileName = file.getName();
			// indicates the current file is now local, even if it wasn't before.
			fileID = 0;	
			fileAnnotation = null;
			return true;
		}
		return false;
	}
	
	/**
	 * Sets the state.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; }
	
	/**
	 * Returns true if the browser is in the {@link Browser#TREE_EDITED} state.
	 * 
	 * @return		see above. 
	 */
	boolean hasDataToSave() 
	{
	 return (browser != null  &&  browser.getState() == Browser.TREE_EDITED);
	}

	/**
	 * Sets the file annotation data.
	 * 
	 * @param data The value to set.
	 */
	void setFileAnnotationData(FileAnnotationData data)
	{
		this.fileAnnotation = data;
		if (data == null) {
			this.fileAnnotation = null;
			fileID = 0;
			fileName = null;
			this.nameSpace = null;
			return;
		} 
		this.fileID = data.getFileID();
		this.fileName = data.getFileName();
	}
	
	/**
	 * This should be called when a file is first opened, so that it is known
	 * whether the file is originally a "Protocol" or "Experiment" file. 
	 * Sets the {@link #nameSpace} according to the presence of experiment info
	 * as determined by {@link Browser#isModelExperiment()}.
	 * Use this in preference to {@link FileAnnotationData#getNameSpace()}
	 * since namespace is not updated on server, and won't work for local files.
	 */
	void updateNameSpace()
	{
		if (browser.isModelExperiment()) {
			nameSpace = FileAnnotationData.EDITOR_EXPERIMENT_NS;
		} else {
			nameSpace = FileAnnotationData.EDITOR_PROTOCOL_NS;
		}
	}
	
	/**
	 * Gets a reference to the Original File if this file has been saved to 
	 * the server. Otherwise returns 0.
	 * 
	 * @return		see above
	 */
	long getOriginalFileId() 
	{
		if (fileAnnotation == null)  return 0;
		
		return fileAnnotation.getId();
	}
}

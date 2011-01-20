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
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.tree.TreeModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.EditorLoader;
import org.openmicroscopy.shoola.agents.editor.FileLoader;
import org.openmicroscopy.shoola.agents.editor.FileSaver;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.editor.model.CPEsummaryExport;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelFactory;
import org.openmicroscopy.shoola.agents.editor.model.CPEexport;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.IOUtil;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;

import pojos.DataObject;
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
	
	/** 
	 * The ID of the annotation for the file on the server, as returned by
	 * {@link FileAnnotationData#getId()}.
	 * Allows {@link #getAnnotationId()} to be called after the 
	 * {@link #EditorModel(long)} constructor has been used, before 
	 * {@link #setFileAnnotationData(FileAnnotationData)} has been called. 
	 */
	private long 				annotationID;
	
	/**  A string that defines the type of file we're editing. eg protocol */
	private String				nameSpace;
	
	/** The file retrieved either from the DB or local machine. */
	private File				fileToEdit;

	/**	The browser component */
	private Browser 			browser;
	
	/** The <code>DataObject</code> to link the editor file to. */
	private DataObject			parent;
	
	/** Either {@link Editor#PROTOCOL} or {@link Editor#EXPERIMENT}. */
	private int					type;
	
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
		type = Editor.PROTOCOL;
	}
	
	/** 
	 * Creates a new instance and sets the state to {@link Editor#NEW}.
	 * 
	 * @param annotationID	The id of the original file to edit.
	 */
	EditorModel(long annotationID)
	{
		state = Editor.NEW;
		this.annotationID = annotationID;
		
		// this sets the fileID with the annotationID so that when 
		// fireFileLoading is subsequently called, it will pass the annotationID
		this.fileID = annotationID;
		type = Editor.PROTOCOL;
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
		fileName = file.getName();
		type = Editor.PROTOCOL;
	}
	
	/**
	 * Creates a new instance and sets the state to {@link Editor#NEW}.
	 * The {@link #fileSize} and {@link #fileID} are not set. 
	 */
	EditorModel() 
	{
		state = Editor.NEW;
		fileName = EditorFactory.BLANK_MODEL;
		type = Editor.PROTOCOL;
	}
	
	/**
	 * Creates a new instance and sets the state to {@link Editor#NEW}.
	 * The {@link #fileSize} and {@link #fileID} are not set. 
	 * 
	 * @param parent The object to the new file to.
	 * @param name	 The name of the file.
	 * @param type   Either {@link Editor#PROTOCOL} or 
	 * 				 {@link Editor#EXPERIMENT}.
	 */
	EditorModel(DataObject parent, String name, int type)
	{
		state = Editor.NEW;
		if (name == null || name.length() == 0) 
			name = EditorFactory.BLANK_MODEL;
		fileName = name;
		this.parent = parent;
		if (type == Editor.PROTOCOL || type == Editor.EXPERIMENT)
			this.type = type;
		else this.type = Editor.PROTOCOL;
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
		browser = BrowserFactory.createBrowser(type);
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
		
		// fileID can be annotationID if fileName is null 
		// E.g. if EditorModel(long annotationID) was the constructor. 
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
	 * Returns the contents of the passed file.
	 * 
	 * @param file The file to handle.
	 * @return See above.
	 */
	String readTextFile(File file)
		throws IOException
	{
		state = Editor.READY;
		if (file == null) throw new IOException("File cannot be null.");
		return IOUtil.readTextFile(file);
	}
	
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
	 * Creates a new file if the name is not <code>null</code>
	 * or a new blank file and opens it in the browser. 
	 * 
	 * @param name The name of the default file or <code>null</code>.
	 */
	void setBlankFile(String name) 
	{
		fileToEdit = null;
		fileID = 0;
		boolean blank = false;
		if (name == null || name.trim().length() == 0) {
			blank = true;
			fileName = "New Blank File";
		} else fileName = name;
		TreeModel treeModel;
		if (blank) treeModel = TreeModelFactory.getTree();
		else {
			if (type == Editor.EXPERIMENT)
				treeModel = TreeModelFactory.getExperimentTree(name);
			else 
				treeModel = TreeModelFactory.getTree();
		}
		browser.setTreeModel(treeModel);
		state = Editor.READY;
	}
	
	/**
	 * Returns the browser component.
	 * 
	 * @return See above.
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
	 * Creates a temporary file in the Editor Home Directory with the 
	 * given fileName, sends the file to the server by 
	 * calling {@link #fireFileSaving(File)}, saving according to the 
	 * current {@link #fileAnnotation}.
	 * 
	 * @param fileName		The name of the file 
	 */
	void fireFileSaving(String fileName)
	{
		String filePath = EditorAgent.getEditorHome() + 
													File.separator + fileName;
		File toEdit = new File(filePath);
		fireFileSaving(toEdit);
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
		else data = new FileAnnotationData(file);
		String description = CPEsummaryExport.export(browser.getTreeModel());
		if (description != null)
			data.setDescription(description);
		currentLoader = new FileSaver(component, file, data, fileType, parent);
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
			//indicates the current file is now local, even if it wasn't before.
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
	 * Returns <code>true</code> if the browser is in the 
	 * {@link Browser#TREE_EDITED} state, <code>false</code> otherwise.
	 * 
	 * @return See above. 
	 */
	boolean hasDataToSave() 
	{
		return (browser != null  &&  
				browser.getSavedState() == Browser.TREE_EDITED);
	}

	/**
	 * Sets the file annotation data.
	 * 
	 * @param data The value to set.
	 */
	void setFileAnnotationData(FileAnnotationData data)
	{
		fileAnnotation = data;
		if (data == null) {
			fileID = 0;
			fileName = null;
			nameSpace = null;
			return;
		} 
		this.fileID = data.getFileID();
		this.fileName = data.getFileName();
		nameSpace = data.getNameSpace();
	}
	
	/**
	 * This should be called when a file is first opened, so that it is known
	 * whether the file is originally a "Protocol" or "Experiment" file. 
	 * Sets the {@link #nameSpace} according to the presence of experiment info
	 * as determined by {@link Browser#isModelExperiment()}.
	 * Use this in preference to {@link FileAnnotationData#getNameSpace()}
	 * since nameSpace is not updated on server, and won't work for local files.
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
	 * @return See above
	 */
	long getAnnotationId() 
	{
		if (fileAnnotation == null) return annotationID;
		return fileAnnotation.getId();
	}
	
	/**
	 * Returns the type.
	 * 
	 * @return See above.
	 */
	int getType() { return type; }

}

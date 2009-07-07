 /*
 * org.openmicroscopy.shoola.agents.editor.view.Autosave 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.tree.TreeModel;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.model.CPEexport;

/** 
 * This Autosave class implements the {@link Runnable} interface. 
 * The auto-save function is run periodically until {@link #shutDown()} 
 * is called. The the delay and the location of the temp file is determined 
 * by settings in the editor.xml file. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class Autosave implements Runnable {
	
	/**  Scheduler for handling auto-save functionality. */
	private ScheduledThreadPoolExecutor 	executor;
	
	/** Browser provides access to the file model */
	private Browser 						browser;
	
	/** The location to save the auto-save file */
	private File 							tempFile;
	
	/** 
	 * An index which is incremented for each Autosave instance and used to
	 * provide different temp file names for each instance. 
	 */
	static int index = 1;
	
	/**
	 * Creates an instance and starts the executor thread. 
	 * 
	 * @param browser		The source of the file to save. 
	 */
	Autosave(Browser browser) 
	{
		this.browser = browser;
		
		// make an executor for autosave with 1 thread 
		executor = new ScheduledThreadPoolExecutor(1);
		
		long delaySecs = 60;	// default delay. 
		try {
			String delay = (String)EditorAgent.getRegistry().lookup
										("/services/editor/autosaveDelay");
			delaySecs = Long.parseLong(delay);
		} catch (Exception ex) {}
		
		executor.scheduleWithFixedDelay
							(this, delaySecs, delaySecs, TimeUnit.SECONDS);
	}
	
	/** creates the tempFile for saving to */
	private void makeTempFile() 
	{
		String fileName = "RecoveredFile" + index + ".cpe.xml";
	
		String autosaveFolder = EditorAgent.getEditorAutosave();
		String filePath = autosaveFolder + File.separator + fileName;
		tempFile = new File(filePath);
		
		// don't overwrite existing file. 
		if (tempFile.exists()) {
			index++;
			makeTempFile();
		}
	}
	
	/**
	 * Implemented as specified by the {@link Runnable} interface. 
	 * Saved the current model from the {@link Browser} as an xml file.
	 * @see Browser#run()
	 */
	public void run() {
		
		TreeModel model = browser.getTreeModel();
		if (model == null)	return;
		
		if (tempFile == null) {
			makeTempFile();
		}
		
		CPEexport xmlExport = new CPEexport();
		boolean saved = xmlExport.export(model, tempFile);
		
		if (! saved) {
			makeTempFile();
		}
	}
	
	/**
	 * Called by the controller when auto-save functionality is no-longer 
	 * needed. Stops the auto-save thread and deletes the temp-file. 
	 * This should not be called if the program crashes - allowing the 
	 * temp file to be recovered.
	 */
	void shutDown() 
	{
		executor.shutdownNow();		// stop auto-saving
		
		if (tempFile != null)
			tempFile.delete();
	}
}

/*
 * org.openmicroscopy.shoola.agents.util.archived.view.DownloaderComponent 
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
package org.openmicroscopy.shoola.agents.util.archived.view;


//Java imports
import java.util.Iterator;
import java.util.List;
import java.util.Map;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

/** 
 * Implements the {@link Downloader} interface. Provided functionality to 
 * download archived files.
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
class DownloaderComponent 
	extends AbstractComponent
	implements Downloader
{
	
	/** The Model sub-component. */
	private DownloaderModel 	model;
	
	/** The Controller sub-component. */
    private DownloaderControl	controller;
	
    /** The view. */
    private FileChooser			view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
	DownloaderComponent(DownloaderModel model)
	{
		if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new DownloaderControl(this);
	}
	
	/** Links up the MVC triad. */
    void initialize()
    {
    	view = new FileChooser(DownloaderFactory.getOwner(), 
    			FileChooser.FOLDER_CHOOSER, "Download archived files",
    			"Select a directory to download the files into.");
    	
    	//view = new FolderChooserDialog(DownloaderFactory.getOwner());
    	controller.initialize(view);
    }
    
	/**
	 * Implemented as specified by the {@link Dowloader} interface.
	 * @see Downloader#activate()
	 */
	public void activate()
	{
		switch (model.getState()) {
			case NEW:
				if (view != null) UIUtilities.centerAndShow(view);
		}
	}

	/**
	 * Implemented as specified by the {@link Dowloader} interface.
	 * @see Downloader#cancel()
	 */
	public void cancel()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked "+
					"in the DISCARDED state.");
		discard();
	}

	/**
	 * Implemented as specified by the {@link Downloader} interface.
	 * @see Downloader#discard()
	 */
	public void discard()
	{
		model.discard();
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link Dowloader} interface.
	 * @see Downloader#getState()
	 */
	public int getState() { return model.getState(); }

	/**
	 * Implemented as specified by the {@link Dowloader} interface.
	 * @see Downloader#setArchivedFiles(Map)
	 */
	public void setArchivedFiles(Map files)
	{
		if (model.getState() != LOADING) return;
		model.setState(READY);
		fireStateChange();
		UserNotifier un = DownloaderFactory.getRegistry().getUserNotifier();
		if (files == null || files.size() == 0) {
			un.notifyInfo("Download Archived files", "No archived file stored" +
					" for the specified set of pixels.");
			return;
		} 
		Iterator i = files.keySet().iterator();
		Integer original;
		List unsaved;
		String f, message;
		int n;
		Iterator j;
		int index;
		while (i.hasNext()) {
			original = (Integer) i.next();
			unsaved = (List) files.get(original);
			if (unsaved == null || unsaved.size() == 0) {
				f = "file has";
				n = original.intValue();
				if (n > 1) f +="files have";
				message = "The archived "+f+" been successfully downloaded.";
			} else {
				f = "file was";
				n = original.intValue();
				if (n > 1) f +="files were";
				message = n+" archived "+f+" found but the " +
						"following couldn't be downloaded: \n";
				j = unsaved.iterator();
				index = 0;
				n = unsaved.size()-1;
				while (j.hasNext()) {
					message += (String) j.next();
					if (index != n) message += "\n";
					else message += ".";
					index++;
				}
			}
			un.notifyInfo("Download Archived files", message);
			break;
		}
	}

	/**
	 * Implemented as specified by the {@link Dowloader} interface.
	 * @see Downloader#download(String)
	 */
	public void download(String location)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked "+
					"in the DISCARDED state.");
		model.fireDownloadArchived(location);
		fireStateChange();
	}

}

/*
 * org.openmicroscopy.shoola.agents.fsimporter.ImagesImporter 
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
package org.openmicroscopy.shoola.agents.fsimporter;



//Java imports
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries


import org.apache.commons.collections.CollectionUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.treeviewer.DataTreeViewerLoader;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.log.LogMessage;

/** 
 * Imports the images.
 * This class calls one of the <code>importImages</code> methods in the
 * <code>ImageDataView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImagesImporter 
	extends DataImporterLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle; 
    
    /** The object hosting the information for the import. */
    private ImportableObject context;
    
    /** The identifier of the loader. */
    private Integer loaderID;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The Importer this data loader is for.
     * 					Mustn't be <code>null</code>.
     * @param ctx The security context.
	 * @param context	The context of the import.
	 * @param loaderID  The identifier of the loader.
	 */
	public ImagesImporter(Importer viewer,
			ImportableObject context, Integer loaderID)
	{
		super(viewer, null);
		if (context == null || CollectionUtils.isEmpty(context.getFiles()))
			throw new IllegalArgumentException("No Files to import.");
		this.context = context;
		this.loaderID = loaderID;
	}

	/** 
	 * Starts the import.
	 * @see DataImporterLoader#load()
	 */
	public void load()
	{
		handle = ivView.importFiles(context, this);
	}

	/** 
	 * Cancels the import.
	 * @see DataImporterLoader#load()
	 */
	public void cancel() { handle.cancel(); }

	/** 
     * Feeds the imported objects.
     * @see DataTreeViewerLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
        Map m = (Map) fe.getPartialResult();
        if (m != null) {
        	Entry entry;
        	Iterator i = m.entrySet().iterator();
        	while (i.hasNext()) {
				entry = (Entry) i.next();
				viewer.uploadComplete((ImportableFile) entry.getKey(),
						entry.getValue(), loaderID);
			}
        }
    }

	/**
	 * Notifies the user that an error has occurred and discards the loader.
	 * @see DSCallAdapter#handleException(Throwable) 
	 */
	public void handleException(Throwable exc) 
	{
		String s = "Data Import Failure: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        registry.getUserNotifier().notifyError("Data Import Failure", s, exc);
		viewer.cancelImport(loaderID);
	}

    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual pay-load (imported files) is delivered progressively
     * during the updates.
     * @see DataTreeViewerLoader#handleNullResult()
     */
    public void handleNullResult() {}

}

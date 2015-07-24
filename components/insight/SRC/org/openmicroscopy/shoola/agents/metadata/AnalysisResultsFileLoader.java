/*
 * org.openmicroscopy.shoola.agents.metadata.AnalysisResultsFileLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.metadata;


//Java imports
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.agents.metadata.util.AnalysisResultsItem;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.FileAnnotationData;

/** 
 * Loads the results.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AnalysisResultsFileLoader
	extends EditorLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** The files to load. */
    private Map<FileAnnotationData, File> results;
    
    /** Reference to the object hosting the analysis results. */
    private AnalysisResultsItem item;
    
    /** The total number of files to load. */
    private int total;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for. 
     * 				 Mustn't be <code>null</code>.
     * @param ctx The security context.
	 * @param item	 The object hosting information about the results to load.
     */
    public AnalysisResultsFileLoader(Editor viewer, SecurityContext ctx,
    		AnalysisResultsItem item)
    {
    	super(viewer, ctx);
    	if (item == null)
    		throw new IllegalArgumentException("No files to load");
    	this.item = item;
    	List<FileAnnotationData> attachments = item.getAttachments();
    	if (attachments == null || attachments.size() == 0)
    		throw new IllegalArgumentException("No files to load");
    	results = new HashMap<FileAnnotationData, File>(); 
    	total = attachments.size();
    }

    /** 
	 * Cancels the data loading. 
	 * @see EditorLoader#cancel()
	 */
	public void cancel()
	{
		handle.cancel();
		item.notifyLoading(false);
	}

	/** 
	 * Downloads the files. 
	 * @see EditorLoader#cancel()
	 */
	public void load()
	{
		List<FileAnnotationData> attachments = item.getAttachments();
		Iterator<FileAnnotationData> i = attachments.iterator();
		Map<FileAnnotationData, File> map = 
			new HashMap<FileAnnotationData, File>();
    	File f;
		String dir = MetadataViewerAgent.getTmpDir();
		FileAnnotationData fa;
    	while (i.hasNext()) {
    		fa = i.next();
    		f = new File(dir+File.separator+fa.getFileID()+"_"+
					fa.getFileName());
			f.deleteOnExit();
			map.put(fa, f);
    	}
		handle = mhView.loadFiles(ctx, map, this);
	}
	
	/** 
	 * Feeds the file back to the viewer, as they arrive. 
	 * @see EditorLoader#update(DSCallFeedbackEvent)
	 */
	public void update(DSCallFeedbackEvent fe) 
	{
		Map m = (Map) fe.getPartialResult();
		if (m != null) {
			Entry entry;
			Iterator i = m.entrySet().iterator();
			FileAnnotationData fa;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				fa = (FileAnnotationData) entry.getKey();
				results.put(fa, (File) entry.getValue());
			}
			if (results.size() == total) {
				item.setLoadedFiles(results);
				viewer.analysisResultsLoaded(item);
			}
		}

	}
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual payload is delivered progressively during the updates
     * if data is <code>null</code>.
     * @see EditorLoader#handleNullResult()
     */
    public void handleNullResult() {}
    
}

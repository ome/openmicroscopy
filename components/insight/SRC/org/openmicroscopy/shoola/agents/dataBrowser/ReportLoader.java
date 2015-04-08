/*
 * org.openmicroscopy.shoola.agents.dataBrowser.ReportLoader 
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
package org.openmicroscopy.shoola.agents.dataBrowser;



//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageNode;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * Loads data for the report.
 * This class calls the <code>loadStructuredData</code> method in the
 * <code>MetadataHandlerView</code>.
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
public class ReportLoader 
	extends DataBrowserLoader
{

	/** The images the reports are for. */
	private Map<Long, ImageNode>	imageMap;
	
	/** The images the reports are for. */
	private List<DataObject>		nodes;
	
	/** The types of annotation to add to the report. */
	private List<Class>				types;
	
	/** The full path of the report. */
	private String					path;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle				handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param types		The types of annotation to add to the report.
     * @param images	The images the report is for.
     * @param path		The name of the report file.
     */
	public ReportLoader(DataBrowser viewer, SecurityContext ctx, 
			List<Class> types, Collection<ImageNode> images, String path)
	{
		super(viewer, ctx);
		if (images == null || images.size() == 0)
			throw new IllegalArgumentException("No images specified.");
		if (path == null || path.trim().length() == 0)
			throw new IllegalArgumentException("No file name specified.");
		this.path = path;
		this.types = types;
		imageMap = new LinkedHashMap<Long, ImageNode>(images.size());
		nodes = new ArrayList<DataObject>(images.size());
		Iterator<ImageNode> i = images.iterator();
		ImageNode n;
		ImageData data;
		while (i.hasNext()) {
			n = i.next();
			data = (ImageData) n.getHierarchyObject();
			imageMap.put(data.getId(), n);
			nodes.add(data);
		}
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see DataBrowserLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/** 
	 * Loads the data for the report.
	 * @see DataBrowserLoader#load()
	 */
	public void load()
	{
		handle = mhView.loadStructuredData(ctx, nodes, -1, false, this);
	}
	
	/**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
    	Map<DataObject, StructuredDataResults> m = 
    			(Map<DataObject, StructuredDataResults>) result;
    	Map<ImageNode, StructuredDataResults> 
    		r = new LinkedHashMap<ImageNode, StructuredDataResults>();
    	Entry<DataObject, StructuredDataResults> entry;
    	Iterator<Entry<DataObject, StructuredDataResults>>
    	i = m.entrySet().iterator();
    	ImageNode n;
    	while (i.hasNext()) {
    		entry = i.next();
			n = imageMap.get(entry.getKey().getId());
			r.put(n, entry.getValue());
		}
    	viewer.setReportData(r, types, path);
    }
    
}

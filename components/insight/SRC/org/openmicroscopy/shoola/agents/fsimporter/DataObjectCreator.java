/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.fsimporter;

import java.util.List;

import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.treeviewer.DataBrowserLoader;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.DataObject;

/** 
 * Creates new data objects i.e. <code>Project</code>, <code>Screen</code>
 * or <code>Dataset</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DataObjectCreator 
	extends DataImporterLoader
{

    /** The {@link DataObject} to handle. */
    private DataObject child;
    
    /** The parent of the data object to create. */
    private DataObject parent;
    
	/** Handle to the asynchronous call so that we can cancel it. */
	private CallHandle handle;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The Importer this data loader is for.
     * Mustn't be <code>null</code>.
     * @param ctx The security context.
	 * @param child The {@link DataObject} to handle.
     * @param parent The parent of the object to create,
     *               <code>null</code> if no parent.
	 */
	public DataObjectCreator(Importer viewer, SecurityContext ctx,
		DataObject child, DataObject parent)
	{
		super(viewer, ctx);
		if (child == null)
			throw new IllegalArgumentException("No object to create.");
		this.parent = parent;
		this.child = child;
	}
	
	/** 
	 * Creates new object.
	 * @see DataImporterLoader#load()
	 */
	public void load()
	{
		handle = dmView.createDataObject(ctx, child, parent, this);
	}
	
	/** 
	 * Cancels the data loading.
	 * @see DataImporterLoader#load()
	 */
	public void cancel() { handle.cancel(); }

   /**
    * Feeds the result back to the viewer.
    * @see DataBrowserLoader#handleResult(Object)
    */
   public void handleResult(Object result)
   {
	   if (viewer.getState() == Importer.DISCARDED) return;
	   List l = (List) result;
	   DataObject d = null;
	   if (l != null && l.size() == 1) d = (DataObject) l.get(0);
	   viewer.onDataObjectSaved(d, parent);
   }

}

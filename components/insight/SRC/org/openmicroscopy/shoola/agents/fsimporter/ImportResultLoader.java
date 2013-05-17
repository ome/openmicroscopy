/*
 * org.openmicroscopy.shoola.agents.fsimporter.ImportResultLoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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



//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.springframework.util.CollectionUtils;
import pojos.ImageData;
import pojos.PlateData;

/**
 * Loads the result of the import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class ImportResultLoader
	extends DataImporterLoader
{

	/** Handle to the asynchronous call so that we can cancel it.*/
	private CallHandle handle;
	
	/** The identifier of the objects to load.*/
	private final List<Long> ids;
	
	/** The type of object to load, either Image or Plate.*/
	private final Class<?> nodeType;

	/** The importable object.*/
	private final ImportableFile object;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The Importer this data loader is for.
	 * Mustn't be <code>null</code>.
     * @param ctx The security context.
	 * @param ids The collection of objects to load.
	 * @param nodeType The type of node either Image or Plate
	 */
	public ImportResultLoader(Importer viewer, SecurityContext ctx,
			List<Long> ids, Class<?> nodeType, ImportableFile object)
	{
		super(viewer, ctx);
		if (CollectionUtils.isEmpty(ids) || object == null || nodeType == null)
			throw new IllegalArgumentException("No data to load");
		//Check supported type
		if (!(nodeType.equals(ImageData.class) ||
				nodeType.equals(PlateData.class)))
			throw new IllegalArgumentException("Type not supported");
		this.ids = ids;
		this.nodeType = nodeType;
		this.object = object;
	}
	
	/** 
	 * Loads the images or plate.
	 * @see DataImporterLoader#load()
	 */
	public void load()
	{
		if (nodeType.equals(ImageData.class)) {
			handle = dmView.getImages(ctx, nodeType, ids, userID, this);
		} else {
			
		}
	}
	
	/** 
	 * Cancels the data loading.
	 * @see DataImporterLoader#load()
	 */
	public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataImporterLoader#handleResult(Object)
     */
	public void handleResult(Object result)
	{
		
	}
}

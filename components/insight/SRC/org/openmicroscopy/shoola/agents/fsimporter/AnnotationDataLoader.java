/*
 * org.openmicroscopy.shoola.agents.fsimporter.AnnotationDataLoader
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.treeviewer.DataBrowserLoader;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.util.processing.chart.ImageData;

import pojos.AnnotationData;
import pojos.FileAnnotationData;
import pojos.FilesetData;

/**
 * Loads the annotations of a given type linked to the specified image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class AnnotationDataLoader
	extends DataImporterLoader
{

	/** Handle to the asynchronous call so that we can cancel it.*/
	private CallHandle handle;
	
	/** The index of the UI element.*/
	private int index;
	
	/** The identifier of the image.*/
	private long fileSetID;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The Importer this data loader is for.
	 * Mustn't be <code>null</code>.
     * @param ctx The security context.
	 * @param imageID The identifier of the image.
	 * @param index The index of the UI element.
	 */
	public AnnotationDataLoader(Importer viewer, SecurityContext ctx,
			long fileSetID, int index)
	{
		super(viewer, ctx);
		this.fileSetID = fileSetID;
		this.index = index;
	}
	
	/** 
	 * Loads the annotations.
	 * @see DataImporterLoader#load()
	 */
	public void load()
	{
		List<String> nsInclude = new ArrayList<String>();
		nsInclude.add(FileAnnotationData.LOG_FILE_NS);
		handle = mhView.loadAnnotations(ctx, ImageData.class,
				Arrays.asList(fileSetID), FilesetData.class, nsInclude,
				null, this);
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
		Map<Long, Collection<AnnotationData>> map =
				(Map<Long, Collection<AnnotationData>>) result;
		viewer.setImportLogFile(map.get(fileSetID), index);
	}
}

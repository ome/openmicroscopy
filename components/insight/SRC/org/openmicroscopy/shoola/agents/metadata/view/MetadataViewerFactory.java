/*
 * org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory 
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
package org.openmicroscopy.shoola.agents.metadata.view;



//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * Factory to create {@link MetadataViewer} component.
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
public class MetadataViewerFactory 
{

	/** The sole instance. */
	private static final MetadataViewerFactory  
						singleton = new MetadataViewerFactory();
	
	/**
	 * Returns the {@link MetadataViewer}.
	 * 
	 * @param data	The object viewed as the root of the browser.
	 * @param type  The type of data object to handle, if type is either
	 *              <code>DatasetData</code> or <code>TagAnnotationData</code>,
	 *              this implies that the viewer is for a batch annotation.
	 * @return See above.
	 */
	public static MetadataViewer getViewer(List<Object> data, Class type)
	{
		if (data == null || data.size() == 0)
			throw new IllegalArgumentException("No data to edit");
		MetadataViewerModel model = new MetadataViewerModel(data, 
				MetadataViewer.RND_GENERAL);
		model.setDataType(type);
		return singleton.createViewer(model);
	}
	
	/**
	 * Returns the {@link MetadataViewer}.
	 * 
	 * @param refObject	The object viewed as the root of the browser.
	 * @return See above.
	 */
	public static MetadataViewer getViewer(Object refObject)
	{
		return getViewer(refObject, MetadataViewer.RND_GENERAL);
	}
	
	/**
	 * Returns the {@link MetadataViewer}.
	 * 
	 * @param refObject	The object viewed as the root of the browser.
	 * @param index 	One of the following constants: 
	 * 					{@link MetadataViewer#RND_GENERAL} or
	 * 					{@link MetadataViewer#RND_SPECIFIC}.
	 * @return See above.
	 */
	public static MetadataViewer getViewer(Object refObject, int index)
	{
		MetadataViewerModel model = new MetadataViewerModel(refObject, index);
		return singleton.createViewer(model);
	}
	
	/** Creates a new instance. */
	private MetadataViewerFactory() {}
	
	/**
	 * Creates and returns a {@link MetadataViewer}.
	 * 
	 * @param model	The Model.
	 * @return See above.
	 */
	private MetadataViewer createViewer(MetadataViewerModel model)
	{
		MetadataViewerComponent comp = new MetadataViewerComponent(model);
		model.initialize(comp);
		comp.initialize();
		return comp;
	}
	
}

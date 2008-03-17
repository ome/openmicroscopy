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
	 * @param refObject			The object viewed as the root of the browser.
	 * @param thumbnailRequired Pass <code>true</code> to indicate to load the
	 * 							thumbnail, <code>false</code> otherwise.
	 * @return See above.
	 */
	public static MetadataViewer getViewer(Object refObject, boolean
										thumbnailRequired)
	{
		MetadataViewerModel model = new MetadataViewerModel(refObject);
		return singleton.createViewer(model, thumbnailRequired);
	}
	
	/**
	 * Returns the {@link MetadataViewer}.
	 * 
	 * @param refObject	The object viewed as the root of the browser.
	 * @return See above.
	 */
	public static MetadataViewer getViewer(Object refObject)
	{
		return getViewer(refObject, true);
	}
	
	/** Creates a new instance. */
	private MetadataViewerFactory()
	{
		
	}
	
	/**
	 * Creates and returns a {@link MetadataViewer}.
	 * 
	 * @param model				The Model.
	 * @param thumbnailRequired Pass <code>true</code> to indicate to load the
	 * 							thumbnail, <code>false</code> otherwise.
	 * @return See above.
	 */
	private MetadataViewer createViewer(MetadataViewerModel model,
										boolean thumbnailRequired)
	{
		MetadataViewerComponent comp = new MetadataViewerComponent(model);
		model.initialize(comp, thumbnailRequired);
		comp.initialize();
		return comp;
	}
	
}

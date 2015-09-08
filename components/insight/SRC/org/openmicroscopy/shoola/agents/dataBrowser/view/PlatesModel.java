/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserTranslator;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import omero.gateway.SecurityContext;

import omero.gateway.model.PlateData;

/**
 * A concrete Model for a collection of plates.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class PlatesModel 
	extends DataBrowserModel
{

	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param parent	The parent of the plates.
	 * @param datasets 	The collection to plates the model is for.
	 */
	PlatesModel(SecurityContext ctx, Object parent, Set<PlateData> plates)
	{
		super(ctx);
		if (plates  == null) 
			throw new IllegalArgumentException("No plates.");
		this.parent = parent;
		Set visTrees = DataBrowserTranslator.transformHierarchy(plates);
        browser = BrowserFactory.createBrowser(visTrees);
        //layoutBrowser();
	}
	
	/**
	 * Creates a concrete loader.
	 * @see DataBrowserModel#createDataLoader(boolean, Collection)
	 */
	protected  List<DataBrowserLoader> createDataLoader(boolean refresh, 
			Collection ids)
	{
		return null;
	}

	/**
	 * Returns the type of this model.
	 * @see DataBrowserModel#getType()
	 */
	protected int getType() { return DataBrowserModel.PLATES; }

	/**
	 * No-operation implementation in our case.
	 * @see DataBrowserModel#getNodes()
	 */
	protected List<ImageDisplay> getNodes() { return null; }

}

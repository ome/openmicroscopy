/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowserFactory 
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;


//Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Factory to create {@link DataBrowser} components.
 * This class keeps track of all {@link DataBrowser} instances that have been
 * created and are not yet {@link DataBrowser#DISCARDED discarded}. A new
 * component is only created if none of the <i>tracked</i> ones is already
 * displaying the given hierarchy.  Otherwise, the existing component is
 * recycled.
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
public class DataBrowserFactory
	implements ChangeListener
{

	/** The sole instance. */
	private static final DataBrowserFactory  
						singleton = new DataBrowserFactory();
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param grandParent	The grandparent of the node.
	 * @param parent		The parent's node.
	 * @param images		The collection to set.
	 * @return See above.
	 */
	public static final DataBrowser getDataBrowser(Object grandParent, 
										Object parent, 
										Set<ImageData> images)
	{
		return singleton.createImagesDataBrowser(grandParent, parent, images);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param parent	The parent's node.
	 * @param nodes		The collection to set.
	 * @return See above.
	 */
	public static final DataBrowser getDataBrowser(ProjectData parent, 
													Set<DatasetData> nodes)
	{
		return singleton.createDatasetsDataBrowser(parent, nodes);
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed node.
	 * 
	 * @param parent	The node.
	 * @return See above.
	 */
	public static final DataBrowser getDataBrowser(Object parent)
	{
		
		return singleton.browsers.get(parent.toString());
	}

	/** Map used to keep track of the browsers. */
	private Map<String, DataBrowser> 	browsers;
	
	/** Creates a new instance. */
	private DataBrowserFactory()
	{
		browsers = new HashMap<String, DataBrowser>();
	}
	
	/**
	 * Creates a new {@link DataBrowser} for the passed collection of images.
	 * 
	 * @param grandParent	The grandParent of the node.
	 * @param parent		The parent's node.
	 * @param images		The collection to set.
	 * @return See above.
	 */
	private DataBrowser createImagesDataBrowser(Object grandParent, 
												Object parent, 
										Set<ImageData> images)
	{
		DataBrowserModel model = new ImagesModel(parent, images);
		model.setGrandParent(grandParent);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		browsers.put(parent.toString(), comp);
		return comp;
	}

	/**
	 * Creates a new {@link DataBrowser} for the passed collection of datasets.
	 * 
	 * @param parent	The parent's node.
	 * @param datasets	The collection to set.
	 * @return See above.
	 */
	private DataBrowser createDatasetsDataBrowser(DataObject parent, 
											Set<DatasetData> datasets)
	{
		DataBrowserModel model = new DatasetsModel(parent, datasets);
		DataBrowserComponent comp = new DataBrowserComponent(model);
		model.initialize(comp);
		comp.initialize();
		browsers.put(parent.toString(), comp);
		return comp;
	}

	/**
	 * Removes a browser from the {@link #browsers} set when it is
	 * {@link DataBrowser#DISCARDED discarded}. 
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
}

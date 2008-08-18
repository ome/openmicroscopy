/*
 * util.ui.DatasetModel 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package util.ui;

import java.util.ArrayList;
import java.util.List;

import omero.api.GatewayPrx;
import omero.model.Dataset;
import omero.model.Project;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DatasetModel
{	
	GatewayPrx service;
	UserNode 	userNode;
	
	public DatasetModel(GatewayPrx service) 
			throws omero.ServerError
	{
		this.service = service;
		populateDataModel();
	}
	
	/**
	 * Populate the Datamodel with the project->dataset list.
	 * @throws omero.ServerError 
	 * @throws DSOutOfServiceException 
	 *
	 */
	private void populateDataModel() 
		throws omero.ServerError
	{
		userNode = new UserNode(service.getUsername());
		List<Project> projects = service.getProjects(null, false);
		for(Project p : projects)
		{
			List<Long> ids = new ArrayList<Long>();
			ids.add(p.id.val);
			List<Dataset> datasets = service.getDatasets(ids, false);
			ProjectNode projectNode = new ProjectNode(p);
			for(Dataset d : datasets)
				projectNode.add(new DatasetNode(d));
			userNode.add(projectNode);
		}
	}
	
	/**
	 * Get the tree constructed. 
	 * @return see above.
	 */
	public UserNode getTree()
	{
		return userNode;
	}
	
}



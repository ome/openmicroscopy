/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
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
package training;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static omero.rtypes.rint;
import static omero.rtypes.rstring;
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.DataManagerFacility;
import omero.log.SimpleLogger;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Project;
import omero.model.TagAnnotation;
import omero.sys.Filter;
import omero.sys.ParametersI;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.TagAnnotationData;

/**
 * More advanced code for how to load data from an OMERO server.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class ReadDataAdvanced
{

	//The value used if the configuration file is not used. To edit*/
	/** The server address.*/
	private String hostName = "serverName";

	/** The username.*/
	private String userName = "userName";
	
	/** The password.*/
	private String password = "password";
	//end edit
	
	/** The name of a Dataset.*/
	private String datasetName = "MyDataset";

	/** The name of a Tag.*/
	private String tagName = "MyTag";

	private Gateway gateway;
    
    private SecurityContext ctx;
	
	/**
	 * Creates 3 Datasets with the name defined by {@link #datasetName}.
	 */
	private void createDatasets()
		throws Exception
	{
	    DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
	    
		for (int i = 0; i < 3; i ++)
		{
			DatasetData d = new DatasetData();
			d.setName(datasetName);
			dm.saveAndReturnObject(ctx, d);
		}
	}

	/**
	 * Creates 3 Tags with the namespace value defined by {@link #tagName}.
	 */
	private void createTags()
		throws Exception
	{
	    DataManagerFacility dm = gateway.getFacility(DataManagerFacility.class);
		for (int i = 0; i < 3; i ++)
		{
			TagAnnotationData t = new TagAnnotationData(tagName);
			((TagAnnotation)t.asIObject()).setNs(rstring(ConfigurationInfo.TRAINING_NS));
			t.setDescription(String.format("%s %s", tagName, i));
			dm.saveAndReturnObject(ctx, t);
		}
	}


	/**
	 * Retrieve the Datasets that match the name value.
	 */
	private void loadDatasetsByName()
		throws Exception
	{
		final boolean caseSensitive = true;
		final Filter filter = new Filter();
		// Return the first 10 hits or less.
		filter.limit = rint(10);
		filter.offset = rint(0);

		IQueryPrx proxy = gateway.getQueryService(ctx);
		List<IObject> datasets = (List<IObject>)
		proxy.findAllByString("Dataset", "name", datasetName, caseSensitive,
				filter);
		System.out.println("\nList Datasets:");
		for (IObject obj : datasets)
		{
			Dataset d = (Dataset) obj;
			System.out.println("ID: " + d.getId().getValue() + " Name: " +
					d.getName().getValue());

		}
	}

	/**
	 * Retrieve the Tags that match the ns value.
	 */
	private void loadTagsByNS()
		throws Exception
	{
		final boolean caseSensitive = true;
		final Filter filter = new Filter();
		// Return the first 10 hits or less.
		filter.limit = rint(10);
		filter.offset = rint(0);

		IQueryPrx proxy = gateway.getQueryService(ctx);
		List<IObject> tags = (List<IObject>)
		proxy.findAllByString("TagAnnotation", "ns",
				ConfigurationInfo.TRAINING_NS, caseSensitive, filter);
		System.out.println("\nList Tags:");
		for (IObject obj : tags)
		{
			TagAnnotation t = (TagAnnotation) obj;
			System.out.println("ID: " + t.getId().getValue() + " NS: " +
					t.getNs().getValue());
		}
	}
	
	/** 
	 * Retrieve the projects and the orphaned datasets i.e. datasets not in
	 * a project.
	 * 
	 * If a project contains datasets, the datasets will automatically be loaded.
	 */
	private void loadProjectsAndOrphanedDatasets()
		throws Exception
	{
		IContainerPrx proxy = gateway.getPojosService(ctx);
		ParametersI param = new ParametersI();
		long userId = gateway.getLoggedInUser().getId();
		param.exp(omero.rtypes.rlong(userId));
		
		//Load the orphaned datasets.
		param.orphan();
		
		//Do not load the images.
		param.noLeaves(); //indicate to load the images
		//param.noLeaves(); //no images loaded, this is the default value.
		List<IObject> results = proxy.loadContainerHierarchy(
				Project.class.getName(), new ArrayList<Long>(), param);
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		Iterator<IObject> i = results.iterator();
		ProjectData project;
		DatasetData dataset;
		IObject o;
		long datasetId = -1;
		while (i.hasNext()) {
			o = i.next();
			if (o instanceof Project) {
				project = new ProjectData((Project) o);
				System.err.println("Project:"+project.getId()+" "+
						project.getName());
			} else if (o instanceof Dataset) {
				dataset = new DatasetData((Dataset) o);
				System.err.println("Dataset:"+dataset.getId()+" "+
						dataset.getName());
				if (datasetId < 0) datasetId = dataset.getId();
				//Image not loaded.
			}
		}
		
		//Now load the image for the first orphaned dataset
		if (datasetId < 0) return;
		param = new ParametersI();
		param.exp(omero.rtypes.rlong(userId));
		param.leaves();
		results = proxy.loadContainerHierarchy(
				Dataset.class.getName(), Arrays.asList(datasetId), param);
		i = results.iterator();
		Iterator j;
		while (i.hasNext()) {
			dataset = new DatasetData((Dataset) i.next());
			Set images = dataset.getImages();
			j = images.iterator();
			System.err.println("Size:"+images.size());
			while (j.hasNext()) {
				ImageData image = (ImageData) j.next();
				System.err.println("Image:"+image.getId()+" "+image.getName());
			}
		}
	}

	/**
	 * Connects and invokes the various methods.
	 * 
	 * @param info The configuration information.
	 */
	ReadDataAdvanced(ConfigurationInfo info)
	{
		if (info == null) {
			info = new ConfigurationInfo();
			info.setHostName(hostName);
			info.setPassword(password);
			info.setUserName(userName);
		}
		
		LoginCredentials cred = new LoginCredentials();
        cred.getServer().setHostname(info.getHostName());
        cred.getServer().setPort(info.getPort());
        cred.getUser().setUsername(info.getUserName());
        cred.getUser().setPassword(info.getPassword());

        gateway = new Gateway(new SimpleLogger());
        
		try {
			// First connect.
		    ExperimenterData user = gateway.connect(cred);
            ctx = new SecurityContext(user.getGroupId());
            
			createDatasets();
			createTags();
			loadDatasetsByName();
			loadTagsByNS();
			loadProjectsAndOrphanedDatasets();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
			    gateway.disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs the script without configuration options.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		new ReadDataAdvanced(null);
		System.exit(0);
	}

}

/*
 * org.openmicroscopy.shoola.agents.zoombrowser.DataManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.zoombrowser;


//Java imports
import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
 
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserProjectSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserImageSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.data.ThumbnailRetriever;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.ui.TaskBar;

/**
 * A utility class for managing communications with registry and 
 * retrieving data
 *  
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */

public class DataManager {

	/** The OME Registry */
	protected Registry registry;
	
	/** Cached list of projects. */
	protected List projects=null;
	
	/** cached list of datasets */
	protected List datasets = null;
	
	/** cached list of modules */
	protected List modules = null;

	/** cached list of module categories */
	protected List moduleCategories = null;		

	/** object to grab thumbnails */
	protected ThumbnailRetriever thumbnailRetriever;
	
	public DataManager(Registry registry) {
		this.registry = registry;
		thumbnailRetriever = new ThumbnailRetriever(registry);	
	}
	
	public List getProjects() {
		if (projects == null ||projects.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				projects = 
					dms.retrieveUserProjects(new BrowserProjectSummary(),
												 new BrowserDatasetSummary());
			} catch(DSAccessException dsae) {
				String s = "Can't retrieve user's projects.";
				registry.getLogger().error(this, s+" Error: "+dsae);
				registry.getUserNotifier().notifyError("Data Retrieval Failure",
														s, dsae);	
			} catch(DSOutOfServiceException dsose) {
				ServiceActivationRequest 
				request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
				registry.getEventBus().post(request);
			}
		}
		return projects;
	}
	
	public IconFactory getIconFactory() {
		return ((IconFactory) registry.lookup("/resources/icons/MyFactory"));
	}
	
	public List getDatasets() {
		if (datasets == null ||projects.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				datasets = 
					dms.retrieveUserDatasets(new BrowserDatasetSummary());
				Collections.sort(datasets);
				registry.getLogger().info(this,"loaded datasets...");
			} catch(DSAccessException dsae) {
				String s = "Can't retrieve user's datasets.";
				registry.getLogger().error(this, s+" Error: "+dsae);
				registry.getUserNotifier().notifyError("Data Retrieval Failure",
														s, dsae);	
			} catch(DSOutOfServiceException dsose) {
				ServiceActivationRequest 
				request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
				registry.getEventBus().post(request);
			}
		}
		return datasets;		
	}

	public void getImages(BrowserDatasetSummary dataset) {
		if (dataset.getImages() == null) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				List images = dms.retrieveImages(dataset.getID(),
					new BrowserImageSummary());
				dataset.setImages(images);
			} catch(DSAccessException dsae) {
				String s = "Can't retrieve user's datasets.";
				registry.getLogger().error(this, s+" Error: "+dsae);
				registry.getUserNotifier().notifyError("Data Retrieval Failure",
														s, dsae);	
			} catch(DSOutOfServiceException dsose) {
				ServiceActivationRequest 
				request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
				registry.getEventBus().post(request);
			}
		}
	}

	public List getDatasetsWithImages() {
		List datasets = getDatasets();
		BrowserDatasetSummary d;
	
		Iterator iter = datasets.iterator();
		while (iter.hasNext()) {
			d = (BrowserDatasetSummary) iter.next();
			if (d.getImages() == null) {
				getImages(d);
				// get the Image items for each of these?
				Collection images= d.getImages();
				Iterator iter2 = images.iterator();
				BrowserImageSummary b;
				while (iter2.hasNext()) {
					b = (BrowserImageSummary) iter2.next();
					Image im = thumbnailRetriever.getImage(b);
					b.setThumbnail(thumbnailRetriever.getImage(b));
				}
			}	
		}
		return datasets;
	}
	
	public TaskBar getTaskBar() {
		return registry.getTaskBar();
	}
	
	public List getModules() {
		if (modules == null ||modules.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				modules = 
					dms.retrieveModules();
			} catch(DSAccessException dsae) {
				String s = "Can't retrieve user's modules.";
				registry.getLogger().error(this, s+" Error: "+dsae);
				registry.getUserNotifier().notifyError("Data Retrieval Failure",
														s, dsae);	
			} catch(DSOutOfServiceException dsose) {
				ServiceActivationRequest 
				request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
				registry.getEventBus().post(request);
			}
		}
		return modules;
	}
	
	public List getModuleCategories() {
		if (moduleCategories == null ||moduleCategories.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				moduleCategories = 
					dms.retrieveModuleCategories();
			} catch(DSAccessException dsae) {
				String s = "Can't retrieve user's modules.";
				registry.getLogger().error(this, s+" Error: "+dsae);
				registry.getUserNotifier().notifyError("Data Retrieval Failure",
														s, dsae);	
			} catch(DSOutOfServiceException dsose) {
				ServiceActivationRequest 
				request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
				registry.getEventBus().post(request);
			}
		}
		return moduleCategories;
	}
}

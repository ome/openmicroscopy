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
import java.util.HashMap;
import java.util.Iterator;
 
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserProjectSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserDatasetData;
import org.openmicroscopy.shoola.agents.zoombrowser.data.BrowserImageSummary;
import org.openmicroscopy.shoola.agents.zoombrowser.data.ThumbnailRetriever;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
//import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.ui.TaskBar;
//import org.openmicroscopy.ds.st.CategoryGroup;
//import org.openmicroscopy.ds.st.Category;

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
	
	/** HashMap of projects */
	protected HashMap projectHash=null;
	
	/** are we loading projects? */
	private boolean loadingProjects =false;
		
	/** HashMap of datasets */
	protected HashMap datasetHash=null;
	
	/** are we loading datasets */
	private boolean loadingDatasets = false;
	
	/** have we loaded datasets and projects*/
	private boolean datasetsLoaded= false;
	private boolean projectsLoaded = false;
	/** hash map of modules */
	protected HashMap moduleHash = null;

	/** are we loading modules? */
	protected boolean loadingModules = false;
	
	/** cached hash of module categories */
	protected HashMap moduleCategoryHash = null;		

	/** are we loading modules categories? */
	protected boolean loadingModuleCategories = false;
	
	/** object to grab thumbnails */
	protected ThumbnailRetriever thumbnailRetriever;
	
	public DataManager(Registry registry) {
		this.registry = registry;
		thumbnailRetriever = new ThumbnailRetriever(registry);	
	}
	
	public Registry getRegistry() {
		return registry;
	}
	
	public synchronized Collection getProjects() {
		
		// if we've done it, .grab it...
		if (projectsLoaded == true) {
			if (projectHash != null && projectHash.size() > 0) {
				return projectHash.values();
			}
			return null;
		}
		
		if (loadingProjects == false) {
			loadingProjects = true;
			retrieveProjects();
			loadingProjects = false;
			Collection res = null;
			if (projectHash != null)
				res= projectHash.values();
			notifyAll();
			return res;
		}
		else {// in progress
			try{ 
				wait();
				if (projectHash != null && projectHash.size() > 0)
					return projectHash.values();
				else 
					return null;
			}
			catch (InterruptedException e) {
				return null;
			}
		}
	}
	
	/**
	 * Do the dirty work of retrieving projects. It's a shame to repeat all of this
	 * code for error handling, but there's no good way around it. At least none that
	 * I've been able to think of.
	 *
	 */
	protected synchronized void retrieveProjects() {
		if (projectHash == null || projectHash.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				Collection projects = 
					dms.retrieveUserProjectsWithDatasetData(
							new BrowserProjectSummary(),new BrowserDatasetData());
				projectHash = buildProjectHash(projects);
				projectsLoaded = true;
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
	}
		
	private HashMap buildProjectHash(Collection projects) {
		HashMap map = new HashMap();
		Iterator iter = projects.iterator();
		while (iter.hasNext()) {
			BrowserProjectSummary p = (BrowserProjectSummary) iter.next();
			Integer id = new Integer(p.getID());
			map.put(id,p);
		}
		return map;
	}
	
	public BrowserProjectSummary getProject(int id) {
		Integer ID = new Integer(id);
		if (projectHash == null)
			getProjects();
		return (BrowserProjectSummary) projectHash.get(ID);
	}
	
	public IconFactory getIconFactory() {
		return ((IconFactory) registry.lookup("/resources/icons/MyFactory"));
	}
	
	public synchronized Collection getDatasets() {
		
		// if we're done, go for it.
		if (datasetsLoaded == true) {
			if (datasetHash != null && datasetHash.size() > 0)  {
				return datasetHash.values();
			}
			else 
				return null;
		}
		
		if (loadingDatasets == false) {
			loadingDatasets = true;
			retrieveDatasets();
			notifyAll();
			loadingDatasets = false;
			return datasetHash.values();
		}
		else {// in progress
			try{ 
				wait();
				if (datasetHash != null && datasetHash.size()> 0)
					return datasetHash.values();
				return null;
			}
			catch (InterruptedException e) {
				return null;
			}
		}	
	}
	protected synchronized void retrieveDatasets() {
		if (datasetHash == null ||datasetHash.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				Collection datasets = 
					dms.retrieveUserDatasets(new BrowserDatasetData(),
							new BrowserImageSummary());
				getDatasetsWithImages(datasets);
				datasetHash = buildDatasetHash(datasets);
				datasetsLoaded = true;
				//Collections.sort(datasets);
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
	}
	
	/**
	 * Build a hashed list of all of the datasets. This code gets a bit repetitive,
	 * but it's not easy to make generic, because  the OME data objects don't
	 * _all_ have IDs. As a result, we can't cast the objects in the list to all 
	 * have IDs.
	 * @param datasets
	 * @return
	 */
	private HashMap buildDatasetHash(Collection datasets) {
		HashMap map = new HashMap();
		Iterator iter = datasets.iterator();
		while (iter.hasNext()) {
			BrowserDatasetData p = (BrowserDatasetData) iter.next();
			Integer id = new Integer(p.getID());
			map.put(id,p);
		}
		return map;
	}
	
	/**
	 * Retrieve that dataset object from the hash by ID. Also hard to make generic:
	 * we have to retrieve all of the datasets if they have not yet been retrieved.
	 * But, what call to make? The wrapper required to make this completely generic
	 * would probably be longer than the procedure.
	 * @param id
	 * @return
	 */
	public BrowserDatasetData getDataset(int id) {
		
		if (datasetHash == null)
			getDatasets();
		Integer ID = new Integer(id);
		return (BrowserDatasetData) datasetHash.get(ID);
	}

	private Collection getDatasetsWithImages(Collection ds) {
		BrowserDatasetData d;
	  	Iterator iter = ds.iterator();
	  	
		while (iter.hasNext()) {
			d = (BrowserDatasetData) iter.next();
			Collection images= d.getImages();
			if (images == null || images.size() == 0)
				continue;
			Iterator iter2 = images.iterator();
			BrowserImageSummary b;
			while (iter2.hasNext()) {
				b = (BrowserImageSummary) iter2.next();
				Image im = thumbnailRetriever.getImage(b);
				b.setThumbnail(im);
			}
		}
		return ds;
	}
	
	
	public TaskBar getTaskBar() {
		return registry.getTaskBar();
	}
	
	public synchronized Collection getModules() {
		
		// if we're done, go for it.
		
		if (moduleHash != null && moduleHash.size() > 0)
			return moduleHash.values();
		
		if (loadingModules == false) {
			loadingModules = true;
			retrieveModules();
		//	retrieveCategories();
			loadingModules = false;
			notifyAll();
			return moduleHash.values();
		}
		else {// in progress
			try{ 
				wait();
				return moduleHash.values();
			}
			catch (InterruptedException e) {
				return null;
			}
		}
	}
	
	/*protected synchronized void retrieveCategories() {
		try { 
			SemanticTypesService sts = registry.getSemanticTypesService();
			CategoryGroup cg = (CategoryGroup) sts.getCategoryGroup(45978);
			if (cg == null) {
				System.err.println("no category found ");
			}	
			else {
				System.err.println("found category group "+cg.getName());
				Collection categories = cg.getCategoryList();
				Iterator iter = categories.iterator();
				while (iter.hasNext()) {
					Category c = (Category) iter.next();
					System.err.println("category... "+c.getName());
				}
			}
			
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
	}*/
	
	protected synchronized void retrieveModules() {
		if (moduleHash == null ||moduleHash.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				Collection modules = 
					dms.retrieveModules();
				moduleHash = buildModuleHash(modules);
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
	}
	
	protected HashMap buildModuleHash(Collection modules) {
		HashMap map = new HashMap();
		Iterator iter = modules.iterator();
		while (iter.hasNext()) {
			ModuleData p = (ModuleData) iter.next();
			Integer id = new Integer(p.getID());
			map.put(id,p);
		}
		return map;
	}
	
	public ModuleData getModule(int id) {
		Integer ID = new Integer(id);
		if (moduleHash == null)
			getModules();
		return (ModuleData) moduleHash.get(ID);
	}
	
	public synchronized Collection getModuleCategories() {
		
		// if we're done, go for it.
		
		if (moduleCategoryHash != null && moduleCategoryHash.size() > 0)
			return moduleCategoryHash.values();
		
		if (loadingModuleCategories == false) {
			loadingModuleCategories = true;
			retrieveModuleCategories();
			loadingModuleCategories = false;
			notifyAll();
			return moduleCategoryHash.values();
		}
		else {// in progress
			try{ 
				wait();
				return moduleCategoryHash.values();
			}
			catch (InterruptedException e) {
				return null;
			}
		}
	}
	
	public synchronized void retrieveModuleCategories() {
		if (moduleCategoryHash== null ||moduleCategoryHash.size() == 0) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				Collection moduleCategories = 
					dms.retrieveModuleCategories();
				moduleCategoryHash = buildModuleCategoryHash(moduleCategories); 
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
	}
	
	protected HashMap buildModuleCategoryHash(Collection moduleCategories) {
		HashMap map = new HashMap();
		Iterator iter = moduleCategories.iterator();
		while (iter.hasNext()) {
			ModuleCategoryData p = (ModuleCategoryData) iter.next();
			Integer id = new Integer(p.getID());
			map.put(id,p);
		}
		return map;
	}
	
	public ModuleCategoryData getModuleCategory(int id) {
		Integer ID = new Integer(id);
		if (moduleCategoryHash == null)
			getModuleCategories();
		return (ModuleCategoryData) moduleHash.get(ID);
	}
}

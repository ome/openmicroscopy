/*
 * org.openmicroscopy.shoola.agents.datamng.DataManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.agents.datamng;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.LoadDataset;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.ui.TopFrame;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * The data manager agent.
 * This agent manages and presents user's data.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DataManager
	implements Agent
{
	
	public static final Color   	STEELBLUE = new Color(0x4682B4);

	/** Width of the editor dialog window. */
	public static final int			EDITOR_WIDTH = 300;
	
	/** Height of the editor dialog window. */
	public static final int			EDITOR_HEIGHT = 300;
	
	public static final Dimension	DIM_SCROLL_TABLE = new Dimension(40, 60);
	
	public static final Dimension	DIM_SCROLL_NAME = new Dimension(40, 25);
	
	public static final int			ROW_TABLE_HEIGHT = 60;
	
	public static final int			ROW_NAME_FIELD = 25;
	
	/** Reference to the registry. */
	private Registry				registry;
	
	/** Reference to the GUI. */
	private DataManagerUIF			presentation;
	
	/** Reference to the control component. */
	private DataManagerCtrl     	control;
	
	/** Reference to the topFrame. */
	private TopFrame				topFrame;
	
	private JCheckBoxMenuItem		viewItem;
	
	/** 
	 * All user's projects. 
	 * That represents all user's data rooted by project objects. 
	 * That is, the whole project-dataset hierarchy for the current user.
	 */
	private List           			projectSummaries;

	/** All user's datasets. */
	private List					datasetSummaries;

	/** 
	 * Creates a new instance.
	 */
	public DataManager() {}
    
	/** Implemented as specified by {@link Agent}. */
	public void activate()
	{	
		topFrame.addToDesktop(presentation, TopFrame.PALETTE_LAYER);
		presentation.setVisible(true);
	}
	
	/** Implemented as specified by {@link Agent}. */
	public void terminate()
	{
	}
	
	/** Implemented as specified by {@link Agent}. */
	public void setContext(Registry ctx)
	{
		registry = ctx;
		control = new DataManagerCtrl(this);
		presentation = new DataManagerUIF(control, registry);
		control.attachListener();
		topFrame = registry.getTopFrame();
		viewItem = getViewMenuItem();
		topFrame.addToMenu(TopFrame.VIEW, viewItem);
	}
	
	/** Implemented as specified by {@link Agent}. */
	public boolean canTerminate()
	{
		return true;
	}
    
    public DataManagerUIF getPresentation()
    {
    	return presentation;
    }
	
	/**
	 * Returns all projects which belong to the current user.
	 * Each project is linked to its datasets.
	 * <p>If an error occurs while trying to retrieve the user's data from 
	 * OMEDS, the user gets notified and this method returns <code>null</code>.
	 * </p>
	 *
	 * @return  A list of project summary objects. 
	 */
	List getUserProjects()
	{
		if (projectSummaries == null) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				projectSummaries = dms.retrieveUserProjects();
			} catch(DSAccessException dsae) {
				UserNotifier un = registry.getUserNotifier();
				un.notifyError("Data Retrieval Failure", 
					"Unable to retrieve user's projects", dsae);
			} catch(DSOutOfServiceException dsose) {
				// pop up login window
				throw new RuntimeException(dsose);
			} 
		}
		
		return projectSummaries;
	}
	
	/**
	 * Returns all datasets which belong to the current user.
	 * <p>If an error occurs while trying to retrieve the user's data from 
	 * OMEDS, the user gets notified and this method returns <code>null</code>.
	 * </p>
	 *
	 * @return  A list of dataset summary objects. 
	 */
	List getUserDatasets()
	{
		if (datasetSummaries == null) {
			try { 
				DataManagementService dms = registry.getDataManagementService();
				datasetSummaries = dms.retrieveUserDatasets();
			} catch(DSAccessException dsae) {
				UserNotifier un = registry.getUserNotifier();
				un.notifyError("Data Retrieval Failure", 
					"Unable to retrieve user's datasets", dsae);
			} catch(DSOutOfServiceException dsose) {	
				// pop up login window
				throw new RuntimeException(dsose);
			} 
		}
		
		return datasetSummaries;
	}
	
	/**
	 * Returns all images which belong to the current user.
	 * <p>If an error occurs while trying to retrieve the user's data from 
	 * OMEDS, the user gets notified and this method returns <code>null</code>.
	 * </p>
	 *
	 * @return  A list of image summary objects. 
	 */
	List getUserImages()
	{
		List images = null;
		try { 
			DataManagementService dms = registry.getDataManagementService();
			images = dms.retrieveUserImages();
		} catch(DSAccessException dsae) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Data Retrieval Failure", 
				"Unable to retrieve user's images", dsae);
		} catch(DSOutOfServiceException dsose) {	
			// pop up login window
			throw new RuntimeException(dsose);
		} 
		
		return images;
	}

	/**
	 * Retrieve a list of images in the specified dataset.
	 * 
	 * @param datasetID		Specified dataset id.
	 * @return list of image summary objects.
	 */
	List getImages(int datasetID) 
	{
		List images = null;
		try { 
			DataManagementService dms = registry.getDataManagementService();
			images = dms.retrieveImages(datasetID);
		} catch(DSAccessException dsae) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Data Retrieval Failure", 
				"Unable to retrieve images for the dataset "+datasetID, dsae);
		} catch(DSOutOfServiceException dsose) {	
			// pop up login window
			throw new RuntimeException(dsose);
		}
		
		return images;	
	}
	
	/**
	 * Retrieve a specified project.
	 * 
	 * @param projectID	Specified project id.
	 * @return projectData object.
	 */
	ProjectData getProject(int projectID)
	{
		ProjectData project = null;
		try { 
			DataManagementService dms = registry.getDataManagementService();
			project = dms.retrieveProject(projectID);
		} catch(DSAccessException dsae) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Data Retrieval Failure", 
				"Unable to retrieve the project "+projectID, dsae);
		} catch(DSOutOfServiceException dsose) {	
			// pop up login window
			throw new RuntimeException(dsose);
		} 
		
		return project;
	}
	
	/**
	 * Retrieve a specified dataset.
	 * 
	 * @param datasetID		Specified dataset id.
	 * @return projectData object.
	 */
	DatasetData getDataset(int datasetID)
	{
		DatasetData dataset = null;
		try { 
			DataManagementService dms = registry.getDataManagementService();
			dataset = dms.retrieveDataset(datasetID);
		} catch(DSAccessException dsae) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Data Retrieval Failure", 
				"Unable to retrieve the dataset "+datasetID, dsae);
		} catch(DSOutOfServiceException dsose) {	
			// pop up login window
			throw new RuntimeException(dsose);
		} 
		
		return dataset;
	}
	
	/**
	 * Retrieve a specified image.
	 * 
	 * @param imageID		Specified image id.
	 * @return imageData object.
	 */
	ImageData getImage(int imageID)
	{
		ImageData image = null;
		try { 
			DataManagementService dms = registry.getDataManagementService();
			image = dms.retrieveImage(imageID);
		} catch(DSAccessException dsae) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Data Retrieval Failure", 
				"Unable to retrieve the image "+imageID, dsae);
		} catch(DSOutOfServiceException dsose) {	
			// pop up login window
			throw new RuntimeException(dsose);
		} 
		
		return image;
	}
	
	/**
	 * Create a new project.
	 * 
	 * @param pd		project data object.
	 */
	void createProject(ProjectData pd)
	{
		ProjectSummary project;
		try { 
			DataManagementService dms = registry.getDataManagementService();
			project = dms.createProject(pd);
			if (projectSummaries == null) projectSummaries = new ArrayList();
			projectSummaries.add(project);
			// forward event to the presentation.
			presentation.addNewProjectToTree(project);
		} catch(DSAccessException dsae) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Data Retrieval Failure", 
				"Unable to create a project: "+pd.getName(), dsae);
		} catch(DSOutOfServiceException dsose) {	
			// pop up login window
			throw new RuntimeException(dsose);
		} 
	}
	
	/**
	 * Create a new dataset.
	 * 
	 * @param projects	list of project summaries, projects to which 
	 * 					the dataset will be added.
	 * @param images	list of image summaries, images in the new dataset.
	 * @param dd		dataset data object.
	 */
	void createDataset(List projects, List images, DatasetData dd)
	{
		DatasetSummary dataset;
		try { 
			DataManagementService dms = registry.getDataManagementService();
			dataset = dms.createDataset(projects, images, dd);
			if (datasetSummaries == null) datasetSummaries = new ArrayList();
			datasetSummaries.add(dataset);
			ProjectSummary ps;
			for (int i = 0; i < projects.size(); i++) {
				ps = (ProjectSummary) projects.get(i);
				ps.getDatasets().add(dataset);	
			}
			// forward event to the presentation.
			if (projects != null) 
				presentation.addNewDatasetToTree(projects);
				
		} catch(DSAccessException dsae) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Data Retrieval Failure", 
				"Unable to create a dataset: "+dd.getName(), dsae);
		} catch(DSOutOfServiceException dsose) {	
			// pop up login window
			throw new RuntimeException(dsose);
		} 
		
	}
	
	/**
	 * Update a specified project.
	 * 
	 * @param pd	Project data object.
	 */
	void updateProject(ProjectData pd, boolean nameChange)
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			dms.updateProject(pd);
			//update the presentation and the project summary contained in the 
			//projectSummaries list accordingly.
			updatePSList(pd);
			if (nameChange) presentation.updateProjectInTree();
		} catch(DSAccessException dsae) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Data Retrieval Failure", 
				"Unable to update the specified project: "+pd.getID(), dsae);
		} catch(DSOutOfServiceException dsose) {	
			// pop up login window
			throw new RuntimeException(dsose);
		} 	
	}
	
	/** 
	 * Update the corresponding project summary object contained in the 
	 * projectSummaries list. The method is called when a project has been 
	 * updated.
	 * 
	 * @param pd	Modified project data object.
	 */
	private void updatePSList(ProjectData pd)
	{
		Iterator i = projectSummaries.iterator();
		ProjectSummary ps;
		while (i.hasNext()) {
			ps = (ProjectSummary) i.next();
			if (ps.getID() ==  pd.getID()) {
				ps.setName(pd.getName());
				ps.setDatasets(pd.getDatasets());
				break;
			}	
		}
	}
	
	/**
	 * Update a specified dataset.
	 * 
	 * @param dd	Dataset data object.
	 */
	void updateDataset(DatasetData dd, boolean nameChange)
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			dms.updateDataset(dd);
			//update the presentation and the dataset summary contained in the 
			//datasetSummaries list accordingly.
			if (datasetSummaries != null) updateDSList(dd);
			updateDatasetInPS(dd);
			if (nameChange) presentation.updateDatasetInTree();
		} catch(DSAccessException dsae) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Data Retrieval Failure", 
				"Unable to update the specified dataset: "+dd.getID(), dsae);
		} catch(DSOutOfServiceException dsose) {	
			// pop up login window
			throw new RuntimeException(dsose);
		} 
	}
	
	/** 
	 * Update datasetSummary object in projectSummaries.
	 * Method called when a dataset has been updated.
	 * 
	 * @param dd	Modified dataset data object.
	 */
	private void updateDatasetInPS(DatasetData dd)
	{
		Iterator i = projectSummaries.iterator();
		ProjectSummary ps;
		Iterator j;
		DatasetSummary ds;
		while (i.hasNext()) {
			ps = (ProjectSummary) i.next();
			j = ps.getDatasets().iterator();
			while (j.hasNext()) {
				ds = (DatasetSummary) j.next();
				if (ds.getID() == dd.getID()) {
					ds.setName(dd.getName());
					break;
				}
					
			}
		}
	}
	
	/** 
	 * Update the corresponding dataset summary object contained in the 
	 * datasetSummaries list. The method is called when a dataset has been 
	 * updated.
	 * @param dd	Modified dataset data object.
	 * 
	 */
	private void updateDSList(DatasetData dd)
	{
		Iterator i = datasetSummaries.iterator();
		DatasetSummary ds;
		while (i.hasNext()) {
			ds = (DatasetSummary) i.next();
			if (ds.getID() ==  dd.getID()) {
				ds.setName(dd.getName());
				break;
			}	
		}
	}
	
	/**
	 * Update a specified image.
	 * 
	 * @param dd	Image data object.
	 */
	void updateImage(ImageData id, boolean nameChange)
	{
		try { 
			DataManagementService dms = registry.getDataManagementService();
			dms.updateImage(id);
			if (nameChange) presentation.updateImageInTree(id);
		} catch(DSAccessException dsae) {
			UserNotifier un = registry.getUserNotifier();
			un.notifyError("Data Retrieval Failure", 
				"Unable to update the specified image: "+id.getID(), dsae);
		} catch(DSOutOfServiceException dsose) {	
			// pop up login window
			throw new RuntimeException(dsose);
		} 	
	}

	/**
	 * Posts a request to view all images in the specified dataset.
	 * 
	 * @param datasetID		The id of the dataset.
	 */
	void viewDataset(int datasetID)
	{
		LoadDataset request = new LoadDataset(datasetID);
		EventBus bus = registry.getEventBus();
		bus.post(request);	
	}

	/**
	 * Posts a request to view the given pixels set within the image.
	 * 
	 * @param imageID	The id of the image containing the pixels set.
	 * @param pixelsID	The id of the pixels set.
	 */
	void viewImage(int imageID, int pixelsID)
	{
		LoadImage request = new LoadImage(imageID, pixelsID);
		EventBus bus = registry.getEventBus();
		bus.post(request);	
	}
	
	/** Display the widget. */
	void showPresentation()
	{
		topFrame.removeFromDesktop(presentation);
		topFrame.addToDesktop(presentation, TopFrame.PALETTE_LAYER);
		try {
			presentation.setClosed(false);
		} catch (Exception e) {}
		presentation.setVisible(true);	
	}
	
	/** Pop up the presentation. */
	void deiconifyPresentation()
	{
		topFrame.deiconifyFrame(presentation);
		try {
			presentation.setIcon(false);
		} catch (Exception e) {}	
	}
	
	/** Select the menuItem. */
	void setMenuSelection(boolean b)
	{
		viewItem.setSelected(b); 
	}
	
	/** 
	 * Menu item to add into the 
	 * {@link org.openmicroscopy.shoola.env.ui.TopFrame} menu bar. */
	private JCheckBoxMenuItem getViewMenuItem()
	{
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("DataManager");
		control.setMenuItemListener(menuItem, DataManagerCtrl.DM_VISIBLE);
		return menuItem;
	}
	
}

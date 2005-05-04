/*
 * org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl
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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.editors.category.CategoryEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.category.CreateCategoryEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup.CreateGroupEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup.GroupEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.dataset.CreateDatasetEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.dataset.DatasetEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.image.ImageEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.image.ImportImageSelector;
import org.openmicroscopy.shoola.agents.datamng.editors.project.CreateProjectEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.project.ProjectEditor;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Agent's control.
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
public class DataManagerCtrl
	implements ActionListener
{
	
	/** ID used to handle the createProject event. */
	static final int			PROJECT_ITEM = 0;
	
	/** ID used to handle the createDataset event. */
	static final int			DATASET_ITEM = 1;
	
	/** ID used to handle the importImages event. */
	static final int			IMAGE_ITEM = 2;
    
    /** ID used to handle the create CategoryGroup event. */
    static final int            CREATE_GROUP = 3;
    
    static final int            CREATE_CATEGORY = 4;

    /** ID to control in which tabbedPane the property sheet is displayed. */
    public static final int     FOR_HIERARCHY = 300, FOR_CLASSIFICATION = 301,
                                FOR_IMAGES = 302;
     
	private DataManager			abstraction;
	
    private int                 selectedCategoryGroupID;
    
	DataManagerCtrl(DataManager	abstraction)
	{
		this.abstraction = abstraction;
	}
	
	public Registry getRegistry() { return abstraction.getRegistry(); }
	
	/** Return the UIF of this agent. */
	public DataManagerUIF getReferenceFrame()
	{
		return abstraction.getPresentation();
	}
	
    /**Handles event fired by menu. */
    public void actionPerformed(ActionEvent e)
    {
        try {
            int index = Integer.parseInt(e.getActionCommand());
            switch (index) { 
                case PROJECT_ITEM:
                    createProject(); break;
                case DATASET_ITEM:
                    createDataset(); break; 
                case IMAGE_ITEM:
                    showImagesImporter();  break;
                case CREATE_GROUP:
                    createGroup(); break;
                case CREATE_CATEGORY:
                    createCategory();       
            }
        } catch(NumberFormatException nfe) {  
            throw new Error("Invalid Action ID "+e.getActionCommand(), nfe);
        } 
    }
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void importImages(List imagesToImport, int datasetID)
	{
		abstraction.importImages(imagesToImport, datasetID);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public List getDatasetsDiff(ProjectData data)
	{
		return abstraction.getDatasetsDiff(data);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public List getImagesDiff(DatasetData data, Map filters, Map complexFilters)
	{
		return abstraction.getImagesDiff(data, filters, complexFilters);
	}
	
    /** Forward event to the {@link DataManager abstraction}. */
    public List getImagesInUserDatasetsDiff(DatasetData data, List datasets, 
                Map filters, Map complexFilters)
    {
        return abstraction.getImagesInUserDatasetsDiff(data, datasets, 
                        filters, complexFilters);
    }
    
    /** Forward event to the {@link DataManager abstraction}. */
    public List getImagesInUserGroupDiff(DatasetData data, Map filters, 
                                        Map complexFilters)
    {
        return abstraction.getImagesInUserGroupDiff(data, filters, 
                                                    complexFilters);
    }
    
    /** Forward event to the {@link DataManager abstraction}. */
    public List getImagesInSystemDiff(DatasetData data, Map filters, 
                                        Map complexFilters)
    {
        return abstraction.getImagesInSystemDiff(data, filters, complexFilters);
    }
    
	/** Forward event to the {@link DataManager abstraction}. */
	public void createProject(ProjectData pd)
	{
		abstraction.createProject(pd);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void addDataset(List projects, List images, DatasetData dd)
	{
		abstraction.createDataset(projects, images, dd);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void updateProject(ProjectData pd, List toRemove, List toAdd,
							 boolean nameChange)
	{
		abstraction.updateProject(pd, toRemove, toAdd, nameChange);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void updateDataset(DatasetData dd, List toRemove, List toAdd, 
								boolean nameChange)
	{
		abstraction.updateDataset(dd, toRemove, toAdd, nameChange);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void updateImage(ImageData id, boolean nameChange)
	{
		abstraction.updateImage(id, nameChange);
	}
    
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public List getImportedImages(Map filters, Map complexFilters)
    { 
        try {
            return abstraction.getImportedImages(filters, complexFilters);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);
        }
        return new ArrayList();
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public List getImagesInDatasets(List datasets, Map filters, 
                                    Map complexFilters)
    { 
        try {
            return abstraction.getImagesInDatasets(datasets, filters, 
                    complexFilters);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);
        }
        return new ArrayList();
    }
    
    /** 
     * Retrieve the list of images contained the specified datasets,
     * then forward to the specified widget manager.  
     * @param datasets
     * @param mng
     */
    public List loadImagesInDatasets(List datasets, int index, DataObject data, 
            Map filters, Map complexFilters)
    {
        if (datasets == null || datasets.size() == 0) return null;
        List images = null;
        switch(index) {
            case FOR_HIERARCHY:
                if (data != null && data instanceof DatasetData)
                    images = getImagesInUserDatasetsDiff((DatasetData) data, 
                            datasets, filters, complexFilters);
                else 
                    images = getImagesInDatasets(datasets, filters, 
                                                    complexFilters); 
                break;
            case FOR_CLASSIFICATION:
                if (data != null && data instanceof CategoryData)
                    images =  getImagesDiffInUserDatasetsNotInCategoryGroup(
                                (CategoryData) data, datasets, filters, 
                                complexFilters);
                else if (data != null && data instanceof CategoryGroupData)
                    images =  getImagesInUserDatasetsNotInCategoryGroup(
                            (CategoryGroupData) data, datasets, filters, 
                            complexFilters);
                break;
        }
        return images;
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public List getGroupImages(Map filters, Map complexFilters)
    { 
        try {
            return abstraction.getGroupImages(filters, complexFilters);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);
        }
        return new ArrayList();
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public List getSystemImages(Map filters, Map complexFilters)
    { 
        try {
            return abstraction.getSystemImages(filters, complexFilters);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);
        }
        return new ArrayList();
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public List getImages(int datasetID)
    {
        try {
            return abstraction.getImages(datasetID);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the images within " +
                    "the specified dataset: "+datasetID;
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);
        }
        return new ArrayList();
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public void viewImage(DataObject data)
    {
        if (data instanceof ImageSummary) {
            ImageSummary is = (ImageSummary) data;
            int[] pxSets = is.getPixelsIDs();
            //TODO: select pixels if more than one!
            abstraction.viewImage(is.getID(), pxSets[0], is.getName());
        } else if (data instanceof ImageData) {
            ImageData idata = (ImageData) data;
            int[] pxSets = idata.getPixelsIDs();
            //TODO: select pixels if more than one!
            abstraction.viewImage(idata.getID(), pxSets[0], idata.getName());
        }
        
    }
    
    /** Return the abstraction. */
    DataManager getAbstraction() {return abstraction; }
    
    /** 
     * Brings up a suitable property sheet dialog for the specified
     * <code>target</code>.
     *
     * @param   target      A project, dataset or image. 
     *                      If you pass anything different this method does
     *                      nothing.
     * @param   index       one of the constant FOR_CLASSIFICATION, 
     *                      FOR_HIERARCHY.
     */
    void showProperties(DataObject target, int index)
    {
        Registry registry = abstraction.getRegistry();
        if (target == null)    return;
        try {
            if (target instanceof ProjectSummary) {
                ProjectData project = abstraction.getProject(
                                        ((ProjectSummary) target).getID());
                showComponent(new ProjectEditor(this, project), index);    
            } else if (target instanceof DatasetSummary) {
                DatasetData dataset = abstraction.getDataset(
                                        ((DatasetSummary) target).getID());
                showComponent(new DatasetEditor(this, dataset), index);
            } else if (target instanceof ImageSummary) {
                ImageData image = abstraction.getImage(
                                        ((ImageSummary) target).getID());
                showComponent(new ImageEditor(this, image, 
                        abstraction.getThumbnail(image)), index);
            } else if (target instanceof CategoryGroupData) {
                showComponent(new GroupEditor(this, (CategoryGroupData) target),
                                                index);
            } else if (target instanceof CategoryData) {
                showComponent(new CategoryEditor(this, (CategoryData) target), 
                        index);
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the specified target.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s, 
                                                    dsae);
        } 
    }
    
    /** Display the propertySheet in a JDialog when an event is posted. */
    void showProperties(DataObject target, Component parent)
    {
        Registry registry = abstraction.getRegistry();
        if (target == null)    return;
        try {
            if (target instanceof ProjectSummary) {
                ProjectData project = abstraction.getProject(
                                        ((ProjectSummary) target).getID());
                showComponent(new ProjectEditor(this, project), FOR_HIERARCHY, 
                                parent);  
            } else if (target instanceof DatasetSummary) {
                DatasetData dataset = abstraction.getDataset(
                                        ((DatasetSummary) target).getID());                                         
                showComponent(new DatasetEditor(this, dataset), FOR_HIERARCHY,
                                parent);
            } else if (target instanceof ImageSummary) {
                ImageData image = abstraction.getImage(
                                        ((ImageSummary) target).getID());
                //Retrieve the thumbnail 
                showComponent(new ImageEditor(this, image, 
                            abstraction.getThumbnail(image)), FOR_HIERARCHY, 
                                parent);
            } else if (target instanceof CategoryGroupData) {
                showComponent(new GroupEditor(this, (CategoryGroupData) target),
                                                FOR_CLASSIFICATION, parent);
            } else if (target instanceof CategoryData) {
                showComponent(new CategoryEditor(this, (CategoryData) target), 
                                            FOR_CLASSIFICATION, parent);
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the specified target.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s, 
                                                    dsae);
        } 
    }
    
    /** Display the specified component in one of the tabbedPane. */
    void showComponent(JComponent c, int index, Component parent)
    {
        DataManagerUIF presentation = getReferenceFrame();
        switch (index) {
            case FOR_HIERARCHY:
                if (presentation != null) {
                    if (c != null) presentation.addComponentToHierarchy(c);
                    else presentation.removeComponentFromHierarchy();
                    presentation.removeComponentFromClassification();
                    presentation.removeComponentFromImages();
                    presentation.setSelectedPane(FOR_HIERARCHY);
                } else {
                    if (parent == null) 
                        parent = getRegistry().getTaskBar().getFrame();
                    UIUtilities.makeForDialog(parent, "Editor", c);
                }
                break;
            case FOR_CLASSIFICATION:
                if (presentation != null) {
                    if (c != null) presentation.addComponentToClassification(c);
                    else presentation.removeComponentFromClassification();
                    presentation.removeComponentFromHierarchy();
                    presentation.removeComponentFromImages();
                    presentation.setSelectedPane(FOR_CLASSIFICATION);
                } else UIUtilities.makeForDialog(parent, "Editor", c);
                break;
            case FOR_IMAGES:
                if (presentation != null) {
                    if (c != null) presentation.addComponentToImages(c);
                    else presentation.removeComponentFromImages();
                    presentation.removeComponentFromHierarchy();
                    presentation.removeComponentFromClassification();
                    presentation.setSelectedPane(FOR_IMAGES);
                } else UIUtilities.makeForDialog(parent, "Editor", c);   
        }
    }
    
    void showComponent(JComponent c, int index)
    {
        showComponent(c, index, null);
    }

    /** Refresh project, dataset. */
    void refresh(DataObject target)
    {
        if (target == null)    return;  //shouldn't happen
        if (target instanceof ProjectSummary) 
            abstraction.refresh();
        else if (target instanceof DatasetSummary) 
            abstraction.refresh((DatasetSummary) target);
        else if (target instanceof CategoryGroupData)
            abstraction.refreshCategoryGroups();
        else if (target instanceof CategoryData)
            abstraction.refreshCategory((CategoryData) target);
    }
    
    void refresh(int index)
    {
        if (index == FOR_HIERARCHY) 
            abstraction.refresh();  
        else if (index == FOR_CLASSIFICATION)  
            abstraction.refreshCategoryGroups();
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    void updateImage(ImageSummary is)
    {
        abstraction.updateImage(is);
    }
    
    
    /** Forward the call to the {@link DataManager abstraction}. */
    List getUserProjects()
    { 
        try {
            return abstraction.getUserProjects(); 
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's projects.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);
        }
        return null;
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public List getUsedDatasets()
    { 
        try {
            return abstraction.getUsedDatasets(); 
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's datasets.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);
        }
        return null;
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public List getUserDatasets()
    { 
        try {
            return abstraction.getUserDatasets(); 
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's datasets.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);
        }
        return null;
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public void browseDataset(DataObject data)
    { 
        abstraction.browseDataset(data);
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public void browseProject(DataObject data)
    { 
        abstraction.browseProject(data);
    }

    /** Forward to the {@link DataManager abstraction}. */
    public void browseCategoryGroup(CategoryGroupData data)
    {
       abstraction.browseCategoryGroup(data);
    }

    /** Forward to the {@link DataManager abstraction}. */
    public void browseCategory(CategoryData data)
    {
        abstraction.browseCategory(data); 
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    void browseRoot()
    { 
        abstraction.browseRoot();
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    void annotate(DataObject target) { abstraction.annotate(target); }
    
    /** Bring up the corresponding editor. */
    void createProject()
    {
        try {
            showComponent(new CreateProjectEditor(this, new ProjectData(), 
                    abstraction.getUserDatasets()), FOR_HIERARCHY);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's datasets.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);
        }
    }

    /** Bring up the corresponding editor. */
    void createDataset()
    {   
        try {
            showComponent(new CreateDatasetEditor(this, new DatasetData(), 
                              abstraction.getUserProjects()), FOR_HIERARCHY);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's datasets.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
    }
    
    /** Bring up the Images Importer file chooser */
    void showImagesImporter(DatasetSummary ds)
    {
        List datasets = new ArrayList();
        datasets.add(ds);
        UIUtilities.centerAndShow(new ImportImageSelector(this, datasets));
    }
    
    /** Bring up the Images Importer file chooser */
    void showImagesImporter()
    {
        try {
            List datasets = abstraction.getUserDatasets();
            if (datasets.size() == 0) {
                UserNotifier un = abstraction.getRegistry().getUserNotifier();
                un.notifyInfo("Import images", "Create a dataset first.");
            } else 
                UIUtilities.centerAndShow(new ImportImageSelector(this, 
                        datasets));
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's projects or images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
    }
    
    //Category manager
    /** 
     * Set the ID of the selected categoryGroup. 
     * This is a workaround to help the user when he/she creates a new 
     * category.
     */
    void setSelectedCategoryGroup(int categoryGroupID)
    {
        selectedCategoryGroupID = categoryGroupID;
    }

    /** Create a new Category. */
    void createCategory()
    {
        try {
            List groups = abstraction.getCategoryGroups();
            if (groups == null || groups.size() == 0) {
                UserNotifier un = abstraction.getRegistry().getUserNotifier();
                un.notifyInfo("Create a category", 
                        "You must create a group first.");
            } else
                showComponent(new CreateCategoryEditor(this, groups, 
                        selectedCategoryGroupID), FOR_CLASSIFICATION);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's categoryGroup.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);   
        } 
    }
    
    /** Create a new categoryGroup. */
    void createGroup()
    {
        showComponent(new CreateGroupEditor(this), FOR_CLASSIFICATION);
    }

    /** Retrieve all categoryGroups. */
    public List getCategoryGroups() 
    {
        try {
            return abstraction.getCategoryGroups();
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the category groups.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
        return new ArrayList();
    }

    /** 
     * List of existing categories not in the current group. 
     * Only, the categories containing images not already in the group
     * will be displayed.
     */
    public List getCategoriesNotInGroup(CategoryGroupData group)
    {
        try {
            return abstraction.retrieveCategoriesNotInGroup(group);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the category.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
        return new ArrayList();
    }

    /** Forward request to the {@link DataManager abstraction}. */
    public List getImagesNotInCategoryGroup(CategoryGroupData group,
                                Map filters, Map complexFilters)
    {
        try {
            return abstraction.retrieveImagesNotInCategoryGroup(group, filters, 
                                complexFilters);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
        return new ArrayList();
    }
    
   /** Forward request to the {@link DataManager abstraction}. */
    public List getImagesInUserDatasetsNotInCategoryGroup(CategoryGroupData 
            group, List datasets, Map filters, Map complexFilters)
    {
        try {
            return abstraction.retrieveImagesInUserDatasetsNotInCategoryGroup(
                    group, datasets, filters, complexFilters);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
        return new ArrayList();
    }
    
    /** Forward request to the {@link DataManager abstraction}. */
    public List getImagesInUserGroupNotInCategoryGroup(CategoryGroupData 
            group, Map filters, Map complexFilters)
    {
        try {
            return abstraction.retrieveImagesInUserGroupNotInCategoryGroup(
                    group, filters, complexFilters);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
        return new ArrayList();
    }
    
    /** Forward request to the {@link DataManager abstraction}. */
    public List getImagesInSystemNotInCategoryGroup(CategoryGroupData group,
            Map filters, Map complexFilters)
    {
        try {
            return abstraction.retrieveImagesInSystemNotInCategoryGroup(
                    group, filters, complexFilters);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
        return new ArrayList();
    }
    
    /** Forward request to the {@link DataManager abstraction}. */
    public List getImagesDiffNotInCategoryGroup(CategoryData data, Map filters, 
                        Map complexFilters)
    {
        return abstraction.retrieveImagesDiffNotInCategoryGroup(data, filters, 
                            complexFilters);
    }
    
    /** Forward request to the {@link DataManager abstraction}. */
    public List getImagesDiffInUserDatasetsNotInCategoryGroup(CategoryData 
            data, List datasets, Map filters, Map complexFilters)
    {
        return abstraction.retrieveImagesDiffInUserDatasetsNotInCategoryGroup(
                    data, datasets, filters, complexFilters);
    }
    
    /** Forward request to the {@link DataManager abstraction}. */
    public List getImagesDiffInUserGroupNotInCategoryGroup(CategoryData data, 
                         Map filters, Map complexFilters)
    {
        return abstraction.retrieveImagesDiffInUserGroupNotInCategoryGroup(
                data, filters, complexFilters);
    }
    
    /** Forward request to the {@link DataManager abstraction}. */
    public List getImagesDiffInSystemNotInCategoryGroup(CategoryData data, 
                Map filters, Map complexFilters)
    {
        return abstraction.retrieveImagesDiffInSystemNotInCategoryGroup(
                data, filters, complexFilters);
    }
    
    /** Forward event to the {@link DataManager} abstraction. */
    public void updateCategoryGroup(CategoryGroupData model, List toAdd, 
                                    boolean nameChange)
    {
        abstraction.updateCategoryGroup(model, toAdd, nameChange);
    }

    /** Forward event to the {@link DataManager} abstraction. */
    public void updateCategory(CategoryData model, List imgsToRemove, 
                                List imgsToAdd, boolean nameChange)
    {
        abstraction.updateCategory(model, imgsToRemove, imgsToAdd, nameChange);
    }
    
    /** Create a new group and existing datasets. */
    public void saveNewGroup(String name, String description)
    {
        CategoryGroupData cgd = new CategoryGroupData();
        cgd.setName(name);
        cgd.setDescription(description);
        abstraction.createCategoryGroup(cgd);
    }
    
    /** Create a new group and a new category. */
    public void createNewCategory(CategoryGroupData group, String name, 
                                String description, List images)
    {
        setSelectedCategoryGroup(group.getID());
        CategoryData cd = new CategoryData();
        cd.setName(name);
        cd.setDescription(description);
        cd.setCategoryGroup(group);
        abstraction.createCategory(cd, images);
    }

    /** Attach listener to a menuItem or a button. */
    void attachItemListener(AbstractButton item, int id)
    {
        item.setActionCommand(""+id);
        item.addActionListener(this);
    }
    
}

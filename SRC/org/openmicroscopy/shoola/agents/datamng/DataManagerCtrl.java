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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;

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
import org.openmicroscopy.shoola.env.data.model.CategorySummary;
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
 * 
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
    
	private DataManager			abstraction;
	
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
	public List getImagesDiff(DatasetData data)
	{
		return abstraction.getImagesDiff(data);
	}
	
	/** Forward event to the {@link DataManager abstraction}. */
	public void addProject(ProjectData pd)
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
    
    /** Return the abstraction. */
    DataManager getAbstraction() {return abstraction; }
    
    /** 
     * Brings up a suitable property sheet dialog for the specified
     * <code>target</code>.
     *
     * @param   target      A project, dataset or image. 
     *                      If you pass anything different this method does
     *                      nothing.
     */
    void showProperties(DataObject target)
    {
        Registry registry = abstraction.getRegistry();
        if (target == null)    return;
        try {
            if (target instanceof ProjectSummary) {
                ProjectData project = abstraction.getProject(
                                        ((ProjectSummary) target).getID());
                UIUtilities.centerAndShow(new ProjectEditor(registry, this, 
                                            project));     
            } else if (target instanceof DatasetSummary) {
                DatasetData dataset = abstraction.getDataset(
                                        ((DatasetSummary) target).getID());                                         
                UIUtilities.centerAndShow(new DatasetEditor(registry, this, 
                                            dataset));
            } else if (target instanceof ImageSummary) {
                ImageData image = abstraction.getImage(
                                        ((ImageSummary) target).getID());
                UIUtilities.centerAndShow(new ImageEditor(registry, this, 
                                        image));
            } else if (target instanceof CategoryGroupData) {
                UIUtilities.centerAndShow(new GroupEditor(registry, this, 
                                (CategoryGroupData) target));
            } else if (target instanceof CategorySummary) {
                CategoryData cd = abstraction.getCategoryData(
                        ((CategorySummary) target).getID());
                UIUtilities.centerAndShow(new CategoryEditor(registry, this, 
                                        cd));
            }
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the specified target.";
            registry.getLogger().error(this, s+" Error: "+dsae);
            registry.getUserNotifier().notifyError("Data Retrieval Failure", s, 
                                                    dsae);
        } 
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    void updateImage(ImageSummary is)
    {
        abstraction.updateImage(is);
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    void viewImage(ImageSummary is)
    {
        int[] pxSets = is.getPixelsIDs();
        //TODO: select pixels if more than one!
        abstraction.viewImage(is.getID(), pxSets[0], is.getName());
    }
    
    /** Forward the call to the {@link DataManager abstraction}. */
    public List getUserImages()
    { 
        try {
            return abstraction.getUserImages();
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve user's images.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae);
        }
        return null;
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
    List getUserDatasets()
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
    void viewDataset(DatasetSummary ds)
    { 
        abstraction.viewDataset(ds.getID());
    }

    /** Forward event to the {@link DataManager abstraction}. */
    void annotateDataset(DatasetSummary ds)
    {
        abstraction.annotateDataset(ds.getID(), ds.getName());
    }
    
    /** Forward event to the {@link DataManager abstraction}. */
    void annotateImage(ImageSummary is)
    {
        int[] pixelsID = is.getPixelsIDs();
        abstraction.annotateImage(is.getID(), is.getName(), pixelsID[0]);
    }
    
    /** Refresh the Tree. */
    void refresh() { abstraction.refresh(); }
    
    /** Bring up the corresponding editor. */
    void createProject()
    {
        try {
            List datasets = abstraction.getUserDatasets();
            UIUtilities.centerAndShow(new CreateProjectEditor(
                                        abstraction.getRegistry(), this,
                                        new ProjectData(), datasets));
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
            List projects = abstraction.getUserProjects();
            //List images = abstraction.getUserImages();
            UIUtilities.centerAndShow(new CreateDatasetEditor(
                                        abstraction.getRegistry(), this,
                                        new DatasetData(), projects));
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
    
    /** Attach listener to a menuItem or a button. */
    void attachItemListener(AbstractButton item, int id)
    {
        item.setActionCommand(""+id);
        item.addActionListener(this);
    }
    
    /** Forward to the {@link DataManager abstraction}. */
    String getUserName() { return abstraction.getUserName(); }

    
    //Category manager
    /** Forward to the {@link DataManager abstraction}. */
    void viewCategoryGroup(CategoryGroupData data)
    {
       //Post an event 
    }

    /** Forward to the {@link DataManager abstraction}. */
    void viewCategory(CategorySummary data)
    {
        //Post an event 
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
            }
            UIUtilities.centerAndShow(new CreateCategoryEditor(this, groups));
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
        UIUtilities.centerAndShow(new CreateGroupEditor(this));
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
        return null;
    }

    /** Retrieve all the images contained in the specified category. */
    public List getImagesInCategory(int id)
    {
        try {
            CategoryData model = abstraction.getCategoryData(id);
            return model.getImages();
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the category.";
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
        /*
        List categories = new ArrayList();
        try {
            
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the categories.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
        return categories;
        */
    }

    /** 
     * Return the images belonging to the user not contained in the
     * specified CategoryGroupgroup.
     * 
     * @param group     corresponding data object.
     * @return  list of {@link ImageSummary}s.
     */
    public List getImagesNotInGroup(CategoryGroupData group)
    {
        try {
            return abstraction.retrieveImagesNotInGroup(group);
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the category.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
        return new ArrayList();
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
        CategoryData cd = new CategoryData();
        cd.setName(name);
        cd.setDescription(description);
        cd.setCategoryGroup(group);
        abstraction.createCategory(cd, images);
    }


}

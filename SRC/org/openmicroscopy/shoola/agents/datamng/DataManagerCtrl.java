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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.editors.category.CategoryEditor;
import org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup.CreateEditor;
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
    
    /** ID used to handle the create CategoryGroup and Category event. */
    static final int            CREATE_CG = 3;
    
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
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) { 
                case PROJECT_ITEM:
                    createProject(); break;
                case DATASET_ITEM:
                    createDataset(); break; 
                case IMAGE_ITEM:
                    showImagesImporter();  break;
                case CREATE_CG:
                    createCG();
                    
            }
        } catch(NumberFormatException nfe) {  
            throw new Error("Invalid Action ID "+index, nfe);
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
    List getUserImages()
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
            List images = abstraction.getUserImages();
            UIUtilities.centerAndShow(new CreateDatasetEditor(
                                        abstraction.getRegistry(), this,
                                        new DatasetData(), projects, images));
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
    String getUserLastName() { return abstraction.getUserLastName(); }

    
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

    /** Create a new categoryGroup. */
    void createCG()
    {
        try {
            List l = abstraction.getUserImages();
            //Prepare the data
            Object[] r = getData(abstraction.getCategoryGroups());
            UIUtilities.centerAndShow(new CreateEditor(this, 
                    (CategoryGroupData[]) r[0], (CategorySummary[]) r[1], l));
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the category groups.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
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
    
    /** List of existing categories not in the current group. */
    public List getCategoriesDiff(CategoryGroupData model)
    {
        try {
            List categories = model.getCategories();
            List allCategories = abstraction.getCategories();
            List results = new ArrayList();
            results.addAll(allCategories);
            Iterator i = allCategories.iterator(), j;
            CategorySummary cs, csAll;
            while (i.hasNext()) {
                csAll = (CategorySummary) i.next();
                j = categories.iterator();
                while (j.hasNext()) {
                    cs = (CategorySummary) j.next();
                    if (cs.getID() == csAll.getID())
                        results.remove(csAll);
                }
            }
            return results;
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the category groups.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
        return null;
    }


    public List getCategoryImagesDiff(List images)
    {
        try {
            List allImages = abstraction.getUserImages();
            List results = new ArrayList();
            results.addAll(allImages);
            Iterator i = allImages.iterator(), j;
            ImageSummary is, isAll;
            while (i.hasNext()) {
                isAll = (ImageSummary) i.next();
                j = images.iterator();
                while (j.hasNext()) {
                    is = (ImageSummary) j.next();
                    if (is.getID() == isAll.getID())
                        results.remove(isAll);
                }
            }
            return results;
        } catch(DSAccessException dsae) {
            String s = "Can't retrieve the category groups.";
            getRegistry().getLogger().error(this, s+" Error: "+dsae);
            getRegistry().getUserNotifier().notifyError("Data Retrieval " +
                    "Failure", s, dsae); 
        } 
        return null;
    }
    
    public void updateCategoryGroup(CategoryGroupData model, List toAdd, 
                                    boolean nameChange)
    {
        abstraction.updateCategoryGroup(model, toAdd, nameChange);
    }

    public void updateCategory(CategoryData model, List imgsToRemove, 
                                List imgsToAdd, boolean nameChange)
    {
        abstraction.updateCategory(model, imgsToRemove, imgsToAdd, nameChange);
    }
    
    /** Create a new group and existing datasets. */
    public void createNewGroup(String name, String description)
    {
        CategoryGroupData cgd = new CategoryGroupData();
        cgd.setName(name);
        cgd.setDescription(description);
        abstraction.createCategoryGroup(cgd);
    }
    
    /** Create a new group and a new category. */
    public void createNews(String nGroup, String nCategory, String dGroup, 
                            String dCategory, List images)
    {
        CategoryGroupData cgd = new CategoryGroupData();
        cgd.setName(nGroup);
        cgd.setDescription(dGroup);
        CategoryData cd = new CategoryData();
        cd.setName(nCategory);
        cd.setDescription(dCategory);
        cd.setCategoryGroup(cgd);
        abstraction.createCategory(cd, images);
    }
    
    /** Create a new category linked to an existing group. */
    public void createNewCategory(CategoryGroupData data, String name, 
                                    String description, List images)
    {
        CategoryData cd = new CategoryData();
        cd.setName(name);
        cd.setDescription(description);
        cd.setCategoryGroup(data);
        abstraction.createCategory(cd, images);
    }
    
    /** Retrieve all the images contained in the specified category. */
    public List getImagesInCategory(int categoryID)
    {
        return getImages(abstraction.getCategoryData(categoryID));
    }
    
    public List getImages(CategoryData model)
    {
        Map classifications = model.getClassifications();
        Iterator i = classifications.keySet().iterator();
        List results = new ArrayList();
        while (i.hasNext())
            results.add(i.next());
        return results;
    }
    
    /** Prepare the data for the GUI. */
    private Object[] getData(List l)
    {
        Object[] results = new Object[2];
        CategoryGroupData[] data; 
        CategorySummary[] summaries;
        if (l != null) {
            Iterator i = l.iterator();
            List categories = new ArrayList();
            data = new CategoryGroupData[l.size()];
            int index = 0;
            CategoryGroupData cgd;
            CategorySummary cs;
            Iterator j;
            while (i.hasNext()) {
                cgd = (CategoryGroupData) i.next();
                data[index] = cgd;
                if (cgd.getCategories() != null) {
                    j = cgd.getCategories().iterator();
                    while (j.hasNext()) {
                        cs = (CategorySummary) j.next();
                        if (!categories.contains(cs))
                            categories.add(cs);
                    }
                }
                index++; 
            }
            summaries = getSummary(categories);
        } else {
            data = new CategoryGroupData[0];
            summaries = new CategorySummary[0];
        }
        results[0] = data;
        results[1] = summaries;
        return results;
    }
    
    private CategorySummary[] getSummary(List l)
    {
        CategorySummary[] data;
        if (l != null && l.size() > 0) {
            Iterator i = l.iterator();
            data = new CategorySummary[l.size()];
            int index = 0;
            while (i.hasNext()) {
                data[index] = (CategorySummary) i.next();
                index++; 
            }
        } else data = new CategorySummary[0];  
        return data;
    }
    
}

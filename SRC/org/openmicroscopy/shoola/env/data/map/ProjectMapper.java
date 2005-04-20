/*
 * org.openmicroscopy.shoola.env.data.map.ProjectMapper
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

package org.openmicroscopy.shoola.env.data.map;



//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.dto.Project;
import org.openmicroscopy.ds.st.DatasetAnnotation;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.ds.st.Group;
import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

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
public class ProjectMapper
{

    /**
     * Build criteria for the retrieveProjectTree method.
     * 
     * @param projectIDs List of projects ID.
     * @return See above.
     */
    public static Criteria buildProjectsTreeCriteria(List projectIDs)
    {
        Criteria c = buildBasicCriteria();
        //Specify which fields we want for the datasets.
        c.addWantedField("datasets", "images");
        //Specify which fields we want for the images.
        c.addWantedField("datasets.images", "name");
        c.addWantedField("datasets.images", "created");
        c.addWantedField("datasets.images", "default_pixels");
        
        //Specify which fields we want for the pixels.
        PixelsMapper.fieldsForPixels(c, "datasets.images.default_pixels");

        if (projectIDs != null) c.addFilter("id", "IN", projectIDs);
        return c;
    }
    
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for updateProject.
	 * 
	 * @param projectID	specified project to retrieve.
	 */
	public static Criteria buildUpdateCriteria(int projectID)
	{
		Criteria c = new Criteria();
		c.addWantedField("name");
		c.addWantedField("description");
		c.addFilter("id", new Integer(projectID));
		return c;
	}
	
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieveUserProjects.
	 * 
	 * @param userID	user ID.
	 */
	public static Criteria buildUserProjectsCriteria(int userID)
	{
		Criteria criteria = buildBasicCriteria();
		//Retrieve by user's ID.
		criteria.addFilter("owner_id", new Integer(userID));
		return criteria;
	}
	
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieveProject.
	 * 
	 */
	public static Criteria buildProjectCriteria(int id)
	{
		Criteria criteria = buildBasicCriteria();
        
		//Specify which fields we want for the project.
		criteria.addWantedField("description");
		criteria.addWantedField("owner");
        UserMapper.objectOwnerCriteria(criteria);
		//Filter by ID.
		criteria.addFilter("id", new Integer(id));
		
		return criteria;
	}
    
    private static Criteria buildBasicCriteria()
    {
        Criteria criteria = new Criteria();
        criteria.addWantedField("name"); 
        criteria.addWantedField("datasets"); 
        //Specify which fields we want for the datasets.
        criteria.addWantedField("datasets", "name");
        return criteria;
    }
	   
    /** Return of Object ID corresponding to the dataset ID. */
    public static List prepareListDatasetsID(List projects)
    {
        Map map = new HashMap();
        Iterator i = projects.iterator(), k;
        List datasets;
        Integer id;
        while (i.hasNext()) {
            datasets = ((Project) i.next()).getDatasets();
            k = datasets.iterator();
            while (k.hasNext()) {
                id = new Integer(((Dataset) k.next()).getID());
                map.put(id, id);
            }
        }
        i = map.keySet().iterator();
        List ids = new ArrayList();
        while (i.hasNext()) 
            ids.add(i.next());

        return ids;
    }
    
    /** Return of Object ID corresponding to the image ID. */
    public static List prepareListImagesID(List projects)
    {
        Map map = new HashMap();
        Iterator i = projects.iterator(), j, k;
        List datasets, images;
        Integer id;
        while (i.hasNext()) {
            datasets = ((Project) i.next()).getDatasets();
            j = datasets.iterator();
            while (j.hasNext()) {
                images = ((Dataset) j.next()).getImages();
                k = images.iterator();
                while (k.hasNext()) {
                    id = new Integer(((Image) k.next()).getID());
                    map.put(id, id);
                }  
            }
        }
        i = map.keySet().iterator();
        List ids = new ArrayList();
        while (i.hasNext()) 
            ids.add(i.next());
        return ids;
    }
    
	/** 
	 * Fill in the project data object. 
	 * 
	 * @param project	project graph.
	 * @param empty		project data to fill in.
	 * 
	 */
	public static void fillProject(Project project, ProjectData empty)
	{
		
		//Fill in the data coming from Project.
		empty.setID(project.getID());
		empty.setName(project.getName());
		empty.setDescription(project.getDescription());
				
		//Fill in the data coming from Experimenter.
		Experimenter owner = project.getOwner();
		empty.setOwnerID(owner.getID());
		empty.setOwnerFirstName(owner.getFirstName());
		empty.setOwnerLastName(owner.getLastName());
		empty.setOwnerEmail(owner.getEmail());
		empty.setOwnerInstitution(owner.getInstitution());
		
		//Fill in the data coming from Group.
		Group group = owner.getGroup();
		empty.setOwnerGroupID(group.getID());
		empty.setOwnerGroupName(group.getName());
		
		//Create the dataset summary list.
		List datasets = new ArrayList();
		Iterator i = project.getDatasets().iterator();
		Dataset d;
		while (i.hasNext()) {
			d = (Dataset) i.next();
			datasets.add(new DatasetSummary(d.getID(), d.getName()));
		}
		empty.setDatasets(datasets);			
	}

	
	/**
	 * Create list of project summary objects.
	 * 
	 * @param projects	OMEDS.
	 * @param pProto	
	 * @param dProto
	 * @return 
	 */
	public static List fillUserProjects(List projects, ProjectSummary pProto, 
										DatasetSummary dProto)
	{
		Map	datasetsMap = new HashMap();
		List projectsList = new ArrayList();  //The returned summary list.
		Iterator i = projects.iterator(), j;
		ProjectSummary ps;
		Project p;
		DatasetSummary ds;
		Dataset d;
		List datasets;
        Integer id;
		//For each p in projects...
		while (i.hasNext()) {
			p = (Project) i.next();
			
			//Make a new DataObject and fill it up.
			ps = (ProjectSummary) pProto.makeNew();
			ps.setID(p.getID());
			ps.setName(p.getName());

			j = p.getDatasets().iterator();
			datasets = new ArrayList();
			while (j.hasNext()) {
				d = (Dataset) j.next();
				id = new Integer(d.getID());
				ds = (DatasetSummary) datasetsMap.get(id);
				if (ds == null) {
					//Make a new DataObject and fill it up.
					ds = (DatasetSummary) dProto.makeNew();		
					ds.setID(d.getID());
					ds.setName(d.getName());
					datasetsMap.put(id, ds);
				}  //else we have already created this object.
				
				//Add the dataset to this project's list.
				datasets.add(ds);	
			}
			
			//Link the datasets to this project.
			ps.setDatasets(datasets);
			
			//Add the project to the list of returned projects.
			projectsList.add(ps);
		}
		
		return projectsList;
	}

    /**
     * Create a list of {@link ProjectSummary}s.
     * @param projects
     * @param pProto
     * @param dProto
     * @param annotations
     * @param userID
     * @return
     */
    public static List fillListAnnotatedDatasets(List projects, ProjectSummary 
                    pProto, DatasetSummary dProto, List annotations)
    {
        List projectsList = new ArrayList();
        Map ids = AnnotationMapper.reverseListDatasetAnnotations(annotations);
        Map datasetsMap = new HashMap();
        Iterator i = projects.iterator(), j;
        //DataObject.
        ProjectSummary ps;
        DatasetSummary ds;
        Project p;
        Dataset d;
        List datasets;
        Integer id;
        //For each p in projects...
        while (i.hasNext()) {
            p = (Project) i.next();
            
            //Make a new DataObject and fill it up.
            ps = (ProjectSummary) pProto.makeNew();
            ps.setID(p.getID());
            ps.setName(p.getName());

            j = p.getDatasets().iterator();
            datasets = new ArrayList();
            while (j.hasNext()) {
                d = (Dataset) j.next();
                id = new Integer(d.getID());
                ds = (DatasetSummary) datasetsMap.get(id);
                if (ds == null) {
                    //Make a new DataObject and fill it up.
                    ds = (DatasetSummary) dProto.makeNew();     
                    ds.setID(d.getID());
                    ds.setName(d.getName());
                    ds.setAnnotation(AnnotationMapper.fillDatasetAnnotation(
                            (DatasetAnnotation) ids.get(id)));
                    datasetsMap.put(id, ds);
                }  //object already created this object.
                //Add the dataset to this project's list.
                datasets.add(ds);   
            }
            
            //Link the datasets to this project.
            ps.setDatasets(datasets);
            
            //Add the project to the list of returned projects.
            projectsList.add(ps);
        }
        
        return projectsList;
    }

	/**
	 * Create list of project summary objects.
	 * 
	 * @param projects	OMEDS.
	 * @param pProto	
	 * @param dProto
	 * @return 
	 */
	public static List fillUserProjectsWithDatasetData(List projects, 
			ProjectSummary pProto, DatasetData dProto)
	{
		Map	datasetsMap = new HashMap();
		List projectsList = new ArrayList();  //The returned summary list.
		Iterator i = projects.iterator(), j;
		ProjectSummary ps;
		Project p;
		DatasetData ds;
		Dataset d;
		List datasets;
        Integer id;
		//For each p in projects...
		while (i.hasNext()) {
			p = (Project) i.next();
			
			//Make a new DataObject and fill it up.
			ps = (ProjectSummary) pProto.makeNew();
			ps.setID(p.getID());
			ps.setName(p.getName());

			j = p.getDatasets().iterator();
			datasets = new ArrayList();
			while (j.hasNext()) {
				d = (Dataset) j.next();
				id = new Integer(d.getID());
				ds = (DatasetData) datasetsMap.get(id);
				if (ds == null) {
					//Make a new DataObject and fill it up.
					ds = (DatasetData) dProto.makeNew();		
					ds.setID(d.getID());
					ds.setName(d.getName());
					datasetsMap.put(id, ds);
				}  //else we have already created this object.
				
				//Add the dataset to this project's list.
				datasets.add(ds);	
			}
			
			//Link the datasets to this project.
			ps.setDatasets(datasets);
			
			//Add the project to the list of returned projects.
			projectsList.add(ps);
		}
		
		return projectsList;
	}
    
	/**
     * Fill in a {@link ProjectSummary} object.
     * 
	 * @param p        Remote object.
	 * @param datasets List of DatasetSummary object to add to the new project.
	 * @param pProto   Prototype.
	 * @return
	 */
	public static List fillNewProject(Project p, List datasets, 
										ProjectSummary pProto)
	{
		List ids = new ArrayList();
		if (datasets != null) {	//To be on the save side
			Iterator i = datasets.iterator();
			while (i.hasNext())
				ids.add(new Integer(((DatasetSummary) i.next()).getID()));
		}
		pProto.setID(p.getID());
		pProto.setName(p.getName());
		pProto.setDatasets(datasets);
		return ids;
	}
    
    /** 
     * Fill in the project tree.
     * 
     * @param projects  List of remote Project objects.
     * @param results   Empty list to fill in with {@link ProjectSummary} 
     *                  objects.
     * @param projectIDs
     * @param dsAnnotations List of datasetAnnotation. Can be <code>null</code>.
     * @param isAnnotations List of imageAnnotation. Can be <code>null</code>.
     */
    public static void fillProjectsTree(List projects, List results, 
                            List projectIDs, List dsAnnotations, 
                            List isAnnotations)
    {
        Map imgAnnotated = 
            AnnotationMapper.reverseListImageAnnotations(isAnnotations);
        Map dAnnotated = 
            AnnotationMapper.reverseListDatasetAnnotations(dsAnnotations);
        Iterator i = projects.iterator(), j, k;
        Map datasetsMap = new HashMap(), imagesMap = new HashMap();
        Project p;
        List datasets, images;
        ProjectSummary ps;
        Dataset d;
        DatasetSummaryLinked ds;
        ImageSummary is;
        Image img;
        Integer id, idImg;
        while (i.hasNext()) {
            p = (Project) i.next();
            if (projectIDs.contains(new Integer(p.getID()))) {
                ps = new ProjectSummary(p.getID(), p.getName());
                datasets = new ArrayList();
                j = p.getDatasets().iterator();
                while (j.hasNext()) {
                    d = (Dataset) j.next();
                    id = new Integer(d.getID());
                    ds = (DatasetSummaryLinked) datasetsMap.get(id);
                    if (ds == null) {
                        //Make a new DataObject and fill it up.
                        ds = new DatasetSummaryLinked(); 
                        ds.setID(d.getID());
                        ds.setName(d.getName());
                        datasetsMap.put(id, ds);
                        ds.setAnnotation(AnnotationMapper.fillDatasetAnnotation(
                                (DatasetAnnotation) dAnnotated.get(id)));
                    }  //object already created this object.
                    //Add the dataset to this project's list.
                    datasets.add(ds);   
                    //Add images to the dataset
                    images = new ArrayList();
                    k = d.getImages().iterator();
                    while (k.hasNext()) {
                        img = (Image) k.next();
                        idImg = new Integer(img.getID());
                        is = (ImageSummary) imagesMap.get(idImg);
                        if (is == null) {
                            is = ImageMapper.buildImageSummary(img, null);
                            is.setAnnotation(
                                    AnnotationMapper.fillImageAnnotation(
                                    (ImageAnnotation) imgAnnotated.get(idImg)));
                            imagesMap.put(idImg, is);
                        }
                        images.add(is);
                    }
                    ds.setImages(images);
                }
                //Link the datasets to this project.
                ps.setDatasets(datasets);
                results.add(ps);
            }
        }
    }
    
}

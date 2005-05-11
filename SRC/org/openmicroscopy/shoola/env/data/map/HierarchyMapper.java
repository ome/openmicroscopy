/*
 * org.openmicroscopy.shoola.env.data.map.HierarchyMapper
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
import java.util.Collection;
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
import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.ds.st.Classification;
import org.openmicroscopy.ds.st.DatasetAnnotation;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.ClassificationData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

/** 
 * Utility class. 
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
public class HierarchyMapper
{

    /**
     * Builds a criteria to retrieve data linked to Classification.
     * 
     * @return See above.
     */
    private static Criteria buildClassificationCriteria()
    {
        Criteria c = new Criteria();
        c.addWantedField("Confidence");
        c.addWantedField("Category");
        //Fields for the category
        c.addWantedField("Category", "Name");
        c.addWantedField("Category", "Description");
        c.addWantedField("Category", "CategoryGroup");
        c.addWantedField("Category.CategoryGroup", "Name");
        c.addWantedField("Category.CategoryGroup", "Description");
        //Fields we want for the images.
        c.addWantedField("image");
        return c;
    }
    
    /** 
     * Fill in a {@link DatasetSummaryLinked} given a 
     * remote {@link Dataset}.
     * 
     * @param dataset   The remote objet.
     * @return See above.
     */
    private static DatasetSummaryLinked createDSL(Dataset dataset)
    {
        DatasetSummaryLinked ds = new DatasetSummaryLinked();
        ds.setID(dataset.getID());
        ds.setName(dataset.getName());
        ds.setImages(new ArrayList());
        return ds;
    }
    
    /** 
     * Fill in a {@link ProjectSummary} given a 
     * remote {@link Project}.
     * 
     * @param dataset   The remote objet.
     * @return See above.
     */
    private static ProjectSummary createPS(Project project)
    {
        ProjectSummary ps = new ProjectSummary();
        ps.setID(project.getID());
        ps.setName(project.getName());
        ps.setDatasets(new ArrayList());
        return ps;
    }
    
    /**
     * Creates a CategoryData object.
     * 
     * @param category  The remote object.
     * @param categories map of already created data object.
     * @return See above.
     */
    private static CategoryData createCategoryData(Category category, 
                                Map categories)
    {
        Integer id = new Integer(category.getID());
        CategoryData data = (CategoryData) categories.get(id);
        if (data == null) {
            data = new CategoryData();
            data.setID(category.getID());
            data.setName(category.getName());
            categories.put(id, data);
        }
        return data;
    }
    
    /**
     * Creates a CategoryGroupData object.
     * 
     * @param group  The remote object.
     * @return See above.
     */
    private static CategoryGroupData createCategoryGroupData(CategoryGroup 
                                            group)
    {
        CategoryGroupData data = new CategoryGroupData();
        data.setID(group.getID());
        data.setName(group.getName());
        return data;
    }
    
    /**
     * Builds the criteria to retrieve the Project-dataset-Image hierarchy.
     * 
     * @param ids   List of image ID.
     * @return See above.
     */
    public static Criteria buildIDPHierarchyCriteria(List ids)
    {
        Criteria c = new Criteria();
        //Specify which fields we want for the image.
        c.addWantedField("datasets");
        
        //Specify which fields we want for the datasets.
        c.addWantedField("datasets", "name");
        c.addWantedField("datasets", "projects");
        c.addWantedField("datasets.projects", "name");
        
        //Add Filter
        if (ids != null) c.addFilter("id", "IN", ids);
        return c;
    }

    /**
     * Builds the criteria to retrieve the CategoryGroup-Category-Images
     * hierarchy.
     * 
     * @param imageIDs  List of image ids.
     * @param userID    user ID.
     * @return See above.
     */
    public static Criteria buildICGHierarchyCriteria(List imageIDs, int userID)
    {
        Criteria c = buildClassificationCriteria();
        c.addFilter("Valid", Boolean.TRUE);
        if (imageIDs != null) c.addFilter("image_id", "IN", imageIDs);
        //In this case, the filter should work ;-)
        if (userID != -1)
            c.addFilter("module_execution.experimenter_id", 
                    new Integer(userID));
        return c;
    }
    
    /**
     * Builds the criteria to retrieve the available paths.
     * @param userID 
     * @return See above.
     */
    public static Criteria buildAvailablePaths(int userID)
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("CategoryGroup");
        c.addWantedField("CategoryGroup", "Name");
        c.addWantedField("ClassificationList");
        //wanted fields for ClassificationList
        c.addWantedField("ClassificationList", "Valid");
        c.addWantedField("ClassificationList", "image");
        
        //May add filtering
        c.addWantedField("ClassificationList", "module_execution");
        c.addWantedField("ClassificationList.module_execution", 
                            "experimenter");
        if (userID != -1) c.addFilter("module_execution.experimenter_id", 
                                        new Integer(userID));
        return c;
    }
    
    /** 
     * Fill in a Project-Dataset-Image hierarchy.
     * 
     * @param images        List of {@link Image} objects.
     * @param mapIS         map of imageID, ImageSummary objects.
     * @param dsAnnotations List of {@link DatasetAnnotation} objects.
     * @return  List of {@link DataObject}s.
     */
    public static List fillIDPHierarchy(List images, Map mapIS, 
                                    List dsAnnotations)
    {
        Map dAnnotated = 
            AnnotationMapper.reverseListDatasetAnnotations(dsAnnotations);
        List unOrderedImages = new ArrayList();
        List orphanDatasets = new ArrayList();
        List results = new ArrayList();
        Iterator i = images.iterator();
        Map datasetsMap = new HashMap(), projectsMap = new HashMap();
        Image image;
        Dataset dataset;
        Project project;
        List dl, pl;
        Iterator j, k;
        ImageSummary is;
        ProjectSummary ps;
        DatasetSummaryLinked ds;
        Integer dID, pID;
        List listImages, listDatasets, listProjects;
        listProjects = new ArrayList();
        AnnotationData ad;
        while (i.hasNext()) {
            image = (Image) i.next();
            is = (ImageSummary) mapIS.get(new Integer(image.getID()));
            if (is != null) {
                dl = image.getDatasets();
                //unorderedImage
                if (dl.size() == 0) unOrderedImages.add(is);
                else {
                    j = dl.iterator();
                    while (j.hasNext()) { //for each dataset
                        dataset = (Dataset) j.next();
                        //TODO: B/c cannot filter before and 
                        //b/c of server implementation
                        if (!(dataset.getName().equals("ImportSet"))) {
                            dID = new Integer(dataset.getID());
                            ds = (DatasetSummaryLinked) datasetsMap.get(dID);
                            if (ds == null) {
                                ds = createDSL(dataset);
                                ad =  AnnotationMapper.fillDatasetAnnotation(
                                     (DatasetAnnotation) dAnnotated.get(dID));
                                ds.setAnnotation(ad);
                                datasetsMap.put(dID, ds);
                            }
                            listImages = ds.getImages();
                            if (!listImages.contains(is)) listImages.add(is);
                            //for eachProject
                            pl = dataset.getProjects();
                            if (pl.size() == 0) { //orphan datasets
                                orphanDatasets.add(ds);
                            } else {
                                k = pl.iterator();
                                while (k.hasNext()) {
                                    project = (Project) k.next();
                                    pID = new Integer(project.getID());
                                    ps = (ProjectSummary) projectsMap.get(pID);
                                    if (ps == null) {
                                        ps = createPS(project);
                                        projectsMap.put(pID, ps);
                                    }
                                    listDatasets = ps.getDatasets();
                                    if (!listDatasets.contains(ds)) 
                                        listDatasets.add(ds);
                                    if (!listProjects.contains(ps)) 
                                        listProjects.add(ps);
                                }
                            }  
                        } 
                    }  
                }
            }
        }
        results.addAll(listProjects);
        results.addAll(orphanDatasets);
        results.addAll(unOrderedImages);
        return results;
    }
    
    /** 
     * Fill in a Image-Category-CategoryGroup hierarchy.
     * 
     * @param classifications List of  remote {@link Classification} objects.
     * @param mapIS           Map of imageID, ImageSummary objects.
     * @return List of {@link DataObject}.
     */
    public static List fillICGHierarchyExisting(List classifications, Map mapIS)
    {
        List results = new ArrayList();
        //OME-JAVA object.
        Classification classification;
        Category category;
        CategoryGroup group;
        //Shoola object.
        ClassificationData cData;
        CategoryData cModel;
        CategoryGroupData gModel;
        ImageSummary is;
        Map categoryMap = new HashMap(), groupMap = new HashMap(), 
            orphanMap = new HashMap(), classificationsMap;
        float f;
        Integer categoryID, groupID;
        Collection unclassifiedImages = mapIS.values();
        List classifiedImages = new ArrayList();
        List categoriesList;
        CategoryGroupData gProto = new CategoryGroupData();
        CategoryData cProto = new CategoryData();
        Iterator i = classifications.iterator();
        while (i.hasNext()) {
            classification = (Classification) i.next();
            f = CategoryMapper.CONFIDENCE;
            if (classification.getConfidence() != null)
                f =  classification.getConfidence().floatValue();
            is = (ImageSummary) mapIS.get(
                    new Integer(classification.getImage().getID()));
           
            //Control needed b/c the IN Filter may not have been used.
            if (is != null) {
                if (!classifiedImages.contains(is)) classifiedImages.add(is);
                
                cData = new ClassificationData(classification.getID(), f);
                category = classification.getCategory();
                
                group = category.getCategoryGroup();
                categoryID = new Integer(category.getID());
                if (group == null) {//Orphan category.
                    cModel = (CategoryData) orphanMap.get(categoryID);
                    if (cModel == null) {
                        cModel = CategoryMapper.buildCategoryData(cProto, 
                                    category, null);
                        cModel.setClassifications(new HashMap());
                        orphanMap.put(categoryID, cModel);
                    }
                } else {
                    groupID = new Integer(group.getID());
                    gModel = (CategoryGroupData) groupMap.get(groupID);
                    //Create CategoryGroupData
                    if (gModel == null) {
                        gModel = CategoryMapper.buildCategoryGroup(gProto, 
                                        group);
                        gModel.setCategories(new ArrayList());
                        groupMap.put(groupID, gModel);
                    }
                    cModel = (CategoryData) categoryMap.get(categoryID);
                    //Create CategoryData
                    if (cModel == null) {
                        cModel = CategoryMapper.buildCategoryData(cProto, 
                                category, gModel);
                        cModel.setClassifications(new HashMap());
                        categoryMap.put(categoryID, cModel);
                    }  
                    categoriesList = gModel.getCategories();
                    if (!categoriesList.contains(cModel))
                        categoriesList.add(cModel);
                }
                classificationsMap = cModel.getClassifications();
                classificationsMap.put(is.copyObject(), cData);
            }
        }
        unclassifiedImages.removeAll(classifiedImages);
        results.addAll(groupMap.values());
        results.addAll(orphanMap.values());
        results.addAll(unclassifiedImages);
        return results;
    }

    
    /** 
     * Fill in a Category-CategoryGroup hierarchy.
     * 
     * @param categories List of  remote {@link Category} objects.
     * @return List of {@link DataObject}.
     */
    public static List fillICGHierarchyAvailable(List categories, int imgID, 
                                            int userID)
    {
        ArrayList results = new ArrayList(), finalResults = new ArrayList();
        Iterator i = categories.iterator(), j;
        Category category;
        CategoryGroup group;
        CategoryGroupData groupData = null;
        CategoryData catData;
        Classification classif;
        Integer groupID;
        HashMap groupsMap = new HashMap(), categoriesMap = new HashMap();
        HashMap usedGroups = new HashMap();
        boolean in;
        while (i.hasNext()) {
            in = false;
            category = (Category) i.next();
            group = category.getCategoryGroup();
            j = category.getClassificationList().iterator();
            while (j.hasNext()) {
                classif = (Classification) j.next();
                if (classif.isValid().equals(Boolean.TRUE) && 
                        classif.getModuleExecution().getExperimenter().getID() 
                        == userID && classif.getImage().getID() == imgID) {
                    in = true;
                    if (group != null) 
                        usedGroups.put(new Integer(group.getID()), group);
                }
            }
            if (!in) {  //the image has been classified in the category.
               if (group != null && 
                       !usedGroups.containsKey(new Integer(group.getID()))) {
                   groupID = new Integer(group.getID());
                   groupData = (CategoryGroupData) groupsMap.get(groupID);
                   if (groupData == null) {
                       groupData = createCategoryGroupData(group);
                       groupsMap.put(groupID, groupData);
                   }
                   //Create the category
                   catData = createCategoryData(category, categoriesMap);
                   catData.setCategoryGroup(groupData);
                   groupData.getCategories().add(catData);
                   results.add(groupData);
               } else { //orphan category, shouldn't happen
                   catData = createCategoryData(category, categoriesMap);
                   results.add(catData);
               }
            } 
        }
        //Copy all elements of results into finalResults
        finalResults.addAll(results);
        i = results.iterator();
        Object userObject;
        while (i.hasNext()) {
            userObject = i.next();
            if (userObject instanceof CategoryGroupData) {
                groupData = (CategoryGroupData) userObject;
                if (usedGroups.containsKey(new Integer(groupData.getID())))
                    finalResults.remove(groupData);
            }
        }
        return finalResults;
    }
 
}

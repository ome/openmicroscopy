/*
 * org.openmicroscopy.shoola.env.data.map.CategoryMapper
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
import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.ds.st.Classification;
import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.CategorySummary;
import org.openmicroscopy.shoola.env.data.model.ClassificationData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

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
public class CategoryMapper
{

    public static final float    CONFIDENCE = 1.0f;
    
    public static final Float    CONFIDENCE_OBJ = new Float(1.0f);
    
    /**
     * Build a basic criteria for {@link Category} or {@link CategoryGroup}.
     * 
     * @param id    attribute id corresponding to the id of the Category or
     *              CategoryGroup.
     * 
     * @return Corresponding criteria.
     */
    public static Criteria buildBasicCriteria(int id, int userID)
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("Description");
        c.addWantedField("module_execution");
        c.addWantedField("module_execution", "experimenter");
        //Specify which fields we want for the owner.
        c.addWantedField("module_execution.experimenter", "id");
        if (id != -1) c.addFilter("id", new Integer(id));
        if (userID != -1) 
            c.addFilter("module_execution.experimenter_id", 
                    new Integer(userID));
        return c;
    }
    
    /**
     * Build a criteria to retrieve {@link CategoryGroup} objects.
     * 
     * @return Corresponding criteria.
     */
    public static Criteria buildCategoryGroupCriteria(int groupID, int userID)
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("Description");
        c.addWantedField("CategoryList");
        //c.addWantedField("module_execution");
        
        c.addWantedField("CategoryList", "Name");
        c.addWantedField("CategoryList", "ClassificationList");
        
        c.addWantedField("CategoryList.ClassificationList", "Valid");
        c.addWantedField("CategoryList.ClassificationList", "image");
        
        c.addWantedField("CategoryList", "module_execution");
        c.addWantedField("CategoryList.module_execution", "experimenter");

        
        c.addWantedField("CategoryList.ClassificationList", "module_execution");
        
        c.addWantedField("CategoryList.ClassificationList.module_execution", 
                            "experimenter");
        //Specify which fields we want for the owner.
        //group mex
       // c.addWantedField("module_execution", "experimenter");

        if (groupID != -1) c.addFilter("id", new Integer(groupID));
        if (userID != -1) 
            c.addFilter("module_execution.experimenter_id", 
                    new Integer(userID));
        return c;
    }
    
    public static Criteria buildExtendedCategoryGroupCriteria(int groupID, 
                                                            int userID)
    {
        Criteria c = buildCategoryGroupCriteria(groupID, userID);
        c.addWantedField("Name");
        c.addWantedField("Description");
        c.addWantedField("CategoryList");
        //c.addWantedField("module_execution");
        
        c.addWantedField("CategoryList", "Name");
        c.addWantedField("CategoryList", "ClassificationList");
        
        c.addWantedField("CategoryList.ClassificationList", "Valid");
        c.addWantedField("CategoryList.ClassificationList", "image");
        
        c.addWantedField("CategoryList", "module_execution");
        c.addWantedField("CategoryList.module_execution", "experimenter");

        
        c.addWantedField("CategoryList.ClassificationList", "module_execution");
        
        c.addWantedField("CategoryList.ClassificationList.module_execution", 
                            "experimenter");
        //Specify which fields we want for the owner.
        //group mex
        //c.addWantedField("module_execution", "experimenter");

        if (groupID != -1) c.addFilter("id", new Integer(groupID));
        return c;
    }
    
    /** Build the criteria for the retrieveCategories() method. */
    public static Criteria buildCategoryWithClassificationsCriteria(int groupID,
                                                    int userID)
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("CategoryGroup");
        
        c.addWantedField("ClassificationList");
        c.addWantedField("ClassificationList", "Valid");
        c.addWantedField("ClassificationList", "image");
        //Fields we want for the images
        c.addWantedField("ClassificationList", "module_execution");
        
        c.addWantedField("ClassificationList.module_execution", 
                            "experimenter");
        //Specify which fields we want for the owner.
        
       // c.addWantedField("module_execution");
       // c.addWantedField("module_execution", "experimenter");
        //Specify which fields we want for the owner.
        
        if (groupID != -1) 
            c.addFilter("CategoryGroup", "!=", new Integer(groupID));
        if (userID != -1) 
            c.addFilter("module_execution.experimenter_id", 
                    new Integer(userID));
        return c;
    }
    
    /**
     * Build a criteria to retrieve all information on the specified
     * {@link Category}.
     * 
     * @param id    id of the category.
     * 
     * @return Corresponding criteria.
     */
    public static Criteria buildCategoryCriteria(int id)
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("Description");
        c.addWantedField("CategoryGroup");
        //Wanted field for CategoryGroup.
        c.addWantedField("CategoryGroup", "Name");
        c.addWantedField("CategoryGroup", "Description");
        if (id != -1) c.addFilter("id", new Integer(id));
        return c;
    }
    
    /**
     * Build a criteria to retrieve the minimum information on the specified 
     * {@link Classification} object.
     * 
     * @param id    id of classification.
     * @return
     */
    public static Criteria buildBasicClassificationCriteria(int id)
    {
        Criteria c = new Criteria();
        c.addWantedField("Valid");
        c.addFilter("id", new Integer(id));
        return c;
    }
    
    
    /** 
     * Build the criteria to retrieve the classification attribute associated
     * to the specified image and category.
     * 
     * @param imageID       id of the image.
     * @param categoryID    id of the category.
     * 
     * @return Corresponding criteria.
     */
    public static Criteria buildClassificationCriteria(int imageID, int catID)
    {
        Criteria c = new Criteria();
        c.addWantedField("Valid");
        c.addWantedField("Category");
        c.addFilter("image_id", new Integer(imageID));
        c.addFilter("Category", new Integer(catID));
        return c;
    }
    
    /** 
     * Criteria to retrieve all the images within this classification.
     * 
     * @param categoryID    ID of the category.
     * 
     * @return Corresponding criteria.
     */
    public static Criteria buildClassificationCriteria(int categoryID)
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("Description");
        c.addWantedField("CategoryGroup");
        //Wanted field for CategoryGroup.
        c.addWantedField("CategoryGroup", "Name");
        c.addWantedField("CategoryGroup", "Description");
        c.addWantedField("CategoryGroup", "CategoryList");
        c.addWantedField("CategoryGroup.CategoryList", "Name");
        c.addWantedField("CategoryGroup.CategoryList", "ClassificationList");
        c.addWantedField("CategoryGroup.CategoryList.ClassificationList", 
                            "Valid");
        c.addWantedField("CategoryGroup.CategoryList.ClassificationList", 
                            "image");
        c.addWantedField("CategoryGroup.CategoryList.ClassificationList.image", 
                            "name");
        c.addWantedField("CategoryGroup.CategoryList.ClassificationList", 
                            "module_execution");
        c.addWantedField(
            "CategoryGroup.CategoryList.ClassificationList.module_execution", 
        "experimenter");
        //Fields we want for the images
        c.addWantedField("ClassificationList.image", "name");
        
        c.addWantedField("ClassificationList");
        c.addWantedField("ClassificationList", "Confidence");
        c.addWantedField("ClassificationList", "Valid");
        c.addWantedField("ClassificationList", "image");
        //Fields we want for the images
        c.addWantedField("ClassificationList.image", "name");
        c.addWantedField("ClassificationList.image", "created");
        //Specify which fields we want for the pixels.
        c.addWantedField("ClassificationList.image", "default_pixels");
        c.addFilter("id", new Integer(categoryID));
        return c;
    }
    
    /**
     * Given a list of {@link CategoryGroup} build the corresponding list of
     * {@link CategoryGroupData} objects
     * 
     * @param l         list of {@link CategoryGroup}s.
     * @param result    list of {@link CategoryGroupData}s.
     * @param userID    ID of the current user.
     */
    public static void fillCategoryGroup(List l, List result, int userID)
    {
        Map cMap = new HashMap();
        Iterator i = l.iterator();
        CategoryGroup cg;
        CategoryGroupData cgd;
        CategorySummary cs;
        Category c;
        Iterator j;
        List categories; 
        Integer id;
        while (i.hasNext()) {
            cg = (CategoryGroup) i.next();
            //if (cg.getModuleExecution().getExperimenter().getID() == userID) {
            cgd = buildCategoryGroup(cg);
            categories = new ArrayList();
            if (cg.getCategoryList() != null) {
                j = cg.getCategoryList().iterator();
                while (j.hasNext()) {
                    c = (Category) j.next();
                    if (c.getModuleExecution().getExperimenter().getID() 
                            == userID) {
                        id = new Integer(c.getID());
                        cs = (CategorySummary) cMap.get(id);
                        if (cs == null) {
                            cs = createCategorySummary(c, userID);
                            cMap.put(id, cs);
                        }
                        //Add the categories
                        categories.add(cs);
                    }
                }
            }
            cgd.setCategories(categories);
            result.add(cgd);
            //}
        }
    }
    
    public static CategoryGroupData fillCategoryGroup(CategoryGroup group, 
                                                    int userID)
    {
        //if (group.getModuleExecution().getExperimenter().getID() != userID) 
        //    return null;
        CategoryGroupData data = buildCategoryGroup(group);
        List categories = new ArrayList();
        if (group.getCategoryList() != null) {
            Iterator j = group.getCategoryList().iterator();
            Category c;
            Integer id;
            Map cMap = new HashMap();
            CategorySummary cs;
            while (j.hasNext()) {
                c = (Category) j.next();
                if (c.getModuleExecution().getExperimenter().getID() 
                        == userID) {
                    id = new Integer(c.getID());
                    cs = (CategorySummary) cMap.get(id);
                    if (cs == null) {
                        cs = createCategorySummary(c, userID);
                        cMap.put(id, cs);
                    }
                    //Add the categories
                    categories.add(cs);
                }
            }
        }
        data.setCategories(categories);
        return data;
    }
    
    /**
     * Given a list of {@link Category} build the corresponding list of
     * {@link CategorySummary} objects.
     * 
     * @param l         list of {@link Category}s.
     * @param result    list of {@link CategorySummary}s.
     * @param userID    ID of the current user.
     */
    public static void fillCategoryWithClassifications(List l, List result, 
                                    CategoryGroupData group)
    {
        //Map of ID of the images contained in the group.
        Map ids = new HashMap(); 
        CategorySummary cs;
        Iterator i = group.getCategories().iterator();
        Iterator k;
        Object obj;
        while (i.hasNext()) {
            cs = (CategorySummary) i.next();
            k = cs.getImages().iterator();
            while (k.hasNext()) {
                obj = k.next(); //Integer
                ids.put(obj, obj);
            }  
        }
        i = l.iterator();
        Category c;
        Map categoryMap = new HashMap();
        Integer id;
        while (i.hasNext()) {
            c = (Category) i.next();
            //if (c.getModuleExecution().getExperimenter().getID() == userID) {
            id = new Integer(c.getID());
            cs = (CategorySummary) categoryMap.get(id);
            if (cs == null) {
                cs = createCategorySummary(c, ids);
                if (cs != null) {
                    categoryMap.put(id, cs);
                    result.add(cs);
                }
            } else result.add(cs);
            //}
        }
    }
    
    /** 
     * Fill up a {@link CategoryData} object. 
     * 
     * @param c         original category object.
     * @param model     data object to fill up.     
     */
    public static void fillCategory(Category c, CategoryData model, int userID)
    {
        model.setID(c.getID());
        model.setName(c.getName());
        model.setDescription(c.getDescription());
        model.setCategoryGroup(buildCategoryGroup(c, userID));
        Iterator i = c.getClassificationList().iterator();
        Classification classification;
        ClassificationData cData;
        ImageSummary is;
        float f;
        Map map = new HashMap();
        while (i.hasNext()) {
            classification = (Classification) i.next();
            f = CONFIDENCE;
            if (classification.getConfidence() != null)
                f =  classification.getConfidence().floatValue();
            //filter doesn't work when applies at the criteria level.
            if (classification.isValid() != null && 
                    classification.isValid().equals(Boolean.TRUE)) {
                cData = new ClassificationData(classification.getID(), f);
                is = ImageMapper.buildImageSummary(classification.getImage(), 
                                                    new ImageSummary());
                map.put(is, cData); 
            }
        }
        model.setClassifications(map);
    }

    /** 
     * Fill up a {@link CategoryGroupData} object. 
     * 
     * @param group        original {@link CategoryGroup} object.    
     */
    public static CategoryGroupData buildCategoryGroup(CategoryGroup group)
    {
        if (group == null) 
            throw new NullPointerException("CategoryGroup cannot be null");
        CategoryGroupData data = new CategoryGroupData();
        data.setID(group.getID());
        data.setName(group.getName());
        data.setDescription(group.getDescription());
        return data;
    }
    
    /** 
     * Fill up a {@link CategoryData} object. 
     * 
     * @param category  original {@link Category} object.    
     * @param gModel    {@link CategoryGroupData} object containing the 
     *                  {@link CategoryData} object.    
     */
    public static CategoryData buildCategoryData(Category category, 
                                            CategoryGroupData gModel)
    {
        if (category == null || gModel == null) 
            throw new NullPointerException("Category or CategoryGroupData" +
                    "cannot be null");
        CategoryData data = new CategoryData();
        data.setID(category.getID());
        data.setName(category.getName());
        data.setDescription(category.getDescription());
        data.setCategoryGroup(gModel);
        return data;
    }
    
    /**
     * Link the {@link ImageSummary} objects (keys of the map) to the
     * corresponding annotation.
     * 
     * @param map           classifications map.
     * @param annotations   list of annotations.
     * @param userID        id of the current user.
     */
    public static void fillImageAnnotationInCategory(Map map, List annotations)
    {
        Map ids = AnnotationMapper.reverseListImageAnnotations(annotations);
        Iterator i = map.keySet().iterator();
        ImageSummary is;
        while (i.hasNext()) {
            is = (ImageSummary) i.next();
            is.setAnnotation(AnnotationMapper.fillImageAnnotation(
                    (ImageAnnotation) ids.get(new Integer(is.getID()))));
        }
    }
    
    /** Create a {@link CategorySummary} object. */
    private static CategorySummary createCategorySummary(Category c, int userID)
    {
        CategorySummary cs = new CategorySummary(c.getID(), c.getName());
        List imageIDS = cs.getImages();
        Iterator k = c.getClassificationList().iterator();
        Classification classification;
        while (k.hasNext()) {
            classification = (Classification) k.next();
            if (classification.isValid().booleanValue() &&
               classification.getModuleExecution().getExperimenter().getID() 
                    == userID) {
                imageIDS.add(new Integer(classification.getImage().getID())); 
            }
        }
        return cs;
    }
    
    /** Create a {@link CategorySummary} object. */
    private static CategorySummary createCategorySummary(Category c, Map ids)
    {
        Iterator k = c.getClassificationList().iterator();
        Classification classification;
        
        while (k.hasNext()) {
            classification = (Classification) k.next();
            if (classification.isValid().booleanValue() && 
               ids.containsKey(new Integer(classification.getImage().getID())))
             return null;
        }
       return new CategorySummary(c.getID(), c.getName());
    }
    
    private static CategoryGroupData buildCategoryGroup(Category c, int userID)
    {
        CategoryGroupData groupData = new CategoryGroupData();
        CategoryGroup group = c.getCategoryGroup();
        groupData.setID(group.getID());
        groupData.setName(group.getName());
        List categories = groupData.getCategories();
        List l = group.getCategoryList();
        Iterator i = l.iterator();
       
        while (i.hasNext())
            categories.add(createCategorySummary((Category) i.next(), userID));
        return groupData;
    }
    
}

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
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.ClassificationData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

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
public class CategoryMapper
{

    public static final float    CONFIDENCE = 1.0f;
    
    public static final Float    CONFIDENCE_OBJ = new Float(1.0f);
    
    /**
     * Fields required for Category
     * @param c existing criteria.
     */
    private static void fieldsForCategory(Criteria c)
    {
        c.addWantedField("Name");
        c.addWantedField("Description");  
        c.addWantedField("CategoryGroup");
        c.addWantedField("CategoryGroup", "Name");
        c.addWantedField("CategoryGroup", "Description");
        c.addWantedField("ClassificationList");
        //wanted fields for ClassificationList
        c.addWantedField("ClassificationList", "Valid");
        
        c.addWantedField("ClassificationList", "Confidence");
        c.addWantedField("ClassificationList", "image");
        
        //May add filtering
        c.addWantedField("ClassificationList", "module_execution");
        c.addWantedField("ClassificationList.module_execution", 
                            "experimenter");

        //wanted fields for images
        c.addWantedField("ClassificationList.image", "name");
        c.addWantedField("ClassificationList.image", "created");
        c.addWantedField("ClassificationList.image", "default_pixels");
        PixelsMapper.fieldsForPixels(c, 
                "ClassificationList.image.default_pixels");  
    }
    
    /** 
     * Fill up a {@link CategoryData} object given a {@link Category} object. 
     * 
     * @param gData         The Data object containing the {@link CategoryData}. 
     * @param proto         The Data object to fill up.     
     * @param c             The remote object.
     * @return See above.
     */
    private static CategoryData fillSimpleCategory(CategoryGroupData gData, 
                            CategoryData proto, Category c)
    {
        CategoryData model = (CategoryData) proto.makeNew();
        model.setID(c.getID());
        model.setName(c.getName());
        model.setDescription(c.getDescription());
        model.setCategoryGroup(gData);
        model.setClassifications(new HashMap());
        return model;
    }

    /** 
     * Fill up a {@link CategoryData} object given a {@link Category} object. 
     * 
     * @param gData         The Data object containing the {@link CategoryData}. 
     * @param proto         The Data object to fill up.     
     * @param c             The remote object.
     * @param annotations   The map with the image Annotations.
     * @return See above.
     */
    private static CategoryData fillCategory(CategoryGroupData gData, 
                            CategoryData proto, Category c, Map annotations)
    {
        CategoryData model = (CategoryData) proto.makeNew();
        model.setID(c.getID());
        model.setName(c.getName());
        model.setDescription(c.getDescription());
        model.setCategoryGroup(gData);
        Iterator i = c.getClassificationList().iterator();
        Classification classification;
        ClassificationData cData;
        ImageSummary is;
        Map map = new HashMap();
        AnnotationData annotation;
        while (i.hasNext()) {
            classification = (Classification) i.next();
            //filter doesn't work when applies at the criteria level.
            if (classification.isValid() != null && 
                    classification.isValid().equals(Boolean.TRUE)) {
                cData = buildClassificationData(classification);
                is = ImageMapper.buildImageSummary(classification.getImage(), 
                        null);
                annotation = AnnotationMapper.fillImageAnnotation(
                 (ImageAnnotation) annotations.get(new Integer(is.getID())));
                is.setAnnotation(annotation);
                map.put(is, cData); 
            }
        }
        model.setClassifications(map);
        return model;
    }
    
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
    public static Criteria buildCategoryGroupCriteria(int groupID, int userID, 
            boolean withImages)
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("Description");
        c.addWantedField("CategoryList");

        //wanted fields for CategoryList
        c.addWantedField("CategoryList", "Name");
        c.addWantedField("CategoryList", "Description");
        c.addWantedField("CategoryList", "module_execution");
        //May add filter
        c.addWantedField("CategoryList.module_execution", "experimenter");      
        
        //wanted fields for CategoryList
        c.addWantedField("CategoryList", "ClassificationList");
        //wanted fields for ClassificationList
        c.addWantedField("CategoryList.ClassificationList", "Valid");
        
        c.addWantedField("CategoryList.ClassificationList", "Confidence");

        c.addWantedField("CategoryList.ClassificationList", "module_execution");
        c.addWantedField("CategoryList.ClassificationList.module_execution", 
                            "experimenter");

        //wanted fields for images
        if (withImages) {
            c.addWantedField("CategoryList.ClassificationList", "image");
            c.addWantedField("CategoryList.ClassificationList.image", "name");
            c.addWantedField("CategoryList.ClassificationList.image", 
                            "created");
            c.addWantedField("CategoryList.ClassificationList.image", 
                            "default_pixels");
            PixelsMapper.fieldsForPixels(c, 
                    "CategoryList.ClassificationList.image.default_pixels"); 
        }
        if (groupID != -1) c.addFilter("id", new Integer(groupID));
        if (userID != -1) 
          c.addFilter("module_execution.experimenter_id", new Integer(userID));
        return c;
    }
    
    /**
     * Build a criteria to retrieve {@link Category} objects.
     * 
     * @return Corresponding criteria.
     */
    public static Criteria buildCategoryCriteria(int categoryID, int userID)
    {
        Criteria c = new Criteria();
        fieldsForCategory(c);   
        if (categoryID != -1) c.addFilter("id", new Integer(categoryID));
        if (userID != -1) c.addFilter("module_execution.experimenter_id", 
                                    new Integer(userID));
        return c;
    }
    
    /** Retrieve the minimal information about the specified CategoryGroup. */
    public static Criteria buildBasicCategoryGroupCriteria(int groupID, 
            int userID)
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("Description");
        if (groupID != -1) c.addFilter("id", new Integer(groupID));
        if (userID != -1) c.addFilter("module_execution.experimenter_id", 
                                    new Integer(userID));
        return c;
    }
    
    /** Build the criteria for the retrieveCategories() method. */
    public static Criteria buildCategoryWithClassificationsCriteria(
                        int groupID, int userID)
    {
        Criteria c = new Criteria();
        fieldsForCategory(c);
        if (groupID != -1) 
            c.addFilter("CategoryGroup", "!=", new Integer(groupID));
        if (userID != -1) c.addFilter("module_execution.experimenter_id", 
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
    public static Criteria buildClassificationCriteria(int imgID, int catID)
    {
        Criteria c = new Criteria();
        c.addWantedField("Valid");
        c.addWantedField("Confidence");
        c.addWantedField("Category");
        //Fields we want for the images.
        c.addWantedField("image");
        if (imgID != -1) c.addFilter("image_id", new Integer(imgID));
        if (catID != -1) c.addFilter("Category", new Integer(catID));
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
    public static List fillCategoryGroup(CategoryGroupData gProto, 
            CategoryData cProto, List l, int userID, List isAnnotations)
    {
        Map imgAnnotated = 
            AnnotationMapper.reverseListImageAnnotations(isAnnotations);
        List results = new ArrayList();
        Map cMap = new HashMap();
        Iterator i = l.iterator(), j;
        CategoryGroup cg;
        Category c;
        CategoryGroupData cgd;
        CategoryData data;
        List categories; 
        Integer id;
        while (i.hasNext()) {
            cg = (CategoryGroup) i.next();
            cgd = buildCategoryGroup(gProto, cg);
            categories = new ArrayList();
            if (cg.getCategoryList() != null) {
                j = cg.getCategoryList().iterator();
                while (j.hasNext()) {
                    c = (Category) j.next();
                    if (c.getModuleExecution().getExperimenter().getID() 
                            == userID) {
                        id = new Integer(c.getID());
                        data = (CategoryData) cMap.get(id);
                        if (data == null) {
                            data = fillCategory(cgd, cProto, c, imgAnnotated);
                            cMap.put(id, data);
                        }
                        //Add the categories to the list
                        categories.add(data);
                    }
                }
            }
            cgd.setCategories(categories);
            results.add(cgd);
        }
        return results;
    }
    
    /**
     * Given a list of {@link CategoryGroup} build the corresponding list of
     * {@link CategoryGroupData} objects
     * 
     * @param l         list of {@link CategoryGroup}s.
     * @param result    list of {@link CategoryGroupData}s.
     * @param userID    ID of the current user.
     */
    public static List fillCategoryGroup(CategoryGroupData gProto, 
            CategoryData cProto, List l, int userID)
    {
        List results = new ArrayList();
        Map cMap = new HashMap();
        Iterator i = l.iterator(), j;
        CategoryGroup cg;
        Category c;
        CategoryGroupData cgd;
        CategoryData data;
        List categories; 
        Integer id;
        while (i.hasNext()) {
            cg = (CategoryGroup) i.next();
            cgd = buildCategoryGroup(gProto, cg);
            categories = new ArrayList();
            if (cg.getCategoryList() != null) {
                j = cg.getCategoryList().iterator();
                while (j.hasNext()) {
                    c = (Category) j.next();
                    if (c.getModuleExecution().getExperimenter().getID() 
                            == userID) {
                        id = new Integer(c.getID());
                        data = (CategoryData) cMap.get(id);
                        if (data == null) {
                            data = buildCategoryData(cProto, c, cgd);
                            cMap.put(id, data);
                        }
                        //Add the categories to the list
                        categories.add(data);
                    }
                }
            }
            cgd.setCategories(categories);
            results.add(cgd);
        }
        return results;
    }
    
    /**
     * Given a list of {@link CategoryGroup} build the corresponding list of
     * {@link CategoryGroupData} objects
     * 
     * @param l         list of {@link CategoryGroup}s.
     * @param result    list of {@link CategoryGroupData}s.
     * @param userID    ID of the current user.
     */
    public static List fillSimpleCategoryGroup(CategoryGroupData gProto, 
            CategoryData cProto, List l, int userID)
    {
        List results = new ArrayList();
        Map cMap = new HashMap();
        Iterator i = l.iterator(), j;
        CategoryGroup cg;
        Category c;
        CategoryGroupData cgd;
        CategoryData data;
        List categories; 
        Integer id;
        while (i.hasNext()) {
            cg = (CategoryGroup) i.next();
            cgd = buildCategoryGroup(gProto, cg);
            categories = new ArrayList();
            if (cg.getCategoryList() != null) {
                j = cg.getCategoryList().iterator();
                while (j.hasNext()) {
                    c = (Category) j.next();
                    if (c.getModuleExecution().getExperimenter().getID() 
                            == userID) {
                        id = new Integer(c.getID());
                        data = (CategoryData) cMap.get(id);
                        if (data == null) {
                            data = fillSimpleCategory(cgd, cProto, c);
                            cMap.put(id, data);
                        }
                        //Add the categories to the list
                        categories.add(data);
                    }
                }
            }
            cgd.setCategories(categories);
            results.add(cgd);
        }
        return results;
    }
    
    /**
     * Fill up a {@link CategoryGroupData} object.
     * 
     * @param gProto        The {@link CategoryGroupData} prototype.
     * @param cProto        The {@link CategoryData} prototype.
     * @param group         The remote {@link CategoryGroup} object.
     * @param userID        The user's id.
     * @param annotation    List of {@link ImageAnnotation}s.
     * @return See above.
     */
    public static CategoryGroupData fillCategoryGroup(CategoryGroupData gProto, 
            CategoryData cProto, CategoryGroup group, int userID, 
            List annotation)
    {
        if (group == null) return null;
        List l = new ArrayList();
        l.add(group);
        List results = fillCategoryGroup(gProto, cProto, l, userID, annotation);
        if (results.size() == 0) return null;
        return (CategoryGroupData) results.get(0);
    }
    
    /**
     * Fill up the {@link CategoryData}.
     * 
     * @param category      The remote {@link Category} object.
     * @param userID        The user's id.
     * @param annotation    List of {@link ImageAnnotation}s.
     * @return
     */
    public static CategoryData fillCategoryTree(Category category, 
            List annotation)
    {
        if (category == null) return null;
        Map imgAnnotated = 
            AnnotationMapper.reverseListImageAnnotations(annotation);
        CategoryGroupData cgd = buildCategoryGroup(new CategoryGroupData(), 
                            category.getCategoryGroup());
        return fillCategory(cgd, new CategoryData(), category, imgAnnotated);
    }
    
    /**
     * Given a list of {@link Category} build the corresponding list of
     * {@link CategoryData} objects.
     * 
     * @param gData         The {@link CategoryGroupData} prototype.
     * @param cProto        The {@link CategoryData} prototype.
     * @param l             List of {@link Category}s.
     * @param annotation    List of {@link ImageAnnotation}s.
     */
    public static List fillCategoryWithClassifications(CategoryGroupData gData,
                CategoryData cProto, List l, List annotation)
    {
        List results = new ArrayList();
        Map imgAnnotated = 
            AnnotationMapper.reverseListImageAnnotations(annotation);
        //Map of ID of the images contained in the group.
        Map ids = new HashMap(); 
        CategoryData data;
        Iterator i = gData.getCategories().iterator();
        Iterator k;
        Object obj;
        while (i.hasNext()) {
            data = (CategoryData) i.next();
            k = data.getImages().iterator();
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
            id = new Integer(c.getID());
            data = (CategoryData) categoryMap.get(id);
            if (data == null) {
                data = fillCategory(gData, cProto, c, imgAnnotated);
                if (data != null) {
                    categoryMap.put(id, data);
                    results.add(data);
                }
            } else results.add(data);
        }
        return results;
    }

    /**
     * Builds a {@link ClassficationData} object given a 
     * {@link Classfication} object.
     * 
     * @param c The remote {@link Classfication} object.
     * @return See above.
     */
    public static ClassificationData buildClassificationData(Classification c)
    {
        float f = CONFIDENCE;
        if (c.getConfidence() != null) f = c.getConfidence().floatValue();
        return new ClassificationData(c.getID(), f);
    }
    
    /** 
     * Fill up a {@link CategoryGroupData} object given a 
     * {@link CategoryGroup} object.  
     * 
     * @param gData The {@link CategoryGroupData} prototype.
     * @param group The remote {@link CategoryGroup} object.  
     * @return See above.  
     */
     public static CategoryGroupData buildCategoryGroup(
            CategoryGroupData gProto, CategoryGroup group)
    {
        if (group == null) 
            throw new NullPointerException("CategoryGroup cannot be null");
        CategoryGroupData data = (CategoryGroupData) gProto.makeNew();
        data.setID(group.getID());
        data.setName(group.getName());
        data.setDescription(group.getDescription());
        return data;
    }
    
     /**
      * Fill up {@link CategoryData} object given a 
      * {@link Category} object.  
      *  
      * @param cProto   The {@link CategoryData} to fill up.
      * @param category The remote object.
      * @param gModel   The {@link CategoryGroupData} object containing the 
      *                 {@link CategoryData}.
      * @return See above.
      */
     public static CategoryData buildCategoryData(CategoryData cProto, 
                             Category category, CategoryGroupData gModel)
     {
         if (category == null) 
             throw new NullPointerException("Category cannot be null");
         CategoryData data = (CategoryData) cProto.makeNew();
         data.setID(category.getID());
         data.setName(category.getName());
         data.setDescription(category.getDescription());
         data.setCategoryGroup(gModel);
         return data;
     }
     
    /**
     * Link the {@link ImageSummary} objects (keys) to the
     * corresponding annotation.
     * 
     * @param map           The classifications map.
     * @param annotations   List of image annotations.
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
    
    /**
     * Given a list of categoryGroups, builds a list of image's IDs.
     * The images have been classified in the specified categoryGroups.
     * 
     * @param categoryGroups    List of {@link CategoryGroup} objects.
     * @return See above
     */
    public static List prepareListImagesID(List categoryGroups)
    {
        Map map = new HashMap();
        Iterator i = categoryGroups.iterator(), j, k;
        List categories, classifications;
        Integer id;
        Classification c;
        while (i.hasNext()) {
            categories = ((CategoryGroup) i.next()).getCategoryList();
            j = categories.iterator();
            while (j.hasNext()) {
                classifications = ((Category) j.next()).getClassificationList();
                k = classifications.iterator();
                while (k.hasNext()) {
                    c = (Classification) k.next();
                    if (c.isValid() != null && c.isValid().equals(Boolean.TRUE))
                    {
                        id = new Integer(c.getImage().getID());
                        map.put(id, id);
                    }
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
     * Given a {@link CategoryGroup} object, builds a list of image's IDs.
     * The images have been classified in the specified categoryGroup.
     * 
     * @param group    The {@link CategoryGroup} objects.
     * @return See above
     */
    public static List prepareListImagesID(CategoryGroup group)
    {
        List l = new ArrayList();
        l.add(group);
        return prepareListImagesID(l);
    }
    
    /**
     * Given a {@link Category} object, builds a list of image's IDs.
     * The images have been classified in the specified category.
     * 
     * @param group    The {@link CategoryGroup} objects.
     * @return See above
     */
    public static List prepareListImagesID(Category category)
    {
        Map map = new HashMap();
        Iterator j, k;
        Integer id;
        Classification c;
        List classifications = category.getClassificationList();
        k = classifications.iterator();
        while (k.hasNext()) {
            c = (Classification) k.next();
            if (c.isValid() != null && c.isValid().equals(Boolean.TRUE)) {
                id = new Integer(c.getImage().getID());
                map.put(id, id);
            }
        }  
        j = map.keySet().iterator();
        List ids = new ArrayList();
        while (j.hasNext()) 
            ids.add(j.next());
        return ids;
    } 
    
}

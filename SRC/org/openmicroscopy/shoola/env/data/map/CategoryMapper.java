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
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.ds.st.Classification;
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
    
    public static Criteria buildBasicCriteria(int id)
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("Description");
        if (id != -1) c.addFilter("id", new Integer(id));
        return c;
    }
    
    public static Criteria buildCategoryGroupCriteria()
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("Description");
        c.addWantedField("CategoryList");
        c.addWantedField("CategoryList", "Name");
        return c;
    }
    
    public static Criteria buildCategoryCriteria(int id)
    {
        Criteria c = new Criteria();
        c.addWantedField("Name");
        c.addWantedField("Description");
        c.addWantedField("CategoryGroup");
        //Wanted field for CategoryGroup.
        c.addWantedField("CategoryGroup", "id");
        c.addWantedField("CategoryGroup", "Name");
        c.addWantedField("CategoryGroup", "Description");
        if (id != -1) c.addFilter("id", new Integer(id));
        return c;
    }
    
    public static Criteria buildBasicClassificationCriteria(int id)
    {
        Criteria c = new Criteria();
        c.addWantedField("Valid");
        c.addFilter("id", new Integer(id));
        return c;
    }
    
    public static Criteria buildClassificationCriteria(int imageID, 
                                                        int categoryID)
    {
        Criteria c = new Criteria();
        c.addWantedField("Valid");
        c.addFilter("image_id", new Integer(imageID));
        c.addFilter("Category", new Integer(categoryID));
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
        c.addWantedField("ClassificationList");
        c.addWantedField("ClassificationList", "Confidence");
        c.addWantedField("ClassificationList", "Valid");
        c.addWantedField("ClassificationList", "image");
        //Fields we want for the images
        c.addWantedField("ClassificationList.image", "id");
        c.addWantedField("ClassificationList.image", "name");
        c.addWantedField("ClassificationList.image", "created");
        //Specify which fields we want for the pixels.
        c.addWantedField("ClassificationList.image", "default_pixels");
        c.addWantedField("ClassificationList.image.default_pixels", "id");
        c.addFilter("id", new Integer(categoryID));
        return c;
    }
    
    public static Criteria buildClassifiedImageCriteria(Number[] imageIDs)
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
        c.addWantedField("image", "id");
        c.addWantedField("image", "name");
        c.addWantedField("image", "created");
        //Specify which fields we want for the pixels.
        c.addWantedField("image", "default_pixels");
        c.addWantedField("image.default_pixels", "id");
        c.addFilter("image_id", "IN", imageIDs);
        //In this case, the filter should work ;-)
        c.addFilter("Valid", Boolean.TRUE);
        return c;
    }

    public static void fillCategoryGroup(List l, List result)
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
            cgd = buildCategoryGroup(cg);
            categories = new ArrayList();
            if (cg.getCategoryList() != null) {
                j = cg.getCategoryList().iterator();
                while (j.hasNext()) {
                    c = (Category) j.next();
                    id = new Integer(c.getID());
                    cs = (CategorySummary) cMap.get(id);
                    if (cs == null) {
                        cs = new CategorySummary(id.intValue(), c.getName());
                        cMap.put(id, cs);
                    }
                    //Add the categories
                    categories.add(cs);
                }
            }
            cgd.setCategories(categories);
            result.add(cgd);
        }
    }
    
    /** Fill up a list of {@link CategorySummary} objects. */
    public static void fillBasicCategory(List l, List result)
    {
        Iterator i = l.iterator();
        Category c;
        while (i.hasNext()) {
            c = (Category) i.next();
            result.add(new CategorySummary(c.getID(), c.getName()));
        }
    }
    
    /** Fill up a {@link CategoryData} object. */
    public static void fillCategory(Category c, CategoryData model)
    {
        model.setID(c.getID());
        model.setName(c.getName());
        model.setDescription(c.getDescription());
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
    
    /** Build the all classification graph. */
    public static void fillClassifications(List classifications, 
                                        Object[] result, List imagesID)
    {
        Iterator i = classifications.iterator();
        Classification classification;
        Category category;
        CategoryGroup group;
        Image image;
        ClassificationData cData;
        CategoryData cModel;
        CategoryGroupData gModel;
        ImageSummary is;
        Map imageMap = new HashMap(), categoryMap = new HashMap(),
            groupMap = new HashMap();
        List groups = new ArrayList();
        float f;
        Integer categoryID, groupID;
        while (i.hasNext()) {
            classification = (Classification) i.next();
            f = CONFIDENCE;
            if (classification.getConfidence() != null)
                f =  classification.getConfidence().floatValue();
            image = classification.getImage();
            cData = new ClassificationData(classification.getID(), f);
            //remove image from list
            imagesID.remove(new Integer(image.getID()));
            is = ImageMapper.buildImageSummary(image, new ImageSummary());
            imageMap.put(is, cData);
            
            category = classification.getCategory();
            group = category.getCategoryGroup();
            groupID = new Integer(group.getID());
            gModel = (CategoryGroupData) groupMap.get(groupID);
            //Create CategoryGroupData
            if (gModel == null) {
                gModel = buildCategoryGroup(group);
                groupMap.put(groupID, gModel);
                groups.add(gModel);
            }
            ///Create CategoryData
            categoryID = new Integer(category.getID());
            cModel = (CategoryData) categoryMap.get(categoryID);
            if (cModel == null) {
                cModel = buildCategoryData(category, gModel);
                categoryMap.put(categoryID, cModel);
            }  
        }
        result[0] = imagesID;
        result[1] = groups;
    }
    
    private static CategoryGroupData buildCategoryGroup(CategoryGroup group)
    {
        CategoryGroupData data = new CategoryGroupData();
        data.setID(group.getID());
        data.setName(group.getName());
        data.setDescription(group.getDescription());
        return data;
    }
    
    private static CategoryData buildCategoryData(Category category, 
                                            CategoryGroupData gModel)
    {
        CategoryData data = new CategoryData();
        data.setID(category.getID());
        data.setName(category.getName());
        data.setDescription(category.getDescription());
        data.setCategoryGroup(gModel);
        return data;
    }
    
}

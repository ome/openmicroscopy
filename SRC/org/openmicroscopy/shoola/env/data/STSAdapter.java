/*
 * org.openmicroscopy.shoola.env.data.STSAdapter
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

package org.openmicroscopy.shoola.env.data;

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Attribute;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Feature;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.dto.SemanticType;
import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;
import org.openmicroscopy.ds.st.Classification;
import org.openmicroscopy.ds.st.DatasetAnnotation;
import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.shoola.env.data.map.AnnotationMapper;
import org.openmicroscopy.shoola.env.data.map.CategoryMapper;
import org.openmicroscopy.shoola.env.data.map.STSMapper;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.ClassificationData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/** 
 *  @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @author <br>Jeff Mellen &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:jeffm@alum.mit.edu">
 *                  jeffm@alum.mit.edu</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class STSAdapter
	implements SemanticTypesService
{

    private OMEDSGateway 	      gateway;
    
    public STSAdapter(OMEDSGateway gateway)
    {
        this.gateway = gateway;
    }
    
    /** @see SemanticTypesService#getAvailableGlobalTypes(). */
    public List getAvailableImageTypes()
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria criteria = STSMapper.buildRetrieveTypeCriteria(
        									STSMapper.IMAGE_GRANULARITY);
        return (List) gateway.retrieveListData(SemanticType.class, criteria);
    }

    /**
     * @see SemanticTypesService#countImageAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, List).
     */
    public int countImageAttributes(String typeName, List imageIDList)
        throws DSOutOfServiceException, DSAccessException
    {
        // test to see if the List is all Integers here
        for (Iterator iter = imageIDList.iterator(); iter.hasNext();) {
            if (!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
        
        Integer[] ints = new Integer[imageIDList.size()];
        imageIDList.toArray(ints);
        Criteria c = STSMapper.buildDefaultCountCriteria(typeName, ints);
        return gateway.countData(typeName, c);
    }
    
    /** @see SemanticTypesService#countImageAttributes(String, List). */
    public int countImageAttributes(SemanticType type, List imageIDList)
        throws DSOutOfServiceException, DSAccessException
    {
        return countImageAttributes(type.getName(), imageIDList);
    }

    /**
     * @see SemanticTypesService#retrieveDatasetAttributes(
     * 			org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public List retrieveDatasetAttributes(String typeName, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = STSMapper.buildDefaultRetrieveCriteria(
        						STSMapper.DATASET_GRANULARITY, datasetID);
		return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
    /**
     * @see SemanticTypesService#retrieveDatasetAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, int).
     */
    public List retrieveDatasetAttributes(SemanticType type, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveDatasetAttributes(type.getName(), datasetID);
    }

    /**
     * @see SemanticTypesService#retrieveImageAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, List).
     */
    public List retrieveImageAttributes(String typeName, List imageIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        if (typeName == null || imageIDs == null || imageIDs.size() == 0)
            return null;
        
        // test to see if the List is all Integers here
        for (Iterator iter = imageIDs.iterator(); iter.hasNext();) {
            if(!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
        
        Integer[] ints = new Integer[imageIDs.size()];
        imageIDs.toArray(ints);
        
        Criteria c = STSMapper.buildDefaultRetrieveCriteria(
        					STSMapper.IMAGE_GRANULARITY, ints);
        
        return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
    /**
     * @see SemanticTypesService#retrieveImageAttributes(
     * org.openmicroscopy.ds.dto.SemanticType, List).
     */
    public List retrieveImageAttributes(SemanticType type, List imageIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveImageAttributes(type.getName(), imageIDs);
    }
    
    /**
     * @see SemanticTypesService#retrieveFeatureAttributes(String, String,
     * 														List).
     */
    public List retrieveImageAttributes(String typeName, String childAttribute,
                                        List imageIDs)
        throws DSOutOfServiceException, DSAccessException
    {
        if (typeName == null || imageIDs == null || imageIDs.size() == 0)
            return null;
        
        // test to see if the List is all Integers here
        for (Iterator iter = imageIDs.iterator(); iter.hasNext();) {
            if(!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
        
        Integer[] ints = new Integer[imageIDs.size()];
        imageIDs.toArray(ints);
        
        Criteria c = new Criteria();
        if (childAttribute == null)
            c = STSMapper.buildDefaultRetrieveCriteria(
            				STSMapper.IMAGE_GRANULARITY, ints);
        else
            c = STSMapper.buildDefaultRetrieveCriteria(
            				STSMapper.IMAGE_GRANULARITY, childAttribute,ints);
        
        return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
	/**
     * Returns a list of image classifications.  This distinction is necessary
     * because image classifications must be filtered by the owner dataset of their
     * respective categories.
     * 
     * @param imageIDs The IDs of the images to query.
     * @param datasetID The ID of the dataset of the images.
     * @return See above.
     */
    public List retrieveImageClassifications(List imageIDs, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        /*
        if (imageIDs == null || imageIDs.size() == 0)
            return null;
        
        // test to see if the List is all Integers here
        for (Iterator iter = imageIDs.iterator(); iter.hasNext();) {
            if(!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
        
        Integer[] ints = new Integer[imageIDs.size()];
        imageIDs.toArray(ints);
        
        Criteria c = STSMapper.buildClassificationRetrieveCriteria(ints,datasetID);
        
        return (List) gateway.retrieveListSTSData(CLASSIFICATION_ST_TYPE, c);
        
        */
        //Method will be removed
        return null;
    }

    /**
     * @see SemanticTypesService#retrieveSemanticType(
     * org.openmicroscopy.ds.dto.SemanticType).
     */
    public SemanticType retrieveSemanticType(SemanticType type)
        throws DSOutOfServiceException, DSAccessException
    {
        return retrieveSemanticType(type.getName());
    }
    
    /** @see SemanticTypesService#retrieveSemanticType(String). */
    public SemanticType retrieveSemanticType(String typeName)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = STSMapper.buildRetrieveSingleTypeCriteria(typeName);
        return (SemanticType) gateway.retrieveData(SemanticType.class, c);
    }

    /**
     * Updates attributes that have already been created, regardless of
     * creation method.
     */
    public void updateAttributes(List attributes)
        throws DSOutOfServiceException, DSAccessException
    {
        gateway.updateAttributes(attributes);
    }
    
    public List retrieveAttributesByMEXs(String typeName, List mexes)
    		throws DSOutOfServiceException, DSAccessException
   {
    		
    	if (typeName == null || mexes == null || mexes.size() == 0) return null;
    
        // test to see if the List is all Integers here
        for (Iterator iter = mexes.iterator(); iter.hasNext();) {
            if (!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
    
        Criteria c = STSMapper.buildRetrieveCriteriaWithMEXs(mexes);
        return (List) gateway.retrieveListSTSData(typeName, c);
    }
    
    public List retrieveTrajectoriesByMEXs(List mexes) 
    	   	throws DSOutOfServiceException, DSAccessException
    {
        if (mexes == null || mexes.size() == 0) return null;
    		// test to see if the List is all Integers here
        for (Iterator iter = mexes.iterator(); iter.hasNext();) {
            if (!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }
    
        Criteria c = STSMapper.buildTrajectoryCriteriaWithMEXs(mexes);
        return (List) gateway.retrieveListSTSData("Trajectory", c);
    }
    
    public List retrieveTrajectoryEntriesByMEXs(List mexes) 
       throws DSOutOfServiceException, DSAccessException
    {
        if (mexes == null || mexes.size() ==0) return null;
		// test to see if the List is all Integers here
		for (Iterator iter = mexes.iterator(); iter.hasNext();) {
		    if (!(iter.next() instanceof Number))
		        throw new IllegalArgumentException("Illegal ID type.");
		}
		
		Criteria c = STSMapper.buildTrajectoryEntryCriteriaWithMEXs(mexes);
		return (List) gateway.retrieveListSTSData("TrajectoryEntry", c);
	}
    
    public List retrieveLocationsByFeatureID(List features)
		throws DSOutOfServiceException, DSAccessException
    {
    	
        if (features == null || features.size() == 0)   return null;
        	
        //test to see if the List is all Integers here
        for (Iterator iter = features.iterator(); iter.hasNext();) {
            if (!(iter.next() instanceof Number))
                throw new IllegalArgumentException("Illegal ID type.");
        }

        Criteria c = STSMapper.buildLocationCriteriaWithFeatures(features);
        return (List) gateway.retrieveListSTSData("Location", c);
    }
    
    public List retrieveExtentsByFeatureID(List features)
         throws DSOutOfServiceException, DSAccessException
    {
        if (features == null || features.size() == 0) return null;
    		
         // test to see if the List is all Integers here
        for (Iterator iter = features.iterator(); iter.hasNext();) {
            if (!(iter.next() instanceof Number))
               throw new IllegalArgumentException("Illegal ID type.");
        }
        Criteria c = STSMapper.buildExtentCriteriaWithFeatures(features);
	    return (List) gateway.retrieveListSTSData("Extent", c);
    }
    
    //Annotation
    /** Implemented as specified in {@link SemanticTypesService}. */
    public Map getImageAnnotations(int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = AnnotationMapper.buildImageAnnotationCriteria(imageID);
        List l = (List) gateway.retrieveListSTSData("ImageAnnotation", c);
        TreeMap map = new TreeMap();
        if (l != null || l.size() > 0) 
            AnnotationMapper.fillImageAnnotations(l, map);
        return map;
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public Map getDatasetAnnotations(int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = AnnotationMapper.buildDatasetAnnotationCriteria(datasetID);
        List l = (List) gateway.retrieveListSTSData("DatasetAnnotation", c);
        TreeMap map = new TreeMap();
        if (l != null || l.size() > 0) 
            AnnotationMapper.fillDatasetAnnotations(l, map);
        return map;
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public void updateImageAnnotation(AnnotationData data, int imgID)
        throws DSOutOfServiceException, DSAccessException
    {
        removeImageAnnotation(data);
        createImageAnnotation(imgID, data.getAnnotation(), data.getTheZ(), 
                            data.getTheT()); 
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public void updateDatasetAnnotation(AnnotationData data, int datasetID)
        throws DSOutOfServiceException, DSAccessException
    {
        removeDatasetAnnotation(data);
        createDatasetAnnotation(datasetID, data.getAnnotation());
    }

    /** Implemented as specified in {@link SemanticTypesService}. */
    public void removeImageAnnotation(AnnotationData data)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = AnnotationMapper.buildBasicCriteria(
                STSMapper.GLOBAL_GRANULARITY, data.getID());
        ImageAnnotation ia = 
            (ImageAnnotation) gateway.retrieveSTSData("ImageAnnotation", c);
        ia.setValid(Boolean.FALSE);
        List l = new ArrayList();
        l.add(ia);
        gateway.updateAttributes(l);  
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public void removeDatasetAnnotation(AnnotationData data)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = AnnotationMapper.buildBasicCriteria(
                STSMapper.GLOBAL_GRANULARITY, data.getID());
        DatasetAnnotation da = 
            (DatasetAnnotation) gateway.retrieveSTSData("DatasetAnnotation", c);
        da.setValid(Boolean.FALSE);
        List l = new ArrayList();
        l.add(da);
        gateway.updateAttributes(l); 
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public void createImageAnnotation(int imageID, String annotation, int theZ,
                                        int theT)
        throws DSOutOfServiceException, DSAccessException
    {
        ImageAnnotation retVal = (ImageAnnotation) 
                    createBasicAttribute("ImageAnnotation", 
                            STSMapper.buildBasicCriteria(imageID));
        retVal.setContent(annotation);
        retVal.setValid(Boolean.TRUE);
        if (theZ != AnnotationData.DEFAULT) retVal.setTheZ(new Integer(theZ));
        if (theT != AnnotationData.DEFAULT) retVal.setTheT(new Integer(theT));
        ArrayList l = new ArrayList();
        l.add(retVal);
        gateway.annotateAttributesData(l);
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public void createDatasetAnnotation(int datasetID, String annotation)
        throws DSOutOfServiceException, DSAccessException
    {
        //Create a new Annotation for the user.
        DatasetAnnotation retVal = (DatasetAnnotation) 
                    createBasicAttribute("DatasetAnnotation", 
                            STSMapper.buildBasicCriteria(datasetID));
        retVal.setContent(annotation);
        retVal.setValid(Boolean.TRUE);
        ArrayList l = new ArrayList();
        l.add(retVal);
        gateway.annotateAttributesData(l);
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public List retrieveCategoryGroups()
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = CategoryMapper.buildCategoryGroupCriteria();
        List l = 
            (List) gateway.retrieveListSTSData("CategoryGroup", c);
        List result = new ArrayList();
        if (l != null || l.size() > 0)
            CategoryMapper.fillCategoryGroup(l, result);
        return result;
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public List retrieveCategories()
        throws DSOutOfServiceException, DSAccessException
    {
        //List of categorySummary objects
        List result = new ArrayList();
        Criteria c = CategoryMapper.buildBasicCriteria(-1);
        List l = (List) gateway.retrieveListSTSData("Category", c);
        if (l != null || l.size() > 0)
            CategoryMapper.fillBasicCategory(l, result);
        return result;
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public CategoryData retrieveCategory(int id)
        throws DSOutOfServiceException, DSAccessException
    {
        //Retrieve the specified category.
        Criteria c = CategoryMapper.buildClassificationCriteria(id);
        Category category = (Category) gateway.retrieveSTSData("Category", c);
        CategoryData model = new CategoryData();
        if (category != null) CategoryMapper.fillCategory(category, model);
        return model;
    }

    /** Implemented as specified in {@link SemanticTypesService}. */
    public void createCategoryGroup(CategoryGroupData data)
        throws DSOutOfServiceException, DSAccessException 
    {
        List l = new ArrayList();
        l.add(buildCategoryGroup(data));
        gateway.annotateAttributesData(l);//to have a mex
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public void createCategory(CategoryData data, List images)
        throws DSOutOfServiceException, DSAccessException 
    {
        CategoryGroupData parent = data.getCategoryGroup();
        if (parent == null) return;
        CategoryGroup cg;
        List newAttributes = new ArrayList(), oldAttributes = new ArrayList();
        if (parent.getID() == -1) { //First create a new group
            cg = buildCategoryGroup(parent);
            //To be on the save-side b/c I don't know if the order is kept
            //on the server side
            newAttributes.add(cg);
            gateway.annotateAttributesData(newAttributes);//to have a mex
            newAttributes.removeAll(newAttributes);
        } else {
            Criteria c = STSMapper.buildBasicCriteria(parent.getID());
            cg = (CategoryGroup) gateway.retrieveSTSData("CategoryGroup", c);
        }
        Category category = buildCategory(data, cg);
        newAttributes.add(category);
        gateway.annotateAttributesData(newAttributes);//to have a mex
        newAttributes.removeAll(newAttributes);
        
        Classification classification;
        //Need to add the images one by one ;-)).
        Iterator j = images.iterator();
        Object[] results;
        while (j.hasNext()) {
            results = buildClassification(category, 
                    ((ImageSummary) j.next()).getID());
            classification = (Classification) results[1];
            //only solution to have a max
            if (((Boolean) results[0]).booleanValue()) { 
                newAttributes.add(classification);
                gateway.annotateAttributesData(newAttributes);
                newAttributes.removeAll(newAttributes);
            } else oldAttributes.add(classification);
        }
        if (oldAttributes.size() != 0) //to have a mex
            gateway.updateAttributes(oldAttributes);
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public void updateCategoryGroup(CategoryGroupData data, List toAdd)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = CategoryMapper.buildBasicCriteria(data.getID());
        CategoryGroup cg = 
            (CategoryGroup) gateway.retrieveSTSData("CategoryGroup", c);
        cg.setName(data.getName());
        cg.setDescription(data.getDescription());
        List l = new ArrayList();
        l.add(cg);
        gateway.updateAttributes(l);
        /* Need to discuss the semantic of the operation
        //Prepare the categories to add or remove.
        List categories = data.getCategories();   
        CategorySummary cs;    
        if (toAdd != null) {
            Iterator j = toAdd.iterator();
            while (j.hasNext()) {
                cs = (CategorySummary) j.next();
                if (!categories.contains(cs)) categories.add(cs);
            }
        }

        Iterator k = categories.iterator();
        List newSelection = new ArrayList();
        while (k.hasNext()) {
            cs = (CategorySummary) k.next();
            c = CategoryMapper.buildBasicCriteria(cs.getID());
            newSelection.add(gateway.retrieveSTSData("Category", c));
        }
        */

    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public void updateCategory(CategoryData data, List imgsToRemove, 
                                List imgsToAdd)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = CategoryMapper.buildBasicCriteria(data.getID());
        Category category = 
            (Category) gateway.retrieveSTSData("Category", c);
        category.setName(data.getName());
        category.setDescription(data.getDescription());
        List toUpdate = new ArrayList(), newAttributes = new ArrayList();
        toUpdate.add(category);
        Map classifications = data.getClassifications();    
        ClassificationData cData;
        Classification classification;
        if (imgsToRemove != null) {
            Iterator i = imgsToRemove.iterator();
            while (i.hasNext()) {
                cData = (ClassificationData) classifications.get(i.next());
                c = CategoryMapper.buildBasicClassificationCriteria(
                                cData.getID());
                classification = 
                    (Classification) gateway.retrieveSTSData("Classification", 
                                                                c);
                classification.setValid(Boolean.FALSE);
                toUpdate.add(classification);
            }     
        }
        if (imgsToAdd != null) {
            Iterator j = imgsToAdd.iterator();
            Object[] results;
            while (j.hasNext()) {
                results = buildClassification(category, 
                        ((ImageSummary) j.next()).getID());
                classification = (Classification) results[1];
                if (((Boolean) results[0]).booleanValue()) {
                    newAttributes.add(classification);
                    gateway.annotateAttributesData(newAttributes);
                    newAttributes.removeAll(newAttributes);
                }
                else toUpdate.add(classification);
            }  
        }
        if (newAttributes.size() != 0) //to have a mex
            gateway.annotateAttributesData(newAttributes);
        gateway.updateAttributes(toUpdate);
    }
    
    /** Return a list of CategoryData object. */
    public Object[] retrieveImageClassifications(List imagesID)
        throws DSOutOfServiceException, DSAccessException
    {
        if (imagesID == null)  return null;
        Criteria c = CategoryMapper.buildClassifiedImageCriteria(
                (Number[]) imagesID.toArray());
        List classifications = 
            (List) gateway.retrieveListSTSData("Classification", c);
        //Return an array of length 2
        // first: list of id of the unclassified images  
        // second: List of categoryGroup object.
        if (classifications == null) return null;
        Object[] result = new Object[2];
        CategoryMapper.fillClassifications(classifications , result, imagesID);                            
        return result;
    }

    /** Create a basic attribute. */
    private Attribute createBasicAttribute(String typeName, Criteria c)
        throws DSOutOfServiceException, DSAccessException
    {
        Attribute retVal = gateway.createNewData(typeName);
        String granularity = retVal.getSemanticType().getGranularity();
        //Build the criteria.
        //Criteria c = STSMapper.buildBasicCriteria(objectID);
        //Criteria c = STSMapper.buildCreateNew(granularity, objectID);
        if (granularity.equals(STSMapper.DATASET_GRANULARITY))
            retVal.setDataset((Dataset) gateway.retrieveData(Dataset.class, c));
        else if (granularity.equals(STSMapper.IMAGE_GRANULARITY))
            retVal.setImage((Image) gateway.retrieveData(Image.class, c));
        else if (granularity.equals(STSMapper.FEATURE_GRANULARITY))
            retVal.setFeature((Feature) gateway.retrieveData(Feature.class, c));
        return retVal; 
    }
    
    /** Create a {@link Classification} attribute. */
    private Object[] buildClassification(Category category, int imgID)
        throws DSOutOfServiceException, DSAccessException 
    {
        Object[] results = new Object[2];
        Criteria c = CategoryMapper.buildClassificationCriteria(imgID, 
                            category.getID());
        Classification classification = (Classification) 
                            gateway.retrieveSTSData("Classification",  c);
        results[0] = Boolean.FALSE;
        if (classification == null) {
            results[0] = Boolean.TRUE;
            c = STSMapper.buildBasicCriteria(imgID);
            classification = (Classification) 
                            createBasicAttribute("Classification", c);
            classification.setCategory(category);
            classification.setConfidence(CategoryMapper.CONFIDENCE_OBJ);
        }
        classification.setValid(Boolean.TRUE);
        results[1] = classification;
        return results;
    }
    
    /** Create a CategoryGroup attribute. */
    private CategoryGroup buildCategoryGroup(CategoryGroupData data)
        throws DSOutOfServiceException, DSAccessException 
    {
        CategoryGroup cg = 
            (CategoryGroup) gateway.createNewData("CategoryGroup");
        cg.setName(data.getName());
        cg.setDescription(data.getDescription());
        return cg;
    }
    
    /** Create a CategoryGroup attribute. */
    private Category buildCategory(CategoryData data, CategoryGroup group)
        throws DSOutOfServiceException, DSAccessException 
    {
        Category category = (Category) gateway.createNewData("Category");
        category.setName(data.getName());
        category.setDescription(data.getDescription());
        category.setCategoryGroup(group);
        return category;
    }
    
}

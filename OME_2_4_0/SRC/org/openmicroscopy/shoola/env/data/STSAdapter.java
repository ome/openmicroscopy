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
import java.util.HashMap;
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
import org.openmicroscopy.ds.st.Dimensions;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.ds.st.LogicalChannel;
import org.openmicroscopy.ds.st.RenderingSettings;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.map.AnnotationMapper;
import org.openmicroscopy.shoola.env.data.map.CategoryMapper;
import org.openmicroscopy.shoola.env.data.map.HierarchyMapper;
import org.openmicroscopy.shoola.env.data.map.ImageMapper;
import org.openmicroscopy.shoola.env.data.map.PixelsMapper;
import org.openmicroscopy.shoola.env.data.map.STSMapper;
import org.openmicroscopy.shoola.env.data.map.UserMapper;
import org.openmicroscopy.shoola.env.data.model.AnnotationData;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.CategorySummary;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.env.data.model.ClassificationData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.ui.UserCredentials;

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

    /** Reference to the gateway. */
    private OMEDSGateway        gateway;
    
    /** Reference to the registry. */
    private Registry            registry;
    
    public STSAdapter(OMEDSGateway gateway, Registry registry)
    {
        this.gateway = gateway;
        this.registry = registry;
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
        Criteria c = CategoryMapper.buildCategoryGroupCriteria(-1);
        List l = 
            (List) gateway.retrieveListSTSData("CategoryGroup", c);
        List result = new ArrayList();
        UserCredentials uc = (UserCredentials)
            registry.lookup(LookupNames.USER_CREDENTIALS);
        if (l != null || l.size() > 0)
            CategoryMapper.fillCategoryGroup(l, result, uc.getUserID());
        return result;
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public List retrieveImagesNotInGroup(CategoryGroupData group)
        throws DSOutOfServiceException, DSAccessException
    {
        Iterator i = group.getCategories().iterator();
        Map ids = new HashMap();
        CategorySummary cs;
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
        List images = new ArrayList();
        List userImages = 
            registry.getDataManagementService().retrieveUserImages();
        Iterator j = userImages.iterator();
        ImageSummary is;
        while (j.hasNext()) {
            is = (ImageSummary) j.next();
            if (!ids.containsKey(new Integer(is.getID())))
                images.add(is);
        }
        return images;
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public List retrieveImagesNotInGroup(int groupID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = CategoryMapper.buildCategoryGroupCriteria(groupID);
        CategoryGroup group = 
            (CategoryGroup) gateway.retrieveSTSData("CategoryGroup", c);
        if (group != null) {
            UserCredentials uc = (UserCredentials)
                registry.lookup(LookupNames.USER_CREDENTIALS);
            CategoryGroupData data = 
                CategoryMapper.fillCategoryGroup(group, uc.getUserID());
            if (data != null) return retrieveImagesNotInGroup(data);
        }
        return new ArrayList();
    }

    /** Implemented as specified in {@link SemanticTypesService}. */
    public List retrieveCategoriesNotInGroup(CategoryGroupData group)
        throws DSOutOfServiceException, DSAccessException
    {
        //List of categorySummary objects
        List result = new ArrayList();
        Criteria c = CategoryMapper.buildCategoryWithClassificationsCriteria(
                        group.getID());
        List l = (List) gateway.retrieveListSTSData("Category", c);
        UserCredentials uc = (UserCredentials)
            registry.lookup(LookupNames.USER_CREDENTIALS);
        if (l != null || l.size() > 0)
            CategoryMapper.fillCategoryWithClassifications(l, result, group,
                            uc.getUserID());
        return result;
    }

    /** Implemented as specified in {@link SemanticTypesService}. */
    public CategoryData retrieveCategory(int id)
        throws DSOutOfServiceException, DSAccessException
    {
        return getCategory(id, false);
    }

    /** Implemented as specified in {@link SemanticTypesService}. */
    public CategoryData retrieveCategoryWithIAnnotations(int id)
        throws DSOutOfServiceException, DSAccessException
    {
        return getCategory(id, true);
    }
    
    /** Implemented as specified in {@link SemanticTypesService}. */
    public void createCategoryGroup(CategoryGroupData data)
        throws DSOutOfServiceException, DSAccessException 
    {
        List l = new ArrayList();
        // Build a CategoryGroup object.
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
        //Retrieve the CategoryGroup object.
        Criteria c = STSMapper.buildBasicCriteria(parent.getID());
        cg = (CategoryGroup) gateway.retrieveSTSData("CategoryGroup", c);
        //Build a Category object.
        Category category = buildCategory(data, cg);
        newAttributes.add(category);
        gateway.annotateAttributesData(newAttributes);//to have a mex
        newAttributes.removeAll(newAttributes);
        
        Classification classification;
        //Need to add the images one by one ;-)).
        Iterator j = images.iterator();
        Object[] results;
        while (j.hasNext()) {
            //Build/Retrieve a Classification object
            results = buildClassification(category, 
                    ((ImageSummary) j.next()).getID());
            classification = (Classification) results[1];
            //only solution to have a mex
            if (((Boolean) results[0]).booleanValue()) { 
                newAttributes.add(classification);
                gateway.annotateAttributesData(newAttributes);
                newAttributes.removeAll(newAttributes);
            } else oldAttributes.add(classification);
        }
        if (oldAttributes.size() != 0) //update the existing classification
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

    /** Implemented as specified in {@link SemanticTypesService}. */
    public Object[] retrieveICGHierarchy(List imageSummaries)
        throws DSOutOfServiceException, DSAccessException
    {
        if (imageSummaries == null)
            throw new NullPointerException("List of imageSummaries " +
                    "cannot be null");
        if (imageSummaries.size() == 0)
            throw new IllegalArgumentException("List of imageSummaries " +
                    "cannot be of length 0");
        Iterator i = imageSummaries.iterator();
        ImageSummary is;
        Map map = new HashMap();
        List ids = new ArrayList();
        Integer id;
        while (i.hasNext()) {
            is = (ImageSummary) i.next();
            id = new Integer(is.getID());
            map.put(id, is);
            ids.add(id);
        }
        Criteria c = HierarchyMapper.buildICGHierarchyCriteria(ids);
        List classifications = 
            (List) gateway.retrieveListSTSData("Classification", c);
        
        if (classifications == null) return null;
        UserCredentials uc = (UserCredentials)
                        registry.lookup(LookupNames.USER_CREDENTIALS);
        
        return HierarchyMapper.fillICGHierarchy(classifications, map, 
                                                uc.getUserID());                            
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public ChannelData[] getChannelData(int imageID)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = PixelsMapper.buildPixelChannelComponentCriteria(imageID);
        List ciList = 
            (List) gateway.retrieveListSTSData("PixelChannelComponent", c);
        c = PixelsMapper.buildLogicalChannelCriteria(
                    STSMapper.IMAGE_GRANULARITY, imageID);
        List lcList = (List) gateway.retrieveListSTSData("LogicalChannel", c);
        if (ciList == null || lcList == null) return null;
        return ImageMapper.fillImageChannelData(ciList, lcList);
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public void updateChannelData(ChannelData retVal)
        throws DSOutOfServiceException, DSAccessException
    {
        Criteria c = PixelsMapper.buildLogicalChannelCriteria(
                                STSMapper.GLOBAL_GRANULARITY, retVal.getID());
        LogicalChannel lc = 
                (LogicalChannel) gateway.retrieveSTSData("LogicalChannel", c);
                
        //update the LogicalChannel object
        lc.setExcitationWavelength(new Integer(retVal.getExcitation()));
        lc.setFluor(retVal.getFluor());
        lc.setPhotometricInterpretation(retVal.getInterpretation());
        List l = new ArrayList();
        l.add(lc);
        gateway.updateAttributes(l);
    }

    /** Implemented as specified in {@link DataManagementService}. */
    public PixelsDescription retrievePixels(int pixelsID, int imageID)
            throws DSOutOfServiceException, DSAccessException
    {
        PixelsDescription retVal = new PixelsDescription();
        //Define the criteria by which the object graph is pulled out.
        Criteria c = PixelsMapper.buildPixelsCriteria(imageID);
        
        Image img = (Image) gateway.retrieveData(Image.class, c);
        //Put the server data into the corresponding client object.
        if (img != null)
            PixelsMapper.fillPixelsDescription(img.getDefaultPixels(), retVal);
        //Retrieve the realSize of a pixel.
        c = PixelsMapper.buildPixelsDimensionCriteria(imageID);
        Dimensions pixelDim = 
            (Dimensions) gateway.retrieveSTSData("Dimensions", c);
        if (pixelDim != null)
            PixelsMapper.fillPixelsDimensions(pixelDim, retVal);
        return retVal;
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public RenderingDef retrieveRenderingSettings(int pixelsID, int imageID, 
                                            int pixelType)
        throws DSOutOfServiceException, DSAccessException
    {
        RenderingDef displayOptions = null;
        //Retrieve the user ID.
        UserCredentials uc = (UserCredentials)
                            registry.lookup(LookupNames.USER_CREDENTIALS);
        
        Criteria c = ImageMapper.buildRenderingSettingsCriteria(
                        STSMapper.IMAGE_GRANULARITY, imageID);
        List rsList = 
            (List) gateway.retrieveListSTSData("RenderingSettings", c);
        if (rsList != null && rsList.size() != 0)
            displayOptions = ImageMapper.fillInRenderingDef(rsList, pixelType, 
                                                uc.getUserID());                               
        return displayOptions;
    }
    
    /** Implemented as specified in {@link DataManagementService}. */
    public void saveRenderingSettings(int pixelsID, int imageID,
                                        RenderingDef rDef)
        throws DSOutOfServiceException, DSAccessException
    { 
        Criteria c = ImageMapper.buildRenderingSettingsCriteria(
                            STSMapper.IMAGE_GRANULARITY, imageID);
        List rsList = 
            (List) gateway.retrieveListSTSData("RenderingSettings", c);
        
        UserCredentials uc = (UserCredentials)
                registry.lookup(LookupNames.USER_CREDENTIALS);
        //List of renderingSettings to save in DB.  
        List l = new ArrayList();
        if (rsList != null) {
            List list = ImageMapper.filterList(rsList, uc.getUserID());
            if (list.size() == 0)  // nothing previously saved
                l = saveRSFirstTime(imageID, rDef);
            else l = saveRS(rDef, list);
            gateway.updateAttributes(l);
        }
    }
    
    /** Save the renderingSettings for the very first time. */
    private List saveRSFirstTime(int imageID, RenderingDef rDef)
        throws DSOutOfServiceException, DSAccessException
    {
        List l = new ArrayList();
        ChannelBindings[] channelBindings = rDef.getChannelBindings();
        int z = rDef.getDefaultZ();
        int t = rDef.getDefaultT();
        int model = rDef.getModel();
        QuantumDef qDef = rDef.getQuantumDef();
        int cdStart = qDef.cdStart;
        int cdEnd = qDef.cdEnd;
        int bitResolution = qDef.bitResolution;
        RenderingSettings rs;
        //Need to retrieve the image object.
        //Define the criteria by which the object graph is pulled out.
        Criteria cImage = ImageMapper.buildImageCriteria(imageID);       
        //Load the graph defined by criteria.
        Image image = (Image) gateway.retrieveData(Image.class, cImage);
        Criteria cExp = UserMapper.getUserStateCriteria();
        Experimenter experimenter = gateway.getCurrentUser(cExp);
        for (int i = 0; i < channelBindings.length; i++) {
            rs = (RenderingSettings) 
                gateway.createNewData("RenderingSettings");
            rs.setImage(image);
            rs.setExperimenter(experimenter);
            ImageMapper.fillInRenderingSettings(z, t, model, cdStart, cdEnd,
                            bitResolution, channelBindings[i], rs);
            l.add(rs);
        }  
        return l;
    }

    /** Save the renderingSettings. */
    private List saveRS(RenderingDef rDef, List rsList)
        throws DSAccessException
    {
        List l = new ArrayList();
        ChannelBindings[] channelBindings = rDef.getChannelBindings();
        int z = rDef.getDefaultZ(), t = rDef.getDefaultT(), 
            model = rDef.getModel();
        QuantumDef qDef = rDef.getQuantumDef();
        int cdStart = qDef.cdStart, cdEnd = qDef.cdEnd,
            bitResolution = qDef.bitResolution;
        RenderingSettings rs;
        Iterator j = rsList.iterator();
        int k;
        if (channelBindings.length != rsList.size()) 
            throw new DSAccessException("Data retrieved from DB don't " +
                "match the parameters passed.");
        while (j.hasNext()) {
            rs = (RenderingSettings) j.next();
            k = rs.getTheC().intValue(); // need to add control
            ImageMapper.fillInRenderingSettings(z, t, model, cdStart, cdEnd,
                        bitResolution, channelBindings[k], rs);
            l.add(rs);
        } 
        return l;
    }

    /** Create a basic attribute. */
    private Attribute createBasicAttribute(String typeName, Criteria c)
        throws DSOutOfServiceException, DSAccessException
    {
        Attribute retVal = gateway.createNewData(typeName);
        String granularity = retVal.getSemanticType().getGranularity();
        //Build the criteria.
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
    
    /** 
     * Build a {@link CategoryData} object. 
     * 
     * @param id                id of the category to retrieve.
     * @param groupData         CategoryGroupData containing the category.
     * @param withAnnotation    flag to retrieve or not the annotation 
     *                          associated to an image in the specified 
     *                          category.
     */
    private CategoryData getCategory(int id, boolean withAnnotation)
        throws DSOutOfServiceException, DSAccessException
    {
        //Retrieve the user ID.
        UserCredentials uc = (UserCredentials)
                registry.lookup(LookupNames.USER_CREDENTIALS);
        //Retrieve the specified category.
        Criteria c = CategoryMapper.buildClassificationCriteria(id);
        Category category = (Category) gateway.retrieveSTSData("Category", c);
        CategoryData model = new CategoryData();
        if (category != null) CategoryMapper.fillCategory(category, model, 
                                                        uc.getUserID());
        if (withAnnotation) {
            List imgs = model.getImages();
            if (imgs.size() != 0) {       //i.e. some classifications
                List ids = new ArrayList();
                Iterator i = imgs.iterator();
                while (i.hasNext()) 
                    ids.add(new Integer(((ImageSummary) i.next()).getID()));
                
                c = AnnotationMapper.buildImageAnnotationCriteria(ids);
                List l = (List) gateway.retrieveListSTSData("ImageAnnotation", 
                                                        c);
                
                CategoryMapper.fillImageAnnotationInCategory(
                        model.getClassifications(), l, uc.getUserID());
            }
        }
        return model;
    }
    
}

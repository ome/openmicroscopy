/*
 * org.openmicroscopy.shoola.env.data.map.DatasetDataMapper
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
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.ds.st.Group;
import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;

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
public class DatasetMapper
{
	
    /**
     * Build a criteria to retrieve minimal information to build the 
     * dataset-image graph.
     * 
     * @param datasetIDs List of dataset's ID.
     * @return See above
     */
    public static Criteria buildDatasetsTree(List datasetIDs)
    {
        Criteria c = new Criteria();
        c.addWantedField("name");
        c.addWantedField("images");
        //Specify which fields we want for the images.
        c.addWantedField("images", "name");
        c.addWantedField("images", "created");
        c.addWantedField("images", "default_pixels");
        
        //Specify which fields we want for the pixels.
        c.addWantedField("images.default_pixels", "ImageServerID"); 
        c.addWantedField("images.default_pixels", "Repository");
        c.addWantedField("images.default_pixels.Repository", "ImageServerURL");      

        if (datasetIDs != null) c.addFilter("id", "IN", datasetIDs);
        return c;
    }
    
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for updateDataset.
	 * 
	 * @param datasetID	specified dataset to retrieve.
	 */
	public static Criteria buildUpdateCriteria(int datasetID)
	{
		Criteria c = new Criteria();
		c.addWantedField("name");
		c.addWantedField("description");
		c.addFilter("id", new Integer(datasetID));
		return c;
	}
	
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieveUserDatasets.
	 * 
	 * @param userID	user ID.
	 */
	public static Criteria buildUserDatasetsCriteria(int userID)
	{
		Criteria c = new Criteria();
		
		//Specify which fields we want for the dataset.
		c.addWantedField("name");
		//Filter
		c.addFilter("owner_id", new Integer(userID));
		c.addFilter("name", "NOT LIKE", "ImportSet");
		return c;
	}
	
	/** 
	 * Create the criteria by which the object graph - including images
	 *  - is pulled out
	 * Criteria built for fullRetrieveUserDatasets.
	 * 
	 * @param userID	user ID.
	 */
	public static Criteria buildFullUserDatasetsCriteria(int userID)
	{
		Criteria c = new Criteria();
		
		//Specify which fields we want for the dataset.
		c.addWantedField("name");
		c.addWantedField("description");
		c.addWantedField("owner");
		c.addWantedField("images");

        UserMapper.objectOwnerCriteria(c);

		//Specify which fields we want for the images.
		c.addWantedField("images", "name");
        c.addWantedField("images", "created");
		c.addWantedField("images", "default_pixels");
		
		//Specify which fields we want for the pixels.
		c.addWantedField("images.default_pixels", "ImageServerID"); 
		c.addWantedField("images.default_pixels", "Repository");
		c.addWantedField("images.default_pixels.Repository", "ImageServerURL");		

        //Add filter
		c.addFilter("owner_id", new Integer(userID));
		c.addFilter("name", "NOT LIKE", "ImportSet");
		return c;
	}
    
	/**
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieveImages.
	 * 
	 * @return 
	 */
	public static Criteria buildImagesCriteria(List datasetIDs, Map filters,
                                    Map complexFilters)
	{
		Criteria c = new Criteria();
		
		//Specify which fields we want for the images.
		c.addWantedField("images");
		c.addWantedField("images", "name");
        c.addWantedField("images", "created");
		c.addWantedField("images", "default_pixels");
		
		//Specify which fields we want for the pixels
		c.addWantedField("images.default_pixels", "ImageServerID");
        c.addWantedField("images.default_pixels", "Repository");
        c.addWantedField("images.default_pixels.Repository", "ImageServerURL");
        if (datasetIDs != null) c.addFilter("id", "IN", datasetIDs);
        UserMapper.setFilters(c, filters, complexFilters);
		return c;
	}
	
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieveDataset.
	 * 
	 */
	public static Criteria buildDatasetCriteria(int id)
	{
		Criteria c = new Criteria();

		//Specify which fields we want for the dataset.
		c.addWantedField("name");
		c.addWantedField("description");
		c.addWantedField("owner");
		c.addWantedField("images"); 

		//Specify which fields we want for the owner.
		c.addWantedField("owner", "FirstName");
		c.addWantedField("owner", "LastName");
		c.addWantedField("owner", "Email");
		c.addWantedField("owner", "Institution");
		c.addWantedField("owner", "Group");

		//Specify which fields we want for the owner's group.
		c.addWantedField("owner.Group", "Name");

		//Specify which fields we want for the images.
		c.addWantedField("images", "name");
        c.addWantedField("images", "created");
		c.addWantedField("images", "default_pixels");
		
		//Specify which fields we want for the pixels.
		c.addWantedField("images.default_pixels", "ImageServerID");
        c.addWantedField("images.default_pixels", "Repository");
        c.addWantedField("images.default_pixels.Repository", "ImageServerURL");

		//Filter by ID.
		c.addFilter("id", new Integer(id));
		return c;
	}

	/** 
     * Fill in the dataset data object. 
	 * 
	 * @param dataset	OMEDS dataset object.
	 * @param empty		dataset data to fill up.
	 * 
	 */
	public static void fillDataset(Dataset dataset, DatasetData empty)
	{
		//Fill up the DataObject with the data coming from Project.
		empty.setID(dataset.getID());
		empty.setName(dataset.getName());
		empty.setDescription(dataset.getDescription());
				
		//Fill up the DataObject with data coming from Experimenter.
		Experimenter owner = dataset.getOwner();
		empty.setOwnerID(owner.getID());
		empty.setOwnerFirstName(owner.getFirstName());
		empty.setOwnerLastName(owner.getLastName());
		empty.setOwnerEmail(owner.getEmail());
		empty.setOwnerInstitution(owner.getInstitution());
		
		//Fill up the DataObject with data coming from Group.
		Group group = owner.getGroup();
		empty.setOwnerGroupID(group.getID());
		empty.setOwnerGroupName(group.getName());
		
		//Create the image summary list.
		List images = new ArrayList();
		Iterator i = dataset.getImages().iterator();
		while (i.hasNext()) 
			images.add(fillImageSummary((Image) i.next()));
		
		empty.setImages(images);	
	}


	/** Fill in the dataset data object. 
	 * 
	 * @param dataset	OMEDS dataset object.
	 * @param empty		dataset data to fill up.
	 * 
	 */
	public static void fillDataset(Dataset dataset, DatasetData empty,
			                    ImageSummary iProto)
	{
		//Fill up the DataObject with the data coming from Project.
		empty.setID(dataset.getID());
		empty.setName(dataset.getName());
		empty.setDescription(dataset.getDescription());
				
		//Fill up the DataObject with data coming from Experimenter.
		Experimenter owner = dataset.getOwner();
		empty.setOwnerID(owner.getID());
		empty.setOwnerFirstName(owner.getFirstName());
		empty.setOwnerLastName(owner.getLastName());
		empty.setOwnerEmail(owner.getEmail());
		empty.setOwnerInstitution(owner.getInstitution());
		
		//Fill up the DataObject with data coming from Group.
		Group group = owner.getGroup();
		empty.setOwnerGroupID(group.getID());
		empty.setOwnerGroupName(group.getName());
		
		//Create the image summary list.
		List images = new ArrayList();
		List  dsImages = dataset.getImages();
		
		if (dsImages != null && dsImages.size() > 0) {
			Iterator i = dsImages.iterator();
			Image img;
			while (i.hasNext()) {
				img = (Image) i.next();
				ImageSummary is = (ImageSummary) iProto.makeNew();
				fillImageSummary(img, is);
				images.add(is);
			}
			empty.setImages(images);
		}
	}
    
    /** Build a list of imageID. */
    public static List prepareListImagesID(Dataset dataset)
    {
        List d = new ArrayList();
        d.add(dataset);
        return prepareListImagesID(d);
    }
    
    /** Build a list of imageID. */
    public static List prepareListImagesID(List datasets)
    {
        Map map = new HashMap();
        Iterator i = datasets.iterator(), j;
        Integer id;
        while (i.hasNext()) {
            j = (((Dataset) i.next()).getImages()).iterator();
            while (j.hasNext()) {
                id = new Integer(((Image) j.next()).getID());
                map.put(id, id);
            }
        }
        List results = new ArrayList();
        Iterator k = map.keySet().iterator();
        while (k.hasNext()) 
            results.add(k.next());
        return results;
    }
    
    public static List fillImagesInUserDatasets(List datasets, 
                    ImageSummary iProto, List datasetIDs)
    {
        List images = new ArrayList();
        HashMap map = new HashMap();
        Iterator d = datasets.iterator();
        Iterator i;
        Image image;
        ImageSummary is;
        Integer imageID;
        Dataset dDTO;
        while (d.hasNext()) {
            dDTO = (Dataset) d.next();
            if (datasetIDs.contains(new Integer(dDTO.getID()))) {
                i = dDTO.getImages().iterator();
                while (i.hasNext()) {
                    image = (Image) i.next();
                    imageID = new Integer(image.getID());
                    is = (ImageSummary) map.get(imageID);
                    if (is == null) {
                        //Make a new DataObject and fill it up.
                        is = (ImageSummary) iProto.makeNew();
                        fillImageSummary(image, is);
                        //Add the image summary object to the list.
                        images.add(is);
                        map.put(imageID, is);
                    }
                } 
            }
        }
        return images;
    }
    
    public static List fillImagesInUserDatasets(List datasets, 
                        ImageSummary iProto, List annotations, List datasetIDs)
    {
        Map ids = AnnotationMapper.reverseListImageAnnotations(annotations);
        List images = new ArrayList();
        HashMap map = new HashMap();
        Iterator d = datasets.iterator();
        Iterator i;
        Image image;
        ImageSummary is;
        Integer imageID;
        ImageAnnotation annotation;
        Dataset dDTO;
        while (d.hasNext()) {
            dDTO = (Dataset) d.next();
            if (datasetIDs.contains(new Integer(dDTO.getID()))) {
                i = dDTO.getImages().iterator();
                while (i.hasNext()) {
                    image = (Image) i.next();
                    imageID = new Integer(image.getID());
                    annotation = (ImageAnnotation) ids.get(imageID);
                    if (annotation != null) {
                        is = (ImageSummary) map.get(imageID);
                        if (is == null) {
                            //Make a new DataObject and fill it up.
                            is = (ImageSummary) iProto.makeNew();
                            fillImageSummary(image, is);
                            is.setAnnotation(
                                AnnotationMapper.fillImageAnnotation(annotation));
                            //Add the image summary object to the list.
                            images.add(is);
                            map.put(imageID, is);
                        }
                    }
                }
            }
        }
        return images;
    }
    
	/**
	 * Creates the image summary list.
	 * 
	 * @param dataset	OMEDS dataset object.
	 * @param iProto	DataObject to fill up.
	 * @return list of image summary objects.
	 */
	public static List fillListImages(Dataset dataset, ImageSummary iProto)
	{
		List datasets = new ArrayList();
        datasets.add(dataset);
        List d = new ArrayList();
        d.add(new Integer(dataset.getID()));
        return fillImagesInUserDatasets(datasets, iProto, d);
	}
	
    /**
     * Creates the image summary list.
     * 
     * @param dataset   OMEDS dataset object.
     * @param iProto    DataObject to fill up.
     * @return list of image summary objects.
     */
    public static List fillListAnnotatedImages(Dataset dataset, 
                                         ImageSummary iProto, List annotations, 
                                         List images)
    {
        Iterator i = dataset.getImages().iterator();
        Image image;
        ImageSummary is;
        int id;
        Map ids = AnnotationMapper.reverseListImageAnnotations(annotations);
        while (i.hasNext()) {
            image = (Image) i.next();
            //Make a new DataObject and fill it up.
            is = (ImageSummary) iProto.makeNew();
            fillImageSummary(image, is);
            id = image.getID();
            is.setAnnotation(AnnotationMapper.fillImageAnnotation(
                    (ImageAnnotation) ids.get(new Integer(id))));
            //Add the image summary object to the list.
            images.add(is);
        }
        return images;
    }
    
	/**
	 * Create a list of dataset summary object.
	 *
	 * @param datasets	list of datasets objects.
	 * @param dProto	dataObject to model.
	 * @return See above.
	 */
	public static List fillUserDatasets(List datasets, DatasetSummary dProto)
	{
		List datasetsList = new ArrayList();  //The returned summary list.
		Iterator i = datasets.iterator();
		DatasetSummary ds;
		Dataset d;
		//For each d in datasets...
		while (i.hasNext()) {
			d = (Dataset) i.next();
			//Make a new DataObject and fill it up.
			ds = (DatasetSummary) dProto.makeNew();
			ds.setID(d.getID());
			ds.setName(d.getName());
			//Add the dataset  summary object to the list.
			datasetsList.add(ds);
		}
		return datasetsList;
	}

	/**
	 * Create a list of dataset summary objects, including images.
	 *
	 * @param datasets	list of datasets objects.
	 * @param dProto	dataObject to model.
	 * @return See above.
	 */
	public static List fillFullUserDatasets(List datasets, DatasetData dProto,
                                            ImageSummary iProto)
	{
		List datasetsList = new ArrayList();  //The returned summary list.
		Iterator i = datasets.iterator();
		DatasetData ds;
		Dataset d;
		//For each d in datasets...
		while (i.hasNext()) {
			d = (Dataset) i.next();
			//Make a new DataObject and fill it up.
			ds = (DatasetData) dProto.makeNew();
			fillDataset(d, ds, iProto);
			datasetsList.add(ds);
		}
		return datasetsList;
	}
    
    public static void fillDatasetsTree(List datasets, List results, 
                                        List datasetIDs)
    {
        Iterator i = datasets.iterator(), k;
        Map imagesMap = new HashMap();
        DatasetSummaryLinked ds;
        Dataset d;
        Image img;
        ImageSummary is;
        List images;
        Integer id;
        while (i.hasNext()) {
            d = (Dataset) i.next();
            if (datasetIDs.contains(new Integer(d.getID()))) {
                ds = new DatasetSummaryLinked();
                ds.setID((d.getID()));
                ds.setName(d.getName());
                k = d.getImages().iterator();
                //Add images to the dataset.
                images = new ArrayList();
                k = d.getImages().iterator();
                while (k.hasNext()) {
                    img = (Image) k.next();
                    id = new Integer(img.getID());
                    is = (ImageSummary) imagesMap.get(id);
                    if (is == null) {
                        is = fillImageSummary(img);
                        imagesMap.put(id, is);
                    }
                    images.add(is);
                }
                ds.setImages(images);
                results.add(ds);
            }
        }
    }
    
    public static ImageSummary fillImageSummary(Image img)
    {
        ImageSummary is = new ImageSummary();
        fillImageSummary(img, is);
        return is;
    }
    
    private static void fillImageSummary(Image img, ImageSummary is)
    {
        is.setID(img.getID());
        is.setName(img.getName());
        is.setPixelsIDs(fillListPixelsID(img));
        is.setDefaultPixels(fillDefaultPixels(img.getDefaultPixels()));
        is.setDate(PrimitiveTypesMapper.getTimestamp(img.getCreated()));
    }
    
	//	TODO: will be modified as soon as we have a better approach.
	private static int[] fillListPixelsID(Image image)
	{
		int[] ids = new int[1];
	  	Pixels px = image.getDefaultPixels();
		ids[0] = px.getID();
	  	return ids;
	}
    
    private static PixelsDescription fillDefaultPixels(Pixels px)
    {
        PixelsDescription pxd = new PixelsDescription();
        pxd.setID(px.getID());
        if (px.getImageServerID() != null)
            pxd.setImageServerID(px.getImageServerID().longValue());
        if (px.getRepository().getImageServerURL() != null)
            pxd.setImageServerUrl(px.getRepository().getImageServerURL());
        pxd.setPixels(px);
        return pxd;
    }
	
}

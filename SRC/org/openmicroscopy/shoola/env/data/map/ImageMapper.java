/*
 * org.openmicroscopy.shoola.env.data.map.ImageMapper
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
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.ds.st.Group;
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
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
public class ImageMapper
{
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria linked to retrieveUserImges.
	 * 
	 * @param userID	user ID.
	 */
	public static Criteria buildUserImagesCriteria(int userID)
	{
		Criteria criteria = new Criteria();
		
		//Specify which fields we want for the image.
		criteria.addWantedField("id");
		criteria.addWantedField("name");
		
		criteria.addWantedField("default_pixels");
		criteria.addWantedField("default_pixels.Repository", "ImageServerID");
		
		criteria.addFilter("owner_id", new Integer(userID));
		
		return criteria;
	}
	
	/** 
	 * Define the criteria by which the object graph is pulled out.
	 * @return
	 */
	public static Criteria buildImageCriteria(int id)
	{
		Criteria criteria = new Criteria();
		
		//Specify which fields we want for the image.
  		criteria.addWantedField("id");
  		criteria.addWantedField("name");
  		criteria.addWantedField("description"); 
		criteria.addWantedField("inserted"); 
		criteria.addWantedField("created"); 
		criteria.addWantedField("owner");	
		criteria.addWantedField("datasets");
		criteria.addWantedField("default_pixels");
		
		//Specify which fields we want for the datasets.
		criteria.addWantedField("datasets", "id");
		criteria.addWantedField("datasets", "name");
		
		
		//Specify which fields we want for the pixels.
		criteria.addWantedField("default_pixels", "id");
		criteria.addWantedField("default_pixels", "SizeX");
		criteria.addWantedField("default_pixels", "SizeY");
		criteria.addWantedField("default_pixels", "SizeZ");
		criteria.addWantedField("default_pixels", "SizeC");
		criteria.addWantedField("default_pixels", "SizeT");
		criteria.addWantedField("default_pixels", "BitsPerPixel");	
		criteria.addWantedField("default_pixels", "Repository");
		criteria.addWantedField("default_pixels", "ImageServerID");
		criteria.addWantedField("default_pixels.Repository", "ImageServerURL");
  		
  		//Specify which fields we want for the owner.
		criteria.addWantedField("owner", "id");
  		criteria.addWantedField("owner", "FirstName");
  		criteria.addWantedField("owner", "LastName");
  		criteria.addWantedField("owner", "Email");
  		criteria.addWantedField("owner", "Institution");
  		criteria.addWantedField("owner", "Group");

  		//Specify which fields we want for the owner's group.
  		criteria.addWantedField("owner.Group", "id");
  		criteria.addWantedField("owner.Group", "Name");
  		
		criteria.addFilter("id", new Integer(id));
		
  		return criteria;
	}
	
	/** 
	 * Fill in the image data object. 
	 * 
	 * @param image		OMEDS Image object.
	 * @param empty		image data to fill in.
	 * 
	 */
	public static void fillImage(Image image, ImageData empty)
	{
	
		//Fill in the data coming from OMEDS object.
		empty.setID(image.getID());
		empty.setName(image.getName());
		empty.setDescription(image.getDescription());
		empty.setCreated(image.getCreated());
		empty.setInserted(image.getInserted());
		
		//Fill in the data coming from Experimenter.
		Experimenter owner = image.getOwner();
		empty.setOwnerID(owner.getID());
		empty.setOwnerFirstName(owner.getFirstName());
		empty.setOwnerLastName(owner.getLastName());
		empty.setOwnerEmail(owner.getEmail());
		empty.setOwnerInstitution(owner.getInstitution());
	
		//Fill in the data coming from Group.
		Group group = owner.getGroup();
		empty.setOwnerGroupID(group.getID());
		empty.setOwnerGroupName(group.getName());
		
		//dataset summary list.
		List datasets = new ArrayList();
		Iterator i = image.getDatasets().iterator();
		Dataset d;
		while (i.hasNext()) {
			d = (Dataset) i.next();
			datasets.add(new DatasetSummary(d.getID(), d.getName()));
		}
		empty.setDatasets(datasets);
		
		// pixelsDescription list.
		if (image.getDefaultPixels() != null) {
			List pixels = fillPixels((Pixels) image.getDefaultPixels());
			empty.setPixels(pixels);
		}	
	}
	
	/**
	 * @param images
	 * @param iProto
	 * @return
	 */
	public static List fillUserImages(List images, ImageSummary iProto)
	{
		List imagesList = new ArrayList();  //The returned summary list.
		Iterator i = images.iterator();
		ImageSummary is;
		Image img;
		Pixels px;
		//For each d in datasets...
		while (i.hasNext()) {
			img = (Image) i.next();
			//Make a new DataObject and fill it up.
			is = (ImageSummary) iProto.makeNew();
			px = (Pixels) img.getDefaultPixels();
			is.setID(img.getID());
			is.setName(img.getName());
			is.setImageServerPixelsID(fillListPixelsID(px));
			//Add the images to the list of returned images
			imagesList.add(is);
		}
		
		return imagesList;
	}
	
	private static List fillPixels(Pixels px)
	{
		List pixels = new ArrayList();
		PixelsDescription pxd = new PixelsDescription();
		pxd.setID(px.getID());
		if (px.getSizeX() != null) pxd.setSizeX((px.getSizeX()).intValue());
		if (px.getSizeY() != null) pxd.setSizeY((px.getSizeY()).intValue());
		if (px.getSizeZ() != null) pxd.setSizeZ((px.getSizeZ()).intValue());
		if (px.getSizeC() != null) pxd.setSizeC((px.getSizeC()).intValue());
		if (px.getSizeT() != null) pxd.setSizeT((px.getSizeT()).intValue());
		if (px.getBitsPerPixel() != null) 
			pxd.setBitsPerPixel((px.getBitsPerPixel()).intValue());
		pxd.setImageServerUrl(px.getRepository().getImageServerURL());
		if (px.getImageServerID() != null)
			pxd.setImageServerID((px.getImageServerID()).intValue());
        pxd.setPixels(px);
		pixels.add(pxd);
		return pixels;
	}
	
	//	TODO: will be modified as soon as we have a better approach.
	private static int[] fillListPixelsID(Pixels px)
	{
		int[] ids = new int[1];
		//to be on the save side
		if (px.getImageServerID() != null)
			ids[0] = (px.getImageServerID()).intValue();
		return ids;
	}
}

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.ds.st.Group;
import org.openmicroscopy.ds.st.LogicalChannel;
import org.openmicroscopy.ds.st.PixelChannelComponent;
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
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
	 * Criteria built for updateImage.
	 * 
	 * @param imageID	specified image to retrieve.
	 */
	public static Criteria buildUpdateCriteria(int imageID)
	{
		Criteria c = new Criteria();
		c.addWantedField("id");
		c.addWantedField("name");
		c.addWantedField("description");
		c.addFilter("id", new Integer(imageID));
		return c;
	}
		
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for retrieveUserImges.
	 * 
	 * @param userID	user ID.
	 */
	public static Criteria buildUserImagesCriteria(int userID)
	{
		Criteria criteria = new Criteria();
		
		//Specify which fields we want for the image.
		criteria.addWantedField("id");
		criteria.addWantedField("name");
		criteria.addWantedField("created");
		
		//Specify which fields we want for the pixels.
		criteria.addWantedField("default_pixels");
		criteria.addWantedField("default_pixels", "id");
		criteria.addFilter("owner_id", new Integer(userID));
		
		return criteria;
	}
	
	/** 
	 * Define the criteria by which the object graph is pulled out.
	 * Criteria built for retrieveImage.
	 * @param	id		image to retrieve.
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
		criteria.addWantedField("default_pixels", "PixelType");
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
	 * Fill in the image summary object.
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
			is.setPixelsIDs(fillListPixelsID(px));
			is.setDate(img.getCreated());
			//is.setImageServerPixelsID(fillListPixelsID(px));
			//Add the images to the list of returned images
			imagesList.add(is);
		}
		
		return imagesList;
	}
	
	/**
	 * 
	 * @param ciList		PixelChannelComponent list.
	 * @param lcList		LogicalChannel List.
	 * @return
	 */
	public static ChannelData[] fillImageChannelData(List ciList, List lcList)
	{
		PixelChannelComponent pcc;
		Iterator k = ciList.iterator();
		HashMap lcIndexes = new HashMap();
		while (k.hasNext()) {
			pcc = (PixelChannelComponent) k.next();
			lcIndexes.put(new Integer(pcc.getLogicalChannel().getID()), 
								new Integer(pcc.getIndex().intValue()));
		}
		LogicalChannel lc;
		int index;
		Iterator i = lcList.iterator();
		int nanometer, excitation;
		ChannelData[] channelData = new ChannelData[lcList.size()];
		while (i.hasNext()) {
			lc = (LogicalChannel) i.next();
			index = 
				((Integer) lcIndexes.get(new Integer(lc.getID()))).intValue();
			if (lc.getEmissionWavelength() == null)	
				nanometer = index;
			else
				nanometer = lc.getEmissionWavelength().intValue();
			
			if (lc.getExcitationWavelength() == null) 
				excitation = nanometer;
			else 
				excitation = lc.getExcitationWavelength().intValue();
	
			channelData[index] = new ChannelData(lc.getID(), index, nanometer,
									lc.getPhotometricInterpretation(), 
									excitation, lc.getFluor());
		}
		return channelData;
	}
	
	/** 
	 * Fill in the PixelDescription object.
	 * @return List of pixelDescription object.
	 */ 
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
		pxd.setPixelType(px.getPixelType());
		pxd.setImageServerUrl(px.getRepository().getImageServerURL());
		if (px.getImageServerID() != null)
			pxd.setImageServerID((px.getImageServerID()).longValue());
        pxd.setPixels(px);
		pixels.add(pxd);
		return pixels;
	}
	
	private static int[] fillListPixelsID(Pixels px)
	{
		int[] ids = new int[1];
		ids[0] = px.getID();
		return ids;
	}
	
}

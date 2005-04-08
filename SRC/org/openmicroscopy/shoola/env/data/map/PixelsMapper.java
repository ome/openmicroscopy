/*
 * org.openmicroscopy.shoola.env.data.map.PixelMapper
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.st.Dimensions;
import org.openmicroscopy.ds.st.Pixels;
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
public class PixelsMapper
{
	
	//TODO: to be modified
	public static Criteria buildPixelsCriteria(int imageID)
	{
		Criteria criteria = new Criteria();

		//Specify which fields we want for the image.
		criteria.addWantedField("default_pixels");
		
		//Specify which fields we want for the pixels.
		criteria.addWantedField("default_pixels", "SizeX");
		criteria.addWantedField("default_pixels", "SizeY");
		criteria.addWantedField("default_pixels", "SizeZ");
		criteria.addWantedField("default_pixels", "SizeC");
		criteria.addWantedField("default_pixels", "SizeT");
		criteria.addWantedField("default_pixels", "PixelType");	
		criteria.addWantedField("default_pixels", "Repository");
		criteria.addWantedField("default_pixels", "ImageServerID");
		criteria.addWantedField("default_pixels.Repository", "ImageServerURL");
		criteria.addFilter("id", new Integer(imageID));
		return criteria;
	}
	
    /** 
     * Build criteria to retrieve the real pixel dimensions
     * 
     * @param imageID
     * @return See above
     */
    public static Criteria buildPixelsDimensionCriteria(int imageID)
    {
        Criteria c = new Criteria();
        c.addWantedField("PixelSizeT");
        c.addWantedField("PixelSizeC");
        c.addWantedField("PixelSizeX");
        c.addWantedField("PixelSizeY");
        c.addWantedField("PixelSizeZ");
        c.addFilter("image_id", new Integer(imageID));
        return c;
    }
    
    /** 
     * Build criteria for the PixelChannelComponent semantic type.
     * 
     * @param imageID       id of the image.
     * @return See above.
     */
    public static Criteria buildPixelChannelComponentCriteria(int imageID)
    {
        Criteria c = new Criteria();
        c.addWantedField("LogicalChannel");
        c.addWantedField("Index");
        c.addWantedField("ColorDomain");        //later used for RGTIFF
        c.addFilter("image_id", new Integer(imageID));
        return c;
    }
    
    /** 
     * Build the criteria to retrieve the logical channels associated to an
     * image.
     * @param g     granularity
     * @param id    image's id.
     * @return
     */
    public static Criteria buildLogicalChannelCriteria(String g, int id)
    {
        
        Criteria c = buildBasicLogicalChannelCriteria(g, id);
        c.addWantedField("ExcitationWavelength");
        c.addWantedField("PhotometricInterpretation"); 
        c.addWantedField("Fluor"); 
        c.addWantedField("NDFilter");
        c.addWantedField("AuxTechnique");
        c.addWantedField("AuxLightWavelength");
        c.addWantedField("AuxLightAttenuation");
        c.addWantedField("AuxLightSource");
        c.addWantedField("ContrastMethod");
        c.addWantedField("Mode");
        c.addWantedField("PinholeSize");
        c.addWantedField("DetectorGain");
        c.addWantedField("DetectorOffset");
        c.addWantedField("LightWavelength");
        c.addWantedField("LightAttenuation");
        c.addWantedField("SamplesPerPixel");
        c.addWantedField("IlluminationType");
        return c;
    }
    
    /**
     * Only retrieve the EmissionWavelength field.
     *  
     * @param g String corresponding to the granularity defined by 
     *          {@link STSMapper}
     * @param id
     * @return
     */
    public static Criteria buildBasicLogicalChannelCriteria(String g, int id)
    {
        Criteria c = new Criteria();
        c.addWantedField("EmissionWavelength");
        String column = (String) STSMapper.granularities.get(g);
        if (column != null) c.addFilter(column, new Integer(id));
        return c;
    }
    
	/** Put the server data into the corresponding client object. */
	public static void fillPixelsDescription(Pixels px, 
											PixelsDescription pdProto)
	{
		pdProto.setID(px.getID());
		if (px.getSizeX() != null) pdProto.setSizeX((px.getSizeX()).intValue());
		if (px.getSizeY() != null) pdProto.setSizeY((px.getSizeY()).intValue());
		if (px.getSizeZ() != null) pdProto.setSizeZ((px.getSizeZ()).intValue());
		if (px.getSizeC() != null) pdProto.setSizeC((px.getSizeC()).intValue());
		if (px.getSizeT() != null) pdProto.setSizeT((px.getSizeT()).intValue());
		pdProto.setPixelType(px.getPixelType());
		pdProto.setImageServerUrl(px.getRepository().getImageServerURL());
		if (px.getImageServerID() != null)
			pdProto.setImageServerID((px.getImageServerID()).longValue());
		//TODO: b/c of is status we need to keep Pixel server object in 
		// PixelsDrescription (client object).
		// should be removed.
		pdProto.setPixels(px);
	}
	
    /** Put the server data into the corresponding client object. */
    public static void fillPixelsDimensions(Dimensions pixelDim, 
                                    PixelsDescription pdProto)
    {
        if (pixelDim.getPixelSizeX() != null)
            pdProto.setPixelSizeX((pixelDim.getPixelSizeX()).doubleValue());
        if (pixelDim.getPixelSizeY() != null)
            pdProto.setPixelSizeY((pixelDim.getPixelSizeY()).doubleValue()); 
        if (pixelDim.getPixelSizeZ() != null)
            pdProto.setPixelSizeZ((pixelDim.getPixelSizeZ()).doubleValue()); 
    }
    
}

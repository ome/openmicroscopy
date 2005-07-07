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
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.Criteria;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Image;
import org.openmicroscopy.ds.st.Experimenter;
import org.openmicroscopy.ds.st.Group;
import org.openmicroscopy.ds.st.ImageAnnotation;
import org.openmicroscopy.ds.st.LogicalChannel;
import org.openmicroscopy.ds.st.PixelChannelComponent;
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.ds.st.RenderingSettings;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;

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
     * Fill in the PixelDescription object.
     * @return List of pixelDescription object.
     */ 
    private static List fillPixels(Pixels px)
    {
        List pixels = new ArrayList();
        if (px == null) return pixels;
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
    
    /** Fill an array of pixels ID. */
    private static int[] fillListPixelsID(Pixels px)
    {
        int[] ids = new int[1];
        if (px == null) return ids;
        ids[0] = px.getID();
        return ids;
    }
    
    private static Criteria buildBasicImagesCriteria()
    {
        Criteria c = new Criteria();
        
        //Specify which fields we want for the image.
        c.addWantedField("name");
        c.addWantedField("created");
        //Specify which fields we want for the pixels.
        c.addWantedField("default_pixels");
        PixelsMapper.fieldsForPixels(c, "default_pixels");
        return c;
    }
    
    /** Build an image summary object. */
    static ImageSummary buildImageSummary(Image img, ImageSummary is)
    {
        if (is == null) is = new ImageSummary();
        is.setID(img.getID());
        is.setName(img.getName());
       
        is.setDate(PrimitiveTypesMapper.getTimestamp(img.getCreated()));
        is.setPixelsIDs(fillListPixelsID(img.getDefaultPixels()));
        List pixels = fillPixels(img.getDefaultPixels());
        if (pixels.size() > 0)
           is.setDefaultPixels((PixelsDescription) pixels.get(0));
        return is;
    }
    
	/** 
	 * Create the criteria by which the object graph is pulled out.
	 * Criteria built for updateImage.
	 * 
	 * @param imageID	specified image to retrieve.
	 */
	public static Criteria buildUpdateCriteria(int imageID)
	{
		Criteria c = new Criteria();
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
	public static Criteria buildUserImagesCriteria(int userID, Map filters, 
                                        Map complexFilters)
	{
		Criteria c = buildBasicImagesCriteria();
        if (userID != -1)
            c.addFilter("owner_id", new Integer(userID));
        UserMapper.setFilters(c, filters, complexFilters); 
        return c;
	}
	
    /** 
     * Create the criteria by which the object graph is pulled out.
     * 
     * @param userGroupIds    List of group's id, the user belongs to.
     */
    public static Criteria buildUserImagesCriteria(List userGroupIds, 
                            Map filters, Map complexFilters)
    {
        Criteria c = buildBasicImagesCriteria();
        UserMapper.setFilters(c, filters, complexFilters); 
        //Shouldn't be a problem
        if (userGroupIds != null) c.addFilter("group_id", "IN", userGroupIds);
        return c;
    }
    
    /** 
     * Simplest criteria to retrieve a remote Image object. 
     * 
     * @param imageID   ID of the remote object to retrieve.
     */
    public static Criteria buildBasicImageCriteria(int imageID)
    {
        Criteria c = new Criteria();
        c.addWantedField("name");
        if (imageID != -1) c.addFilter("id", new Integer(imageID));
        return c;
    }
    
	/** 
	 * Define the criteria by which the object graph is pulled out.
	 * Criteria built for retrieveImage.
	 * @param	id		image to retrieve.
	 */
	public static Criteria buildImageCriteria(int id)
	{
		Criteria c = PixelsMapper.buildPixelsCriteria(id);
		
		//Specify which fields we want for the image.
  		c.addWantedField("name");
  		c.addWantedField("description"); 
		c.addWantedField("inserted"); 
		c.addWantedField("created"); 
		c.addWantedField("owner");	
		c.addWantedField("datasets");
		
		//Specify which fields we want for the datasets.
		c.addWantedField("datasets", "name");

		//Fields for the owner.
        UserMapper.objectOwnerCriteria(c);
        
  		return c;
	}
	
    /**
     * 
     * @param g
     * @param id
     * @return
     */
    public static Criteria buildRenderingSettingsCriteria(String g, int id,
                                                            int userID)
    {
        Criteria c = new Criteria();
        c.addWantedField("Active");
        c.addWantedField("Alpha");
        c.addWantedField("Blue");
        c.addWantedField("Green");
        c.addWantedField("Red");
        c.addWantedField("InputEnd");
        c.addWantedField("InputStart");
        c.addWantedField("TheC");
        c.addWantedField("BitResolution");
        c.addWantedField("CdEnd");
        c.addWantedField("CdStart");
        c.addWantedField("Coefficient");
        c.addWantedField("Family");
        c.addWantedField("Model");
        c.addWantedField("TheT");
        c.addWantedField("TheZ");
        c.addFilter("Experimenter", new Integer(userID));
        String column = (String) STSMapper.granularities.get(g);
        if (column != null) c.addFilter(column, new Integer(id));
        return c;
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
		empty.setCreated(PrimitiveTypesMapper.getTimestamp(image.getCreated()));
		empty.setInserted(
                PrimitiveTypesMapper.getTimestamp(image.getInserted()));
		
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
        Pixels defaultPix = image.getDefaultPixels();
        empty.setPixelsIDs(fillListPixelsID(defaultPix));    
        empty.setPixels(fillPixels(defaultPix));	
	}

    /**
     * 
     * @param images
     * @param iProto
     * @param annotations
     * @return
     */
    public static List fillListImages(List images, ImageSummary iProto, 
                                      List annotations)
    {
        Map ids = AnnotationMapper.reverseListImageAnnotations(annotations);
        List imagesList = new ArrayList();  //The returned summary list.
        Iterator i = images.iterator();
        ImageSummary is;
        Image img;
        int id;
        ImageAnnotation annotation;
        //For each d in datasets...
        while (i.hasNext()) {
            //Make a new DataObject and fill it up.
            img = (Image) i.next();
            id = img.getID();
            annotation = (ImageAnnotation) ids.get(new Integer(id));
            is = (ImageSummary) iProto.makeNew();
            buildImageSummary(img, is);
            is.setAnnotation(AnnotationMapper.fillImageAnnotation(annotation));
            imagesList.add(is);
        }
        return imagesList;
    }
    
	/**
     * Build the logical channel objects.
     * 
     * @param pccList       PixelChannelComponent list.
     * @param lcList        LogicalChannel List.
     * @return array of channelData objects.
     */
    public static ChannelData[] fillImageChannelData(List pccList, List lcList)
    {
        PixelChannelComponent pcc;
        Iterator k = pccList.iterator();
        HashMap lcIndexes = new HashMap();
        ChannelData[] channelData = new ChannelData[lcList.size()];
        while (k.hasNext()) {
            pcc = (PixelChannelComponent) k.next();
            lcIndexes.put(new Integer(pcc.getLogicalChannel().getID()), 
                                new Integer(pcc.getIndex().intValue()));
        }
        LogicalChannel lc;
        int index;
        Iterator i = lcList.iterator();
        int nanometer, excitation = -1;
        ChannelData data;
        while (i.hasNext()) {
            lc = (LogicalChannel) i.next();
            index = 
                ((Integer) lcIndexes.get(new Integer(lc.getID()))).intValue();
            if (lc.getEmissionWavelength() == null) 
                nanometer = index;
            else
                nanometer = lc.getEmissionWavelength().intValue();
            
            if (lc.getExcitationWavelength() != null) 
                excitation = lc.getExcitationWavelength().intValue();
            
            if (channelData[index] == null) {
                data = new ChannelData(lc.getID(), index, 
                        nanometer, lc.getPhotometricInterpretation(), 
                        excitation, lc.getFluor());
                if (lc.getAuxLightAttenuation() != null)
                    data.setAuxLightAttenuation(
                            lc.getAuxLightAttenuation().floatValue()); 
                 if (lc.getAuxLightWavelength() != null)  
                     data.setAuxLightWavelength(
                             lc.getAuxLightWavelength().intValue());
                 if (lc.getDetectorGain() != null)
                     data.setDetectorGain(lc.getDetectorGain().floatValue());
                 if (lc.getDetectorOffset() != null)
                     data.setDetectorOffset(
                             lc.getDetectorOffset().floatValue());
                 if (lc.getLightAttenuation() != null)
                     data.setLightAttenuation(
                         lc.getLightAttenuation().floatValue());
                 if (lc.getLightWavelength() != null)
                     data.setLightWavelength(
                             lc.getLightWavelength().intValue());
                 if (lc.getNDFilter() != null)
                     data.setNDFilter(lc.getNDFilter().floatValue());
                 if (lc.getPinholeSize() != null)
                     data.setPinholeSize(lc.getPinholeSize().intValue());
                 if (lc.getSamplesPerPixel() != null)
                     data.setSamplesPerPixel(
                             lc.getSamplesPerPixel().intValue());
                 
                 data.setMode(lc.getMode());
                 data.setAuxTechnique(lc.getAuxTechnique());
                 data.setContrastMethod(lc.getContrastMethod());
                 data.setIlluminationType(lc.getIlluminationType());  
                 channelData[index] = data;       
            }
        }
        return channelData;
    }

    /** Create an array with the emissionWavelength. */
    public static int[] fillImageChannels(List lcList)
    {
        int[] channels = new int[lcList.size()];
        Iterator i = lcList.iterator();
        LogicalChannel lc;
        int nanometer;
        int index = 0;
        while (i.hasNext()) {
            lc = (LogicalChannel) i.next();
            if (lc.getEmissionWavelength() == null) 
                nanometer = index;
            else
                nanometer = lc.getEmissionWavelength().intValue();
            channels[index] = nanometer;
            index++;
        }
        return channels;
    }

    /** Create a default channels array if no logical channel. */
    public static int[] fillDefaultImageChannels(int sizeC)
    {
        int[] channels = new int[sizeC];
        for (int i = 0; i < sizeC; i++)
            channels[i] = i;
        return channels;
    }
    
    /** Fill in a renderingDef object. */
	public static RenderingDef fillInRenderingDef(List rsList, int pixelType)
	{
		//Create a new QuantumDef object.
        //Default one.
        int cdStart = 0, cdEnd = QuantumFactory.DEPTH_8BIT;
        int bitResolution = QuantumFactory.DEPTH_8BIT;
        ChannelBindings[] channelBindings = new ChannelBindings[rsList.size()];
		int index = 0, z = 0, t = 0, model = RenderingDef.GS;
        double coeff = 1.0, dStart = 0, dEnd = 1;
        int red = ChannelBindings.COLOR_MIN, 
            green = ChannelBindings.COLOR_MIN,
            blue = ChannelBindings.COLOR_MAX,
            alpha = ChannelBindings.COLOR_MAX,
            family = QuantumFactory.LINEAR;
        boolean active = true;
        int j;
        Iterator i = rsList.iterator();
        RenderingSettings rs;
		while (i.hasNext()) {
			rs = (RenderingSettings) i.next();
            j = index;
            if (rs.getTheC() != null) index = rs.getTheC().intValue();
			if (j == 0) {
                if (rs.getTheZ() != null) z = rs.getTheZ().intValue();
                if (rs.getTheT() != null) t = rs.getTheT().intValue();
                if (rs.getModel() != null) model = rs.getModel().intValue();
                if (rs.getCdStart() != null) 
                    cdStart = rs.getCdStart().intValue(); 
                if (rs.getCdEnd() != null)
                    cdEnd = rs.getCdEnd().intValue();
                if (rs.getBitResolution() != null)
                    bitResolution = rs.getBitResolution().intValue();
			}
            if (rs.getInputStart() != null)
                dStart = rs.getInputStart().doubleValue();
            if (rs.getInputEnd() != null)
                dEnd = rs.getInputEnd().doubleValue();
            if (rs.getRed() != null) red = rs.getRed().intValue();
            if (rs.getGreen() != null) green = rs.getGreen().intValue();
            if (rs.getBlue() != null) blue = rs.getBlue().intValue();
            if (rs.getAlpha() != null) alpha = rs.getAlpha().intValue();
            if (rs.getFamily() != null) family = rs.getFamily().intValue();
            if (rs.getCoefficient() != null) 
                coeff = rs.getCoefficient().doubleValue();
            if (rs.isActive() != null) active = rs.isActive().booleanValue();
			channelBindings[index] = new ChannelBindings(index, dStart,
			        dEnd, red, green, blue, alpha, active, family, coeff);
            j++;
		}
        QuantumDef  qDef = new QuantumDef(pixelType, cdStart, cdEnd, 
                                bitResolution);
        
		return new RenderingDef(z, t, model, qDef, channelBindings);	
	}
	
	/** Fill in the renderingSettings SemanticType. */
	public static void fillInRenderingSettings(int z, int t, int model, 
									int cdStart, int cdEnd, int bitResolution, 
									ChannelBindings cb, RenderingSettings rs)
	{
		rs.setTheT(new Integer(t));
		rs.setTheZ(new Integer(z));
		rs.setModel(new Integer(model));
		rs.setCdStart(new Integer(cdStart));
		rs.setCdEnd(new Integer(cdEnd));
		rs.setBitResolution(new Integer(bitResolution));
		int rgba[] = cb.getRGBA();
		rs.setRed(new Integer(rgba[0]));
		rs.setGreen(new Integer(rgba[1]));
		rs.setBlue(new Integer(rgba[2]));
		rs.setAlpha(new Integer(rgba[3]));
		rs.setInputStart(new Double(cb.getInputStart()));
		rs.setInputEnd(new Double(cb.getInputEnd()));
		rs.setTheC(new Integer(cb.getIndex()));		
        rs.setActive(new Boolean(cb.isActive()));
        rs.setFamily(new Integer(cb.getFamily()));
        rs.setCoefficient(new Double(cb.getCurveCoefficient()));
	}

}

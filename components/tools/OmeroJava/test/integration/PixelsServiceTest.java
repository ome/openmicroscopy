/*
 * integration.PixelsServiceTest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IPixelsPrx;
import omero.model.AcquisitionMode;
import omero.model.ArcType;
import omero.model.Binning;
import omero.model.Channel;
import omero.model.ContrastMethod;
import omero.model.Correction;
import omero.model.DetectorType;
import omero.model.Family;
import omero.model.FilamentType;
import omero.model.FilterType;
import omero.model.Format;
import omero.model.IObject;
import omero.model.Illumination;
import omero.model.Image;
import omero.model.Immersion;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.Medium;
import omero.model.MicroscopeType;
import omero.model.PhotometricInterpretation;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.Pulse;

/** 
 * Collections of tests for the <code>Pixels</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class PixelsServiceTest 
	extends AbstractTest
{

	/** 
	 * The maximum number of elements for the <code>immersion</code>
	 * enumeration.
	 */
	private static final int MAX_IMMERSION = 8;
	
	/** 
	 * The maximum number of elements for the <code>correction</code>
	 * enumeration.
	 */
	private static final int MAX_CORRECTION = 15;
	
	/** 
	 * The maximum number of elements for the <code>medium</code>
	 * enumeration.
	 */
	private static final int MAX_MEDIUM = 6;
	
	/** 
	 * The maximum number of elements for the <code>microscope type</code>
	 * enumeration.
	 */
	private static final int MAX_MICROSCOPE_TYPE = 6;
	
	/** 
	 * The maximum number of elements for the <code>detector type</code>
	 * enumeration.
	 */
	private static final int MAX_DETECTOR_TYPE = 15;
	
	/** 
	 * The maximum number of elements for the <code>filter type</code>
	 * enumeration.
	 */
	private static final int MAX_FILTER_TYPE = 8;
	
	/** 
	 * The maximum number of elements for the <code>binning</code>
	 * enumeration.
	 */
	private static final int MAX_BINNING = 4;
	
	/** 
	 * The maximum number of elements for the <code>contrast method</code>
	 * enumeration.
	 */
	private static final int MAX_CONTRAST_METHOD = 10;
	
	/** 
	 * The maximum number of elements for the <code>illumination</code>
	 * enumeration.
	 */
	private static final int MAX_ILLUMINATION = 6;
	
	/** 
	 * The maximum number of elements for the 
	 * <code>photometric interpretation</code> enumeration.
	 */
	private static final int MAX_PHOTOMETRIC_INTERPRETATION = 6;
	
	/** 
	 * The maximum number of elements for the <code>acquisition mode</code>
	 * enumeration.
	 */
	private static final int MAX_ACQUISITION_MODE = 21;
	
	/** 
	 * The maximum number of elements for the <code>laser medium</code>
	 * enumeration.
	 */
	private static final int MAX_LASER_MEDIUM = 35;
	
	/** 
	 * The maximum number of elements for the <code>laser type</code>
	 * enumeration.
	 */
	private static final int MAX_LASER_TYPE = 9;
	
	/** 
	 * The maximum number of elements for the <code>pulse</code>
	 * enumeration.
	 */
	private static final int MAX_PULSE = 7;
	
	/** 
	 * The maximum number of elements for the <code>arc type</code>
	 * enumeration.
	 */
	private static final int MAX_ARC_TYPE = 5;
	
	/** 
	 * The maximum number of elements for the <code>filament type</code>
	 * enumeration.
	 */
	private static final int MAX_FILAMENT_TYPE = 4;
	
	/** 
	 * The maximum number of elements for the <code>format</code>
	 * enumeration.
	 */
	private static final int MAX_FORMAT = 168;
	
	/** 
	 * The maximum number of elements for the <code>family</code>
	 * enumeration.
	 */
	private static final int MAX_FAMILY = 4;
	
	/** 
	 * The maximum number of elements for the <code>Pixels Type</code>
	 * enumeration.
	 */
	private static final int MAX_PIXELS_TYPE = 11;
	
	/**
     * Tests if the objects returned are of the specified type.
     * 
     * @param name The type of object to retrieve.
     * @param max  The number of objects to retrieve.
     */
    private void checkEnumeration(String name, int max)
    	throws Exception 
    {
    	IPixelsPrx svc = factory.getPixelsService();
    	List<IObject> values = svc.getAllEnumerations(name);
    	assertNotNull(values);
    	assertTrue(values.size() == max);
    	Iterator<IObject> i = values.iterator();
    	int count = 0;
    	String v;
    	name = name+"I"; //b/c we handle the instances of the class.s
    	while (i.hasNext()) {
    		v = i.next().getClass().getName();
			if (name.equals(v))
				count++;
		}
    	assertTrue(values.size() == count);
    }
    
    /**
     * Tests the retrieval of the pixels description.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testRetrievePixelsDescription() 
    	throws Exception 
    {
    	Pixels pixels = createPixels();
		Image i = pixels.getImage();
        i = (Image) iUpdate.saveAndReturnObject(i);
        pixels = i.getPrimaryPixels();
        long id = pixels.getId().getValue();
        Pixels p = factory.getPixelsService().retrievePixDescription(id);
        assertNotNull(p);
        assertTrue(pixels.getSizeX().getValue() == p.getSizeX().getValue());
        assertTrue(pixels.getSizeY().getValue() == p.getSizeY().getValue());
        assertTrue(pixels.getSizeT().getValue() == p.getSizeT().getValue());
        assertTrue(pixels.getSizeZ().getValue() == p.getSizeZ().getValue());
        assertTrue(pixels.getSizeC().getValue() == p.getSizeC().getValue());
        assertTrue(pixels.sizeOfChannels() == DEFAULT_CHANNELS_NUMBER);
        assertTrue(p.sizeOfChannels() == DEFAULT_CHANNELS_NUMBER);
        assertTrue(pixels.getPhysicalSizeX().getValue() == 
        	p.getPhysicalSizeX().getValue());
        assertTrue(pixels.getPhysicalSizeY().getValue() == 
        	p.getPhysicalSizeY().getValue());
        assertTrue(pixels.getPhysicalSizeZ().getValue() == 
        	p.getPhysicalSizeZ().getValue());
        assertNotNull(pixels.getPixelsType());
        assertNotNull(p.getPixelsType());
        assertTrue(pixels.getPixelsType().getValue().getValue().equals(
        	p.getPixelsType().getValue().getValue()));
        Channel channel;
        List<Long> ids = new ArrayList<Long>();
        for (int j = 0; j < p.sizeOfChannels(); j++) {
			channel = p.getChannel(j);
			assertNotNull(channel);
			ids.add(channel.getId().getValue());
			assertNotNull(channel.getStatsInfo());
		}
        for (int j = 0; j < pixels.sizeOfChannels(); j++) {
			channel = pixels.getChannel(j);
			assertNotNull(channel);
			assertTrue(ids.contains(channel.getId().getValue()));
			assertNotNull(channel.getStatsInfo());
		}
    }
    
    /**
     * Tests the retrieval of the possible enumerations.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testGetAllEnumerations() 
    	throws Exception 
    {
    	//for instrument
    	checkEnumeration(Immersion.class.getName(), MAX_IMMERSION);
    	checkEnumeration(Correction.class.getName(), MAX_CORRECTION);
    	checkEnumeration(Medium.class.getName(), MAX_MEDIUM);
    	checkEnumeration(MicroscopeType.class.getName(), MAX_MICROSCOPE_TYPE);
    	checkEnumeration(DetectorType.class.getName(), MAX_DETECTOR_TYPE);
    	checkEnumeration(FilterType.class.getName(), MAX_FILTER_TYPE);
    	checkEnumeration(Binning.class.getName(), MAX_BINNING);
    	checkEnumeration(ContrastMethod.class.getName(), MAX_CONTRAST_METHOD);
    	checkEnumeration(Illumination.class.getName(), MAX_ILLUMINATION);
    	checkEnumeration(PhotometricInterpretation.class.getName(), 
    			MAX_PHOTOMETRIC_INTERPRETATION);
    	checkEnumeration(AcquisitionMode.class.getName(), MAX_ACQUISITION_MODE);
    	checkEnumeration(LaserMedium.class.getName(), MAX_LASER_MEDIUM);
    	checkEnumeration(LaserType.class.getName(), MAX_LASER_TYPE);
    	checkEnumeration(Pulse.class.getName(), MAX_PULSE);
    	checkEnumeration(ArcType.class.getName(), MAX_ARC_TYPE);
    	checkEnumeration(FilamentType.class.getName(), MAX_FILAMENT_TYPE);
    	checkEnumeration(Format.class.getName(), MAX_FORMAT);
    	
    	//for rendering engine
    	checkEnumeration(Family.class.getName(), MAX_FAMILY);
    	checkEnumeration(PixelsType.class.getName(), MAX_PIXELS_TYPE);
    }
    
}

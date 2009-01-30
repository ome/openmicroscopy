/*
 * org.openmicroscopy.shoola.agents.util.EditorUtil 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.util;


//Java imports
import java.awt.Color;
import java.awt.Font;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import omero.RFloat;
import omero.model.PlaneInfo;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMEComboBoxUI;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.WellData;

/** 
 * Collection of helper methods to format data objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class EditorUtil 
{
	
    /** Letter corresponding to number. */
	public static final Map<Integer, String> LETTERS;
	
	/** Default text displayed in the acquisition date is not available. */
	public static final String DATE_NOT_AVAILABLE = "Not available";
	
    /** Identifies the <code>Default group</code>. */
	public static final String	DEFAULT_GROUP = "Main Group";
	
	/** Symbols indicating the mandatory values. */
	public static final String	MANDATORY_SYMBOL = " *";
	
	/** Description of the mandatory symbol. */
	public static final String	MANDATORY_DESCRIPTION = "* indicates the " +
													"required fields.";
	
	/** Identifies the <code>Last name</code> field. */
	public static final String	LAST_NAME = "Last Name";
    
    /** Identifies the <code>First name</code> field. */
	public static final String	FIRST_NAME = "First Name";

    /** Identifies the <code>Last name</code> field. */
	public static final String	INSTITUTION = "Institution";

	/** Text displaying before the owner's permissions. */
	public static final String	OWNER = "Owner: ";
    
    /** Text displaying before the group's permissions. */
    public static final String	GROUP = "Group: ";
    
    /** Text displaying before the world's permissions. */
    public static final String	WORLD = "Others: ";
    
    /** Text describing the <code>Read</code> permission. */
    public static final String	READ = "Read";
    
    /** Text describing the <code>Write</code> permission. */
    public static final String	WRITE = "Write";
    
    /** Text describing the <code>Public</code> permission. */
    public static final String	PUBLIC = "public";
    
    /** Text describing the <code>Public</code> permission. */
    public static final String	PUBLIC_DESCRIPTION = 
    								"Visible to Group members only.";
    
    /** Text describing the <code>Private</code> permission. */
    public static final String	PRIVATE = "private";
    
    
	/** Text displayed before the list of existing groups. */
	public static final String	GROUPS = "Belongs to the following groups: ";
    
    /** Identifies the <code>Wavelengths</code> field. */
	public static final String	WAVELENGTHS = "Emissions wavelengths";
    
	 /** Identifies the <code>Email</code> field. */
    public static final String	EMAIL = "E-mail";
    
    /** String to represent the micron symbol. */
    public static final String 	MICRONS = "(\u00B5m)";
    
    /** String to represent the celcius symbol. */
    public static final String 	CELCIUS = "(\u2103)";
    
    /** String to represent the percent symbol. */
    public static final String 	PERCENT = "(\u0025)";
    
    /** String to represent the millibars symbol. */
    public static final String 	MILLIBARS = "(mb)";
    
    /** Identifies the <code>SizeX</code> field. */
    public static final String 	SIZE_X = "Size X";
    
    /** Identifies the <code>SizeY</code> field. */
    public static final String 	SIZE_Y = "Size Y";
    
    /** Identifies the <code>PixelSizeX</code> field. */
    public static final String 	PIXEL_SIZE_X = "Pixel size X "+MICRONS;
    
    /** Identifies the <code>PixelSizeY</code> field. */
    public static final String 	PIXEL_SIZE_Y = "Pixel size Y "+MICRONS;
    
    /** Identifies the <code>PixelSizeZ</code> field. */
    public static final String 	PIXEL_SIZE_Z = "Pixel size Z "+MICRONS;
    
    /** Identifies the <code>Sections</code> field. */
    public static final String 	SECTIONS = "Number of sections";
    
    /** Identifies the <code>Timepoints</code> field. */
    public static final String 	TIMEPOINTS = "Number of timepoints";

    /** Identifies the <code>PixelType</code> field. */
    public static final String 	PIXEL_TYPE = "Pixel Type";

    /** Identifies the <code>Name</code> field. */
    public static final String  NAME = "Name";
    
    /** Identifies the <code>Acquisition date</code> field. */
    public static final String  ACQUISITION_DATE = "Acquisition date";
    
    /** Identifies the <code>Emission wavelength</code> field. */
    public static final String  EM_WAVE = "Emission";
    
    /** Identifies the <code>Excitation wavelength</code> field. */
    public static final String  EX_WAVE = "Excitation";
    
    /** Identifies the <code>Pin hole size</code> field. */
    public static final String  PIN_HOLE_SIZE = "Pin hole size "+MICRONS;
    
    /** Identifies the <code>ND filter</code> field. */
    public static final String  ND_FILTER = "ND Filter "+PERCENT;
    
    /** Identifies the <code>Fluor</code> field. */
    public static final String 	FLUOR = "Fluor";
    
    /** Identifies the <code>Illumination</code> field. */
    public static final String 	ILLUMINATION = "Illumination";
    
    /** Identifies the <code>Contrast Method</code> field. */
    public static final String 	CONTRAST_METHOD = "Contrast Method";
    
    /** Identifies the <code>Mode</code> field. */
    public static final String 	MODE = "Mode";
    
    /** Identifies the <code>Pockel Cell</code> field. */
    public static final String 	POCKEL_CELL_SETTINGS = "Pockel Cell";
    
    /** Identifies the Objective's <code>Nominal Magnification</code> field. */
	public static final String	NOMINAL_MAGNIFICATION = "Nominal Magnification";
	
	/** 
	 * Identifies the Objective's <code>Calibrated Magnification</code> field.
	 */
	public static final String	CALIBRATED_MAGNIFICATION = "Calibrated " +
			"Magnification";
	
	/** Identifies the Objective's <code>Lens NA</code> field. */
	public static final String	LENSNA = "Lens NA";
	
	/** Identifies the Objective's <code>Working distance</code> field. */
	public static final String	WORKING_DISTANCE = "Working Distance";
	
	/** Identifies the Objective's <code>Working distance</code> field. */
	public static final String	IMMERSION = "Immersion";
	
	/** Identifies the Objective's <code>Correction</code> field. */
	public static final String	CORRECTION = "Correction";
	
	/** Identifies the <code>Correction Collar</code> field. */
	public static final String	CORRECTION_COLLAR = "Correction Collar";

	/** Identifies the Objective's <code>Medium</code> field. */
	public static final String	MEDIUM = "Medium";
	
	/** Identifies the Objective's <code>Refactive index</code> field. */
	public static final String	REFRACTIVE_INDEX = "Refractive index";

	/** Identifies the Environment <code>temperature</code> field. */
	public static final String	TEMPERATURE = "Temperature "+CELCIUS;
	
	/** Identifies the Environment <code>Air pressure</code> field. */
	public static final String	AIR_PRESSURE = "Air Pressure "+MILLIBARS;
	
	/** Identifies the Environment <code>Humidity</code> field. */
	public static final String	HUMIDITY = "Humidy "+PERCENT;
	
	/** Identifies the Environment <code>CO2 Percent</code> field. */
	public static final String	CO2_PERCENT = "CO2 Percent "+PERCENT;
	
	/** Identifies the <code>Model</code> field. */
	public static final String	MODEL = "Model";
	
	/** Identifies the <code>Manufacturer</code> field. */
	public static final String	MANUFACTURER = "Manufacturer";
	
	/** Identifies the <code>Serial number</code> field. */
	public static final String	SERIAL_NUMBER = "Serial Number";
	
	/** Identifies the Stage label <code>Position X</code> field. */
	public static final String	POSITION_X = "Position X";
	
	/** Identifies the Stage label <code>Position Y</code> field. */
	public static final String	POSITION_Y = "Position Y";
	
	/** Identifies the Stage label <code>Position Z</code> field. */
	public static final String	POSITION_Z = "Position Z";
	
	/** Identifies the <code>Type</code> field. */
	public static final String	TYPE = "Type";
	
	/** Identifies the <code>Voltage</code> field. */
	public static final String	VOLTAGE = "Voltage";
	
	/** Identifies the <code>Gain</code> field. */
	public static final String	GAIN = "Gain";
	
	/** Identifies the <code>Offset</code> field. */
	public static final String	OFFSET = "Offset";
	
	/** Identifies the <code>Read out rate</code> field. */
	public static final String	READ_OUT_RATE = "Read out rate";
	
	/** Identifies the <code>Binning</code> field. */
	public static final String	BINNING = "Binning";
	
	/** Identifies the <code>Amplication</code> field. */
	public static final String	AMPLIFICATION = "Amplification Gain";
	
	/** Identifies the <code>Zoom</code> field. */
	public static final String	ZOOM = "Zoom";
	
	/** Identifies the <code>Exposure</code> field. */
	public static final String	EXPOSURE_TIME = "Exposure Time";
	
	/** Identifies the <code>Delta</code> field. */
	public static final String	DELTA_T = "DeltaT";
	
	/** Identifies the <code>Power</code> field of light source. */
	public static final String	POWER = "Power";
	
	/** Identifies the <code>type</code> field of the light. */
	public static final String	LIGHT_TYPE = "Light";
	
	/** Identifies the <code>type</code> field of the light. */
	public static final String	TUNEABLE = "Tuneable";
	
	/** Identifies the <code>type</code> field of the light. */
	public static final String	PULSE = "Pulse";
	
	/** Identifies the <code>type</code> field of the light. */
	public static final String	POCKEL_CELL = "PockelCell";
	
	/** Identifies the <code>Repetition rate</code> of the laser. */
	public static final String	REPETITION_RATE = "Repetition Rate (Hz)";
	
	/** Identifies the <code>Repetition rate</code> of the laser. */
	public static final String	PUMP = "Pump";
	
	/** Identifies the <code>Wavelength</code> of the laser. */
	public static final String	WAVELENGTH = "Wavelength";
	
	/** Identifies the <code>Frequency Multiplication</code> of the laser. */
	public static final String	FREQUENCY_MULTIPLICATION =
									"Frequency Multiplication";
	
	/** Identifies the <code>Iris</code> of the objective. */
	public static final String	IRIS = "Iris";
	
	/** Identifies the unset fields. */
	public static final String	NOT_SET = "NotSet";
	
	/** The map identifying the pixels value and its description. */
	public static final Map<String, String> PIXELS_TYPE_DESCRIPTION;
	
	/** The map identifying the pixels value and its description. */
	public static final Map<String, String> PIXELS_TYPE;
	
	static {
		LETTERS = new HashMap<Integer, String>();
		LETTERS.put(1, "A");
		LETTERS.put(2, "B");
		LETTERS.put(3, "C");
		LETTERS.put(4, "D");
		LETTERS.put(5, "E");
		LETTERS.put(6, "F");
		LETTERS.put(7, "G");
		LETTERS.put(8, "H");
		LETTERS.put(9, "I");
		LETTERS.put(10, "J");
		LETTERS.put(11, "K");
		LETTERS.put(12, "L");
		LETTERS.put(13, "M");
		LETTERS.put(14, "N");
		LETTERS.put(15, "O");
		LETTERS.put(16, "P");
		LETTERS.put(17, "Q");
		LETTERS.put(18, "R");
		LETTERS.put(19, "S");
		LETTERS.put(20, "T");
		LETTERS.put(21, "U");
		LETTERS.put(22, "V");
		LETTERS.put(23, "W");
		LETTERS.put(24, "X");
		LETTERS.put(25, "Y");
		LETTERS.put(26, "Z");
		
		PIXELS_TYPE_DESCRIPTION = new LinkedHashMap<String, String>();
		PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.INT_8, 
					"Signed 8-bit (1byte)");
		PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.UINT_8, 
					"Unsigned 8-bit (1 byte)");
		PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.INT_16, 
					"Signed 16-bit (2byte)");
		PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.UINT_16, 
					"Unsigned 16-bit(2byte)");
		PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.INT_32, 
					"Signed 32-bit(4byte)");
		PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.UINT_32, 
					"Unsigned 32-bit(4byte)");
		PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.FLOAT, 
					"Floating precision");
		PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.DOUBLE, 
					"Double precision");
		PIXELS_TYPE = new LinkedHashMap<String, String>();
		Iterator<String> i = PIXELS_TYPE_DESCRIPTION.keySet().iterator();
		String key;
		while (i.hasNext()) {
			key = i.next();
			PIXELS_TYPE.put(PIXELS_TYPE_DESCRIPTION.get(key), key);
		}
	}

    /**
     * Transforms the specified {@link ExperimenterData} object into 
     * a visualization form.
     * 
     * @param data The {@link ExperimenterData} object to transform.
     * @return The map whose keys are the field names, and the values 
     * 			the corresponding fields' values.
     */
    public static Map<String, String> transformExperimenterData(
    									ExperimenterData data)
    {
        LinkedHashMap<String, String> details = 
        							new LinkedHashMap<String, String>(2);
        if (data == null) {
            details.put(OWNER, "");
            details.put(EMAIL, "");
        } else {
            try {
                details.put(OWNER, data.getFirstName()+" "+data.getLastName());
                details.put(EMAIL, data.getEmail());
            } catch (Exception e) {
                details.put(OWNER, "");
                details.put(EMAIL, "");
            }
        }
        return details;
    }
    
    /**
     * Transforms the specified {@link PixelsData} object into 
     * a visualization form.
     * 
     * @param data The {@link PixelsData} object to transform.
     * @return The map whose keys are the field names, and the values 
     * 			the corresponding fields' values.
     */
    public static Map<String, String> transformPixelsData(PixelsData data)
    {
        LinkedHashMap<String, String> details = 
        						new LinkedHashMap<String, String>(9);
        if (data == null) {
            details.put(SIZE_X, "");
            details.put(SIZE_Y, "");
            details.put(SECTIONS, "");
            details.put(TIMEPOINTS, "");
            details.put(PIXEL_SIZE_X, "");
            details.put(PIXEL_SIZE_Y, "");
            details.put(PIXEL_SIZE_Z, "");
            details.put(PIXEL_TYPE, "");  
        } else {
            NumberFormat nf = NumberFormat.getInstance();
            details.put(SIZE_X, ""+data.getSizeX());
            details.put(SIZE_Y, ""+data.getSizeY());
            details.put(SECTIONS, ""+data.getSizeZ());
            details.put(TIMEPOINTS, ""+data.getSizeT());
            try {
                details.put(PIXEL_SIZE_X, nf.format(data.getPixelSizeX()));
                details.put(PIXEL_SIZE_Y, nf.format(data.getPixelSizeY()));
                details.put(PIXEL_SIZE_Z, nf.format(data.getPixelSizeZ()));
                details.put(PIXEL_TYPE, 
                		PIXELS_TYPE_DESCRIPTION.get(""+data.getPixelType())); 
            } catch (Exception e) {
                details.put(PIXEL_SIZE_X, "");
                details.put(PIXEL_SIZE_Y, "");
                details.put(PIXEL_SIZE_Z, "");
                details.put(PIXEL_TYPE, ""); 
            }
        }
        details.put(WAVELENGTHS, "");  
        return details;
    }
      
    /**
     * Transforms the specified {@link ImageData} object into 
     * a visualization form.
     * 
     * @param image The {@link ImageData} object to transform.
     * @return The map whose keys are the field names, and the values 
     * 			the corresponding fields' values.
     */
    public static Map<String, String> transformImageData(ImageData image)
    {
        LinkedHashMap<String, String> details = 
        						new LinkedHashMap<String, String>(10);
        if (image == null) {
        	details.put(SIZE_X, "");
            details.put(SIZE_Y, "");
            details.put(SECTIONS, "");
            details.put(TIMEPOINTS, "");
            details.put(PIXEL_SIZE_X, "");
            details.put(PIXEL_SIZE_Y, "");
            details.put(PIXEL_SIZE_Z, "");
            details.put(PIXEL_TYPE, "");  
            details.put(WAVELENGTHS, "");
            details.put(ACQUISITION_DATE, DATE_NOT_AVAILABLE);
            return details;
        }
        PixelsData data = image.getDefaultPixels();
	
        if (data == null) {
            details.put(SIZE_X, "");
            details.put(SIZE_Y, "");
            details.put(SECTIONS, "");
            details.put(TIMEPOINTS, "");
            details.put(PIXEL_SIZE_X, "");
            details.put(PIXEL_SIZE_Y, "");
            details.put(PIXEL_SIZE_Z, "");
            details.put(PIXEL_TYPE, "");  
        } else {
            NumberFormat nf = NumberFormat.getInstance();
            details.put(SIZE_X, ""+data.getSizeX());
            details.put(SIZE_Y, ""+data.getSizeY());
            details.put(SECTIONS, ""+data.getSizeZ());
            details.put(TIMEPOINTS, ""+data.getSizeT());
            try {
                details.put(PIXEL_SIZE_X, nf.format(data.getPixelSizeX()));
                details.put(PIXEL_SIZE_Y, nf.format(data.getPixelSizeY()));
                details.put(PIXEL_SIZE_Z, nf.format(data.getPixelSizeZ()));
                details.put(PIXEL_TYPE, 
                		PIXELS_TYPE_DESCRIPTION.get(""+data.getPixelType())); 
            } catch (Exception e) {
                details.put(PIXEL_SIZE_X, "");
                details.put(PIXEL_SIZE_Y, "");
                details.put(PIXEL_SIZE_Z, "");
                details.put(PIXEL_TYPE, ""); 
            }
        }
        details.put(WAVELENGTHS, ""); 
        Timestamp date = getAcquisitionTime(image);
        if (date == null) 
        	details.put(ACQUISITION_DATE, DATE_NOT_AVAILABLE);
        else 
        	details.put(ACQUISITION_DATE, UIUtilities.formatTime(date));
        return details;
    }

    /**
     * Returns the creation time associate to the image.
     * 
     * @param image The image to handle.
     * @return See above.
     */
    public static Timestamp getAcquisitionTime(ImageData image)
    {
    	if (image == null) return null;
    	Timestamp date = null;
        try {
        	date = image.getAcquisitionDate();
		} catch (Exception e) {}
		return date;
    }
    
    /**
     * Formats the specified experimenter.
     * 
     * @param exp The experimenter to format.
     * @return See above.
     */
	public static String formatExperimenter(ExperimenterData exp)
	{
		if (exp == null) return "";
		return exp.getFirstName()+" "+exp.getLastName();
	}
	
	/**
     * Transforms the specified {@link ExperimenterData} object into 
     * a visualization form.
     * 
     * @param data The {@link ExperimenterData} object to transform.
     * @return The map whose keys are the field names, and the values 
     * 			the corresponding fields' values.
     */
    public static Map<String, String> convertExperimenter(ExperimenterData data)
    {
        LinkedHashMap<String, String> details = 
        							new LinkedHashMap<String, String>(3);
        if (data == null) {
            details.put(FIRST_NAME, "");
            details.put(LAST_NAME, "");
            details.put(EMAIL, "");
            details.put(INSTITUTION, "");
        } else {
            try {
                details.put(FIRST_NAME, data.getFirstName());
            } catch (Exception e) {
            	details.put(FIRST_NAME, "");
            }
            try {
                details.put(LAST_NAME, data.getLastName());
            } catch (Exception e) {
            	details.put(LAST_NAME, "");
            }
            try {
                details.put(EMAIL, data.getEmail());
            } catch (Exception e) {
            	details.put(EMAIL, "");
            }
            try {
                details.put(INSTITUTION, data.getInstitution());
            } catch (Exception e) {
            	details.put(INSTITUTION, "");
            }
        }
        return details;
    }
    
    /**
	 * Returns <code>true</code> it the object has been annotated,
	 * <code>false</code> otherwise.
	 * 
	 * @param object	The object to handle.
	 * @return See above.
	 */
	public static boolean isAnnotated(Object object)
	{
		if (object == null) return false;
		Map<Long, Long> counts = null;
		if (object instanceof DatasetData) 
			counts = ((DatasetData) object).getAnnotationsCounts();
		else if (object instanceof ProjectData)
			counts = ((ProjectData) object).getAnnotationsCounts();
		else if (object instanceof ImageData)
			counts = ((ImageData) object).getAnnotationsCounts();	
		else if (object instanceof ScreenData)
			counts = ((ScreenData) object).getAnnotationsCounts();	
		else if (object instanceof PlateData)
			counts = ((PlateData) object).getAnnotationsCounts();	
		else if (object instanceof WellData)
			counts = ((WellData) object).getAnnotationsCounts();	
		if (counts == null || counts.size() == 0) return false;
		return true;
	}
	
	/**
	 * Returns <code>true</code> it the object has been updated by the current
	 * user, <code>false</code> otherwise.
	 * 
	 * @param object	The object to handle.
	 * @param userID	The id of the current user.
	 * @return See above.
	 */
	public static boolean isAnnotatedByCurrentUser(Object object, long userID)
	{
		if (object == null) return false;
		Map<Long, Long> counts = null;
		if (object instanceof DatasetData) 
			counts = ((DatasetData) object).getAnnotationsCounts();
		else if (object instanceof ProjectData) 
			counts = ((ProjectData) object).getAnnotationsCounts();
		else if (object instanceof ImageData)
			counts = ((ImageData) object).getAnnotationsCounts();
		else if (object instanceof ScreenData)
			counts = ((ScreenData) object).getAnnotationsCounts();	
		else if (object instanceof PlateData)
			counts = ((PlateData) object).getAnnotationsCounts();	
		else if (object instanceof WellData)
			counts = ((WellData) object).getAnnotationsCounts();
		if (counts == null || counts.size() == 0) return false;
		return counts.keySet().contains(userID);
	}
	
	/**
	 * Returns <code>true</code> it the object has been updated by an
	 * user other than the current user, <code>false</code> otherwise.
	 * 
	 * @param object	The object to handle.
	 * @param userID	The id of the current user.
	 * @return See above.
	 */
	public static boolean isAnnotatedByOtherUser(Object object, long userID)
	{
		if (object == null) return false;
		Map<Long, Long> counts = null;
		if (object instanceof ImageData)
			counts = ((ImageData) object).getAnnotationsCounts();
		else if (object instanceof DatasetData)
			counts = ((DatasetData) object).getAnnotationsCounts();	
		else if (object instanceof ProjectData) 
			counts = ((ProjectData) object).getAnnotationsCounts();
		else if (object instanceof ScreenData)
			counts = ((ScreenData) object).getAnnotationsCounts();	
		else if (object instanceof PlateData)
			counts = ((PlateData) object).getAnnotationsCounts();	
		else if (object instanceof WellData)
			counts = ((WellData) object).getAnnotationsCounts();
		if (counts == null || counts.size() == 0) return false;
		Set set = counts.keySet();
		if (set.size() > 1) return true;
		return !set.contains(userID);
	}
	
	/**
     * Returns the partial name of the image's name
     * 
     * @param originalName The original name.
     * @return See above.
     */
    public static String getPartialName(String originalName)
    {
    	String[] l = UIUtilities.splitString(originalName);
    	if (l != null) {
    		int n = l.length;
    		switch (n) {
				case 0: return originalName;
				case 1: return l[0];
				case 2: return l[n-2]+"/"+l[n-1];
				default: 
					return UIUtilities.DOTS+l[n-2]+"/"+l[n-1]; 
			}
    	}
        return originalName;
    }

    /**
     * Returns <code>true</code> if the specified data object is readable,
     * <code>false</code> otherwise, depending on the permission.
     * 
     * @param ho        The data object to check.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.
     * @return See above.
     */
    public static boolean isReadable(Object ho, long userID, long groupID)
    {
    	if (ho == null || ho instanceof ExperimenterData || 
    		ho instanceof String)
        	return false;
    	if (!(ho instanceof DataObject)) return false;
    	DataObject data = (DataObject) ho;
        PermissionData permissions = data.getPermissions();
        if (userID == data.getOwner().getId())
            return permissions.isUserRead();
        /*
        Set groups = ho.getOwner().getGroups();
        Iterator i = groups.iterator();
        long id = -1;
        boolean groupRead = false;
        while (i.hasNext()) {
            id = ((GroupData) i.next()).getId();
            if (groupID == id) {
                groupRead = true;
                break;
            }
        }
        if (groupRead) return permissions.isGroupRead();
        return permissions.isWorldRead();
        */ 
        return permissions.isGroupRead();
    }
    
    /**
     * Returns <code>true</code> if the specified data object is writable,
     * <code>false</code> otherwise, depending on the permission.
     * 
     * @param ho        The data object to check.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.
     * @return See above.
     */
    public static boolean isWritable(Object ho, long userID, long groupID)
    {
    	if (ho == null || ho instanceof ExperimenterData || 
    		ho instanceof String)
    		return false;
    	if (!(ho instanceof DataObject)) return false;
    	DataObject data = (DataObject) ho;
        PermissionData permissions = data.getPermissions();
        if (userID == data.getOwner().getId())
            return permissions.isUserWrite();
        /*
        Set groups = ho.getOwner().getGroups();
        Iterator i = groups.iterator();
        long id = -1;
        boolean groupRead = false;
        while (i.hasNext()) {
            id = ((GroupData) i.next()).getId();
            if (groupID == id) {
                groupRead = true;
                break;
            }
        }
        if (groupRead) return permissions.isGroupWrite();
        return permissions.isWorldWrite();
        */
        return permissions.isGroupWrite();
    }
    
    /**
     * Returns <code>true</code> if the specified data object is writabla by 
     * group members,
     * <code>false</code> otherwise, depending on the permission.
     * 
     * @param ho        The data object to check.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.
     * @return See above.
     */
    public static boolean isGroupWritable(Object ho)
    {
    	if (ho == null || ho instanceof ExperimenterData || 
        		ho instanceof String) return false;
    	if (!(ho instanceof DataObject)) return false;
    	DataObject data = (DataObject) ho;
    	PermissionData permissions = data.getPermissions();
    	return permissions.isGroupWrite();
    }
    
    /**
     * Removes the extension if any of the passed image's name.
     * 
     * @param originalName The name to handle.
     * @return See above.
     */
    public static String removeFileExtension(String originalName)
    {
    	String name = originalName;
    	String[] l = UIUtilities.splitString(originalName);
    	if (l != null) {
    		 int n = l.length;
             if (n >= 1) name = l[n-1]; 
    	}
    	   	
    	if (Pattern.compile(".").matcher(name).find()) {
    		l = name.split("\\.");
    		if (l.length >= 1) {
    			name = "";
    			int n = l.length-1;
        		for (int i = 0; i < n; i++) {
    				name += l[i];
    				if (i < (n-1)) name += ".";
    			}
    		}
    	}
        return name;
    }
    
    /**
     * Transforms the specified channel information.
     * 
     * @param data  The object to transform.
     * @return      The map whose keys are the field names, and the values 
     *              the corresponding fields' values.
     */
    public static Map<String, Object> transformChannelData(ChannelData data)
    {
        LinkedHashMap<String, Object> 
        		details = new LinkedHashMap<String, Object>(10);
        List<String> notSet = new ArrayList<String>();
        details.put(NAME, "");
        details.put(EM_WAVE, Integer.valueOf(0));
        details.put(EX_WAVE, Integer.valueOf(0));
        details.put(ND_FILTER, Float.valueOf(0));
        details.put(PIN_HOLE_SIZE, Float.valueOf(0));
        details.put(FLUOR, "");
        details.put(ILLUMINATION, "");
        details.put(CONTRAST_METHOD, "");
        details.put(MODE, "");
        details.put(POCKEL_CELL_SETTINGS, Integer.valueOf(0));
        if (data == null) {
        	notSet.add(NAME);
        	notSet.add(EM_WAVE);
        	notSet.add(EX_WAVE);
        	notSet.add(ND_FILTER);
        	notSet.add(PIN_HOLE_SIZE);
        	notSet.add(FLUOR);
        	notSet.add(ILLUMINATION);
        	notSet.add(CONTRAST_METHOD);
        	notSet.add(MODE);
        	notSet.add(POCKEL_CELL_SETTINGS);
        	details.put(NOT_SET, notSet);
        	return details;
        }
        String s = data.getName();
		if (s == null || s.trim().length() == 0) 
			notSet.add(NAME);
        details.put(NAME, s);
        int i = data.getEmissionWavelength();
        if (i < 0) {
        	i = 0;
        	notSet.add(EM_WAVE);
        }
    	details.put(EM_WAVE, i);
    	i = data.getExcitationWavelength();
        if (i < 0) {
        	i = 0;
        	notSet.add(EX_WAVE);
        }
    	details.put(EX_WAVE, i);
    	float f = data.getNDFilter();
    	if (f < 0) {
    		f = 0;
        	notSet.add(ND_FILTER);
    	}
    	details.put(ND_FILTER, f*100);
    	f = data.getPinholeSize();
    	if (f < 0) {
    		f = 0;
        	notSet.add(PIN_HOLE_SIZE);
    	}
        details.put(PIN_HOLE_SIZE, f);
        s = data.getFluor();
		if (s == null || s.trim().length() == 0) 
			notSet.add(FLUOR);
        details.put(FLUOR, s);
        s = data.getIllumination();
		if (s == null || s.trim().length() == 0) 
			notSet.add(ILLUMINATION);
        details.put(ILLUMINATION, s);
        s = data.getContrastMethod();
		if (s == null || s.trim().length() == 0) 
			notSet.add(CONTRAST_METHOD);
        details.put(CONTRAST_METHOD, s);
        s = data.getMode();
		if (s == null || s.trim().length() == 0) 
			notSet.add(MODE);
        details.put(MODE, s);
        i = data.getPockelCell();
        if (i < 0) {
        	i = 0;
        	notSet.add(POCKEL_CELL_SETTINGS);
        }
        details.put(POCKEL_CELL_SETTINGS, i);
        details.put(NOT_SET, notSet);
        return details;
    }
    
    /**
     * Transforms the passed objective.
     * 
     * @param data The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformObjective(
    		ImageAcquisitionData data)
    {
    	LinkedHashMap<String, Object> 
    			details = new LinkedHashMap<String, Object>(9);
    	List<String> notSet = new ArrayList<String>();
    	details.put(MODEL, "");
    	details.put(MANUFACTURER, "");
    	details.put(SERIAL_NUMBER, "");
    	details.put(NOMINAL_MAGNIFICATION, Integer.valueOf(0));
    	details.put(CALIBRATED_MAGNIFICATION,Float.valueOf(0));
        details.put(LENSNA, new Float(0));
        details.put(IMMERSION, "");
        details.put(CORRECTION, "");
        details.put(WORKING_DISTANCE, Float.valueOf(0));
        details.put(CORRECTION_COLLAR, Float.valueOf(0));
    	details.put(MEDIUM, "");
    	details.put(REFRACTIVE_INDEX, Float.valueOf(0));
    	details.put(IRIS, null);
        if (data == null) {
        	notSet.add(MODEL);
        	notSet.add(MANUFACTURER);
        	notSet.add(SERIAL_NUMBER);
        	notSet.add(NOMINAL_MAGNIFICATION);
        	notSet.add(CALIBRATED_MAGNIFICATION);
        	notSet.add(LENSNA);
        	notSet.add(IMMERSION);
        	notSet.add(CORRECTION);
        	notSet.add(WORKING_DISTANCE);
        	notSet.add(CORRECTION_COLLAR);
    		notSet.add(MEDIUM);
    		notSet.add(REFRACTIVE_INDEX);
    		notSet.add(IRIS);
        	details.put(NOT_SET, notSet);
        	return details;
        }
        Object o = data.hasIris();
    	if (o == null) {
    		notSet.add(IRIS);
    	}
    	details.put(IRIS, o);
        String s = data.getModel();
		if (s == null || s.trim().length() == 0) 
			notSet.add(MODEL);
		details.put(MODEL, s);
		s = data.getManufacturer();
		if (s == null || s.trim().length() == 0) 
			notSet.add(MANUFACTURER);
		details.put(MANUFACTURER, s);
		s = data.getSerialNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(SERIAL_NUMBER);
		details.put(SERIAL_NUMBER, s);
		int i = data.getNominalMagnification();
		if (i < 0) {
			i = 0;
			notSet.add(NOMINAL_MAGNIFICATION);
		}
		details.put(NOMINAL_MAGNIFICATION, i);
		float f = data.getCalibratedMagnification();
 		if (f < 0) {
 			f = 0;
 			notSet.add(CALIBRATED_MAGNIFICATION);
 		}
 		details.put(CALIBRATED_MAGNIFICATION, f);
 		f = data.getLensNA();
 		if (f < 0) {
 			f = 0;
 			notSet.add(LENSNA);
 		}
 		details.put(LENSNA, f);
 		s = data.getImmersion();
 		if (s == null || s.trim().length() == 0) 
			notSet.add(IMMERSION);
		details.put(IMMERSION, s);
 		s = data.getCorrection();
 		if (s == null || s.trim().length() == 0) 
			notSet.add(CORRECTION);
		details.put(CORRECTION, s);
 		f = data.getWorkingDistance();
 		if (f < 0) {
 			f = 0;
 			notSet.add(WORKING_DISTANCE);
 		}
 		details.put(WORKING_DISTANCE, f);
 		f = data.getCorrectionCollar();
    	if (f < 0) {
    		f = 0;
    		notSet.add(CORRECTION_COLLAR);
    	}
    	details.put(CORRECTION_COLLAR, f);
    	s = data.getMedium();
    	if (s == null || s.trim().length() == 0) 
    		notSet.add(MEDIUM);
    	details.put(MEDIUM, s);
    	f = data.getRefractiveIndex();
    	if (f < 0) {
    		f = 0;
    		notSet.add(REFRACTIVE_INDEX);
    	}
    	details.put(REFRACTIVE_INDEX, f);
 		details.put(NOT_SET, notSet);
    	return details;
    }
  
    /**
     * Transforms the acquisition's condition.
     * 
     * @param data The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformImageEnvironment(
    		ImageAcquisitionData data)
    {
    	LinkedHashMap<String, Object> 
			details = new LinkedHashMap<String, Object>(4);
    	List<String> notSet = new ArrayList<String>();
    	details.put(TEMPERATURE, new Float(0));
    	details.put(AIR_PRESSURE, new Float(0));
    	details.put(HUMIDITY, new Float(0));
    	details.put(CO2_PERCENT, new Float(0));
    	
    	if (data == null) {
    		notSet.add(TEMPERATURE);
    		notSet.add(AIR_PRESSURE);
    		notSet.add(HUMIDITY);
    		notSet.add(CO2_PERCENT);
    		details.put(NOT_SET, notSet);
    		return details;
    	}
    	Object o = data.getTemperature();
    	float f = 0;
    	if (o == null) {
    		notSet.add(TEMPERATURE);
    	} else f = (Float) o;
    	details.put(TEMPERATURE, f);
    	f = data.getAirPressure();
    	if (f < 0) {
    		notSet.add(AIR_PRESSURE);
    		f = 0;
    	}
    	details.put(AIR_PRESSURE, f);
    	f = data.getHumidity();
    	if (f < 0) {
    		notSet.add(HUMIDITY);
    		f = 0;
    	}
    	details.put(HUMIDITY, f*100);
    	f = data.getCo2Percent();
    	if (f < 0) {
    		notSet.add(CO2_PERCENT);
    		f = 0;
    	}
    	details.put(CO2_PERCENT, f*100);
    	details.put(NOT_SET, notSet);
    	return details;
    }
    
    /**
     * Transforms the position of the image in the microscope's frame.
     * 
     * @param data The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformStageLabel(
    		ImageAcquisitionData data)
    {
    	LinkedHashMap<String, Object> 
			details = new LinkedHashMap<String, Object>(4);
    	List<String> notSet = new ArrayList<String>();
    	details.put(NAME, "");
    	details.put(POSITION_X, new Float(0));
    	details.put(POSITION_Y, new Float(0));
    	details.put(POSITION_Z, new Float(0));
    	
    	if (data == null) {
    		notSet.add(NAME);
    		notSet.add(POSITION_X);
    		notSet.add(POSITION_Y);
    		notSet.add(POSITION_Z);
    		details.put(NOT_SET, notSet);
    		return details;
    	}
    	String s = data.getLabelName();
		if (s == null || s.trim().length() == 0) 
			notSet.add(NAME);
    	details.put(NAME, s);
    	Object o = data.getPositionX();
    	float f = 0;
    	if (o == null) {
    		notSet.add(POSITION_X);
    	} else f = (Float) o;
    	details.put(POSITION_X, f);
    	f = 0;
    	if (o == null) {
    		notSet.add(POSITION_Y);
    	} else f = (Float) o;
    	details.put(POSITION_Y, f);
    	f = 0;
    	if (o == null) {
    		notSet.add(POSITION_Z);
    	} else f = (Float) o;
    	details.put(POSITION_Z, f);
    	
    	details.put(NOT_SET, notSet);
    	return details;
    }
    
    /**
     * Transforms the detector and its settings.
     * 
     * @param kind 	The kind of light source or <code>null</code>.
     * @param data	The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformLightSource(String kind, 
    		ChannelAcquisitionData data)
    {
    	LinkedHashMap<String, Object> 
			details = new LinkedHashMap<String, Object>();

    	List<String> notSet = new ArrayList<String>();
    	details.put(MODEL, "");
    	details.put(MANUFACTURER, "");
    	details.put(SERIAL_NUMBER, "");
    	details.put(LIGHT_TYPE, "");
    	details.put(POWER, new Float(0));
    	details.put(TYPE, "");

    	if (data == null) {
    		notSet.add(MODEL);
    		notSet.add(MANUFACTURER);
    		notSet.add(SERIAL_NUMBER);
    		notSet.add(LIGHT_TYPE);
    		notSet.add(POWER);
    		notSet.add(TYPE);
    		details.put(NOT_SET, notSet);
        	return details;
    		
    	}
    	String s = data.getLightSourceModel();
		if (s == null || s.trim().length() == 0) 
			notSet.add(MODEL);
		details.put(MODEL, s);
		s = data.getLightSourceManufacturer();
		if (s == null || s.trim().length() == 0) 
			notSet.add(MANUFACTURER);
		details.put(MANUFACTURER, s);
		s = data.getLightSourceSerialNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(SERIAL_NUMBER);
		details.put(SERIAL_NUMBER, s);
    	s = data.getLightSourceKind();
    	details.put(LIGHT_TYPE, s);
    	float f = data.getLightSourcePower();
    	if (f < 0) {
    		notSet.add(POWER);
    		f = 0;
    	}
    	details.put(POWER, f);
    	s = data.getLightType();
    	if (s == null || s.trim().length() == 0) 
			notSet.add(TYPE);
        details.put(TYPE, s); 
        if (kind == null) s = data.getLightSourceKind();
        else s = kind;
        if (ChannelAcquisitionData.LASER.equals(s)) {
        	s = data.getLaserMedium();
        	if (s == null || s.trim().length() == 0) 
    			notSet.add(MEDIUM);
        	details.put(MEDIUM, s);
        	
        	int i = data.getLaserWavelength();
        	if (i < 0) {
        		i = 0;
        		notSet.add(WAVELENGTH);
        	}
        	details.put(WAVELENGTH, i); 
			i = data.getLaserFrequencyMultiplication();
			if (i < 0) {
        		i = 0;
        		notSet.add(FREQUENCY_MULTIPLICATION);
        	}
        	details.put(FREQUENCY_MULTIPLICATION, i); 
        	Object o = data.getLaserTuneable();
        	if (o == null) {
        		notSet.add(TUNEABLE);
        	}
        	details.put(TUNEABLE, o);
        	
        	s = data.getLaserPulse();
        	if (s == null || s.trim().length() == 0) 
    			notSet.add(PULSE);
        	details.put(PULSE, s);
        	f = data.getLaserRepetitionRate();
			if (f < 0) {
        		f = 0;
        		notSet.add(REPETITION_RATE);
        	}
        	details.put(REPETITION_RATE, f);
        	o = data.getLaserPockelCell();
        	if (o == null) {
        		notSet.add(POCKEL_CELL);
        	}
        	details.put(POCKEL_CELL, o); 
        	
        	//details.put(PUMP, data.hasPump());
        }

		details.put(NOT_SET, notSet);
    	return details;
    }
    
    /**
     * Transforms the detector.
     * 
     * @param data  The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformDetector(
    		ChannelAcquisitionData data)
    {
    	LinkedHashMap<String, Object> 
			details = new LinkedHashMap<String, Object>(11);
    	List<String> notSet = new ArrayList<String>();
    	details.put(MODEL, "");
    	details.put(MANUFACTURER, "");
    	details.put(SERIAL_NUMBER, "");
    	details.put(GAIN, new Float(0));
    	details.put(VOLTAGE, new Float(0));
        details.put(OFFSET, new Float(0));
        details.put(READ_OUT_RATE, new Float(0));
        details.put(BINNING, "");
        details.put(ZOOM, new Float(0));
        details.put(AMPLIFICATION, "");
        details.put(TYPE, ""); 
        if (data == null) {
        	notSet.add(MODEL);
        	notSet.add(MANUFACTURER);
        	notSet.add(SERIAL_NUMBER);
        	notSet.add(GAIN);
        	notSet.add(VOLTAGE);
        	notSet.add(READ_OUT_RATE);
        	notSet.add(BINNING);
        	notSet.add(ZOOM);
        	notSet.add(AMPLIFICATION);
        	notSet.add(TYPE);
        	details.put(NOT_SET, notSet);
        	return details;
        }
        String s = data.getDetectorModel();
		if (s == null || s.trim().length() == 0) 
			notSet.add(MODEL);
		details.put(MODEL, s);
		s = data.getDetectorManufacturer();
		if (s == null || s.trim().length() == 0) 
			notSet.add(MANUFACTURER);
		details.put(MANUFACTURER, s);
		s = data.getDetectorSerialNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(SERIAL_NUMBER);
		details.put(SERIAL_NUMBER, s);
    	float f = data.getDetectorSettingsGain();
    	if (f > 0)  details.put(GAIN, f);
    	else {
    		f = data.getDetectorGain();
    		if (f < 0) {
    			f = 0;
    			notSet.add(GAIN);
    		}
    		details.put(GAIN, f);
    	}
    	f = data.getDetectorSettingsVoltage();
    	if (f > 0)  details.put(VOLTAGE, f);
    	else {
    		f = data.getDetectorVoltage();
    		if (f < 0) {
    			f = 0;
    			notSet.add(VOLTAGE);
    		}
    		details.put(VOLTAGE, f);
    	}
    	f = data.getDetectorSettingsOffset();
    	if (f > 0)  details.put(OFFSET, f);
    	else {
    		f = data.getDetectorOffset();
    		if (f < 0) {
    			f = 0;
    			notSet.add(OFFSET);
    		}
    		details.put(OFFSET, f);
    	}
    	f = data.getDetectorSettingsReadOutRate();
    	if (f < 0) {
			f = 0;
			notSet.add(READ_OUT_RATE);
		}
        details.put(READ_OUT_RATE, f);
        s = data.getDetectorSettingsBinning();
        if (s == null || s.trim().length() == 0) 
			notSet.add(BINNING);
        details.put(BINNING, s);
        f = data.getDetectorZoom();
    	if (f < 0) {
			f = 0;
			notSet.add(ZOOM);
		}
        details.put(ZOOM, f);
        f = data.getDetectorAmplificationGain();
    	if (f < 0) {
			f = 0;
			notSet.add(AMPLIFICATION);
		}
        details.put(AMPLIFICATION, f);
        s = data.getDetectorType();
        if (s == null || s.trim().length() == 0) 
			notSet.add(TYPE);
        details.put(TYPE, s); 
    	details.put(NOT_SET, notSet);
    	return details;
    }
    
    /**
     * Transforms the plane information.
     * 
     * @param plane The plane to transform.
     * @return See above.
     */
    public static Map<String, Object> transformPlaneInfo(PlaneInfo plane)
    {
    	LinkedHashMap<String, Object> 
		details = new LinkedHashMap<String, Object>(4);
    	details.put(DELTA_T, new Float(0));
		details.put(EXPOSURE_TIME, new Float(0));
		details.put(POSITION_X, new Float(0));
		details.put(POSITION_Y, new Float(0));
		details.put(POSITION_Z, new Float(0));
    	if (plane != null) {
    		RFloat o = plane.getDeltaT();
    		if (o != null) 
    			details.put(DELTA_T, o.getValue());	
    		o = plane.getExposureTime();
    		if (o != null) 
    			details.put(EXPOSURE_TIME, o.getValue());
    		o = plane.getPositionX();
    		if (o != null) 
    			details.put(POSITION_X, o.getValue());
    		o = plane.getPositionY();
    		if (o != null) 
    			details.put(POSITION_Y, o.getValue());
    		o = plane.getPositionZ();
    		if (o != null) 
    			details.put(POSITION_Z, o.getValue());
    	}
    	return details;
    }
    
    /**
	 * Initialises a <code>JComboBox</code>.
	 * 
	 * @param values 		 The values to display.
	 * @param decrement 	 The value by which the font size is reduced.
	 * @param backgoundColor The backgoundColor of the combo box.
	 * @return See above.
	 */
    public static OMEComboBox createComboBox(Object[] values, int decrement, 
    		Color backgoundColor)
	{
    	OMEComboBox box = new OMEComboBox(values);
    	if (backgoundColor != null)
    		box.setBackground(backgoundColor);
		OMEComboBoxUI ui = new OMEComboBoxUI();
		ui.setBackgroundColor(box.getBackground());
	    ui.installUI(box);
		box.setUI(ui);
		Font f = box.getFont();
		int size = f.getSize()-decrement;
		box.setBorder(null);
		box.setFont(f.deriveFont(Font.ITALIC, size));
		return box;
	}
    
    /**
	 * Initialises a <code>JComboBox</code>.
	 * 
	 * @param values 	The values to display.
	 * @param decrement The value by which the font size is reduced.
	 * @return See above.
	 */
    public static OMEComboBox createComboBox(Object[] values, int decrement)
	{
    	return createComboBox(values, decrement, UIUtilities.BACKGROUND_COLOR);
	}
    
    /**
	 * Initialises a <code>JComboBox</code>.
	 * 
	 * @param values The values to display.
	 * @return See above.
	 */
    public static OMEComboBox createComboBox(Object[] values)
	{
    	return createComboBox(values, 3);
	}

    /**
	 * Initialises a <code>JComboBox</code>.
	 * 
	 * @param values The values to display.
	 * @return See above.
	 */
    public static OMEComboBox createComboBox(List values)
	{
    	if (values == null) return null;
    	Iterator i = values.iterator();
    	Object[] array = new Object[values.size()];
    	int index = 0;
    	while (i.hasNext()) {
			array[index] = i.next();
			index++;
		}
    	return createComboBox(array, 3);
	}
    
	/**
	 * Initiliases a <code>JXTaskPane</code>.
	 * 
	 * @param title The title of the component.
	 * @return See above.
	 */
	public static JXTaskPane createTaskPane(String title)
	{
		JXTaskPane taskPane = new JXTaskPane();
		taskPane.setBackground(UIUtilities.BACKGROUND_COLOR);
		taskPane.setTitle(title);
		taskPane.setCollapsed(true);
		Font font = taskPane.getFont();
		taskPane.setFont(font.deriveFont(font.getSize2D()-2));
		return taskPane;
	}
	
}

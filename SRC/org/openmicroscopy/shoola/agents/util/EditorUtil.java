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
import java.awt.Font;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
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
    public static final String 	MICRONS = "(in \u00B5m)";
    
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
    public static final String  PIN_HOLE_SIZE = "Pin hole size";
    
    /** Identifies the <code>ND filter</code> field. */
    public static final String  ND_FILTER = "ND Filter";
    
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
    
    /** Identifies the Objective's <code>Magnification</code> field. */
	public static final String	MAGNIFICATION = "Magnification";
	
	/** Identifies the Objective's <code>Lens NA</code> field. */
	public static final String	LENSNA = "Lens NA";
	
	/** Identifies the Objective's <code>Working distance</code> field. */
	public static final String	WORKING_DISTANCE = "Working Distance";
	
	/** Identifies the Objective's <code>Working distance</code> field. */
	public static final String	IMMERSION = "Immersion";
	
	/** Identifies the Objective's <code>Coating</code> field. */
	public static final String	COATING = "Coating";
	
	/** Identifies the <code>Correction Collar</code> field. */
	public static final String	CORRECTION_COLLAR = "Correction Collar";

	/** Identifies the Objective's <code>Medium</code> field. */
	public static final String	MEDIUM = "Medium";
	
	/** Identifies the Objective's <code>Refactive index</code> field. */
	public static final String	REFRACTIVE_INDEX = "Refractive index";

	/** Identifies the Environment <code>temperature</code> field. */
	public static final String	TEMPERATURE = "Temperature";
	
	/** Identifies the Environment <code>Air pressure</code> field. */
	public static final String	AIR_PRESSURE = "Air Pressure";
	
	/** Identifies the Environment <code>Humidity</code> field. */
	public static final String	HUMIDITY = "Humidy";
	
	/** Identifies the Environment <code>CO2 Percent</code> field. */
	public static final String	CO2_PERCENT = "CO2 Percent";
	
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
	
	/** Identifies the  <code>Voltage</code> field. */
	public static final String	VOLTAGE = "Voltage";
	
	/** Identifies the  <code>Gain</code> field. */
	public static final String	GAIN = "Gain";
	
	/** Identifies the  <code>Offset</code> field. */
	public static final String	OFFSET = "Offset";
	
	/** Identifies the  <code>Read out rate</code> field. */
	public static final String	READ_OUT_RATE = "Read out rate";
	
	/** Identifies the  <code>Binning</code> field. */
	public static final String	BINNING = "Binnning";
	
	/** Identifies the  <code>Aplication</code> field. */
	public static final String	APLIFICATION = "Aplification";
	
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
        Timestamp date = getCreationTime(image);
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
    public static Timestamp getCreationTime(ImageData image)
    {
    	Timestamp date = null;
        try {
        	date = image.getInserted();
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
    public static Map<String, String> transformChannelData(ChannelMetadata data)
    {
        LinkedHashMap<String, String> 
        		details = new LinkedHashMap<String, String>(10);
        if (data == null) {
        	details.put(NAME, "");
            details.put(EM_WAVE, "");
            details.put(EX_WAVE, "");
            details.put(ND_FILTER, "");
            details.put(PIN_HOLE_SIZE, "");
            details.put(FLUOR, "");
            details.put(ILLUMINATION, "");
            details.put(CONTRAST_METHOD, "");
            details.put(MODE, "");
            details.put(POCKEL_CELL_SETTINGS, "");
        } else {
        	details.put(NAME, data.getName());
            details.put(EM_WAVE, ""+data.getEmissionWavelength());
            details.put(EX_WAVE, ""+data.getEmissionWavelength());
            details.put(ND_FILTER, ""+data.getNDFilter());
            details.put(PIN_HOLE_SIZE, ""+data.getPinholeSize());
            details.put(FLUOR, data.getFluor());
            details.put(ILLUMINATION, data.getIllumination());
            details.put(CONTRAST_METHOD, data.getContrastMethod());
            details.put(MODE, data.getMode());
            details.put(POCKEL_CELL_SETTINGS, data.getPockelCell());
        }
        return details;
    }
    
    /**
     * Transforms the passed objective.
     * 
     * @param objective The value to convert.
     * @return See above.
     */
    public static Map<String, String> transformObjective(Object objective)
    {
    	LinkedHashMap<String, String> 
    			details = new LinkedHashMap<String, String>(8);
    	details.put(MODEL, "");
    	details.put(MANUFACTURER, "");
    	details.put(SERIAL_NUMBER, "");
    	details.put(MAGNIFICATION, "");
        details.put(LENSNA, "");
        details.put(IMMERSION, "");
        details.put(COATING, "");
        details.put(WORKING_DISTANCE, "");
        return details;
    }
    
    /**
     * Transforms the passed objective.
     * 
     * @param settings The value to convert.
     * @return See above.
     */
    public static Map<String, String> transformObjectiveSettings(
    								Object settings)
    {
    	LinkedHashMap<String, String> 
    			details = new LinkedHashMap<String, String>(3);
    	details.put(CORRECTION_COLLAR, "");
    	details.put(MEDIUM, "");
    	details.put(REFRACTIVE_INDEX, "");
        return details;
    }
    
    /**
     * Transforms the passed imaging environment.
     * 
     * @param env The value to convert.
     * @return See above.
     */
    public static Map<String, String> transformImageEnvironment(Object env)
    {
    	LinkedHashMap<String, String> 
			details = new LinkedHashMap<String, String>(4);
    	details.put(TEMPERATURE, "");
    	details.put(AIR_PRESSURE, "");
    	details.put(HUMIDITY, "");
    	details.put(CO2_PERCENT, "");
    	return details;
    }
    
    /**
     * Transforms the passed stage label.
     * 
     * @param label The value to convert.
     * @return See above.
     */
    public static Map<String, String> transformStageLabel(Object label)
    {
    	LinkedHashMap<String, String> 
			details = new LinkedHashMap<String, String>(4);
    	details.put(NAME, "");
    	details.put(POSITION_X, "");
    	details.put(POSITION_Y, "");
    	details.put(POSITION_Z, "");
    	return details;
    }
    
    /**
     * Transforms the detector and its settings.
     * 
     * @param detector 			The value to convert.
     * @param detectorSettings	The value to convert.
     * @return See above.
     */
    public static Map<String, String> transformDectector(Object detector, 
    					Object detectorSettings)
    {
    	LinkedHashMap<String, String> 
			details = new LinkedHashMap<String, String>(7);
    	details.put(TYPE, "");
    	details.put(VOLTAGE, "");
    	details.put(GAIN, "");
    	details.put(OFFSET, "");
    	details.put(READ_OUT_RATE, "");
    	details.put(BINNING, "");
    	details.put(APLIFICATION, "");
    	return details;
    }
    
    /**
     * Transforms the detector.
     * 
     * @param detector The value to convert.
     * @return See above.
     */
    public static Map<String, String> transformDectector(Object detector)
    {
    	LinkedHashMap<String, String> 
			details = new LinkedHashMap<String, String>(4);
    	details.put(TYPE, "");
    	details.put(VOLTAGE, "");
    	details.put(GAIN, "");
    	details.put(OFFSET, "");
    	return details;
    }
    
    /**
	 * Initialises a <code>JComboBox</code>.
	 * 
	 * @param values The values to display.
	 * @return See above.
	 */
    public static JComboBox createComboBox(String[] values)
	{
		JComboBox box = new JComboBox(values);
		box.setBackground(UIUtilities.BACKGROUND_COLOR);
		box.setUI(new BasicComboBoxUI() {
			 
			
	        /**
	         * Hides the arrow Button.
	         * 
	         */
	         protected JButton createArrowButton() {
	        	
	        	JButton b = super.createArrowButton();
	          	b.setVisible(false);
	            return b;
	         }
	         
	      });

		Font f = box.getFont();
		int size = f.getSize()-3;
		box.setBorder(null);
		box.setFont(f.deriveFont(Font.ITALIC, size));
		return box;
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

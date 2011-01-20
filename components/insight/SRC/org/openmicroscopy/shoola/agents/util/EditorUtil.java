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
import java.awt.Container;
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
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.JComponent;

//Third-party libraries
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import omero.RDouble;
import omero.model.PlaneInfo;

import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMEComboBoxUI;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.DetectorData;
import pojos.DichroicData;
import pojos.ExperimenterData;
import pojos.FilterData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.InstrumentData;
import pojos.LightSourceData;
import pojos.ObjectiveData;
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
	
	 /** Identifies the <code>Middle name</code> field. */
	public static final String	MIDDLE_NAME = "Middle Name";

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
    
    /** Text describing the <code>Group</code> permission. */
    public static final String	GROUP_VISIBLE = "group visible";
    
    /** Text describing the <code>Group</code> permission. */
    public static final String	GROUP_DESCRIPTION = 
    								"Visible to Group members only.";
    
    /** Text describing the <code>Private</code> permission. */
    public static final String	PRIVATE = "private";

	/** Text displayed before the list of existing groups. */
	public static final String	GROUPS = "Belongs to the following groups: ";
    
	 /** Identifies the <code>Email</code> field. */
    public static final String	EMAIL = "E-mail";
    
    /** String to represent the micron symbol. */
    public static final String 	MICRONS_NO_BRACKET = "\u00B5m";
    
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
    
    /** Identifies the <code>Emission</code> field. */
    public static final String  EMISSION = "Emission";
    
    /** Identifies the <code>Excitation</code> field. */
    public static final String  EXCITATION = "Excitation";
    
    /** Identifies the <code>Pin hole size</code> field. */
    public static final String  PIN_HOLE_SIZE = "Pinhole size "+MICRONS;
    
    /** Identifies the <code>ND filter</code> field. */
    public static final String  ND_FILTER = "NDFilter "+PERCENT;
    
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
	
	/** Identifies the <code>Lot number</code> field. */
	public static final String	LOT_NUMBER = "Lot Number";
	
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
	
	/** Identifies a filter. */
	public static final String	FILTER = "Filter";
	
	/** Identifies a filter wheel. */
	public static final String	FILTER_WHEEL = "FilterWheel";
	
	/** Identifies a transmittance. */
	public static final String	TRANSMITTANCE = "Transmittance";
	
	/** Identifies a cut in. */
	public static final String	CUT_IN = "Cut In";
	
	/** Identifies a cut in tolerance. */
	public static final String	CUT_IN_TOLERANCE = "Cut In Tolerance";
	
	/** Identifies a cut out. */
	public static final String	CUT_OUT = "Cut Out";
	
	/** Identifies a cut out tolerance. */
	public static final String	CUT_OUT_TOLERANCE = "Cut Out Tolerance";
	
	/** Identifies a light source settings attenuation. */
	public static final String	ATTENUATION = "Attenuation "+PERCENT;
	
	/** The maximum number of field for a detector and its settings. */
	public static final int		MAX_FIELDS_DETECTOR_AND_SETTINGS = 12;
	
	/** The maximum number of field for a detector. */
	public static final int		MAX_FIELDS_DETECTOR = 10;
	
	/** The maximum number of field for a filter. */
	public static final int		MAX_FIELDS_FILTER = 11;
	
	/** The maximum number of field for an objective and its settings. */
	public static final int		MAX_FIELDS_OBJECTIVE_AND_SETTINGS = 14;
	
	/** The maximum number of field for an objective. */
	public static final int		MAX_FIELDS_OBJECTIVE = 11;
	
	/** The maximum number of field for a laser. */
	public static final int		MAX_FIELDS_LASER = 14;
	
	/** The maximum number of field for a filament and arc. */
	public static final int		MAX_FIELDS_LIGHT = 7;
	
	/** The maximum number of field for a filament and arc. */
	public static final int		MAX_FIELDS_LIGHT_AND_SETTINGS = 9;
	
	/** The maximum number of field for a laser. */
	public static final int		MAX_FIELDS_LASER_AND_SETTINGS = 15;
	
	/** The maximum number of field for a dichroic. */
	public static final int		MAX_FIELDS_DICHROIC = 4;
	
	/** The maximum number of field for a channel. */
	public static final int		MAX_FIELDS_CHANNEL = 10;
	
	/** The maximum number of field for a Stage Label. */
	public static final int		MAX_FIELDS_STAGE_LABEL = 4;
	
	/** The maximum number of field for an environment. */
	public static final int		MAX_FIELDS_ENVIRONMENT = 4;
	
	/** The maximum number of field for a microscope. */
	public static final int		MAX_FIELDS_MICROSCOPE = 5;
	
	/** Identifies the <code>Indigo</code> color. */
	private static final Color  INDIGO = new Color(75, 0, 130);

	/** Identifies the <code>Violet</code> color. */
	private static final Color  VIOLET = new Color(238, 130, 238);
	
	/** 
	 * The value to multiply the server value by when it is a percent fraction.
	 */
	private static final int	PERCENT_FRACTION = 100;
	
	/** Colors available for the color bar. */
	public static final Map<Color, String>	COLORS_BAR;
	
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
		Entry entry;
		Iterator i = PIXELS_TYPE_DESCRIPTION.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			PIXELS_TYPE.put((String) entry.getValue(), (String) entry.getKey());
		}
		
		COLORS_BAR = new LinkedHashMap<Color, String>();
		COLORS_BAR.put(ImagePaintingFactory.UNIT_BAR_COLOR, 
				ImagePaintingFactory.UNIT_BAR_COLOR_NAME);
		COLORS_BAR.put(Color.ORANGE, "Orange");
		COLORS_BAR.put(Color.YELLOW, "Yellow");
		COLORS_BAR.put(Color.BLACK, "Black");
		COLORS_BAR.put(INDIGO, "Indigo");
		COLORS_BAR.put(VIOLET, "Violet");
		COLORS_BAR.put(Color.RED, "Red");
		COLORS_BAR.put(Color.GREEN, "Green");
		COLORS_BAR.put(Color.BLUE, "Blue");
		COLORS_BAR.put(Color.WHITE, "White");
	}

	/** The filter to determine if a file is an editor file or not. */
	private static final EditorFileFilter editorFilter = new EditorFileFilter();
	
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
        details.put(EMISSION+" "+WAVELENGTH+"s", "");  
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
            details.put(EMISSION+" "+WAVELENGTH+"s", "");
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
        details.put(EMISSION+" "+WAVELENGTH+"s", ""); 
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
    	if (originalName == null) return null;
    	String[] l = UIUtilities.splitString(originalName);
    	String extension = null;
    	if (originalName.endsWith("\\")) extension = "\\";
    	else if (originalName.endsWith("/")) extension = "/";
    	String sep = UIUtilities.getStringSeparator(originalName);
    	if (sep == null) sep = "";
    	if (l != null) {
    		int n = l.length;
    		switch (n) {
				case 0: return originalName;
				case 1: 
					if (extension != null) return l[0]+extension;
					return l[0];
				case 2: 
					if (extension != null) 
						return l[n-2]+sep+l[n-1]+extension;
					return l[n-2]+sep+l[n-1];
				default: 
					if (extension != null) 
						return UIUtilities.DOTS+l[n-2]+sep+l[n-1]+extension;
					return UIUtilities.DOTS+l[n-2]+sep+l[n-1]; 
			}
    	}
        return originalName;
    }

    /**
     * Returns the last portion of the file name.
     * 
     * @param originalName The original name.
     * @return See above.
     */
    public static final String getObjectName(String originalName)
    {
    	if (originalName == null) return null;
    	String[] l = UIUtilities.splitString(originalName);
    	if (l != null) {
    		int n = l.length;
    		if (n > 0) return l[n-1];
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
     * Returns <code>true</code> if the specified data object is writable by 
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
        details.put(EXCITATION, Integer.valueOf(0));
        details.put(EMISSION, Integer.valueOf(0)); 
        details.put(ND_FILTER, Float.valueOf(0));
        details.put(PIN_HOLE_SIZE, Float.valueOf(0));
        details.put(FLUOR, "");
        details.put(ILLUMINATION, "");
        details.put(CONTRAST_METHOD, "");
        details.put(MODE, "");
        details.put(POCKEL_CELL_SETTINGS, Integer.valueOf(0));
        if (data == null) {
        	notSet.add(NAME);
        	notSet.add(EMISSION);
        	notSet.add(EXCITATION);
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
        if (i <= 100) {
        	i = 0;
        	notSet.add(EMISSION);
        } 
        details.put(EMISSION, i);
    	i = data.getExcitationWavelength();
        if (i <= 100) {
        	i = 0;
        	notSet.add(EXCITATION);
        }
    	details.put(EXCITATION, i);
    	double f = data.getNDFilter();
    	if (f < 0) {
    		f = 0;
        	notSet.add(ND_FILTER);
    	}
    	details.put(ND_FILTER, f*100);
    	f = data.getPinholeSize();
    	if (f < 0) {
    		f = 0;
        	notSet.add(PIN_HOLE_SIZE);
    	} else f = UIUtilities.roundTwoDecimals(f);
        details.put(PIN_HOLE_SIZE, f);
        s = data.getFluor();
		if (s == null || s.trim().length() == 0) 
			notSet.add(FLUOR);
        details.put(FLUOR, s);
        try {
        	s = data.getIllumination(); //Check how this can be null.
		} catch (Exception e) {
			s = null;
		}
		if (s == null || s.trim().length() == 0) 
			notSet.add(ILLUMINATION);
        details.put(ILLUMINATION, s);
        s = data.getContrastMethod();
		if (s == null || s.trim().length() == 0) 
			notSet.add(CONTRAST_METHOD);
        details.put(CONTRAST_METHOD, s);
        try {
        	s = data.getMode();
		} catch (Exception e) {
			s = null;
		}
        
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
     * Transforms the passed dichroic.
     * 
     * @param data The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformDichroic(DichroicData data)
    {
    	LinkedHashMap<String, Object> 
		details = new LinkedHashMap<String, Object>();
    	List<String> notSet = new ArrayList<String>();
    	details.put(MODEL, "");
    	details.put(MANUFACTURER, "");
    	details.put(SERIAL_NUMBER, "");
    	details.put(LOT_NUMBER, "");

    	if (data == null) {
    		notSet.add(MODEL);
    		notSet.add(MANUFACTURER);
    		notSet.add(SERIAL_NUMBER);
    		notSet.add(LOT_NUMBER);
    		details.put(NOT_SET, notSet);
        	return details;
    	}
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
		s = data.getLotNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(LOT_NUMBER);
		details.put(LOT_NUMBER, s);
        details.put(NOT_SET, notSet);
    	return details;
    }
    
    /**
     * Transforms the passed microscope.
     * 
     * @param data The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformMicroscope(InstrumentData data)
    {
    	LinkedHashMap<String, Object> 
		details = new LinkedHashMap<String, Object>();
    	List<String> notSet = new ArrayList<String>();
    	details.put(MODEL, "");
    	details.put(MANUFACTURER, "");
    	details.put(SERIAL_NUMBER, "");
    	details.put(LOT_NUMBER, "");
    	details.put(TYPE, "");

    	if (data == null) {
    		notSet.add(MODEL);
    		notSet.add(MANUFACTURER);
    		notSet.add(SERIAL_NUMBER);
    		notSet.add(LOT_NUMBER);
    		notSet.add(TYPE);
    		details.put(NOT_SET, notSet);
        	return details;
    	}
    	String s = data.getMicroscopeModel();
		if (s == null || s.trim().length() == 0) 
			notSet.add(MODEL);
		details.put(MODEL, s);
		s = data.getMicroscopeManufacturer();
		if (s == null || s.trim().length() == 0) 
			notSet.add(MANUFACTURER);
		details.put(MANUFACTURER, s);
		s = data.getMicroscopeSerialNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(SERIAL_NUMBER);
		details.put(SERIAL_NUMBER, s);
		s = data.getMicroscopeLotNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(LOT_NUMBER);
		details.put(LOT_NUMBER, s);
		s = data.getMicroscopeType();
        if (s == null || s.trim().length() == 0) 
			notSet.add(TYPE);
        details.put(TYPE, s); 
        details.put(NOT_SET, notSet);
    	return details;
    }
    
    /**
     * Transforms the passed objective.
     * 
     * @param data The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformObjective(ObjectiveData data)
    {
    	LinkedHashMap<String, Object> 
			details = new LinkedHashMap<String, Object>();
		List<String> notSet = new ArrayList<String>();
		details.put(MODEL, "");
		details.put(MANUFACTURER, "");
		details.put(SERIAL_NUMBER, "");
		details.put(LOT_NUMBER, "");
		details.put(NOMINAL_MAGNIFICATION, Integer.valueOf(0));
		details.put(CALIBRATED_MAGNIFICATION,Float.valueOf(0));
		details.put(LENSNA, new Float(0));
		details.put(IMMERSION, "");
		details.put(CORRECTION, "");
		details.put(WORKING_DISTANCE, Float.valueOf(0));
		details.put(IRIS, null);
		if (data == null) {
			notSet.add(MODEL);
			notSet.add(MANUFACTURER);
			notSet.add(SERIAL_NUMBER);
			notSet.add(LOT_NUMBER);
			notSet.add(NOMINAL_MAGNIFICATION);
			notSet.add(CALIBRATED_MAGNIFICATION);
			notSet.add(LENSNA);
			notSet.add(IMMERSION);
			notSet.add(CORRECTION);
			notSet.add(WORKING_DISTANCE);
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
		
		s = data.getLotNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(LOT_NUMBER);
		details.put(LOT_NUMBER, s);
		int i = data.getNominalMagnification();
		if (i < 0) {
			i = 0;
			notSet.add(NOMINAL_MAGNIFICATION);
		}
		details.put(NOMINAL_MAGNIFICATION, i);
		double f = data.getCalibratedMagnification();
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
 		details.put(NOT_SET, notSet);
		return details;
    }
    
    /**
     * Transforms the passed objective.
     * 
     * @param data The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformObjectiveAndSettings(
    		ImageAcquisitionData data)
    {
    	LinkedHashMap<String, Object> 
    			details = new LinkedHashMap<String, Object>(9);
    	Map<String, Object> m;
    	
    	if (data == null) m = transformObjective(null);
    	else  m = transformObjective(data.getObjective());
    	List<String> notSet = (List) m.get(NOT_SET);
    	m.remove(NOT_SET);
    	details.putAll(m);
        details.put(CORRECTION_COLLAR, Float.valueOf(0));
    	details.put(MEDIUM, "");
    	details.put(REFRACTIVE_INDEX, Float.valueOf(0));
    	details.put(IRIS, null);
        if (data == null) {
        	notSet.add(CORRECTION_COLLAR);
    		notSet.add(MEDIUM);
    		notSet.add(REFRACTIVE_INDEX);
        	details.put(NOT_SET, notSet);
        	return details;
        }
       
 		double f = data.getCorrectionCollar();
    	if (f < 0) {
    		f = 0;
    		notSet.add(CORRECTION_COLLAR);
    	}
    	details.put(CORRECTION_COLLAR, f);
    	String s = data.getMedium();
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
    	details.put(TEMPERATURE, new Double(0));
    	details.put(AIR_PRESSURE, new Double(0));
    	details.put(HUMIDITY, new Double(0));
    	details.put(CO2_PERCENT, new Double(0));
    	
    	if (data == null) {
    		notSet.add(TEMPERATURE);
    		notSet.add(AIR_PRESSURE);
    		notSet.add(HUMIDITY);
    		notSet.add(CO2_PERCENT);
    		details.put(NOT_SET, notSet);
    		return details;
    	}
    	Object o = data.getTemperature();
    	double f = 0;
    	if (o == null) {
    		notSet.add(TEMPERATURE);
    	} else f = (Double) o;
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
    	details.put(HUMIDITY, f*PERCENT_FRACTION);
    	f = data.getCo2Percent();
    	if (f < 0) {
    		notSet.add(CO2_PERCENT);
    		f = 0;
    	}
    	details.put(CO2_PERCENT, f*PERCENT_FRACTION);
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
    	details.put(POSITION_X, new Double(0));
    	details.put(POSITION_Y, new Double(0));
    	details.put(POSITION_Z, new Double(0));
    	
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
    	double f = 0;
    	if (o == null) {
    		notSet.add(POSITION_X);
    	} else f = (Double) o;
    	details.put(POSITION_X, f);
    	f = 0;
    	if (o == null) {
    		notSet.add(POSITION_Y);
    	} else f = (Double) o;
    	details.put(POSITION_Y, f);
    	f = 0;
    	if (o == null) {
    		notSet.add(POSITION_Z);
    	} else f = (Double) o;
    	details.put(POSITION_Z, f);
    	
    	details.put(NOT_SET, notSet);
    	return details;
    }
    
    /**
     * Transforms the filter.
     * 
     * @param data	The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformFilter(FilterData data)
    {
    	LinkedHashMap<String, Object> 
			details = new LinkedHashMap<String, Object>();
	
		List<String> notSet = new ArrayList<String>();
		details.put(MODEL, "");
		details.put(MANUFACTURER, "");
		details.put(SERIAL_NUMBER, "");
		details.put(LOT_NUMBER, "");
		details.put(TYPE, "");
		details.put(FILTER_WHEEL, "");
		details.put(CUT_IN, "");
		details.put(CUT_OUT, "");
		details.put(CUT_IN_TOLERANCE, "");
		details.put(CUT_OUT_TOLERANCE, "");
		details.put(TRANSMITTANCE, "");
		if (data == null) {
    		notSet.add(MODEL);
    		notSet.add(MANUFACTURER);
    		notSet.add(SERIAL_NUMBER);
    		notSet.add(LOT_NUMBER);
    		notSet.add(TYPE);
    		notSet.add(FILTER_WHEEL);
    		notSet.add(CUT_IN);
    		notSet.add(CUT_OUT);
    		notSet.add(CUT_IN_TOLERANCE);
    		notSet.add(CUT_OUT_TOLERANCE);
    		notSet.add(TRANSMITTANCE);
    		details.put(NOT_SET, notSet);
        	return details;
    	}
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
		s = data.getLotNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(LOT_NUMBER);
		details.put(LOT_NUMBER, s);
		s = data.getType();
        if (s == null || s.trim().length() == 0) 
			notSet.add(TYPE);
        details.put(TYPE, s); 
        s = data.getFilterWheel();
        if (s == null || s.trim().length() == 0) 
			notSet.add(FILTER_WHEEL);
        details.put(FILTER_WHEEL, s);
        Integer v = data.getCutIn();
        int i = 0;
        if (v == null) notSet.add(CUT_IN);
        else i = v;
        details.put(CUT_IN, i);
        v = data.getCutOut();
        if (v == null) {
        	notSet.add(CUT_OUT);
        	i = 0;
        } else i = v;
        details.put(CUT_OUT, i);
        v = data.getCutInTolerance();
        if (v == null) {
        	i = 0;
        	notSet.add(CUT_IN_TOLERANCE);
        } else i = v;
        details.put(CUT_IN_TOLERANCE, i);
        
        v = data.getCutOutTolerance();
        if (v == null) {
        	i = 0;
        	notSet.add(CUT_OUT_TOLERANCE);
        } else i = v;
        details.put(CUT_OUT_TOLERANCE, i);
        
        Double d = data.getTransmittance();
        double dv = 0;
        if (d == null) {
        	notSet.add(TRANSMITTANCE);
        } else dv = d;
        	
        details.put(TRANSMITTANCE, dv);
        details.put(NOT_SET, notSet);
		return details;
    }
    
    /**
     * Transforms the light and its settings.
     * 
     * @param data The data to transform.
     * @return See above.
     */
    public static Map<String, Object> transformLightSourceAndSetting(
    		ChannelAcquisitionData data)
    {
    	LinkedHashMap<String, Object> 
			details = new LinkedHashMap<String, Object>();
		Map<String, Object> m;
		
		if (data == null) m = transformLightSource(null);
		else  m = transformLightSource(data.getLightSource());
		List<String> notSet = (List) m.get(NOT_SET);
		m.remove(NOT_SET);
		details.putAll(m);
		details.put(ATTENUATION, new Double(0));
		if (data == null) {
			details.put(WAVELENGTH, new Integer(0));
			notSet.add(ATTENUATION);
        	notSet.add(WAVELENGTH);
        	details.put(NOT_SET, notSet);
        	return details;
		}
		Double f = data.getLigthSettingsAttenuation();
		double v = 0;
		if (f == null) notSet.add(ATTENUATION);
		else v = f;
		details.put(ATTENUATION, v*PERCENT_FRACTION);
		Integer i = data.getLigthSettingsWavelength();
        if (details.containsKey(WAVELENGTH)) {
        	
        	if (i != null) { //override the value.
        		details.put(WAVELENGTH, i);
        	}
        } else {
        	int vi = 0;
			if (i == null) notSet.add(WAVELENGTH);
			else vi = i;
			details.put(WAVELENGTH, vi);
        }
        details.put(NOT_SET, notSet);
    	return details;
    }
    
    /**
     * Transforms the passed source of light.
     * 
     * @param data	The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformLightSource(LightSourceData data)
    {
    	LinkedHashMap<String, Object> 
			details = new LinkedHashMap<String, Object>();

    	List<String> notSet = new ArrayList<String>();
    	details.put(MODEL, "");
    	details.put(MANUFACTURER, "");
    	details.put(SERIAL_NUMBER, "");
    	details.put(LOT_NUMBER, "");
    	details.put(LIGHT_TYPE, "");
    	details.put(POWER, new Double(0));
    	details.put(TYPE, "");

    	if (data == null) {
    		notSet.add(MODEL);
    		notSet.add(MANUFACTURER);
    		notSet.add(SERIAL_NUMBER);
    		notSet.add(LOT_NUMBER);
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
		s = data.getManufacturer();
		if (s == null || s.trim().length() == 0) 
			notSet.add(MANUFACTURER);
		details.put(MANUFACTURER, s);
		s = data.getSerialNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(SERIAL_NUMBER);
		details.put(SERIAL_NUMBER, s);
		s = data.getLotNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(LOT_NUMBER);
		details.put(LOT_NUMBER, s);
		
    	s = data.getKind();
    	details.put(LIGHT_TYPE, s);
    	double f = data.getPower();
    	if (f < 0) {
    		notSet.add(POWER);
    		f = 0;
    	}
    	details.put(POWER, f);
    	s = data.getType();
    	if (s == null || s.trim().length() == 0) 
			notSet.add(TYPE);
        details.put(TYPE, s); 
        s = data.getKind();
        if (LightSourceData.LASER.equals(s)) {
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
        }

		details.put(NOT_SET, notSet);
    	return details;
    }
    
    /**
     * Transforms the detector.
     * 
     * @param data The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformDetector(DetectorData data)
    {

    	LinkedHashMap<String, Object> 
			details = new LinkedHashMap<String, Object>();
    	details.put(MODEL, "");
    	details.put(MANUFACTURER, "");
    	details.put(SERIAL_NUMBER, "");
    	details.put(LOT_NUMBER, "");
        details.put(TYPE, ""); 
    	details.put(GAIN, new Double(0));
    	details.put(VOLTAGE, new Double(0));
        details.put(OFFSET, new Double(0));
        details.put(ZOOM, new Double(0));
        details.put(AMPLIFICATION, "");
        List<String> notSet = new ArrayList<String>();
        if (data == null) {
        	notSet.add(MODEL);
        	notSet.add(MANUFACTURER);
        	notSet.add(SERIAL_NUMBER);
        	notSet.add(LOT_NUMBER);
        	notSet.add(GAIN);
        	notSet.add(VOLTAGE);
        	notSet.add(ZOOM);
        	notSet.add(AMPLIFICATION);
        	notSet.add(TYPE);
        	notSet.add(OFFSET);
        	details.put(NOT_SET, notSet);
        	return details;
        }
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
		s = data.getLotNumber();
		if (s == null || s.trim().length() == 0) 
			notSet.add(LOT_NUMBER);
		details.put(LOT_NUMBER, s);
		
		Double f = data.getGain();
		double v = 0;
		if (f == null) notSet.add(GAIN);
		else v = f;
		details.put(GAIN, v);
    	f = data.getVoltage();
    	if (f == null) {
    		v = 0;
    		notSet.add(VOLTAGE);
    	} else v = f;
		details.put(VOLTAGE, v);
		f = data.getOffset();
		if (f == null) {
			v = 0;
			notSet.add(OFFSET);
		} else v = f;
		details.put(OFFSET, v);
        f = data.getZoom();
        if (f == null) {
        	v = 0;
        	notSet.add(ZOOM);
        } else v = f;
        details.put(ZOOM, v);
        f = data.getAmplificationGain();
        if (f == null) {
        	v = 0;
        	notSet.add(AMPLIFICATION);
        } else v = f;
        details.put(AMPLIFICATION, v);
        s = data.getType();
        if (s == null || s.trim().length() == 0) 
			notSet.add(TYPE);
        details.put(TYPE, s); 
    	details.put(NOT_SET, notSet);
    	return details;
    }
    
    /**
     * Transforms the detector and the detector settings.
     * 
     * @param data  The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformDetectorAndSettings(
    		ChannelAcquisitionData data)
    {
    	LinkedHashMap<String, Object> 
			details = new LinkedHashMap<String, Object>(11);
    	Map<String, Object> m;
    	
    	if (data == null) m = transformDetector(null);
    	else  m = transformDetector(data.getDetector());
    	List<String> notSet = (List) m.get(NOT_SET);
    	m.remove(NOT_SET);
    	details.putAll(m);
        details.put(READ_OUT_RATE, new Double(0));
        details.put(BINNING, "");
        if (data == null) {
        	notSet.add(READ_OUT_RATE);
        	notSet.add(BINNING);
        	details.put(NOT_SET, notSet);
        	return details;
        }

        Double f = data.getDetectorSettingsGain();
        
    	if (f != null)  {
    		details.put(GAIN, f);
    		notSet.remove(GAIN);
    	}
    	
    	f = data.getDetectorSettingsVoltage();
    	if (f != null) {
    		notSet.remove(VOLTAGE);
    		details.put(VOLTAGE, f);
    	}

    	f = data.getDetectorSettingsOffset();
    	if (f != null) {
    		notSet.remove(OFFSET);
    		details.put(OFFSET, f);
    	}
    	
    	f = data.getDetectorSettingsReadOutRate();
    	double v = 0;
    	if (f == null) {
			v = 0;
			notSet.add(READ_OUT_RATE);
		} else v = f;
        details.put(READ_OUT_RATE, v);
        String s = data.getDetectorSettingsBinning();
        if (s == null || s.trim().length() == 0) 
			notSet.add(BINNING);
        details.put(BINNING, s);
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
    	details.put(DELTA_T, new Double(0));
		details.put(EXPOSURE_TIME, new Double(0));
		details.put(POSITION_X, new Double(0));
		details.put(POSITION_Y, new Double(0));
		details.put(POSITION_Z, new Double(0));
    	if (plane != null) {
    		RDouble o = plane.getDeltaT();
    		if (o != null) 
    			details.put(DELTA_T, 
    					UIUtilities.roundTwoDecimals(o.getValue()));	
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
	 * Initializes a <code>JComboBox</code>.
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
    	box.setOpaque(true);
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
	 * Initializes a <code>JComboBox</code>.
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
	 * Initializes a <code>JComboBox</code>.
	 * 
	 * @param values The values to display.
	 * @return See above.
	 */
    public static OMEComboBox createComboBox(Object[] values)
	{
    	return createComboBox(values, 3);
	}

    /**
	 * Initializes a <code>JComboBox</code>.
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
	 * Initializes a <code>JXTaskPane</code>.
	 * 
	 * @param title The title of the component.
	 * @return See above.
	 */
	public static JXTaskPane createTaskPane(String title)
	{
		return UIUtilities.createTaskPane(title, UIUtilities.BACKGROUND_COLOR);
	}

	/**
	 * Returns <code>true</code> if the passed name is the name of an
	 * editor file, <code>false</code> otherwise.
	 * 
	 * @param fileName The name of the file.
	 * @return See above.
	 */
	public static boolean isEditorFile(String fileName)
	{
		return editorFilter.accept(fileName);
	}

}

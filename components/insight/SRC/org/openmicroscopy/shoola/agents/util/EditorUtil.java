/*
 * org.openmicroscopy.shoola.agents.util.EditorUtil
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.Color;
import java.awt.Font;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.jdesktop.swingx.JXTaskPane;

import ome.formats.model.UnitsFactory;
import ome.model.units.BigResult;
import ome.units.UNITS;
import omero.model.ElectricPotential;
import omero.model.Frequency;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.PlaneInfo;
import omero.model.Power;
import omero.model.Pressure;
import omero.model.Temperature;
import omero.model.enums.UnitsLength;

import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.util.filter.file.CppFilter;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.JavaFilter;
import org.openmicroscopy.shoola.util.filter.file.MatlabFilter;
import org.openmicroscopy.shoola.util.filter.file.PythonFilter;
import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMEComboBoxUI;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import com.google.common.math.DoubleMath;

import pojos.AnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.DetectorData;
import pojos.DichroicData;
import pojos.ExperimenterData;
import pojos.FilterData;
import pojos.FilterSetData;
import pojos.GroupData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.InstrumentData;
import pojos.LightSourceData;
import pojos.ObjectiveData;
import pojos.PixelsData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;

/**
 * Collection of helper methods to format data objects.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class EditorUtil
{

    /** Default number formatter instance */
    public static final NumberFormat NF = new DecimalFormat("0.##");
    
    /** The maximum number of characters.*/
    public static final int MAX_CHAR = 256;

    /** The number of characters.*/
    public static final int LENGHT_CHAR = 50;

    /** Identifies the Laser type. */
    public static final String LASER_TYPE = "Laser";

    /** Identifies the Arc type. */
    public static final String ARC_TYPE = "Arc";

    /** Identifies the Filament type. */
    public static final String FILAMENT_TYPE = "Filament";

    /** Identifies the Emitting Diode type. */
    public static final String EMITTING_DIODE_TYPE = "Light Emitting Diode";

    /** The default value for the scale bar. */
    public static final int DEFAULT_SCALE = 5;

    /** Default text displayed in the acquisition date is not available. */
    public static final String DATE_NOT_AVAILABLE = "Not available";

    /** Identifies the <code>Default group</code>. */
    public static final String DEFAULT_GROUP = "Default Group";

    /** Symbols indicating the mandatory values. */
    public static final String MANDATORY_SYMBOL = " *";

    /** Description of the mandatory symbol. */
    public static final String MANDATORY_DESCRIPTION = "* indicates the " +
            "required fields.";


    /** Identifies the <code>Group owner</code> field. */
    public static final String GROUP_OWNER = "Group's owner";

    /** Identifies the <code>Administrator</code> field. */
    public static final String ADMINISTRATOR = "Administrator";

    /** Identifies the <code>Active</code> field. */
    public static final String ACTIVE = "Active";

    /** Identifies the <code>Display name</code> field. */
    public static final String DISPLAY_NAME = "Login Name";

    /** Identifies the <code>Last name</code> field. */
    public static final String LAST_NAME = "Last Name";

    /** Identifies the <code>First name</code> field. */
    public static final String FIRST_NAME = "First Name";

    /** Identifies the <code>Middle name</code> field. */
    public static final String MIDDLE_NAME = "Middle Name";

    /** Identifies the <code>Last name</code> field. */
    public static final String	INSTITUTION = "Institution";

    /** Text displaying before the owner's permissions. */
    public static final String OWNER = "Owner: ";

    /** Text displaying before the group's permissions. */
    public static final String GROUP = "Group: ";

    /** Text displaying before the world's permissions. */
    public static final String WORLD = "Others: ";

    /** Text describing the <code>Read</code> permission. */
    public static final String READ = "Read";

    /** Text describing the <code>Write</code> permission. */
    public static final String WRITE = "Write";

    /** Text describing the <code>Public</code> permission. */
    public static final String PUBLIC = "Public";

    /** Text describing the <code>Group</code> permission. */
    public static final String GROUP_VISIBLE = "Collaborative";

    /** Text describing the <code>Group</code> permission. */
    public static final String	 ROUP_DESCRIPTION =
            "Visible to members of the Group only.";

    /** Text describing the <code>Private</code> permission. */
    public static final String PRIVATE = "Private";

    /** Text displayed before the list of existing groups. */
    public static final String GROUPS = "Belongs to the following groups: ";

    /** Identifies the <code>Email</code> field. */
    public static final String EMAIL = "E-mail";

    /** String to represent the micron symbol. */
    public static final String MICRONS_NO_BRACKET = LengthI.lookupSymbol(UnitsLength.MICROMETER);

    /** String to represent the micron symbol. */
    public static final String MICRONS = "("+MICRONS_NO_BRACKET+")";

    /** String to represent the celsius symbol. */
    public static final String CELSIUS = "(℃)";

    /** String to represent the percent symbol. */
    public static final String PERCENT = "(%)";

    /** String to represent the millibars symbol. */
    public static final String MILLIBARS = "(mb)";

    /** Identifies the <code>SizeX</code> field. */
    public static final String SIZE_X = "Size X";

    /** Identifies the <code>SizeY</code> field. */
    public static final String SIZE_Y = "Size Y";

    /** Identifies the <code>PixelSizeX</code> field. */
    public static final String PIXEL_SIZE_X = "Pixel size X ";

    /** Identifies the <code>PixelSizeY</code> field. */
    public static final String PIXEL_SIZE_Y = "Pixel size Y ";

    /** Identifies the <code>PixelSizeZ</code> field. */
    public static final String PIXEL_SIZE_Z = "Pixel size Z ";

    /** Identifies the <code>Sections</code> field. */
    public static final String SECTIONS = "Number of sections";

    /** Identifies the <code>Timepoints</code> field. */
    public static final String TIMEPOINTS = "Number of timepoints";

    /** Identifies the <code>Timepoints</code> field. */
    public static final String CHANNELS = "Channels";

    /** Identifies the <code>PixelType</code> field. */
    public static final String PIXEL_TYPE = "Pixel Type";

    /** Identifies the <code>Name</code> field. */
    public static final String NAME = "Name";

    /** Identifies the <code>Acquisition date</code> field. */
    public static final String ACQUISITION_DATE = "Acquisition Date";

    /** Identifies the <code>Imported date</code> field. */
    public static final String IMPORTED_DATE = "Import Date";

    /** Identifies the <code>Archived</code> field. */
    public static final String ARCHIVED = "Archived";

    /** Identifies the <code>XY Dimension</code> field. */
    public static final String XY_DIMENSION = "Dimensions (XY)";

    /** Identifies the <code>Z-sections/Timepoints</code> field. */
    public static final String Z_T_FIELDS = "Z-sections/Timepoints";

    /** Identifies the <code>Lifetime</code> field. */
    public static final String SMALL_T_VARIABLE = "t";

    /** Identifies the <code>Emission</code> field. */
    public static final String EMISSION = "Emission";

    /** Identifies the <code>Excitation</code> field. */
    public static final String EXCITATION = "Excitation";

    /** Identifies the <code>Pin hole size</code> field. */
    public static final String PIN_HOLE_SIZE = "Pinhole size";

    /** Identifies the <code>ND filter</code> field. */
    public static final String ND_FILTER = "NDFilter "+PERCENT;

    /** Identifies the <code>Fluor</code> field. */
    public static final String FLUOR = "Fluor";

    /** Identifies the <code>Illumination</code> field. */
    public static final String ILLUMINATION = "Illumination";

    /** Identifies the <code>Contrast Method</code> field. */
    public static final String CONTRAST_METHOD = "Contrast Method";

    /** Identifies the <code>Mode</code> field. */
    public static final String MODE = "Mode";

    /** Identifies the <code>Pockel Cell</code> field. */
    public static final String POCKEL_CELL_SETTINGS = "Pockel Cell";

    /** Identifies the Objective's <code>Nominal Magnification</code> field. */
    public static final String NOMINAL_MAGNIFICATION = "Nominal Magnification";

    /**
     * Identifies the Objective's <code>Calibrated Magnification</code> field.
     */
    public static final String CALIBRATED_MAGNIFICATION = "Calibrated " +
            "Magnification";

    /** Identifies the Objective's <code>Lens NA</code> field. */
    public static final String LENSNA = "Lens NA";

    /** Identifies the Objective's <code>Working distance</code> field. */
    public static final String WORKING_DISTANCE = "Working Distance";

    /** Identifies the Objective's <code>Working distance</code> field. */
    public static final String IMMERSION = "Immersion";

    /** Identifies the Objective's <code>Correction</code> field. */
    public static final String CORRECTION = "Correction";

    /** Identifies the <code>Correction Collar</code> field. */
    public static final String CORRECTION_COLLAR = "Correction Collar";

    /** Identifies the Objective's <code>Medium</code> field. */
    public static final String MEDIUM = "Medium";

    /** Identifies the Objective's <code>Refactive index</code> field. */
    public static final String REFRACTIVE_INDEX = "Refractive index";

    /** Identifies the Environment <code>temperature</code> field. */
    public static final String TEMPERATURE = "Temperature";

    /** Identifies the Environment <code>Air pressure</code> field. */
    public static final String AIR_PRESSURE = "Air Pressure";

    /** Identifies the Environment <code>Humidity</code> field. */
    public static final String HUMIDITY = "Humidity";

    /** Identifies the Environment <code>CO2 Percent</code> field. */
    public static final String CO2_PERCENT = "CO2 Percent";

    /** Identifies the <code>Model</code> field. */
    public static final String MODEL = "Model";

    /** Identifies the <code>Manufacturer</code> field. */
    public static final String MANUFACTURER = "Manufacturer";

    /** Identifies the <code>Serial number</code> field. */
    public static final String SERIAL_NUMBER = "Serial Number";

    /** Identifies the <code>Lot number</code> field. */
    public static final String LOT_NUMBER = "Lot Number";

    /** Identifies the Stage label <code>Position X</code> field. */
    public static final String POSITION_X = "Position X";

    /** Identifies the Stage label <code>Position Y</code> field. */
    public static final String POSITION_Y = "Position Y";

    /** Identifies the Stage label <code>Position Z</code> field. */
    public static final String POSITION_Z = "Position Z";

    /** Identifies the <code>Type</code> field. */
    public static final String TYPE = "Type";

    /** Identifies the <code>Voltage</code> field. */
    public static final String VOLTAGE = "Voltage";

    /** Identifies the <code>Gain</code> field. */
    public static final String GAIN = "Gain";

    /** Identifies the <code>Offset</code> field. */
    public static final String OFFSET = "Offset";

    /** Identifies the <code>Read out rate</code> field. */
    public static final String READ_OUT_RATE = "Read out rate";

    /** Identifies the <code>Binning</code> field. */
    public static final String	BINNING = "Binning";

    /** Identifies the <code>Amplification</code> field. */
    public static final String AMPLIFICATION = "Amplification Gain";

    /** Identifies the <code>Zoom</code> field. */
    public static final String ZOOM = "Zoom";

    /** Identifies the <code>Exposure</code> field. */
    public static final String EXPOSURE_TIME = "Exposure Time";

    /** Identifies the <code>Delta</code> field. */
    public static final String DELTA_T = "DeltaT";

    /** Identifies the <code>Power</code> field of light source. */
    public static final String POWER = "Power";

    /** Identifies the <code>type</code> field of the light. */
    public static final String LIGHT_TYPE = "Light";

    /** Identifies the <code>type</code> field of the light. */
    public static final String TUNEABLE = "Tuneable";

    /** Identifies the <code>type</code> field of the light. */
    public static final String PULSE = "Pulse";

    /** Identifies the <code>type</code> field of the light. */
    public static final String POCKEL_CELL = "PockelCell";

    /** Identifies the <code>Repetition rate</code> of the laser. */
    public static final String REPETITION_RATE = "Repetition Rate";

    /** Identifies the <code>Repetition rate</code> of the laser. */
    public static final String PUMP = "Pump";

    /** Identifies the <code>Wavelength</code> of the laser. */
    public static final String WAVELENGTH = "Wavelength";

    /** Identifies the <code>Frequency Multiplication</code> of the laser. */
    public static final String FREQUENCY_MULTIPLICATION =
            "Frequency Multiplication";

    /** Identifies the <code>Iris</code> of the objective. */
    public static final String IRIS = "Iris";

    /** Identifies the unset fields. */
    public static final String NOT_SET = "NotSet";

    /** Identifies the ROI count field. */
    public static final String ROI_COUNT = "ROI Count";

    /** The text for the external identifier. */
    public static final String EXTERNAL_IDENTIFIER = "External Identifier";

    /** The text for the external description.*/
    public static final String EXTERNAL_DESCRIPTION = "External Description";

    /** The text for the external description.*/
    public static final String STATUS = "Status";

    /** The map identifying the pixels value and its description. */
    public static final Map<String, String> PIXELS_TYPE_DESCRIPTION;

    /** The map identifying the pixels value and its description. */
    public static final Map<String, String> PIXELS_TYPE;

    /** Identifies a filter. */
    public static final String FILTER = "Filter";

    /** Identifies a filter wheel. */
    public static final String FILTER_WHEEL = "FilterWheel";

    /** Identifies a transmittance. */
    public static final String TRANSMITTANCE = "Transmittance";

    /** Identifies a cut in. */
    public static final String CUT_IN = "Cut In";

    /** Identifies a cut in tolerance. */
    public static final String CUT_IN_TOLERANCE = "Cut In Tolerance";

    /** Identifies a cut out. */
    public static final String CUT_OUT = "Cut Out";

    /** Identifies a cut out tolerance. */
    public static final String CUT_OUT_TOLERANCE = "Cut Out Tolerance";

    /** Identifies a light source settings attenuation. */
    public static final String ATTENUATION = "Attenuation "+PERCENT;

    /** The maximum number of fields for a detector and its settings. */
    public static final int MAX_FIELDS_DETECTOR_AND_SETTINGS = 12;

    /** The maximum number of fields for a detector. */
    public static final int MAX_FIELDS_DETECTOR = 10;

    /** The maximum number of fields for a filter. */
    public static final int MAX_FIELDS_FILTER = 11;

    /** The maximum number of fields for an objective and its settings. */
    public static final int MAX_FIELDS_OBJECTIVE_AND_SETTINGS = 14;

    /** The maximum number of fields for an objective. */
    public static final int MAX_FIELDS_OBJECTIVE = 11;

    /** The maximum number of fields for a laser. */
    public static final int MAX_FIELDS_LASER = 15;

    /** The maximum number of fields for a filament and arc. */
    public static final int MAX_FIELDS_LIGHT = 7;

    /** The maximum number of fields for a filament and arc. */
    public static final int MAX_FIELDS_LIGHT_AND_SETTINGS = 9;

    /** The maximum number of fields for a laser. */
    public static final int MAX_FIELDS_LASER_AND_SETTINGS = 15;

    /** The maximum number of fields for a dichroic. */
    public static final int MAX_FIELDS_DICHROIC = 4;

    /** The maximum number of fields for a channel. */
    public static final int MAX_FIELDS_CHANNEL = 10;

    /** The maximum number of fields for a Stage Label. */
    public static final int MAX_FIELDS_STAGE_LABEL = 4;

    /** The maximum number of fields for an environment. */
    public static final int MAX_FIELDS_ENVIRONMENT = 4;

    /** The maximum number of fields for a microscope. */
    public static final int MAX_FIELDS_MICROSCOPE = 5;

    /** The maximum number of fields for an environment. */
    public static final int MAX_FIELDS_PLANE_INFO = 5;

    /** The unit used to store time in Plane info. */
    public static final String TIME_UNIT = "s";

    /** Identifies the <code>Indigo</code> color. */
    private static final Color INDIGO = new Color(75, 0, 130);

    /** Identifies the <code>Violet</code> color. */
    private static final Color VIOLET = new Color(238, 130, 238);

    /** Unicode character for a non-breaking space */
    public static final String NONBRSPACE = "\u00A0";

    /** The date format for the date pickers */
    public static final String DATE_PICKER_FORMAT = "yyyy-MM-dd";

    /**
     * The value to multiply the server value by when it is a percent fraction.
     */
    private static final int PERCENT_FRACTION = 100;

    /** Colors available for the color bar. */
    public static final Map<Color, String> COLORS_BAR;

    /** Collection of filters to select the supported type of scripts. */
    public static final List<CustomizedFileFilter> SCRIPTS_FILTERS;

    /** List of files format with companion files.*/
    public static final List<String> FORMATS_WITH_COMPANION;

    static {
        FORMATS_WITH_COMPANION = new ArrayList<String>();
        FORMATS_WITH_COMPANION.add("deltavision");

        SCRIPTS_FILTERS = new ArrayList<CustomizedFileFilter>();
        SCRIPTS_FILTERS.add(new CppFilter());
        SCRIPTS_FILTERS.add(new JavaFilter());
        SCRIPTS_FILTERS.add(new MatlabFilter());
        SCRIPTS_FILTERS.add(new PythonFilter());

        PIXELS_TYPE_DESCRIPTION = new LinkedHashMap<String, String>();
        PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.INT_8,
                "Signed 8-bit (1 byte)");
        PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.UINT_8,
                "Unsigned 8-bit (1 byte)");
        PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.INT_16,
                "Signed 16-bit (2 byte)");
        PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.UINT_16,
                "Unsigned 16-bit (2 byte)");
        PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.INT_32,
                "Signed 32-bit (4 byte)");
        PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.UINT_32,
                "Unsigned 32-bit (4 byte)");
        PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.FLOAT,
                "Float");
        PIXELS_TYPE_DESCRIPTION.put(OmeroImageService.DOUBLE,
                "Double");
        PIXELS_TYPE = new LinkedHashMap<String, String>();
        Entry<String, String> entry;
        Iterator<Entry<String, String>>
        i = PIXELS_TYPE_DESCRIPTION.entrySet().iterator();
        while (i.hasNext()) {
            entry = i.next();
            PIXELS_TYPE.put(entry.getValue(), entry.getKey());
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

    /**
     * Returns the pixels size as a string.
     *
     * @param details The map to convert.
     * @return See above.
     */
    private static String formatPixelsSize(Map details)
    {
        String units = null;
        Length x = (Length) details.get(PIXEL_SIZE_X);
        Length y = (Length) details.get(PIXEL_SIZE_Y);
        Length z = (Length) details.get(PIXEL_SIZE_Z);
        Double dx = null, dy = null, dz = null;
        NumberFormat nf = NumberFormat.getInstance();
        try {
        	x = UIUtilities.transformSize(x);
            dx = x.getValue();
            units = ((LengthI)x).getSymbol();
        } catch (Exception e) {
        }
        try {
        	y = UIUtilities.transformSize(y);
            dy = y.getValue();
            if (units == null) units = ((LengthI)y).getSymbol();
        } catch (Exception e) {
        }
        try {
        	z = UIUtilities.transformSize(z);
            dz = z.getValue();
            if (units == null) units = ((LengthI)z).getSymbol();
        } catch (Exception e) {
        }

        String label = "<b>Pixels Size (";
        String value = "";
        if (dx != null && dx.doubleValue() > 0) {
            value += nf.format(dx);
            label += "X";
        }
        if (dy != null && dy.doubleValue() > 0) {
            if (value.length() == 0) value += nf.format(dy);
            else value +="x"+nf.format(dy);
            label += "Y";
        }
        if (dz != null && dz.doubleValue() > 0) {
            if (value.length() == 0) value += nf.format(dz);
            else value +="x"+nf.format(dz);
            label += "Z";
        }
        label += ") ";
        if (value.length() == 0) return null;
        if (units == null) units = LengthI.lookupSymbol(UnitsLength.MICROMETER);
        return label+units+": </b>"+value;
    }

    /**
     * Rounds the value.
     *
     * @param v The value to handle.
     * @return See above.
     */
    private static double roundValue(double v)
    {
        if (v <= 0) return UIUtilities.roundTwoDecimals(v);
        int decimal = UIUtilities.findDecimal(v, 1);
        if (decimal <= 2) return UIUtilities.roundTwoDecimals(v);
        return UIUtilities.ceil(v, decimal+1);
    }
    
    /**
     * Transforms the specified {@link ExperimenterData} object into
     * a visualization form.
     *
     * @param data The {@link ExperimenterData} object to transform.
     * @return See above.
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
     * @return See above.
     */
    public static Map<String, Object> transformPixelsData(PixelsData data)
    {
    	Length nullLength = new LengthI(0, UnitsLength.PIXEL);
        LinkedHashMap<String, Object> details =
                new LinkedHashMap<String, Object>(9);
        if (data == null) {
            details.put(SIZE_X, "");
            details.put(SIZE_Y, "");
            details.put(SECTIONS, "");
            details.put(TIMEPOINTS, "");
            details.put(PIXEL_SIZE_X, nullLength);
            details.put(PIXEL_SIZE_Y, nullLength);
            details.put(PIXEL_SIZE_Z, nullLength);
            details.put(PIXEL_TYPE, "");
            details.put(CHANNELS, "");
        } else {
            details.put(SIZE_X, ""+data.getSizeX());
            details.put(SIZE_Y, ""+data.getSizeY());
            details.put(SECTIONS, ""+data.getSizeZ());
            details.put(TIMEPOINTS, ""+data.getSizeT());
            details.put(CHANNELS, ""+data.getSizeC());
            Length l = null;
            try {
                l = data.getPixelSizeX(UnitsLength.MICROMETER);
            } catch (BigResult e) {
                details.put(PIXEL_SIZE_X, e);
            }
			details.put(PIXEL_SIZE_X,  l == null ? nullLength : l);
			try {
                l = data.getPixelSizeY(UnitsLength.MICROMETER);
            } catch (BigResult e) {
                details.put(PIXEL_SIZE_Y, e);
            }
			details.put(PIXEL_SIZE_Y,  l == null ? nullLength : l);
			try {
                l = data.getPixelSizeZ(UnitsLength.MICROMETER);
            } catch (BigResult e) {
                details.put(PIXEL_SIZE_Z, e);
            }
			details.put(PIXEL_SIZE_Z,  l == null ? nullLength : l);
			details.put(PIXEL_TYPE, data.getPixelType());
        }
        details.put(EMISSION+" "+WAVELENGTH+"s", "");
        return details;
    }

    /**
     * Transforms the specified {@link ImageData} object into
     * a visualization form.
     *
     * @param image The {@link ImageData} object to transform.
     * @return See above
     */
    public static Map<String, Object> transformImageData(ImageData image)
    {
        LinkedHashMap<String, Object> details =
                new LinkedHashMap<String, Object>(10);
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
            details.put(SIZE_X, ""+data.getSizeX());
            details.put(SIZE_Y, ""+data.getSizeY());
            details.put(SECTIONS, ""+data.getSizeZ());
            details.put(TIMEPOINTS, ""+data.getSizeT());
            try {
                details.put(PIXEL_SIZE_X,
                        NF.format(data.getPixelSizeX(UnitsLength.MICROMETER)));
            } catch (BigResult e) {
                details.put(PIXEL_SIZE_X, e);
            }
            try {
                details.put(PIXEL_SIZE_Y,
                        NF.format(data.getPixelSizeY(UnitsLength.MICROMETER)));
            } catch (BigResult e) {
                details.put(PIXEL_SIZE_Y, e);
            }
            try {
                details.put(PIXEL_SIZE_Z,
                        NF.format(data.getPixelSizeZ(UnitsLength.MICROMETER)));
            } catch (BigResult e) {
                details.put(PIXEL_SIZE_Z, e);
            }
            details.put(PIXEL_TYPE,
                    PIXELS_TYPE_DESCRIPTION.get("" + data.getPixelType()));
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
        try {
            String s1 = exp.getFirstName();
            String s2 = exp.getLastName();
            if (s1.trim().length() == 0 && s2.trim().length() == 0)
                return exp.getUserName();
            if (s1.length() == 0) return s2;
            if (s2.length() == 0) return s1;
            StringBuffer buf = new StringBuffer();
            buf.append(s1);
            buf.append(" ");
            buf.append(s2);
            return buf.toString();
        } catch (Exception e) {
            //not loaded
        }
        return "";
    }

    /**
     * Formats the specified experimenter. Use the initial of the user
     * for the first name.
     *
     * @param exp The experimenter to format.
     * @param capitalize Pass <code>true</code> to capitalize the first letter
     *                   <code>false</code> otherwise.
     * @return See above.
     */
    public static String formatExperimenterInitial(ExperimenterData exp,
            boolean capitalize)
    {
        if (exp == null) return "";
        try {
            String s1 = exp.getFirstName();
            String s2 = exp.getLastName();
            if (s1.trim().length() == 0 && s2.trim().length() == 0)
                return exp.getUserName();
            if (s1.length() == 0) return s2;
            if (s2.length() == 0) return s1;
            StringBuffer buf = new StringBuffer();
            if (capitalize) buf.append(Character.toUpperCase(s1.charAt(0)));
            else buf.append(Character.toLowerCase(s1.charAt(0)));
            buf.append(". ");
            buf.append(s2);
            return buf.toString();
        } catch (Exception e) {
            //not loaded
        }
        return "";
    }

    /**
     * Transforms the specified {@link ExperimenterData} object into
     * a visualization form.
     *
     * @param data The {@link ExperimenterData} object to transform.
     * @return See above.
     */
    public static Map<String, String> convertExperimenter(ExperimenterData data)
    {
        LinkedHashMap<String, String> details =
                new LinkedHashMap<String, String>(3);
        if (data == null) {
            details.put(FIRST_NAME, "");
            details.put(MIDDLE_NAME, "");
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
                details.put(MIDDLE_NAME, data.getMiddleName());
            } catch (Exception e) {
                details.put(MIDDLE_NAME, "");
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
     * @param object The object to handle.
     * @return See above.
     */
    public static boolean isAnnotated(Object object)
    {
        return isAnnotated(object, 0);
    }

    /**
     * Returns <code>true</code> it the object has been annotated,
     * <code>false</code> otherwise.
     *
     * @param object The object to handle.
     * @return See above.
     */
    public static boolean isAnnotated(Object object, int count)
    {
        if (object == null) return false;
        Map<Long, Long> counts = null;
        if (object instanceof DatasetData)
            counts = ((DatasetData) object).getAnnotationsCounts();
        else if (object instanceof ProjectData)
            counts = ((ProjectData) object).getAnnotationsCounts();
        else if (object instanceof ImageData) {
            ImageData image = (ImageData) object;
            counts = image.getAnnotationsCounts();
            if (counts == null || counts.size() <= 0) {
                return count > 0;
            }
            int n = 1;
            try {
                String format = image.getFormat();
                if (format != null &&
                        FORMATS_WITH_COMPANION.contains(format.toLowerCase()))
                    n = 2;
            } catch (Exception e) {
            }
            Iterator<Entry<Long, Long>> i = counts.entrySet().iterator();
            long value = 0;
            Entry<Long, Long> entry;
            while (i.hasNext()) {
                entry = i.next();
                value += (Long) entry.getValue();
            }
            value += count;
            return value > n;
        } else if (object instanceof ScreenData)
            counts = ((ScreenData) object).getAnnotationsCounts();
        else if (object instanceof PlateData)
            counts = ((PlateData) object).getAnnotationsCounts();
        else if (object instanceof WellData)
            counts = ((WellData) object).getAnnotationsCounts();
        else if (object instanceof PlateAcquisitionData)
            counts = ((PlateAcquisitionData) object).getAnnotationsCounts();
        if (counts == null || counts.size() <= 0) {
            return count > 0;
        }
        return counts.size()+count > 0;
    }

    /**
     * Returns <code>true</code> it the object has been updated by the current
     * user, <code>false</code> otherwise.
     *
     * @param object The object to handle.
     * @param userID The id of the current user.
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
        else if (object instanceof PlateAcquisitionData)
            counts = ((PlateAcquisitionData) object).getAnnotationsCounts();
        if (counts == null || counts.size() == 0) return false;
        return counts.keySet().contains(userID);
    }

    /**
     * Returns <code>true</code> it the object has been updated by an
     * user other than the current user, <code>false</code> otherwise.
     *
     * @param object The object to handle.
     * @param userID The id of the current user.
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
        else if (object instanceof PlateAcquisitionData)
            counts = ((PlateAcquisitionData) object).getAnnotationsCounts();
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
        return UIUtilities.getPartialName(originalName);
    }

    /**
     * Returns the last characters of the name when the name is longer that the
     * specified value.
     *
     * @param name The name to truncate.
     * @return See above.
     */
    public static String truncate(String name)
    {
        return truncate(name, LENGHT_CHAR);
    }

    /**
     * Returns the last characters of the name when the name is longer that the
     * specified value.
     *
     * @param name The name to truncate.
     * @param maxLength The maximum length.
     * @param start Pass <code>true</code> to truncate the start of the word,
     *              <code>false</code> to truncate the end.
     * @return See above.
     */
    public static String truncate(String name, int maxLength, boolean start)
    {
        if (name == null || maxLength < 0) return "";
        int v = maxLength+UIUtilities.DOTS.length();
        int n = name.length();
        if (n > v) {
            if (start) return UIUtilities.DOTS+name.substring(n-maxLength, n);
            return name.substring(0, maxLength)+UIUtilities.DOTS;
        }
        return name;
    }

    /**
     * Returns the last characters of the name when the name is longer that the
     * specified value.
     *
     * @param name The name to truncate.
     * @param maxLength The maximum length.
     * @return See above.
     */
    public static String truncate(String name, int maxLength)
    {
        return truncate(name, maxLength, true);
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
     * @param ho The data object to check.
     * @return See above.
     */
    public static boolean isReadable(Object ho)
    {
        if (ho == null || ho instanceof ExperimenterData ||
                ho instanceof String)
            return false;
        if (!(ho instanceof DataObject)) return false;
        return true; //change in permissions.
    }

    /**
     * Returns <code>true</code> if the specified data object is writable,
     * <code>false</code> otherwise, depending on the permission.
     *
     * @param ho The data object to check.
     * @param userID The id of the current user.
     * @return See above.
     */
    public static boolean isUserOwner(Object ho, long userID)
    {
        if (ho == null || ho instanceof ExperimenterData ||
                ho instanceof String)
            return false;
        if (!(ho instanceof DataObject)) return false;
        DataObject data = (DataObject) ho;
        try {
            if (userID == data.getOwner().getId())
                return true;
        } catch (Exception e) { //owner not loaded
            return false;
        }

        return false;
    }

    /**
     * Returns <code>true</code> if the user is an owner of the passed group,
     * <code>false</code> otherwise, depending on the permission.
     *
     * @param group The group to check.
     * @param userID The id of the current user.
     * @return See above.
     */
    public static boolean isUserGroupOwner(GroupData group, long userID)
    {
        if (group == null) return false;
        Set<ExperimenterData> owners = group.getLeaders();
        if (owners == null) return false;
        Iterator<ExperimenterData> i = owners.iterator();
        ExperimenterData exp;
        while (i.hasNext()) {
            exp = i.next();
            if (exp.getId() == userID) return true;
        }
        return false;
    }

    /**
     * Transforms the specified channel information.
     *
     * @param data The object to transform.
     * @return The map whose keys are the field names, and the values
     *         the corresponding fields' values.
     */
    public static Map<String, Object> transformChannelData(ChannelData data)
    {
        LinkedHashMap<String, Object>
        details = new LinkedHashMap<String, Object>(10);
        List<String> notSet = new ArrayList<String>();
        details.put(NAME, "");
        details.put(EXCITATION, Integer.valueOf(0));
        details.put(EMISSION, Integer.valueOf(0));
        details.put(ND_FILTER, NF.format(Float.valueOf(0)));
        details.put(PIN_HOLE_SIZE, NF.format(Float.valueOf(0)));
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
        if (CommonsLangUtils.isBlank(s))
            notSet.add(NAME);
        details.put(NAME, s);

        Length wl = null;
        try {
            wl = data.getEmissionWavelength(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (wl == null) {
        	notSet.add(EMISSION);
        } else {
        	double wave = wl.getValue();
            if (wave <= 100) {
                notSet.add(EMISSION);
            } else {
                //First check if the wave is a int
                if (DoubleMath.isMathematicalInteger(wave)) {
                    details.put(EMISSION, (int)wave+NONBRSPACE+wl.getSymbol());
                } else {
                    details.put(EMISSION, NF.format(wave)+NONBRSPACE+wl.getSymbol());
                }
            }
        }

        try {
            wl = data.getExcitationWavelength(null);
        } catch (BigResult e) {
         // can't happen as null is passed to the method
        }
        if (wl == null) {
        	notSet.add(EXCITATION);
        } else {
        	double wave =  wl.getValue();
            if (wave <= 100) {
                notSet.add(EXCITATION);
            } else {
              //First check if the wave is a int
                if (DoubleMath.isMathematicalInteger(wave)) {
                    details.put(EXCITATION, ((int)wave)+ NONBRSPACE + wl.getSymbol());
                } else {
                    details.put(EXCITATION, NF.format(wave)+ NONBRSPACE +wl.getSymbol());
                }
            }
        }

        double f = data.getNDFilter();
        if (f < 0)
            notSet.add(ND_FILTER);
        else
        	details.put(ND_FILTER, NF.format(f*100));

        Length ph = null ;
        try {
            ph = data.getPinholeSize(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (ph == null) {
            notSet.add(PIN_HOLE_SIZE);
        }
        else {
        	f = ph.getValue();
        	details.put(PIN_HOLE_SIZE, NF.format(f)+NONBRSPACE+ph.getSymbol());
        }

        s = data.getFluor();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(FLUOR);
        details.put(FLUOR, s);
        s = data.getIllumination();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(ILLUMINATION);
        details.put(ILLUMINATION, s);
        s = data.getContrastMethod();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(CONTRAST_METHOD);
        details.put(CONTRAST_METHOD, s);
        s = data.getMode();

        if (CommonsLangUtils.isBlank(s))
            notSet.add(MODE);
        details.put(MODE, s);
        int i = data.getPockelCell();
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
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MODEL);
        details.put(MODEL, s);
        s = data.getManufacturer();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MANUFACTURER);
        details.put(MANUFACTURER, s);
        s = data.getSerialNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(SERIAL_NUMBER);
        details.put(SERIAL_NUMBER, s);
        s = data.getLotNumber();
        if (CommonsLangUtils.isBlank(s))
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
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MODEL);
        details.put(MODEL, s);
        s = data.getMicroscopeManufacturer();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MANUFACTURER);
        details.put(MANUFACTURER, s);
        s = data.getMicroscopeSerialNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(SERIAL_NUMBER);
        details.put(SERIAL_NUMBER, s);
        s = data.getMicroscopeLotNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(LOT_NUMBER);
        details.put(LOT_NUMBER, s);
        s = data.getMicroscopeType();
        if (CommonsLangUtils.isBlank(s))
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
        details.put(LENSNA, Float.valueOf(0));
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
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MODEL);
        details.put(MODEL, s);
        s = data.getManufacturer();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MANUFACTURER);
        details.put(MANUFACTURER, s);
        s = data.getSerialNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(SERIAL_NUMBER);
        details.put(SERIAL_NUMBER, s);

        s = data.getLotNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(LOT_NUMBER);
        details.put(LOT_NUMBER, s);
        double f = data.getNominalMagnification();
        if (f < 0) {
            f = 0;
            notSet.add(NOMINAL_MAGNIFICATION);
        }
        details.put(NOMINAL_MAGNIFICATION, NF.format(f));
        f = data.getCalibratedMagnification();
        if (f < 0) {
            f = 0;
            notSet.add(CALIBRATED_MAGNIFICATION);
        }
        details.put(CALIBRATED_MAGNIFICATION, NF.format(f));
        f = data.getLensNA();
        if (f < 0) {
            f = 0;
            notSet.add(LENSNA);
        }
        details.put(LENSNA, f);
        s = data.getImmersion();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(IMMERSION);
        details.put(IMMERSION, s);
        s = data.getCorrection();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(CORRECTION);
        details.put(CORRECTION, s);
        Length wd = null;
        try {
            wd = data.getWorkingDistance(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (wd==null) {
            notSet.add(WORKING_DISTANCE);
        }
        else {
        	f = wd.getValue();
        	details.put(WORKING_DISTANCE, NF.format(f)+NONBRSPACE+wd.getSymbol());
        }
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
        List<String> notSet = (List<String>) m.get(NOT_SET);
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
        details.put(CORRECTION_COLLAR, NF.format(f));
        String s = data.getMedium();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MEDIUM);
        details.put(MEDIUM, s);
        f = data.getRefractiveIndex();
        if (f < 0) {
            f = 0;
            notSet.add(REFRACTIVE_INDEX);
        }
        details.put(REFRACTIVE_INDEX, NF.format(f));
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
        details.put(TEMPERATURE, Double.valueOf(0));
        details.put(AIR_PRESSURE, Double.valueOf(0));
        details.put(HUMIDITY, Double.valueOf(0));
        details.put(CO2_PERCENT, Double.valueOf(0));

        if (data == null) {
            notSet.add(TEMPERATURE);
            notSet.add(AIR_PRESSURE);
            notSet.add(HUMIDITY);
            notSet.add(CO2_PERCENT);
            details.put(NOT_SET, notSet);
            return details;
        }
        Temperature t = null;
        try {
            t = data.getTemperature(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        double f = 0;
        if (t == null) {
            notSet.add(TEMPERATURE);
        } else {
        	f = t.getValue();
        	details.put(TEMPERATURE, NF.format(f)+NONBRSPACE+t.getSymbol());
        }

        Pressure p = null;
        try {
            p = data.getAirPressure(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (p == null) {
            notSet.add(AIR_PRESSURE);
        }
        else {
        	f = p.getValue();
        	details.put(AIR_PRESSURE, NF.format(f)+NONBRSPACE+p.getSymbol());
        }

        f = data.getHumidity();
        if (f < 0) {
            notSet.add(HUMIDITY);
        }
        else {
        	details.put(HUMIDITY, NF.format(f*PERCENT_FRACTION)+NONBRSPACE+"%");
        }
        f = data.getCo2Percent();
        if (f < 0) {
            notSet.add(CO2_PERCENT);
        }
        else {
        	details.put(CO2_PERCENT, NF.format(f*PERCENT_FRACTION)+NONBRSPACE+"%");
        }
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
        details.put(POSITION_X, Double.valueOf(0));
        details.put(POSITION_Y, Double.valueOf(0));
        details.put(POSITION_Z, Double.valueOf(0));

        if (data == null) {
            notSet.add(NAME);
            notSet.add(POSITION_X);
            notSet.add(POSITION_Y);
            notSet.add(POSITION_Z);
            details.put(NOT_SET, notSet);
            return details;
        }
        String s = data.getLabelName();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(NAME);
        details.put(NAME, s);
        Length p;
        double f;
        try {
            p = data.getPositionX(UnitsLength.REFERENCEFRAME);
            f = 0;
            if (p == null) {
                notSet.add(POSITION_X);
            } else
                f = p.getValue();
            details.put(POSITION_X, NF.format(f));
        } catch (BigResult e) {
            details.put(POSITION_X, e);
        }

        try {
            p = data.getPositionY(UnitsLength.REFERENCEFRAME);
            f = 0;
            if (p == null) {
                notSet.add(POSITION_Y);
            } else f = p.getValue();
            details.put(POSITION_Y, NF.format(f));
        } catch (BigResult e) {
            details.put(POSITION_Y, e);
        }
       
        try {
            p = data.getPositionZ(UnitsLength.REFERENCEFRAME);
            f = 0;
            if (p == null) {
                notSet.add(POSITION_Z);
            } else f = p.getValue();
            details.put(POSITION_Z, NF.format(f));
        } catch (BigResult e) {
            details.put(POSITION_Z, e);
        }

        details.put(NOT_SET, notSet);
        return details;
     }

    /**
     * Transforms the manufacturer information of a filter set.
     *
     * @param data The value to convert.
     * @return See above.
     */
    public static Map<String, Object> transformFilterSetManufacturer(
            FilterSetData data)
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
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MODEL);
        details.put(MODEL, s);
        s = data.getManufacturer();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MANUFACTURER);
        details.put(MANUFACTURER, s);
        s = data.getSerialNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(SERIAL_NUMBER);
        details.put(SERIAL_NUMBER, s);
        s = data.getLotNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(LOT_NUMBER);
        details.put(LOT_NUMBER, s);
        details.put(NOT_SET, notSet);
        return details;
    }

    /**
     * Transforms the filter.
     *
     * @param data The value to convert.
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
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MODEL);
        details.put(MODEL, s);
        s = data.getManufacturer();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MANUFACTURER);
        details.put(MANUFACTURER, s);
        s = data.getSerialNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(SERIAL_NUMBER);
        details.put(SERIAL_NUMBER, s);
        s = data.getLotNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(LOT_NUMBER);
        details.put(LOT_NUMBER, s);
        s = data.getType();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(TYPE);
        details.put(TYPE, s);
        s = data.getFilterWheel();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(FILTER_WHEEL);
        details.put(FILTER_WHEEL, s);
        Length wl = null;
        try {
            wl = data.getCutIn(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        int i = 0;
        if (wl == null)
        	notSet.add(CUT_IN);
        else  {
        	i = (int)wl.getValue();
        	 details.put(CUT_IN, i+NONBRSPACE+wl.getSymbol());
        }

        try {
            wl = data.getCutOut(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (wl == null) {
            notSet.add(CUT_OUT);
        }
        else {
        	i = (int)wl.getValue();
        	details.put(CUT_OUT, i+NONBRSPACE+wl.getSymbol());
        }

        try {
            wl = data.getCutInTolerance(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (wl == null) {
            notSet.add(CUT_IN_TOLERANCE);
        }
        else {
        	i = (int)wl.getValue();
        	details.put(CUT_IN_TOLERANCE, i+NONBRSPACE+wl.getSymbol());
        }

        try {
            wl = data.getCutOutTolerance(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (wl == null) {
            notSet.add(CUT_OUT_TOLERANCE);
        }
        else {
        	i = (int)wl.getValue();
        	details.put(CUT_OUT_TOLERANCE, i+NONBRSPACE+wl.getSymbol());
        }

        Double d = data.getTransmittance();
        double dv = 0;
        if (d == null) {
            notSet.add(TRANSMITTANCE);
        } else dv = d;

        details.put(TRANSMITTANCE, NF.format(dv));
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
        details.put(ATTENUATION, Double.valueOf(0));
        if (data == null) {
            details.put(WAVELENGTH, Float.valueOf(0));
            notSet.add(ATTENUATION);
            notSet.add(WAVELENGTH);
            details.put(NOT_SET, notSet);
            return details;
        }
        Double f = data.getLightSettingsAttenuation();
        double v = 0;
        if (f == null) notSet.add(ATTENUATION);
        else v = f;
        details.put(ATTENUATION, NF.format(v*PERCENT_FRACTION));

        Length wl = null;
        try {
            wl = data.getLightSettingsWavelength(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (details.containsKey(WAVELENGTH)) {
            if (wl != null) { //override the value.
                details.put(WAVELENGTH, NF.format(wl.getValue())+NONBRSPACE+wl.getSymbol());
                notSet.remove(WAVELENGTH);
            }
        } else {
            Double vi = 0.0;
            if (wl == null)
            	notSet.add(WAVELENGTH);
            else  {
            	vi = wl.getValue();
            	details.put(WAVELENGTH, NF.format(vi)+NONBRSPACE+wl.getSymbol());
            	notSet.remove(WAVELENGTH);
            }
        }
        details.put(NOT_SET, notSet);
        return details;
    }

    /**
     * Transforms the passed source of light.
     *
     * @param data The value to convert.
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
        details.put(POWER, Double.valueOf(0));
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
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MODEL);
        details.put(MODEL, s);
        s = data.getManufacturer();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MANUFACTURER);
        details.put(MANUFACTURER, s);
        s = data.getSerialNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(SERIAL_NUMBER);
        details.put(SERIAL_NUMBER, s);
        s = data.getLotNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(LOT_NUMBER);
        details.put(LOT_NUMBER, s);

        s = data.getKind();
        details.put(LIGHT_TYPE, s);
        Power p = null;
        try {
            p = data.getPower(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        double f = 0;
        if (p == null) {
            notSet.add(POWER);
        }
        else {
        	f = p.getValue();
        	details.put(POWER, NF.format(f)+NONBRSPACE+p.getSymbol());
        }
        s = data.getType();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(TYPE);
        details.put(TYPE, s);
        s = data.getKind();
        if (LightSourceData.LASER.equals(s)) {
            LightSourceData pump = data.getLaserPump();
            if (pump != null) {
                String value = getLightSourceType(pump.getKind());
                s = pump.getLightSourceModel();
                if (CommonsLangUtils.isBlank(s)) {
                    s = pump.getManufacturer();
                    if (CommonsLangUtils.isBlank(s)) {
                        s = pump.getSerialNumber();
                        if (CommonsLangUtils.isBlank(s)) {
                            s = pump.getLotNumber();
                        }
                        if (CommonsLangUtils.isBlank(s))
                            s = ""+pump.getId();
                    }
                }
                details.put(PUMP, value+": "+s);
            } else notSet.add(PUMP);
            s = data.getLaserMedium();
            if (CommonsLangUtils.isBlank(s))
                notSet.add(MEDIUM);
            details.put(MEDIUM, s);

            Length wl = null;
            try {
                wl = data.getLaserWavelength(null);
            } catch (BigResult e) {
                // can't happen as null is passed to the method
            }
            double wave = 0;
            if (wl == null) {
                notSet.add(WAVELENGTH);
            }
            else {
            	wave = wl.getValue();
            	details.put(WAVELENGTH, NF.format((wave))+NONBRSPACE+wl.getSymbol());
            }

            int i = data.getLaserFrequencyMultiplication();
            if (i < 0) {
                notSet.add(FREQUENCY_MULTIPLICATION);
            }
            else {
            	details.put(FREQUENCY_MULTIPLICATION, i);
            }
            Object o = data.getLaserTuneable();
            if (o == null) {
                notSet.add(TUNEABLE);
            }
            details.put(TUNEABLE, o);

            s = data.getLaserPulse();
            if (CommonsLangUtils.isBlank(s))
                notSet.add(PULSE);
            details.put(PULSE, s);
            Frequency freq = null; 
            try {
                freq = data.getLaserRepetitionRate(null);
            } catch (BigResult e) {
                // can't happen as null is passed to the method
            }
            if (freq == null) {
                notSet.add(REPETITION_RATE);
            }
            else {
            	f = freq.getValue();
            	details.put(REPETITION_RATE, NF.format(f)+NONBRSPACE+freq.getSymbol());
            }
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
        details.put(GAIN, Double.valueOf(0));
        details.put(VOLTAGE, Double.valueOf(0));
        details.put(OFFSET, Double.valueOf(0));
        details.put(ZOOM, Double.valueOf(0));
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
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MODEL);
        details.put(MODEL, s);
        s = data.getManufacturer();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(MANUFACTURER);
        details.put(MANUFACTURER, s);
        s = data.getSerialNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(SERIAL_NUMBER);
        details.put(SERIAL_NUMBER, s);
        s = data.getLotNumber();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(LOT_NUMBER);
        details.put(LOT_NUMBER, s);

        Double f = data.getGain();
        double v = 0;
        if (f == null) notSet.add(GAIN);
        else v = f.doubleValue();
        details.put(GAIN, NF.format(v));
        ElectricPotential p = null;
        try {
            p = data.getVoltage(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (p == null) {
            notSet.add(VOLTAGE);
        }
        else {
        	v = p.getValue();
        	details.put(VOLTAGE, NF.format(v)+NONBRSPACE+p.getSymbol());
        }
        f = data.getOffset();
        if (f == null) {
            notSet.add(OFFSET);
        }
        else {
        	v = f.doubleValue();
        	details.put(OFFSET, NF.format(v));
        }
        f = data.getZoom();
        if (f == null) {
            notSet.add(ZOOM);
        }
        else {
        	v = f.doubleValue();
        	details.put(ZOOM, NF.format(v));
        }
        f = data.getAmplificationGain();
        if (f == null) {
            notSet.add(AMPLIFICATION);
        }
        else {
        	v = f.doubleValue();
        	 details.put(AMPLIFICATION, NF.format(v));
        }
        s = data.getType();
        if (CommonsLangUtils.isBlank(s))
            notSet.add(TYPE);
        else
        	details.put(TYPE, s);
        details.put(NOT_SET, notSet);
        return details;
    }

    /**
     * Transforms the detector and the detector settings.
     *
     * @param data The value to convert.
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
        details.put(READ_OUT_RATE, Double.valueOf(0));
        details.put(BINNING, "");
        if (data == null) {
            notSet.add(READ_OUT_RATE);
            notSet.add(BINNING);
            details.put(NOT_SET, notSet);
            return details;
        }

        Double f = data.getDetectorSettingsGain();

        if (f != null)  {
            details.put(GAIN, NF.format(f));
            notSet.remove(GAIN);
        }

        ElectricPotential p = null;
        try {
            p = data.getDetectorSettingsVoltage(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (p != null) {
        	f = p.getValue();
            notSet.remove(VOLTAGE);
            details.put(VOLTAGE, NF.format(f)+NONBRSPACE+p.getSymbol());
        }

        f = data.getDetectorSettingsOffset();
        if (f != null) {
            notSet.remove(OFFSET);
            details.put(OFFSET, NF.format(f));
        }

        Frequency freq = null;
        try {
            freq = data.getDetectorSettingsReadOutRate(null);
        } catch (BigResult e) {
            // can't happen as null is passed to the method
        }
        if (freq == null) {
            notSet.add(READ_OUT_RATE);
        }
        else {
        	details.put(READ_OUT_RATE, NF.format(freq.getValue())+NONBRSPACE+freq.getSymbol());
        }
        String s = data.getDetectorSettingsBinning();
        if (CommonsLangUtils.isBlank(s)) {
            notSet.add(BINNING);
        }
        details.put(BINNING, s);
        details.put(NOT_SET, notSet);
        return details;
    }

    /**
     * Formats the passed value in seconds.
     *
     * @param value The value to transform.
     * @return See above.
     */
    public static String formatTimeInSeconds(Double value)
    {
        if (value == null) return "";
        return UIUtilities.formatTimeInSeconds(value.intValue());
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
        details.put(DELTA_T, Double.valueOf(0));
        details.put(EXPOSURE_TIME, Double.valueOf(0));
        details.put(POSITION_X, Double.valueOf(0));
        details.put(POSITION_Y, Double.valueOf(0));
        details.put(POSITION_Z, Double.valueOf(0));
        final List<String> notSet = new ArrayList<String>(5);
        notSet.add(DELTA_T);
        notSet.add(EXPOSURE_TIME);
        notSet.add(POSITION_X);
        notSet.add(POSITION_Y);
        notSet.add(POSITION_Z);
        details.put(NOT_SET, notSet);
        if (plane != null) {
            omero.model.Time t = plane.getDeltaT();
            if (t != null)  {
                notSet.remove(DELTA_T);
                try {
                    details.put(DELTA_T, roundValue(UnitsFactory.convertTime(t, UNITS.S).getValue()));
                } catch (BigResult e) {
                    details.put(DELTA_T, e);
                }
            }
            t = plane.getExposureTime();
            if (t != null) {
                notSet.remove(EXPOSURE_TIME);
                try {
                    details.put(EXPOSURE_TIME, roundValue(UnitsFactory.convertTime(t, UNITS.S).getValue()));
                } catch (BigResult e) {
                    details.put(EXPOSURE_TIME, e);
                }
            }

            Length o = plane.getPositionX();
            if (o != null) {
                notSet.remove(POSITION_X);
                details.put(POSITION_X, NF.format(o.getValue()));
            }
            o = plane.getPositionY();
            if (o != null) {
                notSet.remove(POSITION_Y);
                details.put(POSITION_Y, NF.format(o.getValue()));
            }
            o = plane.getPositionZ();
            if (o != null) {
                notSet.remove(POSITION_Z);
                details.put(POSITION_Z, NF.format(o.getValue()));
            }
        }
        return details;
    }

    /**
     * Initializes a <code>JComboBox</code>.
     *
     * @param values The values to display.
     * @param decrement The value by which the font size is reduced.
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
     * @param values The values to display.
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
        Iterator<Object> i = values.iterator();
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
     * Formats the label for a required field.
     *
     * @param value The value to display.
     * @param required Pass <code>true</code> if the field is required,
     *                 <code>false</code> otherwise.
     * @return See above
     */
    public static JComponent getLabel(String value, boolean required)
    {
        if (required) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            JLabel l = UIUtilities.setTextFont(value);
            p.add(l);
            l = UIUtilities.setTextFont(MANDATORY_SYMBOL);
            l.setForeground(UIUtilities.REQUIRED_FIELDS_COLOR);
            p.add(l);
            return p;
        }
        return UIUtilities.setTextFont(value);
    }

    /**
     * Formats the workflow.
     *
     * @param value The value to handle.
     * @return See above.
     */
    public static String getWorkflowForDisplay(String value)
    {
        if (value == null) return value;
        String result = value;
        if (value.contains("/")) {
            String[] list = value.split("/");
            result = list[list.length-1];
        }
        return result;
    }


    /**
     * Returns the date.
     *
     * @param object The object to handle.
     * @return See above.
     */
    public static String formatDate(DataObject object)
    {
        String date = "";
        Timestamp time = null;
        if (object == null) 
            return date;
        
        if (object instanceof AnnotationData)
            time = ((AnnotationData) object).getLastModified();
        else if (object instanceof ImageData)
            time = getAcquisitionTime((ImageData) object);
        else 
            time = object.getCreated();
        
        if (time != null && time.getTime()>0) 
            date = UIUtilities.formatDefaultDate(time);
        
        return date;
    }

    /**
     * Formats the tooltip for an image.
     *
     * @param object The object to handle.
     * @return See above.
     */
    public static List<String> formatObjectTooltip(DataObject object)
    {
        if (object == null) return null;
        if (!(object instanceof ImageData || object instanceof WellSampleData
                || object instanceof PlateData || object instanceof WellData))
            return null;

        List<String> l = new ArrayList<String>();
        String v;
        ImageData img = null;
        if (object instanceof ImageData) img = (ImageData) object;
        else if (object instanceof WellSampleData) {
           img = ((WellSampleData) object).getImage();
        } else if (object instanceof PlateData) {
            PlateData plate = (PlateData) object;
            v = plate.getPlateType();
            if (v != null && v.trim().length() > 0) {
                v = "<b>"+TYPE+": </b>"+v;
                l.add(v);
            }
            v = plate.getExternalIdentifier();
            if (v != null && v.trim().length() > 0) {
                v = "<b>"+EXTERNAL_IDENTIFIER+": </b>"+v;
                l.add(v);
            }
            v = plate.getStatus();
            if (v != null && v.trim().length() > 0) {
                v = "<b>"+STATUS+": </b>"+v;
                l.add(v);
            }
        } else if (object instanceof WellData) {
            WellData well = (WellData) object;
            v = well.getWellType();
            if (v != null && v.trim().length() > 0) {
                v = "<b>"+TYPE+": </b>"+v;
                l.add(v);
            }
            v = well.getExternalDescription();
            if (v != null && v.trim().length() > 0) {
                v = "<b>"+EXTERNAL_IDENTIFIER+": </b>"+v;
                l.add(v);
            }
            v = well.getStatus();
            if (v != null && v.trim().length() > 0) {
                v = "<b>"+STATUS+": </b>"+v;
                l.add(v);
            }
        }
        if (img == null) return l;
        v = "<b>"+ACQUISITION_DATE+": </b>"+formatDate(img);
        l.add(v);
        try {
            v = "<b>"+IMPORTED_DATE+": </b>"+
                    UIUtilities.formatDefaultDate(img.getInserted());
            l.add(v);
        } catch (Exception e) {}
        PixelsData data = null;
        try {
            data = img.getDefaultPixels();
        } catch (Exception e) {}
        if (data == null) return l;
        Map details = transformPixelsData(data);
        v = "<b>"+XY_DIMENSION+": </b>";
        v += (String) details.get(SIZE_X);
        v += " x ";
        v += (String) details.get(SIZE_Y);
        l.add(v);
        v = "<b>"+PIXEL_TYPE+": </b>"+details.get(PIXEL_TYPE);
        l.add(v);
        v = formatPixelsSize(details);
        if (v != null) l.add(v);
        v = "<b>ZxTxC: </b>";
        v += (String) details.get(SECTIONS);
        v += " x ";
        v += (String) details.get(TIMEPOINTS);
        v += " x ";
        v += (String) details.get(CHANNELS);
        l.add(v);
        return l;
    }

    /**
     * Returns the type of light source to handle.
     *
     * @param kind The type of light source.
     * @return See above.
     */
    public static String getLightSourceType(String kind)
    {
        if (LightSourceData.LASER.equals(kind))
            return LASER_TYPE;
        else if (LightSourceData.ARC.equals(kind))
            return ARC_TYPE;
        else if (LightSourceData.FILAMENT.equals(kind))
            return FILAMENT_TYPE;
        else if (LightSourceData.LIGHT_EMITTING_DIODE.equals(kind))
            return EMITTING_DIODE_TYPE;
        return "Light Source";
    }

    /**
     * Returns the node hosting the experimenter passing a child node.
     *
     * @param node The child node.
     * @return See above.
     */
    public static TreeImageDisplay getDataOwner(TreeImageDisplay node)
    {
        if (node == null) return null;
        TreeImageDisplay parent = node.getParentDisplay();
        Object ho;
        if (parent == null) {
            ho = node.getUserObject();
            if (ho instanceof ExperimenterData)
                return node;
            return null;
        }
        ho = parent.getUserObject();
        if (ho instanceof ExperimenterData) return parent;
        return getDataOwner(parent);
    }

    /**
     * Returns the node hosting the experimenter passing a child node.
     *
     * @param node The child node.
     * @return See above.
     */
    public static TreeImageDisplay getDataGroup(TreeImageDisplay node)
    {
        if (node == null) return null;
        TreeImageDisplay parent = node.getParentDisplay();
        Object ho;
        if (parent == null) {
            ho = node.getUserObject();
            if (ho instanceof GroupData)
                return node;
            return null;
        }
        ho = parent.getUserObject();
        if (ho instanceof GroupData) return parent;
        return getDataGroup(parent);
    }

    /**
     * Returns <code>true</code> if the node can be transfered,
     * <code>false</code> otherwise.
     *
     * @param target The target of the D&D action.
     * @param src The node to transfer.
     * @param userID The id of the user currently logged in.
     * @return See above.
     */
    public static boolean isTransferable(Object target, Object src, long userID)
    {
        if (target instanceof ProjectData && src instanceof DatasetData)
            return true;
        else if (target instanceof DatasetData && src instanceof ImageData)
            return true;
        else if (target instanceof ScreenData && src instanceof PlateData)
            return true;
        else if (target instanceof GroupData) {
            if (src instanceof ExperimenterData) return true;
            if (src instanceof DataObject) {
                GroupData g = (GroupData) target;
                DataObject data = (DataObject) src;
                return (g.getId() != data.getGroupId());
            }
        } else if (target instanceof ExperimenterData) {
            ExperimenterData exp = (ExperimenterData) target;
            return exp.getId() == userID;
        } else if (target instanceof TagAnnotationData
                && src instanceof TagAnnotationData) {
            TagAnnotationData tagSet = (TagAnnotationData) target;
            TagAnnotationData tag = (TagAnnotationData) src;
            if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                    tagSet.getNameSpace())) {
                return !(TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                        tag.getNameSpace()));
            }
            return false;
        }
        return false;
    }
    
    /**
     * Checks if the given namespace is an internal one.
     * 
     * @param ns
     *            The namespace to check
     * @return See above
     */
    public static boolean isInternalNS(String ns) {
        return ns.startsWith("openmicroscopy.org") || ns.startsWith("omero.");
    }
}

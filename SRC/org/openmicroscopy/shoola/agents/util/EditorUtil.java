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
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;

/** 
 * 
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
	
	 /** Text displaying before the owner's permissions. */
	public static final String    OWNER = "Owner: ";
    
    /** Text displaying before the group's permissions. */
    public static final String     GROUP = "Group: ";
    
    /** Text displaying before the world's permissions. */
    public static final String     WORLD = "Others: ";
    
    /** Text describing the <code>Read</code> permission. */
    public static final String     READ = "Read";
    
    /** Text describing the <code>Write</code> permission. */
    public static final String     WRITE = "Write";
    
	/** Text displayed before the list of existing groups. */
	public static final String	GROUPS = "Belongs to the following groups: ";
    
    /** Identifies the <code>Wavelengths</code> field. */
	public static final String	WAVELENGTHS = "Emissions wavelengths";
    
    /** String to represent the micron symbol. */
    private static final String MICRONS = "(in \u00B5m)";
    
    /** Identifies the <code>SizeX</code> field. */
    private static final String SIZE_X = "Size X";
    
    /** Identifies the <code>SizeY</code> field. */
    private static final String SIZE_Y = "Size Y";
    
    /** Identifies the <code>PixelSizeX</code> field. */
    private static final String PIXEL_SIZE_X = "Pixel size X "+MICRONS;
    
    /** Identifies the <code>PixelSizeY</code> field. */
    private static final String PIXEL_SIZE_Y = "Pixel size Y "+MICRONS;
    
    /** Identifies the <code>PixelSizeZ</code> field. */
    private static final String PIXEL_SIZE_Z = "Pixel size Z "+MICRONS;
    
    /** Identifies the <code>Sections</code> field. */
    private static final String SECTIONS = "Number of sections";
    
    /** Identifies the <code>Timepoints</code> field. */
    private static final String TIMEPOINTS = "Number of timepoints";

    /** Identifies the <code>PixelType</code> field. */
    private static final String PIXEL_TYPE = "Pixel Type";

    /** Identifies the <code>Acquisition date</code> field. */
    private static final String ACQUISITION_DATE = "Acquisition date";
    
    /** Identifies the <code>Name</code> field. */
    private static final String NAME = "Owner";
    
    /** Identifies the <code>Email</code> field. */
    private static final String EMAIL = "E-mail";
    
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
            details.put(NAME, "");
            details.put(EMAIL, "");
        } else {
            try {
                details.put(NAME, data.getFirstName()+" "+data.getLastName());
                details.put(EMAIL, data.getEmail());
            } catch (Exception e) {
                details.put(NAME, "");
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
                details.put(PIXEL_TYPE, ""+data.getPixelType()); 
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
            details.put(ACQUISITION_DATE, 
            			TreeViewerTranslator.DATE_NOT_AVAILABLE);
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
                details.put(PIXEL_TYPE, ""+data.getPixelType()); 
            } catch (Exception e) {
                details.put(PIXEL_SIZE_X, "");
                details.put(PIXEL_SIZE_Y, "");
                details.put(PIXEL_SIZE_Z, "");
                details.put(PIXEL_TYPE, ""); 
            }
        }
        details.put(WAVELENGTHS, ""); 
        Timestamp date = null;
        try {
        	date = image.getInserted();
		} catch (Exception e) {}
        
        if (date == null) 
        	details.put(ACQUISITION_DATE, 
        				TreeViewerTranslator.DATE_NOT_AVAILABLE);
        else 
        	details.put(ACQUISITION_DATE, UIUtilities.formatTime(date));
        return details;
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
    
}

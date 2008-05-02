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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.ProjectData;

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
	
	/** Default text displayed in the acquisition date is not available. */
	public static final String DATE_NOT_AVAILABLE = "Not available";
	
    /** Identifies the <code>Default group</code>. */
	public static final String	DEFAULT_GROUP = "Default Group";
	
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
    
	/** Text displayed before the list of existing groups. */
	public static final String	GROUPS = "Belongs to the following groups: ";
    
    /** Identifies the <code>Wavelengths</code> field. */
	public static final String	WAVELENGTHS = "Emissions wavelengths";
    
	 /** Identifies the <code>Email</code> field. */
    public static final String	EMAIL = "E-mail";
    
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
                details.put(PIXEL_TYPE, ""+data.getPixelType()); 
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
			
		else if (object instanceof ProjectData) {
			counts = ((ProjectData) object).getAnnotationsCounts();
		}
		else if (object instanceof ImageData) {
			counts = ((ImageData) object).getAnnotationsCounts();
		}
			
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
			
		else if (object instanceof ProjectData) {
			counts = ((ProjectData) object).getAnnotationsCounts();
		}
		else if (object instanceof ImageData) {
			counts = ((ImageData) object).getAnnotationsCounts();
		}
			
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
		else if (object instanceof DatasetData) {
			
			counts = ((DatasetData) object).getAnnotationsCounts();
		}
			
		else if (object instanceof ProjectData) 
			counts = ((ProjectData) object).getAnnotationsCounts();
		
		if (counts == null || counts.size() == 0) return false;
		Set set = counts.keySet();
		if (set.size() > 1) return true;
		return !set.contains(userID);
	}
	
	public static Map getAnnotations(Object object, Map userGroups)
	{
		Map<ExperimenterData, Long> 
			result = new HashMap<ExperimenterData, Long>();
		if (object == null) return result;
		Map<Long, Long> counts = null;
		if (object instanceof ImageData)
			counts = ((ImageData) object).getAnnotationsCounts();
		else if (object instanceof DatasetData)
			counts = ((DatasetData) object).getAnnotationsCounts();
		else if (object instanceof ProjectData) 
			counts = ((ProjectData) object).getAnnotationsCounts();
		
		if (counts == null) return result;
		Iterator i = userGroups.keySet().iterator(), j;
		GroupData g;
		Map<Long, ExperimenterData> 
			users = new HashMap<Long, ExperimenterData>();
		Set children;
		ExperimenterData user;
 		while (i.hasNext()) {
			g = (GroupData) i.next();
			children = g.getExperimenters();
			if (children != null) {
				j = children.iterator();
				while (j.hasNext()) {
					user = (ExperimenterData) j.next();
					users.put(user.getId(), user);
				}
			}
		}
 		i = counts.keySet().iterator();
 		Long id;
 		while (i.hasNext()) {
 			id = (Long) i.next();
 			user = users.get(id);
 			if (user != null)
 				result.put(users.get(id), counts.get(id));
		}
		return result;
	}
	
}

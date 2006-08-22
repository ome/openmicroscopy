/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorUtil
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;



//Java imports
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.PixelsData;

/** 
 * Helper class to transform a <code>DataObject</code> into a visualization
 * representation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class EditorUtil
{
    
    /** Text displayed before the list of existing groups. */
    static final String GROUPS = "Belongs to the following groups: ";

    /** String to represent the micron symbol. */
    private static final String MICRONS = "(in \u00B5)";
    
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

    /** Identifies the <code>Name</code> field. */
    private static final String NAME = "Name";
    
    /** Identifies the <code>Leader</code> field. */
    private static final String LEADER = "Leader";
    
    /** Identifies the <code>Email</code> field. */
    private static final String EMAIL = "Email";
    
    /** Identifies the <code>Institution</code> field. */
    private static final String INSTITUTION = "Institution";
    
    /**
     * Transforms the specified {@link ExperimenterData} object into 
     * a visualization form.
     * 
     * @param data The {@link ExperimenterData} object to transform.
     * @return The map whose keys are the field names, and the values 
     * 			the corresponding fields' values.
     */
    static Map transformExperimenterData(ExperimenterData data)
    {
        LinkedHashMap details = new LinkedHashMap(3);
        if (data == null) {
            details.put(NAME, "");
            details.put(EMAIL, "");
            details.put(INSTITUTION, "");
            
        } else {
            details.put(NAME, data.getFirstName()+" "+data.getLastName());
            details.put(EMAIL, data.getEmail());
            details.put(INSTITUTION, data.getInstitution());
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
    static Map transformPixelsData(PixelsData data)
    {
        LinkedHashMap details = new LinkedHashMap(8);
        if (data == null) {
            details.put(SIZE_X, "");
            details.put(SIZE_Y, "");
            details.put(PIXEL_SIZE_X, "");
            details.put(PIXEL_SIZE_Y, "");
            details.put(PIXEL_SIZE_Z, "");
            details.put(SECTIONS, "");
            details.put(TIMEPOINTS, "");
            //details.put("Emission wavelength", "");
            details.put(PIXEL_TYPE, "");  
        } else {
            NumberFormat    nf = NumberFormat.getInstance();
            details.put(SIZE_X, ""+data.getSizeX());
            details.put(SIZE_Y, ""+data.getSizeY());
            details.put(PIXEL_SIZE_X, nf.format(data.getPixelSizeX()));
            details.put(PIXEL_SIZE_Y, nf.format(data.getPixelSizeY()));
            details.put(PIXEL_SIZE_Z, nf.format(data.getPixelSizeZ()));
            details.put(SECTIONS, ""+data.getSizeZ());
            details.put(TIMEPOINTS, ""+data.getSizeT());
            //details.put("Emission wavelength", "");
            details.put(PIXEL_TYPE, ""+data.getPixelType());  
        }
        return details;
    }
    
    /**
     * Transforms the specified {@link GroupData} object into 
     * a visualization form.
     * 
     * @param data The {@link GroupData} object to transform.
     * @return The map whose keys are the field names, and the values 
     * 			the corresponding fields' values.
     */
    static Map transformGroup(GroupData data)
    {
        HashMap details = new HashMap(2);
        if (data == null) {
            details.put(NAME, "");
            details.put(LEADER, "");
        } else {
            details.put(NAME, data.getName());
            ExperimenterData exp = data.getOwner();
            if (exp != null)
                details.put(LEADER, exp.getFirstName()+" "+exp.getLastName());
            else details.put(LEADER, "");
        }
        return details;
    }
      
}

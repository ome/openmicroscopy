/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.info.InfoPaneUtil
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.info;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import pojos.PixelsData;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class to transform a <code>DataObject</code> into a visualization
 * representation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class InfoPaneUtil
{
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
    
    /** Identifies the <code>EmissionWavelength</code> field. */
    //private static final String EMISSION_WAVELENGTH = "Emission wavelength";
    
    /**
     * Transforms the specified {@link PixelsData} object into 
     * a visualization form.
     * 
     * @param data The {@link PixelsData} object to transform.
     * @return The map whose keys are the field names, and the values 
     *          the corresponding fields' values.
     */
    static Map transformPixelsData(PixelsData data)
    {
        LinkedHashMap details = new LinkedHashMap(8);
        if (data == null) {
            details.put(SIZE_X, "");
            details.put(SIZE_Y, "");
            
            details.put(SECTIONS, "");
            details.put(TIMEPOINTS, "");
            //details.put("Emission wavelength", "");
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
            //details.put("Emission wavelength", "");
            try {
                details.put(PIXEL_SIZE_X, nf.format(data.getPixelSizeX()));
                details.put(PIXEL_SIZE_Y, nf.format(data.getPixelSizeY()));
                details.put(PIXEL_SIZE_Z, nf.format(data.getPixelSizeZ()));
                details.put(PIXEL_TYPE, ""+data.getPixelType()); 
            } catch (Exception e) {
                details.put(PIXEL_TYPE, ""); 
            }
             
        }
        return details;
    }
    
}

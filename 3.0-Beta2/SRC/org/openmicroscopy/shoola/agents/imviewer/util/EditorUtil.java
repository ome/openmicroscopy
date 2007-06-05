/*
 * org.openmicroscopy.shoola.agents.imviewer.util.EditorUtil
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.util;


//Java imports
import java.util.LinkedHashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;

/** 
 * 
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
class EditorUtil
{

    /** String to represent the micron symbol. */
    private static final String MICRONS = "(in \u00B5)";
    
    /** Identifies the <code>Emission wavelength</code> field. */
    private static final String EM_WAVE = "Emission Wavelength "+MICRONS;
    
    /** Identifies the <code>Excitation wavelength</code> field. */
    private static final String EX_WAVE = "Excitation Wavelength "+MICRONS;
    
    /** Identifies the <code>Pin hole size</code> field. */
    private static final String PIN_HOLE_SIZE = "Pin hole size";
    
    /** Identifies the <code>ND filter</code> field. */
    private static final String ND_FILTER = "ND Filter";
    
    /**
     * Transforms the specified channel information.
     * 
     * @param data  The object to transform.
     * @return      The map whose keys are the field names, and the values 
     *              the corresponding fields' values.
     */
    static Map transformChannelData(ChannelMetadata data)
    {
        LinkedHashMap<String, String> 
        		details = new LinkedHashMap<String, String>(4);
        if (data == null) {
            details.put(EM_WAVE, "");
            details.put(EX_WAVE, "");
            details.put(ND_FILTER, "");
            details.put(PIN_HOLE_SIZE, "");
        } else {
            details.put(EM_WAVE, ""+data.getEmissionWavelength());
            details.put(EX_WAVE, ""+data.getEmissionWavelength());
            details.put(ND_FILTER, ""+data.getNDFilter());
            details.put(PIN_HOLE_SIZE, ""+data.getPinholeSize());
        }
        return details;
    }
    
}

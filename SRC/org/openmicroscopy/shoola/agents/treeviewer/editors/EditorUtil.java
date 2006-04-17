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
    static final String	GROUPS = "Belongs to the following groups: ";
    
    /** String to represent the micron symbol. */
    private static final String MICRONS = "(in \u00B5)";

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
        HashMap details = new HashMap(3);
        if (data == null) {
            details.put("Name", "");
            details.put("Email", "");
            details.put("Institution", "");
            
        } else {
            details.put("Name", data.getFirstName()+" "+data.getLastName());
            details.put("Email", data.getEmail());
            details.put("Institution", data.getInstitution());
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
        HashMap details = new HashMap(9);
        if (data == null) {
            details.put("Size X", "");
            details.put("Size Y", "");
            details.put("Pixel size X "+MICRONS, "");
            details.put("Pixel size Y "+MICRONS, "");
            details.put("Pixel size Z "+MICRONS, "");
            details.put("Sections", "");
            details.put("Timepoints", "");
            //details.put("Emission wavelength", "");
            details.put("Pixel Type", "");  
        } else {
            NumberFormat    nf = NumberFormat.getInstance();
            
            details.put("Size X", ""+data.getSizeX());
            details.put("Size Y", ""+data.getSizeY());
            details.put("Pixel size X "+MICRONS, 
                        nf.format(data.getPixelSizeX()));
            details.put("Pixel size Y "+MICRONS, 
                        nf.format(data.getPixelSizeY()));
            details.put("Pixel size Z "+MICRONS, 
                        nf.format(data.getPixelSizeZ()));
            details.put("Sections", ""+data.getSizeZ());
            details.put("Timepoints", ""+data.getSizeT());
            //details.put("Emission wavelength", "");
            details.put("Pixel Type", ""+data.getPixelType());  
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
            details.put("Name", "");
            
        } else {
            details.put("Name", data.getName());
            ExperimenterData exp = data.getOwner();
            if (exp != null)
                details.put("Leader", exp.getFirstName()+" "+exp.getLastName());
            else details.put("Leader", "");
        }
        return details;
    }
      
}

/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor.EditorPaneUtil
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor;


//Java imports
import java.util.LinkedHashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import pojos.ExperimenterData;

/** 
 * Helper class to transform a <code>DataObject</code> into a visualization
 * representation.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class EditorPaneUtil
{
    
    /** Identifies the <code>Name</code> field. */
    private static final String NAME = "Owner";
    
    /** Identifies the <code>Email</code> field. */
    private static final String EMAIL = "Email";
    
    /**
     * Transforms the specified {@link ExperimenterData} object into 
     * a visualization form.
     * 
     * @param data The {@link ExperimenterData} object to transform.
     * @return The map whose keys are the field names, and the values 
     *          the corresponding fields' values.
     */
    static Map transformExperimenterData(ExperimenterData data)
    {
        LinkedHashMap details = new LinkedHashMap(3);
        if (data == null) {
            details.put(NAME, "");
            details.put(EMAIL, "");  
        } else {
            details.put(NAME, data.getFirstName()+" "+data.getLastName());
            details.put(EMAIL, data.getEmail());
        }
        return details;
    }

}

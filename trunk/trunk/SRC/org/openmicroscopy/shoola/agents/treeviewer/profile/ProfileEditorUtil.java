/*
 * org.openmicroscopy.shoola.agents.treeviewer.profile.ProfileEditorUtil 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.profile;



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
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ProfileEditorUtil
{
    
	/** Identifies the <code>Default group</code>. */
	static final String			DEFAULT_GROUP = "Default Group";
	
	/** Symbols indicating the mandatory values. */
	static final String			MANDATORY_SYMBOL = " *";
	
	/** Description of the mandatory symbol. */
	static final String			MANDATORY_DESCRIPTION = "* indicates the " +
													"required fields.";
	
	/** Identifies the <code>Last name</code> field. */
    static final String 		LAST_NAME = "Last Name";
    
    /** Identifies the <code>Email</code> field. */
    static final String 		EMAIL = "E-mail";
    
    /** Identifies the <code>First name</code> field. */
    static final String 		FIRST_NAME = "First Name";

    /** Identifies the <code>Last name</code> field. */
    static final String 		INSTITUTION = "Institution";

	/**
     * Transforms the specified {@link ExperimenterData} object into 
     * a visualization form.
     * 
     * @param data The {@link ExperimenterData} object to transform.
     * @return The map whose keys are the field names, and the values 
     * 			the corresponding fields' values.
     */
    static Map<String, String> manageExperimenterData(ExperimenterData data)
    {
        LinkedHashMap<String, String> details = 
        							new LinkedHashMap<String, String>(3);
        //ADD USER NAME;
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
    
}

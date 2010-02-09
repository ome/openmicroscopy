/*
 * org.openmicroscopy.shoola.env.data.views.calls.AdminSaver 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.views.calls;

import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.ExperimenterData;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
 * @since 3.0-Beta4
 */
public class AdminSaver extends BatchCallTree
{

    /** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to create experimenters.
     * 
	 * @param object 	The experimenters to create.
     * @return The {@link BatchCall}.
     */
    private BatchCall createExperimenters(final AdminObject object)
    {
        return new BatchCall("Create experimenters") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.createExperimenters(object);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to create group.
     * 
	 * @param object 	The experimenters to create.
     * @return The {@link BatchCall}.
     */
    private BatchCall createGroup(final AdminObject object)
    {
        return new BatchCall("Create group") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.createGroup(object);
            }
        };
    }
    
	 /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param exp The experimenter to update. Mustn't be <code>null</code>.
     */
    public AdminSaver(AdminObject object)
    {
    	if (object == null)
    		throw new IllegalArgumentException("Object not valid.");
    	switch (object.getIndex()) {
			case AdminObject.CREATE_EXPERIMENTER:
				loadCall = createExperimenters(object);
				break;
			case AdminObject.CREATE_GROUP:
				loadCall = createGroup(object);
				break;
		}
    }
    
}

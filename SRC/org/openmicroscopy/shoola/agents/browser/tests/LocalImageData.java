/*
 * org.openmicroscopy.shoola.agents.browser.tests.LocalImageData
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.tests;

import java.util.List;

import org.openmicroscopy.shoola.env.data.model.ImageData;

/**
 * A temporary class that extends ImageData to contain a local path of an
 * image, so that it can be loaded in local testing classes (detach from OME,
 * OMEIS)
 * 
 * REMOVE THIS CLASS ONCE INTEGRATION IS COMPLETE
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class LocalImageData extends ImageData
{
    private String localPath;
    
    /**
     * Constructs the dummy class with the specified name.
     * @param localPath
     */
    public LocalImageData(String localPath)
    {
        super();
        this.localPath = localPath;
    }
    
    public LocalImageData(String localPath,
                          int ID,
                          String name,
                          String description,
                          String inserted,
                          String created,
                          int ownerID,
                          String ownerFirstName,
                          String ownerLastName,
                          String ownerEmail,
                          String ownerInstitution,
                          int ownerGroupID,
                          String ownerGroupName,
                          List pixels,
                          List datasets)
                          
    {
        super(ID,name,description,inserted,created,ownerID,ownerFirstName,
              ownerLastName,ownerEmail,ownerInstitution,ownerGroupID,
              ownerGroupName,pixels,datasets);
        this.localPath = localPath;
    }
    
    /**
     * Returns the local path.
     * @return See above.
     */
    public String getLocalPath()
    {
        return localPath;
    }
    
    public void setLocalPath(String localPath)
    {
        // test class-- n real need to nullcheck (will throw error in nearly
        // all cases)
        this.localPath = localPath;
    }
}

/*
 * org.openmicroscopy.shoola.agents.events.datamng.ShowProperties
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

package org.openmicroscopy.shoola.agents.events.datamng;


//Java imports
import java.awt.Component;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.ImageData;
import pojos.PixelsData;

/**
 * Request to edit the specified <code>DataObject</code>.
 * This event should be caught by agents managing the users' data.
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *               <a href="mailto:a.falconi@dundee.ac.uk">
 *                   a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ShowProperties
  extends RequestEvent
{
    
    /** Indicates that the request is for a <code>CREATE</code> editor. */
    public static final int CREATE = 0;
    
    /** Indicates that the request is for a <code>EDIT</code> editor. */
    public static final int EDIT = 1;
    
    //TO BE REMOVED
    private Component   parent;
      
    /** The <code>DataObject</code> to edit. */
    private pojos.DataObject    userObject;
    
    /** One of the constants defined by this class. */
    private int                 editorType;
      
    /**
     * Checks if the type specified is one of the constants defined 
     * by this class.
     * 
     * @param type The type to check.
     */
    private void checkEditorType(int type)
    {
        switch (type) {
            case CREATE:
            case EDIT:    
                return;
            default:
                throw new IllegalArgumentException("Editor type not supported");
        }
    }
    
    //TO REMOVE ASAP
    public ShowProperties(DataObject object) 
    {
        if (object instanceof ImageSummary) {
            ImageSummary is = (ImageSummary) object;
            ImageData data = new ImageData();
            data.setId(is.getID());
            data.setName(is.getName());
            data.setCreated(is.getDate());
            PixelsDescription pix = is.getDefaultPixels();
            PixelsData pixData = new PixelsData();
            pixData.setId(pix.getID());
            pixData.setImageServerID(pix.getImageServerID());
            pixData.setImageServerURL(pix.getImageServerUrl());
            data.setDefaultPixels(pixData);
            userObject = data;
        } else
            throw new IllegalArgumentException("Dataobject not valid.");
      
        parent = null;
    }
  
    
    public ShowProperties(pojos.DataObject userObject, int type) 
    {
        if (userObject == null)
            throw new IllegalArgumentException("Object to edit cannot be null");
        checkEditorType(type);
        this.userObject = userObject;
        editorType = type;
        parent = null;
    }
      
    public ShowProperties(pojos.DataObject userObject, Component parent) 
    {
        this.userObject = userObject;
        this.parent = parent;
    }
      
    public Component getParent() { return parent; }
      
    /**
     * Returns the <code>DataObject</code> this request is for.
     * 
     * @return See above.
     */
    public pojos.DataObject getUserObject() { return userObject; }
    
    /**
     * Returns the type of editor requested.
     * 
     * @return See above.
     */
    public int getEditorType() { return editorType; }
  
}

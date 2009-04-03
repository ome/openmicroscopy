/*
 * org.openmicroscopy.shoola.agents.events.treeviewer.ShowProperties
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

package org.openmicroscopy.shoola.agents.events.treeviewer;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.DataObject;


/** 
 * Request to edit the specified <code>DataObject</code>.
 * This event should be caught by agents managing the users' data.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
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
    
    /** The <code>DataObject</code> to edit or create. */
    private DataObject  userObject;
    
    /** The type of editor, one of the constants defined by this class. */
    private int         editorType;
    
    /**
     * Controls if the type specified is one of the constants defined 
     * by this class.
     * 
     * @param type The type to control.
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
    
    /**
     * Creates a new instance.
     * 
     * @param userObject    The object to edit or create. Mustn't be 
     *                      <code>null</code>.
     * @param type          The type of editor. One of the constants defined by 
     *                      this class.
     */
    public ShowProperties(DataObject userObject, int type) 
    {
        if (userObject == null)
            throw new IllegalArgumentException("No data object.");
        checkEditorType(type);
        this.userObject = userObject;
        editorType = type;
    }
    
    /**
     * Returns the <code>DataObject</code> this request is for.
     * 
     * @return See above.
     */
    public DataObject getUserObject() { return userObject; }
    
    /**
     * Returns the type of editor.
     * 
     * @return See above.
     */
    public int getEditorType() { return editorType; }
    
}

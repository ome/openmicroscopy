/*
 * rg.openmicroscopy.shoola.agents.treeviewer.util.FilterCheckBox
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

package org.openmicroscopy.shoola.agents.treeviewer.util;




//Java imports
import javax.swing.JCheckBox;



//Third-party libraries

//Application-internal dependencies
import pojos.CategoryData;
import pojos.DataObject;
import pojos.DatasetData;

/** 
 * A selection box hosting a <code>DataObject</code> to be selected.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class FilterCheckBox
    extends JCheckBox
{

    /** The <code>Data Object</code> to handle. */
    private DataObject dataObject;
    
    /** 
     * Returns the name of the  {@link DataObject}.
     * 
     * @return See above.
     */
    private String getDataObjectName()
    {
        if (dataObject instanceof DatasetData)
            return ((DatasetData) dataObject).getName();
        else if (dataObject instanceof CategoryData)
            return ((CategoryData) dataObject).getName();
        return "";
    }
    
    /**
     * Creates a new instance.
     * 
     * @param object    The <code>DataObject</code> to handle. Mustn't be 
     *                  <code>null</code>.
     */
    public FilterCheckBox(DataObject object)
    {
        super();
        if (object == null) 
            throw new NullPointerException("Data object cannot be null.");
        dataObject = object;
        setText(getDataObjectName());
    }
    
    /**
     * Returns the {@link DataObject}.
     * 
     * @return See above.
     */
    public DataObject getDataObject() { return dataObject; }
    
    /**
     * Returns the {@link DataObject}'s ID as an <code>Integer</code>.
     * 
     * @return See above.
     */
    public Integer getDataObjectID()
    {
        if (dataObject instanceof DatasetData)
            return new Integer(((DatasetData) dataObject).getId());
        else if (dataObject instanceof CategoryData)
            return new Integer(((CategoryData) dataObject).getId());
        return null;
    }

}

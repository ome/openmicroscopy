/*
 * org.openmicroscopy.shoola.env.data.model.CategoryGroupData
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

package org.openmicroscopy.shoola.env.data.model;

import java.util.List;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class CategoryGroupData
    implements DataObject
{

    private int         id;
    private String      name;
    private String      description;
    
    /** 
     * List of {@link CategorySummary} objects or 
     * {@link CategoryData} objects. 
     */
    private List        categories;
    
    public CategoryGroupData()
    {
        id = -1;    //default
    }
    
    public CategoryGroupData(int id, String name, String description, List
                            categories) 
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categories = categories;
    }
    
    /** Required by the DataObject interface. */
    public DataObject makeNew() {  return new CategoryGroupData(); }

    public String toString() { return name; }
    
    public String getDescription() { return description; }
    
    public void setDescription(String description) 
    {
        this.description = description;
    }
    
    public int getID() { return id; }
    
    public void setID(int id) { this.id = id; }
    
    public String getName() { return name; }
    
    public void setName(String name) { this.name = name; }
    
    public void setCategories(List l) { categories = l; }
    
    public List getCategories() { return categories; }
    
}

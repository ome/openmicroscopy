/*
 * pojos.DataObject
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

package pojos;

//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.Iterator;
import java.util.Set;

import ome.api.ModelBased;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.util.ModelMapper;

/** 
 * Abstract superclass for objects that hold <i>OMEDS</i> data.
 * Subclasses should be struct-like,
 * with <code>public</code> fields to hold the data.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public abstract class DataObject implements ModelBased
{
    
    private long id = -1;
    
    private boolean loaded = true;
    
    private Set filtered;

    public long getId()
    {
        return id;
    }
    
    public void setId(long id)
    {
        this.id = id;
    }
    
    public boolean isLoaded()
    {
        return loaded;
    }
    
    public void setLoaded(boolean loaded)
    {
        this.loaded = loaded;
    }

    protected Set getFiltered()
    {
        return filtered;
    }
    
    protected void setFiltered(Set filtered)
    {
        this.filtered = filtered;
    }
    
    public boolean isFiltered(String fieldName)
    {
        if (getFiltered() == null) return false;
        return getFiltered().contains(fieldName);
    }
    
    public String toString() {
        return getClass().getName()+" (id="+getId()+")";
    }
    
    public void copy(IObject model, ModelMapper mapper)
    {
        this.setId(mapper.nullSafeLong(model.getId()));
        this.setLoaded(model.isLoaded());
        this.setFiltered(model.getDetails()==null ? 
                null : model.getDetails().filteredSet());
    }
    
    /** 
     * 
     * @param model
     * @return continue tells whether object should be further filled.
     */
    public boolean fill(IObject model)
    {
        if (this.getId() > -1)
            model.setId(new Long(this.getId()));
        
        model.setDetails(new Details());
        if (!this.isLoaded()){
            model.unload();
            return false;
        } else {
            if (this.filtered != null) {
                for (Iterator it = this.filtered.iterator(); it.hasNext();)
                {
                    model.getDetails().addFiltered((String)it.next());
                }
            }
            return true;
        }
    }

    
}

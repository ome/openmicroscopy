/*
 * ome.parameters.Page
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

package ome.parameters;

//Java imports
import java.util.ArrayList;
import java.util.Collection;

import ome.conditions.ApiUsageException;

//Third-party libraries

//Application-internal dependencies

/** 
 * parameter which defines the ordering as well as the start and offset
 * for a List-valued result set.  
 * 
 * @author  <br>Josh Moore&nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">
 * 					josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 3.0-M2
 */
public class Period
{
    final private int m_offset;
    final private int m_limit;
    final private Collection m_order = new ArrayList();
    
    public Period(int offset, int limit){
        m_offset = offset;
        m_limit = limit;
    }
    
    public Period add( String field )
    {
        if ( null == field )
            throw new ApiUsageException(
                    "Field name argument to addOrder cannot be null."
                    );
        m_order.add( field );
        return this;
    }
    
}

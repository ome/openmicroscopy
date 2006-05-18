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

//Third-party libraries

//Application-internal dependencies

/** 
 * parameter to generally reduce the size of a query result set.    
 * 
 * @author  <br>Josh Moore&nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">
 * 					josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since 3.0-M2
 * @see ome.parameters.Page
 * @see ome.parameters.Order
 * @see 
 */
public class Filter
{
    private long id_owner, id_group;
    private Page page = new Page( 0, Integer.MAX_VALUE );

    // ~ Owner
    // =========================================================================
    public Filter owner( long ownerId )
    { 
        id_owner = ownerId;
        return this;
    }
    
    public long owner()
    {
        return id_owner;
    }
    
    public Filter group( long groupId )
    {
        id_group = groupId;
        return this;
    }
    
    public long group()
    {
        return id_group;
    }
    
    // ~ Page
    // =========================================================================
    
    public Filter page( int offset, int limit )
    {
        page = new Page( offset, limit );
        return this;
    }
    
    public int firstResult()
    {
        return page.offset();
    }
    
    public int maxResults()
    {
        return page.limit();
    }
    
}

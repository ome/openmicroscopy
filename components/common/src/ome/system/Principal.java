/*
 * ome.system.Principal
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.system;

//Java imports
import java.io.Serializable;

//Third-party libraries

//Application-internal dependencies

/**
* implementation of {@link java.security.Principal}. Specialized for Omero
* to carry a <code>group</code> and <code>eventType</code> string value. 
* 
* @author <br>
*         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
*         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
* @version 1.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
*          </small>
* @since OME3.0
*/
public class Principal implements java.security.Principal, Serializable
{
    
    protected String name;
    protected String group;
    protected String type;
    
    public Principal(String name, String group, String eventType ) 
    {
        this.name = name;
        this.group = group;
        this.type = eventType;
    }

    public String getName()
    {
        return this.name;
    }
    
    public String getGroup()
    {
        return this.group;
    }
    
    public String getEventType()
    {
        return this.type;
    }
    
    public String toString()
    {
        return this.name;
    }

    public boolean equals(Object obj)
    {
        if ( ! (obj instanceof Principal))
            return false;
        
        Principal p = (Principal) obj;
        
        if ( this == p )
            return true;
        
        if ( p.name == null || ! p.name.equals( this.name ) )
            return false;
        
        if ( p.group == null || ! p.group.equals( this.group ) )
            return false;
        
        if ( p.type == null || ! p.type.equals( this.type) )
            return false;
        
        return true;
    }
    
    public int hashCode()
    {
        int result = 11;
        result = 17 * result + name.hashCode();
        result = 19 * result + group.hashCode();
        result = 23 * result + type.hashCode();
        return result;
    }
    
}

/*
 * ome.client.JndiStatefulObjectFactoryBean
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

package ome.client;

//Java imports


//Third-party libraries
import javax.naming.NamingException;

import org.springframework.jndi.JndiObjectFactoryBean;

import ome.conditions.InternalException;

//Application-internal dependencies

/** 
 * allows prototype-like lookup of stateful session beans.  This is achieved by
 * overriding {@link JndiObjectFactoryBean#isSingleton()} to always return false
 * (i.e. prototype) and by recalling {@link JndiObjectFactoryBean#afterPropertiesSet()}
 * on each {@link JndiObjectFactoryBean#getObject()} call. 
 * 
 * This class is fairly sensitive to changes in {@link JndiObjectFactoryBean}.
 *  
 *  @author  <br>Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:josh.more@gmx.de">
 *                  josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME3.0
 * @see ome.client.Session#register(IObject)
 */
public class JndiStatefulObjectFactoryBean extends JndiObjectFactoryBean
{
    @Override
    public boolean isSingleton()
    {
        return false;
    }
    
    @Override
    public Object getObject()
    {
        try {
            afterPropertiesSet();
        } catch ( NamingException ne ) {
            InternalException ie = new InternalException( ne.getMessage() );
            ie.setStackTrace( ne.getStackTrace() );
            throw ie;
        }
        
        return super.getObject();
    }
    
    
}

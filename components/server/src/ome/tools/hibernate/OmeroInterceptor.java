/*
 * ome.tools.hibernate.OmeroSessionFactoryBean
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

package ome.tools.hibernate;

//Java imports
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

//Third-party libraries
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

//Application-internal dependencies
import ome.conditions.InternalException;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;



/** 
 * extends {@link org.hibernate.EmptyInterceptor} for controlling various
 * aspects of the Hibernate runtime.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 */
public class OmeroInterceptor extends EmptyInterceptor{

    @Override
    public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)
    {
        if ( entity instanceof Experimenter )
        {
            return new int[]{};
        }
        
        // Use default logic.
        return null;
    }
    
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
    {
        int i = detailsIndex( propertyNames );
        Details details = (Details) state[i];
        return false;
    }
    
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)
    {
        int i = detailsIndex( propertyNames );
        Details currentDetails = (Details) currentState[i];
        Details previousDetails = (Details) previousState[i];
        
        return false;
    }
    
    // ~ Helpers
    // =========================================================================
    private int detailsIndex( String[] propertyNames )
    {
        for (int i = 0; i < propertyNames.length; i++)
        {
            if ( propertyNames[i].equals( "details" ))
                return i;
        }
        throw new InternalException( "No \"details\" property found." );
    }

    // ~ Serialiation
    // =========================================================================
    
    private static final long serialVersionUID = 7616611615023614920L;
    
    private void readObject(ObjectInputStream s) 
    throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
    }
     
}

/*
 * ome.tools.hibernate.UpdateFilter
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

// Java imports
import java.util.Collection;

// Third-party libraries
import ome.model.IObject;
import ome.security.CurrentDetails;
import ome.util.ContextFilter;
import ome.util.Filterable;

// Application-internal dependencies

/**
 * enforces the detached-graph re-attachment "Commandments" as outlined in TODO.
 * 
 * Objects that are transient (no ID) are given details from the CurrentDetails.
 * Objects that are managed (with ID) are checked for validity. (TODO implement)
 * Collections that ...
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class UpdateFilter extends ContextFilter
{

    @Override
    public Filterable filter(String fieldId, Filterable f)
    {
        if (f instanceof IObject)
        {
            IObject obj = (IObject) f;
            State state = test(obj);

            switch (state)
            {
                case TRANSIENT:
                    transferDetails(obj);
                    break;

                default:
                    break;
            }

        }

        return super.filter(fieldId, f);
    }

    @Override
    public Collection filter(String fieldId, Collection c)
    {
        if (c == null)
        {
            Object o = this.currentContext();
            if (o instanceof IObject)
            {
                State state = test((IObject) o);
                switch (state)
                {
                    case MANAGED:
                        // obtain the
                        break;
                    default:
                        break;
                }
            }
        }
        return super.filter(fieldId, c);
    }

    
    protected void transferDetails(IObject m)
    {
        if (m.getDetails() == null) 
            /* FIXME and if not null?
             * then need CDetails.fillDetails()
             * which to change
             */
        {
            m.setDetails(CurrentDetails.createDetails());
        }
        /* FIXME check for valid type creation
         * i.e. no creating types, users, etc.
         */
        
    }
    
    public enum State { NULL,MANAGED,TRANSIENT };
    
    public State test(IObject obj)
    {
        if (obj == null) return State.NULL;
        if (obj.getId() == null) return State.TRANSIENT;
        return State.MANAGED;
    }
    
    
}

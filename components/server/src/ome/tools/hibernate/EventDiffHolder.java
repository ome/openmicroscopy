/* ome.tools.hibernate.EventDiffHoler
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
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Application-internal dependencies
import ome.model.IObject;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;
import ome.security.SecuritySystem;

public class EventDiffHolder// FIXME Rename
{

    private static Log log = LogFactory.getLog(EventDiffHolder.class);

    private SecuritySystem secSys;
    
    public EventDiffHolder( SecuritySystem securitySystem )
    {
    	this.secSys = securitySystem;
    }
    
    // TODO http://www.hibernate.org/195.html
    // http://www.jroller.com/page/ksevindik/20050417
    class Events
    {

        public Map<Class, Set<IObject>> inserts = new HashMap<Class, Set<IObject>>();

        public Map<Class, Set<IObject>> updates = new HashMap<Class, Set<IObject>>();

        public Map<Class, Set<IObject>> deletes = new HashMap<Class, Set<IObject>>();
    }

    private ThreadLocal eventSetHolder = new ThreadLocal();

    private Events events()
    {
        Object o = eventSetHolder.get();
        if (o == null)
        {
            o = new Events();
            eventSetHolder.set(o);
        }
        return (Events) o;
    }

    public void reset()
    {
        eventSetHolder.remove();
    }

    public void addInserted(Object entity, Serializable id)
    {
        add(events().inserts, entity, id);
        doIt("INSERT",entity,id);
    }

    public void addUpdated(Object entity, Serializable id)
    {
        add(events().updates, entity, id);
        doIt("UPDATE",entity,id);
    }

    public void addDeleted(Object entity, Serializable id)
    {
        add(events().deletes, entity, id);
        doIt("DELETE",entity,id);
    }

    // Method New
    public void doIt(String action, Object entity, Serializable id)
    {
        // perform inserting audit logs for entities those were enlisted in
        // inserts, updates, and deletes sets...

        secSys.addLog(action,entity.getClass(),(Long)id);
        
    }

    // Method Old
    private void add(Map<Class, Set<IObject>> m, Object entity, Serializable id)
    {
        if (entity instanceof IObject && !(entity instanceof Event /*
                                                                     * FIXME
                                                                     * obtuse
                                                                     */
                || entity instanceof EventDiff || entity instanceof EventLog))
        {
            IObject ome = (IObject) entity;
            Class c = ome.getClass();
            Object key = m.get(c);
            if (null == key) m.put(c, new HashSet<IObject>());

            if (ome.getId() == null)
                throw new RuntimeException("null id on " + ome);
            m.get(c).add(ome);

        }
    }

}

/*
 * ome.security.CurrentDetails
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

package ome.security;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.api.IUpdate;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/** Stores information related to the security context of the current thread.
 * Code calling into the server must setup CurrentDetails properly. An existing
 * user must be set (the creation of a new user is only allowed if the current
 * user is set to root; root always exists. QED.) The event must also be set.
 * Umask is optional.
 * 
 * This information is stored in a Details object, but unlike Details which 
 * assumes that an empty value signifies increased security levels, empty values
 * here signifiy reduced security levels. E.g.,
 * 
 * Details:         user == null ==> object belongs to root
 * CurrentDetails:  user == null ==> current user is "nobody" (anonymous)
 * 
 */
public abstract class CurrentDetails
{
    private static Log log = LogFactory.getLog(CurrentDetails.class);
    
    private static ThreadLocal<Details> detailsHolder = 
        new ThreadLocal<Details>();

    private static ThreadLocal<Permissions> umaskHolder = 
        new ThreadLocal<Permissions>();
    
    // ~ Internals
    // ================================================================

    protected static void setDetails(Details details)
    {
        detailsHolder.set(details);
    }

    protected static Details getDetails()
    {
        Details details = detailsHolder.get();
        if (details == null)
        {
            details = new Details();
            setDetails(details);
        }
        return details;
    }

    // ~ Main methods
    // =================================================================
    public static void newEvent(EventType type) // TODO keep up with stack here?
    {
        Event e = new Event();
        e.setType(type);
        e.setTime(new Timestamp(System.currentTimeMillis()));
        e.setLogs(new HashSet());
        setCreationEvent(e);
    }
    
    public static void addLog(String action, String klass, Long id)
    {

        if (Event.class.getName() != klass 
                && EventLog.class.getName() != klass
                && EventDiff.class.getName() != klass)
        {
        
            log.info("Adding log:"+action+","+klass+","+id);
            
            EventLog l = new EventLog();
            l.setAction(action);
            l.setType(klass); // TODO could be id to Type entity
            l.setIdList(id.toString());
            l.setDetails(createDetails());
            l.setEvent(CurrentDetails.getCreationEvent()); // FIXME needed?
            //FIXME refactor to CurrentDetails
    
            getCreationEvent().getLogs().add(l);
        }
    }
    
    public static Details createDetails()
    {
        Details d = new Details();
        d.setCreationEvent(getCreationEvent());
        d.setOwner(getOwner());
        d.setGroup(getGroup());
        d.setPermissions(getUmask());
        return d;
    }
    
    // ~ Umask
    // =================================================================
    public static Permissions getUmask()
    {
        Permissions umask = umaskHolder.get();
        if (umask == null)
        {
            umask = new Permissions();
            setUmask(umask);
        }
        return umask; 
        /* FIXME
         * getOwner().getProfile().getUmask
         * object.getDetails().getUmask()
         * CurrentDetails.getDetails().getUmask();
         */
    }
    
    public static void setUmask(Permissions umask)
    {
        umaskHolder.set(umask);
    }
    
    // ~ Delegation FIXME possibly remove setters for set(Exp,Grp)
    // =================================================================
    
    public static Event getCreationEvent()
    {
        return getDetails().getCreationEvent();
    }

    public static Experimenter getOwner()
    {
        return getDetails().getOwner();
    }

    public static Permissions getPermissions()
    {
        return getDetails().getPermissions();
    }

    public static Event getUpdateEvent()
    {
        return getDetails().getUpdateEvent();
    }

    public static void setCreationEvent(Event e)
    {
        getDetails().setCreationEvent(e);
    }

    public static void setOwner(Experimenter exp)
    {
        getDetails().setOwner(exp);
    }

    public static void setPermissions(Permissions perms)
    {
        getDetails().setPermissions(perms);
    }

    // TODO hide these specifics. possibly also Owner->User & CreationEvent -> Event
    public static void setUpdateEvent(Event e)
    {
        getDetails().setUpdateEvent(e);
    }
    
    public static ExperimenterGroup getGroup()
    {
        return getDetails().getGroup();
    }
    
    public static void setGroup(ExperimenterGroup group)
    {
        getDetails().setGroup(group);
    }
    
}

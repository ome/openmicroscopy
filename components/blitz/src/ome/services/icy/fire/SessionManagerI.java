/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.icy.fire;

import java.util.List;
import java.util.Map;

import ome.conditions.InternalException;
import ome.logic.HardWiredInterceptor;
import ome.security.SecuritySystem;
import ome.services.icy.util.CreateSessionMessage;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.util.messages.MessageException;
import omero.constants.EVENT;
import omero.constants.GROUP;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import Glacier2.CannotCreateSessionException;

public final class SessionManagerI extends Glacier2._SessionManagerDisp
        implements ApplicationContextAware {

    private final static List<HardWiredInterceptor> CPTORS = HardWiredInterceptor
            .parse(new String[] { 
            BasicSecurityWiring.class.getName()
            /* @REPLACE@ */});

    private final static Log log = LogFactory.getLog(SessionManagerI.class);

    protected OmeroContext context;

    protected SecuritySystem securitySystem;

    public SessionManagerI(SecuritySystem secSys) {
        this.securitySystem = secSys;
        for (HardWiredInterceptor hwi : CPTORS) {
            // HACK
            if (hwi instanceof BasicSecurityWiring) {
                ((BasicSecurityWiring)hwi).setSecuritySystem(secSys);
            }
        }
        
    }
    
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = (OmeroContext) applicationContext;
    }

    public Glacier2.SessionPrx create(String userId,
            Glacier2.SessionControlPrx control, Ice.Current current) 
        throws CannotCreateSessionException {

        Roles roles = securitySystem.getSecurityRoles();
        
        String group = getGroup(current);
        if (group == null) {
            group = roles.getUserGroupName();
        }
        String event = getEvent(current);
        if (event == null) {
            event = "User"; // FIXME This should be in Roles as well.
        }

        Principal principal = new Principal(userId, group, event);
        Session session = (Session) context.getBean("Session");
        session.setPrincipal(principal);
        session.setInterceptors(CPTORS);

        Ice.Identity id = new Ice.Identity();
        id.category = "";
        id.name = Ice.Util.generateUUID();
        Ice.ObjectPrx _prx = current.adapter.add(session,id);
        Glacier2.SessionPrx prx = Glacier2.SessionPrxHelper.uncheckedCast(_prx);
        
        if (log.isDebugEnabled()) {
            log.debug(String.format("Created session %s for user %s", session,
                    userId));
        }

        CreateSessionMessage msg = new CreateSessionMessage(this,id.name,principal);
        try {
            context.publishMessage(msg);
        } catch (CannotCreateSessionException ccse) {
            throw ccse;
        } catch (Throwable t) {
            // FIXME this copying should be a part of ome.conditions.*
            InternalException ie = new InternalException(t.getMessage());
            ie.setStackTrace(t.getStackTrace());
            throw ie;
        }
        return prx;
    }
    
    protected String getGroup(Ice.Current current) {
        if (current.ctx == null) return null;
        return current.ctx.get(GROUP.value);
    }
    
    protected String getEvent(Ice.Current current) {
        if (current.ctx == null) return null;
        return current.ctx.get(EVENT.value);
    }
    
}

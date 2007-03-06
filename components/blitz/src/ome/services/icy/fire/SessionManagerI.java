/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.icy.fire;

import java.util.List;

import ome.logic.HardWiredInterceptor;
import ome.security.SecuritySystem;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import omero.constants.EVENT;
import omero.constants.GROUP;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
            Glacier2.SessionControlPrx control, Ice.Current current) {

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

        if (log.isDebugEnabled()) {
            log.debug(String.format("Created session %s for user %s", session,
                    userId));
        }

        return Glacier2.SessionPrxHelper.uncheckedCast(current.adapter
                .addWithUUID(session));
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
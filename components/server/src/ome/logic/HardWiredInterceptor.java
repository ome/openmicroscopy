/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

// Java imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.SessionContext;

// Third-party imports
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

/**
 * Extension point for interceptors which should be compiled in from 
 * third-party code. Subclasses can be added to the 
 * {@link ome.logic.AbstractBean} class via server/build.xml and the
 * omero.hard-wired.interceptors property value. All subclasses must
 * have a no-arg constructor, but can assess various environment 
 * variables via the getters defined on this class.
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-RC1
 */
@RevisionDate("$Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1167 $")
public abstract class HardWiredInterceptor implements MethodInterceptor {

    /** Unique string for the current ServiceFactory instance */
    private final static String SF = "ome.hard-wired.service-factory";

    /** Unique string for the current {@link Principal} instance */
    private final static String PR = "ome.hard-wired.principal";
    
    public static void configure(List<HardWiredInterceptor> hwi, OmeroContext ctx) {
        for (HardWiredInterceptor interceptor : hwi) {
            interceptor.selfConfigure(ctx);
        }
    }
    
    /**
     * Can be implemented by all subclasses, so that they can configure themselves
     * in {@link #selfConfigure(OmeroContext)}. If the method returns null,
     * {@link #selfConfigure(OmeroContext)} will not run.
     */
    public String getName() {
        return null;
    }
    
    /**
     * Calls {@link OmeroContext#applyBeanPropertyValues(Object, String)} to 
     * have properties injected.
     */
    public void selfConfigure(OmeroContext context) {
        String name = getName();
        if (name != null) {
            context.applyBeanPropertyValues(this, getName());
        }
    }
    
    /** 
     * Produces a {@link List} of instantiated interceptors from
     * a list of {@link HardWiredInterceptor} subclass names.
     */
    public static List<HardWiredInterceptor> parse(String[] classNames) {

        List<HardWiredInterceptor> cptors = new ArrayList<HardWiredInterceptor>();

        for (int i = 0; i < classNames.length; i++) {
            try {
                Class klass = Class.forName(classNames[i]);
                cptors.add((HardWiredInterceptor) klass.newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate hard-wired "
                        + "interceptors:" + classNames[i], e);
            }
        }
        return Collections.unmodifiableList(cptors);
    }

    /** 
     * Adds the given environment context objects to the 
     * {@link ReflectiveMethodInvocation#getUserAttributes()} 
     * {@link java.util.Map} for lookup in subclasses
     */
    public static void initializeUserAttributes(MethodInvocation mi,
            ServiceFactory sf, Principal pr) {
        ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) mi;
        rmi.getUserAttributes().put(SF, sf);
        rmi.getUserAttributes().put(PR, pr);
    }

    protected ServiceFactory getServiceFactory(MethodInvocation mi) {
        ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) mi;
        return (ServiceFactory) rmi.getUserAttribute(SF);
    }

    protected Principal getPrincipal(MethodInvocation mi) {
        ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) mi;
        return (Principal) rmi.getUserAttribute(PR);
    }

}

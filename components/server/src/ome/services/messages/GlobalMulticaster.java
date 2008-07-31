/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.messages;

import ome.system.OmeroContext;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

/**
 * Global {@link ApplicationEventMulticaster} which can be used to integrate
 * parent and child {@link OmeroContext} instances.
 * 
 * @see ome.system.OmeroContext
 */
public class GlobalMulticaster implements ApplicationEventMulticaster {

    private static int TOTAL = 0;

    private final static SimpleApplicationEventMulticaster _em = new SimpleApplicationEventMulticaster();

    private final int index;

    private final Object lock = new Object();

    /**
     * Keeps track of which instance this is. Only the first instance will
     * actively call {@link #multicastEvent(ApplicationEvent)}, but all
     * instances cann add to the static list of
     * {@link ApplicationListener listeners}.
     */
    public GlobalMulticaster() {
        synchronized (lock) {
            TOTAL++;
            index = TOTAL;
        }
    }

    public void addApplicationListener(ApplicationListener arg0) {
        _em.addApplicationListener(arg0);
    }

    /**
     * Multicast only if this instance was the first created.
     */
    public void multicastEvent(ApplicationEvent arg0) {
        if (index == 1) {
            _em.multicastEvent(arg0);
        }
    }

    public void removeAllListeners() {
        _em.removeAllListeners();
    }

    public void removeApplicationListener(ApplicationListener arg0) {
        _em.removeApplicationListener(arg0);
    }

}
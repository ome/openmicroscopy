/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */


package ome.services.blitz.impl;

import java.util.NoSuchElementException;

import ome.api.ServiceInterface;
import ome.api.StatefulServiceInterface;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.UnregisterServantMessage;
import ome.util.messages.InternalMessage;
import omero.ShutdownInProgress;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.util.CloseableServant;
import omero.util.IceMapper;
import Ice.ObjectAdapterDeactivatedException;

/**
 * Base class for all servants that must guarantee proper clean-up of held
 * resources on close.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4.9
 */
public abstract class AbstractCloseableAmdServant extends AbstractAmdServant
    implements CloseableServant {

    public AbstractCloseableAmdServant(ServiceInterface service, BlitzExecutor be) {
        super(service, be);
    }

    public final void close(Ice.Current __current) {
        final RuntimeException[] re = new RuntimeException[1];
        AMD_StatefulServiceInterface_close cb =
            new AMD_StatefulServiceInterface_close() {
            public void ice_exception(Exception ex) {
                if (ex instanceof RuntimeException) {
                    re[0] = (RuntimeException) ex;
                } else {
                    re[0] = new RuntimeException(ex);
                }
            }
            public void ice_response() {
                // ok.
            }
        };
        close_async(cb, __current);
        if (re[0] != null) {
            throw re[0];
        }
    }

    /**
     * {@link ome.tools.hibernate.SessionHandler} also
     * specially catches close() calls, but cannot remove the servant
     * from the {@link Ice.ObjectAdapter} and thereby prevent any
     * further communication. Once the invocation is finished, though,
     * it is possible to raise the message and have the servant
     * cleaned up.
     * 
     * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/1855">ticket:1855</a>
     */
    public final void close_async(AMD_StatefulServiceInterface_close __cb,
            Ice.Current __current) {

        Throwable t = null;

        // First we call close on the object
        try {
            preClose(__current);
            if (service instanceof StatefulServiceInterface) {
                StatefulServiceInterface ss = (StatefulServiceInterface) service;
                ss.close();
            }
        } catch (NoSuchElementException nsee) {
            log.info("NoSuchElementException: Login is already gone");
            t = nsee;
        } catch (Throwable t1) {
            log.error("Error on close, stage1", t1);
            t = t1;
        }

        // Then we publish the close event
        try {
            InternalMessage msg = new UnregisterServantMessage(this, __current, holder);
            ctx.publishMessage(msg);
        } catch (ObjectAdapterDeactivatedException oade) {
            log.warn("ObjectAdapter deactivated!");
            ShutdownInProgress sip = new ShutdownInProgress();
            IceMapper.fillServerError(sip, oade);
            t = sip;
        } catch (Throwable t2) {
            log.error("Error on close, stage2", t2);
            t = t2;
        }

        // Now we've finished that, let's return control to the user.
        try {
            if (t == null) {
                __cb.ice_response();
            } else {
                __cb.ice_exception(new IceMapper().handleException(t, ctx));
            }
        } finally {
            postClose(__current);
        }
    }

    protected abstract void preClose(Ice.Current current) throws Throwable;

    /**
     * Should not throw any exceptions which should be detected by clients
     * since it is called in a finally block after the client thread has been
     * released.
     */
    protected abstract void postClose(Ice.Current current);

}

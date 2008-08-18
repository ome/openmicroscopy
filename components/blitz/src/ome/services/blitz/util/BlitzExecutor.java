/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.blitz.util;

import ome.api.ServiceInterface;

/**
 * Single-point of execution for all AMD blitz calls.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public interface BlitzExecutor {

    public interface Task {
        void run();
    }

    void serviceInterfaceCall(ServiceInterface service,
            IceMethodInvoker invoker, ServantHelper helper, Object __cb,
            Ice.Current __current, Object... args);

    void runnableCall(Ice.Current __current, Task task);

}

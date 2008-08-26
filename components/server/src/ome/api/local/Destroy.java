/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api.local;

import javax.annotation.PreDestroy;

/**
 * The JBoss services all contain a "destroy" method marked with
 * {@link PreDestroy} which are used to clean up state. To make calling this
 * method from OmeroBlitz simpler, we are adding a otherwise useless interface
 * Destroy so that the method can be called via reflection on our proxies. If
 * this is not in place, the following is thrown:
 * 
 * <pre>
 * java.lang.NoSuchMethodException: $Proxy69.destroy()
 *   at java.lang.Class.getMethod(Class.java:1581)
 *   at ome.services.blitz.util.IceMethodInvoker.callOrClose(IceMethodInvoker.java:264)
 *   at ome.services.blitz.util.IceMethodInvoker.invoke(IceMethodInvoker.java:178)
 *   at ome.services.throttling.Callback.run(Callback.java:68)
 * </pre>
 */
public interface Destroy {
    void destroy();
}
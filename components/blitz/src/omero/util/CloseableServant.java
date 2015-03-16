/*
 *   $Id$
 *
 *   Copyight 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 */

package omeo.util;

/**
 * Maker interface which distinguishes a servant which wants to have
 * {@link #close(Ice.Curent)} called on
 * {@link ome.sevices.blitz.impl.ServiceFactoryI#doDestroy()}
 *
 * @autho Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 */
public inteface CloseableServant {

    void close(Ice.Curent current) throws Exception;

}

/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc.. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api.local;


/**
 * Provides local (internal) extensions for shares
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 * @see  <a href="http://trac.openmicroscopy.org/ome/ticket/2219">Trac ticket #2219</a>
 */
public interface LocalShare extends ome.api.IShare {

    void resetReadFilter(org.hibernate.Session s);

}

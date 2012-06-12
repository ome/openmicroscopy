/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.util;

import omero.ServerError;

/**
 * Servant which is aware of the {@link Ice.TieBase}-instance which it
 * belongs to and will have it injected on instantiation.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 */
public interface TieAware {

    void setTie(Ice.TieBase tie) throws ServerError;

}

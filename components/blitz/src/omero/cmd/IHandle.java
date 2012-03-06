/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
package omero.cmd;

import omero.util.CloseableServant;

/**
 * SPIOrthogonal interface hierarchy of types for working with the
 * {@link omero.cmd.Request} hierarchy.
 *
 * @since Beta4.3.2
 */
public interface IHandle extends Runnable, CloseableServant {

    void initialize(Ice.Identity id, IRequest req);

}

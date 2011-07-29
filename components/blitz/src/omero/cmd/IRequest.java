/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
package omero.cmd;

/**
 * SPIOrthogonal interface hierarchy of types for working with the
 * {@link omero.cmd.Request} hierarchy.
 *
 * @since Beta4.3.2
 */
public interface IRequest {

    /**
     * Implementations must properly initialize the "step" field of the
     * {@link Status} object.
     *
     * @param status
     */
    void init(Status status);

    void step(int i);

    Response finish();

}

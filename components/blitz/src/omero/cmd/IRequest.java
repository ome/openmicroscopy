/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */
package omero.cmd;

import java.util.Map;

import org.hibernate.Session;

import ome.system.ServiceFactory;
import ome.util.SqlAction;

import omero.cmd.HandleI.Cancel;

/**
 * SPIOrthogonal interface hierarchy of types for working with the
 * {@link omero.cmd.Request} hierarchy.
 *
 * @since Beta4.3.2
 */
public interface IRequest {

    /**
     * Returns the desired call context for this request. Some request
     * implementations will require "omero.group":"-1" for example and will
     * hard-code that value. Others may permit users to pass in the desired
     * values which will be merged into the static {@link Map} as desired.
     */
    Map<String, String> getCallContext();

    /**
     * Implementations must properly initialize the "step" field of the
     * {@link Status} object.
     *
     * @param status
     */
    void init(Status status, SqlAction sql, Session session, ServiceFactory sf) throws Cancel;

    void step(int i) throws Cancel;

    void finish() throws Cancel;

    /**
     * Returns the current response value. This method should be protected
     * by synchronization where necessary, and should never an exception.
     */
    Response getResponse();

}

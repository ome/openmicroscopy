/*
 * Copyright 2011-2012 Glencoe Software, Inc. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package omero.cmd;

import java.util.Map;

import org.hibernate.Session;

import ome.system.ServiceFactory;
import ome.util.SqlAction;

import omero.cmd.HandleI.Cancel;

/**
 * SPI Orthogonal interface hierarchy of types for working with the
 * {@link omero.cmd.Request} hierarchy. All request implementations
 * handled by the server <em>must</em> also be instances of {@link IRequest},
 * which defines the lifecycle methods needed for processing.
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
     * Method called within the thread boundaries before any processing occurs.
     * 
     * Implementations must properly initialize the "step" field of the
     * {@link Status} object. This count will define how many times the
     * {@link #step(int)} method will be called.
     * 
     * The arguments passed in are those needed by IRequests to interact with
     * data and should be stored for later use. Most implementations will pass
     * these arguments to
     * {@link Helper#Helper(Request, Status, SqlAction, Session, ServiceFactory)}
     * for thread-safe storage and accesss.
     */
    void init(Status status, SqlAction sql, Session session, ServiceFactory sf)
            throws Cancel;

    /**
     * Single uncancellable action which will be performed by this IRequest.
     * 
     * The return value can be an ome.model object that is attached to the
     * current thread and transaction. After processing and detachment from
     * the transaction, the object will be passed to
     * {@link #buildResponse(int, Object)} for conversion and storage.
     * 
     * @param i
     * @return
     * @throws Cancel
     */
    Object step(int step) throws Cancel;

    /**
     * Post-transaction chance to map from the return value of
     * {@link #step(int)} to a {@link omero.cmd.Respnse} object.
     * 
     * @param i
     * @param object
     */
    void buildResponse(int step, Object object);

    /**
     * Returns the current response value. This method should be protected
     * by synchronization where necessary, and should never raise an exception.
     */
    Response getResponse();

}

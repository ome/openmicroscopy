/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.Map;

import ome.api.IDelete;

import org.hibernate.Session;

/**
 * Specification of a delete operation. These instances are defined in
 * ome/services/delete/spec.xml as non-singletons, i.e each time a request is
 * made for a new {@link DeleteSpecFactory} one of each {@link DeleteSpec} is
 * initalized and gathered into the factory. A single thread, then, can
 * repeatedly call {@link #initialize(long, Map)} on the {@link DeleteSpec}
 * instances.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public interface DeleteSpec {

    /**
     * The name of this specification. Usually the first component of the
     * entries in spec.xml
     */
    String getName();

    /**
     * Gives all specs a chance to reference subspecs.
     */
    void postProcess(Map<String, DeleteSpec> speces);

    /**
     * Called as each delete command is started. This instance will inly be used
     * serially (i.e. by one thread) and so this is safe. When the last step
     * is reached, {@link #delete(Session, int)} can take clean up actions.
     *
     * @param id
     *            identifer of the root object which defines the graph to be
     *            deleted.
     * @param supersec
     *            points to the relationship between the root object and the
     *            current graph. In many cases, this value will be null so
     *            that the current object is taken to be the root, but if this
     *            is a subspec, or a non-standard naming is being used, then
     *            the supersec will be used.
     * @param options
     *            possibly null or empty map of options which can override the
     *            operations provided in the definition of the specification.
     *            For example, if the spec "/Image" defines "/Image/Annotation"
     *            as "HARD" (the default), then the options map could contain
     *
     *            <pre>
     * {"/Image/Annotation":"ORPHAN"}
     * </pre>
     *
     *            to modify that setting.
     * @return number of steps which are to be processed.
     */
    int initialize(long id, String supersec, Map<String, String> options) throws DeleteException;

    /**
     *
     * @param session
     *            non-null, active Hibernate session that will be used to delete
     *            all necessary items.
     * @param step
     *            which step is to be invoked. Running a step multiple times is
     *            not supported.
     *
     * @return Any warnings which were noted during execution.
     * @throws DeleteException
     *             Any errors which were caused during execution. Which
     *             execution states may be encountered is strongly tied to the
     *             definition of the specification and to the options which are
     *             passed in.
     */
    String delete(Session session, int step) throws DeleteException;

}

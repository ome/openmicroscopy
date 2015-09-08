/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.security.basic.OmeroInterceptor;
import ome.system.EventContext;

/**
 * Base filter interface ...
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.4
 * @see <a
 *      href="http://trac.openmicroscopy.org.uk/ome/ticket/117">ticket117</a>
 * @see <a
 *      href="http://trac.openmicroscopy.org.uk/ome/ticket/1154">ticket1154</a>
 * @see <a
 *      href="http://trac.openmicroscopy.org.uk/ome/ticket/3529">ticket3529</a>
 */
public interface SecurityFilter {

    static public final String is_share = "is_share";

    static public final String is_adminorpi = "is_adminorpi";

    static public final String is_nonprivate = "is_nonprivate";

    static public final String current_user = "current_user";

    /**
     * Name of this security filter. By default this will likely return
     * the simple class name for the instance. This value will be used
     * to activate the filter on the Hibernate session.
     */
    public String getName();

    /**
     * Return a mapping of the hibernate types for each of the parameters
     * that the condition takes.
     */
    public Map<String, String> getParameterTypes();

    /**
     * Return the string to be used as the condition.
     */
    public String getDefaultCondition();

    /**
     * tests that the {@link Details} argument passes the security test that
     * this filter defines. The two must be kept in sync. This will be used
     * mostly by the
     * {@link OmeroInterceptor#onLoad(Object, java.io.Serializable, Object[], String[], org.hibernate.type.Type[])}
     * method.
     *
     * @param d
     *            Details instance. If null (or if its {@link Permissions} are
     *            null all {@link Right rights} will be assumed.
     * @return true if the object to which this
     */
    public boolean passesFilter(Session session, Details d, EventContext c);

    /**
     * Enables this filter with the settings from this filter. The intent is
     * that after this call, no Hibernate queries will return any objects that
     * would fail a call to
     * {@link #passesFilter(Session, Details, EventContext)}.
     *
     * @param sess Non-null.
     * @param ec Non-null.
     */
    public void enable(Session sess, EventContext ec);

    /**
     * Reverts the call to {@link #enable(Session, EventContext)}.
     */
    public void disable(Session sess);

}

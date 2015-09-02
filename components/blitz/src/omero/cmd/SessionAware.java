/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.cmd;

import omero.ServerError;

/**
 * Servant which is aware of the {@link SessionI}-instance which it
 * belongs to and will have it injected on instantiation. By definition, such
 * servants should be stateful and have "singleton=true" in the Spring
 * configuration.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.2
 */
public interface SessionAware {

    void setSession(SessionI session) throws ServerError;

}

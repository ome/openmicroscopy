/*
 *   $Id$
 *
 *   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.SecuritySystem;

/**
 * Provides {@link Experimenter user} and {@link ExperimenterGroup group}
 * creation, deletion, and modification for use by services. All invocations are
 * assumed "trusted" (services are responsible for authorization, and will take
 * part in the current Hibernate {@link org.hibernate.Session session}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see ome.api.IAdmin
 * @see SecuritySystem
 * @since 4.0
 */
public interface RoleProvider {

    String nameById(long id);

    long createGroup(ExperimenterGroup group);

    long createGroup(String name, Permissions perms, boolean strict);

    long createGroup(String name, Permissions perms, boolean strict,
            boolean isLdap);

    long createExperimenter(Experimenter experimenter,
            ExperimenterGroup defaultGroup, ExperimenterGroup... otherGroups);

    void setDefaultGroup(final Experimenter user, final ExperimenterGroup group);

    void setGroupOwner(final Experimenter user, final ExperimenterGroup group,
            final boolean value);

    void addGroups(final Experimenter user, final ExperimenterGroup... groups);

    void removeGroups(final Experimenter user,
            final ExperimenterGroup... groups);

    boolean isIgnoreCaseLookup();
}

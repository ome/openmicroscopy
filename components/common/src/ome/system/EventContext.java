/*
 * ome.system.EventContext
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.util.List;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.model.internal.Permissions;

/**
 * manages authenticated principals and other context for a given event. Just as
 * all API method calls take place in a transaction and a session (in that
 * order), they also take place within an Event.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see ome.model.meta.Experimenter
 * @see ome.model.meta.ExperimenterGroup
 * @since 3.0
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public interface EventContext {

    Long getCurrentShareId();

    Long getCurrentSessionId();

    String getCurrentSessionUuid();

    Long getCurrentUserId();

    String getCurrentUserName();

    Long getCurrentGroupId();

    String getCurrentGroupName();

    boolean isCurrentUserAdmin();

    boolean isReadOnly();

    Long getCurrentEventId();

    String getCurrentEventType();

    List<Long> getMemberOfGroupsList();

    List<Long> getLeaderOfGroupsList();

    Permissions getCurrentGroupPermissions();

}

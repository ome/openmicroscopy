/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions.events;

import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.security.SecuritySystem;
import ome.services.sessions.SessionManager;
import ome.util.messages.InternalMessage;

/**
 * {@link InternalMessage} published by the {@link SecuritySystem} when an
 * {@link Experimenter}, {@link ExperimenterGroup}, or
 * {@link GroupExperimenterMap} is inserted or updated.
 * 
 * This signals the {@link SessionManager} to update its cache.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class UserGroupUpdateEvent extends InternalMessage {

    public UserGroupUpdateEvent(Object source) {
        super(source);
    }

    private static final long serialVersionUID = 1L;

}

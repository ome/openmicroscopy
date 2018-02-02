/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import ome.model.meta.Session;
import org.springframework.context.ApplicationListener;

/**
 * Is for ISession a cache and will be kept there in sync? OR Factors out the
 * logic from ISession and SessionManagerI
 *
 * Therefore either called directly, or via synchronous messages.
 *
 * Uses the name of a Principal as the key to the session. We may need to limit
 * user names to prevent this. (Strictly alphanumeric)
 *
 * Receives notifications as an {@link ApplicationListener}, which should be
 * used to keep the {@link Session} instances up-to-date.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @deprecated superseded by {@link SessionManagerInDb} but temporarily provided for compatibility
 */
@Deprecated
public class SessionManagerImpl extends SessionManagerInDb { }
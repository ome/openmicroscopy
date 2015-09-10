/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions.events;

import java.util.ArrayList;
import java.util.List;

import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.services.sessions.SessionManager;
import ome.util.messages.InternalMessage;

/**
 * {@link InternalMessage} published by the {@link SessionManager} when
 * setSecurityContext() is called.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 */
public class ChangeSecurityContextEvent extends InternalMessage {

    final List<String> cancellations = new ArrayList<String>();

    final private String sessionUuid;

    final private IObject previous;

    final private IObject next;

    public ChangeSecurityContextEvent(Object source, String sessionUuid,
            IObject previous, IObject next) {
        super(source);
        this.sessionUuid = sessionUuid;
        this.previous = previous;
        this.next = next;
    }

    private static final long serialVersionUID = 1L;

    public synchronized String getUuid() {
        return sessionUuid;
    }

    public synchronized void cancel(String message) {
        cancellations.add(message);
    }

    public synchronized void throwIfCancelled() {
        if (cancellations.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("SecurityContext change cancelled:\n");
            for (String str : cancellations) {
                sb.append(str);
                sb.append("\n");
            }
            throw new SecurityViolation(sb.toString());
        }
    }
}

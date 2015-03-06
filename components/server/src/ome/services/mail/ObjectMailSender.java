/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.listeners;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.HashSet;
import java.util.Set;

import ome.model.IObject;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.services.messages.EventLogMessage;
import ome.system.Roles;

import org.springframework.context.ApplicationListener;

/**
 * When an {@link EventLogMessage} of the specified type and kind is received,
 * an email is sent to all users which are returned by a given query. A number
 * of parameters are made available to the query via a {@link Parameters}
 * instance.
 */
public class ObjectMailSender extends MailSender implements
        ApplicationListener<EventLogMessage> {

    private Class<IObject> klass;

    private String action;

    private String queryString;

    //
    // GETTERS & SETTERS
    //

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Class<IObject> getObjectClass() {
        return klass;
    }

    public void setObjectClass(Class<IObject> klass) {
        this.klass = klass;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    //
    // Main method
    //

    @Override
    public void onApplicationEvent(EventLogMessage elm) {

        if (!elm.entityType.isAssignableFrom(klass)) {
            return;
        }

        if (!isEmpty(this.queryString) && !elm.action.equals(this.action)) {
            return;
        }
        sendEmail(elm);
    }

    //
    // Main method
    //

    protected void sendEmail(EventLogMessage elm) {
        Set<String> addresses = new HashSet<String>();
        Parameters p = new Parameters();
        Roles roles = getRoles();
        p.addString("systemGroup", roles.getSystemGroupName());
        p.addLong("systemGroupId", roles.getSystemGroupId());
        p.addString("userGroup", roles.getUserGroupName());
        p.addLong("userGroupId", roles.getUserGroupId());

        StringBuilder sb = new StringBuilder();
        sb.append("Modified objects:\n");
        for (Long id : elm.entityIds) {
            p.addId(id);
            sb.append(klass);
            sb.append(":");
            sb.append(id);
            sb.append("\n");
            for (IObject obj : getQueryService().findAllByQuery(queryString, p)) {
                if (obj instanceof Experimenter) {
                    addUser(addresses, (Experimenter) obj);
                } else if (obj instanceof ExperimenterGroup) {
                    for (Experimenter exp : ((ExperimenterGroup) obj)
                            .linkedExperimenterList()) {
                        addUser(addresses, exp);
                    }
                }
            }
        }
        sendBlind(addresses, String.format("%s %s notification", elm.action,
                elm.entityType), sb.toString());
    }

    protected void addUser(Set<String> addresses, Experimenter exp) {
        String email = exp.getEmail();
        if (!isEmpty(email)) {
            addresses.add(email);
        }
    }

}
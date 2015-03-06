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

package ome.services.mail;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ome.api.IQuery;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.services.util.Executor;
import ome.system.Roles;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic bean with setter injection that can be used as a base class for other
 * senders.
 */
public class MailSender {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Executor executor;

    private MailUtil util;

    private IQuery query;

    private Roles roles;

    private String subjectPrefix = "[OMERO] ";

    //
    // GETTERS & SETTERS
    //

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public MailUtil getMailUtil() {
        return util;
    }

    public void setMailUtil(MailUtil util) {
        this.util = util;
    }

    public IQuery getQueryService() {
        return query;
    }

    public void setQueryService(IQuery query) {
        this.query = query;
    }

    public Roles getRoles() {
        return roles;
    }

    public void setRoles(Roles roles) {
        this.roles = roles;
    }

    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    public void setSubjectPrefix(String subjectPrefix) {
        this.subjectPrefix = subjectPrefix;
    }

    //
    // Helpers
    //

    protected void sendBlind(Set<String> addresses, String subject, String text) {

        if (addresses == null || addresses.isEmpty()) {
            log.debug("No emails found.");
            return;
        }

        try {
            String from = getMailUtil().getSender();
            // FIXME: Misusing from as the TO for BCCs. If there's no workaround
            // then likely we'll need to break this into multiple sendEmail
            // calls. (multi-threading?)
            getMailUtil().sendEmail(from, getSubjectPrefix() + subject, text,
                false /* not html */, null, new ArrayList<String>(addresses));
        } catch (Exception e) {
            log.error("Failed to send emails: {} ", addresses, e);
        }
    }

    protected void addUser(Set<String> addresses, Experimenter exp) {
        String email = exp.getEmail();
        if (!isEmpty(email)) {
            addresses.add(email);
        }
    }

    protected Set<String> getAllSystemUsers(boolean newTx) {
        Set<String> addresses = new HashSet<String>();
        if (newTx) {
          loadFromAction(addresses);
        } else {
          loadFromQuery(addresses);
        }
        return addresses;
    }

    private void loadFromQuery(Set<String> addresses) {
        for (Object[] o : getQueryService().projection(
                "select e.email from Experimenter e "
                        + "join e.groupExperimenterMap m "
                        + "join m.parent g where g.id = :id",
                new Parameters().addId(getRoles().getSystemGroupId()))) {
            if (o != null && o.length >= 1 && o[0] != null) {
                String email = o[0].toString();
                if (!email.isEmpty()) {
                    addresses.add(email);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromAction(Set<String> addresses) {
        Collection<String> rv = (Collection<String>)
        executor.executeSql(new Executor.SimpleSqlWork(this, "loadAdminEmails") {
            @Override
            @Transactional(readOnly=true)
            public Collection<String> doWork(SqlAction sql) {
                return sql.getUserEmailsByGroup(roles.getSystemGroupId());
            }
        });
        for (String email : rv) {
            if (!email.isEmpty()) {
                addresses.add(email);
            }
        }
    }

}

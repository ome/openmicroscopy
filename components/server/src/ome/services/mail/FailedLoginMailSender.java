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

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.util.Collections;
import java.util.Set;

import ome.services.messages.LoginAttemptMessage;

import org.springframework.context.ApplicationListener;

/**
 * On an {@link LoginAttemptMessage} potentially send an email to administrators
 * or the user herself to alert of possible security issues.
 *
 * @since 5.1.0
 */
public class FailedLoginMailSender extends MailSender implements
        ApplicationListener<LoginAttemptMessage> {

    private boolean contactUser;

    private boolean contactSystem;

    private boolean onAllUsers;

    private Set<String> onSpecificUsers;

    public void setContactUser(boolean contactUser) {
        this.contactUser = contactUser;
    }

    public void setContactSystem(boolean contactSystem) {
        this.contactSystem = contactSystem;
    }

    public void setOnAllUsers(boolean onAllUsers) {
        this.onAllUsers = onAllUsers;
    }

    public void setOnSpecificUsers(Set<String> onSpecificUsers) {
        this.onSpecificUsers = onSpecificUsers;
    }

    //
    // Main method
    //

    @Override
    public void onApplicationEvent(LoginAttemptMessage lam) {

        if (!isEnabled()) {
            return;
        }

        if (lam.success) {
            return;
        }

        final String subj = String.format("Login failed for '%s'", lam.user);
        final boolean hasSpecific = (!isEmpty(onSpecificUsers) &&
                onSpecificUsers.contains(lam.user));

        if (onAllUsers || hasSpecific) {
            if (contactSystem) {
                sendBlind(getAllSystemUsers(true), subj, "body");
            }
            if (contactUser) {
                sendBlind(Collections.singleton(getUserEmail(lam.user)),
                        subj, "body");
            }
        }
    }

}

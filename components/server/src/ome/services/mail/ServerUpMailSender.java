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

import java.util.Set;

import ome.tools.spring.OnContextRefreshedEventListener;

import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Announces to all members of the system group that the server has come up.
 */
public class ServerUpMailSender extends OnContextRefreshedEventListener {

    protected MailSender sender;

    public ServerUpMailSender(MailSender sender) {
        this.sender = sender;
    }

    @Override
    public void handleContextRefreshedEvent(ContextRefreshedEvent event) {
        if (!sender.isEnabled()) {
            return;
        }
        Set<String> addresses = sender.getAllSystemUsers(true);
        sender.sendBlind(addresses, "Server up", "");
    }

}

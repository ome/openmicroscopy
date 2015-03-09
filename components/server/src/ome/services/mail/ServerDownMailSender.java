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

import ome.util.messages.ShutdownMessage;

import org.springframework.context.ApplicationListener;

/**
 * Send an email to all members of the system group when the server is
 * shutting down.
 */
public class ServerDownMailSender extends MailSender implements
        ApplicationListener<ShutdownMessage> {

    @Override
    public void onApplicationEvent(ShutdownMessage sm) {
        if (!isEnabled()) {
            return;
        }
        Set<String> addresses = getAllSystemUsers(true);
        sendBlind(addresses, "Server shutdown", "body");
    }



}

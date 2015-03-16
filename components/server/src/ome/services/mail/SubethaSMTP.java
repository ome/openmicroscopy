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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

/**
 * Simple server bean which can be started on Spring context creation
 * and shutdown when the application is editing. The
 * {@link MessageHandlerFactory} implementation which is passed to the server
 * is critical in deciding what will happen with the email.
 *
 * @since 5.1.0
 */

public class SubethaSMTP {

    public final static int DEFAULT_PORT = 2525;

    private final static Logger log = LoggerFactory.getLogger(SubethaSMTP.class);

    private final SMTPServer smtpServer;

    public SubethaSMTP(MessageHandlerFactory factory) {
        this(factory, DEFAULT_PORT);
    }
    
    public SubethaSMTP(MessageHandlerFactory factory, int port) {
        this.smtpServer = new SMTPServer(factory);
        smtpServer.setPort(port);
    }

    public void start() {
        smtpServer.start();
        log.info("SMTP started");
    }
    
    public void stop() {
        smtpServer.stop();
        log.info("SMTP stopped");
    }
}
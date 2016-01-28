/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
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
package ome.services.messages;

import java.util.Map;

import ome.util.messages.InternalMessage;

public class ContextMessage extends InternalMessage {

    private static final long serialVersionUID = -1L;

    public final Map<String, String> context;

    public ContextMessage(Object source, Map<String, String> context) {
        super(source);
        this.context = context;
    }

    /**
     * Published when an internal service would like to
     * modify the current {@link ome.system.EventContext}.
     * A {@link ContextMessage.Pop PopContextMessage} should be used once
     * the temporary login is complete.
     */
    public static class Push extends ContextMessage {

        private static final long serialVersionUID = -1L;

        public Push(Object source, Map<String, String> context) {
            super(source, context);
        }

    }

    /**
     * Published when an internal service is finished with
     * the context previously signaled through publishing
     * a {@link Push}.
     */
    public static class Pop extends ContextMessage {

        private static final long serialVersionUID = -1L;

        public Pop(Object source, Map<String, String> context) {
            super(source, context);
        }

    }
}

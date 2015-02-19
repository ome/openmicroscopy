/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import ome.util.messages.InternalMessage;

public class DeleteLogsMessage extends InternalMessage {

    private static final long serialVersionUID = 2948752938475908723L;

    private final List<DeleteLogMessage> messages = new ArrayList<DeleteLogMessage>();

    public DeleteLogsMessage(Object source, List<Long> fileIds) {
        super(source);
        for (Long fileId : fileIds) {
            messages.add(new DeleteLogMessage(source, fileId));
        }
    }

    public List<DeleteLogMessage> getMessages() {
        return messages;
    }

}

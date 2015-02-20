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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.util.SqlAction.DeleteLog;
import ome.util.messages.InternalMessage;

public class DeleteLogMessage extends InternalMessage {

    private static final long serialVersionUID = 1946424150689223625L;

    private final long fileId;

    private final List<DeleteLog> successes = new ArrayList<DeleteLog>();

    private final Map<DeleteLog, Throwable> errors = new HashMap<DeleteLog, Throwable>();

    public DeleteLogMessage(Object source, long fileId) {
        super(source);
        this.fileId = fileId;
    }

    public long getFileId() {
        return fileId;
    }

    public int count() {
        return successes.size() + errors.size();
    }

    public void success(DeleteLog log) {
        successes.add(log);
    }

    public void error(DeleteLog log, Throwable t) {
        errors.put(log, t);
    }

    public boolean isError(DeleteLog log) {
        return errors.containsKey(log);
    }

}

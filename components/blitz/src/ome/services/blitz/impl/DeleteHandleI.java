/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import omero.ServerError;
import omero.api.delete.DeleteCommand;
import omero.api.delete._DeleteHandleDisp;
import Ice.Current;

/**
 * Return proxy from the IDelete service.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.2.1
 * @see ome.api.IDelete
 */
public class DeleteHandleI extends _DeleteHandleDisp {

    private final DeleteCommand[] commands;

    private boolean finished = false;

    public DeleteHandleI(final DeleteCommand[] commands) {
        if (commands == null || commands.length == 0) {
            this.commands = new DeleteCommand[0];
            this.finished = true;
        } else {
            this.commands = new DeleteCommand[commands.length];
            System.arraycopy(commands, 0, this.commands, 0, commands.length);
        }
    }

    public DeleteCommand[] commands(Current __current) throws ServerError {
        return commands;
    }

    public boolean finished(Current __current) throws ServerError {
        return finished;
    }

    public int errors(Current __current) throws ServerError {
        return 0;
    }

    public List<String> report(Current __current) throws ServerError {
        return null;
    }

}

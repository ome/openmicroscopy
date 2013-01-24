/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import Ice.Current;

import ome.io.nio.AbstractFileSystemService;
import ome.services.delete.DeleteStepFactory;
import ome.services.delete.Deletion;

import omero.ServerError;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteReport;
import omero.api.delete._DeleteHandleOperations;
import omero.cmd.HandleI;
import omero.cmd.basic.DoAllI;
import omero.cmd.graphs.DeleteI;

/**
 * Wraps a {@link HandleI} instance (as of 4.4.0).
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.2.1
 * @see ome.api.IDelete
 */
public class DeleteHandleI extends AbstractAmdServant implements
        _DeleteHandleOperations, Runnable {

    private static final long serialVersionUID = 159204352095939345L;

    private static final Log log = LogFactory.getLog(DeleteHandleI.class);

    /**
     * {@link DeleteCommand} instances passed into this instance on creation. No
     * methods will modify this array, but they may be returned to the client.
     */
    private final DeleteCommand[] commands;

    private final DeleteI[] deletions;

    private final HandleI delegate;

    /**
     * Call the main constructor with a null call context.
     */
    public DeleteHandleI(final ApplicationContext ctx, final Ice.Identity id, final ServiceFactoryI sf,
            final AbstractFileSystemService afs, final DeleteCommand[] commands, int cancelTimeoutMs)
            throws ServerError {
        this(ctx, id, sf, afs, commands, cancelTimeoutMs, null);
    }

    /**
     * Main constructor.
     *
     * @param id
     * @param sf
     * @param factory
     * @param commands
     * @param cancelTimeoutMs
     * @param callContext
     */
    public DeleteHandleI(final ApplicationContext ctx, final Ice.Identity id, final ServiceFactoryI sf,
            final AbstractFileSystemService afs, final DeleteCommand[] commands, int cancelTimeoutMs,
            Map<String, String> callContext) throws ServerError {

        super(null, null);

        if (commands == null || commands.length == 0) {
            this.commands = new DeleteCommand[0];
            this.deletions = new DeleteI[0];
            this.delegate = null;
        } else {
            final DoAllI all = new DoAllI(sf.context);
            this.commands = new DeleteCommand[commands.length];
            this.deletions = new DeleteI[commands.length];
            for (int i = 0; i < this.commands.length; i++) {
                this.commands[i] = commands[i];

                DeleteI req ;
                all.requests = new ArrayList<omero.cmd.Request>();
                for (DeleteCommand command : commands) {
                    if (command != null) {
                        DeleteStepFactory dsf = new DeleteStepFactory(sf.context);
                        Deletion d = new Deletion(ctx, dsf, afs, this.ctx);
                        req = new DeleteI(d);
                        req.type = command.type;
                        req.id = command.id;
                        req.options = command.options;
                        all.requests.add(req);
                        deletions[i] = req;
                    }
                }
            }
            this.delegate = new HandleI(cancelTimeoutMs);
            this.delegate.setSession(sf);
            this.delegate.initialize(id, all, callContext);

        }
    }

    //
    // DeleteHandle. See documentation in slice definition.
    //

    public DeleteCommand[] commands(Current __current) throws ServerError {
        return commands;
    }

    public boolean finished(Current __current) throws ServerError {
        if (delegate == null) {
            return true; // No commands
        }
        return null != delegate.getResponse(__current);
    }

    public int errors(Current __current) throws ServerError {
        int errors = 0;
        for (DeleteI deletion : deletions) {
            Deletion d = deletion.getDeletion();
            if (d.error()) {
                errors++;
            }
        }
        return errors;
    }

    public DeleteReport[] report(Current __current) throws ServerError {
        DeleteReport[] rv = new DeleteReport[commands.length];
        for (int i = 0; i < commands.length; i++) {
            DeleteI d = deletions[i];
            rv[i] = d.getDeleteReport();
        }
        return rv;
    }

    public boolean cancel(Current __current) throws ServerError {
        if (delegate == null) {
            return true; // No commands
        }
        return delegate.cancel(__current);
    }

    //
    // CloseableServant. See documentation in interface.
    //

    @Override
    protected void preClose(Ice.Current current) throws ServerError {
        if (!finished(current)) {
            log.warn("Handle closed before finished!");
        }
    }

    //
    // Runnable
    //

    /**
     * Calls {@link HandleI#run()}.
     */
    public void run() {
        if (delegate != null) {
            delegate.run();
        }
    }

}

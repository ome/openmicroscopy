/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.throttling;

import ome.api.ServiceInterface;
import ome.services.blitz.util.IceMethodInvoker;
import ome.system.OmeroContext;
import omero.util.IceMapper;

import org.springframework.util.Assert;

/**
 * Manages AMD-based method dispatches from blitz.
 * 
 */
public class Callback extends Task {

    private final Boolean io;
    private final Boolean db;
    private final IceMethodInvoker invoker;
    private final ServiceInterface service;
    private final Object[] args;
    private final IceMapper mapper;

    public Callback(Boolean io, Boolean db, ServiceInterface service,
            IceMethodInvoker invoker, Object cb, IceMapper mapper,
            Ice.Current current, Object... args) {
        
        super(cb, current, invoker.isVoid(current));

        Assert.notNull(invoker, "Null invoker");
        Assert.notNull(service, "Null service");
        Assert.notNull(args, "Null argument array");

        this.io = io;
        this.db = db;
        this.service = service;
        this.invoker = invoker;
        this.args = args;
        this.mapper = mapper;

    }

    public Callback(ServiceInterface service, IceMethodInvoker invoker,
            IceMapper mapper, Object cb, Ice.Current current, Object... args) {
        this(null, null, service, invoker, cb, mapper, current, args);
    }

    public void run(OmeroContext ctx) {
        try {
            Object retVal = invoker.invoke(service, current, mapper, args);
            response(retVal, ctx);
        } catch (Throwable e) {
            exception(e, ctx);
        }
    }

    /**
     * Callback can be either IO-intensive ({@link Boolean#TRUE}),
     * IO-non-intensive ({@link Boolean#FALSE}), or it can be unknown ({@link <code>null</code>}).
     */
    Boolean ioIntensive() {
        return io;
    }

    /**
     * Callback can be either database-intensive ({@link Boolean#TRUE}),
     * database-non-intensive ({@link Boolean#FALSE}), or it can be unknown ({@link <code>null</code>}).
     */
    Boolean dbIntensive() {
        return db;
    }

}

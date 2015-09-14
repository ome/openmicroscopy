/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.throttling;

import java.util.concurrent.Callable;

import ome.services.blitz.util.BlitzOnly;
import ome.system.OmeroContext;

import org.springframework.util.Assert;

/**
 * {@link Callable}-based callback which guarantees that ice_response or
 * ice_exception is called. Similar to {@link Callback}, this instance is useful
 * for servants which are {@link BlitzOnly}
 */
public class Callback2<R> extends Task {

    private final Object cb;

    private final Ice.Current current;

    private final Callable<R> callable;

    public Callback2(Ice.Current current, Object cb, boolean isVoid,
            Callable<R> callable) {

        super(cb, current, isVoid);

        Assert.notNull(cb, "Null callback object");
        Assert.notNull(callable, "Null callable object");

        this.cb = cb;
        this.current = current;
        this.callable = callable;
    }

    public void run(OmeroContext ctx) {
        try {
            R rv = callable.call();
            response(rv, ctx);
        } catch (Throwable e) {
            exception(e, ctx);
        }
    }

}

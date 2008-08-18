/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.throttling;

/**
 * Worker which consumes from the {@link Queue}.
 * 
 */
public class Slot implements Runnable {

    private final Queue q;

    public Slot(Queue q) {
        this.q = q;
    }

    public void run() {
        Callback cb = q.take();
        throw new UnsupportedOperationException();
    }
}

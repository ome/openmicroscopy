/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.messages;

import ome.util.messages.InternalMessage;

/**
 * Published when an request is made for a pixels pyramid when the file does
 * not yet exist.
 */
public class MissingPyramidMessage extends InternalMessage {

    private static final long serialVersionUID = 7132548299119420025L;

    final public long pixelsID;

    private boolean tryAgain = false;

    public MissingPyramidMessage(Object source, long pixelsID) {
        super(source);
        this.pixelsID = pixelsID;
    }

    public void setRetry() {
        this.tryAgain = true;
    }

    public boolean isRetry() {
        return tryAgain;
    }

}
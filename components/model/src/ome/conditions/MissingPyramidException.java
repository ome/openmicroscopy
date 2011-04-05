/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
public class MissingPyramidException extends ApiUsageException {

    /**
     *
     */
    private static final long serialVersionUID = 98734598734987811L;

    final protected long pixelsID;

    public MissingPyramidException(String msg, long pixelsID) {
        super(msg);
        this.pixelsID = pixelsID;
    }

    public long getPixelsId() {
        return pixelsID;
    }

}

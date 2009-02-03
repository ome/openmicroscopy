/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Error due to simultaneous access of some resource. Provides a suggested back
 * off time in milliseconds.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
public class ConcurrencyException extends RootException {

    /**
     * 
     */
    private static final long serialVersionUID = 8958921873970581811L;

    public final long backOff;

    public ConcurrencyException(String msg, long backOffInMilliseconds) {
        super(msg);
        this.backOff = backOffInMilliseconds;
    }

}

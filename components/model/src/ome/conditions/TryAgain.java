/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Background processing necessary to process the request. Wait and try again.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3
 */
public class TryAgain extends ConcurrencyException {

    /**
     *
     */
    private static final long serialVersionUID = 5920393509820381811L;

    public TryAgain(String msg, long backOffInMilliseconds) {
        super(msg, backOffInMilliseconds);
    }

}

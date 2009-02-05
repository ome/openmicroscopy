/*
 *   $Id$
 *
 *   Copyright (c) 2007 Glencoe Software Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.util;

import ome.util.messages.InternalMessage;

/**
 * Internal event which gives third party services a chance to map their 
 * exceptions to a blitz exception.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ConvertToBlitzExceptionMessage extends InternalMessage {

    private static final long serialVersionUID = 5309582093802L;

    public Throwable from, to;    
    
    public ConvertToBlitzExceptionMessage(Object source, Throwable from) {
        super(source);
        this.from = from;
    }
    
}

/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * More specific {@link ome.conditions.ApiUsageException ApiUsageException}, in
 * that the current use of the OMERO API could overwhelm the server and has been blocked.
 * 
 * <p>
 * Examples include:
 * <ul>
 * <li>Creating too many sessions in too short a period of time</li>
 * <li>Opening too many stateful services
 * <li>Requesting too many database objects in one call</li>
 * </ul>
 * </p>
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
public class OverUsageException extends ApiUsageException {

    /**
     * 
     */
    private static final long serialVersionUID = 8958921873970581811L;

    public OverUsageException(String msg) {
        super(msg);
    }

}

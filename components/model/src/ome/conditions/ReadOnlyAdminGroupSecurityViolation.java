/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Extension of {@link GroupSecurityViolation} signalling that an admin or
 * group owner has tried to make a modification in a private group.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 */
public class ReadOnlyAdminGroupSecurityViolation extends GroupSecurityViolation {

    private static final long serialVersionUID = -45134342129377L;

    public ReadOnlyAdminGroupSecurityViolation(String msg) {
        super(msg);
    }

}

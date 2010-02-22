/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Extension of {@link GroupSecurityViolation} signalling that an object
 * has a permission which does not match the group permissions.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 */
public class PermissionMismatchGroupSecurityViolation extends GroupSecurityViolation {

    private static final long serialVersionUID = -45134342129377L;

    public PermissionMismatchGroupSecurityViolation(String msg) {
        super(msg);
    }

}

/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions;

/**
 * Extension of {@link SecurityViolation} which signifies that the violation
 * in question goes against the group-based permissions introduced in 4.2.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2
 */
public class GroupSecurityViolation extends SecurityViolation {

    private static final long serialVersionUID = -45134342129377L;

    public GroupSecurityViolation(String msg) {
        super(msg);
    }

}

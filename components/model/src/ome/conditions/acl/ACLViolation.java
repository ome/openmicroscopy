/*
 * ome.conditions.ACLViolation
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions.acl;

import ome.model.internal.Permissions;

/**
 * User has attempted an action which is not permitted by the
 * {@link Permissions} of a given instance.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 2.5
 * @since 2.5
 */
public abstract class ACLViolation extends ome.conditions.SecurityViolation {

    private Class klass;

    private Long id;

    public ACLViolation(Class klass, Long id, String msg) {
        super(msg);
        this.klass = klass;
        this.id = id;
    }

    @Override
    public String getMessage() {

        String s = super.getMessage();
        if (s == null) {
            s = "";
        }

        String k = klass == null ? "No class" : klass.getName();

        String i = id == null ? "No id" : id.toString();

        int size = s.length() + k.length() + i.length();

        StringBuilder sb = new StringBuilder(size + 16);
        sb.append(k);
        sb.append(":");
        sb.append(i);
        sb.append(" -- ");
        sb.append(s);
        return sb.toString();
    }
}

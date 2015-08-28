/*
 * ome.conditions.CollectedACLViolations
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.conditions.acl;

import java.util.ArrayList;
import java.util.List;

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
public class CollectedACLViolations extends ome.conditions.SecurityViolation {

    /**
     * 
     */
    private static final long serialVersionUID = -340834454556786566L;
    private List<ACLViolation> violations;

    public CollectedACLViolations(String msg) {
        super(msg);
    }

    public void addViolation(ACLViolation v) {
        if (null == violations) {
            violations = new ArrayList<ACLViolation>();
        }

        violations.add(v);
    }

    @Override
    public String getMessage() {
        int size = super.getMessage().length() * violations.size();
        StringBuilder sb = new StringBuilder(size);
        sb.append(super.getMessage());
        sb.append("\n");
        for (int i = 0; i < violations.size(); i++) {
            sb.append("\t");
            sb.append("(");
            sb.append(Integer.toString(i));
            sb.append(") ");
            sb.append(violations.get(i).getMessage());
            sb.append("\n");
        }
        return sb.toString();
    }

}

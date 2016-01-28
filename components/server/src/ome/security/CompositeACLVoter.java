/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import java.util.Set;

import ome.api.IShare;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.security.basic.BasicACLVoter;
import ome.security.basic.CurrentDetails;
import ome.security.sharing.SharingACLVoter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see IShare
 * @since 3.0-Beta4
 */
public class CompositeACLVoter implements ACLVoter {

    private final static Logger log = LoggerFactory.getLogger(CompositeACLVoter.class);

    private final CurrentDetails cd;

    private final BasicACLVoter basic;

    private final SharingACLVoter sharing;

    public CompositeACLVoter(CurrentDetails cd, BasicACLVoter basic,
            SharingACLVoter sharing) {
        this.basic = basic;
        this.sharing = sharing;
        this.cd = cd;
    }

    public ACLVoter choose() {
        Long shareId = cd.getCurrentEventContext().getCurrentShareId();
        if (shareId == null || shareId.longValue() < 0) { // ticket:2219
            return basic;
        } else {
            return sharing;
        }
    }

    // Delegation
    // =========================================================================

    public boolean allowChmod(IObject object) {
        return choose().allowChmod(object);
    }

    public boolean allowCreation(IObject object) {
        return choose().allowCreation(object);
    }

    public boolean allowDelete(IObject object, Details trustedDetails) {
        return choose().allowDelete(object, trustedDetails);
    }

    public boolean allowLoad(Session session,Class<? extends IObject> klass,
            Details trustedDetails, long id) {
        return choose().allowLoad(session, klass, trustedDetails, id);
    }

    public boolean allowAnnotate(IObject object, Details trustedDetails) {
        return choose().allowAnnotate(object, trustedDetails);
    }

    public boolean allowUpdate(IObject object, Details trustedDetails) {
        return choose().allowUpdate(object, trustedDetails);
    }

    public void throwCreationViolation(IObject object) throws SecurityViolation {
        choose().throwCreationViolation(object);
    }

    public void throwDeleteViolation(IObject object) throws SecurityViolation {
        choose().throwDeleteViolation(object);
    }

    public void throwLoadViolation(IObject object) throws SecurityViolation {
        choose().throwLoadViolation(object);
    }

    public void throwUpdateViolation(IObject object) throws SecurityViolation {
        choose().throwUpdateViolation(object);
    }

    @Override
    public Set<String> restrictions(IObject object) {
        return choose().restrictions(object);
    }

    public void postProcess(IObject object) {
        choose().postProcess(object);
    }
}

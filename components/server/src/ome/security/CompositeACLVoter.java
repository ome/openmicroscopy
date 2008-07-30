/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import ome.api.IShare;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.security.basic.BasicACLVoter;
import ome.security.basic.CurrentDetails;
import ome.security.sharing.SharingACLVoter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see IShare
 * @since 3.0-Beta4
 */
public class CompositeACLVoter implements ACLVoter {

    private final static Log log = LogFactory.getLog(CompositeACLVoter.class);

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
        if (shareId == null) {
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

    public boolean allowLoad(Class<? extends IObject> klass,
            Details trustedDetails, long id) {
        return choose().allowLoad(klass, trustedDetails, id);
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

}

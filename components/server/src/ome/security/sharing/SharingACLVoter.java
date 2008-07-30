/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.sharing;

import ome.api.IShare;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.security.ACLVoter;
import ome.security.SystemTypes;
import ome.security.basic.BasicSecuritySystem;
import ome.security.basic.CurrentDetails;
import ome.services.sharing.ShareStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see IShare
 * @since 3.0-Beta4
 */
public class SharingACLVoter implements ACLVoter {

    private final static Log log = LogFactory.getLog(SharingACLVoter.class);

    private final SystemTypes sysTypes;

    private final ShareStore store;

    private final CurrentDetails cd;

    public SharingACLVoter(CurrentDetails cd, SystemTypes sysTypes,
            ShareStore store) {
        this.sysTypes = sysTypes;
        this.store = store;
        this.cd = cd;
    }

    // ~ Interface methods
    // =========================================================================

    /**
     * 
     */
    public boolean allowChmod(IObject iObject) {
        return false;
    }

    /**
     * 
     */
    public boolean allowLoad(Class<? extends IObject> klass, Details d, long id) {
        Assert.notNull(klass);
        // Assert.notNull(d);
        if (d == null || sysTypes.isSystemType(klass)) {
            return true;
        }
        long session = cd.getCurrentEventContext()
        return store.contains(session, klass, id);
    }

    public void throwLoadViolation(IObject iObject) throws SecurityViolation {
        Assert.notNull(iObject);
        throw new SecurityViolation(iObject + " not contained in share");
    }

    public boolean allowCreation(IObject iObject) {
        return false;
    }

    public void throwCreationViolation(IObject iObject)
            throws SecurityViolation {
        throwDisabled("Creation");
    }

    public boolean allowUpdate(IObject iObject, Details trustedDetails) {
        return false;
    }

    public void throwUpdateViolation(IObject iObject) throws SecurityViolation {
        throwDisabled("Update");
    }

    public boolean allowDelete(IObject iObject, Details trustedDetails) {
        return false;
    }

    public void throwDeleteViolation(IObject iObject) throws SecurityViolation {
        throwDisabled("Delete");
    }

    // Helpers
    // =========================================================================
    protected void throwDisabled(String action) {
        throw new SecurityViolation(action + " is not allowed while in share.");
    }

}

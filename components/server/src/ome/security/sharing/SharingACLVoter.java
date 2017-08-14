/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.sharing;

import java.util.Map;
import java.util.Set;

import ome.api.IShare;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.security.ACLVoter;
import ome.security.SystemTypes;
import ome.security.basic.CurrentDetails;
import ome.security.basic.TokenHolder;
import ome.services.sharing.ShareStore;
import ome.services.sharing.data.ShareData;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see IShare
 * @since 3.0-Beta4
 */
public class SharingACLVoter implements ACLVoter {

    private final static Logger log = LoggerFactory.getLogger(SharingACLVoter.class);

    private final SystemTypes sysTypes;

    private final ShareStore store;

    private final CurrentDetails cd;

    private final TokenHolder tokenHolder;

    public SharingACLVoter(CurrentDetails cd, SystemTypes sysTypes,
            ShareStore store, TokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
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
    public boolean allowLoad(Session session, Class<? extends IObject> klass, Details d, long id) {
        Assert.notNull(klass);
        // Assert.notNull(d);
        if (d == null ||
                sysTypes.isSystemType(klass) ||
                sysTypes.isInSystemGroup(d)) {
            return true;
        }
        long sessionID = cd.getCurrentEventContext().getCurrentShareId();
        ShareData data = store.get(sessionID);
        if (data.enabled) {
            return store.contains(sessionID, klass, id);
        }
        return false;
    }

    public void throwLoadViolation(IObject iObject) throws SecurityViolation {
        Assert.notNull(iObject);
        throw new SecurityViolation(iObject + " not contained in share");
    }

    public boolean allowCreation(IObject iObject) {
        if (tokenHolder.hasPrivilegedToken(iObject)) {
            return true;
        }
        return false;
    }

    public void throwCreationViolation(IObject iObject)
            throws SecurityViolation {
        throwDisabled("Creation");
    }

    public boolean allowAnnotate(IObject iObject, Details trustedDetails) {
        return false;
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

    @Override
    public Set<String> restrictions(IObject object) {
        return null;
    }

    @Override
    public void setPermittedClasses(Map<Integer, Set<Class<? extends IObject>>> objectClassesPermitted) {
    }

    @Override
    public void postProcess(IObject object) {
        if (object != null && object.isLoaded()) {
            Details d = object.getDetails();
            Permissions p = d.getPermissions();
            Permissions copy = new Permissions(p);
            copy.copyRestrictions(0, null);
            d.setPermissions(copy);
        }
    }

    // Helpers
    // =========================================================================
    protected void throwDisabled(String action) {
        throw new SecurityViolation(action + " is not allowed while in share.");
    }
}

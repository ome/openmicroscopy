/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.security.basic.BasicSecuritySystem;
import ome.security.sharing.SharingSecuritySystem;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;

/**
 * Security dispatcher holding each currently active {@link SecuritySystem}
 * instance and allowing dispatching between them.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see BasicSecuritySystem
 * @see SharingSecuritySystem
 * @since 3.0-Beta4
 */

public class SecuritySystemHolder implements SecuritySystem {

    final protected BasicSecuritySystem basic;

    final protected SharingSecuritySystem sharing;

    protected ThreadLocal<SecuritySystem> current = new ThreadLocal<SecuritySystem>() {
        @Override
        protected SecuritySystem initialValue() {
            return basic;
        }
    };

    public SecuritySystemHolder(ome.security.basic.BasicSecuritySystem basic,
            SharingSecuritySystem sharing) {
        this.basic = basic;
        this.sharing = sharing;
    }

    public void chooseBasic() {
        current.set(basic);
    }

    public void chooseSharing() {
        current.set(sharing);
    }

    // Delegation
    // =========================================================================

    public Details checkManagedDetails(IObject object, Details trustedDetails)
            throws ApiUsageException, SecurityViolation {
        return current.get().checkManagedDetails(object, trustedDetails);
    }

    public void clearEventContext() {
        current.get().clearEventContext();
    }

    public void disable(String... ids) {
        current.get().disable(ids);
    }

    public <T extends IObject> T doAction(SecureAction action, T... objs) {
        return current.get().doAction(action, objs);
    }

    public void enable(String... ids) {
        current.get().enable(ids);
    }

    public ACLVoter getACLVoter() {
        return current.get().getACLVoter();
    }

    public EventContext getEventContext() {
        return current.get().getEventContext();
    }

    public Roles getSecurityRoles() {
        return current.get().getSecurityRoles();
    }

    public boolean hasPrivilegedToken(IObject obj) {
        return current.get().hasPrivilegedToken(obj);
    }

    public boolean isDisabled(String id) {
        return current.get().isDisabled(id);
    }

    public boolean isEmptyEventContext() {
        return current.get().isEmptyEventContext();
    }

    public boolean isReady() {
        return current.get().isReady();
    }

    public boolean isSystemType(Class<? extends IObject> klass) {
        return current.get().isSystemType(klass);
    }

    public void loadEventContext(boolean isReadyOnly) {
        current.get().loadEventContext(isReadyOnly);
    }

    public void login(Principal principal) {
        current.get().login(principal);
    }

    public int logout() {
        return current.get().logout();
    }

    public Details newTransientDetails(IObject object)
            throws ApiUsageException, SecurityViolation {
        return current.get().newTransientDetails(object);
    }

    public void runAsAdmin(AdminAction action) {
        current.get().runAsAdmin(action);
    }

    public void setEventContext(EventContext context) {
        current.get().setEventContext(context);
    }

}

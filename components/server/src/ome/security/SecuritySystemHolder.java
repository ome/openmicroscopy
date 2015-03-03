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
import ome.model.meta.ExperimenterGroup;
import ome.security.basic.BasicSecuritySystem;
import ome.security.policy.Policy;
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

    public SecuritySystem choose() {
        Long shareId = this.basic.getEventContext().getCurrentShareId();
        if (shareId == null) {
            return basic;
        } else {
            return sharing;
        }
    }

    // Delegation
    // =========================================================================

    public Details checkManagedDetails(IObject object, Details trustedDetails)
            throws ApiUsageException, SecurityViolation {
        return choose().checkManagedDetails(object, trustedDetails);
    }

    public void invalidateEventContext() {
        choose().invalidateEventContext();
    }

    public void disable(String... ids) {
        choose().disable(ids);
    }

    public <T extends IObject> T doAction(SecureAction action, T... objs) {
        return choose().doAction(action, objs);
    }

    public void enable(String... ids) {
        choose().enable(ids);
    }

    public EventContext getEventContext() {
        return choose().getEventContext();
    }

    public EventContext getEventContext(boolean refresh) {
        return choose().getEventContext(refresh);
    }

    public Long getEffectiveUID() {
        return choose().getEffectiveUID();
    }

    public Roles getSecurityRoles() {
        return choose().getSecurityRoles();
    }

    public boolean hasPrivilegedToken(IObject obj) {
        return choose().hasPrivilegedToken(obj);
    }

    @Override
    public void checkRestriction(String name, IObject obj) {
        choose().checkRestriction(name, obj);
    }

    public boolean isDisabled(String id) {
        return choose().isDisabled(id);
    }

    public boolean isReady() {
        return choose().isReady();
    }

    public boolean isSystemType(Class<? extends IObject> klass) {
        return choose().isSystemType(klass);
    }

    public void loadEventContext(boolean isReadOnly) {
        choose().loadEventContext(isReadOnly);
    }

    public void login(Principal principal) {
        choose().login(principal);
    }

    public int logout() {
        return choose().logout();
    }

    public Details newTransientDetails(IObject object)
            throws ApiUsageException, SecurityViolation {
        return choose().newTransientDetails(object);
    }

    public void runAsAdmin(AdminAction action) {
        choose().runAsAdmin(action);
    }

    public void runAsAdmin(ExperimenterGroup group, AdminAction action) {
        choose().runAsAdmin(group, action);
    }

    public boolean isGraphCritical(Details details) {
        return choose().isGraphCritical(details);
    }

}

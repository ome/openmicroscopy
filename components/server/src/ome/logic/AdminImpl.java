/*
 * ome.logic.AdminImpl
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import ome.annotations.NotNull;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.IAdmin;
import ome.api.ServiceInterface;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.security.ACLVoter;
import ome.security.AdminAction;
import ome.security.LdapUtil;
import ome.security.PasswordUtil;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.security.basic.BasicSecuritySystem;
import ome.security.basic.UpdateEventListener;
import ome.services.query.Definitions;
import ome.services.query.Query;
import ome.services.query.QueryParameterDef;
import ome.services.util.OmeroAroundInvoke;
import ome.system.EventContext;
import ome.system.Roles;
import ome.system.SimpleEventContext;
import ome.tools.hibernate.HibernateUtils;
import ome.util.Utils;

import org.hibernate.Criteria;
import org.hibernate.EmptyInterceptor;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Provides methods for administering user accounts, passwords, as well as
 * methods which require special privileges.
 * 
 * Developer note: As can be expected, to perform these privileged the Admin
 * service has access to several resources that should not be generally used
 * while developing services. Misuse could circumvent security or auditing.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision:1754 $, $Date:2007-08-20 10:36:07 +0100 (Mon, 20 Aug 2007) $
 * @see SecuritySystem
 * @see Permissions
 * @since 3.0-M3
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@RevisionDate("$Date:2007-08-20 10:36:07 +0100 (Mon, 20 Aug 2007) $")
@RevisionNumber("$Revision:1754 $")
@Stateless
@Remote(IAdmin.class)
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/ome.api.IAdmin"),
    @RemoteBinding(jndiBinding = "omero/secure/ome.api.IAdmin",
		   clientBindUrl="sslsocket://0.0.0.0:3843")
})
@Local(IAdmin.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.IAdmin")
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
public class AdminImpl extends AbstractLevel2Service implements LocalAdmin {

    /**
     * Action used by various methods to save objects with the blessing of the
     * {@link SecuritySystem}.Only the first object will be saved and returned,
     * but all of the varargs will be given a token.
     * 
     * @see SecuritySystem#doAction(IObject, SecureAction)
     */
    private static class SecureUpdate implements SecureAction {
        protected final LocalUpdate iUpdate;

        SecureUpdate(LocalUpdate iUpdate) {
            this.iUpdate = iUpdate;
        }

        public <T extends IObject> T updateObject(final T... objs) {
            return iUpdate.saveAndReturnObject(objs[0]);
        }
    };

    /**
     * Action used to flush already saved objects. The top-level objects will be
     * given a token and so can be safely flushed.
     */
    private static class SecureFlush extends SecureUpdate {
        SecureFlush(LocalUpdate iUpdate) {
            super(iUpdate);
        }

        @Override
        public <T extends IObject> T updateObject(T... objs) {
            iUpdate.flush();
            return null;
        }
    }

    protected transient SimpleJdbcTemplate jdbc;

    protected transient SessionFactory sf;

    /** injector for usage by the container. Not for general use */
    public final void setJdbcTemplate(SimpleJdbcTemplate jdbcTemplate) {
        getBeanHelper().throwIfAlreadySet(this.jdbc, jdbcTemplate);
        jdbc = jdbcTemplate;
    }

    /** injector for usage by the container. Not for general use */
    public final void setSessionFactory(SessionFactory sessions) {
        getBeanHelper().throwIfAlreadySet(this.sf, sessions);
        sf = sessions;
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return IAdmin.class;
    }

    // ~ LOCAL PUBLIC METHODS
    // =========================================================================

    @RolesAllowed("user")
    public Experimenter userProxy(final Long id) {
        if (id == null) {
            throw new ApiUsageException("Id argument cannot be null.");
        }

        Experimenter e = iQuery.get(Experimenter.class, id);
        return e;
    }

    @RolesAllowed("user")
    public Experimenter userProxy(final String omeName) {
        if (omeName == null) {
            throw new ApiUsageException("omeName argument cannot be null.");
        }

        Experimenter e = iQuery.findByString(Experimenter.class, "omeName",
                omeName);

        if (e == null) {
            throw new ApiUsageException("No such experimenter: " + omeName);
        }

        return e;
    }

    @RolesAllowed("user")
    public ExperimenterGroup groupProxy(Long id) {
        if (id == null) {
            throw new ApiUsageException("Id argument cannot be null.");
        }

        ExperimenterGroup g = iQuery.get(ExperimenterGroup.class, id);
        return g;
    }

    @RolesAllowed("user")
    public ExperimenterGroup groupProxy(final String groupName) {
        if (groupName == null) {
            throw new ApiUsageException("groupName argument cannot be null.");
        }

        ExperimenterGroup g = iQuery.findByString(ExperimenterGroup.class,
                "name", groupName);

        if (g == null) {
            throw new ApiUsageException("No such group: " + groupName);
        }

        return g;
    }

    @RolesAllowed("user")
    public List<Long> getLeaderOfGroupIds(final Experimenter e) {
        Assert.notNull(e);
        Assert.notNull(e.getId());

        List<Long> groupIds = iQuery.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                org.hibernate.Query q = session
                        .createQuery("select g.id from ExperimenterGroup g where g.details.owner.id = :id");
                q.setParameter("id", e.getId());
                return q.list();
            }
        });
        return groupIds;
    }

    @RolesAllowed("user")
    public List<Long> getMemberOfGroupIds(final Experimenter e) {
        Assert.notNull(e);
        Assert.notNull(e.getId());

        List<Long> groupIds = iQuery.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                org.hibernate.Query q = session
                        .createQuery("select m.parent.id from GroupExperimenterMap m "
                                + "where m.child.id = :id");
                q.setParameter("id", e.getId());
                return q.list();
            }
        });
        return groupIds;
    }

    @RolesAllowed("user") // TODO copied from getMemberOfGroupIds
    public List<String> getUserRoles(final Experimenter e) {
        Assert.notNull(e);
        Assert.notNull(e.getId());

        List<String> groupNames = iQuery.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                org.hibernate.Query q = session
                        .createQuery("select m.parent.name from GroupExperimenterMap m "
                                + "where m.child.id = :id");
                q.setParameter("id", e.getId());
                return q.list();
            }
        });
        return groupNames;
    }

    // ~ User accessible interface methods
    // =========================================================================

    @RolesAllowed("user")
    public Experimenter getExperimenter(final Long id) {
        Experimenter e = iQuery.execute(new UserQ(new Parameters().addId(id)));

        if (e == null) {
            throw new ApiUsageException("No such experimenter: " + id);
        }

        return e;
    }

    @RolesAllowed("user")
    public Experimenter lookupExperimenter(final String omeName) {
        Experimenter e = iQuery.execute(new UserQ(new Parameters().addString(
                "name", omeName)));

        if (e == null) {
            throw new ApiUsageException("No such experimenter: " + omeName);
        }

        return e;
    }

    @RolesAllowed("user")
    public List<Experimenter> lookupExperimenters() {
        return iQuery.findAllByQuery("select e from Experimenter e "
                + "left outer join fetch e.groupExperimenterMap m "
                + "left outer join fetch m.parent g", null);
    }

    @Transactional(readOnly = true)
    @RolesAllowed("user")
    public List<Map<String, Object>> lookupLdapAuthExperimenters() {
        return LdapUtil.lookupLdapAuthExperimenters(jdbc);
    }

    @RolesAllowed("user")
    public String lookupLdapAuthExperimenter(Long id) {
        return LdapUtil.lookupLdapAuthExperimenter(jdbc, id);
    }

    @RolesAllowed("user")
    public ExperimenterGroup getGroup(Long id) {
        ExperimenterGroup g = iQuery.execute(new GroupQ(new Parameters()
                .addId(id)));

        if (g == null) {
            throw new ApiUsageException("No such group: " + id);
        }

        return g;
    }

    @RolesAllowed("user")
    public ExperimenterGroup lookupGroup(final String groupName) {
        ExperimenterGroup g = iQuery.execute(new GroupQ(new Parameters()
                .addString("name", groupName)));

        if (g == null) {
            throw new ApiUsageException("No such group: " + groupName);
        }

        return g;
    }

    @RolesAllowed("user")
    public List<ExperimenterGroup> lookupGroups() {
        return iQuery.findAllByQuery("select g from ExperimenterGroup g "
                + "left outer join fetch g.groupExperimenterMap m "
                + "left outer join fetch m.child", null);
    }

    @RolesAllowed("user")
    public Experimenter[] containedExperimenters(Long groupId) {
        List<Experimenter> experimenters = iQuery.findAllByQuery(
                "select e from Experimenter as e left outer "
                        + "join e.groupExperimenterMap as map left outer join "
                        + "map.parent as g where g.id = :id", new Parameters()
                        .addId(groupId));
        return experimenters.toArray(new Experimenter[experimenters.size()]);
    }

    @RolesAllowed("user")
    public ExperimenterGroup[] containedGroups(Long experimenterId) {
        List<ExperimenterGroup> groups = iQuery
                .findAllByQuery(
                        "select g from ExperimenterGroup as g left "
                                + "outer join g.groupExperimenterMap as map left outer "
                                + "join map.child as e where e.id = :id",
                        new Parameters().addId(experimenterId));
        return groups.toArray(new ExperimenterGroup[groups.size()]);
    }

    // ~ System-only interface methods
    // =========================================================================

    @RolesAllowed("system")
    public void synchronizeLoginCache() {
        String string = "omero:service=LoginConfig";
        // using Spring utilities to get MBeanServer
        MBeanServer mbeanServer = JmxUtils.locateMBeanServer();
        getBeanHelper().getLogger().debug("Acquired MBeanServer.");
        ObjectName name;
        try {
            // defined in app/resources/jboss-service.xml
            name = new ObjectName(string);
            mbeanServer.invoke(name, "flushAuthenticationCaches",
                    new Object[] {}, new String[] {});
            getBeanHelper().getLogger().debug("Flushed authentication caches.");
        } catch (InstanceNotFoundException infe) {
            getBeanHelper().getLogger().warn(
                    string + " not found. Won't synchronize login cache.");
        } catch (Exception e) {
            InternalException ie = new InternalException(e.getMessage());
            ie.setStackTrace(e.getStackTrace());
            throw ie;
        }
    }

    @RolesAllowed("user")
    public void updateSelf(@NotNull
    Experimenter e) {
        EventContext ec = getSecuritySystem().getEventContext();
        final Experimenter self = getExperimenter(ec.getCurrentUserId());
        self.setFirstName(e.getFirstName());
        self.setMiddleName(e.getMiddleName());
        self.setLastName(e.getLastName());
        self.setEmail(e.getEmail());
        self.setInstitution(e.getInstitution());
        getSecuritySystem().runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                iUpdate.flush();
            }
        });
        getBeanHelper().getLogger().info(
                "Updated own user info: " + self.getOmeName());
    }

    @RolesAllowed("system")
    public void updateExperimenter(@NotNull
    Experimenter experimenter) {
        String name = experimenter.getOmeName();
        iUpdate.saveObject(experimenter);
        getBeanHelper().getLogger().info("Updated user info for " + name);
    }

    @RolesAllowed("system")
    public void updateGroup(@NotNull
    ExperimenterGroup group) {
        iUpdate.saveObject(group);
        getBeanHelper().getLogger().info("Updated group info for " + group);
    }

    @RolesAllowed("system")
    public long createUser(Experimenter newUser, String defaultGroup) {
        // logged via createExperimenter
        return createExperimenter(newUser, groupProxy(defaultGroup),
                groupProxy("user"));
    }

    @RolesAllowed("system")
    public long createSystemUser(Experimenter newSystemUser) {
        // logged via createExperimenter
        return createExperimenter(newSystemUser, groupProxy("system"),
                groupProxy("user"));
    }

    @RolesAllowed("system")
    @SuppressWarnings("unchecked")
    public long createExperimenter(Experimenter experimenter,
            ExperimenterGroup defaultGroup, ExperimenterGroup... otherGroups) {

        SecureAction action = new SecureUpdate(iUpdate);

        Experimenter e = copyUser(experimenter);
        e.getDetails().copy(getSecuritySystem().newTransientDetails(e));
        e = getSecuritySystem().doAction(action, e);
        iUpdate.flush();

        GroupExperimenterMap link = linkGroupAndUser(defaultGroup, e);
        if (null != otherGroups) {
            for (ExperimenterGroup group : otherGroups) {
                linkGroupAndUser(group, e);
            }
        }

        changeUserPassword(e.getOmeName(), " ");

        getBeanHelper().getLogger().info(
                "Created user with blank password: " + e.getOmeName());
        return e.getId();
    }

    private GroupExperimenterMap linkGroupAndUser(ExperimenterGroup group,
            Experimenter e) {

        if (group == null || group.getId() == null) {
            throw new ApiUsageException("Group must be persistent.");
        }

        group = new ExperimenterGroup(group.getId(), false);

        GroupExperimenterMap link = e.linkExperimenterGroup(group);
        link.getDetails().copy(getSecuritySystem().newTransientDetails(link));
        getSecuritySystem().doAction(new SecureUpdate(iUpdate),
                userProxy(e.getId()), link);
        iUpdate.flush();
        return link;
    }

    @RolesAllowed("system")
    public long createGroup(ExperimenterGroup group) {
        group = copyGroup(group);
        ExperimenterGroup g = getSecuritySystem().doAction(
                new SecureUpdate(iUpdate), group);

        getBeanHelper().getLogger().info("Created group: " + g.getName());
        return g.getId();
    }

    @RolesAllowed("system")
    public void addGroups(final Experimenter user,
            final ExperimenterGroup... groups) {
        assertManaged(user);

        final List<String> added = new ArrayList<String>();

        Experimenter foundUser = userProxy(user.getId());
        for (ExperimenterGroup group : groups) {
            assertManaged(group);
            ExperimenterGroup foundGroup = groupProxy(group.getId());
            boolean found = false;
            for (ExperimenterGroup currentGroup : foundUser
                    .linkedExperimenterGroupList()) {
                found |= HibernateUtils.idEqual(foundGroup, currentGroup);
            }
            if (!found) {
                linkGroupAndUser(foundGroup, foundUser);
                added.add(foundGroup.getName());
            }
        }

        getBeanHelper().getLogger().info(
                String.format("Added user %s to groups %s", userProxy(
                        user.getId()).getOmeName(), added));
    }

    @RolesAllowed("system")
    public void removeGroups(Experimenter user, ExperimenterGroup... groups) {
        if (user == null) {
            return;
        }
        if (groups == null) {
            return;
        }

        Experimenter foundUser = getExperimenter(user.getId());
        List<Long> toRemove = new ArrayList<Long>();
        List<String> removed = new ArrayList<String>();

        for (ExperimenterGroup g : groups) {
            if (g.getId() != null) {
                toRemove.add(g.getId());
            }
        }
        for (GroupExperimenterMap map : foundUser
                .<GroupExperimenterMap> collectGroupExperimenterMap(null)) {
            Long pId = map.parent().getId();
            Long cId = map.child().getId();
            if (toRemove.contains(pId)) {
                ExperimenterGroup p = iQuery.get(ExperimenterGroup.class, pId);
                Experimenter c = iQuery.get(Experimenter.class, cId);
                p.unlinkExperimenter(c);
                getSecuritySystem().doAction(new SecureUpdate(iUpdate), p);
                removed.add(p.getName());
            }
        }
        iUpdate.flush();

        getBeanHelper().getLogger().info(
                String.format("Removed user %s from groups %s", foundUser
                        .getOmeName(), removed));
    }

    @RolesAllowed("user")
    public void setDefaultGroup(Experimenter user, ExperimenterGroup group) {
        if (user == null) {
            return;
        }
        if (group == null) {
            return;
        }

        if (group.getId() == null) {
            throw new ApiUsageException("Group argument to setDefaultGroup "
                    + "must be managed (i.e. have an id)");
        }

        EventContext ec = getSecuritySystem().getEventContext();
        if (!ec.isCurrentUserAdmin()
                && !ec.getCurrentUserId().equals(user.getId())) {
            throw new SecurityViolation("User " + user.getId()
                    + " can only set own default group.");
        }

        Roles roles = getSecuritySystem().getSecurityRoles();
        if (Long.valueOf(roles.getUserGroupId()).equals(group.getId())) {
            throw new ApiUsageException("Cannot set default group to: "
                    + roles.getUserGroupName());
        }

        Experimenter foundUser = getExperimenter(user.getId());
        ExperimenterGroup foundGroup = getGroup(group.getId());
        Set<GroupExperimenterMap> foundMaps = foundUser
                .findGroupExperimenterMap(foundGroup);
        if (foundMaps.size() < 1) {
            throw new ApiUsageException("Group " + group.getId() + " was not "
                    + "found for user " + user.getId());
        } else if (foundMaps.size() > 1) {
            getBeanHelper().getLogger().warn(
                    foundMaps.size() + " copies of " + foundGroup
                            + " found for " + foundUser);
        } else {
            // May throw an exception
            foundUser.setPrimaryGroupExperimenterMap(foundMaps.iterator()
                    .next());
        }

        // TODO: May want to move this outside the loop
        // and after the !newDefaultSet check.
        getSecuritySystem().doAction(new SecureUpdate(iUpdate), foundUser);

        getBeanHelper().getLogger().info(
                String.format("Changing default group for %s to %s", foundUser
                        .getOmeName(), foundGroup.getName()));

    }

    @RolesAllowed("system")
    public void setGroupOwner(ExperimenterGroup group, Experimenter owner) {
        if (owner == null) {
            return;
        }
        if (group == null) {
            return;
        }

        if (group.getId() == null) {
            throw new ApiUsageException("Group argument to setGroupOwner "
                    + "must be managed (i.e. have an id)");
        }

        // TODO add an @Managed annotation
        if (owner.getId() == null) {
            throw new ApiUsageException("Owner argument to setGroupOwner "
                    + "must be managed (i.e. have an id)");
        }

        Experimenter foundUser = userProxy(owner.getId());
        ExperimenterGroup foundGroup = groupProxy(group.getId());
        foundGroup.getDetails().setOwner(foundUser);
        iUpdate.flush();

        getBeanHelper().getLogger().info(
                String.format("Changing owner for group %s to %s", foundGroup
                        .getName(), foundUser.getOmeName()));
    }

    @RolesAllowed("user")
    public ExperimenterGroup getDefaultGroup(@NotNull
    Long experimenterId) {
        ExperimenterGroup g = iQuery.findByQuery(
                "select g from ExperimenterGroup g, Experimenter e "
                        + "join e.groupExperimenterMap m "
                        + "where e.id = :id and m.parent = g.id "
                        + "and g.name != :userGroup and index(m) = 0",
                new Parameters().addId(experimenterId).addString("userGroup",
                        sec.getSecurityRoles().getUserGroupName()));
        if (g == null) {
            throw new ValidationException("The user " + experimenterId
                    + " has no default group set.");
        }
        return g;
    }

    @RolesAllowed("system")
    public void deleteExperimenter(Experimenter user) {
        Experimenter e = userProxy(user.getId());
        int count = jdbc.update(
                "delete from password where experimenter_id = ?", e.getId());

        if (count == 0) {
            getBeanHelper().getLogger().info(
                    "No password found for user " + e.getOmeName()
                            + ". Cannot delete.");
        }

        iUpdate.deleteObject(e);
        getBeanHelper().getLogger().info("Deleted user: " + e.getOmeName());
    }

    // ~ chown / chgrp / chmod
    // =========================================================================

    @RolesAllowed("user")
    public void changeOwner(IObject iObject, String omeName) {
        // should take an Owner
        IObject copy = iQuery.get(iObject.getClass(), iObject.getId());
        Experimenter owner = userProxy(omeName);
        copy.getDetails().setOwner(owner);
        iUpdate.saveObject(copy);
    }

    @RolesAllowed("user")
    public void changeGroup(IObject iObject, String groupName) {
        final LocalUpdate update = iUpdate;
        // should take a group
        final IObject copy = iQuery.get(iObject.getClass(), iObject.getId());
        final ExperimenterGroup group = groupProxy(groupName);

        // do check TODO refactor
        final EventContext ec = getSecuritySystem().getEventContext();
        if (!ec.getMemberOfGroupsList().contains(group.getId())
                && !ec.isCurrentUserAdmin()) {
            throw new SecurityViolation("Cannot change group for:" + iObject);
        }

        // make change.
        copy.getDetails().setGroup(group);
        getSecuritySystem().doAction(new SecureFlush(iUpdate), copy);
    }

    /**
     * the implementation of this method is somewhat tricky in that
     * {@link Permissions} changes must be allowed even when other updates are
     * not. Therefore, we must manually check if the object belongs to this user
     * or is admin (before the call to
     * {@link SecuritySystem#runAsAdmin(AdminAction)}
     * 
     * This logic is duplicated in
     * {@link BasicSecuritySystem#checkManagedDetails(IObject, ome.model.internal.Details)}.
     * 
     * @see IAdmin#changePermissions(IObject, Permissions)
     * @see <a
     *      href="http://trac.openmicroscopy.org.uk/omero/ticket/293">ticket:293</a>
     */
    @RolesAllowed("user")
    public void changePermissions(final IObject iObject, final Permissions perms) {

        final ACLVoter aclVoter = getSecuritySystem().getACLVoter(); // TODO
        // inject
        final IObject[] copy = new IObject[1];

        // first load the instance.
        getSecuritySystem().runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                copy[0] = iQuery.get(iObject.getClass(), iObject.getId());
            }
        });

        // now check for ownership _outside_ of runAsAdmin
        if (!aclVoter.allowChmod(copy[0])) {
            throw new SecurityViolation("Cannot change permissions for:"
                    + copy[0]);
        }

        // if we reach here, ok to save.
        getSecuritySystem().runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                copy[0].getDetails().setPermissions(perms);
                iUpdate.flush();
            }
        });
    }

    @RolesAllowed("system")
    public boolean[] unlock(final IObject... iObjects) {
        // do nothing if possible
        if (iObjects == null || iObjects.length < 1) {
            return new boolean[] {};
        }

        // create a new session. It's important that we pass in the empty
        // interceptor here, otherwise even root wouldn't be allowed to unlock
        // the instance.
        Session s = SessionFactoryUtils.getNewSession(sf,
                EmptyInterceptor.INSTANCE);

        // similarly, we need to disable certain backend systems. first we
        // disable the UpdateEventListener because it wants to set entities
        // from a different session on these IObjects.
        // See: https://trac.openmicroscopy.org.uk/omero/ticket/366
        getSecuritySystem().disable(UpdateEventListener.UPDATE_EVENT);

        try {
            Long eventId = getSecuritySystem().getEventContext()
                    .getCurrentEventId();
            Event updateEvent = (Event) s.get(Event.class, eventId);

            try {
                boolean[] isUnlocked = new boolean[iObjects.length];
                for (int i = 0; i < iObjects.length; i++) {
                    IObject orig = iObjects[i];

                    // do nothing if possible again.
                    if (orig == null || orig.getId() == null) {
                        isUnlocked[i] = true;
                        continue;
                    }

                    // get the original to operate on
                    final IObject object = (IObject) s.load(orig.getClass(),
                            orig.getId());

                    // if it's not locked, we don't need to look further.
                    if (!object.getDetails().getPermissions()
                            .isSet(Flag.LOCKED)) {
                        isUnlocked[i] = true;
                        continue;
                    }

                    Map<String, Long> counts = getLockingIds(object);
                    long total = counts.get("*");

                    // reporting
                    if (getBeanHelper().getLogger().isDebugEnabled()) {
                        getBeanHelper().getLogger().debug(counts);
                    }

                    // if there are no links, the we can unlock
                    // the actual unlocking happens on flush below.
                    if (total == 0) {
                        object.getDetails().getPermissions().unSet(Flag.LOCKED);
                        object.getDetails().setUpdateEvent(updateEvent);
                        isUnlocked[i] = true;
                    } else {
                        isUnlocked[i] = false;
                    }

                }
                return isUnlocked;
            }

            finally {
                s.flush();
                s.disconnect();
                s.close();
            }

        } finally {
            getSecuritySystem().enable(UpdateEventListener.UPDATE_EVENT);
        }

    }

    public Map<String, Long> getLockingIds(IObject object) {

        // since it's a managed entity it's class.getName() might
        // contain
        // some byte-code generation string
        final Class<? extends IObject> klass = Utils.trueClass(object
                .getClass());

        final long id = object.getId().longValue();

        // the values that could possibly link to this instance.
        final String[][] checks = metadata.getLockChecks(klass);

        // reporting
        final long total[] = new long[] { 0L };
        final Map<String, Long> counts = new HashMap<String, Long>();

        // run the individual queries
        for (final String[] check : checks) {
            final String hql = String.format(
                    "select id from %s where %s%s = :id ", check[0], check[1],
                    ".id");
            this.iQuery.execute(new HibernateCallback() {

                public Object doInHibernate(Session session)
                        throws HibernateException, SQLException {

                    org.hibernate.Query q = session.createQuery(hql);
                    q.setLong("id", id);

                    long count = 0L;
                    Iterator<Long> it = q.iterate();

                    // This is a slower implementation with the intent
                    // that the actual ids will be returned soon.
                    while (it.hasNext()) {
                        Long countedId = it.next();
                        count++;

                    }

                    if (count > 0) {
                        total[0] += count;
                        counts.put(check[0], count);
                    }
                    counts.put("*", total[0]);
                    return null;
                }

            });
        }

        return counts;

    }

    // ~ Passwords
    // =========================================================================

    @RolesAllowed("user")
    public void changePassword(String newPassword) {
        long id = getSecuritySystem().getEventContext().getCurrentUserId();
        changePasswordById(id, newPassword);
    }

    @RolesAllowed("system")
    public void changeUserPassword(String omeName, String newPassword) {
        Experimenter e = lookupExperimenter(omeName);
        changePasswordById(e.getId(), newPassword);
    }

    private void changePasswordById(long id, String newPassword) {
        PasswordUtil.changeUserPasswordById(jdbc, id, newPassword);
        synchronizeLoginCache();
        getBeanHelper().getLogger().info("Changed password for user: " + id);
    }

    /**
     * Jumps through some hurdles (see
     * {@link PasswordUtil#userId(SimpleJdbcTemplate, String)} to not have to
     * use Hibernate in order to prevent unauthorized access to Hibernate.
     */
    public boolean checkPassword(String name, String password) {
        Long id = PasswordUtil.userId(jdbc, name);
        if (null == id) {
            return false; // Unknown user. TODO Guest?
        }
        String hash = PasswordUtil.getUserPasswordHash(jdbc, id);
        if (hash == null) {
            return false; // Password is turned off.
        } else if (hash.trim().length() == 0) {
            return true; // Password is blank. Open for all.
        }

        String digest = PasswordUtil.preparePassword(password);
        return hash.equals(digest);
    }

    // ~ Security context
    // =========================================================================

    @RolesAllowed("user")
    public Roles getSecurityRoles() {
        return getSecuritySystem().getSecurityRoles();
    }

    @RolesAllowed("user")
    public EventContext getEventContext() {
        return new SimpleEventContext(getSecuritySystem().getEventContext());
    }

    // ~ Helpers
    // =========================================================================

    protected Experimenter copyUser(Experimenter e) {
        if (e.getOmeName() == null) {
            throw new ValidationException("OmeName may not be null.");
        }
        Experimenter copy = new Experimenter();
        copy.setOmeName(e.getOmeName());
        copy.setFirstName(e.getFirstName());
        copy.setMiddleName(e.getMiddleName());
        copy.setLastName(e.getLastName());
        copy.setEmail(e.getEmail());
        copy.setInstitution(e.getInstitution());
        if (e.getDetails() != null && e.getDetails().getPermissions() != null) {
            copy.getDetails().setPermissions(e.getDetails().getPermissions());
        }
        // TODO make ShallowCopy-like which ignores collections and details.
        // if possible, values should be validated. i.e. iTypes should say what
        // is non-null
        return copy;
    }

    protected ExperimenterGroup copyGroup(ExperimenterGroup g) {
        if (g.getName() == null) {
            throw new ValidationException("Group name may not be null.");
        }
        ExperimenterGroup copy = new ExperimenterGroup();
        copy.setDescription(g.getDescription());
        copy.setName(g.getName());
        copy.getDetails().copy(getSecuritySystem().newTransientDetails(g));
        // TODO see shallow copy comment on copy user
        return copy;
    }

    protected void assertManaged(IObject o) {
        if (o == null) {
            throw new ApiUsageException("Argument may not be null.");
        } else if (o.getId() == null) {
            throw new ApiUsageException(o.getClass().getName() + " has no id.");
        }
    }

    // ~ Password access
    // =========================================================================

    // ~ Queries for pulling full experimenter/experimenter group graphs
    // =========================================================================

    static abstract class BaseQ<T> extends Query<T> {
        static Definitions defs = new Definitions(new QueryParameterDef("name",
                String.class, true), new QueryParameterDef("id", Long.class,
                true));

        public BaseQ(Parameters params) {
            super(defs, new Parameters(new Filter().unique()).addAll(params));
        }

    }

    static class UserQ extends BaseQ<Experimenter> {
        public UserQ(Parameters params) {
            super(params);
        }

        @Override
        protected void buildQuery(Session session) throws HibernateException,
                SQLException {
            Criteria c = session.createCriteria(Experimenter.class);

            Criteria m = c.createCriteria("groupExperimenterMap",
                    Query.LEFT_JOIN);
            Criteria g = m.createCriteria("parent", Query.LEFT_JOIN);

            if (value("name") != null) {
                c.add(Restrictions.eq("omeName", value("name")));
            }

            else if (value("id") != null) {
                c.add(Restrictions.eq("id", value("id")));
            }

            else {
                throw new InternalException(
                        "Name and id are both null for user query.");
            }
            setCriteria(c);

        }
    }

    static class GroupQ extends BaseQ<ExperimenterGroup> {
        public GroupQ(Parameters params) {
            super(params);
        }

        @Override
        protected void buildQuery(Session session) throws HibernateException,
                SQLException {
            Criteria c = session.createCriteria(ExperimenterGroup.class);
            Criteria m = c.createCriteria("groupExperimenterMap",
                    Query.LEFT_JOIN);
            Criteria e = m.createCriteria("child", Query.LEFT_JOIN);

            if (value("name") != null) {
                c.add(Restrictions.eq("name", value("name")));
            }

            else if (value("id") != null) {
                c.add(Restrictions.eq("id", value("id")));
            }

            else {
                throw new InternalException(
                        "Name and id are both null for group query.");
            }
            setCriteria(c);

        }
    }
}

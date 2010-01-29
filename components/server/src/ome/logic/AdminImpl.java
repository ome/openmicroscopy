    /*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ome.annotations.NotNull;
import ome.annotations.PermitAll;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.annotations.RolesAllowed;
import ome.api.IAdmin;
import ome.api.ServiceInterface;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.AuthenticationException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.security.ACLVoter;
import ome.security.AdminAction;
import ome.security.LdapUtil;
import ome.security.PasswordUtil;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.security.auth.PasswordChangeException;
import ome.security.auth.PasswordProvider;
import ome.security.auth.RoleProvider;
import ome.security.basic.BasicSecuritySystem;
import ome.security.basic.UpdateEventListener;
import ome.services.query.Definitions;
import ome.services.query.Query;
import ome.services.query.QueryParameterDef;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.system.SimpleEventContext;
import ome.tools.hibernate.QueryBuilder;
import ome.util.Utils;

import org.hibernate.Criteria;
import org.hibernate.EmptyInterceptor;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
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
@Transactional
@RevisionDate("$Date:2007-08-20 10:36:07 +0100 (Mon, 20 Aug 2007) $")
@RevisionNumber("$Revision:1754 $")
public class AdminImpl extends AbstractLevel2Service implements LocalAdmin,
        ApplicationContextAware {

    protected final SimpleJdbcOperations jdbc;

    protected final SessionFactory sf;

    protected final MailSender mailSender;

    protected final SimpleMailMessage templateMessage;

    protected final ACLVoter aclVoter;
    
    protected final PasswordProvider passwordProvider;
    
    protected final RoleProvider roleProvider;

    protected OmeroContext context;

    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.context = (OmeroContext) ctx;
    }
    
    public AdminImpl(SimpleJdbcOperations jdbc, SessionFactory sf,
            MailSender mailSender, SimpleMailMessage templateMessage,
            ACLVoter aclVoter, PasswordProvider passwordProvider,
            RoleProvider roleProvider) {
        this.jdbc = jdbc;
        this.sf = sf;
        this.mailSender = mailSender;
        this.templateMessage = templateMessage;
        this.aclVoter = aclVoter;
        this.passwordProvider = passwordProvider;
        this.roleProvider = roleProvider;
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

        final QueryBuilder qb = new QueryBuilder();
        qb.select("g.id").from("ExperimenterGroup", "g");
        qb.join("g.groupExperimenterMap", "m", false, false);
        qb.where();
        qb.and("m.owner = true");
        qb.and("m.parent.id = g.id");
        qb.and("m.child.id = :id");
        qb.param("id", e.getId());

        List<Long> groupIds = iQuery.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                org.hibernate.Query q = qb.query(session);
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

    @RolesAllowed("user")
    // TODO copied from getMemberOfGroupIds
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
    public boolean canUpdate(final IObject obj) {
        
        if (obj == null) {
            throw new ApiUsageException("Argument cannot be null");
        }
        
        Class c = Utils.trueClass(obj.getClass());
        IObject trusted = iQuery.get(c, obj.getId());
        return aclVoter.allowUpdate(trusted, trusted.getDetails());
    }


    @RolesAllowed("user")
    public Experimenter getExperimenter(final long id) {
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
        return iQuery.findAllByQuery("select distinct e from Experimenter e "
                + "left outer join fetch e.groupExperimenterMap m "
                + "left outer join fetch m.parent g", null);
    }

    @Transactional(readOnly = true)
    @RolesAllowed("user")
    public List<Map<String, Object>> lookupLdapAuthExperimenters() {
        return LdapUtil.lookupLdapAuthExperimenters(jdbc);
    }

    @RolesAllowed("user")
    public String lookupLdapAuthExperimenter(long id) {
        return LdapUtil.lookupLdapAuthExperimenter(jdbc, id);
    }

    @RolesAllowed("user")
    public ExperimenterGroup getGroup(long id) {
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
        return iQuery.findAllByQuery("select distinct g from ExperimenterGroup g "
                + "left outer join fetch g.groupExperimenterMap m "
                + "left outer join fetch m.child u "
                + "left outer join fetch u.groupExperimenterMap m2 "
                + "left outer join fetch m2.parent", null);
    }

    @RolesAllowed("user")
    public Experimenter[] containedExperimenters(long groupId) {
        List<Experimenter> experimenters = iQuery.findAllByQuery(
                "select distinct e from Experimenter as e "
                + "join fetch e.groupExperimenterMap as map "
                + "join fetch map.parent g "
                + "where e.id in "
                + "  (select m.child from GroupExperimenterMap m "
                + "  where m.parent.id = :id )", new Parameters()
                        .addId(groupId));
        return experimenters.toArray(new Experimenter[experimenters.size()]);
    }

    @RolesAllowed("user")
    public ExperimenterGroup[] containedGroups(long experimenterId) {
        List<ExperimenterGroup> groups = iQuery
                .findAllByQuery(
                        "select distinct g from ExperimenterGroup as g "
                        + "join fetch g.groupExperimenterMap as map "
                        + "join fetch map.parent e "
                        + "where g.id in "
                        + "  (select m.parent from GroupExperimenterMap m "
                        + "  where m.child.id = :id )",
                        new Parameters().addId(experimenterId));
        return groups.toArray(new ExperimenterGroup[groups.size()]);
    }

    // ~ System-only interface methods
    // =========================================================================

    @RolesAllowed("system")
    public void synchronizeLoginCache() {
        context.publishEvent(new UserGroupUpdateEvent(this));
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
    public void updateExperimenterWithPassword(@NotNull
    Experimenter experimenter, String password) {
        String name = experimenter.getOmeName();
        iUpdate.saveObject(experimenter);
        changeUserPassword(name, password);
        getBeanHelper().getLogger().info(
                "Updated user info and password for " + name);
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
                groupProxy(sec.getSecurityRoles().getUserGroupName()));
    }

    @RolesAllowed("system")
    public long createSystemUser(Experimenter newSystemUser) {
        // logged via createExperimenter
        return createExperimenter(newSystemUser,
                groupProxy(sec.getSecurityRoles().getSystemGroupName()),
                groupProxy(sec.getSecurityRoles().getUserGroupName()));
    }

    @RolesAllowed("system")
    @SuppressWarnings("unchecked")
    public long createExperimenter(Experimenter experimenter,
            ExperimenterGroup defaultGroup, ExperimenterGroup... otherGroups) {
        
        long uid = roleProvider.createExperimenter(experimenter, defaultGroup, otherGroups);
        // If this method passes, then the Experimenter is valid.
        changeUserPassword(experimenter.getOmeName(), " ");
        getBeanHelper().getLogger().info(
                "Created user with blank password: " + experimenter.getOmeName());
        return uid;
    }

    @RolesAllowed("system")
    @SuppressWarnings("unchecked")
    public long createExperimenterWithPassword(Experimenter experimenter,
            String password, ExperimenterGroup defaultGroup,
            ExperimenterGroup... otherGroups) {

        long uid = roleProvider.createExperimenter(experimenter, defaultGroup, otherGroups);
        // If this method passes, then the Experimenter is valid.
        changeUserPassword(experimenter.getOmeName(), password);

        getBeanHelper().getLogger().info(
                "Created user with password: " + experimenter.getOmeName());
        return uid;
    }

    @RolesAllowed("system")
    public long createGroup(ExperimenterGroup group) {
        long gid = roleProvider.createGroup(group);
        getBeanHelper().getLogger().info("Created group: " + group.getName());
        return gid;
    }

    @RolesAllowed("system")
    public void addGroups(final Experimenter user,
            final ExperimenterGroup... groups) {
        
        if (groups == null || groups.length == 0) {
            throw new ValidationException("Nothing to do.");
        }
        
        assertManaged(user);
        for (ExperimenterGroup group : groups) {
            assertManaged(group);
        }

        roleProvider.addGroups(user, groups);
        getBeanHelper().getLogger().info(
                String.format("Added user %s to groups %s", userProxy(
                        user.getId()).getOmeName(), Arrays.asList(groups)));
    }

    @RolesAllowed("system")
    public void removeGroups(Experimenter user, ExperimenterGroup... groups) {
        if (user == null) {
            return;
        }
        if (groups == null) {
            return;
        }
        roleProvider.removeGroups(user, groups);
        getBeanHelper().getLogger().info(
                String.format("Removed user %s from groups %s", user, groups));
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

        roleProvider.setDefaultGroup(user, group);
        getBeanHelper().getLogger().info(
                String.format("Changing default group for %s to %s", user, group));

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
    long experimenterId) {
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

    @RolesAllowed("system")
    public void deleteGroup(ExperimenterGroup group) {
        ExperimenterGroup g = groupProxy(group.getId());

        iUpdate.deleteObject(g);
        getBeanHelper().getLogger().info("Deleted group: " + g.getName());
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
        getSecuritySystem().doAction(new SecureAction(){
            public <T extends IObject> T updateObject(T... objs) {
                iUpdate.flush();
                return null;
            }}, copy);
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

    @PermitAll
    public void reportForgottenPassword(final String name, final String email)
            throws AuthenticationException {

        sec.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                Experimenter e = iQuery.findByString(Experimenter.class,
                        "omeName", name);
                if (e.getEmail() == null) {
                    throw new AuthenticationException(
                            "User has no email address.");
                } else if (!e.getEmail().equals(email)) {
                    throw new AuthenticationException(
                            "Email address does not match.");
                } else if (isDnById(e.getId())) {
                    throw new AuthenticationException(
                            "User is authenticated by LDAP server you cannot reset this password.");
                } else {
                    String passwd = PasswordUtil.generateRandomPasswd();
                    sendEmail(e, passwd);
                    changeUserPassword(e.getOmeName(), passwd);
                }
            }
        });
    }

    private boolean isDnById(long id) {
        String dn = PasswordUtil.getDnById(jdbc, id);
        if (dn != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean sendEmail(Experimenter e, String newPassword) {
        // Create a thread safe "copy" of the template message and customize it
        SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
        msg.setSubject("OMERO - Reset password");
        msg.setTo(e.getEmail());
        msg.setText("Dear " + e.getFirstName() + " " + e.getLastName() + " ("
                + e.getOmeName() + ")" + " your new password is: "
                + newPassword);
        try {
            this.mailSender.send(msg);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Exception: "
                            + ex.getMessage()
                            + ". "
                            + "Password was not changed because email could not be sent "
                            + "to the "
                            + e.getOmeName()
                            + ". Please turn on the debuge "
                            + "mode in omero.properties by the: omero.resetpassword.mail.debug=true");
        }
        return true;
    }
    
    // ~ Password access
    // =========================================================================

    @PermitAll
    public void changeExpiredCredentials(String name, String oldCred,
            String newCred) throws AuthenticationException {
        throw new UnsupportedOperationException();
    }

    @RolesAllowed("user")
    public void changePassword(String newPassword) {
        String user = getSecuritySystem().getEventContext().getCurrentUserName();
        _changePassword(user, newPassword);
    }

    @RolesAllowed("system")
    public void changeUserPassword(String user, String newPassword) {
        _changePassword(user, newPassword);
    }

    private void _changePassword(String user, String newPassword) {
        try {
            passwordProvider.changePassword(user, newPassword);
            getBeanHelper().getLogger().info("Changed password for user: " + user);
        } catch (PasswordChangeException e) {
            throw new SecurityViolation("PasswordChangeException: "
                    + e.getMessage());
        }
    }

    /**
     * Jumps through some hurdles (see
     * {@link PasswordUtil#userId(SimpleJdbcTemplate, String)} to not have to
     * use Hibernate in order to prevent unauthorized access to Hibernate.
     * 
     * If ldap plugin turned, creates Ldap accounts and authentication by LDAP
     * available.
     */
    public boolean checkPassword(String name, String password) {
        Boolean result = passwordProvider.checkPassword(name, password);
        if (result == null) {
            getBeanHelper().getLogger().warn("Password provider returned null: "
                    + passwordProvider);
            return false;
        } else {
            return result.booleanValue();
        }
    }

    // ~ Security context
    // =========================================================================

    @RolesAllowed("user")
    public Roles getSecurityRoles() {
        return getSecuritySystem().getSecurityRoles();
    }

    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public EventContext getEventContext() {
        return new SimpleEventContext(getSecuritySystem().getEventContext());
    }

    // ~ Helpers
    // =========================================================================

    protected void assertManaged(IObject o) {
        if (o == null) {
            throw new ApiUsageException("Argument may not be null.");
        } else if (o.getId() == null) {
            throw new ApiUsageException(o.getClass().getName() + " has no id.");
        }
    }

    // ~ Queries for pulling full experimenter/experimenter group graphs
    // =========================================================================

    static abstract class BaseQ<T> extends Query<T> {
        static Definitions defs = new Definitions(new QueryParameterDef("name",
                String.class, true), new QueryParameterDef("id", Long.class,
                true));

        public BaseQ(Parameters params) {
            super(defs, new Parameters().unique().addAll(params));
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
            
            QueryBuilder qb = new QueryBuilder();
            qb.select("g");
            qb.from("ExperimenterGroup", "g");
            qb.join("g.groupExperimenterMap","m",true, true);
            qb.join("m.child","user", true,true);
            qb.where();
            
            Object name = value("name");
            Object id = value("id");
            
            if (name != null) {
                qb.and("g.name = :name");
                qb.param("name", name);
            }
            
            else if (id != null) {
                qb.and("g.id = :id");
                qb.param("id",id);
            }
            
            else {
                throw new InternalException(
                        "Name and id are both null for group query.");
            }
            setQuery(qb.query(session));

        }
    }
}

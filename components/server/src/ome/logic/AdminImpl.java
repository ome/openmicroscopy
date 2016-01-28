    /*
 *   $Id$
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ome.annotations.NotNull;
import ome.annotations.PermitAll;
import ome.annotations.RolesAllowed;
import ome.api.IAdmin;
import ome.api.RawFileStore;
import ome.api.ServiceInterface;
import ome.api.local.LocalAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.AuthenticationException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IGlobal;
import ome.model.IObject;
import ome.model.annotations.ExperimenterAnnotationLink;
import ome.model.annotations.FileAnnotation;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.enums.ChecksumAlgorithm;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.parameters.Parameters;
import ome.security.ACLVoter;
import ome.security.AdminAction;
import ome.security.ChmodStrategy;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.security.auth.PasswordChangeException;
import ome.security.auth.PasswordProvider;
import ome.security.auth.PasswordUtil;
import ome.security.auth.RoleProvider;
import ome.security.basic.BasicSecuritySystem;
import ome.services.query.Definitions;
import ome.services.query.Query;
import ome.services.query.QueryParameterDef;
import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.system.SimpleEventContext;
import ome.tools.hibernate.QueryBuilder;
import ome.tools.hibernate.SecureMerge;
import ome.tools.hibernate.SessionFactory;
import ome.util.SqlAction;
import ome.util.Utils;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumType;

/**
 * Provides methods for administering user accounts, passwords, as well as
 * methods which require special privileges.
 *
 * Developer note: As can be expected, to perform these privileged the Admin
 * service has access to several resources that should not be generally used
 * while developing services. Misuse could circumvent security or auditing.
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @see SecuritySystem
 * @see Permissions
 * @since 3.0-M3
 */
@Transactional(readOnly = true)
public class AdminImpl extends AbstractLevel2Service implements LocalAdmin,
        ApplicationContextAware {

    protected final SqlAction sql;

    protected final SessionFactory osf;

    protected final MailSender mailSender;

    protected final SimpleMailMessage templateMessage;

    protected final ACLVoter aclVoter;
    
    protected final PasswordProvider passwordProvider;
    
    protected final RoleProvider roleProvider;

    protected final PasswordUtil passwordUtil;

    protected final LdapImpl ldapUtil;

    protected final ChmodStrategy chmod;

    protected final ChecksumProviderFactory cpf;

    protected OmeroContext context;

    public void setApplicationContext(ApplicationContext ctx)
            throws BeansException {
        this.context = (OmeroContext) ctx;
    }

    public AdminImpl(SqlAction sql, SessionFactory osf,
            MailSender mailSender, SimpleMailMessage templateMessage,
            ACLVoter aclVoter, PasswordProvider passwordProvider,
            RoleProvider roleProvider, LdapImpl ldapUtil, PasswordUtil passwordUtil,
            ChmodStrategy chmod, ChecksumProviderFactory cpf) {
        this.sql = sql;
        this.osf = osf;
        this.mailSender = mailSender;
        this.templateMessage = templateMessage;
        this.aclVoter = aclVoter;
        this.passwordProvider = passwordProvider;
        this.roleProvider = roleProvider;
        this.ldapUtil = ldapUtil;
        this.passwordUtil = passwordUtil;
        this.chmod = chmod;
        this.cpf = cpf;
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
                roleProvider.isIgnoreCaseLookup() ? omeName.toLowerCase()
                        : omeName);

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

        List<Long> groupIds = iQuery.execute(new HibernateCallback<List<Long>>() {
            public List<Long> doInHibernate(Session session)
                    throws HibernateException, SQLException {
                org.hibernate.Query q = qb.query(session);
                return (List<Long>) q.list();
            }
        });
        return groupIds;
    }

    @SuppressWarnings("unchecked")
    @RolesAllowed("user")
    public List<Long> getMemberOfGroupIds(final Experimenter e) {
        return (List<Long>) getGroupField(e, "id");
    }

    @SuppressWarnings("unchecked")
    @RolesAllowed("user")
    public List<String> getUserRoles(final Experimenter e) {
        return (List<String>) getGroupField(e, "name");
    }

    @SuppressWarnings("rawtypes")
    private List getGroupField(final Experimenter e, final String name) {
        Assert.notNull(e);
        Assert.notNull(e.getId());

        List<String> groupNames = iQuery.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                org.hibernate.Query q = session
                        .createQuery("select m.parent." + name + " " +
                            "from Experimenter e " +
                            "join e.groupExperimenterMap m " +
                            "where e.id = :id order by index(m)");
                q.setParameter("id", e.getId());
                return q.list();
            }
        });
        return groupNames;
    }

    // ~ User accessible interface methods
    // =========================================================================

    @RolesAllowed("user")
    public boolean canAnnotate(final IObject obj) {

        if (obj == null) {
            throw new ApiUsageException("Argument cannot be null");
        }

        final Class<? extends IObject> c = Utils.trueClass(obj.getClass());
        IObject trusted = iQuery.get(c, obj.getId());
        return aclVoter.allowAnnotate(trusted, trusted.getDetails());
    }

    @RolesAllowed("user")
    public boolean canUpdate(final IObject obj) {
        
        if (obj == null) {
            throw new ApiUsageException("Argument cannot be null");
        }
        
        final Class<? extends IObject> c = Utils.trueClass(obj.getClass());
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
                "name",
                roleProvider.isIgnoreCaseLookup() ? omeName.toLowerCase()
                        : omeName)));

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

    @RolesAllowed("user")
    public List<Map<String, Object>> lookupLdapAuthExperimenters() {
        return ldapUtil.lookupLdapAuthExperimenters();
    }

    @RolesAllowed("user")
    public String lookupLdapAuthExperimenter(long id) {
        return ldapUtil.lookupLdapAuthExperimenter(id);
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
                        + "left outer join fetch map.child u "
                        + "left outer join fetch u.groupExperimenterMap m2 "
                        + "where g.id in "
                        + "  (select m.parent from GroupExperimenterMap m "
                        + "  where m.child.id = :id )",
                        new Parameters().addId(experimenterId));
        return groups.toArray(new ExperimenterGroup[groups.size()]);
    }

    // ~ System-only interface methods
    // =========================================================================

    @RolesAllowed("system")
    @Transactional(readOnly = false)
    public void synchronizeLoginCache() {

        final Logger log = getBeanHelper().getLogger();
        final List<Map<String, Object>> dnIds = ldapUtil.lookupLdapAuthExperimenters();

        if (dnIds.size() > 0) {
            log.info("Synchronizing " + dnIds.size() + " ldap user(s)");
        }


        for (Map<String, Object> dnId: dnIds) {
            String dn = (String) dnId.get("dn");
            Long id = (Long) dnId.get("experimenter_id");
            try {
                Experimenter e = userProxy(id);
                ldapUtil.synchronizeLdapUser(e.getOmeName());
            } catch (ApiUsageException aue) {
                // User likely doesn't exist
                log.debug("User not found: " + dn);
            } catch (Exception e) {
                log.error("synchronizeLdapUser:" + dnId, e);
            }
        }
        context.publishEvent(new UserGroupUpdateEvent(this));
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
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

    protected static final String NSEXPERIMENTERPHOTO = "openmicroscopy.org/omero/experimenter/photo";

    public List<OriginalFile> getMyUserPhotos() {
        Parameters parameters = new Parameters();
        parameters.addId(getEventContext().getCurrentUserId());
        parameters.addString("ns", NSEXPERIMENTERPHOTO);
        List<OriginalFile> photos = iQuery.findAllByQuery(
                "select f from Experimenter e join e.annotationLinks l " +
			"join l.child a join a.file f where e.id = :id and a.ns = :ns",
			parameters);
        return photos;
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public long uploadMyUserPhoto(String filename, String mimetype, byte[] data) {

        Long uid = getEventContext().getCurrentUserId();
        List<OriginalFile> photos = getMyUserPhotos();
        OriginalFile file = null;
        if (photos.size() > 0) {
            file = photos.get(0);
        }

        if (file == null) {
            file = new OriginalFile();
            file.setName(filename);
            file.setPath(filename); // FIXME this should be something like /users/<name>/photo
            file.setSize((long) data.length);
            file.setHasher(new ChecksumAlgorithm("SHA1-160"));
            file.setHash(cpf.getProvider(ChecksumType.SHA1).putBytes(data)
                    .checksumAsString());
            file.setMimetype(mimetype);
            FileAnnotation fa = new FileAnnotation();
            fa.setNs(NSEXPERIMENTERPHOTO);
            fa.setFile(file);
            ExperimenterAnnotationLink link = new ExperimenterAnnotationLink();
            link.link(new Experimenter(uid, false), fa);
            link = iUpdate.saveAndReturnObject(link);
            fa = (FileAnnotation) link.getChild();
            file = fa.getFile();
            internalMoveToCommonSpace(file);
            internalMoveToCommonSpace(fa);
            internalMoveToCommonSpace(link);
        } else {
            file.setName(filename);
            file.setPath(filename);
            file.setMimetype(mimetype);
            file = iUpdate.saveAndReturnObject(file);
        }

        RawFileStore rfs = (RawFileStore) context.getBean("internal-ome.api.RawFileStore");
        try {
            rfs.setFileId(file.getId());
            rfs.write(data, 0, data.length);
            file = rfs.save();
        } finally {
            rfs.close();
        }

        return file.getId();

    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void updateExperimenter(@NotNull final
    Experimenter experimenter) {

        try {
            adminOrPiOfUser(experimenter);
            String name = experimenter.getOmeName();
            copyAndSaveExperimenter(experimenter);
            getBeanHelper().getLogger().info("Updated user info for " + name);
        } catch (SecurityViolation sv) {
            final Long currentID = getEventContext().getCurrentUserId();
            final Long experimenterID = experimenter.getId();

            // If we're not an admin, allow for the possibility
            // of delegating to updateSelf.
            if (currentID.equals(experimenterID)) {
                updateSelf(experimenter);
            } else {
                // But throw if that's not the case.
                throw sv;
            }
        }
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void updateExperimenterWithPassword(@NotNull final
    Experimenter experimenter, final String password) {
        adminOrPiOfUser(experimenter);
        copyAndSaveExperimenter(experimenter);
        final Experimenter orig = userProxy(experimenter.getId());
        String name = orig.getOmeName();
        changeUserPassword(name, password);
        getBeanHelper().getLogger().info(
                "Updated user info and password for " + name);
    }

    /**
     * @param experimenter
     */
    private void copyAndSaveExperimenter(final Experimenter experimenter) {
        final Experimenter orig = userProxy(experimenter.getId());
        final String origOmeName = orig.getOmeName();
        final String newOmeName = experimenter.getOmeName();
        if (!origOmeName.equals(newOmeName)) {
            final Roles roles = getSecurityRoles();
            final Set<String> fixedExperimenterNames =
                    ImmutableSet.of(roles.getRootName(), roles.getGuestName());
            if (fixedExperimenterNames.contains(origOmeName)) {
                throw new ValidationException("cannot change name of special experimenter '" + origOmeName + "'");
            } else if (fixedExperimenterNames.contains(newOmeName)) {
                throw new ValidationException("cannot change name to special experimenter '" + newOmeName + "'");
            }
        }
        orig.setOmeName(newOmeName);
        orig.setEmail(experimenter.getEmail());
        orig.setFirstName(experimenter.getFirstName());
        orig.setMiddleName(experimenter.getMiddleName());
        orig.setLastName(experimenter.getLastName());
        orig.setInstitution(experimenter.getInstitution());
        reallySafeSave(orig);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void updateGroup(@NotNull final
    ExperimenterGroup group) {
        adminOrPiOfGroup(group);
        Permissions p = group.getDetails().getPermissions();
        if (p != null) {
            // Setting permissions is not allowed via IUpdate
            // so use the logic in changePermissions and then
            // reset permissions to the current value.
            changePermissions(group, p); // ticket:1776 WORKAROUND
        }
        final ExperimenterGroup orig = getGroup(group.getId());
        final String origName = orig.getName();
        final String newName = group.getName();
        if (!origName.equals(newName)) {
            final Roles roles = getSecurityRoles();
            final Set<String> fixedGroupNames =
                    ImmutableSet.of(roles.getGuestGroupName(), roles.getSystemGroupName(), roles.getUserGroupName());
            if (fixedGroupNames.contains(origName)) {
                throw new ValidationException("cannot change name of special group '" + origName + "'");
            } else if (fixedGroupNames.contains(newName)) {
                throw new ValidationException("cannot change name to special group '" + newName + "'");
            }
            if (group.getId().equals(getEventContext().getCurrentGroupId())) {
                throw new ValidationException("cannot rename the current group context '" + origName + "'");
            }
        }
        orig.setName(newName);
        orig.setDescription(group.getDescription());

        reallySafeSave(orig);
        getBeanHelper().getLogger().info("Updated group info for " + group);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public long createUser(final Experimenter newUser, String defaultGroup) {
        // logged via createExperimenter

        final ExperimenterGroup proxy = groupProxy(defaultGroup);
        // logged & secured via createExperimenter
        return createExperimenter(newUser, proxy, groupProxy(sec
                    .getSecurityRoles().getUserGroupName()));

    }

    @RolesAllowed("system")
    @Transactional(readOnly = false)
    public long createSystemUser(Experimenter newSystemUser) {
        // logged & secured via createExperimenter
        return createExperimenter(newSystemUser,
                groupProxy(sec.getSecurityRoles().getSystemGroupName()),
                groupProxy(sec.getSecurityRoles().getUserGroupName()));
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public long createExperimenter(final Experimenter experimenter,
            ExperimenterGroup defaultGroup, ExperimenterGroup... otherGroups) {

        adminOrPiOfNonUserGroups(defaultGroup, otherGroups);
        
        long uid = roleProvider.createExperimenter(experimenter, defaultGroup, otherGroups);
        // If this method passes, then the Experimenter is valid.
        changeUserPassword(experimenter.getOmeName(), " ");
        getBeanHelper().getLogger().info(
                "Created user with blank password: "
                + experimenter.getOmeName());
        return uid;
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public long createExperimenterWithPassword(final Experimenter experimenter,
            final String password, final ExperimenterGroup defaultGroup,
            final ExperimenterGroup... otherGroups) {

        adminOrPiOfNonUserGroups(defaultGroup, otherGroups);

        long uid = roleProvider.createExperimenter(experimenter,
                        defaultGroup, otherGroups);
        // If this method passes, then the Experimenter is valid.
        changeUserPassword(experimenter.getOmeName(), password);
        getBeanHelper().getLogger().info(
                "Created user with password: " + experimenter.getOmeName());
        return uid;
    }

    @RolesAllowed("system")
    @Transactional(readOnly = false)
    public long createGroup(ExperimenterGroup group) {
        long gid = roleProvider.createGroup(group);
        getBeanHelper().getLogger().info("Created group: " + group.getName());
        return gid;
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void addGroups(final Experimenter user,
            final ExperimenterGroup... groups) {
        
        if (groups == null || groups.length == 0) {
            throw new ValidationException("Nothing to do.");
        }
        
        assertManaged(user);
        for (ExperimenterGroup group : groups) {
            assertManaged(group);
        }

        adminOrPiOfGroups(null, groups);
        roleProvider.addGroups(user, groups);

        getBeanHelper().getLogger().info(
                String.format("Added user %s to groups %s", userProxy(
                        user.getId()).getOmeName(), Arrays.asList(groups)));
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void removeGroups(final Experimenter user, final ExperimenterGroup... groups) {
        if (user == null) {
            return;
        }
        if (groups == null) {
            return;
        }

        adminOrPiOfGroups(null, groups);

        final Roles roles = getSecurityRoles();
        final boolean removeSystemOrUser =
                Iterators.any(Iterators.forArray(groups),Predicates.or(roles.IS_SYSTEM_GROUP, roles.IS_USER_GROUP));
        if (removeSystemOrUser && roles.isRootUser(user)) {
            throw new ValidationException("experimenter '" + roles.getRootName() + "' may not be removed from the '" +
                roles.getSystemGroupName() + "' or '" + roles.getUserGroupName() + "' group");
        }
        final EventContext eventContext = getEventContext();
        final boolean userOperatingOnThemself = eventContext.getCurrentUserId().equals(user.getId());
        if (removeSystemOrUser && userOperatingOnThemself) {
            throw new ValidationException("experimenters may not remove themselves from the '" +
                roles.getSystemGroupName() + "' or '" + roles.getUserGroupName() + "' group");
        }

        /* The properly loaded user object is needed for collecting the group-experimenter map. */
        final Experimenter loadedUser = userProxy(user.getId());

        final Set<Long> resultingGroupIds = new HashSet<Long>();
        for (final GroupExperimenterMap map : loadedUser.<GroupExperimenterMap>collectGroupExperimenterMap(null)) {
            resultingGroupIds.add(map.parent().getId());
        }
        for (final ExperimenterGroup group : groups) {
            resultingGroupIds.remove(group.getId());
        }
        if (resultingGroupIds.isEmpty()) {
            throw new ValidationException("experimenter must remain a member of some group");
        } else if (resultingGroupIds.equals(Collections.singleton(roles.getUserGroupId()))) {
            throw new ValidationException("experimenter cannot be a member of only the '" +
                roles.getUserGroupName() + "' group, a different default group is also required");
        }
        roleProvider.removeGroups(user, groups);

        getBeanHelper().getLogger().info(
                String.format("Removed user %s from groups %s", user, groups));
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
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

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void setGroupOwner(final ExperimenterGroup group, final Experimenter owner) {
        adminOrPiOfGroup(group);
        toggleGroupOwner(group, owner, Boolean.TRUE);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void unsetGroupOwner(final ExperimenterGroup group, final Experimenter owner) {
        adminOrPiOfGroup(group);
        toggleGroupOwner(group, owner, Boolean.FALSE);
    }
    
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void addGroupOwners(final ExperimenterGroup group, final Experimenter... owner) {
        adminOrPiOfGroup(group);
        for (Experimenter o : owner) {
            toggleGroupOwner(group, o, Boolean.TRUE);
        }
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void removeGroupOwners(final ExperimenterGroup group, final Experimenter... owner) {
        adminOrPiOfGroup(group);
        for (Experimenter o : owner) {
            toggleGroupOwner(group, o, Boolean.FALSE);
        }
    }

    private void toggleGroupOwner(ExperimenterGroup group, Experimenter owner,
            Boolean value) {

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

        GroupExperimenterMap m = findLink(group, owner);
        if (m == null) {
            addGroups(owner, group);
            m = findLink(group, owner);
        }
        m.setOwner(value);
        getSecuritySystem().runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                iUpdate.flush();
            }
        });
        getBeanHelper().getLogger().info(
                String.format("%s user %s as owner of group %s",
                        value ? "Setting" : "Unsetting",
                                owner.getId(), group.getId()));
    }

    private GroupExperimenterMap findLink(ExperimenterGroup group,
            Experimenter owner) {
        GroupExperimenterMap m = iQuery.findByQuery(
                "select m from GroupExperimenterMap m " +
			"where m.parent.id = :pid " +
			"and m.child.id = :cid",
			new Parameters()
                    .addLong("pid", group.getId())
                    .addLong("cid", owner.getId()));
        return m;
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

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void deleteExperimenter(Experimenter user) {

        adminOrPiOfUser(user);

        final Experimenter e = userProxy(user.getId());
        int count = sql.removePassword(e.getId());

        if (count == 0) {
            getBeanHelper().getLogger().info(
                    "No password found for user " + e.getOmeName()
                            + ". Cannot delete.");
        }

        getSecuritySystem().runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                iUpdate.deleteObject(e);
            }
        });
        getBeanHelper().getLogger().info("Deleted user: " + e.getOmeName());
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void deleteGroup(ExperimenterGroup group) {

        adminOrPiOfGroup(group);

        final ExperimenterGroup g = groupProxy(group.getId());

        getSecuritySystem().runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                iUpdate.deleteObject(g);
            }
        });
        getBeanHelper().getLogger().info("Deleted group: " + g.getName());
    }

    // ~ chown / chgrp / chmod
    // =========================================================================

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void changeOwner(IObject iObject, String omeName) {
        // should take an Owner
        IObject copy = iQuery.get(iObject.getClass(), iObject.getId());
        Experimenter owner = userProxy(omeName);
        copy.getDetails().setOwner(owner);
        iUpdate.saveObject(copy);
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void changeGroup(IObject iObject, String groupName) {
        // should take a group
        final IObject copy = iQuery.get(iObject.getClass(), iObject.getId());
        final ExperimenterGroup group = groupProxy(groupName);

        // Check object
        final EventContext ec = getSecuritySystem().getEventContext();
        if (!ec.getCurrentUserId().equals(copy.getDetails().getOwner().getId())
                && !ec.isCurrentUserAdmin()) {
            throw new SecurityViolation("Cannot change group for:" + iObject);
        }
        
        // Check target group
        if (getSecurityRoles().getUserGroupId() == group.getId().longValue()) {
            throw new SecurityViolation("Use moveToCommonSpace for moving to user group");
        } else if (!ec.getMemberOfGroupsList().contains(group.getId())) {
            throw new SecurityViolation("Can't change to group; " +
            		"not a member of " + group.getId());
        }
        
        // make change.
        copy.getDetails().setGroup(group);
        secureFlush(copy);
        
        if (copy instanceof Image) {
            Image img = (Image) copy;
            Iterator<Pixels> it = img.iteratePixels();
            while (it.hasNext()) {
                Pixels pix = it.next();
                pix.getDetails().setGroup(group);
                secureFlush(pix);
            }
        }

        // Detect group mismatch
        // What would need to be changed?
        @SuppressWarnings("unchecked")
        Map<String, Long> locks = getLockingIds(
                (Class<IObject>) copy.getClass(), copy.getId(), group.getId());

        Long total = locks.get("*");
        if (total != null && total > 0) {
            throw new SecurityViolation("Locks: " + locks);
        }
        
    }

    private void secureFlush(final IObject copy) {
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
     * As of OMERO 4.2 (ticket:1434), this method has special handling for an
     * instance of {@link ExperimenterGroup} and <em>limited</em> capabilities
     * for changing any other object type (ticket:1776).
     *
     * For groups, the permission changes will be propagated to all the
     * contained objects. For other objects, changes may not override group
     * settings.
     *
     * @see IAdmin#changePermissions(IObject, Permissions)
     * @see <a
     *      href="http://trac.openmicroscopy.org.uk/ome/ticket/293">ticket:293</a>
     * @see <a
     *      href="http://trac.openmicroscopy.org.uk/ome/ticket/1434">ticket:1434</a>
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void changePermissions(final IObject iObject, final Permissions perms) {
        final Session s = osf.getSession();
        final String p = perms.toString();
        final Object[] checks = chmod.getChecks(iObject, p);
        chmod.chmod(iObject, p);
        for (Object check : checks) {
            chmod.check(iObject, check);
        }
    }

    @RolesAllowed("user")
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = false)
    public void moveToCommonSpace(IObject... iObjects) {
        // ticket:1794
        for (IObject object : iObjects) {
            if (object != null) {
                Long id = object.getId();
                Class<IObject> c = (Class<IObject>) Utils.trueClass(object.getClass());
                IObject o = iQuery.get(c, id);
                ExperimenterGroup g = o.getDetails().getGroup();
                if (!g.getId().equals(getSecurityRoles().getUserGroupId())) {
                    adminOrPiOfGroup(g);
                    internalMoveToCommonSpace(o);
                }
            }
        }
    }

    /**
     * Helpers which unconditionally moves the object to the common space. This
     * can be used by other methods like {@link #uploadMyUserPhoto(String, String, byte[])}
     *
     * @param obj a model object, linked to the current session; never {@code null}
     */
    public void internalMoveToCommonSpace(IObject obj) {
        /* Can this next line be removed? - ajp */
        final Session session = osf.getSession();
        obj.getDetails().setGroup(
                groupProxy(getSecurityRoles().getUserGroupId()));
        secureFlush(obj);
        getBeanHelper().getLogger().info("Moved object to common space: " + obj);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> getLockingIds(IObject object) {
        return getLockingIds((Class<IObject>) object.getClass(), object.getId(), null);
    }
    
    public Map<String, Long> getLockingIds(final Class<IObject> type,
            final long id, final Long groupId) {

        String groupClause = "";
        if (groupId != null) {
            groupClause = "and details.group.id <> " + groupId;
        }
        
        // since it's a managed entity it's class.getName() might
        // contain
        // some byte-code generation string
        final Class<? extends IObject> klass = Utils.trueClass(type);

        // the values that could possibly link to this instance.
        final String[][] checks = metadata.getLockChecks(klass);
        return this.metadata.countLocks(osf.getSession(), id, checks, groupClause);

    }

    // ~ Passwords
    // =========================================================================

    @PermitAll
    @Transactional(readOnly = false)
    public void reportForgottenPassword(final String name, final String email)
            throws AuthenticationException {

        if (name == null) {
            throw new IllegalArgumentException("Unexpected null username.");
        }
        if (email == null) {
            throw new IllegalArgumentException("Unexpected null e-mail.");
        }
        sec.runAsAdmin(new AdminAction() {
            public void runAsAdmin() {
                Experimenter e = iQuery.findByString(Experimenter.class,
                        "omeName", name);
                if (e == null) {
                    throw new AuthenticationException("Unknown user.");
                } else if (e.getEmail() == null) {
                    throw new AuthenticationException(
                            "User has no email address.");
                } else if (!e.getEmail().equals(email)) {
                    throw new AuthenticationException(
                            "Email address does not match.");
                } else if (passwordUtil.getDnById(e.getId())) {
                    throw new AuthenticationException(
                            "User is authenticated by LDAP server you cannot reset this password.");
                } else {
                    String passwd = passwordUtil.generateRandomPasswd();
                    sendEmail(e, passwd);
                    // changeUserPassword checks adminOrPiOfUser
                    // Skipping that. See #7327
                    _changePassword(e.getOmeName(), passwd);
                }
            }
        });
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
                            + "to user:"
                            + e.getOmeName()
                            + ". Please turn on the debug "
                            + "mode in omero.properties by the: omero.mail.debug=true");
        }
        return true;
    }
    
    // ~ Password access
    // =========================================================================

    @PermitAll
    @Transactional(readOnly = false)
    public void changeExpiredCredentials(String name, String oldCred,
            String newCred) throws AuthenticationException {
        throw new UnsupportedOperationException();
    }

    @RolesAllowed({"user", "HasPassword"})
    @Transactional(readOnly = false)
    public void changePassword(String newPassword) {
        String user = getSecuritySystem().getEventContext().getCurrentUserName();
        _changePassword(user, newPassword);
    }

    @RolesAllowed({"user"})
    @Transactional(readOnly = false)
    public void changePasswordWithOldPassword(String oldPassword, String newPassword) {
        String user = getSecuritySystem().getEventContext().getCurrentUserName();
        if (!checkPassword(user, oldPassword, false)) {
            throw new SecurityViolation("Old password is invalid");
        }
        _changePassword(user, newPassword);
    }

    @RolesAllowed({"user", "HasPassword"})
    @Transactional(readOnly = false)
    public void changeUserPassword(final String user, final String newPassword) {
        adminOrPiOfUser(userProxy(user));
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
     * If ldap plugin turned, creates Ldap accounts and authentication by LDAP
     * available.
     */
    public boolean checkPassword(String name, String password, boolean readOnly) {
        Boolean result = passwordProvider.checkPassword(name, password, readOnly);
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

    @PermitAll
    public Roles getSecurityRoles() {
        return getSecuritySystem().getSecurityRoles();
    }

    @PermitAll
    public EventContext getEventContext() {
        return new SimpleEventContext(getSecuritySystem().getEventContext(true));
    }

    public EventContext getEventContextQuiet() {
        return new SimpleEventContext(getSecuritySystem().getEventContext(false));
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

            /* Should these calls be using g not c? - ajp */
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

    // ~ group permissions
    // =========================================================================

    private Set<String> classes() {
        return getExtendedMetadata().getClasses();
    }

    private String table(String className) {
        try {
            Class<?> c = Class.forName(className);
            Table t = null;
            if (IGlobal.class.isAssignableFrom(c)) {
                return null;
            } else if (c.getAnnotation(Table.class) == null) {
                return null;
            } else if (c.getAnnotation(PrimaryKeyJoinColumn.class) != null) {
                return null;
            } else {
                t = c.getAnnotation(Table.class);
            }

            return t.name();
        } catch (Exception e) {
            throw new InternalException("Miss configuration. Should never happen.");
        }
    }

    // ticket:1781 - group-owner admin privileges
    // =========================================================================

    /**
     * Saves an object as admin.
     *
     * Due to the disabling of the MergeEventListener, it is necessary to
     * jump through several hoops to get non-admin saving of system types
     * to work properly.
     */
    private void reallySafeSave(final IObject obj) {
        final Session session = osf.getSession();
        sec.doAction(new SecureMerge(session), obj);
        sec.runAsAdmin(new AdminAction(){
            public void runAsAdmin() {
                session.flush();
            }});
    }

    private boolean isAdmin() {
        return getEventContext().isCurrentUserAdmin();
    }

    private boolean isPiOf(Experimenter user) {
        if (user == null) {
            return true;
        }
        List<Long> userIn = getMemberOfGroupIds(user);
        List<Long> piOf = getEventContext().getLeaderOfGroupsList();
        for (Long id : piOf) {
            if (userIn.contains(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPiOf(ExperimenterGroup group) {
        if (group == null) {
            return true;
        }

        EventContext ec = getEventContext();
        List<Long> piOf = ec.getLeaderOfGroupsList();
        return piOf.contains(group.getId());
    }

    private void throwNonAdminOrPi() {
        String msg = "Current user is neither admin nor group-leader for " +
            "the given user(s)/group(s)";
        throw new SecurityViolation(msg);
    }

    private void adminOrPiOfUser(Experimenter user) {
        if (!isAdmin() && ! isPiOf(user)) {
            throwNonAdminOrPi();
        }
    }

    private void adminOrPiOfGroup(ExperimenterGroup group) {
        if (!isAdmin() && ! isPiOf(group)) {
            throwNonAdminOrPi();
        }
    }

    private void adminOrPiOfGroups(ExperimenterGroup group, ExperimenterGroup ... groups) {
        if (!isAdmin()) {
            if (!isPiOf(group)) {
                throwNonAdminOrPi();
            } else {
                for (ExperimenterGroup g : groups) {
                    if (!isPiOf(g)) {
                        throwNonAdminOrPi();
                    }
                }
            }

        }
    }

    /**
     * Filters out the "user" group since it is unlikely that anyone will be an
     * owner of it.
     *
     * @param defaultGroup
     * @param otherGroups
     */
    private void adminOrPiOfNonUserGroups(ExperimenterGroup defaultGroup,
            ExperimenterGroup... otherGroups) {
        Set<ExperimenterGroup> nonUserGroupGroups = new HashSet<ExperimenterGroup>();
        for (ExperimenterGroup eg : otherGroups) {
            if (!eg.getId().equals(getSecurityRoles().getUserGroupId())) {
                nonUserGroupGroups.add(eg);
            }
        }
        adminOrPiOfGroups(defaultGroup,
                nonUserGroupGroups.toArray(new ExperimenterGroup[0]));
    }

}

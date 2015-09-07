/*
 *   $Id$
 *
 *   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.tools.hibernate.HibernateUtils;
import ome.tools.hibernate.SecureMerge;
import ome.tools.hibernate.SessionFactory;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link RoleProvider}.
 *
 * Note: All implementations were originally copied from AdminImpl for
 * ticket:1226.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */

public class SimpleRoleProvider implements RoleProvider {

    final private static Logger log = LoggerFactory.getLogger(SimpleRoleProvider.class);

    final protected SecuritySystem sec;

    final protected SessionFactory sf;

    private AtomicBoolean ignoreCaseLookup;

    public SimpleRoleProvider(SecuritySystem sec, SessionFactory sf) {
        this(sec, sf, new AtomicBoolean(false));
    }

    public SimpleRoleProvider(SecuritySystem sec, SessionFactory sf,
            AtomicBoolean ignoreCaseLookup) {
        this.sec = sec;
        this.sf = sf;
        this.ignoreCaseLookup = ignoreCaseLookup;
    }

    public String nameById(long id) {
        Session s = sf.getSession();
        return (String) s.createQuery(
                "select omeName from Experimenter where id = :id")
                .setParameter("id", id).uniqueResult();
    }

    public long createGroup(String name, Permissions perms, boolean strict) {
        return this.createGroup(name, perms, strict, false);
    }

    public long createGroup(String name, Permissions perms, boolean strict,
            boolean isLdap) {
        Session s = sf.getSession();
        ExperimenterGroup g = groupByName(name, s);

        if (g == null) {
            g = new ExperimenterGroup();
            g.setName(name);
            g.setLdap(isLdap);
            if (perms == null) {
                perms = Permissions.USER_PRIVATE; // ticket:1434
            }
            g.getDetails().setPermissions(perms);
            g = (ExperimenterGroup) s.merge(g);
        } else {
            if (strict) {
                throw new ValidationException("Group already exists: " + name);
            }
        }
        return g.getId();
    }

    public long createGroup(ExperimenterGroup group) {

        group = copyGroup(group);
        if (group.getDetails().getPermissions() == null) {
            group.getDetails().setPermissions(Permissions.USER_PRIVATE);
        }

        final Session session = sf.getSession();
        ExperimenterGroup g = sec.doAction(new SecureMerge(session), group);
        return g.getId();
    }

    public long createExperimenter(Experimenter experimenter,
            ExperimenterGroup defaultGroup, ExperimenterGroup... otherGroups) {

        Session session = sf.getSession();

        SecureAction action = new SecureMerge(session);

        Experimenter e = copyUser(experimenter);
        if (isIgnoreCaseLookup()) {
            e.setOmeName(e.getOmeName().toLowerCase());
        }
        e.getDetails().copy(sec.newTransientDetails(e));
        e = sec.doAction(action, e);
        session.flush();

        linkGroupAndUser(defaultGroup, e, false);
        if (null != otherGroups) {
            for (ExperimenterGroup group : otherGroups) {
                linkGroupAndUser(group, e, false);
            }
        }

        return e.getId();
    }

    public void setDefaultGroup(Experimenter user, ExperimenterGroup group) {
        Session session = sf.getSession();
        Experimenter foundUser = userById(user.getId(), session);
        ExperimenterGroup foundGroup = groupById(group.getId(), session);
        Set<GroupExperimenterMap> foundMaps = foundUser
                .findGroupExperimenterMap(foundGroup);
        if (foundMaps.size() < 1) {
            throw new ApiUsageException("Group " + group.getId() + " was not "
                    + "found for user " + user.getId());
        } else if (foundMaps.size() > 1) {
            log.warn(foundMaps.size() + " copies of " + foundGroup
                    + " found for " + foundUser);
        } else {
            // May throw an exception
            GroupExperimenterMap newDef = foundMaps.iterator().next();
            log.info(String.format("Changing default group for user %s to %s",
                    foundUser.getId(), group.getId()));
            foundUser.setPrimaryGroupExperimenterMap(newDef);
        }

        // TODO: May want to move this outside the loop
        // and after the !newDefaultSet check.
        sec.doAction(new SecureMerge(session), foundUser);

    }

    public void setGroupOwner(Experimenter user, ExperimenterGroup group, boolean value) {
        Session session = sf.getSession();
        Experimenter foundUser = userById(user.getId(), session);
        ExperimenterGroup foundGroup = groupById(group.getId(), session);
        Set<GroupExperimenterMap> foundMaps = foundUser
                .findGroupExperimenterMap(foundGroup);
        if (foundMaps.size() < 1) {
            throw new ApiUsageException("Group " + group.getId() + " was not "
                    + "found for user " + user.getId());
        } else if (foundMaps.size() > 1) {
            log.warn(foundMaps.size() + " copies of " + foundGroup
                    + " found for " + foundUser);
        } else {
            // May throw an exception
            GroupExperimenterMap newDef = foundMaps.iterator().next();
            log.info(String.format("Seeting ownership flag on user %s to %s for %s",
                    foundUser.getId(), value, group.getId()));
            newDef.setOwner(value);
            sec.doAction(new SecureMerge(session), newDef);
        }
    }

    public void addGroups(final Experimenter user,
            final ExperimenterGroup... groups) {

        final Session session = sf.getSession();
        final List<String> added = new ArrayList<String>();

        Experimenter foundUser = userById(user.getId(), session);
        for (ExperimenterGroup group : groups) {
            ExperimenterGroup foundGroup = groupById(group.getId(), session);
            boolean found = false;
            for (ExperimenterGroup currentGroup : foundUser
                    .linkedExperimenterGroupList()) {
                found |= HibernateUtils.idEqual(foundGroup, currentGroup);
            }
            if (!found) {
                linkGroupAndUser(foundGroup, foundUser, false);
                added.add(foundGroup.getName());
            }
        }

        fixDefaultGroup(foundUser, session);

    }

    public void removeGroups(Experimenter user, ExperimenterGroup... groups) {

        final Session session = sf.getSession();

        Experimenter foundUser = userById(user.getId(), session);

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
                ExperimenterGroup p = groupById(pId, session);
                Experimenter c = userById(cId, session);
                p.unlinkExperimenter(c);
                sec.doAction(new SecureAction(){
                    public <T extends IObject> T updateObject(T... objs) {
                        for (T t : objs) {
                            session.delete(t);
                        }
                        session.flush();
                        return null;
                    }}, map);
                // sec.doAction(new SecureMerge(session, true), p);
                removed.add(p.getName());
            }
        }

        fixDefaultGroup(foundUser, session);

        session.flush();
    }

    public boolean isIgnoreCaseLookup() {
        return ignoreCaseLookup.get();
    }

    // ~ Helpers
    // =========================================================================

    protected GroupExperimenterMap linkGroupAndUser(ExperimenterGroup group,
            Experimenter e, boolean owned) {

        if (group == null || group.getId() == null) {
            throw new ApiUsageException("Group must be persistent.");
        }

        group = new ExperimenterGroup(group.getId(), false);

        // ticket:1021 - check for already added groups
        for (GroupExperimenterMap link : e.unmodifiableGroupExperimenterMap()) {
            ExperimenterGroup test = link.parent();
            if (test.getId().equals(group.getId())) {
                return link; // EARLY EXIT!
            }
        }

        GroupExperimenterMap link = e.linkExperimenterGroup(group);
        // ticket:1434
        link.setOwner(owned);

        link.getDetails().copy(sec.newTransientDetails(link));

        Session session = sf.getSession();
        sec.<IObject> doAction(new SecureMerge(session),
                userById(e.getId(), session), link);
        session.flush();
        return link;
    }

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
        copy.setLdap(e.getLdap());
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
        copy.setLdap(g.getLdap());
        copy.setConfig(g.getConfig());
        copy.getDetails().copy(sec.newTransientDetails(g));
        copy.getDetails().setPermissions(g.getDetails().getPermissions());
        // TODO see shallow copy comment on copy user
        return copy;
    }

    private ExperimenterGroup groupByName(String name, Session s) {
        Query q = s.createQuery("select g from ExperimenterGroup g "
                + "where g.name = :name");
        q.setParameter("name", name);
        ExperimenterGroup g = (ExperimenterGroup) q.uniqueResult();
        return g;
    }

    private Experimenter userById(long id, Session s) {
        return (Experimenter) s.load(Experimenter.class, id);
    }

    private ExperimenterGroup groupById(long id, Session s) {
        return (ExperimenterGroup) s.load(ExperimenterGroup.class, id);
    }

    private void fixDefaultGroup(Experimenter u, Session s) {
        ExperimenterGroup g = shouldBeDefault(u, s);
        if (g != null) {
            setDefaultGroup(u, g);
        }
    }

    /**
     * If the "user" group is the first element of the list of groups that this
     * user is a member of, then check if there's a second value which could be
     * used as the default instead.
     */
    private ExperimenterGroup shouldBeDefault(Experimenter usr, Session s) {
        List<ExperimenterGroup> grps = usr.linkedExperimenterGroupList();
        if (grps.size() >= 2) {
            // If there are either no groups, or no alternatives so there's
            // nothing we need to check.
            final String USER = sec.getSecurityRoles().getUserGroupName();
            if (USER.equals(grps.get(0).getName())) {
                return grps.get(1);
            }
        }
        return null;
    }
}

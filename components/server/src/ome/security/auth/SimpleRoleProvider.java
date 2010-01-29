/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.tools.hibernate.HibernateUtils;
import ome.tools.hibernate.SessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

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

    final private static Log log = LogFactory.getLog(SimpleRoleProvider.class);

    final protected SecuritySystem sec;

    final protected SessionFactory sf;

    public SimpleRoleProvider(SecuritySystem sec, SessionFactory sf) {
        this.sec = sec;
        this.sf = sf;
    }

    public long createGroup(String name, boolean strict) {
        Session s = sf.getSession();
        ExperimenterGroup g = groupByName(name, s);

        if (g == null) {
            g = new ExperimenterGroup();
            g.setName(name);
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

        final Session session = sf.getSession();
        ExperimenterGroup g = sec.doAction(new SecureMerge(session), group);
        return g.getId();
    }

    public long createExperimenter(Experimenter experimenter,
            ExperimenterGroup defaultGroup, ExperimenterGroup... otherGroups) {

        Session session = sf.getSession();

        SecureAction action = new SecureMerge(session);

        Experimenter e = copyUser(experimenter);
        e.getDetails().copy(sec.newTransientDetails(e));
        e = sec.doAction(action, e);
        session.flush();

        GroupExperimenterMap link = linkGroupAndUser(defaultGroup, e);
        if (null != otherGroups) {
            for (ExperimenterGroup group : otherGroups) {
                linkGroupAndUser(group, e);
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
            foundUser.setPrimaryGroupExperimenterMap(foundMaps.iterator()
                    .next());
        }

        // TODO: May want to move this outside the loop
        // and after the !newDefaultSet check.
        sec.doAction(new SecureMerge(session), foundUser);

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
                linkGroupAndUser(foundGroup, foundUser);
                added.add(foundGroup.getName());
            }
        }
    }

    public void removeGroups(Experimenter user, ExperimenterGroup... groups) {

        Session session = sf.getSession();

        Experimenter foundUser = (Experimenter) session.load(
                Experimenter.class, user.getId());
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
                sec.doAction(new SecureMerge(session), p);
                removed.add(p.getName());
            }
        }
        session.flush();
    }

    // ~ Helpers
    // =========================================================================

    protected GroupExperimenterMap linkGroupAndUser(ExperimenterGroup group,
            Experimenter e) {

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
        link.getDetails().copy(sec.newTransientDetails(link));
        worldReadable(link);

        Session session = sf.getSession();
        sec.doAction(new SecureMerge(session), userById(e.getId(), session),
                link);
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
        if (e.getDetails() != null && e.getDetails().getPermissions() != null) {
            copy.getDetails().setPermissions(e.getDetails().getPermissions());
        } else {
            // ticket:1204 - If no permissions are set, we will need to make
            // sure that this instance is visible, otherwise non-admin users
            // will have significant problems.
            worldReadable(copy);
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
        copy.getDetails().copy(sec.newTransientDetails(g));
        worldReadable(copy);
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

    /**
     * @see ticket:1204
     */
    private void worldReadable(IObject obj) {
        Permissions p = obj.getDetails().getPermissions();
        if (p == null) {
            p = new Permissions(Permissions.WORLD_IMMUTABLE);
            obj.getDetails().setPermissions(p);
        } else {
            p.grant(Role.GROUP, Right.READ);
            p.grant(Role.WORLD, Right.READ);
        }
    }

    private final class SecureMerge implements SecureAction {
        private final Session session;

        private SecureMerge(Session session) {
            this.session = session;
        }

        public <T extends IObject> T updateObject(T... objs) {
            return (T) session.merge(objs[0]);
        }
    }
}

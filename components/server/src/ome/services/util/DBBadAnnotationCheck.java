/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.conditions.InternalException;
import ome.model.annotations.AnnotationAnnotationLink;
import ome.model.annotations.ChannelAnnotationLink;
import ome.model.annotations.DatasetAnnotationLink;
import ome.model.annotations.ExperimenterAnnotationLink;
import ome.model.annotations.ExperimenterGroupAnnotationLink;
import ome.model.annotations.FilesetAnnotationLink;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.NamespaceAnnotationLink;
import ome.model.annotations.NodeAnnotationLink;
import ome.model.annotations.OriginalFileAnnotationLink;
import ome.model.annotations.PixelsAnnotationLink;
import ome.model.annotations.PlaneInfoAnnotationLink;
import ome.model.annotations.PlateAcquisitionAnnotationLink;
import ome.model.annotations.PlateAnnotationLink;
import ome.model.annotations.ProjectAnnotationLink;
import ome.model.annotations.ReagentAnnotationLink;
import ome.model.annotations.RoiAnnotationLink;
import ome.model.annotations.ScreenAnnotationLink;
import ome.model.annotations.SessionAnnotationLink;
import ome.model.annotations.WellAnnotationLink;
import ome.model.annotations.WellSampleAnnotationLink;
import ome.system.Roles;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Spring bean run on start-up to make sure that there are no annotations with a bad discriminator,
 * nor any inter-group annotation links.
 * A SQL upgrade script handles this issue for 5.1 and beyond.
 * 
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.2
 */
public class DBBadAnnotationCheck extends BaseDBCheck {

    private static final Logger log = LoggerFactory.getLogger(DBBadAnnotationCheck.class);

    private static final String[] badDiscriminators = {"/basic/text/uri/", "/basic/text/url/"};

    private static final String[] annotationLinkClasses = {
        AnnotationAnnotationLink.class.getName(),
        ChannelAnnotationLink.class.getName(),
        DatasetAnnotationLink.class.getName(),
        ExperimenterAnnotationLink.class.getName(),
        ExperimenterGroupAnnotationLink.class.getName(),
        FilesetAnnotationLink.class.getName(),
        ImageAnnotationLink.class.getName(),
        NamespaceAnnotationLink.class.getName(),
        NodeAnnotationLink.class.getName(),
        OriginalFileAnnotationLink.class.getName(),
        PixelsAnnotationLink.class.getName(),
        PlaneInfoAnnotationLink.class.getName(),
        PlateAcquisitionAnnotationLink.class.getName(),
        PlateAnnotationLink.class.getName(),
        ProjectAnnotationLink.class.getName(),
        ReagentAnnotationLink.class.getName(),
        RoiAnnotationLink.class.getName(),
        ScreenAnnotationLink.class.getName(),
        SessionAnnotationLink.class.getName(),
        WellAnnotationLink.class.getName(),
        WellSampleAnnotationLink.class.getName()};

    private static final ImmutableSet<String> noParentGroup = ImmutableSet.of(
            ExperimenterAnnotationLink.class.getName(),
            ExperimenterGroupAnnotationLink.class.getName(),
            NodeAnnotationLink.class.getName(),
            SessionAnnotationLink.class.getName());

    private final SessionFactory sessionFactory;

    private final long userGroupId;

    public DBBadAnnotationCheck(Executor executor, SessionFactory sessionFactory, Roles roles) {
        super(executor);
        this.sessionFactory = sessionFactory;
        this.userGroupId = roles.getUserGroupId();
    }

    /**
     * Check if the group IDs include multiple non-user groups.
     * @param groupIds some group IDs
     * @return if they include multiple non-user groups
     */
    private boolean isTooManyGroupIds(long... groupIds) {
        Long otherGroupId = null;
        for (final long currGroupId : groupIds) {
            if (userGroupId != currGroupId) {
                if (otherGroupId == null) {
                    otherGroupId = currGroupId;
                } else if (otherGroupId != currGroupId) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void doCheck() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            int deleteCount = 0;
            String hql;
            hql = "DELETE FROM AnnotationAnnotationLink WHERE id IN " +
                    "(SELECT link.id FROM AnnotationAnnotationLink link WHERE link.parent.class IN (:classes))";
            deleteCount += session.createQuery(hql).setParameterList("classes", badDiscriminators).executeUpdate(); 
            for (final String annotationLinkClass : annotationLinkClasses) {
                hql = "DELETE FROM " + annotationLinkClass + " WHERE id IN " +
                        "(SELECT link.id FROM " + annotationLinkClass + " link WHERE link.child.class IN (:classes))";
                deleteCount += session.createQuery(hql).setParameterList("classes", badDiscriminators).executeUpdate();

                final StringBuffer sb = new StringBuffer();
                sb.append("SELECT id, ");
                if (!noParentGroup.contains(annotationLinkClass)) {
                    sb.append("parent.details.group.id, ");
                }
                sb.append("child.details.group.id, details.group.id");
                sb.append(" FROM ");
                sb.append(annotationLinkClass);
                hql = sb.toString();

                final Set<Long> toDelete = new HashSet<Long>();
                for (final Object resultRow : session.createQuery(hql).list()) {
                    final Object[] resultArray = (Object[]) resultRow;
                    final long[] ids = new long[resultArray.length];
                    for (int index = 0; index < resultArray.length; index++) {
                        ids[index] = (Long) resultArray[index];
                    }
                    final long actualLinkId = ids[0];
                    ids[0] = ids[1];
                    if (isTooManyGroupIds(ids)) {
                        toDelete.add(actualLinkId);
                    }
                }
                if (!toDelete.isEmpty()) {
                    hql = "DELETE FROM " + annotationLinkClass + " WHERE id IN (:ids)";
                    for (final List<Long> toDeleteBatch : Iterables.partition(toDelete, 256)) {
                        deleteCount += session.createQuery(hql).setParameterList("ids", toDeleteBatch).executeUpdate();
                    }
                }
            }
            hql = "DELETE FROM Annotation annotation WHERE annotation.class IN (:classes)";
            deleteCount += session.createQuery(hql).setParameterList("classes", badDiscriminators).executeUpdate();
            if (deleteCount > 0) {
                log.warn("deleted bad annotations or links, count = " + deleteCount);
            } else if (log.isDebugEnabled()) {
                log.debug("verified annotations and links");
            }
        } catch (HibernateException e) {
            final String message = "error in checking annotations and links";
            log.error(message, e);
            throw new InternalException(message);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}

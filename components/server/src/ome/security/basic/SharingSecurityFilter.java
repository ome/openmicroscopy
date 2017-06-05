/*
 * Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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

package ome.security.basic;

import java.util.List;
import java.util.Map;

import ome.model.core.Image;
import ome.model.internal.Details;
import ome.services.sharing.ShareStore;
import ome.services.sharing.data.ShareData;
import ome.system.EventContext;
import ome.system.Roles;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Filter;
import org.hibernate.Session;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * The sharing security filter provides database-level access protection for share contexts.
 * @see ome.security.sharing.SharingACLVoter
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.2.5
 */
public class SharingSecurityFilter extends AbstractSecurityFilter {

    private static final ImmutableMap<String, String> PARAMETER_TYPES =
            ImmutableMap.of("is_admin", "int",
                            "is_share", "int",
                            "images", "long");

    private ShareStore shares;

    /**
     * Construct a new sharing security filter.
     * @param roles the users and groups that are special to OMERO
     * @param shares the shares
     */
    public SharingSecurityFilter(Roles roles, ShareStore shares) {
        super(roles);
        this.shares = shares;
    }

    @Override
    public Map<String, String> getParameterTypes() {
        return PARAMETER_TYPES;
    }

    @Override
    public String getDefaultCondition() {
        /* provided instead by annotations */
        return null;
    }

    @Override
    public boolean passesFilter(Session session, Details details, EventContext ec) {
        final Long shareId = ec.getCurrentShareId();
        if (shareId == null) {
            return true;
        }
        final ShareData share = shares.get(shareId);
        return ec.isCurrentUserAdmin() || share != null && share.enabled;
    }

    @Override
    public void enable(Session session, EventContext ec) {
        List<Long> imageIds = null;
        final Long shareId = ec.getCurrentShareId();
        if (shareId != null) {
            final ShareData shareData = shares.get(shareId);
            if (shareData != null && shareData.enabled) {
                imageIds = shareData.objectMap.get(Image.class.getName());
            }
        }
        if (CollectionUtils.isEmpty(imageIds)) {
            imageIds = ImmutableList.of(-1L);
        }
        final int isAdmin01 = ec.isCurrentUserAdmin() ? 1 : 0;
        final int isShare01 = isShare(ec) ? 1 : 0;

        final Filter filter = session.enableFilter(getName());
        filter.setParameter("is_admin", isAdmin01);
        filter.setParameter("is_share", isShare01);
        filter.setParameterList("images", imageIds);
        enableBaseFilters(session, isAdmin01, ec.getCurrentUserId());
    }
}

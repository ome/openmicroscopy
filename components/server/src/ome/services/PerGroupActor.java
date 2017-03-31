/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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

package ome.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.api.IQuery;
import ome.model.core.Pixels;
import ome.parameters.Parameters;
import ome.services.messages.ContextMessage;
import ome.system.OmeroContext;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Perform an operation on {@link Pixels} in contexts corresponding to the {@link Pixels}' group.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.3.1
 */
abstract class PerGroupActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerGroupActor.class);

    private final OmeroContext applicationContext;
    private final IQuery queryService;
    private final long currentGroupId;

    /**
     * Create a new per-group actor.
     * @param applicationContext the OMERO application context
     * @param queryService the query service for retrieving the pixels' groups
     * @param currentGroupId the current group ID, may be {@code -1}
     */
    PerGroupActor(OmeroContext applicationContext, IQuery queryService, long currentGroupId) {
        this.applicationContext = applicationContext;
        this.queryService = queryService;
        this.currentGroupId = currentGroupId;
    }

    /**
     * Act on the {@link Pixels} by setting the group context and calling {@link #actOnOneGroup(Set)} as necessary.
     * @param pixelsIds the IDs of the {@link Pixels} on which to act
     */
    void actOnByGroup(Collection<Long> pixelsIds) {
        if (pixelsIds.isEmpty()) {
            return;
        }
        final SetMultimap<Long, Long> pixelsByGroup = HashMultimap.create();
        for (final Object[] resultRow : queryService.projection("SELECT id, details.group.id FROM Pixels WHERE id IN (:ids)",
                new Parameters().addIds(pixelsIds))) {
            final Long pixelsId = (Long) resultRow[0];
            final Long groupId  = (Long) resultRow[1];
            pixelsByGroup.put(groupId, pixelsId);
        }
        for (final Map.Entry<Long, Collection<Long>> pixelsOneGroup : pixelsByGroup.asMap().entrySet()) {
            final long groupId = pixelsOneGroup.getKey();
            if (groupId == currentGroupId) {
                actOnOneGroup((Set<Long>) pixelsOneGroup.getValue());
            } else {
                final Map<String, String> groupContext = new HashMap<>();
                groupContext.put("omero.group", Long.toString(groupId));
                try {
                    try {
                        applicationContext.publishMessage(new ContextMessage.Push(this, groupContext));
                    } catch (Throwable t) {
                        LOGGER.error("could not publish context change push", t);
                    }
                    actOnOneGroup((Set<Long>) pixelsOneGroup.getValue());
                } finally {
                    try {
                        applicationContext.publishMessage(new ContextMessage.Pop(this, groupContext));
                    } catch (Throwable t) {
                        LOGGER.error("could not publish context change pop", t);
                    }
                }
            }
        }
    }

    /**
     * Act on the {@link Pixels}. Called within a context corresponding to the {@link Pixels}' group.
     * @param pixelsIds the IDs of the {@link Pixels} on which to act
     */
    abstract protected void actOnOneGroup(Set<Long> pixelsIds);
}

/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

package ome.services.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hibernate.Session;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

/**
 * OMERO model objects sometimes must be processed in a specific order.
 * This class groups the utility methods for performing such ordering.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.1
 */
public class ModelObjectSequencer {
    /**
     * Sort a list of original file IDs such that files precede containing directories.
     * @param session the Hibernate session
     * @param unorderedIds the IDs of original files
     * @return a batching of the given IDs such that a batch containing a file precedes a batch containing a containing directory
     */
    public static Collection<List<Long>> sortOriginalFileIds(Session session, Collection<Long> unorderedIds) {
        if (unorderedIds.size() < 2) {
            /* no need to rearrange anything, as there are not multiple original files */
            return Collections.<List<Long>>singletonList(new ArrayList<Long>(unorderedIds));
        }
        final String hql = "SELECT id, length(path) FROM OriginalFile WHERE id IN (:ids)";
        final SortedMap<Integer, List<Long>> filesByPathLength = new TreeMap<Integer, List<Long>>(Ordering.natural().reverse());
        for (final Collection<Long> idBatch : Iterables.partition(unorderedIds, 256)) {
            for (final Object[] result : (List<Object[]>) session.createQuery(hql).setParameterList("ids", idBatch).list()) {
                final Long id = (Long) result[0];
                final Integer length = (Integer) result[1];
                List<Long> idList = filesByPathLength.get(length);
                if (idList == null) {
                    idList = new ArrayList<Long>();
                    filesByPathLength.put(length, idList);
                }
                idList.add(id);
            }
        }
        final Collection<List<Long>> orderedIds = new ArrayList<List<Long>>();
        for (final List<Long> ids : filesByPathLength.values()) {
            orderedIds.add(ids);
        }
        return orderedIds;
    }
}

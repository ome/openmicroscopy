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

package omero.cmd.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import omero.cmd.GraphModify;
import omero.cmd.GraphModify2;

import com.google.common.base.Splitter;

/**
 * Static utility methods for model graph operations.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
class GraphUtil {
    /**
     * Split a list of strings by a given separator, trimming whitespace and ignoring empty items.
     * @param separator the separator between the list items
     * @param list the list
     * @return a means of iterating over the list items
     */
    static Iterable<String> splitList(char separator, String list) {
        return Splitter.on(separator).trimResults().omitEmptyStrings().split(list);
    }

    /**
     * Count how many objects are listed in a {@code IdListMap}.
     * @param idListMap lists of object IDs indexed by type name
     * @return how many objects are listed in given {@code IdListMap}
     */
    static int getIdListMapSize(Map<?, long[]> idListMap) {
        int size = 0;
        for (final long[] ids : idListMap.values()) {
            size += ids.length;
        }
        return size;
    }

    /**
     * Copy the given collection of IDs to an array of native {@code long}s.
     * @param ids a collection of IDs, none of which may be {@code null}
     * @return the same IDs in a new array
     */
    static long[] idsToArray(Collection<Long> ids) {
        final long[] idArray = new long[ids.size()];
        int index = 0;
        for (final long id : ids) {
            idArray[index++] = id;
        }
        return idArray;
    }

    /**
     * Copy the {@link GraphModify2} fields of one request to another.
     * @param requestFrom the source of the field copy
     * @param requestTo the target of the field copy
     */
    static void copyFields(GraphModify2 requestFrom, GraphModify2 requestTo) {
        requestTo.dryRun = requestFrom.dryRun;
        requestTo.targetObjects = requestFrom.targetObjects == null ? null : new HashMap<String, long[]>(requestFrom.targetObjects);
        requestTo.includeNs = requestTo.includeNs == null ? null : new ArrayList<String>(requestFrom.includeNs);
        requestTo.excludeNs = requestTo.excludeNs == null ? null : new ArrayList<String>(requestFrom.excludeNs);
        requestTo.includeChild = requestTo.includeChild == null ? null : new ArrayList<String>(requestFrom.includeChild);
        requestTo.excludeChild = requestTo.excludeChild == null ? null : new ArrayList<String>(requestFrom.excludeChild);
    }

    /**
     * Approximately translate {@link GraphModify} options in setting the parameters of a {@link GraphModify2} request.
     * @param options {@link GraphModify} options
     * @param request the request whose options should be updated
     */
    static void translateOptions(Map<String, String> options, GraphModify2 request) {
        for (final Map.Entry<String, String> option : options.entrySet()) {
            /* find type to which options apply */
            String optionType = option.getKey();
            if (optionType.charAt(0) == '/') {
                optionType = optionType.substring(1);
            }
            for (final String optionValue : GraphUtil.splitList(';', option.getValue())) {
                /* approximately translate each option */
                if ("KEEP".equals(optionValue)) {
                    if (request.excludeChild == null) {
                        request.excludeChild = new ArrayList<String>();
                    }
                    request.excludeChild.add(optionType);
                } else if (optionValue.startsWith("excludes=")) {
                    if (request.excludeNs == null) {
                        request.excludeNs = new ArrayList<String>();
                    }
                    for (final String nameSpace : GraphUtil.splitList(',', optionValue)) {
                        request.excludeNs.add(nameSpace);
                    }
                }
            }
        }
    }
}

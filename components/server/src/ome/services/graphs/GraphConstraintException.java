/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.graphs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ome.services.graphs.GraphEntry;
import ome.services.graphs.GraphException;

/**
 * Exception which will be thrown by {@link GraphStep} implementations like
 * {@link ome.services.chgrp.ChgrpStep} and {@link ome.services.chrp.ChgrpValidation}
 * when constraints are found against the current {@link GraphEntry}, i.e. an
 * improper link. The id of all such improper links are available in the exception
 * so that clients can take corrective measures.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0
 */
public class GraphConstraintException extends GraphException {

    private static final long serialVersionUID = 1L;

    private final Map<String, List<Long>> constraints;

    public GraphConstraintException(String msg, Map<String, List<Long>> constraints) {
        super(msg);
        this.constraints = constraints;
    }

    public Map<String, long[]> getConstraints() {
        Map<String, long[]> rv = new HashMap<String, long[]>();
        for (Map.Entry<String, List<Long>> entry : constraints.entrySet()) {
            String key = toSimpleKey(entry);
            long[] arr = toArray(entry);
            rv.put(key, arr);
        }
        return rv;
    }

    private String toSimpleKey(Entry<String, List<Long>> entry) {
        String key = entry.getKey();
        return key.substring(key.lastIndexOf(".")+1);
    }

    private long[] toArray(Map.Entry<String, List<Long>> entry) {
        List<Long> value = entry.getValue();
        Long[] arr = value.toArray(new Long[value.size()]);
        long[] arr2 = new long[arr.length];
        for (int i=0; i < arr.length; i++) {
            if (arr[i] == null) {
                arr2[i] = -1l;
            } else {
                arr2[i] = arr[i];
            }
        }
        return arr2;
    }
}

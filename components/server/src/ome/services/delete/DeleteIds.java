/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ome.api.IDelete;

import org.hibernate.Session;

/**
 * Wrapper around a map from {@link DeleteSpec} to lists of ids which will be
 * collected in a preliminary phase of delete. This is necessary since
 * intermediate deletes, may disconnect the graph, causing later deletes to fail
 * if they were solely based on the id of the root element.
 *
 * The {@link DeleteIds} instance can only be initialized with a graph of
 * initialized {@DeleteSpec}s.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class DeleteIds {

    private final Map<DeleteSpec, List<List<Long>>> ids = new HashMap<DeleteSpec, List<List<Long>>>();

    public DeleteIds(Session session, DeleteSpec spec) throws DeleteException {

        List<String[]> paths = new ArrayList<String[]>();
        load(spec, paths);
        paths = Collections.unmodifiableList(paths);

        Iterator<DeleteSpec> it = spec.walk();
        while (it.hasNext()) {
            DeleteSpec subSpec = it.next();
            List<List<Long>> ids = subSpec.backupIds(session, paths);
            this.ids.put(subSpec, ids);
        }
    }

    private void load(DeleteSpec spec, List<String[]> paths) {
        for (DeleteEntry entry : spec.entries()) {
            DeleteSpec subSpec = entry.getSubSpec();
            if (subSpec == null) {
                String[] p = entry.path(spec.getSuperSpec());
                paths.add(p);
            } else {
                load(subSpec, paths);
            }
        }
    }

    public List<Long> get(DeleteSpec deleteSpec, int step) {
        return ids.get(deleteSpec).get(step);
    }

}

/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.model.IObject;
import ome.util.CBlock;
import ome.util.Utils;

import org.hibernate.Session;

/**
 * {@link CBlock} implementation which counts the number of locking instances
 * there are for a single {@link IObject} while walking a graph.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @see ome.api.IDelete
 */
class UnloadedCollector implements CBlock {

    final protected boolean count;
    final protected LocalQuery query;
    final protected LocalAdmin admin;
    final List<IObject> list = new ArrayList<IObject>();
    final Map<String, Map<Long, Map<String, Long>>> map = new HashMap<String, Map<Long, Map<String, Long>>>();

    public UnloadedCollector(LocalQuery query, LocalAdmin admin, boolean count) {
        this.query = query;
        this.admin = admin;
        this.count = count;
    }

    public void addAll(List<IObject> list) {
        for (IObject object : list) {
            call(object);
        }
    }

    public Object call(IObject object) {

        if (object == null) {
            return null;
        }

        IObject copy = (IObject) Utils.trueInstance(object.getClass());
        copy.setId(object.getId());
        copy.unload();
        list.add(copy);
        if (count) {
            count(object); /* PERFORMANCE HIT */
        }
        return null;
    }

    /**
     * Counts via {@link LocalAdmin#getLockingIds(Session, IObject)} all the
     * items which entities which link to the given object.
     *
     * @param object
     */
    @SuppressWarnings("unchecked")
    void count(final IObject object) {

        Map<Long, Map<String, Long>> id_class_id = map.get(object.getClass()
                .getName());

        if (id_class_id == null) {
            id_class_id = new HashMap<Long, Map<String, Long>>();
            map.put(object.getClass().getName(), id_class_id);
        }

        if (!id_class_id.containsKey(object.getId())) {
            id_class_id.put(object.getId(), admin.getLockingIds(
                    (Class<IObject>) object.getClass(), object.getId(), null));
        }

    }
}

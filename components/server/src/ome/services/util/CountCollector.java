/*
 * ome.services.util.CountCollector
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.tools.hibernate.ProxySafeFilter;
import ome.util.Filterable;

/**
 * filter implementation which collects the ids of certain fields.
 */
public class CountCollector extends ProxySafeFilter {

    /**
     * { Class : { Id : { Field : Count } } }
     */
    private final Map<Class<? extends IObject>, Map<Long, Map<String, Long>>> lookup = new HashMap<Class<? extends IObject>, Map<Long, Map<String, Long>>>();

    /**
     * Execution method.
     * 
     * @param target
     */
    public void collect(Object target) {
        super.filter(null, target);
    }

    /**
     * This is the method that actually hooks us into the graph-parsing logic.
     */
    @Override
    public Filterable filter(String fieldId, Filterable f) {
        if (f instanceof IObject) {
            addIfHit((IObject) f);
        }
        return super.filter(fieldId, f);
    }

    /**
     * @param k
     * @param field
     * @param list
     *            List as returned by the count queries from extended metadata.
     */
    public void addCounts(Class<? extends IObject> k, String field,
            List<Object[]> list) {

        Map<Long, Map<String, Long>> id_field_count = lookup.get(k);
        if (id_field_count == null) {
            id_field_count = new HashMap<Long, Map<String, Long>>();
            lookup.put(k, id_field_count);
        }

        for (Object[] longs : list) {
            Long id = (Long) longs[0];
            Long count = (Long) longs[1];

            Map<String, Long> field_count = id_field_count.get(id);
            if (field_count == null) {
                field_count = new HashMap<String, Long>();
                id_field_count.put(id, field_count);
            }

            // Checks
            if (count == null) {
                throw new ApiUsageException("Count cannot be null for " + field);
            } else if (count.longValue() < 0L) {
                throw new ApiUsageException("Count cannot be negative.");
            }

            field_count.put(field, count);
        }

    }

    protected void addIfHit(IObject o) {

        Class<? extends IObject> k = o.getClass();
        Long id = o.getId();

        Set<Class<? extends IObject>> keys = lookup.keySet();
        for (Class<? extends IObject> key : keys) {
        	if (key.isAssignableFrom(k)) {
        		k = key;
        		break;
        	}
        }
        Map<Long, Map<String, Long>> id_field_count = lookup.get(k);
        if (id_field_count == null) {
            return;
        }

        Map<String, Long> field_count = id_field_count.get(id);
        if (field_count == null) {
            return;
        }

        Map counts = o.getDetails().getCounts();
        if (counts == null) {
            counts = new HashMap();
            o.getDetails().setCounts(counts);
        }
        counts.putAll(field_count);

    }

}
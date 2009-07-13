/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.tools.hibernate;

import java.util.HashMap;
import java.util.Map;

import ome.conditions.ApiUsageException;
import ome.model.IEnum;
import ome.model.IObject;
import ome.util.Filterable;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * {@link UpdateFilter} subclass specialized for use with the
 * {@link Session#save(Object)} (as opposed to {@link Session#merge(Object)})
 * method. This is primarily of use during import when all objects are either
 * unloaded, enums, or newly created instances.
 * 
 * @since Beta4.1
 */
public class ReloadFilter extends UpdateFilter {

    private final Session session;

    /**
     * Cache of all the enumerations which have been looked up via the session
     * in {@link #filter(String, Object)}.
     */
    private final Map<Class, Map<String, IEnum>> enumsMap = new HashMap<Class, Map<String, IEnum>>();

    public ReloadFilter(Session s) {
        this.session = s;
    }

    public Filterable filter(String fieldId, Filterable f) {

        // If the session already has this object load then we can move on.
        if (session.contains(f)) {
            return f;
        }

        if (f instanceof IObject) {

            // For the moment we are only worrying with IObjects.
            // The logic for handling Details, and other Filterable objects
            // should be the default.
            IObject o = (IObject) f;

            // If the object is unloaded, then we load it.
            if (!o.isLoaded()) {
                return (IObject) session.get(o.getClass(), o.getId());
            }

            // If the object is an enum, then we try to match based on value.
            if (f instanceof IEnum) {

                IEnum e = (IEnum) f;
                String val = e.getValue();

                // First, heck if in map.
                Map<String, IEnum> enums = enumsMap.get(f.getClass());
                if (enums != null) {
                    IEnum enu = enums.get(val);
                    if (enu != null) {
                        return enu;
                    }
                }

                // We haven't seen this enum yet. Try to load it, and if found
                // put it in enumsMap
                Query q = session.createQuery(String.format(
                        "select e from %s e where e.value = :val", f.getClass()
                                .getName()));
                q.setString("val", val);
                IEnum existing = (IEnum) q.uniqueResult();
                if (existing != null) {
                    enums = enumsMap.get(f.getClass());
                    if (enums == null) {
                        enums = new HashMap<String, IEnum>();
                        enumsMap.put(f.getClass(), enums);
                    }
                    enums.put(val, existing);
                    return existing;
                }
            }

            // Finally, if we've reached here, then this object is not an enum
            // and not unloaded. If it has an id, that means a user *might* be
            // trying to make an UPDATE, but these operations assume INSERT
            // only.
            if (o.getId() != null) {
                throw new ApiUsageException(
                        "INSERTs only! Pass only new objects, enums, or "
                                + "unloaded objects to this method.");
            }

        }

        return super.filter(fieldId, f);

    }
}

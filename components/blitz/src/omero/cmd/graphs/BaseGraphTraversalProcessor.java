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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import com.google.common.collect.Maps;

import ome.model.IObject;
import ome.services.graphs.GraphTraversal;

/**
 * Useful methods for {@link GraphTraversal.Processor} instances to share.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.x TODO
 */
public abstract class BaseGraphTraversalProcessor implements GraphTraversal.Processor {
    protected final Session session;

    public BaseGraphTraversalProcessor(Session session) {
        this.session = session;
    }

    @Override
    public void nullProperties(String className, String propertyName,
            Collection<Long> ids) {
        final String update = "UPDATE " + className + " SET " + propertyName + " = NULL WHERE id IN (:ids)";
        session.createQuery(update).setParameterList("ids", ids).executeUpdate();
    }

    @Override
    public void filterProperties(String className, String propertyName,
            Collection<Entry<Long, Collection<Entry<String, Long>>>> ids) {
        final Map<Long, Collection<Entry<String, Long>>> idMap = new HashMap<Long, Collection<Entry<String, Long>>>(2 * ids.size());
        for (final Entry<Long, Collection<Entry<String, Long>>> idEntry : ids) {
            idMap.put(idEntry.getKey(), idEntry.getValue());
        }
        final String query = "FROM " + className + " root LEFT JOIN FETCH root." + propertyName + " WHERE root.id IN (:ids)";
        final List<IObject> retrieved = session.createQuery(query).setParameterList("ids", idMap.keySet()).list();
        for (final IObject proxy : retrieved) {
            final Set<Entry<String, Long>> toRemove = new HashSet<Entry<String, Long>>(idMap.get(proxy.getId()));
            final Collection<IObject> items;
            try {
                items = (Collection<IObject>) PropertyUtils.getNestedProperty(proxy, propertyName);
            } catch (/* TODO Java SE 7 ReflectiveOperation*/Exception e) {
                throw new RuntimeException(Hibernate.getClass(proxy).getName() + "[" + proxy.getId() +
                        "] has no accessible object collection property " + propertyName, e);
            }
            final Iterator<IObject> itemIterator = items.iterator();
            while (itemIterator.hasNext()) {
                final IObject item = itemIterator.next();
                if (toRemove.contains(Maps.immutableEntry(Hibernate.getClass(item), item.getId()))) {
                    itemIterator.remove();
                }
            }
        }
    }

    @Override
    public void deleteInstances(String className, Collection<Long> ids) {
        final String update = "DELETE FROM " + className + " WHERE id IN (:ids)";
        session.createQuery(update).setParameterList("ids", ids).executeUpdate();
    }
}

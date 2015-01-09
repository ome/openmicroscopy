/*
 * Copyright (C)2013 Glencoe Software, Inc. All rights reserved.
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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0.0
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class GraphSteps extends AbstractList<GraphStep> implements RandomAccess {

    /**
     * Lookup key which represents an operation/row-in-db pair.
     */
    private static class Key {

        private final Class<? extends GraphStep> k;

        private final String table;

        private final long id;

        private final int hashCode;

        Key(Class<? extends GraphStep> k, String table, long id) {
            this.k = k;
            this.table = table;
            this.id = id;
            this.hashCode = _hashCode();
        }

        public int _hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (id ^ (id >>> 32));
            result = prime * result + ((k == null) ? 0 : k.hashCode());
            result = prime * result + ((table == null) ? 0 : table.hashCode());
            return result;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (id != other.id)
                return false;
            if (k == null) {
                if (other.k != null)
                    return false;
            } else if (!k.equals(other.k))
                return false;
            if (table == null) {
                if (other.table != null)
                    return false;
            } else if (!table.equals(other.table))
                return false;
            return true;
        }

    }

    private static class Values {
        private final Map<GraphStep, Integer> index;
        private final List<GraphStep> steps;
        private boolean success;
        private boolean validated;
        Values() {
            this.index = new HashMap<GraphStep, Integer>();
            this.steps = new ArrayList<GraphStep>();
        }
        void add(GraphStep step) {
            this.index.put(step, this.steps.size());
            this.steps.add(step);
        }
        public boolean isLast(GraphStep step) {
            final int idx = index.get(step);
            final int max = steps.size() - 1;
            return idx == max;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(GraphSteps.class);

    /**
     * {@link GraphStep} instances which are passed to the constructor of this
     * instance. No changes can or should be made to this {@link List}.
     */
    private final List<GraphStep> steps;

    /**
     * Map from a key based on the table & id array of a given row to
     * all the instances of that row that have been found. A multi-map
     * is not used because we want to maintain an index to the various
     * internal values.
     */
    private final Map<Key, Values> sameRows = new HashMap<Key, Values>();

    public GraphSteps(List<GraphStep> steps) {
        this.steps = Collections.unmodifiableList(steps);
        for (GraphStep step : steps) {
            Key k = k(step);
            if (k == null) {
                continue;
            }
            Values v = sameRows.get(k);
            if (v == null) {
                v = new Values();
                sameRows.put(k, v);
            }
            v.add(step);
        }
    }

    Key k(GraphStep step) {
        long[] ids = step.getIds();
        if (ids == null) {
            return null; // superspec
        }
        return new Key(step.getClass(), step.table, ids[ids.length-1]);
    }

    Values v(GraphStep step) {
        Key k = k(step);
        if (k == null) {
            return null;
        }
        return sameRows.get(k);
    }
    //
    // List implementation
    //

    @Override
    public GraphStep get(int index) {
        return this.steps.get(index);
    }

    @Override
    public int size() {
        return this.steps.size();
    }

    //
    // GraphState usage
    //

    public boolean willBeTriedAgain(GraphStep step) {
        Values v = v(step);
        if (v == null) {
            return false;
        }
        return !v.isLast(step);
    }

    public void succeeded(GraphStep step) {
        Values v = v(step);
        if (v == null) {
            return; // no-op
        }
        v.success = true;
    }

    public boolean alreadySucceeded(GraphStep step) {
        Values v = v(step);
        if (v == null) {
            return false;
        }
        return v.success;
    }


    public void validated(GraphStep step) {
        Values v = v(step);
        if (v == null) {
            return; // no-op
        }
        v.validated = true;
    }

    public boolean alreadyValidated(GraphStep step) {
        Values v = v(step);
        if (v == null) {
            return false;
        }
        return v.validated;
    }
}

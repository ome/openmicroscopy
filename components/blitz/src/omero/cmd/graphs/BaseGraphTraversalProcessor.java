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

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.model.internal.Details;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphTraversal;

/**
 * Useful methods for {@link ome.services.graphs.GraphTraversal.Processor} instances to share.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public abstract class BaseGraphTraversalProcessor implements GraphTraversal.Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseGraphTraversalProcessor.class);

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
    public void deleteInstances(String className, Collection<Long> ids) throws GraphException {
        final String update = "DELETE FROM " + className + " WHERE id IN (:ids)";
        final int count = session.createQuery(update).setParameterList("ids", ids).executeUpdate();
        if (count != ids.size()) {
            LOGGER.warn("not all the objects of type " + className + " could be deleted");
        }
    }

    @Override
    public void assertMayProcess(String className, long id, Details details) throws GraphException {
        /* no check */
    }
}

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
import java.util.Set;

import ome.model.internal.Details;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPolicy.Ability;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphTraversal;

/**
 * A {@link GraphTraversal.Processor} that does nothing whatsoever.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class NullGraphTraversalProcessor implements GraphTraversal.Processor {

    private final Set<GraphPolicy.Ability> requiredAbilities;

    /**
     * Construct a {@link GraphTraversal.Processor} that does nothing whatsoever.
     * @param requiredAbilities the {@link Ability} set to be returned by {@link #getRequiredPermissions()}
     */
    public NullGraphTraversalProcessor(Set<Ability> requiredAbilities) {
        this.requiredAbilities = requiredAbilities;
    }

    @Override
    public void nullProperties(String className, String propertyName, Collection<Long> ids) { }

    @Override
    public void deleteInstances(String className, Collection<Long> ids) { }

    @Override
    public void processInstances(String className, Collection<Long> ids) { }

    @Override
    public Set<Ability> getRequiredPermissions() {
        return requiredAbilities;
    }

    @Override
    public void assertMayProcess(String className, long id, Details details) throws GraphException { }
}

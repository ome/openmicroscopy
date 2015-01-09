/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
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

import java.util.List;

/**
 * Base {@link GraphStepFactory} which guarantees that
 * {@link GraphOpts.Op.REAP} processing takes place.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public abstract class AbstractStepFactory implements GraphStepFactory {

    protected int originalSize;

    public final GraphSteps postProcess(List<GraphStep> steps) {
        originalSize = steps.size();
        onPostProcess(steps);
        return new GraphSteps(steps);
    }

    protected void onPostProcess(List<GraphStep> steps) {
        // no-op
    }

}

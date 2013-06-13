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

import com.google.common.collect.HashMultimap;

/**
 * Base {@link GraphStepFactory} which guarantees that
 * {@link GraphOpts.Op.REAP} processing takes place.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0
 */
public abstract class AbstractStepFactory implements GraphStepFactory {

    protected HashMultimap<String, Long> reapTableIds = HashMultimap.create();

    protected int originalSize;

    public final List<GraphStep> postProcess(List<GraphStep> steps) {

        originalSize = steps.size();

        // Handle REAP
        for (int i = originalSize - 1; i >= 0; i--) {
            GraphStep step = steps.get(i);
            step.handleReap(reapTableIds);
        }

        onPostProcess(steps);

        return steps;
    }

    protected void onPostProcess(List<GraphStep> steps) {
        // no-op
    }

}

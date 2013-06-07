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

import ome.tools.hibernate.QueryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractHierarchyGraphSpec} specialized for only loading a single
 * /Fileset regardless of how many may exist at a given level.
 *
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0.0
 * @see IGraph
 */
public class FilesetGraphSpec extends BaseGraphSpec {

    private final static Logger log = LoggerFactory
            .getLogger(FilesetGraphSpec.class);

    /**
     * Creates a new instance.
     *
     * @param entries
     *            The entries to handle.
     */
    public FilesetGraphSpec(List<String> entries) {
        super(entries);
    }

    /**
     * Implements special logic for the synthetic paths from container objects
     * to Filesets.
     */
    @Override
    protected void walk(String[] sub, final GraphEntry entry, final QueryBuilder qb)
            throws GraphException {

        qb.from(sub[0], "ROOT0");
        if ("Dataset".equals(sub[0])) {
            joinDataset(qb, "ROOT0", "ROOT1");
        } else if ("Plate".equals(sub[0])) {
            joinPlate(qb, "ROOT0", "ROOT1");
        }
        // Start at 2 since ROOT1 is the Fileset we've already joined
        for (int p = 2; p < sub.length; p++) {
            String p_1 = sub[p - 1];
            String p_0 = sub[p];
            join(qb, p_1, "ROOT" + (p - 1), p_0, "ROOT" + p);
        }
    }
}
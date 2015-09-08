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

package ome.services.blitz.repo.path;

import java.util.List;
import java.util.Random;

import ome.services.util.SleepTimer;
import omero.ServerError;

/**
 * Abstracts a pattern for using repository template path directories in
 * {@link ome.services.blitz.repo.ManagedRepositoryI.TemplateDirectoryCreator)}:
 * pinpoint the next directory to use, then try to use it while watching for
 * conflicts from other similar threads.
 *
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.3
 */
public abstract class MakeNextDirectory {
    private Random rng = new Random();

    /**
     * Get the subdirectories to create for the given index,
     * the first directories to be created corresponding to {@code index == 0}.
     * @param index a non-negative index
     * @return the subdirectories for that index
     */
    public abstract List<String> getPathFor(long index);

    /**
     * If the circumstances (filesystem, etc.) are such that it is okay to use the given subdirectories.
     * @param path the subdirectories to possibly use
     * @return if the path may be used
     * @throws ServerError if the path could not be tested
     */
    public abstract boolean isAcceptable(List<String> path) throws ServerError;

    /**
     * Actually use the path. For instance, may create the directory or ensure that it exists.
     * @param path the subdirectories to use
     * @throws ServerError if the path could not be used
     */
    public abstract void usePath(List<String> path) throws ServerError;

    /**
     * Use the first acceptable path (that with the lowest {@code index} for {@link #getPathFor(long)})
     * and return the corresponding subdirectories.
     * @return the used subdirectories
     * @throws ServerError if the first acceptable path could not be found or used
     */
    public List<String> useFirstAcceptable() throws ServerError {
        /* highest unacceptable index found so far */
        Long inclusiveLower = null;
        /* lowest acceptable index found so far */
        Long exclusiveHigher = null;
        /* find and narrow bounds to pinpoint lowest acceptable index by binary search */
        long index;
        while (true) {
            final long toProbe;
            if (inclusiveLower == null) {
                /* no bounds yet */
                toProbe = 0;
                final List<String> path = getPathFor(toProbe);
                if (isAcceptable(path)) {
                    /* use the first of the directories */
                    index = toProbe;
                    break;
                } else {
                    /* found a lower bound */
                    inclusiveLower = toProbe;
                }
            } else if (exclusiveHigher == null) {
                /* only a lower bound, look further */
                toProbe = inclusiveLower == 0 ? 1 : inclusiveLower << 1;
                final List<String> path = getPathFor(toProbe);
                if (isAcceptable(path)) {
                    /* found upper bound */
                    exclusiveHigher = toProbe;
                } else {
                    /* moved lower bound */
                    inclusiveLower = toProbe;
                }
            } else if (exclusiveHigher - inclusiveLower < 2) {
                /* the tight bounds identify the next directory */
                index = exclusiveHigher;
                break;
            } else {
                /* tighten bounds */
                toProbe = (inclusiveLower + exclusiveHigher) >> 1;
                final List<String> path = getPathFor(toProbe);
                if (isAcceptable(path)) {
                    exclusiveHigher = toProbe;
                } else {
                    inclusiveLower = toProbe;
                }
            }
        }
        while (true) {
            List<String> path;
            while (true) {
                path = getPathFor(index);
                if (isAcceptable(path)) {
                    /* the path is ready for using */
                    break;
                } else {
                    /* the path has since been used, move on to the next increment */
                    index++;
                }
            }
            try {
                /* try using the next directory */
                usePath(path);
            } catch (ServerError e) {
                /* is another thread trying to use the same directory? give it time to finish up */
                SleepTimer.sleepFor(1000);
                if (isAcceptable(path)) {
                    /* an acceptable path could not be used so something bad happened */
                    throw e;
                } else {
                    /* try to fall out of synchronization with other threads in this same loop */
                    SleepTimer.sleepFor(rng.nextInt(500));
                    /* find the first non-existing directory and try again */
                    continue;
                }
            }
            /* the directory that was used */
            return path;
        }
    }
}

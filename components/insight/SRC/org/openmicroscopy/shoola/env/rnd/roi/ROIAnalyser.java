/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2017 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.rnd.roi;


import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.CollectionUtils;

import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DataSourceException;
import omero.gateway.facility.RawDataFacility;

import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

import omero.gateway.model.PixelsData;

/** 
 * Does some basic statistic analysis on a collection of {@link ROIShape} 
 * which all refer to the same pixels set.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ROIAnalyser
{

    /** 
     * Iterates an {@link ROIShape} over our pixels set.
     * Observers compute the statistics as the iteration moves forward.
     */
    private PointIterator runner;

    /** The number of z-sections. */
    private int sizeZ;

    /** The number of time-points. */
    private int sizeT;

    /** The number of channels. */
    private int sizeC;

    /**
     * Controls if the specified coordinates are valid.
     * Returns <code>true</code> if the passed values are in the correct ranges,
     * <code>false</code> otherwise.
     * 
     * @param z The z coordinate. Must be in the range <code>[0, sizeZ)</code>.
     * @param t The t coordinate. Must be in the range <code>[0, sizeT)</code>.
     * @return See above.
     */
    private boolean checkPlane(int z, int t)
    {
        if (z < 0 || sizeZ <= z) return false;
        if (t < 0 || sizeT <= t) return false;
        return true; 
    }
    
    /**
     * Checks if z and t match the given plane.
     * 
     * @param z
     *            The z plane to check
     * @param t
     *            The t plane to check
     * @param plane
     *            The plane to check against
     * @return See above
     */
    private boolean matchesPlane(int z, int t, Coord3D plane) {
        return (z == plane.getZSection() || plane.getZSection() < 0)
                && (t == plane.getTimePoint() || plane.getTimePoint() < 0);
    }

    /**
     * Controls if the specified channel is valid. 
     * Returns <code>true</code> if the passed value is in the correct range,
     * <code>false</code> otherwise.
     * 
     * @param w The w coordinate. Must be in the range <code>[0, sizeW)</code>.
     * @return See above.
     */
    private boolean checkChannel(int w)
    {
        return !(w < 0 || sizeC <= w);
    }

    /**
     * Creates a new instance to analyze the pixels set accessible through
     * <code>source</code>.
     * 
     * @param gateway Gateway to the raw data of the pixels set this iterator
     *               will work on. Mustn't be <code>null</code>.
     * @param pixels The pixels to analyze.
     * @throws ExecutionException If {@link RawDataFacility} can't be accessed
     */
    public ROIAnalyser(Gateway gateway, PixelsData pixels) throws ExecutionException
    {
        //Constructor will check source and dims.
        runner = new PointIterator(gateway, pixels);
        this.sizeZ = pixels.getSizeZ();
        this.sizeT = pixels.getSizeT();
        this.sizeC = pixels.getSizeC();
    }

    /**
     * Computes an {@link ROIShapeStats} object for each {@link ROIShape} 
     * specified
     * 
     * @param ctx The security context.
     * @param shapes The shapes to analyze.
     * @param channels Collection of selected channels.
     * @param plane The plane to analyze the shapes for, can be <code>null</code>
     * @return A map whose keys are the {@link ROIShape} objects specified
     *         and whose values are a map (keys: channel index, value
     *         the corresponding {@link AbstractROIShapeStats} objects computed by
     *         this method).
     * @throws DataSourceException  If an error occurs while retrieving plane
     *                              data from the pixels source.
     */
    public Map<ROIShape, Map<Integer, AbstractROIShapeStats>> analyze(
            SecurityContext ctx, ROIShape[] shapes,
            Collection<Integer> channels, Coord3D plane)
    throws DataSourceException
    {
        if (shapes == null) throw new NullPointerException("No shapes.");
        if (shapes.length == 0) 
            throw new IllegalArgumentException("No shapes defined.");
        if (CollectionUtils.isEmpty(channels))
            throw new IllegalArgumentException("No channels defined.");
        Map<ROIShape, Map<Integer, AbstractROIShapeStats>>
        r = new HashMap<ROIShape, Map<Integer, AbstractROIShapeStats>>();
        AbstractROIShapeStats computer;
        Map<Integer, AbstractROIShapeStats> stats;
        Iterator<Integer> j;
        int n = channels.size();
        Integer w;
        ROIShape shape;
        boolean close = false;
        for (int i = 0; i < shapes.length; ++i) {
            shape = shapes[i];
            close = i == shapes.length-1;
            if (checkPlane(shape.getZ(), shape.getT())) {
                stats = new HashMap<Integer, AbstractROIShapeStats>(n);
                if (plane == null || matchesPlane(shape.getZ(), shape.getT(), plane)) {
                    j = channels.iterator();
                    List<Point> points = shape.getFigure().getPoints();
                    int count = 0;
                    boolean last = false;
                    while (j.hasNext()) {
                        w = j.next();
                        if (checkChannel(w.intValue())) {
                            computer =  new ROIShapeStatsSimple();
                            runner.register(computer);
                            if (close) {
                                last = count == channels.size()-1;
                            }
                            runner.iterate(ctx, shape, points, w.intValue(), last);
                            runner.remove(computer);
                            stats.put(w, computer);
                        }
                        count++;
                    }
                }
                r.put(shape, stats);
            }
        }
        return r;
    }

}

/*
 * org.openmicroscopy.shoola.env.rnd.roi.PointIterator 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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

//Java imports
import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.data.Plane2D;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

/** 
 * Iterates over the pixels contained in an <code>ROIShape</code> or an 
 * <code>ROI</code>.
 * The iteration advances in  point by point through each
 * {@link ROIFigure} contained in the {@link ROIShape} &#151; the image data is 
 * obviously assumed to be in XYZWT order. Each instance of this class is
 * bound to a given pixels set; however the same instance can be used to iterate
 * multiple {@link ROIShape}s over said pixels set.  
 * {@link PointIteratorObserver}s
 * are attached to an instance of this class before an iteration starts so to 
 * get notified of every iterated pixels value.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class PointIterator
{

    /** Gateway to the raw data of the pixels set bound to this iterator. */
    private DataSink source;

    /** The number of z-sections. */
    private int sizeZ;

    /** The number of timepoints. */
    private int sizeT;

    /** The number of pixels along the x-axis. */
    private int sizeX;

    /** The number of pixels along the y-axis. */
    private int sizeY;

    /** The number of channels. */
    private int sizeC;

    /** 
     * All currently registered {@link PointIteratorObserver}s.
     * This is a set (no duplicates) and mustn't contain <code>null</code>s.
     */
    private Set<PointIteratorObserver> observers;

    /** Notifies all observers, if any, that the iteration has started. */
    private void notifyIterationStart()
    {
        PointIteratorObserver obs;
        Iterator<PointIteratorObserver> i = observers.iterator();
        while (i.hasNext()) {
            obs =i.next();
            obs.iterationStarted();
        }
    }

    /** Notifies all observers, if any, that the iteration has finished. */
    private void notifyIterationEnd()
    {
        PointIteratorObserver obs;
        Iterator<PointIteratorObserver> i = observers.iterator();
        while (i.hasNext()) {
            obs = i.next();
            obs.iterationFinished();
        }
    }

    /**
     * Notifies all observers, if any, that a 2D selection within a given plane
     * is about to be iterated.
     * 
     * @param z The z coordinate (stack frame) of the plane.
     * @param w The w coordinate (channel) of the plane.
     * @param t The t coordinate (timepoint) of the plane.
     * @param pointsCount How many points are contained in the selection that
     *                    is about to be iterated.
     */
    private void notifyPlaneStart(int z, int w, int t, int pointsCount)
    {
        PointIteratorObserver obs;
        Iterator<PointIteratorObserver> i = observers.iterator();
        while (i.hasNext()) {
            obs = i.next();
            obs.onStartPlane(z, w, t, pointsCount);
        }
    }

    /**
     * Notifies all observers, if any, of the current pixel being iterated.
     * 
     * @param pixelValue The value of the current pixel.
     * @param z The z coordinate (stack frame) of the plane.
     * @param w The w coordinate (channel) of the plane.
     * @param t The t coordinate (timepoint) of the plane.
     * @param loc The location of the pixelValue on the 2D-selection.
     */
    private void notifyValue(double pixelValue, int z, int w, int t, 
            Point loc)
    {
        PointIteratorObserver obs;
        Iterator<PointIteratorObserver> i = observers.iterator();
        while (i.hasNext()) {
            obs = i.next();
            obs.update(pixelValue, z, w, t, loc);
        }
    }

    /**
     * Notifies all observers, if any, that a 2D selection within a given plane
     * has been iterated.
     * 
     * @param z The z coordinate (stack frame) of the plane.
     * @param w The w coordinate (channel) of the plane.
     * @param t The t coordinate (timepoint) of the plane.
     * @param pointsCount How many points are contained in the selection that
     *                   has been iterated.
     */
    private void notifyPlaneEnd(int z, int w, int t, int pointsCount)
    {
        PointIteratorObserver obs;
        Iterator<PointIteratorObserver> i = observers.iterator();
        while (i.hasNext()) {
            obs = i.next();
            obs.onEndPlane(z, w, t, pointsCount);
        }
    }


    /**
     * Returns <code>true</code> if the passed coordinates are valid,
     * <code>false</code> otherwise.
     * 
     * @param x The x-coordinate of the point.
     * @param y The y-coordinate of the point.
     * @return See above.
     */
    private boolean isValidPoint(int x, int y)
    {
        if (x < 0 || x >= sizeX) return false;
        if (y < 0 || y >= sizeY) return false;
        return true;
    }

    /**
     * Creates a new instance to iterate over the pixels set accessible through
     * <code>source</code>.
     * 
     * @param source Gateway to the raw data of the pixels set this iterator
     *               will work on. Mustn't be <code>null</code>.
     * @param sizeZ The number of z-sections.
     * @param sizeT The number of timepoints.
     * @param sizeC The number of channels.
     * @param sizeX The number of pixels along the x-axis.
     * @param sizeY The number of pixels along the y-axis.
     */
    PointIterator(DataSink source, int sizeZ, int sizeT, int sizeC, int sizeX, 
            int sizeY)
    {
        if (source == null) throw new NullPointerException("No source.");
        this.source = source;
        this.sizeZ = sizeZ;
        this.sizeC = sizeC;
        this.sizeT = sizeT;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        observers = new HashSet<PointIteratorObserver>();
    }

    /**
     * Adds an iteration observer to the notification list.
     * This method will do nothing if the specified observer is already in the
     * list. This means it is not possible for an observer to subscribe twice.
     * 
     * @param observer  The observer to add. Mustn't be <code>null</code>.
     */
    void register(PointIteratorObserver observer)
    {
        if (observer == null) throw new NullPointerException("No observer.");
        observers.add(observer);
    }

    /**
     * Removes an iteration observer from the notification list.
     * 
     * @param observer  The observer to remove. Mustn't be <code>null</code>.
     */
    void remove(PointIteratorObserver observer)
    {
        if (observer == null) throw new NullPointerException("No observer.");
        observers.remove(observer); 
    }

    /** Removes all iteration observers from the notification list. */
    void clearNotificationList()
    { 
        observers = new HashSet<PointIteratorObserver>();
    }

    /**
     * Iterates over the pixels contained in <code>roi</code>.
     * The pixel values come from the pixels set that was bound to this
     * iterator at creation time.
     * All registered {@link PointIteratorObserver}s get notified of every
     * iterated pixels value. 
     * 
     * @param ctx The security context.
     * @param shape The shape to analyze. Mustn't be <code>null</code>.
     * @param points The collection of points contained in the shape.
     * @param w The selected channel.
     * @param close Pass <code></code>
     * @throws DataSourceException If an error occurs while retrieving plane
     *                             data from the pixels source.
     */
    public void iterate(SecurityContext ctx, ROIShape shape, List<Point> points,
            int w, boolean close)
    throws DataSourceException
    {
        if (shape == null) throw new NullPointerException("No shapes.");
        if (w < 0 || w >= sizeC) 
            throw new NullPointerException("Channel not valid.");
        notifyIterationStart();
        try { 
            int z = shape.getZ();
            int t = shape.getT();
            if (z >= 0 && z < sizeZ && t >= 0 && t < sizeT) {
                notifyPlaneStart(z, w, t, points.size());
                Plane2D data = source.getPlane(ctx, z, t, w, close);
                double value;
                int length = 0;
                int x1, x2;
                Iterator<Point> i = points.iterator();
                Point p;
                while (i.hasNext()) {
                    p = i.next();
                    x1 = p.x;
                    x2 = p.y;
                    if (isValidPoint(x1, x2)) {
                        value = data.getPixelValue(x1, x2);
                        notifyValue(value, z, w, t, p);
                        length++;
                    }
                }
                notifyPlaneEnd(z, w, t, length);
            }
        } finally {  
            //Give the observers a chance to clean up even when 
            //something goes wrong. 
            notifyIterationEnd();
        }
    }
    /**
     * Iterates over the pixels contained in <code>roi</code>.
     * The pixel values come from the pixels set that was bound to this
     * iterator at creation time.
     * All registered {@link PointIteratorObserver}s get notified of every
     * iterated pixels value. 
     * 
     * @param ctx The security context.
     * @param shape The shape to analyze. Mustn't be <code>null</code>.
     * @param points The collection of points contained in the shape.
     * @param w The selected channel.
     * @throws DataSourceException If an error occurs while retrieving plane
     *                             data from the pixels source.
     */
    public void iterate(SecurityContext ctx, ROIShape shape, List<Point> points,
            int w)
    throws DataSourceException
    {
        iterate(ctx, shape, points, w, true);
    }

}

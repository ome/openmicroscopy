/*
 * org.openmicroscopy.shoola.env.rnd.roi.PointIterator
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.rnd.roi;


//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.data.Plane2D;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.util.image.roi.ROI5D;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;

/** 
 * Iterates over the pixels contained in an {@link ROI5D}.
 * The iteration advances in ZWT order, point by point through each
 * {@link PlaneArea} contained in the {@link ROI5D} &#151; the image data is 
 * obviously assumed to be in XYZWT order.  Each instance of this class is
 * bound to a given pixels set; however the same instance can be used to iterate
 * multiple {@link ROI5D}s over said pixels set.  {@link PointIteratorObserver}s
 * are attached to an instance of this class before an iteration starts so to 
 * get notified of every iterated pixels value.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class PointIterator
{
    
    /** Gateway to the raw data of the pixels set bound to this iterator. */
    private DataSink            source;
    
    /** The dimensions of the pixels set this iterator is working on. */
    private PixelsDimensions    dims;
    
    /** 
     * All currently registered {@link PointIteratorObserver}s.
     * This is a set (no duplicates) and mustn't contain <code>null</code>s.
     */
    private Set                 observers;
    
    
    /**
     * Notifies all observers, if any, that the iteration has started.
     */
    private void notifyIterationStart()
    {
        PointIteratorObserver obs;
        Iterator i = observers.iterator();
        while (i.hasNext()) {
            obs = (PointIteratorObserver) i.next();
            obs.iterationStarted();
        }
    }
    
    /**
     * Notifies all observers, if any, that the iteration has finsihed.
     */
    private void notifyIterationEnd()
    {
        PointIteratorObserver obs;
        Iterator i = observers.iterator();
        while (i.hasNext()) {
            obs = (PointIteratorObserver) i.next();
            obs.iterationFinished();
        }
    }
    
    /**
     * Notifies all observers, if any, that a 2D selection within a given plane
     * is about to be iterated.
     * 
     * @param z  The z coord (stack frame) of the plane.
     * @param w  The w coord (channel) of the plane.
     * @param t  The t coord (timepoint) of the plane.
     * @param pointsCount  How many points are contained in the selection that
     *                      is about to be iterated.
     */
    private void notifyPlaneStart(int z, int w, int t, int pointsCount)
    {
        PointIteratorObserver obs;
        Iterator i = observers.iterator();
        while (i.hasNext()) {
            obs = (PointIteratorObserver) i.next();
            obs.onStartPlane(z, w, t, pointsCount);
        }
    }
    
    /**
     * Called to notify the observers of the current pixel being iterated.
     * 
     * @param pixelValue  The value of the current pixel. 
     * @param z  The index of the plane in the stack.
     * @param w  The index of the channel at which the plane must be taken.
     * @param t  The timepoint index.
     */
    private void notifyValue(double pixelValue, int z, int w, int t)
    {
        PointIteratorObserver obs;
        Iterator i = observers.iterator();
        while (i.hasNext()) {
            obs = (PointIteratorObserver) i.next();
            obs.update(pixelValue, z, w, t);
        }
    }
    
    /**
     * Notifies all observers, if any, that a 2D selection within a given plane
     * has been iterated.
     * 
     * @param z  The z coord (stack frame) of the plane.
     * @param w  The w coord (channel) of the plane.
     * @param t  The t coord (timepoint) of the plane.
     * @param pointsCount  How many points are contained in the selection that
     *                      has been iterated.
     */
    private void notifyPlaneEnd(int z, int w, int t, int pointsCount)
    {
        PointIteratorObserver obs;
        Iterator i = observers.iterator();
        while (i.hasNext()) {
            obs = (PointIteratorObserver) i.next();
            obs.onEndPlane(z, w, t, pointsCount);
        }
    }
    
    /**
     * Iterates over the pixel values within the specified 2D selection.
     * 
     * @param points  The coords of all points within the 2D selection.
     * @param pd  Identifies the plane on which the selection lies.
     * @param z  The index of the plane in the stack.
     * @param w  The index of the channel at which the plane must be taken.
     * @param t  The timepoint index.
     * @throws DataSourceException  If an error occurs while retrieving the
     *                              plane data from the pixels source.
     */
    private void iterateArea(PlanePoint[] points, PlaneDef pd, 
                                int z, int w, int t) 
        throws DataSourceException
    {
        Plane2D data = source.getPlane2D(pd, w);
        double value;
        for (int i = 0; i < points.length; ++i) {
            value = data.getPixelValue((int) points[i].x1, (int) points[i].x2);
            notifyValue(value, z, w, t);
        }    
    }
    
    /**
     * Creates a new instance to iterate over the pixels set accessible through
     * <code>source</code>.
     * 
     * @param source   Gateway to the raw data of the pixels set this iterator
     *                  will work on.  Mustn't be <code>null</code>.
     * @param dims     The dimensions of the pixels set.  Mustn't be 
     *                  <code>null</code>.
     */
    public PointIterator(DataSink source, PixelsDimensions dims)
    {
        if (source == null) throw new NullPointerException("No source.");
        if (dims == null) throw new NullPointerException("No dimensions.");
        this.source = source;
        this.dims = dims;
        observers = new HashSet();
    }
    
    /**
     * Adds an iteration observer to the notification list.
     * This method will do nothing if the specified observer is already in the
     * list.  This means it's not possible for an observer to subscribe twice.
     * 
     * @param observer  The observer to add.  Mustn't be <code>null</code>. 
     */
    public void register(PointIteratorObserver observer)
    {
        if (observer == null) throw new NullPointerException("No observer.");
        observers.add(observer);
    }
    
    /**
     * Removes an iteration observer from the notification list.
     * 
     * @param observer  The observer to remove.  Mustn't be <code>null</code>.
     */
    public void remove(PointIteratorObserver observer)
    {
        if (observer == null) throw new NullPointerException("No observer.");
        observers.remove(observer); 
    }
    
    /**
     * Removes all iteration observers from the notification list.
     */
    public void clearNotificationList()
    {
        observers = new HashSet(); 
    }
    
    /**
     * Iterates over the pixels contained in <code>roi</code>.
     * The pixel values come from the pixels set that was bound to this
     * iterator at creation time.
     * All registered {@link PointIteratorObserver}s get notified of every
     * iterated pixels value. 
     * 
     * @param roi   The 5D selection to iterate.  Mustn't be <code>null</code>.
     * @throws DataSourceException  If an error occurs while retrieving plane
     *                              data from the pixels source.
     */
    public void iterate(ROI5D roi) 
        throws DataSourceException
    {
        if (roi == null) throw new NullPointerException("No ROI.");
        PlaneArea selection2D;
        PlanePoint[] points;
        PlaneDef pd;
        notifyIterationStart();
        try {  //Iterate in ZWT order and notify observers.
            for (int t = 0; t < dims.sizeT; ++t) {
                pd = new PlaneDef(PlaneDef.XY, t);
                for (int w = 0; w < dims.sizeW; ++w) {
                    for (int z = 0; z < dims.sizeZ; ++z) {
                        selection2D = roi.getPlaneArea(z, t, w);
                        points = selection2D.getPoints();
                        notifyPlaneStart(z, w, t, points.length);
                        pd.setZ(z);
                        iterateArea(points, pd, z, w, t);
                        notifyPlaneEnd(z, w, t, points.length);
                    }
                }
            }
        } finally {  
            //Give the observers a chance to clean up even when 
            //something goes wrong. 
            notifyIterationEnd();
        }
    }

}

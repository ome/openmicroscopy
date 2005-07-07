/*
 * org.openmicroscopy.shoola.env.rnd.StatsComputer
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

package org.openmicroscopy.shoola.env.rnd;




//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.data.Plane2D;
import org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.metadata.MetadataSource;
import org.openmicroscopy.shoola.env.rnd.metadata.MetadataSourceException;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsGlobalStatsEntry;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStats;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;
import org.openmicroscopy.shoola.util.math.geom2D.Segment;

/** 
 * TEMPORARY CLASS, SHOULD'T CODE AGAINST IT.
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
class StatsComputer
{
    
    /** Defines the number of sub-intervals. */
    private static final int    NB_BIN = 20; // >=3
    
    private static final int    EPSILON = 4;
    
    private static final double THRESHOLD = 0.99;
    
    private static final double NR_THRESHOLD = 0.95;
    
    static void computeStats(DataSink dataSink, MetadataSource source, 
                            ChannelBindings[] cBindings, PlaneDef planeDef, 
                            boolean flag)
        throws MetadataSourceException
    {
        Plane2D wData;
        PixelsDimensions pixelsDims = source.getPixelsDims();
        PixelsStats pixelsStats = source.getPixelsStats();
        try {
            for (int i = 0; i < cBindings.length; i++) {
                wData = dataSink.getPlane2D(planeDef, i);
                computeDefaultWindow(i, cBindings[i], wData, pixelsStats, 
                                    pixelsDims.sizeY, pixelsDims.sizeX, flag);
            } 
        } catch (DataSourceException e) {
            throw new MetadataSourceException("Cannot compute default interval",
                    e);
        }
    }
    
    /** Compute the default interval. Should be done at import time. */
    private static void computeDefaultWindow(int index, ChannelBindings wave,
            Plane2D plane, PixelsStats stats, int sizeX2, int sizeX1, 
            boolean flag)
        throws DataSourceException
    {
        PixelsGlobalStatsEntry wGlobal;
        double gMin, gMax;
        wGlobal = stats.getGlobalEntry(index);
        gMin = wGlobal.getGlobalMin();
        gMax = wGlobal.getGlobalMax();
        if (gMax-gMin >= NB_BIN && NB_BIN > 2) 
            computeWindow(plane, sizeX2, sizeX1, gMax, gMin, wave, flag);
    }
    
    /** Compute the interval. */
    private static void computeWindow(Plane2D plane, int sizeX2, int sizeX1,
                        double max, double min, ChannelBindings wave, 
                        boolean flag)
        throws DataSourceException
    {
        double sizeBin = (max-min)/NB_BIN, epsilon = sizeBin/EPSILON;
        int[] totals = new int[NB_BIN];
        double[] stats = new double[NB_BIN];
        PlanePoint o, e;
        Segment[] segments = new Segment[NB_BIN];
        for (int i = 0; i < NB_BIN; i++) {
            o = new PlanePoint(min+i*sizeBin, 0);
            e = new PlanePoint(min+(i+1)*sizeBin, 0);
            segments[i] = new Segment(o, e);
        }
        PlanePoint point;
        //checj segment [o,e[
        for (int x2 = 0; x2 < sizeX2; ++x2) {
            for (int x1 = 0; x1 < sizeX1; ++x1) {
                point = new PlanePoint(plane.getPixelValue(x1, x2), 0);
                for (int i = 0; i < segments.length; i++) {
                    if (segments[i].lies(point) && 
                            !segments[i].getPoint(1).equals(point)) {
                        totals[i]++;
                        break;
                    }
                } // end i
            } //end x1
        }// end x2
        
        double total = sizeX2*sizeX1;
        for (int i = 0; i < totals.length; i++) 
            stats[i] = totals[i]/total;
        //Default, we assume that we have at least 3 sub-intervals.
        double start = segments[0].getPoint(1).x1;
        double end = segments[NB_BIN-1].getPoint(1).x1;;
        total = total-totals[0]-totals[NB_BIN-1];
        
        if (totals[0] >= totals[NB_BIN-1])
            end = accumulateMin(totals, segments, total, epsilon);
        else
            start = accumulateMax(totals, segments, total, epsilon);
        wave.setStats(stats);
        wave.setNoiseReduction(noiseReduction(stats));
        if (flag) wave.setInputWindow(start, end);
    }

    private static boolean noiseReduction(double[] stats)
    {
        double sumMin = 0, sumMax = 0;
        for (int i = 0; i < stats.length; i++) {
            if (i < 2) sumMin += stats[i];
            if (i >= stats.length-2) sumMax += stats[i];
        }
        boolean nr = true;
        if (sumMin >= NR_THRESHOLD || sumMax >= NR_THRESHOLD) nr = false;
        return nr;
    }
    
    /** Value accumulate closed to the Min. */
    private static double accumulateMin(int[] totals, Segment[] segments, 
                                        double total, double epsilon)
    {
        double e = segments[NB_BIN-1].getPoint(1).x1, sum = 0;
        for (int i = 1; i < totals.length-1; i++) {
            sum += totals[i];
            if (sum/total > THRESHOLD) {
                e = segments[i].getPoint(1).x1+epsilon;
                break;
            }
        }
        return e;
    }
    
    /** Value accumulate closed to the Max. */
    private static double accumulateMax(int[] totals, Segment[] segments, 
                                    double total, double epsilon)
    {
        double s = segments[0].getPoint(1).x1, sum = 0;
        for (int i = totals.length-2; i > 0; i--) {
            sum += totals[i];
            if (sum/total > THRESHOLD) {
                s = segments[i].getPoint(1).x1-epsilon;
                break;
            }
        }
        return s;
    }
    
}
